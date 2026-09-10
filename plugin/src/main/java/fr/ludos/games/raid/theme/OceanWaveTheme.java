package fr.ludos.games.raid.theme;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.ludos.core.generator.OceanChunkGenerator;
import fr.ludos.games.raid.RaidGame;
import fr.ludos.games.raid.items.MobilityTrident;
import fr.ludos.games.raid.monsters.GoldenKnightBoss;
import fr.ludos.games.raid.monsters.RaidMonsterBoss;
import fr.ludos.games.raid.monsters.RaidMonsterBoss.Element;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * The default Oceans-related Theme of a Wave, used in {@link RaidGame}.
 */
public class OceanWaveTheme extends RaidWaveTheme {
	private static final List<WaveUnit> WAVE_UNITS = List.of(
		new WaveUnit(EntityType.DROWNED, 12, 30, false),
		new WaveUnit(EntityType.GUARDIAN, 22, 18, false),
		new WaveUnit(EntityType.ELDER_GUARDIAN, 170, 2, false),
		new WaveUnit(EntityType.SKELETON, 14, 22, false),
		new WaveUnit(EntityType.SLIME, 16, 16, false),
		new WaveUnit(EntityType.CREEPER, 22, 15, false),
		new WaveUnit(EntityType.MAGMA_CUBE, 28, 10, false),
		new WaveUnit(EntityType.CAVE_SPIDER, 18, 12, false),
		new WaveUnit(EntityType.SPIDER, 12, 14, false),
		new WaveUnit(EntityType.RAVAGER, 160, 2, false),
		new WaveUnit(EntityType.HOGLIN, 62, 5, false),
		new WaveUnit(EntityType.WITHER_SKELETON, 900, 1, true)
	);

	private final MobilityTrident.Events tridentEvents = new MobilityTrident.Events(game());

	public OceanWaveTheme(RaidGame game) {
		super(game);
	}

	@Override
	public TextComponent displayName() {
		return Component.text("Ocean").color(NamedTextColor.DARK_AQUA);
	}
	@Override
	public WorldCreator getWorldCreator() {
		return new WorldCreator("raid_water_" + UUID.randomUUID())
			.environment(World.Environment.NORMAL)
			.type(WorldType.AMPLIFIED)
			.generator(new OceanChunkGenerator());
	}
	@Override
	public Consumer<World> getWorldConfig() {
		return (world) -> {
			world.setStorm(true);
			world.setWeatherDuration(Integer.MAX_VALUE);
			world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
		};
	}

	@Override
	public Location getSpawnLocation(World world) {
		return super.getSpawnLocation(world).add(0, 1, 0);
	}

	@Override
	public List<WaveUnit> getPossibleUnits() {
		return WAVE_UNITS;
	}

	@Override
	public RaidMonsterBoss<? extends Monster> createBoss(RaidGame game) {
		return new GoldenKnightBoss(game, Element.WATER);
	}

	@Override
	public void applyMobEffects(LivingEntity mob) {
		mob.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, Integer.MAX_VALUE, 0, true, false));
		mob.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, Integer.MAX_VALUE, 0, true, false));
	}

	@Override
	public void applyPlayerEffects(Player player) {
		player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, Integer.MAX_VALUE, 0, true, false));
		player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, Integer.MAX_VALUE, 0, true, false));

		if (! tridentEvents.isOwnedBy(player)) {
			tridentEvents.createAndGiveTo(player);

			player.sendMessage(Component.text(
				"Water boss kit: mobility trident granted"
			).color(NamedTextColor.AQUA));
		}
	}

	@Override
	public void removePlayerEffects(Player player) {
		player.removePotionEffect(PotionEffectType.WATER_BREATHING);
		player.removePotionEffect(PotionEffectType.DOLPHINS_GRACE);
	}

}
