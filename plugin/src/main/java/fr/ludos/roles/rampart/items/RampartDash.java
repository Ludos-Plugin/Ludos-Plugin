package fr.ludos.roles.rampart.items;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import fr.ludos.core.game.Game;
import fr.ludos.core.item.ItemSlot;
import fr.ludos.core.item.SpecialItem;
import fr.ludos.core.item.SpecialItemInterface;
import fr.ludos.roles.rampart.RampartRole;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * Implementation of the Rampart Dash/Charge, for use by any Player with {@link RampartRole}.
 */
public class RampartDash extends SpecialItem {
	public static final String ID = "rampart_dash";

	private static final double DASH_POWER = 1.1;
	private static final double UNDERWATER_DASH_POWER = 0.55;
	private static final int DASH_DURATION_TICKS = 12;
	private static final Vector COLLISION_RANGE = new Vector(1.5, 1.0, 1.5);
	private static final double COLLISION_DAMAGE = 4.0;
	private static final double COLLISION_KNOCKBACK = 1.2;
	private static final int COOLDOWN_DURATION = 15 * 20;

	protected RampartDash(ItemStack stack, Player owner, Game game) {
		super(stack, owner, game);
	}

	public static @Nullable RampartDash fromItemStack(ItemStack stack, Game game) throws IllegalArgumentException {
		UUID itemId = SpecialItemInterface.getSpecialItemId(stack, ID, game);
		if (itemId == null) return null;

		// TrapperDagger cached = cachedItems.get(itemId);
		// if (cached != null) return cached;

		Player owner = SpecialItemInterface.getSpecialItemOwner(stack, game);
		if (owner == null) return null;

		RampartDash dasher = new RampartDash(stack, owner, game);
		// cachedItems.put(itemId, dagger);

		return dasher;
	}

	public static RampartDash createItem(Player owner, Game game) {
		RampartDash dasher = new RampartDash(createItemStack(), owner, game);
		dasher.initializeItem();

		// cachedItems.put(itemId, dagger);

		return dasher;
	}

	public void useDash() {
		Player player = this.getOwner();

		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0f, 1.0f);

		Set<Entity> alreadyHit = new HashSet<>();

		new BukkitRunnable() {
			int ticks = 0;

			@Override
			public void run() {
				if (ticks >= DASH_DURATION_TICKS) {
					cancel();
					return;
				}

				if (ticks % 9 == 0) {
					player.getWorld().playSound(player.getLocation(), Sound.ENTITY_HORSE_GALLOP, 1.0f, 1.0f);
				}

				if (player.isInWater()) {
					player.setVelocity(
						player.getLocation().getDirection()
						.multiply(UNDERWATER_DASH_POWER)
					);
				}
				else {
					double yaw_rad = Math.toRadians(player.getLocation().getYaw());
					Vector velocity = new Vector(
							-Math.sin(yaw_rad),
							0,
							Math.cos(yaw_rad)
						)
						.normalize().multiply(DASH_POWER);
					player.setVelocity(
						player.getVelocity()
							.setX(0)
							.setZ(0)
							.add(velocity)
					);
				}

				player.spawnParticle(
					Particle.FIREWORKS_SPARK,
					player.getLocation().add(0, 1.25, 0),
					1,
					0.05, 0.05, 0.05,
					0
				);

				Stream<Entity> targetsStream = player.getNearbyEntities(COLLISION_RANGE.getX(), COLLISION_RANGE.getY(), COLLISION_RANGE.getZ()).stream()
					.filter(getGame().getTeamController().isEntityEnemyOfPlayer(player));
				Iterable<Entity> targets = () -> targetsStream.iterator();

				for (Entity entity : targets) {
					if (! (entity instanceof Damageable target)) continue;
					if (alreadyHit.contains(target)) continue;

					alreadyHit.add(target);

					target.damage(COLLISION_DAMAGE, player);

					Vector knockback = target.getLocation().toVector()
						.subtract(player.getLocation().toVector())
						.normalize()
						.multiply(COLLISION_KNOCKBACK)
						.setY(0.4);
					target.setVelocity(knockback);
				}
				ticks++;
			}
		}.runTaskTimer(this.getGame().getPlugin(), 0, 1);
	}

	@Override
	public String getTypeId() {
		return ID;
	}

	@Override
	public Component getName() {
		return Component.text("Rampart Charge")
				.decoration(TextDecoration.ITALIC, false);
	}

	@Override
	public List<Component> getLore() {
		List<Component> lore = super.getLore();
		lore.add(SpecialItemInterface.getActionAnnotation("key.use", Component.text("Charge")));
		return lore;
	}

	private static ItemStack createItemStack() {
		ItemStack stack = new ItemStack(Material.FIREWORK_ROCKET);

		return stack;
	}

	/**
	 * Events for the {@link RampartDash}.
	 */
	public static class Events extends SpecialItem.Events<RampartDash> {

		public Events(Game game) {
			super(game, new Events.Info(ItemSlot.HOTBAR_3));
		}

		@Override
		@Nullable
		public RampartDash getItem(ItemStack stack) {
			return RampartDash.fromItemStack(stack, game);
		}

		@Override
		public RampartDash createItem(Player owner) {
			return RampartDash.createItem(owner, game);
		}

		@Override
		protected Boolean isPlayerValidInternal(OfflinePlayer owner) {
			return getGame().getLudos().getRoleManager().isPlayerRole(owner, RampartRole.ID);
		}

		@EventHandler
		public void onPlayerUseDash(PlayerInteractEvent event) {
			if (event.getHand() != EquipmentSlot.HAND)
				return;
			if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
				return;

			Player player = event.getPlayer();
			ItemStack item = player.getInventory().getItemInMainHand();

			RampartDash dasher = RampartDash.fromItemStack(item, game);
			if (dasher == null) return;
			event.setCancelled(true);

			if (player.getCooldown(Material.FIREWORK_ROCKET) > 0) return;

			dasher.useDash();

			player.setCooldown(Material.FIREWORK_ROCKET, COOLDOWN_DURATION);
		}
	}
}