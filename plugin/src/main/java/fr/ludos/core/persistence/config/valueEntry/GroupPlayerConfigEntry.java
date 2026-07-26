package fr.ludos.core.persistence.config.valueEntry;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.group.Group;
import fr.ludos.core.group.GroupManager;
import fr.ludos.core.gui.BorderItem;
import fr.ludos.core.gui.ChangePageItem;
import fr.ludos.core.gui.PlayerItemBuilder;
import fr.ludos.core.gui.SinglePickerItem;
import fr.ludos.core.gui.configValue.ResetValueItem;
import fr.ludos.core.gui.configValue.display.PlayerDisplayItem;
import fr.ludos.core.persistence.config.sectionProvider.ConfigSectionContext;
import fr.ludos.core.persistence.serializer.PlayerSerializer;
import fr.ludos.core.persistence.serializer.Serializer;
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
import xyz.xenondevs.invui.item.builder.AbstractItemBuilder;
import xyz.xenondevs.invui.window.Window;

/**
 * {@link ConfigEntry} for a single {@link OfflinePlayer} instance, present in the {@link CommandSender}'s current {@link Group}.
 */
public class GroupPlayerConfigEntry extends ConfigEntry<OfflinePlayer, String> {
	private final GroupManager groupManager;
	private final boolean excludeSelf;

	public GroupPlayerConfigEntry(GroupManager groupManager, @NotNull TextComponent name, AbstractItemBuilder<?> displayItem, @NotNull String key, boolean excludeSelf) {
		super(name, displayItem, key);
		this.groupManager = Objects.requireNonNull(groupManager);
		this.excludeSelf = excludeSelf;
	}
	public GroupPlayerConfigEntry(GroupManager groupManager, @NotNull TextComponent name, AbstractItemBuilder<?> displayItem, @NotNull String key) {
		this(groupManager, name, displayItem, key, false);
	}

	@Override
	public final Serializer<OfflinePlayer, String> getSerializer() {
		return PlayerSerializer.INSTANCE;
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
	public boolean validateValue(OfflinePlayer value, CommandSender sender) {
		if (! (sender instanceof Player player )) return false;

		Group group = groupManager.getGroupOfPlayer(player);
		return (group.isPlayer(value));
	}

	@Override
	public OfflinePlayer defaultValue() {
		return null;
	}


	@Override
	public Window configGui(Player player, ConfigSectionContext context) {
		ConfigurationSection config = context.getConfig(player);

		PagedGui.Builder<Item> gui = PagedGui.items()
			.setStructure(new Structure(
				"# # # # # # # # #",
				"# x x x x x x x #",
				"# x x x x x x x #",
				"# x x x x x x x #",
				"V # # # P # # # R")
			)
			.addIngredient('#', BorderItem.INSTANCE)
			.addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
			.addIngredient('P', ChangePageItem.INSTANCE)
			.addIngredient('V', new PlayerDisplayItem(this, context))
			.addIngredient('R', new ResetValueItem<>(this, context));

		Group group = groupManager.getGroupOfPlayer(player);
		Collection<OfflinePlayer> players = group == null
			? Collections.emptyList()
			: group.getPlayers();

		List<Item> items = players.stream()
			.map(playerValue ->
				new SinglePickerItem<OfflinePlayer, Item>(playerValue, this, context) {
					@Override
					public ItemProvider getItemProvider(PagedGui<Item> gui) {
						PlayerItemBuilder item = new PlayerItemBuilder(playerValue);
						OfflinePlayer currentPlayer = getValueOrDefault(config);

						return playerValue.equals(currentPlayer)
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
			.setTitle(new AdventureComponentWrapper(displayName()))
			.setGui(gui.setContent(items))
			.build(player);
	}
}
