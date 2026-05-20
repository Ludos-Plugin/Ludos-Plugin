package fr.ludos.command.ludos;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import fr.ludos.Ludos;
import fr.ludos.command.CommandUtility;
import fr.ludos.command.Subcommand;
import fr.ludos.command.SubcommandManager;

public enum LudosSubcommand implements Subcommand {
	game(new SubcommandManager<>(GameSubcommand.values())) {
		@Override
		public String getDescription() {
			return "Manage Ludos games.";
		}
		@Override
		public boolean requireOp() {
			return false;
		}
	},
	role(new SubcommandManager<>(RoleSubcommand.values())) {
		@Override
		public String getDescription() {
			return "Manage Ludos roles.";
		}
		@Override
		public boolean requireOp() {
			return false;
		}
	},
	group(new SubcommandManager<>(GroupSubcommand.values())) {
		@Override
		public String getDescription() {
			return "Manage Ludos groups.";
		}
		@Override
		public boolean requireOp() {
			return false;
		}
	},
	cheats(new SubcommandManager<>(CheatsSubcommand.values())) {
		@Override
		public String getDescription() {
			return "Use operator-only cheats on held game items.";
		}
		@Override
		public boolean requireOp() {
			return true;
		}
	},
	guidebook {
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
		public String getUsage() {
			return "[player]";
		}
		@Override
		public boolean requireOp() {
			return false;
		}
	},
	help {
		@Override
		public String getDescription() {
			return "Show help for Ludos commands.";
		}
		@Override
		public boolean requireOp() {
			return false;
		}
	};

	public static Stream<LudosSubcommand> getAllowedSubcommands(@Nullable CommandSender sender) {
		if (! (sender instanceof Player player) || ! player.isOp()) {
			return Arrays.stream(LudosSubcommand.values()).filter(s -> ! s.requireOp());
		}
		else {
			return Arrays.stream(LudosSubcommand.values());
		}
	}

	protected final SubcommandManager<?> subcommands;
	private LudosSubcommand(SubcommandManager<?> subcommands) {
		this.subcommands = subcommands;
	}
	private LudosSubcommand() {
		this(null);
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		return subcommands.onCommand(sender, command, label, args);
	}
	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		return subcommands.onTabComplete(sender, command, label, args);
	}
	@Override
	public String getUsage() {
		return subcommands.getUsage();
	}
}