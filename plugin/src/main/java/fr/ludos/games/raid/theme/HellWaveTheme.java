package fr.ludos.games.raid.theme;

import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.ludos.core.Utility;
import fr.ludos.games.raid.RaidGame;
import fr.ludos.games.raid.monsters.GoldenKnightBoss;
import fr.ludos.games.raid.monsters.RaidMonsterBoss;
import fr.ludos.games.raid.monsters.RaidMonsterBoss.Element;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * The default Nether-related Theme of a Wave, used in {@link RaidGame}.
 */
public class HellWaveTheme extends RaidWaveTheme {
	private static final List<WaveUnit> WAVE_UNITS = List.of(
		new WaveUnit(EntityType.BLAZE, 20, 24, false),
		new WaveUnit(EntityType.MAGMA_CUBE, 18, 20, false),
		new WaveUnit(EntityType.WITHER_SKELETON, 42, 12, false),
		new WaveUnit(EntityType.PIGLIN_BRUTE, 72, 8, false),
		new WaveUnit(EntityType.HOGLIN, 52, 10, false),
		new WaveUnit(EntityType.ZOGLIN, 64, 8, false),
		new WaveUnit(EntityType.GHAST, 96, 5, false),
		new WaveUnit(EntityType.EVOKER, 88, 5, false),
		new WaveUnit(EntityType.RAVAGER, 145, 3, false),
		new WaveUnit(EntityType.WITHER_SKELETON, 900, 1, true)
	);

	public HellWaveTheme(RaidGame game) {
		super(game);
	}

	@Override
	public TextComponent displayName() {
		return Component.text("Hell").color(NamedTextColor.RED);
	}
	@Override
	public WorldCreator getWorldCreator() {
		return new WorldCreator("raid_fire_" + UUID.randomUUID())
			.environment(World.Environment.NETHER)
			.type(WorldType.NORMAL);
	}

	@Override
	public Location getSpawnLocation(World world) {
		return Utility.snapToHighestY(super.getSpawnLocation(world), true);
	}

	@Override
	public List<WaveUnit> getPossibleUnits() {
		return WAVE_UNITS;
	}

	@Override
	public RaidMonsterBoss<? extends Monster> createBoss(RaidGame game) {
		return new GoldenKnightBoss(game, Element.FIRE);
	}

	@Override
	public void applyMobEffects(LivingEntity mob) {
		mob.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, true, false));
	}
}
