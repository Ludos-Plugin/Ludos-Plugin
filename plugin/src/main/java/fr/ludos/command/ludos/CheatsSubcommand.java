package fr.ludos.command.ludos;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import fr.ludos.command.Subcommand;
import fr.ludos.game.Game;
import fr.ludos.group.Group;
import fr.ludos.item.LevelItem;
import fr.ludos.item.LevelItemInterface;
import fr.ludos.item.MultiLevelBranchItem;
import fr.ludos.item.SpecialItem;

public enum CheatsSubcommand implements Subcommand {
	xp {
		@Override
		public String getDescription() {
			return "Add XP to the held level item.";
		}
		@Override
		public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			if (args.length != 1) return false;

			Integer amount = parsePositiveInt(args[0]);
			if (amount == null) return false;

			if (! (sender instanceof Player player)) {
				sender.sendMessage("Only players can use cheats.");
				return true;
			}
			if (!player.isOp()) {
				sender.sendMessage("Only server operators can use cheats.");
				return true;
			}

			Group group = Group.getGroupOfPlayer(player);
			if (group == null) {
				sender.sendMessage("You are not in a group.");
				return true;
			}

			Game game = group.getGame();
			if (game == null) {
				sender.sendMessage("You are not in a game.");
				return true;
			}

			ItemStack heldItem = player.getInventory().getItemInMainHand();
			if (heldItem == null || heldItem.getType().isAir()) {
				sender.sendMessage("Hold a LevelItem or MultiLevelBranchItem in your main hand.");
				return true;
			}

			SpecialItem item = findHeldSpecialItem(game, heldItem);
			if (item == null) {
				sender.sendMessage("Held item is not a valid game item.");
				return true;
			}

			if (item instanceof LevelItemInterface levelItem) {
				levelItem.addXp(amount);
			} else {
				sender.sendMessage("Held item is not a valid level item.");
				return true;
			}

			sender.sendMessage("Added " + amount + " XP to your held item.");
			return true;
		}
		@Override
		public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			return null;
		}
		@Override
		public String getUsage(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label) {
			return "/" + label + " cheats xp <amount>";
		}
	},
	level {
		@Override
		public String getDescription() {
			return "Add levels to the held level item.";
		}
		@Override
		public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			if (args.length != 1) return false;

			Integer amount = parsePositiveInt(args[0]);
			if (amount == null) return false;

			if (! (sender instanceof Player player)) {
				sender.sendMessage("Only players can use cheats.");
				return true;
			}
			if (!player.isOp()) {
				sender.sendMessage("Only server operators can use cheats.");
				return true;
			}

			Group group = Group.getGroupOfPlayer(player);
			if (group == null) {
				sender.sendMessage("You are not in a group.");
				return true;
			}

			Game game = group.getGame();
			if (game == null) {
				sender.sendMessage("You are not in a game.");
				return true;
			}

			ItemStack heldItem = player.getInventory().getItemInMainHand();
			if (heldItem == null || heldItem.getType().isAir()) {
				sender.sendMessage("Hold a LevelItem or MultiLevelBranchItem in your main hand.");
				return true;
			}

			SpecialItem item = findHeldSpecialItem(game, heldItem);
			if (item == null) {
				sender.sendMessage("Held item is not a valid game item.");
				return true;
			}

			if (item instanceof LevelItemInterface levelItem) {
				levelItem.addLvl(amount);
			} else {
				sender.sendMessage("Held item is not a valid level item.");
				return true;
			}

			sender.sendMessage("Added " + amount + " level(s) to your held item.");
			return true;
		}
		@Override
		public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			return null;
		}
		@Override
		public String getUsage(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label) {
			return "/" + label + " cheats level <amount>";
		}
	},
	help {
		@Override
		public String getDescription() {
			return "Show cheats usage.";
		}
		@Override
		public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			sender.sendMessage(getUsage(sender, command, label));
			return true;
		}
		@Override
		public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			return null;
		}
		@Override
		public String getUsage(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label) {
			return "/" + label + " cheats <xp|level> <amount>";
		}
	};

	private static @Nullable Integer parsePositiveInt(String raw) {
		try {
			int value = Integer.parseInt(raw);
			return value > 0 ? value : null;
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private static @Nullable SpecialItem findHeldSpecialItem(Game game, ItemStack stack) {
		for (SpecialItem.Events<?> event : game.getActiveItems()) {
			SpecialItem item = event.getItem(stack);
			if (item != null) {
				return item;
			}
		}
		return null;
	}

	@Override
	public boolean requireOp() {
		return true;
	}
}
