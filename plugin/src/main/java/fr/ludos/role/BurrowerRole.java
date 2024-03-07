package fr.ludos.role;

import fr.ludos.Main;
import fr.ludos.item.burrower.pick.BurrowerPickEvents;
import fr.ludos.item.burrower.digtool.BurrowingShovelEvents;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.generator.structure.StructureType;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.PluginManager;
import org.bukkit.Bukkit;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.StructureSearchResult;

import java.util.List;
import java.util.stream.Collectors;

public class BurrowerRole extends Role {

	private final BurrowerPickEvents pickEvents;
	private final BurrowingShovelEvents clawEvents;

	public static final String id = "burrower";

	public static List<Player> burrowers;


	public BurrowerRole(Builder builder) {
		super(builder);
		PluginManager manager = Bukkit.getPluginManager();

		startPassiveGain();
		burrowers = Role.getPlayerRoles().entrySet().stream()
			.filter(entry -> (entry.getValue().equals(id)))
			.map(entry -> Bukkit.getPlayerExact(entry.getKey()))
			.collect(Collectors.toList());

		pickEvents = new BurrowerPickEvents();
		manager.registerEvents((Listener)pickEvents, Main.getInstance());

		clawEvents = new BurrowingShovelEvents();
		manager.registerEvents((Listener)clawEvents, Main.getInstance());

		for (Player player : burrowers) {
			pickEvents.updateItemInInventory(player);
			clawEvents.updateItemInInventory(player);
		}
	}

	@Override
	public void stop() {
		super.stop();

		HandlerList.unregisterAll(pickEvents);
		HandlerList.unregisterAll(clawEvents);
	}

	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		String message = event.getMessage();

		if (message.startsWith("radar") && Role.isPlayerRole(player, id)) {
			radar(player);
		}
	}

	public void radar(Player player) {
		try {
			int maxDistanceDetection = 200;

			StructureSearchResult location = player.getWorld().locateNearestStructure(player.getLocation(), StructureType.MINESHAFT, maxDistanceDetection, false);

			int distanceFromThePlayer = location.getLocation().getBlockZ() - player.getLocation().getBlockZ();

			if (distanceFromThePlayer < maxDistanceDetection) {

				player.sendMessage(ChatColor.GREEN + "Mineshaft détecté à : " +
					" x : " + location.getLocation().getX() +
					", y : " + location.getLocation().getY() +
					", z : " + location.getLocation().getZ());

			} else {
				player.sendMessage(ChatColor.RED + "Aucun mineshaft détecté à proximité.");
			}


		} catch (Exception e) {

		}
	}

	@EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
		if ( ! Role.isPlayerRole(event.getView().getPlayer(), id) ) {
			return;
		}


        Recipe recipe = event.getRecipe();
        if (recipe == null) {
			return;
        }

		ItemStack result = recipe.getResult();
		if (result.getType() == Material.FURNACE) {
			CraftingInventory craft = event.getInventory();
			craft.setResult(new ItemStack(Material.BLAST_FURNACE));
		}
	}

	public static class Builder extends Role.Builder {
		@Override
		public String getId() {
			return id;
		}

		@Override
		public Role build(String gameId){
			return new BurrowerRole(this);
		}
	}

	public static Player getPlayerByNamespacedKey(NamespacedKey key) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PersistentDataContainer container = player.getPersistentDataContainer();
            if (container.has(key, PersistentDataType.STRING)) {
                return player;
            }
        }
        return null;
    }

	public void startPassiveGain() {
		new BukkitRunnable() {
			@Override
			public void run() {
				giveRandomOreToPlayers();
			}
		}.runTaskTimer(Main.getInstance(), 0, 20 * 600 * 1);
	}

	private void giveRandomOreToPlayers() {
		for (Player player : burrowers) {
			if (player == null) {
				return;
			}
			var randomOre = RandomOre();
			player.getInventory().addItem(new ItemStack(randomOre));
		}
	}

	private Material RandomOre() {
		Material[] oresTypes  = { Material.COAL, Material.IRON_INGOT, Material.GOLD_INGOT, Material.DIAMOND };
		Material randomOre = oresTypes[(int) (Math.random() * oresTypes.length)];
		return randomOre;
	}
}