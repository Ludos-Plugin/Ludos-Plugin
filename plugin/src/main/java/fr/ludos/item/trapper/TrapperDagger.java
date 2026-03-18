package fr.ludos.item.trapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import fr.ludos.game.Game;
import fr.ludos.item.LevelItem;
import fr.ludos.item.SpecialItem;
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

public class TrapperDagger extends LevelItem<TrapperDaggerLevels> {
	private final static String ID = "trapperDagger";
	private final static Map<ItemStack, TrapperDagger> cachedItems = new HashMap<>();


	public static @Nullable TrapperDagger fromItemStack(ItemStack stack, Game game) throws IllegalArgumentException {
		TrapperDagger cached = cachedItems.get(stack);
		if (cached != null) return cached;

		Player owner = SpecialItem.getSpecialItemOwner(stack, ID, game);
		if (owner == null) return null;

		return new TrapperDagger(stack, owner, new LevelState(), game);
	}

	public static TrapperDagger createItem(Player owner, Game game) {
		TrapperDagger dagger = new TrapperDagger(new ItemStack(Material.STONE_SWORD), owner, new LevelState(), game);
		dagger.initializeItem();

		return dagger;
	}

	protected TrapperDagger(ItemStack stack, Player owner, LevelState level, Game game) {
		super(TrapperDaggerLevels.class, stack, owner, level, game);
	}


	@Override
	protected String getId() {
		return ID;
	}

	@Override
	public Component getName() {
		return Component.text("Trapper Dagger")
			.decoration(TextDecoration.ITALIC, false);
	}

	public static class Events extends SpecialItem.Events<TrapperDagger> {
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


		@Override
		@Nullable
		protected TrapperDagger getItem(ItemStack stack, Game game) {
			return TrapperDagger.fromItemStack(stack, game);
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
}