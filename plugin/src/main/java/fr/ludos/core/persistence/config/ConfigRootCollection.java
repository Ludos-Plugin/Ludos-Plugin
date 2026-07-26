package fr.ludos.core.persistence.config;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.gui.BorderItem;
import fr.ludos.core.gui.ChangePageItem;
import fr.ludos.core.gui.ConfigNodeItem;
import fr.ludos.core.persistence.config.sectionProvider.ConfigSectionContext;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.gui.structure.Structure;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.window.Window;

/**
 * {@link ConfigRoot} implemented as a Collection of sub-{@link ConfigNode}s.
 */
public abstract class ConfigRootCollection implements ConfigRoot {
	public abstract @Nullable ConfigNode getEntry(String key);
	public abstract @Nullable Collection<ConfigNode> getEntries();

	public final @NotNull Set<@NotNull String> getEntryOptions(String key, CommandSender sender) {
		ConfigNode node = getEntry(key);
		if (node == null) return Collections.emptySet();

		return node.options(sender);
	}

	@Override
	public boolean execute(@NotNull String[] args, CommandSender sender, ConfigSectionContext context, ConfigNodeOperation mode) {
		if (mode == ConfigNodeOperation.set && args.length == 0) {
			if (! (sender instanceof Player player)) return false;
			openConfigWindow(player, context);
			return true;
		}

		String key = args[0];
		ConfigNode node = getEntry(key);
		if (node == null) return false;

		return node.execute(Arrays.copyOfRange(args, 1, args.length), sender, prepareForChildren(context), mode);
	}

	@Override
	public @Nullable List<@NotNull String> tabComplete(@NotNull String[] args, CommandSender sender, ConfigNodeOperation mode) {
		if (args.length <= 1) {
			return options(sender).stream().toList();
		}

		ConfigNode node = getEntry(args[0]);
		if (node == null) return null;

		return node.tabComplete(Arrays.copyOfRange(args, 1, args.length), sender, mode);
	}

	@Override
	public Window configWindow(Player player, ConfigSectionContext context) {
		Boolean[] doModalBack = new Boolean[]{true};
		ConfigSectionContext childrenContext = prepareForChildren(context);
		List<Item> items = getEntries().stream()
			.filter(Objects::nonNull)
			.map(node -> new ConfigNodeItem(node, childrenContext).addClickHandler(() -> doModalBack[0] = false))
			.collect(Collectors.toList());

		if (items.isEmpty()) return null;

		Window window = Window.single()
			.setTitle(new AdventureComponentWrapper(displayName()))
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
			.addCloseHandler(() -> {
				if (doModalBack[0]) {
					context.openPreviousWindow(player);
					doModalBack[0] = false;
				}
			})
			.build(player);
		return window;
	}
	protected ConfigSectionContext prepareForChildren(ConfigSectionContext context) {
		return context.getDeeper(null, this);
	}
}
