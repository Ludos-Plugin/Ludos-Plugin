package fr.ludos.core.item;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.ludos.core.game.Game;

public interface SpecialItemInterface {
	public Game getGame();
	public ItemStack getStack();
	public Player getOwner();

	public void update();
}
