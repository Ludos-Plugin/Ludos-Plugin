package fr.ludos.role;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.generator.structure.StructureType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StructureSearchResult;

import fr.ludos.Ludos;
import fr.ludos.game.Game;
import fr.ludos.game.GameEvents;
import fr.ludos.item.Categories;
import fr.ludos.item.LevelItem;
import fr.ludos.item.harvester.HarvesterPick;
import fr.ludos.item.harvester.HarvesterScythe;
import fr.ludos.item.harvester.HarvesterSpade;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;


public class HarvesterRole extends Role {
	public static final String id = "harvester";
	private static final NamespacedKey explosiveChestplateKey = new NamespacedKey(JavaPlugin.getPlugin(Ludos.class), "harvester_explosive_chestplate");
	private static final String explosiveModeLorePrefix = ChatColor.DARK_GRAY + "Mode: ";
	private static final String explosiveModeLoreEnabled = explosiveModeLorePrefix + ChatColor.RED + "Explosif";
	private static final String explosiveModeLoreDisabled = explosiveModeLorePrefix + ChatColor.GREEN + "Normal";

	private static final double EXPLOSIVE_RADIUS = 3.0;
	private static final double EXPLOSIVE_RADIUS_SQUARED = EXPLOSIVE_RADIUS * EXPLOSIVE_RADIUS;

	public static final Map<Material, Double> CHESTPLATE_EXPLOSIVE_DAMAGE;

	static {
		EnumMap<Material, Double> damageMap = new EnumMap<>(Material.class);
		for (int i = 0; i < Categories.CHESTPLATES.size(); i++) {
			Material material = Categories.CHESTPLATES.stream().skip(i).findFirst().orElseThrow();
			damageMap.put(material, 2.0 * (i + 1));
		}

		CHESTPLATE_EXPLOSIVE_DAMAGE = Collections.unmodifiableMap(damageMap);
	}


	// public static List<Player> harvesters;

	// private BukkitTask passiveResourcesTask;


	public HarvesterRole(Builder builder, Game game) {
		super(builder, game);
	}

	@Override
	protected void onRoleStart() {
		// harvesters = Role.getPlayersOfRole(id);

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
					put("scythe", new HarvesterScythe.Events(game));
					put("pick", new HarvesterPick.Events(game));
					put("spade", new HarvesterSpade.Events(game));
				}};
		}
	}

	@EventHandler
	public void onHarvesterToggleExplosiveChestplate(PlayerInteractEvent event) {
		Action action = event.getAction();

		if (action.isRightClick()) return;

		Player player = event.getPlayer();
		if (!Role.isPlayerRole(player, id)) return;

		ItemStack item = event.getItem();
		if (!Categories.isChestplate(item)) return;

		boolean explosive = isExplosiveChestplate(item);

		if ( action.isLeftClick() ) setExplosiveChestplate(item, !explosive);

		// event.setCancelled(true);
	}

	@EventHandler
	public void onHarvesterExplosiveChestplateProximity(PlayerMoveEvent event) {
		Player harvester = event.getPlayer();
		if (!Role.isPlayerRole(harvester, id)) return;
		if (event.getTo() == null) return;

		if (
			event.getFrom().getBlockX() == event.getTo().getBlockX() &&
			event.getFrom().getBlockY() == event.getTo().getBlockY() &&
			event.getFrom().getBlockZ() == event.getTo().getBlockZ()
		) return;

		ItemStack chestplate = harvester.getInventory().getChestplate();

		if (!Categories.isChestplate(chestplate)) return;
		if (!isExplosiveChestplate(chestplate)) return;

		List<Player> enemies = getGame().getGameTeamController().getEnemyPlayers(harvester).stream()
			.filter(enemy -> enemy.getWorld().equals(harvester.getWorld()))
			.filter(enemy -> enemy.getLocation().distanceSquared(harvester.getLocation()) <= EXPLOSIVE_RADIUS_SQUARED)
			.collect(Collectors.toList());

		if (enemies.isEmpty()) return;

		double damage = CHESTPLATE_EXPLOSIVE_DAMAGE.get(chestplate.getType());

		harvester.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, harvester.getLocation(), 1, 0.0, 0.0, 0.0, 0.0);
		harvester.getWorld().playSound(harvester.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);

		for (Player enemy : enemies) {
			enemy.damage(damage, harvester);
			damageEquipmentByThird(enemy);
		}

		harvester.setVelocity(harvester.getLocation().getDirection().multiply(-0.5).setY(0.5));

		// harvester.getInventory().setChestplate(null);
	}

	private static final Set<BiFunction<ItemStack, Game, LevelItem<?>>> levelItemGetters = Set.of(
		HarvesterScythe::fromItemStack,
		HarvesterPick::fromItemStack,
		HarvesterSpade::fromItemStack
	);
	public static void awardBreak(Player player, Block block, Game game) {
		if (player == null || block == null) return;
		if (!Role.isPlayerRole(player, id)) return;

		Inventory inventory = player.getInventory();
		if (inventory == null) return;

		double oreXp = getOreReward(block);
		if (oreXp == 0) return;
		for (var test : levelItemGetters) {
			LevelItem.findAllIn(inventory, (itemStack) -> test.apply(itemStack, game)).forEach(item -> item.addXp(oreXp));
		}
	}


	public static double getOreReward(Block ore) {
		Material material = ore.getType();
		switch (material) {
			case ANCIENT_DEBRIS:
				return 60;
			case EMERALD_ORE:
				return 50;
			case DIAMOND_ORE:
				return 45;
			case GOLD_ORE:
				return 40;
			case REDSTONE_ORE:
				return 35;
			case LAPIS_ORE:
				return 30;
			case NETHER_QUARTZ_ORE:
				return 25;
			case IRON_ORE:
				return 20;
			case OBSIDIAN:
				return 15;
			case COAL_ORE:
				return 10;
			case COPPER_ORE:
				return 5;
			default:
				return material.getHardness();
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


	private static boolean isExplosiveChestplate(ItemStack item) {
		if (!Categories.isChestplate(item)) return false;

		ItemMeta meta = item.getItemMeta();
		if (meta == null) return false;

		PersistentDataContainer container = meta.getPersistentDataContainer();
		if (!container.has(explosiveChestplateKey, PersistentDataType.BYTE)) return false;

		Byte value = container.get(explosiveChestplateKey, PersistentDataType.BYTE);
		return value != null && value == (byte) 1;
	}

	private static void setExplosiveChestplate(ItemStack item, boolean explosive) {
		if (!Categories.isChestplate(item)) return;

		ItemMeta meta = item.getItemMeta();
		PersistentDataContainer container = meta.getPersistentDataContainer();

		container.set(explosiveChestplateKey, PersistentDataType.BYTE, explosive ? (byte)1 : (byte)0);

		List<String> lore = meta.getLore() == null ? new ArrayList<>() : new ArrayList<>(meta.getLore());
		lore.removeIf(line -> line != null && line.startsWith(explosiveModeLorePrefix));
		lore.add(explosive ? explosiveModeLoreEnabled : explosiveModeLoreDisabled);
		meta.setLore(lore);

		item.setItemMeta(meta);
	}

	private static void damageEquipmentByThird(Player player) {
		for (ItemStack item : player.getInventory().getArmorContents()) {
			damageItemByThird(item);
		}
	}

	private static void damageItemByThird(ItemStack item) {
		if (item == null || item.getType() == Material.AIR) return;
		if (!Categories.IMPORTANT_DURABILITY.contains(item.getType())) return;

		int maxDurability = item.getType().getMaxDurability();
		if (maxDurability <= 0) return;

		ItemMeta meta = item.getItemMeta();
		if (!(meta instanceof Damageable damageable)) return;

		int toDamage = Math.max(1, (int)Math.ceil(maxDurability / 3.0));
		int nextDamage = Math.min(maxDurability, damageable.getDamage() + toDamage);

		damageable.setDamage(nextDamage);
		item.setItemMeta(meta);

		item.setDurability((short) (maxDurability - nextDamage));

		// if (nextDamage >= maxDurability) {
		// 	item.setAmount(0);
		// }
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
	// 	for (Player player : harvesters) {
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

		public Builder(Ludos plugin) {
			super(plugin);
		}

		@Override
		public Role build(Game game){
			return new HarvesterRole(this, game);
		}

		@Override
		public TextComponent getDisplayName() {
			return Component.text("Harvester");
		}

		@Override
		public TextComponent getDescription() {
			return Component.text("");
		}
	}
}