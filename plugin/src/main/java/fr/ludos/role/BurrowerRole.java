package fr.ludos.role;

import fr.ludos.Main;
import fr.ludos.item.burrower.pick.BurrowerPickEvents;
import fr.ludos.item.burrower.digtool.BurrowingClawEvents;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import java.util.ArrayList;
import org.bukkit.generator.structure.StructureType;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.PluginManager;
import org.bukkit.Bukkit;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.StructureSearchResult;

public class BurrowerRole extends Role {

	private final BurrowerPickEvents pickEvents;
	private final BurrowingClawEvents clawEvents;

	public BurrowerRole(Builder builder) {
		super(builder);
		PluginManager manager = Bukkit.getPluginManager();

		pickEvents = new BurrowerPickEvents();
		manager.registerEvents((Listener)pickEvents, Main.getInstance());

		clawEvents = new BurrowingClawEvents();
		manager.registerEvents((Listener)clawEvents, Main.getInstance());
	}

	@Override
	public void stop() {
		super.stop();

		HandlerList.unregisterAll(pickEvents);
		HandlerList.unregisterAll(clawEvents);
	}

	public static final String id = "burrower";


	private ArrayList<Player> radarPlayers = new ArrayList<>();

	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		String message = event.getMessage();

		if (message.startsWith("radar")) {
			Bukkit.broadcastMessage("radar command");
			onRadar(player);
		}
	}

	

	@EventHandler
	public void onRadar(Player player) {

		StructureSearchResult location = player.getWorld().locateNearestStructure(player.getLocation(), StructureType.MINESHAFT, 150, false);	

		if (!radarPlayers.contains(player)) {
			return;
		}

		if (location != null) {
			player.sendMessage(ChatColor.GREEN + "Mineshaft détecté à " + location.getLocation());
		} else {
			player.sendMessage(ChatColor.RED + "Aucun mineshaft détecté.");
		}
	}

	@EventHandler
	public void createAdvancedFurnace(CraftItemEvent event) {
		Inventory inventory = event.getInventory();
		Player player = (Player) event.getWhoClicked();
		for (ItemStack item : inventory) {
			if (item != null && item.getType() == Material.FURNACE) {
				inventory.remove(item);
				player.sendMessage(ChatColor.GREEN + "Furnace upgraded to Blast Furnace.");
				return;
			}
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
		}.runTaskTimer(Main.getInstance(), 0, 20 * 60 * 1); // 20 ticks * 60 seconds * 1 minutes
	}

	private void giveRandomOreToPlayers() {
		// Player player;
		// BurrowerPick currentPickBurrowerPick = SpecialItem.findIn(player.getInventory(), (item) -> new BurrowerPick(item));

		// NamespacedKey  playerKey = currentPickBurrowerPick.getOwnerKey();
		// Player player = getPlayerByNamespacedKey(playerKey);

		for (int i = 0; i < Bukkit.getOnlinePlayers().size(); i++) {
			// player = (Player) Bukkit.getOnlinePlayers().toArray()[i];
			// var randomOre = RandomOre();
			// player.getInventory().addItem(new ItemStack(randomOre));
			// if (randomOre != null) {
			// 	ItemStack ore = new ItemStack(randomOre);
			
		
		}
		
	}
	private Material RandomOre() {
		Material[] oresTypes  = { Material.COAL_ORE, Material.IRON_ORE, Material.GOLD_ORE, Material.DIAMOND_ORE,  Material.LAPIS_ORE };
		Material randomOre = oresTypes[(int) (Math.random() * oresTypes.length)];
		return randomOre;
	}
}