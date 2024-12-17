package fr.ludos.role;

import fr.ludos.game.Game;
import fr.ludos.item.burrower.BurrowerPick;
import fr.ludos.item.burrower.BurrowerShovel;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.generator.structure.StructureType;

import org.bukkit.util.StructureSearchResult;

import java.util.List;

public class BurrowerRole extends Role {

	private final BurrowerPick.Events pickEvents;
	private final BurrowerShovel.Events shovelEvents;

	public static final String id = "burrower";

	public static List<Player> burrowers;

	// private BukkitTask passiveResourcesTask;


	public BurrowerRole(Builder builder, Game game) {
		super(builder, game);


		pickEvents = new BurrowerPick.Events(game);
		shovelEvents = new BurrowerShovel.Events(game);
	}

	@Override
	public void start() {
		super.start();

		pickEvents.start();
		shovelEvents.start();

		burrowers = Role.getPlayersOfRole(id);

		// passiveResourcesTask = new BukkitRunnable() {    // FIXME: Quentin, quand cette tâche s'éxecute pour la première fois, elle remplace la pelle dans l'inventaire
		// 	@Override									    // Le seul moyen de faire réapparaître la pelle est de déco reco
		// 	public void run() {
		// 		giveRandomOreToPlayers();
		// 	}
		// }.runTaskTimer(Main.getInstance(), 0, 20 * 600 * 1);
	}

	@Override
	public void stop() {
		super.stop();

		// passiveResourcesTask.cancel();
		// passiveResourcesTask = null;

		pickEvents.stop();
		shovelEvents.stop();
	}

	// @EventHandler
	// public void onPlayerChat(AsyncPlayerChatEvent event) {
	// 	Player player = event.getPlayer();
	// 	String message = event.getMessage();

	// 	if (message.startsWith("radar") && Role.isPlayerRole(player, id)) {
	// 		radar(player);
	// 	}
	// }

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



	public static class Builder extends Role.Builder {
		@Override
		public String getId() {
			return id;
		}

		@Override
		public Role build(Game.Builder builder, Game game){
			return new BurrowerRole(this, game);
		}
	}
}