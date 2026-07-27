package fr.ludos.core.persistence.config;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.persistence.config.sectionProvider.ConfigSectionContext;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.item.builder.AbstractItemBuilder;
import xyz.xenondevs.invui.window.Window;

/**
 * A structure to represent a Configurable value (ex: The number of waves in a Raid), and its valid values (options).
 */
public interface ConfigNode extends ConfigRoot {
	public @Nullable String key();

	public AbstractItemBuilder<?> item(Player player);
	public default AbstractItemBuilder<?> displayItem(Player player) {
		AbstractItemBuilder<?> builder = item(player);
		builder.setDisplayName(new AdventureComponentWrapper(displayName()));
		builder.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DYE, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_POTION_EFFECTS);
		return builder;
	}
	public default AbstractItemBuilder<?> displayItem(Player player, ConfigSectionContext context) {
		return displayItem(player);
	}

	public boolean execute(@NotNull String[] args, CommandSender sender, ConfigSectionContext context, ConfigNodeOperation mode);

	public default @Nullable List<@NotNull String> tabComplete(@NotNull String[] args, CommandSender sender, ConfigNodeOperation mode) {
		if (args.length <= 1) {
			return options(sender).stream().toList();
		}

		return Collections.emptyList();
	}

	public Window configWindow(Player player, ConfigSectionContext context);
	public default void openConfigWindow(Player player, ConfigSectionContext context) {
		Window window = configWindow(player, context);
		if (window == null) {
			player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.1f, 0.8f);
			return;
		}
		window.open();
	}
}
