package fr.ludos.core.persistence.config;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.gui.GuiContext;
import fr.ludos.core.gui.WindowProvider;
import fr.ludos.core.gui.item.BorderItem;
import fr.ludos.core.gui.item.ChangePageItem;
import fr.ludos.core.gui.item.ConfigNodeItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.gui.structure.Structure;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.window.Window;

/**
 * {@link ConfigNode} implemented as a Collection of sub-{@link ConfigNode}.
 */
public abstract class ConfigNodeCollection extends ConfigRootCollection implements ConfigNode {
	private final TextComponent name;
	private final String namespace;

	public ConfigNodeCollection(@NotNull TextComponent name, @Nullable String namespace) {
		this.name = Objects.requireNonNull(name);
		this.namespace = namespace;
	}

	public String namespace() {
		return namespace;
	}
	@Override
	public String key() {
		return namespace;
	}
	@Override
	public Component displayName() {
		return name;
	}

	@Override
	public boolean execute(@NotNull String[] args, CommandSender sender, GuiContext context) {
		if (args.length == 0 && sender instanceof Player player) {
			if (! openConfigWindow(player, context)) {
				WindowProvider.playDenySound(player);
			}
			return true;
		}

		String key = args[0];
		ConfigNode node = getNode(key);
		if (node == null) return false;

		return node.execute(Arrays.copyOfRange(args, 1, args.length), sender, context.deeper(namespace));
	}

	@Override
	public @Nullable List<@NotNull String> tabComplete(@NotNull String[] args, CommandSender sender) {
		if (args.length <= 1) {
			return options(sender).stream().toList();
		}

		ConfigNode node = getNode(args[0]);
		if (node == null) return null;

		return node.tabComplete(Arrays.copyOfRange(args, 1, args.length), sender);
	}

	public Window configWindow(Player player, GuiContext context) {
		GuiContext childrenContext = context.setWindow(this).deeper(namespace);

		Boolean[] doModalBack = new Boolean[]{true};
		List<Item> items = getNodes().stream()
			.filter(Objects::nonNull)
			.map(node -> new ConfigNodeItem(node, childrenContext).addClickHandler(() -> doModalBack[0] = false))
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
			.addCloseHandler(() -> {
				if (doModalBack[0]) {
					context.openPreviousWindow(player);
					doModalBack[0] = false;
				}
			})
			.build(player);
	}
}
