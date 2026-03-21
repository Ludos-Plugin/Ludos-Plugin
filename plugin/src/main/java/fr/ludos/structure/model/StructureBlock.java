package fr.ludos.structure.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;

public final class StructureBlock {
	private final int x;
	private final int y;
	private final int z;
	private final Material material;
	private final String mapperId;
	private final Map<String, String> payload;

	public StructureBlock(int x, int y, int z, Material material, String mapperId, Map<String, String> payload) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.material = material;
		this.mapperId = mapperId;
		this.payload = Collections.unmodifiableMap(new HashMap<>(payload));
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getZ() {
		return z;
	}

	public Material getMaterial() {
		return material;
	}

	public String getMapperId() {
		return mapperId;
	}

	public Map<String, String> getPayload() {
		return payload;
	}
}