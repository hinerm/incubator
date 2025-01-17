
package org.scijava.types.inference;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.scijava.types.Types;

/**
 * Adapter class wrapping a {@code Map<TypeVariable, TypeMapping>} into a
 * {@code Map<TypeVariable, Type>} for use with {@link Types} API.
 */
public class TypeVarAssigns implements Map<TypeVariable<?>, Type> {

	private static TypeMapping suitableTypeMapping(TypeVariable<?> typeVar,
		Type newType, boolean malleability)
	{
		if (newType instanceof WildcardType) {
			return new WildcardTypeMapping(typeVar, (WildcardType) newType,
				malleability);
		}
		return new TypeMapping(typeVar, newType, malleability);
	}

	private Map<TypeVariable<?>, TypeMapping> map;

	public TypeVarAssigns(Map<TypeVariable<?>, TypeMapping> map) {
		this.map = map;
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}

	@Override
	public Set<Entry<TypeVariable<?>, Type>> entrySet() {
		return map.entrySet().stream().map(e -> {
			return new Map.Entry<TypeVariable<?>, Type>() {

				@Override
				public TypeVariable<?> getKey() {
					return e.getKey();
				}

				@Override
				public Type getValue() {
					return get(getKey());
				}

				@Override
				public Type setValue(Type value) {
					return put(getKey(), value);
				}
			};
		}).collect(Collectors.toSet());
	}

	@Override
	public Type get(Object key) {
		TypeMapping value = map.get(key);
		return value == null ? null : value.getType();
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public Set<TypeVariable<?>> keySet() {
		return map.keySet();
	}

	@Override
	public Type put(TypeVariable<?> typeVar, Type type) {
		final TypeMapping previousMapping = //
			map.put(typeVar, suitableTypeMapping(typeVar, type, isMalleable(
				typeVar)));
		return previousMapping == null ? null : previousMapping.getType();
	}

	@Override
	public void putAll(Map<? extends TypeVariable<?>, ? extends Type> m) {
		for (Map.Entry<? extends TypeVariable<?>, ? extends Type> e : m
			.entrySet())
		{
			put(e.getKey(), e.getValue());
		}
	}

	@Override
	public Type putIfAbsent(TypeVariable<?> typeVar, Type type) {
		if (!map.containsKey(typeVar)) return put(typeVar, type);
		return get(typeVar);
	}

	@Override
	public Type remove(Object key) {
		TypeMapping value = map.remove(key);
		return value == null ? null : value.getType();
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public Collection<Type> values() {
		return map.values().stream().map(v -> v.getType()).collect(Collectors
			.toList());
	}

	private boolean isMalleable(TypeVariable<?> typeVar) {
		return map.containsKey(typeVar) && map.get(typeVar).malleable;
	}

}
