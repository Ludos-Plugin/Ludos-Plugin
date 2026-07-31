package fr.ludos.core.persistence.config.valueEntry;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.group.Group;
import fr.ludos.core.group.GroupManager;
import fr.ludos.core.gui.GuiContext;
import fr.ludos.core.gui.configValue.ResetValueItem;
import fr.ludos.core.gui.item.BorderItem;
import fr.ludos.core.gui.item.ChangePageItem;
import fr.ludos.core.gui.item.MultiPickerItem;
import fr.ludos.core.gui.item.PlayerItemBuilder;
import fr.ludos.core.persistence.PersistentAccessor;
import fr.ludos.core.persistence.config.sectionProvider.ConfigSectionContext;
import fr.ludos.core.persistence.serializer.PlayerSerializer;
import fr.ludos.core.persistence.serializer.StringSetSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.gui.structure.Structure;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.window.Window;

/**
 * {@link ConfigEntry} for multiple {@link OfflinePlayer} instances, present in the {@link CommandSender}'s current {@link Group}.
 */
public abstract class GroupPlayersConfigEntry extends SetConfigEntry<OfflinePlayer> {
	private final GroupManager groupManager;
	private final @Nullable Integer limit;
	private final boolean excludeSelf;

	public GroupPlayersConfigEntry(GroupManager groupManager, @NotNull TextComponent name, @NotNull String key, String placeholder, @Nullable Integer limit, boolean excludeSelf) {
		super(name, key, new StringSetSerializer<>(PlayerSerializer.INSTANCE), placeholder);
		this.groupManager = Objects.requireNonNull(groupManager);
		this.limit = limit;
		this.excludeSelf = excludeSelf;
	}
	public GroupPlayersConfigEntry(GroupManager groupManager, @NotNull TextComponent name, @NotNull String key, String placeholder, @Nullable Integer limit) {
		this(groupManager, name, key, placeholder, limit, false);
	}
	public GroupPlayersConfigEntry(GroupManager groupManager, @NotNull TextComponent name, @NotNull String key, String placeholder, boolean excludeSelf) {
		this(groupManager, name, key, placeholder, null, excludeSelf);
	}
	public GroupPlayersConfigEntry(GroupManager groupManager, @NotNull TextComponent name, @NotNull String key, String placeholder) {
		this(groupManager, name, key, placeholder, false);
	}

	@Override
	public Set<String> options(CommandSender sender) {
		if (! (sender instanceof Player player )) return Collections.emptySet();

		Group group = groupManager.getGroupOfPlayer(player);
		if (group == null) return Collections.emptySet();

		Set<String> res = group.getPlayers().stream()
			.map(OfflinePlayer::getName)
			.collect(Collectors.toCollection(HashSet<String>::new));

		if (excludeSelf) {
			res.remove(player.getName());
		}

		return res;
	}

	@Override
	public boolean validateSingleValue(OfflinePlayer value, CommandSender sender) {
		if (! (sender instanceof Player player )) return false;

		Group group = groupManager.getGroupOfPlayer(player);
		if (group == null) return false;

		boolean res = group.isPlayer(value);
		return res;
	}

	@Override
	public Window configWindow(Player player, GuiContext context) {
		ConfigSectionContext configContext = context.configContext();
		if (configContext == null) return null;

		PersistentAccessor<Set<OfflinePlayer>> accessor = new PersistentAccessor<>(this, configContext, player);

		PagedGui.Builder<Item> gui = PagedGui.items()
			.setStructure(new Structure(
				"# # # # # # # # #",
				"# x x x x x x x #",
				"# x x x x x x x #",
				"# x x x x x x x #",
				"# # # # P # # # R")
			)
			.addIngredient('#', BorderItem.INSTANCE)
			.addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
			.addIngredient('P', ChangePageItem.INSTANCE)
			.addIngredient('R', new ResetValueItem<>(accessor));

		Group group = groupManager.getGroupOfPlayer(player);
		Collection<OfflinePlayer> players = group == null
			? Collections.emptyList()
			: group.getPlayers();

		List<Item> items = players.stream()
			.map(playerValue ->
				new MultiPickerItem<OfflinePlayer, Set<OfflinePlayer>, Item>(playerValue, accessor, HashSet::new) {
					@Override
					public ItemProvider getItemProvider(PagedGui<Item> gui) {
						PlayerItemBuilder item = new PlayerItemBuilder(playerValue);
						Set<OfflinePlayer> selected = accessor.getOrDefault();

						return selected != null && selected.contains(playerValue)
							? item.addItemFlags(ItemFlag.HIDE_ENCHANTS)
								.addEnchantment(Enchantment.CHANNELING, 1, false)
								.addLoreLines(new AdventureComponentWrapper(
									Component.text("Currently selected")
										.decoration(TextDecoration.ITALIC, false)
										.color(NamedTextColor.GRAY)
								))
							: item;
					}
				}
			)
			.collect(Collectors.toList());

		return Window.single()
			.setTitle(new AdventureComponentWrapper(normalizedDisplayName()))
			.setGui(gui.setContent(items))
			.addCloseHandler(() -> context.openPreviousWindow(player))
			.build(player);
	}
}
