package fr.ludos.command;

import java.util.List;

import org.bukkit.entity.Player;

import fr.ludos.item.huntsman.HuntsmanBow;
import fr.ludos.item.huntsman.HuntsmanBowBranches;
import fr.ludos.item.huntsman.HuntsmanLevelSelector;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

public class SetBowLevelCommand implements TabExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			return false;
		}

		Player player = Bukkit.getPlayer(sender.getName());
		if (player == null) {
			return false;
		}

		Integer level;
		try {
			level = Integer.parseInt(args[0].trim());
		} catch (Exception e) {
			return true;
		}

		HuntsmanBow bow = HuntsmanBow.findIn(player.getInventory(), HuntsmanBow::getItem);
		if (bow == null) {
			return true;
		}

		bow.setBranch(HuntsmanBowBranches.values[level]);

		// HuntsmanLevelSelector grimoire = HuntsmanLevelSelector.findIn(player.getInventory(), HuntsmanLevelSelector::getItem);
		// if (grimoire == null) {
		// 	return true;
		// }
		// player.getInventory().removeItem(grimoire.getStack());
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		return null;
	}
}