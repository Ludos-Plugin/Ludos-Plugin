package fr.ludos.structure.mapper;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.bukkit.block.BlockState;

public final class StructureMapperRegistry {
	private final Map<String, StructureMapper<?>> mappers;

	public StructureMapperRegistry() {
		this.mappers = new LinkedHashMap<>();
	}

	public void register(StructureMapper<?> mapper) {
		if (mapper == null) {
			throw new IllegalArgumentException("Mapper cannot be null");
		}

		mappers.put(mapper.getId(), mapper);
	}

	public Optional<StructureMapper<?>> get(String mapperId) {
		return Optional.ofNullable(mappers.get(mapperId));
	}

	public Optional<StructureMapper<?>> find(BlockState state) {
		return mappers.values().stream().filter(mapper -> mapper.supports(state)).findFirst();
	}

	@SuppressWarnings("unchecked")
	public <T> Optional<StructureMapper<T>> getTyped(String mapperId, Class<T> type) {
		StructureMapper<?> mapper = mappers.get(mapperId);
		if (mapper == null) {
			return Optional.empty();
		}

		if (!mapper.getType().equals(type)) {
			return Optional.empty();
		}

		return Optional.of((StructureMapper<T>) mapper);
	}

	public Collection<String> ids() {
		return mappers.keySet();
	}
}