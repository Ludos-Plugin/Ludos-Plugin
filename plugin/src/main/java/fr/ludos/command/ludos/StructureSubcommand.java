package fr.ludos.command.ludos;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import fr.ludos.Ludos;
import fr.ludos.command.Subcommand;
import fr.ludos.structure.service.StructureService;

public enum StructureSubcommand implements Subcommand {
	pos1() {
		@Override
		public String getDescription() {
			return "Set structure selection position 1.";
		}

		@Override
		public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			if (!(sender instanceof Player player)) {
				sender.sendMessage("Player only command");
				return true;
			}

			structureService.setPos1(player, player.getLocation());
			sender.sendMessage("pos1 set: " + format(player.getLocation()));
			return true;
		}

		@Override
		public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			return null;
		}

		@Override
		public String getUsage(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label) {
			return "/" + label + " structure pos1";
		}
	},
	pos2() {
		@Override
		public String getDescription() {
			return "Set structure selection position 2.";
		}

		@Override
		public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			if (!(sender instanceof Player player)) {
				sender.sendMessage("Player only command");
				return true;
			}

			structureService.setPos2(player, player.getLocation());
			sender.sendMessage("pos2 set: " + format(player.getLocation()));
			return true;
		}

		@Override
		public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			return null;
		}

		@Override
		public String getUsage(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label) {
			return "/" + label + " structure pos2";
		}
	},
	save() {
		@Override
		public String getDescription() {
			return "Save selected blocks into a structure file.";
		}

		@Override
		public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			if (!(sender instanceof Player player)) {
				sender.sendMessage("Player only command");
				return true;
			}

			if (args.length < 1) {
				return false;
			}

			String name = args[0];
			StructureService.SaveResult result = structureService.saveSelection(player, name);
			if (!result.success()) {
				sender.sendMessage("Save failed: " + result.error());
				return true;
			}

			sender.sendMessage(
				"Structure saved: " + name
					+ " | blocks=" + result.blockCount()
					+ " | size=" + result.sizeX() + "x" + result.sizeY() + "x" + result.sizeZ()
			);
			return true;
		}

		@Override
		public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			if (args.length == 1) {
				return Collections.singletonList("my_building");
			}

			return null;
		}

		@Override
		public String getUsage(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label) {
			return "/" + label + " structure save <name>";
		}
	},
	paste() {
		@Override
		public String getDescription() {
			return "Paste a saved structure at target location.";
		}

		@Override
		public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			if (args.length < 1) {
				return false;
			}

			String name = args[0];
			Location target = resolvePasteLocation(sender, args);
			if (target == null) {
				return false;
			}

			StructureService.PasteResult result = structureService.paste(name, target);
			if (!result.success()) {
				sender.sendMessage("Paste failed: " + result.error());
				return true;
			}

			sender.sendMessage(
				"Structure pasted: " + result.name()
					+ " | blocks=" + result.blockCount()
					+ " | size=" + result.sizeX() + "x" + result.sizeY() + "x" + result.sizeZ()
					+ " | at=" + format(target)
			);
			return true;
		}

		@Override
		public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			if (args.length == 1) {
				return structureService.listStructures();
			}

			if (args.length == 2) {
				return Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList());
			}

			if (args.length >= 3 && args.length <= 5) {
				return Collections.singletonList("0");
			}

			return null;
		}

		@Override
		public String getUsage(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label) {
			return "/" + label + " structure paste <name> [world x y z]";
		}
	},
	remove() {
		@Override
		public String getDescription() {
			return "Remove a saved structure.";
		}

		@Override
		public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			if (args.length < 1) {
				return false;
			}

			boolean removed = structureService.remove(args[0]);
			sender.sendMessage(removed ? "Structure removed" : "Structure not found");
			return true;
		}

		@Override
		public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			if (args.length == 1) {
				return structureService.listStructures();
			}

			return null;
		}

		@Override
		public String getUsage(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label) {
			return "/" + label + " structure remove <name>";
		}
	},
	list() {
		@Override
		public String getDescription() {
			return "List saved structures.";
		}

		@Override
		public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			List<String> structures = structureService.listStructures();
			if (structures.isEmpty()) {
				sender.sendMessage("No saved structure");
				return true;
			}

			sender.sendMessage("Structures: " + String.join(", ", structures));
			return true;
		}

		@Override
		public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			return null;
		}

		@Override
		public String getUsage(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label) {
			return "/" + label + " structure list";
		}
	},
	help() {
		@Override
		public String getDescription() {
			return "Show help for structure commands.";
		}

		@Override
		public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			if (args.length < 1) {
				sender.sendMessage(getUsage(sender, command, label));
				return true;
			}

			String arg = args[0].toLowerCase();
			StructureSubcommand option = Arrays.stream(StructureSubcommand.values())
				.filter(o -> o != help)
				.filter(o -> o.name().equals(arg))
				.findFirst()
				.orElse(null);
			if (option == null) {
				return false;
			}

			sender.sendMessage(option.getUsage(sender, command, label));
			return true;
		}

		@Override
		public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			if (args.length == 1) {
				return Arrays.stream(StructureSubcommand.values())
					.filter(o -> o != help)
					.map(StructureSubcommand::name)
					.collect(Collectors.toList());
			}

			return null;
		}

		@Override
		public String getUsage(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label) {
			return "/" + label + " structure <"
				+ Arrays.stream(StructureSubcommand.values())
					.filter(o -> o != help)
					.map(StructureSubcommand::name)
					.collect(Collectors.joining(" | "))
				+ "> [option]";
		}
	};

	public static final String arg = "structure";
	private static final StructureService structureService = new StructureService(JavaPlugin.getPlugin(Ludos.class));

	private static String format(Location location) {
		String worldName = location.getWorld() == null ? "null" : location.getWorld().getName();
		return worldName + ":" + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
	}

	private static Location resolvePasteLocation(CommandSender sender, String[] args) {
		if (args.length == 1) {
			if (!(sender instanceof Player player)) {
				return null;
			}

			return player.getLocation().toBlockLocation();
		}

		if (args.length == 5) {
			World world = Bukkit.getWorld(args[1]);
			if (world == null) {
				return null;
			}

			try {
				int x = Integer.parseInt(args[2]);
				int y = Integer.parseInt(args[3]);
				int z = Integer.parseInt(args[4]);
				return new Location(world, x, y, z);
			} catch (NumberFormatException exception) {
				return null;
			}
		}

		return null;
	}
}