package fr.ludos.role;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class RogueRole extends JavaPlugin {

	@Override
	public void onEnable() {
		getLogger().info("StealCommand plugin enabled!");
	}

	@Override
	public void onDisable() {
		getLogger().info("StealCommand plugin disabled!");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player player) {
			if (args.length == 1) {
				Player target = Bukkit.getPlayer(args[0]);
				if (target != null) {
					stealRandomItem(player, target);
					return true;
				} else {
					player.sendMessage("Le joueur cible n'est pas en ligne !");
					return false;
				}
			} else {
				player.sendMessage("Utilisation: /steal <joueur>");
				return false;
			}
		}
		return false;
	}

	private void stealRandomItem(Player player, Player target) {
		Inventory targetInventory = target.getInventory();
		Random random = new Random();

		// Vérifier si l'inventaire n'est pas vide
		if (targetInventory.firstEmpty() == -1 && targetInventory.getSize() > 0) {
			// Obtenir un emplacement d'inventaire aléatoire
			int randomSlot = random.nextInt(targetInventory.getSize());

			// Obtenir l'item à cet emplacement
			ItemStack stolenItem = targetInventory.getItem(randomSlot);

			if (stolenItem != null) {
				// Retirer l'item de l'inventaire de la cible
				targetInventory.setItem(randomSlot, null);

				// Ajouter l'item volé à l'inventaire du joueur
				if (player.getInventory().firstEmpty() != -1) {
					player.getInventory().addItem(stolenItem);
					player.sendMessage("Vous avez volé un(e) " + stolenItem.getType().toString() + " à " + target.getName() + "!");
				} else {
					// Si l'inventaire du joueur est plein, drop l'item au sol
					player.getWorld().dropItem(player.getLocation(), stolenItem);
					player.sendMessage("Votre inventaire est plein, l'item volé a été droppé au sol !");
				}
			} else {
				player.sendMessage("L'inventaire de la cible à cet emplacement est vide !");
			}
		} else {
			player.sendMessage("L'inventaire de la cible est vide !");
		}
	}
}