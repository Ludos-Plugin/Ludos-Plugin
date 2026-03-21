package fr.ludos.structure.mapper;

import java.util.Map;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

public interface StructureMapper<T> {
	String getId();

	Class<T> getType();

	boolean supports(BlockState state);

	T capture(BlockState state);

	Map<String, String> serialize(T value);

	T deserialize(Map<String, String> value);

	void apply(Block block, T value);
}