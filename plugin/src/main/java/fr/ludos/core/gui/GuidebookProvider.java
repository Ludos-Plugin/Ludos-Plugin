package fr.ludos.core.gui;

import org.bukkit.inventory.ItemStack;

/**
 * Provider of a Ludos Guidebook instance.
 */
public interface GuidebookProvider {
	public ItemStack createGuidebook();
}
