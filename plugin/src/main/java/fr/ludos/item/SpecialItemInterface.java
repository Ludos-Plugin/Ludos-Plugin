package fr.ludos.item;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.ludos.game.Game;

public interface SpecialItemInterface {
	public Game getGame();
	public ItemStack getStack();
	public Player getOwner();

	public void update();
}
