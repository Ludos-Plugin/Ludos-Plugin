package fr.ludos.item.burrower;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import fr.ludos.game.Game;
import fr.ludos.item.BranchItem;
import fr.ludos.item.LevelItem;
import fr.ludos.item.SpecialItem;
import fr.ludos.role.BurrowerRole;
import fr.ludos.role.Role;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class BurrowerScythe extends LevelItem<BurrowerScytheLevels> {
	public static final String ID = "burrower_scythe";

	private static final double SCYTHE_RANGE = 5.0;
	private static final double SCYTHE_RANGE_SQUARED = SCYTHE_RANGE * SCYTHE_RANGE;
	private static final int SCYTHE_MAX_TARGETS = 5;
	private static final double SCYTHE_CONE_DOT = 0.35;

	// private final static Map<UUID, BurrowerPick> cachedItems = new HashMap<>();


	public static BurrowerScythe fromItemStack(ItemStack stack, Game game) throws IllegalArgumentException {
		UUID itemId = SpecialItem.getSpecialItemId(stack, ID, game);
		if (itemId == null) return null;

		// BurrowerScythe cached = cachedItems.get(itemId);
		// if (cached != null) return cached;

		Player owner = SpecialItem.getSpecialItemOwner(stack, game);
		if (owner == null) return null;
		LevelState levelState = LevelItem.levelFromItemStack(stack, game);
		if (levelState == null) return null;

		BurrowerScythe burrowerScythe = new BurrowerScythe(stack, owner, levelState, game);
		// cachedItems.put(itemId, burrowerScythe);

		return burrowerScythe;
	}

	public static BurrowerScythe createItem(Player owner, LevelState level, Game game) {
		BurrowerScytheLevels lvl = BurrowerScytheLevels.values()[level.getLevel()];
		BurrowerScythe burrowerScythe = new BurrowerScythe(new ItemStack(lvl.getMaterial()), owner, level, game);
		UUID itemId = burrowerScythe.initializeItem();

		// cachedItems.put(itemId, burrowerScythe);

		return burrowerScythe;
	}

	protected BurrowerScythe(ItemStack stack, Player owner, LevelState level, Game game) {
		super(BurrowerScytheLevels.class, stack, owner, level, game);
	}

	@Override
	protected String getTypeId() {
		return ID;
	}

	@Override
	public Component getName() {
		return Component.text("Burrower's Scythe")
			.decoration(TextDecoration.ITALIC, false); // TODO: Translate
	}


	public static class Events extends LevelItem.Events<BurrowerScythe, BurrowerScytheLevels> {
		private final Set<UUID> slashingPlayers = new HashSet<>();

		public Events(Game game) {
			super(game, 0);
		}

		@Override
		protected BurrowerScythe createItem(Player owner, LevelState level, Game game) {
			return BurrowerScythe.createItem(owner, level, game);
		}

		@Override
		@Nullable
		protected BurrowerScythe getItem(ItemStack stack, Game game) {
			return BurrowerScythe.fromItemStack(stack, game);
		}

		@EventHandler
		public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
			if (!(event.getDamager() instanceof Player attacker)) return;
			if (!(event.getEntity() instanceof Player primaryTarget)) return;

			BurrowerScythe scythe = getItem(attacker.getInventory().getItemInMainHand(), game);
			if (scythe == null) return;

			if (game.getGameTeamController().areAllies(attacker, primaryTarget)) {
				event.setCancelled(true);
				return;
			}
			if (!slashingPlayers.add(attacker.getUniqueId())) return;

			try {
				List<Player> candidates = new ArrayList<>();
				for (org.bukkit.entity.Entity nearby : attacker.getNearbyEntities(SCYTHE_RANGE, SCYTHE_RANGE, SCYTHE_RANGE)) {
					if (!(nearby instanceof Player target)) continue;
					if (target.equals(attacker) || target.equals(primaryTarget)) continue;
					if (target.isDead()) continue;
					if (game.getGameTeamController().areAllies(attacker, target)) continue;

					Vector toTarget = target.getLocation().toVector().subtract(attacker.getLocation().toVector());
					double distanceSquared = toTarget.lengthSquared();
					if (distanceSquared > SCYTHE_RANGE_SQUARED || distanceSquared <= 0.0001) continue;

					Vector direction = attacker.getLocation().getDirection().normalize();
					double dot = direction.dot(toTarget.normalize());
					if (dot < SCYTHE_CONE_DOT) continue;

					candidates.add(target);
				}

				candidates.sort((a, b) -> {
					double da = a.getLocation().distanceSquared(attacker.getLocation());
					double db = b.getLocation().distanceSquared(attacker.getLocation());
					return Double.compare(da, db);
				});

				int extraHits = Math.max(0, SCYTHE_MAX_TARGETS - 1);
				for (int i = 0; i < Math.min(extraHits, candidates.size()); i++) {
					Player extraTarget = candidates.get(i);
					extraTarget.damage(event.getDamage(), attacker);
				}
			} finally {
				slashingPlayers.remove(attacker.getUniqueId());
			}
		}


		@Override
		protected Boolean canPlayerHaveItem(HumanEntity owner) {
			return Role.isPlayerRole(owner, BurrowerRole.id);
		}
	}
}
