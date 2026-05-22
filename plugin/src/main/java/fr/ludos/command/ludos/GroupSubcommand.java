package fr.ludos.command.ludos;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import fr.ludos.Ludos;
import fr.ludos.command.CommandUtility;
import fr.ludos.command.ConfigSubcommandManager;
import fr.ludos.command.Subcommand;
import fr.ludos.command.SubcommandManager;
import fr.ludos.game.Game;
import fr.ludos.group.Group;
import fr.ludos.group.GroupRightsOption;
import fr.ludos.group.Group.JoinMethod;
import fr.ludos.group.Group.JoinResult;

public enum GroupSubcommand implements Subcommand {
	create() {
		@Override
		public String getDescription() {
			return "Create a new group.";
		}
		@Override
		public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			if (!(sender instanceof Player player)) {
				sender.sendMessage("Only players can create groups.");
				return true;
			}

			Group group = Group.createGroup(player, null);

			Set<Player> members = CommandUtility.getPlayersFromArgs(args, 0, sender);
			for (Player member : members) {
				group.requestPlayerJoin(member, JoinMethod.Invite);
			}

			Ludos plugin = JavaPlugin.getPlugin(Ludos.class);
			plugin.saveConfig();

			return true;
		}
		@Override
		public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			return CommandUtility.getOnlinePlayerNames();
		}
		@Override
		public String getUsage() {
			return "[member1] [member2] ...";
		}
		@Override
		public boolean requireOp() {
			return false;
		}
	},
	disband() {
		@Override
		public String getDescription() {
			return "Disband the current group.";
		}
		@Override
		public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			if (!(sender instanceof Player player)) {
				sender.sendMessage("Only players can disband groups.");
				return true;
			}

			Group group = Group.getGroupOfPlayer(player);
			if (group == null) {
				sender.sendMessage("You are not in a group.");
				return true;
			}

			boolean membersCanManage = GroupConfigs.getGroupRightsOption(group.getConfig()).canManage();
			if (! group.isLeader(player) && ! membersCanManage) {
				sender.sendMessage("Only the group leader can disband the group.");
				return true;
			}

			group.disband();

			Ludos plugin = JavaPlugin.getPlugin(Ludos.class);
			plugin.saveConfig();

			return true;
		}
		@Override
		public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			return null;
		}
		@Override
		public String getUsage() {
			return "";
		}
		@Override
		public boolean requireOp() {
			return false;
		}
	},
	invite() {
		@Override
		public String getDescription() {
			return "Invite a player to your group.";
		}
		@Override
		public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			if (args.length < 1) return false;

			if (!(sender instanceof Player player)) {
				sender.sendMessage("Only players can invite others to groups.");
				return true;
			}

			Group group = Group.getGroupOfPlayer(player);
			if (group == null) {
				sender.sendMessage("You are not in a group.");
				return true;
			}

			Set<Player> targets = CommandUtility.getPlayersFromArgs(args, 0, sender).stream()
				.filter(p -> ! group.isPlayer(p))
				.collect(Collectors.toSet());

			if (targets.isEmpty()) {
				sender.sendMessage("No valid player names provided.");
				return true;
			}

			boolean membersCanInvite = GroupConfigs.getGroupRightsOption(group.getConfig()).canInvite();
			if (! group.isLeader(player) && ! membersCanInvite) {
				sender.sendMessage("Only the group leader can invite new members.");
				return true;
			}

			boolean hasJoined = false;
			for (Player target : targets) {
				switch (group.requestPlayerJoin(target, JoinMethod.Invite)) {
					case Succeeded:
						hasJoined = true;
					case Failed:
						targets.remove(target);
						break;
					default:
						break;
				}
			}
			if (hasJoined) {
				Ludos plugin = JavaPlugin.getPlugin(Ludos.class);
				plugin.saveConfig();
			}

			if (targets.size() > 0) {
				player.sendMessage("Invited " + targets.stream().map(Player::getName).collect(Collectors.joining(", ")) + " to the group.");
			}

			return true;
		}
		@Override
		public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			if (! (sender instanceof Player player)) return null;

			Group group = Group.getGroupOfPlayer(player);
			if (group == null) return null;

			HashSet<Player> onlines = Bukkit.getServer().getOnlinePlayers()
				.stream()
				.collect(Collectors.toCollection(HashSet::new));
			onlines.removeAll(group.getPlayers());

			return onlines.stream()
				.map(Player::getName)
				.toList();
		}
		@Override
		public String getUsage() {
			return "[player1] [player2] ...";
		}
		@Override
		public boolean requireOp() {
			return false;
		}
	},
	join() {
		@Override
		public String getDescription() {
			return "Join a group.";
		}
		@Override
		public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			if (!(sender instanceof Player player)) {
				sender.sendMessage("Only players can join groups.");
				return true;
			}
			if (args.length < 1) return false;

			String leaderName = args[0];
			Player leader = Bukkit.getPlayerExact(leaderName);
			if (leader == null) {
				sender.sendMessage("Player not found: " + leaderName);
				return true;
			}

			Group group = Group.getGroupOfPlayer(leader);
			if (group == null) {
				sender.sendMessage(leader.getName() + " is not in a group.");
				return true;
			}

			JoinResult res = group.requestPlayerJoin(player, JoinMethod.Join);
			switch (res) {
				case Succeeded:
					Ludos plugin = JavaPlugin.getPlugin(Ludos.class);
					plugin.saveConfig();
					break;
				case Requested:
					player.sendMessage("Requested to join " + leaderName + "'s group.");
				default:
					break;
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
			return "<memberName>";
		}
		@Override
		public boolean requireOp() {
			return false;
		}
	},
	leave() {
		@Override
		public String getDescription() {
			return "Leave the current group.";
		}
		@Override
		public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			if (!(sender instanceof Player player)) {
				sender.sendMessage("Only players can leave groups.");
				return true;
			}

			Group group = Group.getGroupOfPlayer(player);
			if (group == null) {
				sender.sendMessage("You are not in a group.");
				return true;
			}

			group.removePlayer(player, false);

			Ludos plugin = JavaPlugin.getPlugin(Ludos.class);
			plugin.saveConfig();

			return true;
		}
		@Override
		public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			return null;
		}
		@Override
		public String getUsage() {
			return "";
		}
		@Override
		public boolean requireOp() {
			return false;
		}
	},
	kick() {
		@Override
		public String getDescription() {
			return "Kick a player from the group.";
		}
		@Override
		public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			if (!(sender instanceof Player player)) {
				sender.sendMessage("Only players can kick members from groups.");
				return true;
			}
			if (args.length < 1) return false;

			Group group = Group.getGroupOfPlayer(player);
			if (group == null) {
				sender.sendMessage("You are not in a group.");
				return true;
			}

			boolean membersCanManage = GroupConfigs.getGroupRightsOption(group.getConfig()).canManage();
			if (! group.isLeader(player) && ! membersCanManage) {
				sender.sendMessage("Only the group leader can kick members.");
				return true;
			}

			List<String> targetNames = Arrays.asList(args);
			for (String targetName : targetNames) {
				OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

				if (group.isMember(target)) {
					group.removePlayer(target, true);
					player.sendMessage("Kicked " + targetName + " from the group.");
				} else {
					sender.sendMessage(targetName + " is not a member of your group.");
				}

			}

			Ludos plugin = JavaPlugin.getPlugin(Ludos.class);
			plugin.saveConfig();

			return true;
		}
		@Override
		public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			if (!(sender instanceof Player player)) return null;

			Group group = Group.getGroupOfPlayer(player);
			if (group == null) return null;

			if (! group.isLeader(player)) return null;

			return group.getMembers().stream()
				.map(OfflinePlayer::getName)
				.collect(Collectors.toList());
		}
		@Override
		public String getUsage() {
			return "[member1] [member2] ...";
		}
		@Override
		public boolean requireOp() {
			return false;
		}
	},
	config() {
		@Override
		public String getDescription() {
			return "Configure a game for this group.";
		}
		@Override
		public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			if (args.length < 1) return false;

			if (!(sender instanceof Player player)) {
				sender.sendMessage("Only players can configure groups.");
				return true;
			}

			Group group = Group.getGroupOfPlayer(player);
			if (group == null) {
				sender.sendMessage("You are not in a group.");
				return true;
			}

			GroupRightsOption opt = GroupConfigs.getGroupRightsOption(group.getConfig());
			boolean membersCanConfig = opt.canConfig();
			if (! group.isLeader(player) && ! membersCanConfig) {
				sender.sendMessage("Only the group leader can configure the group. " + opt);
				return true;
			}

			boolean res = configCommand.onCommand(sender, command, label, group.getConfig(), args);

			if (res) {
				Ludos plugin = JavaPlugin.getPlugin(Ludos.class);
				Group.saveConfigGroup(plugin, group);
				plugin.saveConfig();
			}

			return res;
		}
		@Override
		public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			return configCommand.onTabComplete(sender, command, label, args);
		}
		@Override
		public String getUsage() {
			return "<" +
				Game.getRegistered().keySet().stream().sorted()
					.collect(Collectors.joining(" | "))
				+ "> [option]";
		}
		@Override
		public boolean requireOp() {
			return false;
		}
	},
	info() {
		@Override
		public String getDescription() {
			return "Get information about your current group.";
		}
		@Override
		public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			if (!(sender instanceof Player player)) {
				sender.sendMessage("Only players can get group information.");
				return true;
			}

			Group group = Group.getGroupOfPlayer(player);
			if (group == null) {
				sender.sendMessage("You are not in a group.");
				return true;
			}

			sender.sendMessage("Group leader: " + group.getLeader().getName());
			sender.sendMessage("Group members: " + group.getMembers().stream().map(OfflinePlayer::getName).collect(Collectors.joining(", ")));
			return true;
		}
		@Override
		public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			return null;
		}
		@Override
		public String getUsage() {
			return "";
		}
		@Override
		public boolean requireOp() {
			return false;
		}
	},
	help() {
		@Override
		public String getDescription() {
			return "Show help for group commands.";
		}
		@Override
		public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			if (args.length < 1) {
				sender.sendMessage(getUsage());
				return true;
			}

			String arg = args[0].toLowerCase();
			GroupSubcommand option = Arrays.stream(GroupSubcommand.values())
				.filter(o -> o != help)
				.filter(o -> o.name().equals(arg))
				.findFirst().orElse(null);
			if (option == null) return false;

			sender.sendMessage(option.getUsage());
			return true;
		}
		@Override
		public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			if (args.length == 1) {
				return Arrays.stream(GroupSubcommand.values())
					.filter(o -> o != help)
					.map(GroupSubcommand::name)
					.collect(Collectors.toList());
			}
			return null;
		}
		@Override
		public String getUsage() {
			return SubcommandManager.getUsage(
				Arrays.stream(GroupSubcommand.values())
					.filter(o -> o != help)
			);
		}
		@Override
		public boolean requireOp() {
			return false;
		}
	};


	protected final ConfigSubcommandManager<GroupConfigs> configCommand = new ConfigSubcommandManager<>(GroupConfigs.values());
}