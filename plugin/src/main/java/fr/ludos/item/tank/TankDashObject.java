package fr.ludos.item.tank;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import fr.ludos.game.Game;
import fr.ludos.item.SpecialItem;
import fr.ludos.role.Role;
import fr.ludos.role.TankRole;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

public class TankDashObject extends SpecialItem {
	private static final String ID = "tank_dasher";
	private static final int COOLDOWN_DURATION = 30;
	public static final double dashpower = 5.0;

	protected TankDashObject(ItemStack stack, Player owner, Game game) {
		super(stack, owner, game);
	}

	public static @Nullable TankDashObject fromItemStack(ItemStack stack, Game game) throws IllegalArgumentException {
		UUID itemId = SpecialItem.getSpecialItemId(stack, ID, game);
		if (itemId == null) return null;

		// TrapperDagger cached = cachedItems.get(itemId);
		// if (cached != null) return cached;

		Player owner = SpecialItem.getSpecialItemOwner(stack, game);
		if (owner == null) return null;

		TankDashObject dasher = new TankDashObject(stack, owner, game);
		// cachedItems.put(itemId, dagger);

		return dasher;
	}

	public static TankDashObject createItem(Player owner, Game game) {
		TankDashObject dasher = new TankDashObject(createItemStack(), owner, game);
		dasher.initializeItem();

		// cachedItems.put(itemId, dagger);

		return dasher;
	}

	private static final double COLLISION_RADIUS = 1.5;
	private static final double COLLISION_DAMAGE = 4.0;
	private static final double COLLISION_KNOCKBACK = 1.5;
	private static final int DASH_DURATION_TICKS = 10;

	public void useDash(Vector direction) {
		Player player = this.getOwner();
		player.setVelocity(direction.normalize().multiply(dashpower));

		Set<UUID> alreadyHit = new HashSet<>();

		new BukkitRunnable() {
			int ticks = 0;

			@Override
			public void run() {
				if (ticks++ >= DASH_DURATION_TICKS) {
					cancel();
					return;
				}

				for (Entity entity : player.getNearbyEntities(COLLISION_RADIUS, COLLISION_RADIUS, COLLISION_RADIUS)) {
					if (!(entity instanceof Player target)) continue;
					if (alreadyHit.contains(target.getUniqueId())) continue;

					alreadyHit.add(target.getUniqueId());

					target.damage(COLLISION_DAMAGE, player);

					Vector knockback = target.getLocation().toVector()
						.subtract(player.getLocation().toVector())
						.normalize()
						.multiply(COLLISION_KNOCKBACK)
						.setY(0.4);
					target.setVelocity(knockback);
				}
			}
		}.runTaskTimer(this.getGame().getPlugin(), 0, 1);
	}

	@Override
	public String getTypeId() {
		return ID;
	}

	@Override
	public Component getName() {
		return Component.text("Tank Dasher")
				.decoration(TextDecoration.ITALIC, false);
	}

	private static ItemStack createItemStack() {
		ItemStack stack = new ItemStack(Material.FIREWORK_ROCKET);

		return stack;
	}

	public static class Events extends SpecialItem.Events<TankDashObject> {

		public Events(Game game) {
			super(game, 2, false);
		}

		@Override
		@Nullable
		protected TankDashObject getItem(ItemStack stack, Game game) {
			return TankDashObject.fromItemStack(stack, game);
		}

		@Override
		protected TankDashObject createItem(Player owner, Game game) {
			return TankDashObject.createItem(owner, game);
		}

		@Override
		protected Boolean canPlayerHaveItem(HumanEntity owner) {
			return Role.isPlayerRole(owner, TankRole.id);
		}

		@EventHandler
		public void onPlayerUseDash(PlayerInteractEvent event) {
			if (event.getHand() != EquipmentSlot.HAND)
				return;
			if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
				return;

			Player player = event.getPlayer();
			ItemStack item = player.getInventory().getItemInMainHand();

			TankDashObject dasher = TankDashObject.fromItemStack(item, game);
			if (dasher == null) return;
			event.setCancelled(true);

			if (player.getCooldown(Material.FIREWORK_ROCKET) > 0) return;

			dasher.useDash(player.getLocation().getDirection());

			player.setCooldown(Material.FIREWORK_ROCKET, COOLDOWN_DURATION * 20);
		}
	}

}