package fr.ludos.structure.mapper;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

public final class BlockDataStructureMapper extends AbstractStructureMapper<String> {
	private static final String VALUE_KEY = "value";

	public BlockDataStructureMapper() {
		super("block_data", String.class);
	}

	@Override
	public boolean supports(BlockState state) {
		return state != null;
	}

	@Override
	public String capture(BlockState state) {
		return state.getBlockData().getAsString();
	}

	@Override
	public Map<String, String> serialize(String value) {
		Map<String, String> serialized = new HashMap<>();
		serialized.put(VALUE_KEY, value);
		return serialized;
	}

	@Override
	public String deserialize(Map<String, String> value) {
		return value.get(VALUE_KEY);
	}

	@Override
	public void apply(Block block, String value) {
		if (value == null || value.isBlank()) {
			return;
		}

		block.setBlockData(Bukkit.createBlockData(value), false);
	}
}