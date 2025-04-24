package fr.ludos.item.trapper;

import java.util.Random;
import javax.annotation.Nullable;

import net.kyori.adventure.text.Component;

import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import fr.ludos.item.SpecialItem;
import fr.ludos.item.LevelItem;
import fr.ludos.game.Game;
import fr.ludos.role.Role;
import fr.ludos.role.TrapperRole;


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

public class TrapperDagger extends LevelItem<TrapperDaggerLevels> {

	public TrapperDagger(ItemStack stack, Game game) {
		super(stack, game);
	}
	public TrapperDagger(Player owner, Game game) {
		super(new ItemStack(Material.STONE_SWORD), owner, game);
	}

	@Override
	protected String getId() {
		return "trapperDagger";
	}
	@Override
	protected Component getName() {
		return Component.text("Trapper Dagger");
	}


	@Nullable
	public static TrapperDagger getItem(ItemStack stack, Game game) {
		try {
			TrapperDagger dagger = new TrapperDagger(stack, game);
			return dagger;
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	public static TrapperDagger createItem(Player owner, Game game) {
		return new TrapperDagger(owner, game);
	}


	public static class Events extends SpecialItem.Events<TrapperDagger> {
		private final int luck = 2;

		public Events(Game game) {
			super(game);
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


		@Override
		@Nullable
		protected TrapperDagger getItem(ItemStack stack, Game game) {
			return TrapperDagger.getItem(stack, game);
		}

		@Override
		protected TrapperDagger createItem(Player owner, Game game) {
			return TrapperDagger.createItem(owner, game);
		}

		@Override
		protected Boolean canPlayerHaveItem(HumanEntity owner) {
			return Role.isPlayerRole(owner, TrapperRole.id);
		}
	}

	@Override
	public TrapperDaggerLevels convertToLevel(int level) {
		return TrapperDaggerLevels.findByKey(level);
	}
}