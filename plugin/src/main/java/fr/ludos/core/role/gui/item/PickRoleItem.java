package fr.ludos.core.role.gui.item;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.gui.item.EventItem;
import fr.ludos.core.role.Role;
import fr.ludos.core.role.RoleManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;

/**
 * An Item representing a {@link Role} that a Player can click to set as their own {@link Role}.
 */
public class PickRoleItem extends EventItem<PickRoleItem> {
	private final Role.Builder role;
	private final RoleManager manager;

	public PickRoleItem(Role.Builder role, RoleManager manager) {
		this.role = role;
		this.manager = manager;
	}

	@Override
	public void handleClickInternal(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
		if (manager.isPlayerRole(player, role.getId())) {
			manager.userUnsetRole(player, player);
		} else {
			manager.userSetRole(player, player, role.getId());
		}
		notifyWindows();
	}

	@Override
	public ItemProvider getItemProvider(Player viewer) {
		ItemBuilder res = new ItemBuilder(Material.PAPER)
			.setDisplayName(new AdventureComponentWrapper(
				Component.text("Pick Role")
					.decoration(TextDecoration.ITALIC, false)
					.color(NamedTextColor.GRAY)
			));
		if (manager.isPlayerRole(viewer, role.getId())) {
			res.addLoreLines(new AdventureComponentWrapper(
				Component.text("Currently Picked")
					.decoration(TextDecoration.ITALIC, true)
					.color(NamedTextColor.DARK_GRAY)
			), new AdventureComponentWrapper(
				Component.text("Click to unset")
					.decoration(TextDecoration.ITALIC, true)
					.color(NamedTextColor.DARK_GRAY)
			));
		} else {
			res.addLoreLines(new AdventureComponentWrapper(
				Component.text("Click to Pick")
					.decoration(TextDecoration.ITALIC, true)
					.color(NamedTextColor.DARK_GRAY)
			));
		}

		return res;
	}
}
