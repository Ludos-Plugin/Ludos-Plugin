package fr.ludos.command;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Arrays;

/**
 * MonsterCommand class implements the Bukkit CommandExecutor interface and represents a command
 * to give players a custom spawn egg for a special zombie entity known as "Bomber."
 * The spawn egg, when used, allows players to spawn a Bomber zombie that explodes when approached.
 * <br><br>
 * Features:
 * <br><br>
 * - Executes the /bomberzombie command, giving the player a custom spawn egg for a Bomber zombie.
 * <br><br>
 * - The custom spawn egg has a display name and lore.
 * <br><br>
 * - The spawn egg, when used, spawns a Bomber zombie that explodes when approached.
 * <br><br>
 * Usage:
 * <br><br>
 * - Register the command in the plugin's main class using: getCommand("bomberzombie").setExecutor(new MonsterCommand());
 * <br><br>
 * - Players can use the /bomberzombie command to receive a custom spawn egg for a Bomber zombie.
 *  <br><br>
 * Example:
 * <br><br>
 * /bomberzombie
 * <br><br>
 * @param sender  The command sender, expected to be a Player.
 * @param command The command that was executed.
 * @param label   The alias used for the command.
 * @param args    The arguments provided with the command.
 * @return true if the command was handled successfully, false otherwise.
 *
 * @author feur25
 * @version 1.0
 * @see org.bukkit.command.CommandExecutor
 * @see org.bukkit.command.Command
 * @see org.bukkit.command.CommandSender
 * @see org.bukkit.entity.Player
 * @see org.bukkit.inventory.ItemStack
 * @see org.bukkit.Material
 * @see org.bukkit.inventory.meta.ItemMeta
 * @see java.util.Arrays
 */

public class MonsterCommand implements CommandExecutor {

	/** The display name for the custom spawn egg representing the Bomber zombie. */
	public static final String bomberZombieName = "Bomber";

	/**
	 * Executes the /bomberzombie command, giving the player a custom spawn egg for a Bomber zombie.
	 *
	 * @param sender  The command sender, expected to be a Player.
	 * @param command The command that was executed.
	 * @param label   The alias used for the command.
	 * @param args    The arguments provided with the command.
	 * @return true if the command was handled successfully, false otherwise.
	 */

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player player) {

			ItemStack zombieCustomEgg = new ItemStack(Material.ZOMBIE_SPAWN_EGG, 1);
			ItemMeta zombieCustomEggMeta = zombieCustomEgg.getItemMeta();

			zombieCustomEggMeta.setDisplayName(bomberZombieName);
			zombieCustomEggMeta.setLore(Arrays.asList("Spawns a bomber zombie, explodes if you near"));
			zombieCustomEgg.setItemMeta(zombieCustomEggMeta);

			player.getInventory().addItem(zombieCustomEgg);
		}

		return true;
	}
}