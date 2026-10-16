package fr.ludos.games.sheepwars;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

import fr.ludos.core.Ludos;
import fr.ludos.core.PlayerList;
import fr.ludos.core.area.WorldBorderArea;
import fr.ludos.core.command.ludos.config.group.GroupConfigMap;
import fr.ludos.core.game.Game;
import fr.ludos.core.game.GameConfigs;
import fr.ludos.core.game.GameManager;
import fr.ludos.core.generator.VoidGenerator;
import fr.ludos.core.group.Group;
import fr.ludos.core.lobby.Lobby;
import fr.ludos.core.lobby.Lobby.ClearMode;
import fr.ludos.core.persistence.config.ConfigNodeCollection;
import fr.ludos.core.persistence.config.ConfigNodeMap;
import fr.ludos.core.persistence.config.valueEntry.GroupPlayersConfigEntry;
import fr.ludos.core.structure.NBTFileStructure;
import fr.ludos.core.structure.Structure;
import fr.ludos.core.world.WorldManager;
import fr.ludos.games.sheepwars.items.AbstractSheep;
import fr.ludos.games.sheepwars.items.SheepRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.TriState;
import xyz.xenondevs.invui.item.builder.AbstractItemBuilder;
import xyz.xenondevs.invui.item.builder.ItemBuilder;

/**
 * Implementation of the Sheepwars {@link Game}.
 */
public class SheepwarsGame extends Game {
	public static final String ID = "sheepwars";
	private static final int SHIP_SPACING = 10;

	private final SheepRegistry sheepRegistry;
	private final List<NBTFileStructure.Builder> shipStructures;

	private List<AbstractSheep> sheepList = null;
	private SheepDrop sheepDrop;

	private File path;

	private final Builder builder;
	public Builder getSheepwarsBuilder() {
		return this.builder;
	}

	private final SheepwarsTeamController teamController;
	@Override
	public SheepwarsTeamController teamController() {
		return this.teamController;
	}

	private final WorldManager worldManager;
	@Override
	public WorldManager worldManager() {
		return worldManager;
	}

	private Structure redShip;
	public final Structure redShip() {
		return redShip;
	}

	private Structure blueShip;
	public final Structure blueShip() {
		return blueShip;
	}


	public SheepwarsGame(Builder builder, Group group, Scoreboard scoreboard) {
		super(builder, group, scoreboard);
		this.builder = builder;

		this.sheepRegistry = new SheepRegistry();
		this.shipStructures = List.of(
			new NBTFileStructure.Builder(ludos(), "red_ship.nbt").offsetEntrance(new Vector(-0.5, -25.5, 14.5)),
			new NBTFileStructure.Builder(ludos(), "blue_ship.nbt").offsetEntrance(new Vector(-2.5, -23, 0.5))
		);
		this.worldManager = WorldManager.within(this)
			.of(builder.createWorldCreator())
			.config(w -> {
				w.setDifficulty(Difficulty.PEACEFUL);
				w.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
				w.setGameRule(GameRule.DO_FIRE_TICK, false);
				w.setGameRule(GameRule.DO_INSOMNIA, false);
				w.setGameRule(GameRule.DO_MOB_SPAWNING, false);
				w.setGameRule(GameRule.DO_PATROL_SPAWNING, false);
				w.setGameRule(GameRule.DO_TILE_DROPS, false);
				w.setGameRule(GameRule.DO_TRADER_SPAWNING, false);

				final NBTFileStructure.Builder selectedTemplate = shipStructures.get(ThreadLocalRandom.current().nextInt(shipStructures.size()));
				final BlockVector structSize = selectedTemplate.structure().getSize();

				final Location redBoatLocation;
				final Location blueBoatLocation;
				if (structSize.getBlockZ() >= structSize.getBlockX()) {
					// sideAxisIsX = true;
					final int finalSpacing = (SHIP_SPACING + structSize.getBlockX()) / 2;
					redBoatLocation = new Location(w, -finalSpacing - (structSize.getX() / 2), 0, - (structSize.getZ() / 2));
					blueBoatLocation = new Location(w, finalSpacing - (structSize.getX() / 2), 0, - (structSize.getZ() / 2));
				} else {
					// sideAxisIsX = false;
					final int finalSpacing = (SHIP_SPACING + structSize.getBlockZ()) / 2;
					redBoatLocation = new Location(w, - (structSize.getX() / 2), 0, finalSpacing - (structSize.getZ() / 2));
					blueBoatLocation = new Location(w, - (structSize.getX() / 2), 0, -finalSpacing - (structSize.getZ() / 2));
				}

				redShip = selectedTemplate.build(redBoatLocation);
				blueShip = selectedTemplate.build(blueBoatLocation);
			})
			.withLobby(
				Lobby.within(this)
					.at(new Location(null, 0, 10, 0))
					.waitFor(new PlayerList.GroupWaitPlayers(group))
					.clear(ClearMode.ALL)
					.wait(Duration.ofSeconds(GroupConfigMap.START_DELAY.getGroupConfig(group)))
					.then(this::start)
			)
			.build();
		this.teamController = new SheepwarsTeamController(
			this,
			builder.team1Players.getGameConfig(group, builder),
			builder.team2Players.getGameConfig(group, builder)
		);

		this.path = new File(builder.getLudos().getDataFolder(), "sheep.json");
	}

	@Override
	protected void onGameInit() {

		JSONArray sheepJsonArray = new JSONArray();

		if (!path.exists()) {
			path.getParentFile().mkdirs();
			InputStream inputStream = plugin().getResource("sheep.json");

			if (inputStream != null) {
				try (InputStreamReader reader = new InputStreamReader(inputStream)) {
					sheepJsonArray = (JSONArray) new JSONParser().parse(reader);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		if (path.exists() && sheepJsonArray.isEmpty()) {
			try (FileReader reader = new FileReader(path)) {
				sheepJsonArray = (JSONArray) new JSONParser().parse(reader);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// try (FileReader reader = new FileReader(path)) {
		// 	sheepJsonArray = (JSONArray) new JSONParser().parse(reader);
		// } catch (Exception e) {
		// 	e.printStackTrace();
		// }

		Bukkit.getLogger().info("Path where sheep.json was found : " + path.toString());

		sheepList = SheepRegistry.loadFromJson(sheepJsonArray);

		// Register all sheep in the registry
		for (AbstractSheep sheep : sheepList) {
			sheepRegistry.register(sheep);
		}

		group().sendMessage(
			Component.text("Sheepwars has started! Use your Sheep Wool wisely!")
				.color(NamedTextColor.GREEN)
		);
	}

	@Override
	protected void onGameStart() {
		// Register the sheep registry (handles interaction events)
		Bukkit.getPluginManager().registerEvents(sheepRegistry, this.builder().getLudos());

		// Register all sheep instances for shared event handlers (damage, death, movement)
		for (AbstractSheep sheep : sheepList) {
			Bukkit.getPluginManager().registerEvents(sheep, this.builder().getLudos());
		}

		sheepDrop = new SheepDrop(this, sheepList, 15);
		sheepDrop.start();
	}

	@Override
	protected void onGameStop() {
		// // Remove netherite armor from all players
		// for (Player player : teamController.getSelectedPlayers()) {
		// 	removeNetheriteArmor(player);
		// }

		// Stop the wool drop timer
		if (sheepDrop != null) {
			sheepDrop.stop();
		}
		sheepDrop = null;

		// Unregister both sheep registry and shared event handlers
		HandlerList.unregisterAll(sheepRegistry);
		if (!sheepList.isEmpty()) {
			HandlerList.unregisterAll(sheepList.get(0));
		}

		group().sendMessage(
			Component.text("Sheepwars has ended!")
				.color(NamedTextColor.RED)
		);
	}

	private ItemStack createProtectedArmorPiece(Material material) {
		ItemStack item = new ItemStack(material);
		ItemMeta meta = item.getItemMeta();
		if (meta != null) {
			meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4, true);
			item.setItemMeta(meta);
		}
		return item;
	}

	// /**
	//  * Equips iron armor to a player.
	//  * @param player The player to equip
	//  */
	// private void equipIronArmor(Player player) {
	// 	PlayerInventory inventory = player.getInventory();
	// 	inventory.setHelmet(createProtectedArmorPiece(Material.IRON_HELMET));
	// 	inventory.setChestplate(createProtectedArmorPiece(Material.IRON_CHESTPLATE));
	// 	inventory.setLeggings(createProtectedArmorPiece(Material.IRON_LEGGINGS));
	// 	inventory.setBoots(createProtectedArmorPiece(Material.IRON_BOOTS));
	// }

	// /**
	//  * Removes iron armor from a player.
	//  * @param player The player to remove armor from
	//  */
	// private void removeIronArmor(Player player) {
	// 	PlayerInventory inventory = player.getInventory();
	// 	inventory.setHelmet(null);
	// 	inventory.setChestplate(null);
	// 	inventory.setLeggings(null);
	// 	inventory.setBoots(null);
	// }

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (! group().isPlayer(player)) return;
		if (isStarted()) {
			player.setScoreboard(scoreboard());
		}

		// player.sendMessage(Component.text("Bienvenue sur le serveur Ludos!", NamedTextColor.AQUA, TextDecoration.BOLD));
		// ItemStack sheepItem = new ItemStack(Material.SHEEP_SPAWN_EGG, 1);

		// if (!player.getInventory().contains(sheepItem)) {
		//     player.getInventory().addItem(sheepItem);
		// }

		// ItemMeta meta = sheepItem.getItemMeta();

		// List<Component> lore = meta.lore();
		// lore.add(Component.text("Gros mouton sa mère, хорошо, хорошо, хорошо", NamedTextColor.YELLOW, TextDecoration.BOLD));
		// meta.lore(lore);

		// sheepItem.setItemMeta(meta);
	}

	// @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	// public void onBlockBreak(BlockBreakEvent event) {
	// 	if (!SheepwarsWorldManager.ACTIVE_WORLD_NAME.equals(event.getBlock().getWorld().getName())) {
	// 		return;
	// 	}

	// 	event.setDropItems(false);
	// }

	// @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	// public void onEntityExplode(EntityExplodeEvent event) {
	// 	if (!SheepwarsWorldManager.ACTIVE_WORLD_NAME.equals(event.getLocation().getWorld().getName())) {
	// 		return;
	// 	}

	// 	event.setYield(0.0F);
	// }

	// private static final EnumSet<EntityType> MONSTER_TYPES = EnumSet.of(
	// 	EntityType.ZOMBIE,
	// 	EntityType.SKELETON,
	// 	EntityType.SPIDER
	// );

	// @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	// public void onCreatureSpawn(CreatureSpawnEvent event) {
	// 	if (!SheepwarsWorldManager.ACTIVE_WORLD_NAME.equals(event.getLocation().getWorld().getName())) {
	// 		return;
	// 	}

	// 	if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM) {
	// 		return;
	// 	}

	// 	if (MONSTER_TYPES.contains(event.getEntityType())) {
	// 		event.setCancelled(false);
	// 	}
	// }

	/**
	 * Builder for {@link SheepwarsGame}.
	 */
	public static class Builder extends Game.Builder {
		public final GroupPlayersConfigEntry team1Players = GameConfigs.createConfigForTeam(getManager().getLudos().groupManager(), 1);
		public final GroupPlayersConfigEntry team2Players = GameConfigs.createConfigForTeam(getManager().getLudos().groupManager(), 2);

		public final ConfigNodeMap configMap = getConfigMap(List.of(
			getLudos().playersConfig,
			team1Players,
			team2Players,
			WorldBorderArea.CONFIG
		));

		public Builder(GameManager manager) {
			super(manager);
		}

		@Override
		public String getId() {
			return ID;
		}

		public WorldCreator createWorldCreator() {
			String worldName = "sheepwars_" + UUID.randomUUID();
			WorldCreator wc = new WorldCreator(worldName, new NamespacedKey(Ludos.NAMESPACE, worldName))
				.environment(Environment.NORMAL)
				.type(WorldType.FLAT)
				.generateStructures(false)
				.seed(new Random().nextLong())
				.generator(VoidGenerator.INSTANCE);
			wc.keepSpawnLoaded(TriState.FALSE);
			return wc;
		}

		@Override
		public TextComponent displayName() {
			return Component.text("Sheepwars");
		}

		@Override
		public TextComponent getDescription() {
			return Component.text("A team-based battle game where players use Cow Eggs to spawn allies and fight for victory!");
		}

		@Override
		public @NotNull AbstractItemBuilder<?> createItem(Player player) {
			return new ItemBuilder(Material.BROWN_WOOL);
		}

		@Override
		public Game build(Group group) {
			return new SheepwarsGame(this, group, Bukkit.getServer().getScoreboardManager().getNewScoreboard());
		}

		@Override
		public ConfigNodeCollection getConfig() {
			return configMap;
		}

	}
}