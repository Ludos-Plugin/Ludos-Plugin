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
 * An Item used by a Player to unset their own {@link Role}.
 */
public class ResetRoleItem extends EventItem<ResetRoleItem> {
	private final RoleManager manager;

	public ResetRoleItem(RoleManager manager) {
		this.manager = manager;
	}

	@Override
	public void handleClickInternal(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
		manager.userUnsetRole(player, player);
		notifyWindows();
	}

	@Override
	public ItemProvider getItemProvider(Player viewer) {
		ItemBuilder builder = new ItemBuilder(Material.BARRIER)
			.setDisplayName(
				new AdventureComponentWrapper(
					Component.text("Reset Role")
				)
			);

		Role.Builder currentRole = manager.getPlayerRole(viewer);
		if (currentRole != null) {
			builder.addLoreLines(new AdventureComponentWrapper(
				Component.text("Currently picked : ")
					.decoration(TextDecoration.ITALIC, true)
					.color(NamedTextColor.DARK_GRAY)
				.append(currentRole.normalizedDisplayName())
			));
		} else {
			builder.addLoreLines(new AdventureComponentWrapper(
				Component.text("No current role")
					.decoration(TextDecoration.ITALIC, true)
					.color(NamedTextColor.DARK_GRAY)
			));
		}

		return builder;
	}

}
