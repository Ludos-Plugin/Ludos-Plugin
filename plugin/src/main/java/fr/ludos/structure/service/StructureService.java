package fr.ludos.structure.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import fr.ludos.structure.mapper.BlockDataStructureMapper;
import fr.ludos.structure.mapper.StructureMapper;
import fr.ludos.structure.mapper.StructureMapperRegistry;
import fr.ludos.structure.model.StructureBlock;
import fr.ludos.structure.model.StructureBlueprint;
import fr.ludos.structure.model.StructureSelection;
import fr.ludos.structure.repository.StructureBlueprintRepository;

public final class StructureService {
	private final JavaPlugin plugin;
	private final StructureMapperRegistry mapperRegistry;
	private final StructureBlueprintRepository repository;
	private final Map<UUID, StructureSelection> selections;

	public StructureService(JavaPlugin plugin) {
		this.plugin = plugin;
		this.mapperRegistry = new StructureMapperRegistry();
		this.repository = new StructureBlueprintRepository(plugin);
		this.selections = new HashMap<>();

		mapperRegistry.register(new BlockDataStructureMapper());
	}

	public StructureMapperRegistry getMapperRegistry() {
		return mapperRegistry;
	}

	public void setPos1(Player player, Location location) {
		StructureSelection selection = selections.getOrDefault(player.getUniqueId(), new StructureSelection(null, null));
		selections.put(player.getUniqueId(), new StructureSelection(location.toBlockLocation(), selection.getPos2()));
	}

	public void setPos2(Player player, Location location) {
		StructureSelection selection = selections.getOrDefault(player.getUniqueId(), new StructureSelection(null, null));
		selections.put(player.getUniqueId(), new StructureSelection(selection.getPos1(), location.toBlockLocation()));
	}

	public Optional<StructureSelection> getSelection(Player player) {
		return Optional.ofNullable(selections.get(player.getUniqueId()));
	}

	public SaveResult saveSelection(Player player, String structureName) {
		StructureSelection selection = selections.get(player.getUniqueId());
		if (selection == null || !selection.isComplete()) {
			return SaveResult.incompleteSelection();
		}

		if (selection.getPos1().getWorld() == null || selection.getPos2().getWorld() == null) {
			return SaveResult.invalidWorld();
		}

		if (!selection.getPos1().getWorld().getUID().equals(selection.getPos2().getWorld().getUID())) {
			return SaveResult.invalidWorld();
		}

		int minX = Math.min(selection.getPos1().getBlockX(), selection.getPos2().getBlockX());
		int minY = Math.min(selection.getPos1().getBlockY(), selection.getPos2().getBlockY());
		int minZ = Math.min(selection.getPos1().getBlockZ(), selection.getPos2().getBlockZ());
		int maxX = Math.max(selection.getPos1().getBlockX(), selection.getPos2().getBlockX());
		int maxY = Math.max(selection.getPos1().getBlockY(), selection.getPos2().getBlockY());
		int maxZ = Math.max(selection.getPos1().getBlockZ(), selection.getPos2().getBlockZ());

		World world = selection.getPos1().getWorld();
		List<StructureBlock> blocks = new ArrayList<>();

		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				for (int z = minZ; z <= maxZ; z++) {
					Block block = world.getBlockAt(x, y, z);
					if (block.getType() == Material.AIR) {
						continue;
					}

					BlockState state = block.getState();
					Optional<StructureMapper<?>> mapperOptional = mapperRegistry.find(state);
					if (mapperOptional.isEmpty()) {
						continue;
					}

					StructureMapper<?> mapper = mapperOptional.get();
					Map<String, String> payload = serializeFromMapper(mapper, state);

					blocks.add(
						new StructureBlock(
							x - minX,
							y - minY,
							z - minZ,
							block.getType(),
							mapper.getId(),
							payload
						)
					);
				}
			}
		}

		StructureBlueprint blueprint = new StructureBlueprint(
			structureName,
			maxX - minX + 1,
			maxY - minY + 1,
			maxZ - minZ + 1,
			blocks
		);

		try {
			repository.save(structureName, blueprint);
		} catch (IOException exception) {
			plugin.getLogger().warning("Failed to save structure " + structureName + ": " + exception.getMessage());
			return SaveResult.error(exception.getMessage());
		}

		return SaveResult.success(blueprint.getBlocks().size(), blueprint.getSizeX(), blueprint.getSizeY(), blueprint.getSizeZ());
	}

	public Optional<StructureBlueprint> getStructure(String name) {
		return repository.find(name);
	}

	public PasteResult paste(String name, Location anchor) {
		Optional<StructureBlueprint> blueprintOptional = repository.find(name);
		if (blueprintOptional.isEmpty()) {
			return PasteResult.notFound();
		}

		if (anchor.getWorld() == null) {
			return PasteResult.invalidWorld();
		}

		StructureBlueprint blueprint = blueprintOptional.get();
		World world = anchor.getWorld();

		int placed = 0;
		for (StructureBlock structureBlock : blueprint.getBlocks()) {
			int x = anchor.getBlockX() + structureBlock.getX();
			int y = anchor.getBlockY() + structureBlock.getY();
			int z = anchor.getBlockZ() + structureBlock.getZ();

			Block block = world.getBlockAt(x, y, z);

			block.setType(structureBlock.getMaterial(), false);

			Optional<StructureMapper<?>> mapperOptional = mapperRegistry.get(structureBlock.getMapperId());
			if (mapperOptional.isPresent()) {
				StructureMapper<?> mapper = mapperOptional.get();
				applyToBlock(mapper, block, structureBlock.getPayload());
			}

			placed++;
		}

		return PasteResult.success(blueprint.getName(), placed, blueprint.getSizeX(), blueprint.getSizeY(), blueprint.getSizeZ());
	}

	public boolean remove(String name) {
		return repository.delete(name);
	}

	public List<String> listStructures() {
		return repository.listKeys();
	}

	@SuppressWarnings("unchecked")
	private <T> Map<String, String> serializeFromMapper(StructureMapper<T> mapper, BlockState state) {
		T value = mapper.capture(state);
		return mapper.serialize(value);
	}

	@SuppressWarnings("unchecked")
	private <T> void applyToBlock(StructureMapper<T> mapper, Block block, Map<String, String> payload) {
		T value = mapper.deserialize(payload);
		mapper.apply(block, value);
	}

	public record SaveResult(boolean success, String error, int blockCount, int sizeX, int sizeY, int sizeZ) {
		public static SaveResult success(int blockCount, int sizeX, int sizeY, int sizeZ) {
			return new SaveResult(true, null, blockCount, sizeX, sizeY, sizeZ);
		}

		public static SaveResult incompleteSelection() {
			return new SaveResult(false, "Selection must have pos1 and pos2", 0, 0, 0, 0);
		}

		public static SaveResult invalidWorld() {
			return new SaveResult(false, "Selection must be in a single world", 0, 0, 0, 0);
		}

		public static SaveResult error(String message) {
			return new SaveResult(false, message, 0, 0, 0, 0);
		}
	}

	public record PasteResult(boolean success, String error, String name, int blockCount, int sizeX, int sizeY, int sizeZ) {
		public static PasteResult success(String name, int blockCount, int sizeX, int sizeY, int sizeZ) {
			return new PasteResult(true, null, name, blockCount, sizeX, sizeY, sizeZ);
		}

		public static PasteResult notFound() {
			return new PasteResult(false, "Structure not found", null, 0, 0, 0, 0);
		}

		public static PasteResult invalidWorld() {
			return new PasteResult(false, "Target world not found", null, 0, 0, 0, 0);
		}
	}
}