package fr.ludos.structure.mapper;

import java.util.Map;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

public abstract class AbstractStructureMapper<T> implements StructureMapper<T> {
	private final String id;
	private final Class<T> type;

	protected AbstractStructureMapper(String id, Class<T> type) {
		this.id = id;
		this.type = type;
	}

	@Override
	public final String getId() {
		return id;
	}

	@Override
	public final Class<T> getType() {
		return type;
	}

	@Override
	public abstract boolean supports(BlockState state);

	@Override
	public abstract T capture(BlockState state);

	@Override
	public abstract Map<String, String> serialize(T value);

	@Override
	public abstract T deserialize(Map<String, String> value);

	@Override
	public abstract void apply(Block block, T value);
}