package fr.ludos.roles.harvester;

import java.util.EnumSet;
import java.util.LinkedHashMap;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import fr.ludos.core.Ludos;
import fr.ludos.core.game.Game;
import fr.ludos.core.game.GameEvents;
import fr.ludos.core.item.SpecialItem;
import fr.ludos.core.item.level.LevelItem;
import fr.ludos.core.item.level.LevelItemInterface;
import fr.ludos.core.role.Role;
import fr.ludos.core.role.RoleFlag;
import fr.ludos.roles.harvester.items.HarvesterPick;
import fr.ludos.roles.harvester.items.HarvesterScythe;
import fr.ludos.roles.harvester.items.HarvesterSpade;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Implementation of the Harvester {@link Role}.
 */
public class HarvesterRole extends Role {
	public static final String ID = "harvester";


	public HarvesterRole(Builder builder, Game game) {
		super(builder, game);
	}

	@Override
	protected void onRoleStart() {
		super.onRoleStart();
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
		super.onRoleStop();
		// passiveResourcesTask.cancel();
		// passiveResourcesTask = null;
	}

	@Override
	protected LinkedHashMap<String, GameEvents> createGameEvents(Role.Builder builder, Game game) {
		HarvesterRole harvesterRole = this;
		switch (builder.getId()) {
			default:
				return new LinkedHashMap<>() {{
					put(HarvesterScythe.ID, new HarvesterScythe.Events(game));
					put(HarvesterPick.ID, new HarvesterPick.Events(harvesterRole, game));
					put(HarvesterSpade.ID, new HarvesterSpade.Events(harvesterRole, game));
				}};
		}
	}


	public void awardBreak(Player player, Block block, Game game) {
		if (player == null || block == null) return;
		if (! getBuilder().getManager().isPlayerRole(player, ID)) return;

		Inventory inventory = player.getInventory();
		if (inventory == null) return;

		double oreXp = getOreReward(block);
		if (oreXp == 0) return;
		for (var events : getGameEvents().values()) {
			if (events instanceof SpecialItem.Events itemEvents) {
				LevelItem.findAllIn(inventory, (itemStack) -> itemEvents.getItem(itemStack))
					.stream()
					.filter(o -> o instanceof LevelItemInterface)
					.forEach(item -> ((LevelItemInterface) item).addXp(oreXp));
			}
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

	/**
	 * Builder for the {@link HarvesterRole}.
	 */
	public static class Builder extends Role.Builder {

		@Override
		public String getId() {
			return ID;
		}

		@Override
		public EnumSet<RoleFlag> getRoleFlags() {
			return EnumSet.of(RoleFlag.SUPPORT);
		}

		public Builder(Ludos ludos) {
			super(ludos.getRoleManager(), ludos);
		}

		@Override
		public Role build(Game game){
			return new HarvesterRole(this, game);
		}

		@Override
		public TextComponent getDisplayName() {
			return Component.text("Harvester")
				.color(NamedTextColor.GREEN);
		}

		@Override
		public TextComponent getDescription() {
			return Component.text("The Harvester seeks the riches of the earth,\n" +
				"using various tools to scrape his way through obstacles and escape his enemies."
			);
		}
	}
}