package org.scijava.ops.engine.reduce;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.scijava.common3.validity.ValidityException;
import org.scijava.common3.validity.ValidityProblem;
import org.scijava.ops.api.Hints;
import org.scijava.ops.api.OpDependencyMember;
import org.scijava.ops.api.OpDescription;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.features.BaseOpHints;
import org.scijava.ops.engine.struct.OpResizingMemberParser;
import org.scijava.ops.engine.struct.RetypingRequest;
import org.scijava.struct.FunctionalMethodType;
import org.scijava.struct.Member;
import org.scijava.struct.Struct;
import org.scijava.struct.StructInstance;
import org.scijava.struct.Structs;

public class ReducedOpInfo implements OpInfo {

	protected static final String IMPL_DECLARATION = "|Reduction:";
	protected static final String PARAMS_REDUCED = "|ParamsReduced:";
	protected static final String ORIGINAL_INFO = "|OriginalInfo:";

	private final OpInfo srcInfo;
	private final Type reducedOpType;
	private final int paramsReduced;

	private final Hints hints;
	private final Struct struct;
	public ReducedOpInfo(OpInfo src, Type reducedOpType, int paramsReduced) {
		this.srcInfo = src;
		this.reducedOpType = reducedOpType;
		this.paramsReduced = paramsReduced;
		this.hints = srcInfo.declaredHints().plus(BaseOpHints.Reduction.FORBIDDEN);
		List<ValidityProblem> problems = new ArrayList<>();

		RetypingRequest r = retypingRequest();
		this.struct = Structs.from(r, reducedOpType, problems, new OpResizingMemberParser());

		if (!problems.isEmpty()) {
			throw new ValidityException(problems);
		}
	}

	/**
	 * For an Op with n inputs and one output (which may also be an input), we
	 * want to preserve n-paramsReduced inputs and that one output. This retyping
	 * request will preserve ONLY those inputs. Finding those inputs in the struct
	 * of {@code srcInfo}, though, is tricky because the output could appear
	 * anywhere in that {@link Struct} (thanks Inplaces :P). So, we split this
	 * problem into two subproblems:
	 * <ol>
	 * <li>When the output is <b>also</b> an input</li>
	 * <li>When the output is <b>not</b> an input</li>
	 * </ol>
	 * 
	 * @return a {@link RetypingRequest} defining how to create the {@link Struct} of this {@link ReducedOpInfo}
	 */
	private RetypingRequest retypingRequest() {
		if(srcInfo.output().isInput()) {
			return mutableOutputOpRetypingRequest();
		}
		return pureOutputOpRetypingRequest();
	}

	/**
	 * For an Op with n inputs and one <b>mutable</b> output, we want to retain the
	 * mutable output (wherever it is in the {@link Struct}), as well as the first
	 * {@code n-paramsReduced-1} <b>pure</b> inputs.
	 * 
	 * @return a {@link RetypingRequest} defining how to create the {@link Struct}
	 *         of this {@link ReducedOpInfo}
	 */
	private RetypingRequest mutableOutputOpRetypingRequest() {
		List<Member<?>> inputs = srcInfo.inputs();
		// We need n - paramsReduced - 1 pure inputs
		int retainedPureInputs = inputs.size() - paramsReduced - 1;
		List<FunctionalMethodType> newFmts = new ArrayList<>();
		int addedPureInputs = 0;
		for (Member<?> m : srcInfo.inputs()) {
			// if pure input, add only if we haven't added enough pure inputs
			if (!m.isOutput() && addedPureInputs < retainedPureInputs) {
				newFmts.add(new FunctionalMethodType(m));
				addedPureInputs++;
			}
			// if I/O output, add it
			else if (m.isOutput()) {
				newFmts.add(new FunctionalMethodType(m));
			}
		}

		return new RetypingRequest(srcInfo.struct(), newFmts);
	}

	/**
	 * For an Op with n inputs and one <b>pure</b> output, we want to retain the
	 * first {@code n-paramsReduced} <b>pure</b> inputs as well as that pure
	 * output.
	 * 
	 * @return a {@link RetypingRequest} defining how to create the {@link Struct}
	 *         of this {@link ReducedOpInfo}
	 */
	private RetypingRequest pureOutputOpRetypingRequest() {
		List<Member<?>> inputs = srcInfo.inputs();
		// Of the n inputs in srcInfo, we need to keep n-paramsReduced of them.
		long retainedPureInputs = inputs.size() - paramsReduced;
		List<FunctionalMethodType> newFmts = inputs.stream() //
			.limit(retainedPureInputs) //
			.map(FunctionalMethodType::new) //
			.collect(Collectors.toList());
		// Grab the pure output, and tack it onto the end.
		newFmts.add(new FunctionalMethodType(srcInfo.output()));
		return new RetypingRequest(srcInfo.struct(), newFmts);
	}

	@Override
	public Type opType() {
		return reducedOpType;
	}

	@Override
	public Struct struct() {
		return struct;
	}

	@Override public Hints declaredHints() {
		return hints;
	}

	@Override
	public List<String> names() {
		return srcInfo.names();
	}

	@Override
	public double priority() {
		return srcInfo.priority();
	}

	@Override
	public String implementationName() {
		// TODO: improve this name
		return srcInfo.implementationName() + "Reduction" + paramsReduced; 
	}

	/**
	 * Gets the op's dependencies on other ops. NB the reduction wrapper has no
	 * dependencies, but the Op itself might. So the dependencies are actually
	 * reflected in {@code srcInfo}
	 */
	@Override
	public List<OpDependencyMember<?>> dependencies() {
		return srcInfo().dependencies();
	}

	@Override
	public StructInstance<?> createOpInstance(List<?> dependencies) {
		final Object op = srcInfo.createOpInstance(dependencies).object();
		try {
			Object reducedOp = ReductionUtils.javassistOp(op, this);
			return struct().createInstance(reducedOp);
		}
		catch (Throwable ex) {
			throw new IllegalArgumentException(
				"Failed to invoke reduction of Op: \n" + srcInfo +
					"\nProvided Op dependencies were: " + Objects.toString(dependencies),
				ex);
		}
	}

	@Override
	public AnnotatedElement getAnnotationBearer() {
		return srcInfo.getAnnotationBearer();
	}

	@Override public String version() {
		return srcInfo().version();
	}

	/**
	 * For a reduced Op, we define the implementation as the concatenation
	 * of:
	 * <ol>
	 * <li>The number of reduced parameters
	 * <li>The id of the source Op
	 * </ol>
	 * <p>
	 */
	@Override
	public String id() {
		// declaration
		StringBuilder sb = new StringBuilder(IMPL_DECLARATION);
		// params reduced
		sb.append(PARAMS_REDUCED);
		sb.append(paramsReduced());
		// original info
		sb.append(ORIGINAL_INFO);
		sb.append(srcInfo().id());
		return sb.toString();
	}

	public OpInfo srcInfo() {
		return srcInfo;
	}

	public int paramsReduced() {
		return paramsReduced;
	}

	@Override
	public String toString() {
		return OpDescription.basic(this);
	}

}
