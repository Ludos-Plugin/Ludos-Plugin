package fr.ludos.games.raid.theme;

import java.util.List;
import java.util.UUID;

import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;

import fr.ludos.games.raid.RaidGame;
import fr.ludos.games.raid.monsters.GoldenKnightBoss;
import fr.ludos.games.raid.monsters.RaidMonsterBoss;
import fr.ludos.games.raid.monsters.RaidMonsterBoss.Element;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * The default Overworld-related Theme of a Wave, used in {@link RaidGame}.
 */
public class EarthWaveTheme extends RaidWaveTheme {
	private static final List<WaveUnit> WAVE_UNITS = List.of(
		new WaveUnit(EntityType.ZOMBIE, 12, 30, false),
		new WaveUnit(EntityType.HUSK, 13, 24, false),
		new WaveUnit(EntityType.SKELETON, 14, 22, false),
		new WaveUnit(EntityType.STRAY, 17, 14, false),
		new WaveUnit(EntityType.SPIDER, 12, 18, false),
		new WaveUnit(EntityType.CAVE_SPIDER, 18, 12, false),
		new WaveUnit(EntityType.CREEPER, 22, 15, false),
		new WaveUnit(EntityType.ENDERMAN, 38, 9, false),
		new WaveUnit(EntityType.SLIME, 20, 14, false),
		new WaveUnit(EntityType.PILLAGER, 26, 20, false),
		new WaveUnit(EntityType.VINDICATOR, 34, 14, false),
		new WaveUnit(EntityType.WITCH, 42, 10, false),
		new WaveUnit(EntityType.EVOKER, 88, 6, false),
		new WaveUnit(EntityType.RAVAGER, 140, 3, false),
		new WaveUnit(EntityType.HOGLIN, 62, 5, false),
		new WaveUnit(EntityType.ZOGLIN, 84, 4, false),
		new WaveUnit(EntityType.WITHER_SKELETON, 900, 1, true)
	);

	public EarthWaveTheme(RaidGame game) {
		super(game);
	}

	@Override
	public TextComponent displayName() {
		return Component.text("Earth").color(NamedTextColor.GREEN);
	}
	@Override
	public WorldCreator getWorldCreator() {
		return new WorldCreator("raid_earth_" + UUID.randomUUID())
			.environment(World.Environment.NORMAL)
			.type(WorldType.NORMAL);
	}


	@Override
	public List<WaveUnit> getPossibleUnits() {
		return WAVE_UNITS;
	}

	@Override
	public RaidMonsterBoss<? extends Monster> createBoss(RaidGame game) {
		return new GoldenKnightBoss(game, Element.EARTH);
	}
}
