package fr.ludos.core.persistence.config.scope;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Triple;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import fr.ludos.core.Ludos;
import fr.ludos.core.command.Subcommand;
import fr.ludos.core.group.GroupManager;
import fr.ludos.core.gui.GuiContext;
import fr.ludos.core.gui.WindowUtility;
import fr.ludos.core.gui.WindowUtility.WindowSettings;
import fr.ludos.core.gui.item.ConfigProviderItem;
import fr.ludos.core.persistence.config.ConfigNode;
import fr.ludos.core.persistence.config.sectionProvider.ConfigSectionCollection;
import fr.ludos.core.persistence.config.sectionProvider.ConfigSectionContext;
import fr.ludos.core.persistence.config.sectionProvider.ConfigSectionProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.AbstractItemBuilder;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.window.Window;

/**
 * {@link Subcommand} used to select a given Configuration scope for subsequent Configuration Options.
 */
public class ScopeConfigMap extends ConfigSectionCollection {
	public static final String GLOBAL_KEY = "global";
	public static final String GROUP_KEY = "group";
	public static final String PLAYER_KEY = "player";

	private final TextComponent name;
	private final @Nullable ConfigNode globalRoot;
	private final @Nullable GlobalConfigProvider globalProvider;
	private final @Nullable ConfigNode groupRoot;
	private final @Nullable GroupConfigProvider groupProvider;
	private final @Nullable ConfigNode playerRoot;
	private final @Nullable PlayerConfigProvider playerProvider;

	public ScopeConfigMap(TextComponent name, Ludos ludos, GroupManager groupManager, @Nullable ConfigNode globalRoot, @Nullable ConfigNode groupRoot, @Nullable ConfigNode playerRoot) {
		this.name = name;
		this.globalRoot = globalRoot;
		this.groupRoot = groupRoot;
		this.playerRoot = playerRoot;
		this.globalProvider = globalRoot != null ? new GlobalConfigProvider(ludos) : null;
		this.groupProvider = groupRoot != null ? new GroupConfigProvider(groupManager) : null;
		this.playerProvider = playerRoot != null ? new PlayerConfigProvider(ludos) : null;
	}
	public ScopeConfigMap(TextComponent name, Ludos ludos, @Nullable ConfigNode globalRoot, @Nullable ConfigNode groupRoot, @Nullable ConfigNode playerRoot) {
		this(name, ludos, ludos.getGroupManager(), globalRoot, groupRoot, playerRoot);
	}
	public ScopeConfigMap(TextComponent name, Ludos ludos, GroupManager groupManager, @Nullable ConfigNode root) {
		this(name, ludos, groupManager, root, root, root);
	}
	public ScopeConfigMap(TextComponent name, Ludos ludos, @Nullable ConfigNode root) {
		this(name, ludos, root, root, root);
	}

	@Override
	public TextComponent displayName() {
		return name;
	}
	@Override
	public @NotNull Set<@NotNull String> options(CommandSender sender) {
		HashSet<String> set = new HashSet<>();
		if (globalRoot != null) set.add(GLOBAL_KEY);
		if (groupRoot != null) set.add(GROUP_KEY);
		if (playerRoot != null) set.add(PLAYER_KEY);
		return set;
	}
	@Override
	public Collection<ConfigNode> getNodes() {
		return Set.of(globalRoot, groupRoot, playerRoot);
	}
	@Override
	public ConfigNode getNode(String key) {
		switch (key) {
			case GLOBAL_KEY:
				return globalRoot;
			case GROUP_KEY:
				return groupRoot;
			case PLAYER_KEY:
				return playerRoot;
			default:
				return null;
		}
	}
	@Override
	public @NotNull ConfigSectionProvider getProvider(String key, CommandSender sender) {
		switch (key) {
			case GLOBAL_KEY:
				return globalProvider;
			case GROUP_KEY:
				return groupProvider;
			case PLAYER_KEY:
				return playerProvider;
			default:
				return null;
		}
	}
	@Override
	public @NotNull AbstractItemBuilder<?> getItem(String key, CommandSender sender) {
		switch (key) {
			case GLOBAL_KEY:
				if (globalRoot == null) return null;
				return getGlobalItem(sender);
			case GROUP_KEY:
				if (groupRoot == null) return null;
				return getGroupItem(sender);
			case PLAYER_KEY:
				if (playerRoot == null) return null;
				return getPlayerItem(sender);
			default:
				return null;
		}
	}

	@Override
	public Window window(Player player, GuiContext context) {
		Set<String> options = options(player);
		if (options.isEmpty()) return null;

		WindowUtility.WindowSettings state = new WindowSettings(true);

		List<Triple<String, ConfigSectionProvider, ConfigNode>> nodes = options.stream()
			.map(key -> {
				ConfigSectionProvider provider = getProvider(key, player);
				if (provider == null || ! provider.checkAuthorizationSilent(player)) return null;

				ConfigNode node = getNode(key);

				return Triple.of(key, provider, node);
			})
			.filter(Objects::nonNull)
			.toList();

		if (nodes.size() == 1) {
			Triple<String, ConfigSectionProvider, ConfigNode> item = nodes.get(0);
			ConfigNode node = item.getRight();

			return node.window(player, context.setConfig(new ConfigSectionContext(item.getMiddle())));
		}

		List<Item> items = nodes.stream()
			.map(item -> new ConfigProviderItem(context, item.getMiddle(), item.getRight()) {
					@Override
					public ItemProvider getItemProvider(Player viewer) {
						return getItem(item.getLeft(), player);
					}
				}.addClickHandler(state::disableModalReturn)
			)
			.collect(Collectors.toList());

		return WindowUtility.pagedItemsWindow(player, context, items, normalizedDisplayName(), state);
	}

	protected @NotNull AbstractItemBuilder<?> getGlobalItem(CommandSender sender) {
		return new ItemBuilder(Material.NETHER_STAR)
			.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DYE, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_POTION_EFFECTS)
			.setDisplayName(new AdventureComponentWrapper(
				Component.text("Server-wide Configuration")
					.decoration(TextDecoration.ITALIC, false)
					.color(NamedTextColor.LIGHT_PURPLE)
			))
			.addLoreLines(new AdventureComponentWrapper(
				Component.text("Configure options for the entire server.")
					.decoration(TextDecoration.ITALIC, false)
					.color(NamedTextColor.GRAY)
			));
	}
	protected @NotNull AbstractItemBuilder<?> getGroupItem(CommandSender sender) {
		return new ItemBuilder(Material.BLUE_BANNER)
			.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DYE, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_POTION_EFFECTS)
			.setDisplayName(new AdventureComponentWrapper(
				Component.text("Group-wide Configuration")
					.decoration(TextDecoration.ITALIC, false)
					.color(NamedTextColor.GREEN)
			))
			.addLoreLines(new AdventureComponentWrapper(
				Component.text("Configure options for your group.")
					.decoration(TextDecoration.ITALIC, false)
					.color(NamedTextColor.GRAY)
			));
	}
	protected @NotNull AbstractItemBuilder<?> getPlayerItem(CommandSender sender) {
		return new ItemBuilder(Material.PLAYER_HEAD)
			.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DYE, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_POTION_EFFECTS)
			.setDisplayName(new AdventureComponentWrapper(
				Component.text("Personal Configuration")
					.decoration(TextDecoration.ITALIC, false)
					.color(NamedTextColor.BLUE)
			))
			.addLoreLines(new AdventureComponentWrapper(
				Component.text("Configure options for yourself.")
					.decoration(TextDecoration.ITALIC, false)
					.color(NamedTextColor.GRAY)
			));
	}

}
