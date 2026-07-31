package fr.ludos.core.persistence.config.sectionProvider;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import fr.ludos.core.gui.GuiContext;
import fr.ludos.core.gui.WindowProvider;
import fr.ludos.core.gui.item.BorderItem;
import fr.ludos.core.gui.item.ChangePageItem;
import fr.ludos.core.gui.item.ConfigProviderItem;
import fr.ludos.core.persistence.config.ConfigNode;
import fr.ludos.core.persistence.config.ConfigRootCollection;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.gui.structure.Structure;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.AbstractItemBuilder;
import xyz.xenondevs.invui.window.Window;

/**
 * Collection of {@link ConfigSectionProvider}s, with their corresponding {@link ConfigNode}.
 */
public abstract class ConfigSectionCollection extends ConfigRootCollection {
	public abstract @NotNull ConfigSectionProvider getProvider(String key, CommandSender sender);
	public abstract @NotNull AbstractItemBuilder<?> getItem(String key, CommandSender sender);

	public final boolean execute(Plugin plugin, @NotNull String[] args, CommandSender sender) {
		GuiContext context = GuiContext.of(plugin, this);

		if (args.length == 0) {
			if (! (sender instanceof Player player)) return false;
			if (! openConfigWindow(player, context)) {
				WindowProvider.playDenySound(player);
			}
			return true;
		}

		String key = args[0];
		ConfigSectionProvider provider = getProvider(key, sender);
		if (provider == null) return false;
		if (! provider.checkAuthorizationNotify(sender)) return true;

		ConfigNode node = getNode(key);
		if (node == null) return false;

		context.setConfig(new ConfigSectionContext(provider));

		if (args.length == 1) {
			if (! (sender instanceof Player player)) return false;
			if (! node.openConfigWindow(player, context)) {
				WindowProvider.playDenySound(player);
			}
			return true;
		}

		if (node.execute(Arrays.copyOfRange(args, 1, args.length), sender, context)) {
			provider.saveConfig();
		}
		return true;
	}

	public final @Nullable List<@NotNull String> tabComplete(@NotNull String[] args, CommandSender sender) {
		if (args.length <= 1) {
			return options(sender).stream().toList();
		}

		ConfigNode root = getNode(args[0]);
		if (root == null) return null;

		return root.tabComplete(Arrays.copyOfRange(args, 1, args.length), sender);
	}

	public Window configWindow(Player player, GuiContext context) {
		Set<String> options = options(player);
		if (options.isEmpty()) return null;

		List<Item> items = options.stream()
			.map(key -> {
				ConfigSectionProvider provider = getProvider(key, player);
				if (provider == null || ! provider.checkAuthorizationSilent(player)) return null;

				ConfigNode node = getNode(key);

				return new ConfigProviderItem(context, provider, node) {
					@Override
					public ItemProvider getItemProvider(Player viewer) {
						return getItem(key, player);
					}
				};
			})
			.filter(Objects::nonNull)
			.collect(Collectors.toList());

		if (items.isEmpty()) return null;

		return Window.single()
			.setTitle(new AdventureComponentWrapper(normalizedDisplayName()))
			.setGui(PagedGui.items()
				.setStructure(new Structure(
					"# # # # # # # # #",
					"# x x x x x x x #",
					"# x x x x x x x #",
					"# # # # P # # # #")
				)
				.addIngredient('#', BorderItem.INSTANCE)
				.addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
				.addIngredient('P', ChangePageItem.INSTANCE)
				.setContent(items)
			)
			.build(player);
	}
}
