package fr.ludos.core.gui;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import xyz.xenondevs.invui.item.builder.AbstractItemBuilder;

/**
 * Simple Player head item.
 */
public class PlayerItemBuilder extends AbstractItemBuilder<PlayerItemBuilder> {
	private static final ItemStack createStack(OfflinePlayer player) {
		ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
		SkullMeta meta = (SkullMeta) skull.getItemMeta();
		meta.setOwningPlayer(player);
		meta.displayName(
			Component.text(player.getName())
				.decoration(TextDecoration.ITALIC, false)
				.color(NamedTextColor.WHITE)
		);
		skull.setItemMeta(meta);

		return skull;
	}

	public PlayerItemBuilder(OfflinePlayer player) {
		super(createStack(player));
	}
}
