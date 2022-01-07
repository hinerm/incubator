/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2018 ImageJ developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package org.scijava.ops.engine.impl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.scijava.Priority;
import org.scijava.discovery.Discoverer;
import org.scijava.discovery.ManualDiscoverer;
import org.scijava.log2.Logger;
import org.scijava.ops.api.*;
import org.scijava.ops.api.features.*;
import org.scijava.ops.api.features.BaseOpHints.Adaptation;
import org.scijava.ops.api.features.BaseOpHints.DependencyMatching;
import org.scijava.ops.api.features.BaseOpHints.History;
import org.scijava.ops.api.features.BaseOpHints.Simplification;
import org.scijava.ops.engine.hint.DefaultHints;
import org.scijava.ops.engine.matcher.impl.DefaultOpMatcher;
import org.scijava.ops.engine.matcher.impl.DefaultOpRef;
import org.scijava.ops.engine.matcher.impl.InfoMatchingOpRef;
import org.scijava.ops.engine.matcher.impl.OpClassInfo;
import org.scijava.ops.engine.struct.FunctionalParameters;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpDependency;
import org.scijava.struct.FunctionalMethodType;
import org.scijava.struct.ItemIO;
import org.scijava.types.Nil;
import org.scijava.types.TypeReifier;
import org.scijava.types.Types;
import org.scijava.util.VersionUtils;

/**
 * Default implementation of {@link OpEnvironment}, whose ops and related state
 * are discovered from a SciJava application context.
 * 
 * @author Curtis Rueden
 */
public class DefaultOpEnvironment implements OpEnvironment {

	private final List<Discoverer> discoverers;

	private final ManualDiscoverer manDiscoverer;

	private final OpMatcher matcher;

	private final Logger log;

	private final TypeReifier typeService;

	private final OpHistory history;

	/**
	 * Data structure storing all known Ops, grouped by name. This reduces the
	 * search size for any Op request to the number of known Ops with the name
	 * given in the request.
	 */
	private Map<String, Set<OpInfo>> opDirectory;

	/**
	 * Data structure storing all known Ops, discoverable using their id.
	 */
	private Map<String, OpInfo> idDirectory;

	/**
	 * Map containing pairs of {@link MatchingConditions} (i.e. the {@link OpRef}
	 * and {@link Hints} used to find an Op) and the {@link OpInstance} (wrapping an Op
	 * with its backing {@link OpInfo}) that matched those requests. Used to
	 * quickly return Ops when the matching conditions are identical to those of a
	 * previous call.
	 *
	 * @see MatchingConditions#equals(Object)
	 */
	private Map<MatchingConditions, OpInstance<?>> opCache;

	/**
	 * Data structure storing all known {@link OpWrapper}s. Each {@link OpWrapper}
	 * is retrieved by providing the {@link Class} that it is able to wrap.
	 */
	private Map<Class<?>, OpWrapper<?>> wrappers;

	/**
	 * Data structure storing this Environment's {@link Hints}. NB whenever this
	 * Object is used, <b>a copy should be made</b> to prevent concurrency issues.
	 */
	private Hints environmentHints = null;

	public DefaultOpEnvironment(final TypeReifier typeService,
		final Logger log, final OpHistory history,
		final List<Discoverer> discoverers)
	{
		this.discoverers = new ArrayList<>(discoverers);
		this.manDiscoverer = new ManualDiscoverer();
		this.discoverers.add(this.manDiscoverer);
		this.typeService = typeService;
		this.log = log;
		this.history = history;
		matcher = new DefaultOpMatcher(getMatchingRoutines(this.discoverers));
	}

	public DefaultOpEnvironment(final TypeReifier typeService,
		final Logger log, final OpHistory history,
		final Discoverer... d)
	{
		this(typeService, log, history, Arrays.asList(d));
	}

	public static List<MatchingRoutine> getMatchingRoutines(
		final List<Discoverer> discoverers)
	{
		List<MatchingRoutine> matchers = new ArrayList<>();
		for (Discoverer d : discoverers) {
			List<MatchingRoutine> routines = d.discover(MatchingRoutine.class);
			matchers.addAll(routines);
		}
		return matchers;
	}

	@Override
	public Set<OpInfo> infos() {
		if (opDirectory == null) initOpDirectory();
		return opDirectory.values().stream().flatMap(Collection::stream).collect(
			Collectors.toSet());
	}

	@Override
	public Set<OpInfo> infos(String name) {
		if (opDirectory == null) initOpDirectory();
		if (name == null || name.isEmpty()) return infos();
		return opsOfName(name);
	}

	@Override
	public Set<OpInfo> infos(Hints hints) {
		return filterInfos(infos(), hints);
	}

	@Override
	public Set<OpInfo> infos(String name, Hints hints) {
		return filterInfos(infos(name), hints);
	}

	private Set<OpInfo> filterInfos(Set<OpInfo> infos, Hints hints) {
		boolean adapting = hints.contains(Adaptation.IN_PROGRESS);
		boolean simplifying = hints.contains(Simplification.IN_PROGRESS);
		// if we aren't doing any
		if (!(adapting || simplifying)) return infos;
		return infos.stream() //
			// filter out unadaptable ops
			.filter(info -> !adapting || !info.declaredHints().contains(
				Adaptation.FORBIDDEN)) //
			// filter out unadaptable ops
			.filter(info -> !simplifying || !info.declaredHints().contains(
				Simplification.FORBIDDEN)) //
			.collect(Collectors.toSet());
	}

	@Override
	public <T> T op(final String opName, final Nil<T> specialType,
		final Nil<?>[] inTypes, final Nil<?> outType)
	{
		return op(opName, specialType, inTypes, outType, getDefaultHints());
	}

	@Override
	public <T> T op(final String opName, final Nil<T> specialType,
		final Nil<?>[] inTypes, final Nil<?> outType, Hints hints)
	{
		return findOp(opName, specialType, inTypes, outType, hints).asOpType();
	}

	@Override
	public InfoChain infoChain(String opName, Nil<?> specialType,
		Nil<?>[] inTypes, Nil<?> outType)
	{
		return infoChain(opName, specialType, inTypes, outType, getDefaultHints());
	}

	@Override
	public InfoChain infoChain(String opName, Nil<?> specialType,
		Nil<?>[] inTypes, Nil<?> outType, Hints hints)
	{
		try {
			return findOp(opName, specialType, inTypes, outType, hints).infoChain();
		}
		catch (OpMatchingException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public InfoChain chainFromInfo(OpInfo info, Nil<?> specialType) {
		return findOp(info, specialType, getDefaultHints()).infoChain();
	}

	@Override
	public InfoChain chainFromInfo(OpInfo info, Nil<?> specialType, Hints hints) {
		return findOp(info, specialType, hints).infoChain();
	}

	@Override
	public <T> T opFromSignature(final String signature,
		final Nil<T> specialType)
	{
		InfoChain info = chainFromID(signature);
		return opFromInfoChain(info, specialType);
	}

	@Override
	public <T> T opFromInfoChain(final InfoChain chain,
		final Nil<T> specialType)
	{
		if (!(specialType.getType() instanceof ParameterizedType))
			throw new IllegalArgumentException("TODO");
		@SuppressWarnings("unchecked")
		OpInstance<T> instance = (OpInstance<T>) chain.op(specialType.getType());
		Hints hints = getDefaultHints();
		RichOp<T> wrappedOp = wrapOp(instance, hints);
		return wrappedOp.asOpType();

	}

	@Override
	public InfoChain chainFromID(String signature) {
		if (idDirectory == null) initIdDirectory();
		List<InfoChainGenerator> infoChainGenerators = discoverers.stream() //
				.flatMap(d -> d.discover(InfoChainGenerator.class).stream()) //
				.collect(Collectors.toList());

		InfoChainGenerator genOpt = InfoChainGenerator.findSuitableGenerator(
			signature, infoChainGenerators);
		return genOpt.generate(signature, idDirectory, infoChainGenerators);
	}

	@Override
	public Type genericType(Object obj) {
		return typeService.reify(obj);
	}

	@Override
	public OpInfo opify(final Class<?> opClass) {
		return opify(opClass, Priority.NORMAL);
	}

	@Override
	public OpInfo opify(final Class<?> opClass, final double priority,
		final String... names)
	{
		return new OpClassInfo(opClass, VersionUtils.getVersion(opClass), new DefaultHints(), priority,
			names);
	}

	@Override
	public <T> T bakeLambdaType(T op, Type type) {
		return LambdaTypeBaker.bakeLambdaType(op, type);
	}

	@Override
	public void register(Object... objects) {
		for (Object o : objects) {
			if (o.getClass().isArray())
				register(o);
			else if (o instanceof Iterable<?>) {
				((Iterable<?>) o).forEach(this::register);
			}
			else
				this.manDiscoverer.register(o);
		}
	}
	
	@Override
	public Collection<OpInfo> infosFrom(Object o) {
		return infoGenerators().parallelStream() //
				.filter(g -> g.canGenerateFrom(o)) //
				.flatMap(g -> g.generateInfosFrom(o).stream()) //
				.collect(Collectors.toSet());
	}

	@SuppressWarnings("unchecked")
	private <T> RichOp<T> findOp(final String opName, final Nil<T> specialType,
		final Nil<?>[] inTypes, final Nil<?> outType, Hints hints)
	{
		final OpRef ref = DefaultOpRef.fromTypes(opName, specialType.getType(),
			outType != null ? outType.getType() : null, toTypes(inTypes));
		MatchingConditions conditions = generateCacheHit(ref, hints);
		return (RichOp<T>) wrapViaCache(conditions);
	}

	@SuppressWarnings("unchecked")
	private <T> OpInstance<T> findOp(final OpInfo info, final Nil<T> specialType,
		Hints hints) throws OpMatchingException
	{
		OpRef ref = new InfoMatchingOpRef(info, specialType);
		MatchingConditions conditions = insertCacheHit(ref, hints, info);
		return (OpInstance<T>) getInstance(conditions);
	}

	private Type[] toTypes(Nil<?>... nils) {
		return Arrays.stream(nils) //
				.filter(Objects::nonNull) //
				.map(Nil::getType) //
				.toArray(Type[]::new);
	}

	/**
	 * Creates an Op instance from an {@link OpInfo} with the provided
	 * {@link MatchingConditions} as the guidelines for {@link OpInfo} selection.
	 * This Op instance is put into the {@link #opCache}, and is retrievable via
	 * {@link DefaultOpEnvironment#wrapViaCache(MatchingConditions)}
	 * 
	 * @param ref the {@link OpRef} request
	 * @param hints the {@link Hints} containing matching preferences
	 * @param info the {@link OpInfo} describing the Op that should match these
	 *          conditions1
	 * @return the {@link MatchingConditions} that will return the Op described by
	 *         {@code info} from the op cache
	 */
	private MatchingConditions insertCacheHit(final OpRef ref, final Hints hints,
		final OpInfo info)
	{
		MatchingConditions conditions = MatchingConditions.from(ref, hints);

		// create new OpCandidate from ref and info
		OpCandidate candidate = new OpCandidate(this, ref, info);

		instantiateAndCache(conditions, candidate);

		return conditions;
	}

	/**
	 * Finds an Op instance matching the request described by {@link OpRef}
	 * {@code ref} and stores this Op in {@link #opCache}. NB the return must be an
	 * {@link Object} here (instead of some type variable T where T is the Op
	 * type} since there is no way to ensure that the {@code OpRef} can provide
	 * that T (since the OpRef could require that the Op returned is of multiple
	 * types).
	 * 
	 * @param ref the {@link OpRef} request
	 * @param hints the {@link Hints} containing matching preferences
	 * @return the {@link MatchingConditions} that will return the Op found from
	 *         the op cache
	 */
	private MatchingConditions generateCacheHit(OpRef ref, Hints hints) {
		MatchingConditions conditions = MatchingConditions.from(ref, hints);
		// see if the ref has been matched already
		OpInstance<?> cachedOp = getInstance(conditions);
		if (cachedOp != null) return conditions;

		// obtain suitable OpCandidate
		OpCandidate candidate = findOpCandidate(conditions.ref(), conditions
			.hints());

		instantiateAndCache(conditions, candidate);

		return conditions;
	}

	private void instantiateAndCache(MatchingConditions conditions,
		OpCandidate candidate)
	{
		// obtain Op instance (with dependencies)
		OpInstance<?> op = instantiateOp(candidate, conditions.hints());

		// cache instance
		opCache.putIfAbsent(conditions, op);
	}

	private OpInstance<?> getInstance(MatchingConditions conditions) {
		if (opCache == null) {
			opCache = new HashMap<>();
		}
		return opCache.get(conditions);
	}

	private OpCandidate findOpCandidate(OpRef ref, Hints hints) {
		return matcher.match(MatchingConditions.from(ref, hints), this);
	}

	/**
	 * Creates an instance of the Op from the {@link OpCandidate} <b>with its
	 * required {@link OpDependency} fields</b>.
	 * 
	 * @param candidate the {@link OpCandidate} to be instantiated
	 * @param hints the {@link Hints} to use in instantiation
	 * @return an Op with all needed dependencies
	 */
	private OpInstance<?> instantiateOp(final OpCandidate candidate,
		Hints hints)
	{
		final List<RichOp<?>> conditions = resolveOpDependencies(candidate, hints);
		InfoChain adaptorChain = new DependencyRichOpInfoChain(candidate
			.opInfo(), conditions);
		return adaptorChain.op(candidate.getType());
	}

	/**
	 * Wraps the matched op into an Op that knows its generic typing.
	 * 
	 * @param instance - the {@link OpInstance} to wrap.
	 * @param hints - the {@link Hints} used to create the {@link OpInstance}
	 * @return an Op wrapping of op.
	 */
	@SuppressWarnings("unchecked")
	private <T> RichOp<T> wrapOp(OpInstance<T> instance, Hints hints)
		throws IllegalArgumentException
	{
		if (wrappers == null) initWrappers();

		try {
			// find the opWrapper that wraps this type of Op
			Class<?> wrapper = getWrapperClass(instance.op(), instance.infoChain()
				.info());
			// obtain the generic type of the Op w.r.t. the Wrapper class
			Type reifiedSuperType = Types.getExactSuperType(instance.getType(),
				wrapper);
			OpMetadata metadata = new OpMetadata(reifiedSuperType, instance
				.infoChain(), hints, history);
			// wrap the Op
			final OpWrapper<T> opWrapper = (OpWrapper<T>) wrappers.get(Types.raw(
				reifiedSuperType));
			return opWrapper.wrap(instance, metadata);
		}
		catch (IllegalArgumentException | SecurityException exc) {
			throw new IllegalArgumentException(exc.getMessage() != null ? exc
				.getMessage() : "Cannot wrap " + instance.op().getClass());
		}
		catch (NullPointerException e) {
			throw new IllegalArgumentException("No wrapper exists for " + Types.raw(
				instance.getType()).toString() + ".");
		}
	}

	private Class<?> getWrapperClass(Object op, OpInfo info) {
		List<Class<?>> suitableWrappers = wrappers.keySet().stream().filter(
			wrapper -> wrapper.isInstance(op)).collect(Collectors.toList());
		List<Class<?>> filteredWrappers = filterWrapperSuperclasses(
			suitableWrappers);
		if (filteredWrappers.size() == 0) throw new IllegalArgumentException(info
			.implementationName() + ": matched op Type " + info.opType().getClass() +
			" does not match a wrappable Op type.");
		if (filteredWrappers.size() > 1) throw new IllegalArgumentException(
			"Matched op Type " + info.opType().getClass() +
				" matches multiple Op types: " + filteredWrappers);
		if (!Types.isAssignable(Types.raw(info.opType()), filteredWrappers.get(0)))
			throw new IllegalArgumentException(Types.raw(info.opType()) +
				"cannot be wrapped as a " + filteredWrappers.get(0));
		return filteredWrappers.get(0);
	}

	private List<Class<?>> filterWrapperSuperclasses(
		List<Class<?>> suitableWrappers)
	{
		if (suitableWrappers.size() < 2) return suitableWrappers;
		List<Class<?>> list = new ArrayList<>();
		for (Class<?> c : suitableWrappers) {
			boolean isSuperclass = false;
			for (Class<?> other : suitableWrappers) {
				if (c.equals(other)) continue;
				if (c.isAssignableFrom(other)) isSuperclass = true;
			}
			if (!isSuperclass) list.add(c);
		}
		return list;
	}

	private List<RichOp<?>> resolveOpDependencies(OpCandidate candidate,
		Hints hints)
	{
		return resolveOpDependencies(candidate.opInfo(), candidate.typeVarAssigns(),
			hints);
	}

	@SuppressWarnings("rawtypes")
	private synchronized void initWrappers() {
		if (wrappers != null) return;
		wrappers = new HashMap<>();
		for (Discoverer d : discoverers)
			for (OpWrapper wrapper : d.discover(OpWrapper.class))
					wrappers.put(wrapper.type(), wrapper);
	}

	/**
	 * Attempts to inject {@link OpDependency} annotated fields of the specified
	 * object by looking for Ops matching the field type and the name specified in
	 * the annotation. The field type is assumed to be functional.
	 *
	 * @param info - the {@link OpInfo} whose {@link OpDependency}s will be
	 *          injected
	 * @param typeVarAssigns - the mapping of {@link TypeVariable}s in the
	 *          {@code OpInfo} to {@link Type}s given in the request.
	 */
	private List<RichOp<?>> resolveOpDependencies(OpInfo info,
		Map<TypeVariable<?>, Type> typeVarAssigns, Hints hints)
	{

		final List<OpDependencyMember<?>> dependencies = info.dependencies();
		final List<RichOp<?>> dependencyChains = new ArrayList<>();

		for (final OpDependencyMember<?> dependency : dependencies) {
			final OpRef dependencyRef = inferOpRef(dependency, typeVarAssigns);
			try {
				// TODO: Consider a new Hint implementation
				Hints hintsCopy = hints.plus(DependencyMatching.IN_PROGRESS,
					History.SKIP_RECORDING, Simplification.FORBIDDEN);
				if (!dependency.isAdaptable()) {
					hintsCopy = hintsCopy.plus(Adaptation.FORBIDDEN);
				}

				MatchingConditions conditions = generateCacheHit(dependencyRef,
					hintsCopy);
				dependencyChains.add(wrapViaCache(conditions));
			}
			catch (final OpMatchingException e) {
				String message = DependencyMatchingException.message(info
					.implementationName(), dependency.getKey(), dependencyRef);
				if (e instanceof DependencyMatchingException) {
					throw new DependencyMatchingException(message,
						(DependencyMatchingException) e);
				}
				throw new DependencyMatchingException(message);
			}
		}
		return dependencyChains;
	}

	private OpRef inferOpRef(OpDependencyMember<?> dependency,
		Map<TypeVariable<?>, Type> typeVarAssigns) 
	{
		final Type mappedDependencyType = Types.mapVarToTypes(new Type[] {
			dependency.getType() }, typeVarAssigns)[0];
		final String dependencyName = dependency.getDependencyName();
		return inferOpRef(mappedDependencyType, dependencyName,
			typeVarAssigns);
	}

	private RichOp<?> wrapViaCache(MatchingConditions conditions) {
		OpInstance<?> instance = getInstance(conditions);
		return wrap(instance, conditions.hints());
	}

	private RichOp<?> wrap(OpInstance<?> instance, Hints hints) {
		return wrapOp(instance, hints);
	}


	/**
	 * Tries to infer a {@link OpRef} from a functional Op type. E.g. the type:
	 * 
	 * <pre>
	 * Computer&lt;Double[], Double[]&gt
	 * </pre>
	 * 
	 * Will result in the following {@link OpRef}:
	 * 
	 * <pre>
	 * Name: 'specified name'
	 * Types:       [Computer&lt;Double, Double&gt]
	 * InputTypes:  [Double[], Double[]]
	 * OutputTypes: [Double[]]
	 * </pre>
	 * 
	 * Input and output types will be inferred by looking at the signature of the
	 * functional method of the specified type. Also see
	 * {@link FunctionalParameters#findFunctionalMethodTypes(Type)}.
	 *
	 * @param type the functional {@link Type} of the {@code op} we're looking for
	 * @param name the name of the {@code op} we're looking for
	 * @param typeVarAssigns the mappings of {@link TypeVariable}s to {@link Type}s 
	 * @return null if the specified type has no functional method
	 */
	private OpRef inferOpRef(Type type, String name, Map<TypeVariable<?>, Type> typeVarAssigns) {
		List<FunctionalMethodType> fmts = FunctionalParameters.findFunctionalMethodTypes(type);

		EnumSet<ItemIO> inIos = EnumSet.of(ItemIO.INPUT, ItemIO.CONTAINER, ItemIO.MUTABLE);
		EnumSet<ItemIO> outIos = EnumSet.of(ItemIO.OUTPUT, ItemIO.CONTAINER, ItemIO.MUTABLE);

		Type[] inputs = fmts.stream() //
				.filter(fmt -> inIos.contains(fmt.itemIO())) //
				.map(FunctionalMethodType::type) //
				.toArray(Type[]::new);

		Type[] outputs = fmts.stream() //
				.filter(fmt -> outIos.contains(fmt.itemIO())) //
				.map(FunctionalMethodType::type) //
				.toArray(Type[]::new);

		Type[] mappedInputs = Types.mapVarToTypes(inputs, typeVarAssigns);
		Type[] mappedOutputs = Types.mapVarToTypes(outputs, typeVarAssigns);

		final int numOutputs = mappedOutputs.length;
		if (numOutputs != 1) {
			String error = "Op '" + name + "' of type " + type + " specifies ";
			error += numOutputs == 0 //
					? "no outputs" //
					: "multiple outputs: " + Arrays.toString(outputs);
			error += ". This is not supported.";
			throw new OpMatchingException(error);
		}
		return new DefaultOpRef(name, type, mappedOutputs[0], mappedInputs);
	}

	private synchronized void initOpDirectory() {
		if (opDirectory != null) return;
		opDirectory = new HashMap<>();
		// add all OpInfos that are directly discoverable
		discoverers.stream().flatMap(d -> d.discover(OpInfo.class).stream()).forEach(info -> addToOpIndex.accept(info, log));
		List<OpInfoGenerator> generators = infoGenerators();
		discoverers.stream().flatMap(d -> d.discover(Op.class).stream()).forEach(o -> registerOpsFrom(o, generators));
		discoverers.stream().flatMap(d -> d.discover(OpCollection.class).stream()).forEach(o -> registerOpsFrom(o, generators));
	}

	/**
	 * Generates a {@link List} of {@link OpInfo}s from {@code o} using a List of {@link OpInfoGenerator}s.
	 * @param o the {@link Object} to parse {@link OpInfo}s from.
	 * @param generators the {@link List} of {@link OpInfoGenerator}s
	 * @return a {@link List} of {@link OpInfo}s
	 */
	private Collection<OpInfo> opsFromObject(Object o, List<OpInfoGenerator> generators) {
		return generators.stream() //
				.filter(g -> g.canGenerateFrom(o)) //
				.flatMap(g -> g.generateInfosFrom(o).stream()) //
				.collect(Collectors.toList());
	}

	private void registerOpsFrom(Object o, List<OpInfoGenerator> generators) {
		opsFromObject(o, generators).forEach(info -> addToOpIndex.accept(info, log));
	}
	
	private List<OpInfoGenerator> infoGenerators() {
		return discoverers.stream() //
				.flatMap(d -> d.discover(OpInfoGenerator.class).stream()) //
				.collect(Collectors.toList());
	}

	private synchronized void initIdDirectory() {
		if (idDirectory != null) return;
		idDirectory = new HashMap<>();
		if (opDirectory == null) initOpDirectory();

		opDirectory.values().stream() //
				.flatMap(Collection::stream) //
				.forEach(info -> idDirectory.put(info.id(), info));
	}

	private final BiConsumer<OpInfo, Logger> addToOpIndex = (final OpInfo opInfo, final Logger log) -> {
		if (opInfo.names() == null || opInfo.names().size() == 0) {
			log.error("Skipping Op " + opInfo.implementationName() + ":\n" +
				"Op implementation must provide name.");
			return;
		}
		if (!opInfo.isValid()) {
			log.error("Skipping invalid Op " + opInfo.implementationName() + ":\n" +
				opInfo.getValidityException().getMessage());
			return;
		}
		for (String opName : opInfo.names()) {
			opDirectory.putIfAbsent(opName, new TreeSet<>());
			boolean success = opDirectory.get(opName).add(opInfo);
			if (!success) System.out.println("Did not add OpInfo " + opInfo);
		}
	};

	private Set<OpInfo> opsOfName(final String name) {
		final Set<OpInfo> ops = opDirectory.getOrDefault(name, Collections
			.emptySet());
		return Collections.unmodifiableSet(ops);
	}

	/**
	 * Sets the default {@link Hints} used for finding Ops.
	 * <p>
	 * Note that this method is <b>not</b> thread safe and is provided for
	 * convenience. If the user wishes to use {@link Hints} in a thread-safe manner,
	 * they should use
	 * {@link DefaultOpEnvironment#op(String, Nil, Nil[], Nil, Hints)} if using
	 * different {@link Hints} for different calls. Alternatively, this method can be
	 * called before all Ops called in parallel without issues.
	 */
	@Override
	public void setDefaultHints(Hints hints) {
		this.environmentHints = hints.copy();
	}

	@Override
	public Hints getDefaultHints() {
		if (this.environmentHints != null) return this.environmentHints.copy();
		return new DefaultHints();
	}

	@Override
	public Hints createHints(String... startingHints) {
		return new DefaultHints(startingHints);
	}

}
