// package fr.ludos.item;

// import org.bukkit.Bukkit;
// import org.bukkit.Location;
// import org.bukkit.Material;
// import org.bukkit.block.Block;
// import org.bukkit.entity.Player;
// import org.bukkit.entity.Villager;
// import org.bukkit.inventory.ItemStack;
// import org.bukkit.util.Vector;

// import fr.ludos.Ludos;

// import java.util.ArrayList;
// import java.util.List;
// import java.util.Random;

// /**
//  * The MinerFamiliar class represents a familiar that autonomously mines blocks based on tasks.
//  * It is equipped with a pickaxe matching its owner's and performs mining tasks within a specified radius.
//  * Mined ores are sent directly to the owner's inventory.
//  * After completing a task, the familiar teleports to the owner and resumes a new task.
//  */

// public class MinerFamiliar {

// 	private Villager villager;
// 	private List<Task> tasks = new ArrayList<>();
// 	private final Ludos plugin;
// 	private final Player owner;

// 	/**
// 	 * Constructor for the MinerFamiliar class.
// 	 *
// 	 * @param owner  The owner (player) of the familiar.
// 	 * @param plugin The main plugin instance.
// 	 */

// 	public MinerFamiliar(Player owner, Ludos plugin) {
// 		this.owner = owner;
// 		this.plugin = plugin;
// 		summonFamiliar(owner);
// 		addTasks();
// 		startTaskScheduler();
// 	}

// 	/**
// 	 * Summons the familiar at the owner's location and configures its appearance and equipment.
// 	 *
// 	 * @param owner The owner (player) of the familiar.
// 	 */

// 	private void summonFamiliar(Player owner) {
// 		Location spawnLocation = owner.getLocation();
// 		villager = owner.getWorld().spawn(spawnLocation, Villager.class);

// 		// Configure the familiar (appearance, profession, etc.)
// 		villager.setAdult();
// 		villager.setCustomName(owner.getName() + "'s Familiar");
// 		villager.setCustomNameVisible(true);

// 		// Equip the familiar with the same pickaxe as its master
// 		ItemStack pickaxe = owner.getInventory().getItemInMainHand().clone();
// 		villager.getEquipment().setItemInMainHand(pickaxe);
// 	}

// 	/**
// 	 * Adds initial tasks to the familiar's task list.
// 	 */

// 	private void addTasks() {
// 		tasks.add(new MoveToRandomLocationTask());
// 		tasks.add(new MineBlockTask());
// 	}

// 	/**
// 	 * Starts the task scheduler to execute the familiar's tasks at regular intervals.
// 	 */

// 	private void startTaskScheduler() {
// 		Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
// 			if (!tasks.isEmpty()) {
// 				Task currentTask = tasks.get(0);

// 				if (currentTask.isComplete(villager)) {
// 					tasks.remove(0);
// 					if (!tasks.isEmpty()) {
// 						currentTask = tasks.get(0);
// 						currentTask.start(villager);
// 					} else {
// 						// Teleport to owner after completing tasks
// 						villager.teleport(owner.getLocation());
// 					}
// 				}

// 				currentTask.execute(villager);
// 			}
// 		}, 0L, 20L); // Execute every second (20 ticks)
// 	}

// 	/**
// 	 * The Task interface represents a task that the familiar can perform.
// 	 */

// 	private interface Task {
// 		boolean isComplete(Villager villager);
// 		void start(Villager villager);
// 		void execute(Villager villager);
// 	}

// 	/**
// 	 * The MoveToRandomLocationTask class represents a task where the familiar moves to a random location.
// 	 */

// 	private class MoveToRandomLocationTask implements Task {
// 		private Location targetLocation;

// 		@Override
// 		public boolean isComplete(Villager villager) {
// 			return villager.getLocation().distance(targetLocation) < 1.0;
// 		}

// 		@Override
// 		public void start(Villager villager) {
// 			Random random = new Random();
// 			double x = owner.getLocation().getX() + random.nextInt(10) - 5;
// 			double y = owner.getLocation().getY();
// 			double z = owner.getLocation().getZ() + random.nextInt(10) - 5;
// 			targetLocation = new Location(owner.getWorld(), x, y, z);
// 		}

// 		@Override
// 		public void execute(Villager villager) {
// 			Vector direction = targetLocation.toVector().subtract(villager.getLocation().toVector()).normalize();
// 			villager.setVelocity(direction.multiply(0.5));
// 		}
// 	}

// 	/**
// 	 * The MineBlockTask class represents a task where the familiar mines a specific block.
// 	 */

// 	private class MineBlockTask implements Task {
// 		private Material targetMaterial;

// 		@Override
// 		public boolean isComplete(Villager villager) {
// 			return villager.getTargetBlockExact(1).getType() != targetMaterial;
// 		}

// 		@Override
// 		public void start(Villager villager) {
// 			// Set the target block based on the desired material
// 			ItemStack pickaxe = villager.getEquipment().getItemInMainHand();
// 			if (pickaxe.getType() == Material.WOODEN_PICKAXE || pickaxe.getType() == Material.STONE_PICKAXE) {
// 				targetMaterial = Material.IRON_ORE;
// 			} else if (pickaxe.getType() == Material.IRON_PICKAXE) {
// 				targetMaterial = Material.IRON_ORE;
// 			} else if (pickaxe.getType() == Material.GOLDEN_PICKAXE) {
// 				targetMaterial = Material.GOLD_ORE;
// 			} else if (pickaxe.getType() == Material.DIAMOND_PICKAXE) {
// 				targetMaterial = Material.DIAMOND_ORE;
// 			} else {
// 				targetMaterial = Material.STONE;
// 			}
// 		}

// 		@Override
// 		public void execute(Villager villager) {

// 			Block minedBlock = villager.getTargetBlockExact(1);

// 			if (isMineable(minedBlock.getType())) {
// 				ItemStack minedItem = new ItemStack(minedBlock.getType(), 1);
// 				owner.getInventory().addItem(minedItem);

// 				minedBlock.setType(Material.AIR);
// 			} else {
// 				minedBlock.setType(Material.AIR);
// 			}

// 			villager.swingMainHand();
// 		}

// 		/**
// 		 * Checks if a block is mineable.
// 		 *
// 		 * @param type The type of the block to check.
// 		 * @return True if the block is mineable, false otherwise.
// 		 */

// 		private boolean isMineable(Material type) {
// 			return type == Material.COAL_ORE ||
// 					type == Material.IRON_ORE ||
// 					type == Material.GOLD_ORE ||
// 					type == Material.DIAMOND_ORE ||
// 					type == Material.EMERALD_ORE;
// 		}
// 	}
// }