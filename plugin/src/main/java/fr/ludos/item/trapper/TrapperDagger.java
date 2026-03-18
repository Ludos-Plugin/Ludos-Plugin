package fr.ludos.item.trapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import fr.ludos.game.Game;
import fr.ludos.item.LevelItem;
import fr.ludos.item.LevelItem.LevelState;
import fr.ludos.item.MultiLevelBranchItem;
import fr.ludos.item.SpecialItem;
import fr.ludos.item.burrower.BurrowerPick;
import fr.ludos.item.huntsman.HuntsmanCrossbow;
import fr.ludos.item.huntsman.HuntsmanCrossbowBranches;
import fr.ludos.role.Role;
import fr.ludos.role.TrapperRole;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;


enum ArmorSlot {
	HELMET(EquipmentSlot.HEAD),
	CHESTPLATE(EquipmentSlot.CHEST),
	LEGGINGS(EquipmentSlot.LEGS),
	BOOTS(EquipmentSlot.FEET);

	private final EquipmentSlot slot;

	ArmorSlot(EquipmentSlot slot) {
		this.slot = slot;
	}

	public EquipmentSlot getSlot() {
		return slot;
	}

	public static ArmorSlot getRandomSlot() {
		ArmorSlot[] slots = values();
		return slots[new Random().nextInt(slots.length)];
	}
}

public class TrapperDagger extends MultiLevelBranchItem<TrapperDaggerBranches> {
	private final static String ID = "trapperDagger";

	// private final static Map<UUID, TrapperDagger> cachedItems = new HashMap<>();

	private static final int BRANCH_COUNT = TrapperDaggerBranches.values().length;
	private static final LevelState[] defaultLevels() {
		LevelState[] levels = new LevelState[BRANCH_COUNT];
		for (int i = 0; i < levels.length; i++) {
			levels[i] = new LevelState();
		}
		return levels;
	}

	public static @Nullable TrapperDagger fromItemStack(ItemStack stack, Game game) throws IllegalArgumentException {
		UUID itemId = SpecialItem.getSpecialItemId(stack, ID, game);
		if (itemId == null) return null;

		// TrapperDagger cached = cachedItems.get(itemId);
		// if (cached != null) return cached;

		Player owner = SpecialItem.getSpecialItemOwner(stack, game);
		if (owner == null) return null;

		TrapperDagger dagger = new TrapperDagger(stack, owner, TrapperDaggerBranches.SHARPNESS, defaultLevels(), game);
		// cachedItems.put(itemId, dagger);

		return dagger;
	}
	public static TrapperDagger createItem(Player owner, LevelItem.LevelState[] levels, Game game) {
		TrapperDagger dagger = new TrapperDagger(new ItemStack(Material.STONE_SWORD), owner, TrapperDaggerBranches.SHARPNESS, levels, game);
		UUID itemId = dagger.initializeItem();

		// cachedItems.put(itemId, dagger);

		return dagger;
	}

	protected TrapperDagger(ItemStack stack, Player owner, TrapperDaggerBranches branch, LevelItem.LevelState[] levels, Game game) {
		super(TrapperDaggerBranches.class, stack, owner, branch, levels, game);
	}


	@Override
	protected String getTypeId() {
		return ID;
	}
	@Override
	public Component getName() {
		return Component.text("Trapper Dagger ")
			.append(getBranchAnnotation())
			.decoration(TextDecoration.ITALIC, false);
	}


	public static class Events extends MultiLevelBranchItem.Events<TrapperDagger, TrapperDaggerBranches> {
		private final int luck = 2;

		public Events(Game game) {
			super(game, 0);
		}


		@EventHandler
		public void onPlayerImpactDagger(EntityDamageByEntityEvent event) {
			if (!(event.getDamager() instanceof Player attacker) || !(event.getEntity() instanceof Player victim)) {
				return;
			}

			TrapperDagger dagger = getItem(attacker.getInventory().getItemInMainHand(), game);
			if (dagger == null) return;

			victim.addPotionEffect(PotionEffectType.POISON.createEffect(60, 1));

			if (new Random().nextInt(100) < luck) {
				ArmorSlot randomSlot = ArmorSlot.getRandomSlot();
				EquipmentSlot equipmentSlot = randomSlot.getSlot();

				ItemStack originalItem = victim.getEquipment().getItem(equipmentSlot);

				if (originalItem != null && originalItem.getType() != Material.AIR) {
					victim.getEquipment().setItem(equipmentSlot, null);

					BukkitRunnable restoreTask = new BukkitRunnable() {
						@Override
						public void run() {
							victim.getEquipment().setItem(equipmentSlot, originalItem);
						}
					};
					restoreTask.runTaskLater(game.getPlugin(), 40L);
				}
			}
		}


		@EventHandler
		public void onPlayerInteract(PlayerInteractEvent event) {
			Action action = event.getAction();
			if (! action.isRightClick()) return;

			Player player = event.getPlayer();

			TrapperDagger dagger = getItem(player.getInventory().getItemInMainHand(), game);
			if (dagger == null) return;

			if (! dagger.refreshUseCooldown()) return;
			event.setCancelled(true);

			dagger.cycleBranch();
		}

		@Override
		@Nullable
		protected TrapperDagger getItem(ItemStack stack, Game game) {
			return TrapperDagger.fromItemStack(stack, game);
		}

		@Override
		protected TrapperDagger createItem(Player owner, LevelItem.LevelState[] levels, Game game) {
			return TrapperDagger.createItem(owner, levels, game);
		}

		@Override
		protected TrapperDaggerBranches[] getBranches() {
			return TrapperDaggerBranches.values();
		}

		@Override
		protected Boolean canPlayerHaveItem(HumanEntity owner) {
			return Role.isPlayerRole(owner, TrapperRole.id);
		}
	}
}