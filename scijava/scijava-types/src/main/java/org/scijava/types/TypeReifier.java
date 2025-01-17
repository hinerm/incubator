package org.scijava.types;

import com.google.common.reflect.TypeToken;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.scijava.log2.Logger;
import org.scijava.types.extractors.IterableTypeExtractor;

public interface TypeReifier {

	/**
	 * Gets the type extractor which handles the given class, or null if none.
	 */
	Optional<TypeExtractor> getExtractor(Class<?> c);

	Logger log();

	/**
	 * Extracts the generic {@link Type} of the given {@link Object}.
	 * <p>
	 * The ideal goal of the extraction is to reconstitute a fully concrete
	 * generic type, with all type variables fully resolved&mdash;e.g.:
	 * {@code ArrayList<Integer>} rather than a raw {@code ArrayList} class or
	 * even {@code ArrayList<N extends Number>}. Failing that, though, type
	 * variables which are still unknown after analysis will be replaced with
	 * wildcards&mdash;e.g., {@code HashMap} might become
	 * {@code HashMap<String, ?>} if a concrete type for the map values cannot be
	 * determined.
	 * </p>
	 * <p>
	 * For objects whose concrete type has no parameters, this method simply
	 * returns {@code o.getClass()}. For example:
	 * 
	 * <pre>
	 *      StringList implements List&lt;String&gt;
	 * </pre>
	 * 
	 * will return {@code StringList.class}.
	 * <p>
	 * The interesting case is for objects whose concrete class <em>does</em> have
	 * type parameters. E.g.:
	 * 
	 * <pre>
	 *      NumberList&lt;N extends Number&gt; implements List&lt;N&gt;
	 *      ListMap&lt;K, V, T&gt; implements Map&lt;K, V&gt;, List&lt;T&gt;
	 * </pre>
	 * 
	 * For such types, we try to fill the type parameters recursively, using
	 * {@link TypeExtractor} plugins that know how to glean types at runtime from
	 * specific sorts of objects.
	 * </p>
	 * <p>
	 * For example, {@link IterableTypeExtractor} knows how to guess a {@code T}
	 * for any {@code Iterable<T>} by examining the type of the elements in its
	 * iteration. (Of course, this may be inaccurate if the elements in the
	 * iteration are heterogeneously typed, but for many use cases this guess is
	 * better than nothing.)
	 * </p>
	 * <p>
	 * In this way, the behavior of the generic type extraction is fully
	 * extensible, since additional {@link TypeExtractor} plugins can always be
	 * introduced which extract types more intelligently in cases where more
	 * <em>a priori</em> knowledge about that type is available at runtime.
	 * </p>
	 */
	default Type reify(final Object o) {
		if (o == null) return new Any();
		
		if (o instanceof GenericTyped) {
			// Object implements the GenericTyped interface; it explicitly declares
			// the generic type by which it wants to be known. This makes life easy!
			return ((GenericTyped) o).getType();
		}

		final Class<?> c = o.getClass();
		final TypeVariable<?>[] typeVars = c.getTypeParameters();
		final int numVars = typeVars.length;

		if (numVars == 0) {
			// if the class is synthetic, we are probably missing something due to
			// type erasure.
			if (c.isSynthetic()) {
				log().warn("Object " + o + " is synthetic. " +
					"Its type parameters are not reifiable and thus will likely cause unintended behavior!");
			}
			// Object has no generic parameters; we are done!
			return c;
		}

		// Object has parameters which need to be resolved. Let's do it.

		// Here we will store all of our object's resolved type variables.
		final Map<TypeVariable<?>, Type> resolved = new HashMap<>();

		for (final TypeToken<?> token : TypeToken.of(c).getTypes()) {
			if (resolved.size() == numVars) break; // Got 'em all!

			final Type type = token.getType();
			if (!Types.containsTypeVars(type)) {
				// No type variables are buried in this type; it is useless to us!
				continue;
			}

			// Populate relevant type variables from the reified supertype!
			final Map<TypeVariable<?>, Type> vars = //
				args(o, token.getRawType());

			if (vars != null) {
				// Remember any resolved type variables.
				// Note that vars may contain other type variables from other layers
				// of the generic type hierarchy, which we don't care about here.
				for (final TypeVariable<?> typeVar : typeVars) {
					if (vars.containsKey(typeVar)) {
						resolved.putIfAbsent(typeVar, vars.get(typeVar));
					}
				}
			}
		}

		// fill in any remaining unresolved type parameters with wildcards
		for (final TypeVariable<?> typeVar : typeVars) {
			resolved.putIfAbsent(typeVar, new Any());
		}

		// now apply all the type variables we resolved
		return Types.parameterize(c, resolved);
	}

	/**
	 * Extracts the resolved type variables of the given {@link Object}, as viewed
	 * through the specified supertype.
	 * <p>
	 * For example, if you call:
	 * </p>
	 * 
	 * <pre>
	 * args(Collections.singleton("Hi"), Iterable.class)
	 * </pre>
	 * <p>
	 * Then it returns a map with contents <code>{T: String}</code> by using the
	 * {@link IterableTypeExtractor} to analyze the object.
	 * </p>
	 * <p>
	 * Note that this method only provides results if there is a
	 * {@link TypeExtractor} plugin which handles <em>exactly</em> the given
	 * supertype.
	 * </p>
	 * 
	 * @see #reify(Object)
	 */
	default <T> Map<TypeVariable<?>, Type> args(final Object o,
		final Class<T> superType)
	{
		final Class<?> c = o.getClass();

		final Optional<TypeExtractor> extractor = getExtractor(superType);
		if (extractor.isEmpty()) return null; // No plugin for this specific class.

		if (!superType.isInstance(o)) {
			throw new IllegalStateException("'" + o.getClass() +
				"' is not an instance of '" + superType.getName() + "'");
		}
		@SuppressWarnings("unchecked")
		final T t = (T) o;

		final Type extractedType = extractor.get().reify(this, t);
		if (extractedType instanceof ParameterizedType) {
			return Types.args(c, (ParameterizedType) extractedType);
		}
		else {
			return Collections.emptyMap();
		}
	}

}
