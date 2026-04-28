package fr.ludos.role;

import java.util.LinkedHashMap;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.StructureType;
import org.bukkit.entity.Player;
import org.bukkit.util.StructureSearchResult;

import fr.ludos.Ludos;
import fr.ludos.game.Game;
import fr.ludos.game.GameEvents;
import fr.ludos.item.SpecialItem;
import fr.ludos.item.burrower.BurrowerPick;
import fr.ludos.item.burrower.BurrowerShovel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;


public class BurrowerRole extends Role {
	public static final String id = "burrower";


	// public static List<Player> burrowers;

	// private BukkitTask passiveResourcesTask;


	public BurrowerRole(Builder builder, Game game) {
		super(builder, game);
	}

	@Override
	protected void onRoleStart() {
		// burrowers = Role.getPlayersOfRole(id);

		// passiveResourcesTask = new BukkitRunnable() {    // FIXME: Quentin, quand cette tâche s'éxecute pour la première fois, elle remplace la pelle dans l'inventaire
		// 	@Override									    // Le seul moyen de faire réapparaître la pelle est de déco reco
		// 	public void run() {
		// 		giveRandomOreToPlayers();
		// 	}
		// }.runTaskTimer(Main.getInstance(), 0, 20 * 600 * 1);
	}

	@Override
	protected void onRoleStop() {
		// passiveResourcesTask.cancel();
		// passiveResourcesTask = null;
	}

	@Override
	protected LinkedHashMap<String, GameEvents> createGameEvents(Role.Builder builder, Game game) {
		switch (builder.getId()) {
			default:
				return new LinkedHashMap<>() {{
					put("pick", new BurrowerPick.Events(game));
					put("shovel", new BurrowerShovel.Events(game));
				}};
		}
	}

	// @EventHandler
	// public void onPlayerChat(AsyncPlayerChatEvent event) {
	// 	Player player = event.getPlayer();
	// 	String message = event.getMessage();

	// 	if (message.startsWith("radar") && Role.isPlayerRole(player, id)) {
	// 		radar(player);
	// 	}
	// }

	// public void radar(Player player) {
	// 	try {
	// 		int maxDistanceDetection = 200;

	// 		Location location = player.getWorld().locateNearestStructure(player.getLocation(), StructureType.MINESHAFT, maxDistanceDetection, false);

	// 		int distanceFromThePlayer = location.getLocation().getBlockZ() - player.getLocation().getBlockZ();

	// 		if (distanceFromThePlayer < maxDistanceDetection) {

	// 			player.sendMessage(ChatColor.GREEN + "Mineshaft détecté à : " +
	// 				" x : " + location.getLocation().getX() +
	// 				", y : " + location.getLocation().getY() +
	// 				", z : " + location.getLocation().getZ());

	// 		} else {
	// 			player.sendMessage(ChatColor.RED + "Aucun mineshaft détecté à proximité.");
	// 		}


	// 	} catch (Exception e) {

	// 	}
	// }

	// @EventHandler
    // public void onPrepareItemCraft(PrepareItemCraftEvent event) {
	// 	if ( ! Role.isPlayerRole(event.getView().getPlayer(), id) ) {
	// 		return;
	// 	}


    //     Recipe recipe = event.getRecipe();
    //     if (recipe == null) {
	// 		return;
    //     }

	// 	ItemStack result = recipe.getResult();
	// 	if (result.getType() == Material.FURNACE) {
	// 		CraftingInventory craft = event.getInventory();
	// 		craft.setResult(new ItemStack(Material.BLAST_FURNACE));
	// 	}
	// }

	// private void giveRandomOreToPlayers() {
	// 	for (Player player : burrowers) {
	// 		if (player == null) {
	// 			return;
	// 		}
	// 		Material randomOre = getRandomOre();
	// 		player.getInventory().addItem(new ItemStack(randomOre));
	// 	}
	// }

	// private Material getRandomOre() {
	// 	Material[] oresTypes  = { Material.COAL, Material.IRON_INGOT, Material.GOLD_INGOT, Material.DIAMOND };
	// 	Material randomOre = oresTypes[(int) (Math.random() * oresTypes.length)];
	// 	return randomOre;
	// }

	@Override
	protected Boolean isPlayerValidInternal(OfflinePlayer player) {
		return Role.isPlayerRole(player, id);
	}


	public static class Builder extends Role.Builder {

		@Override
		public String getId() {
			return id;
		}

		public Builder(Ludos plugin) {
			super(plugin);
		}

		@Override
		public Role build(Game game){
			return new BurrowerRole(this, game);
		}

		@Override
		public TextComponent getDisplayName() {
			return Component.text("Burrower");
		}

		@Override
		public TextComponent getDescription() {
			return Component.text("");
		}
	}
}