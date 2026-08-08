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
import fr.ludos.core.gui.WindowUtility;
import fr.ludos.core.gui.item.WindowItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
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
			if (! openWindow(player, context)) {
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

	public Window window(Player player, GuiContext context) {
		GuiContext childrenContext = context.setWindow(this).deeper(namespace);

		WindowUtility.WindowSettings settings = new WindowUtility.WindowSettings(true);
		List<Item> items = getNodes().stream()
			.filter(Objects::nonNull)
			.map(node -> WindowItem.of(node, childrenContext).addClickHandler(() -> settings.doReturn = false))
			.collect(Collectors.toList());

		return WindowUtility.pagedItemsWindow(player, context, items, normalizedDisplayName(), settings);
	}
}
