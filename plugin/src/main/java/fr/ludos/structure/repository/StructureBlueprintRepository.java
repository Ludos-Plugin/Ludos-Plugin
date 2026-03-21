package fr.ludos.structure.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import fr.ludos.structure.model.StructureBlock;
import fr.ludos.structure.model.StructureBlueprint;

public final class StructureBlueprintRepository extends AbstractYamlRepository<StructureBlueprint> {
	public StructureBlueprintRepository(JavaPlugin plugin) {
		super(plugin, "structures");
	}

	@Override
	protected String getRootKey() {
		return "structure";
	}

	@Override
	protected void serialize(StructureBlueprint value, YamlConfiguration config) {
		config.set(rootPath("name"), value.getName());
		config.set(rootPath("size.x"), value.getSizeX());
		config.set(rootPath("size.y"), value.getSizeY());
		config.set(rootPath("size.z"), value.getSizeZ());

		List<Map<String, Object>> serializedBlocks = new ArrayList<>();
		for (StructureBlock block : value.getBlocks()) {
			Map<String, Object> map = new HashMap<>();
			map.put("x", block.getX());
			map.put("y", block.getY());
			map.put("z", block.getZ());
			map.put("material", block.getMaterial().name());
			map.put("mapper", block.getMapperId());
			map.put("payload", new HashMap<>(block.getPayload()));
			serializedBlocks.add(map);
		}

		config.set(rootPath("blocks"), serializedBlocks);
	}

	@Override
	protected StructureBlueprint deserialize(String key, YamlConfiguration config) {
		String name = config.getString(rootPath("name"), key);
		int sizeX = config.getInt(rootPath("size.x"));
		int sizeY = config.getInt(rootPath("size.y"));
		int sizeZ = config.getInt(rootPath("size.z"));

		List<?> list = config.getList(rootPath("blocks"), Collections.emptyList());
		List<StructureBlock> blocks = new ArrayList<>();

		for (Object object : list) {
			if (!(object instanceof Map<?, ?> map)) {
				continue;
			}

			int x = toInt(map.get("x"));
			int y = toInt(map.get("y"));
			int z = toInt(map.get("z"));
			String materialName = String.valueOf(map.get("material"));
			String mapperId = String.valueOf(map.get("mapper"));

			Material material = Material.matchMaterial(materialName);
			if (material == null) {
				continue;
			}

			Map<String, String> payload = new HashMap<>();
			Object payloadObject = map.get("payload");
			if (payloadObject instanceof Map<?, ?> payloadMap) {
				for (Map.Entry<?, ?> payloadEntry : payloadMap.entrySet()) {
					payload.put(String.valueOf(payloadEntry.getKey()), String.valueOf(payloadEntry.getValue()));
				}
			}

			blocks.add(new StructureBlock(x, y, z, material, mapperId, payload));
		}

		return new StructureBlueprint(name, sizeX, sizeY, sizeZ, blocks);
	}

	private int toInt(Object value) {
		if (value instanceof Number number) {
			return number.intValue();
		}

		return Integer.parseInt(String.valueOf(value));
	}
}