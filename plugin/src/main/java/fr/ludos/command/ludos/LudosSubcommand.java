package fr.ludos.command.ludos;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import fr.ludos.Ludos;
import fr.ludos.command.CommandUtility;
import fr.ludos.command.Subcommand;
import fr.ludos.game.Game;
import fr.ludos.role.Role;

public enum LudosSubcommand implements Subcommand {
	game() {
		@Override
		public String getDescription() {
			return "Manage Ludos games.";
		}
		@Override
		public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			if (args.length == 0) return false;

			String arg = args[0].toLowerCase();
			GameSubcommand option = Arrays.stream(GameSubcommand.values()).filter(o -> o.name().equalsIgnoreCase(arg)).findFirst().orElse(null);
			if (option == null) return false;

			return option.onCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
		}
		@Override
		public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			if (args.length <= 1) {
				return Arrays.stream(GameSubcommand.values())
					.map(GameSubcommand::name)
					.collect(Collectors.toList());
			}

			String arg = args[0].toLowerCase();
			GameSubcommand option = Arrays.stream(GameSubcommand.values()).filter(o -> o.name().equalsIgnoreCase(arg)).findFirst().orElse(null);
			if (option == null) return null;

			return option.onTabComplete(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
		}
		@Override
		public String getUsage(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label) {
			StringBuilder usage = new StringBuilder("/" + label + " game ");
			usage.append('<');
			usage.append(
				Arrays.stream(GameSubcommand.values()).sorted().map(GameSubcommand::name)
					.collect(Collectors.joining(" | "))
			);
			usage.append('>');
			usage.append(' ');
			usage.append('<');
			usage.append(
				Game.getRegistered().keySet().stream().sorted()
					.collect(Collectors.joining(" | "))
			);
			usage.append(' ');
			usage.append("[option]");
			return usage.toString();
		}
	},
	role() {
		@Override
		public String getDescription() {
			return "Manage Ludos roles.";
		}
		@Override
		public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			if (args.length == 0) return false;

			String arg = args[0].toLowerCase();
			RoleSubcommand option = Arrays.stream(RoleSubcommand.values()).filter(o -> o.name().equalsIgnoreCase(arg)).findFirst().orElse(null);
			if (option == null) return false;

			return option.onCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
		}
		@Override
		public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			if (args.length <= 1) {
				return Arrays.stream(RoleSubcommand.values())
					.map(RoleSubcommand::name)
					.collect(Collectors.toList());
			}

			String arg = args[0].toLowerCase();
			RoleSubcommand option = Arrays.stream(RoleSubcommand.values()).filter(o -> o.name().equalsIgnoreCase(arg)).findFirst().orElse(null);
			if (option == null) return null;

			return option.onTabComplete(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
		}
		@Override
		public String getUsage(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label) {
			StringBuilder usage = new StringBuilder("/" + label + " role ");
			usage.append('<');
			usage.append(
				Arrays.stream(RoleSubcommand.values()).sorted().map(RoleSubcommand::name)
					.collect(Collectors.joining(" | "))
			);
			usage.append('>');
			usage.append(' ');
			usage.append('<');
			usage.append(
				Role.getRegistered().keySet().stream().sorted()
					.collect(Collectors.joining(" | "))
			);
			usage.append(' ');
			usage.append("[option]");
			return usage.toString();
		}
	},
	guidebook() {
		@Override
		public String getDescription() {
			return "Give a Ludos guidebook to a player.";
		}
		@Override
		public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			Player player = CommandUtility.getPlayerFromArgsOrSender(args, 0, sender);
			if (player != null) {
				ItemStack book = Ludos.createGuidebook();
				player.getInventory().addItem(book);
			}
			return true;
		}
		@Override
		public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			if (args.length == 1)
				return CommandUtility.getOnlinePlayerNames();
			return null;
		}
		@Override
		public String getUsage(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label) {
			return "/" + label + " guidebook [player]";
		}
	},
	help() {
		@Override
		public String getDescription() {
			return "Show help for Ludos commands.";
		}
		@Override
		public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			if (args.length < 1) {
				sender.sendMessage(getUsage(sender, command, label));
				return true;
			}

			String arg = args[0].toLowerCase();
			LudosSubcommand option = Arrays.stream(LudosSubcommand.values()).filter(o -> o.name().equalsIgnoreCase(arg)).findFirst().orElse(null);
			if (option == null) return false;

			sender.sendMessage(option.getUsage(sender, command, label));
			return true;
		}
		@Override
		public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			if (args.length == 1) {
				return Arrays.stream(LudosSubcommand.values())
					.map(LudosSubcommand::name)
					.collect(Collectors.toList());
			}
			return null;
		}
		@Override
		public String getUsage(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label) {
			StringBuilder usage = new StringBuilder("/" + label + " ");

			usage.append('<');
			usage.append(
				Arrays.stream(LudosSubcommand.values()).map(LudosSubcommand::name)
					.collect(Collectors.joining(" | "))
			);
			usage.append('>');
			return usage.toString();
		}
	};
}