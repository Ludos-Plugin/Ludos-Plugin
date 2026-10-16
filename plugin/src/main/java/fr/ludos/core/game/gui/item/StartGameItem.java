package fr.ludos.core.game.gui.item;

import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.game.Game;
import fr.ludos.core.game.GameManager;
import fr.ludos.core.group.Group;
import fr.ludos.core.gui.item.ControlEventItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;

/**
 * An item used to start a specific {@link Game}.
 */
public class StartGameItem extends ControlEventItem<StartGameItem, Gui> {
	private final Game.Builder game;
	private final GameManager manager;

	public StartGameItem(Game.Builder game, GameManager manager) {
		this.game = game;
		this.manager = manager;
	}

	@Override
	public void handleClickInternal(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
		AtomicReference<Group> outGroup = new AtomicReference<>();
		if (! manager.verifyPlayerCanStartGame(player, game, outGroup)) return;

		notifyActionHandlers();

		getGui().closeForAllViewers();
		manager.startGame(game, outGroup.get());
	}

	@Override
	public ItemProvider getItemProvider(Gui gui) {
		return new ItemBuilder(Material.JUKEBOX)
			.setDisplayName(new AdventureComponentWrapper(
				Component.text("Start Game")
					.color(NamedTextColor.GREEN)
					.decoration(TextDecoration.ITALIC, false)
			));
	}

}
