package fr.ludos.item.assassin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import fr.ludos.game.Game;
import fr.ludos.item.SpecialItem;
import fr.ludos.role.AssassinRole;
import fr.ludos.role.Role;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

public class TeleportScroll extends SpecialItem {
	public static final String ID = "teleport_scroll";

	// private final static Map<UUID, TeleportScroll> cachedItems = new HashMap<>();

	public static @Nullable TeleportScroll fromItemStack(ItemStack stack, Game game) throws IllegalArgumentException {
		UUID itemId = SpecialItem.getSpecialItemId(stack, ID, game);
		if (itemId == null) return null;

		// TeleportScroll cached = cachedItems.get(itemId);
		// if (cached != null) return cached;

		Player owner = SpecialItem.getSpecialItemOwner(stack, game);
		if (owner == null) return null;

		TeleportScroll scroll = new TeleportScroll(stack, owner, game);
		// cachedItems.put(itemId, scroll);

		return scroll;
	}

	public static TeleportScroll createItem(Player owner, Game game) {
		TeleportScroll scroll = new TeleportScroll(new ItemStack(Material.PAPER), owner, game);
		UUID itemId = scroll.initializeItem();

		// cachedItems.put(itemId, scroll);

		return scroll;
	}

	public TeleportScroll(ItemStack stack, Player player, Game game) {
		super(stack, player, game);
	}

	@Override
	public String getTypeId() {
		return ID;
	}

	@Override
	protected Component getName(){
		return Component.text("Parchemin de Téléportation")
			.decoration(TextDecoration.ITALIC, false);
	}

	@Override
	public List<Component> getLore(){
		return new ArrayList<>(Arrays.asList(
			Component.text("Téléporte-toi aléatoirement"),
			Component.text("Perd 2 coeurs max à l'utilisation"),
			Component.text("Cooldown : 30 secondes")
		));
	}

	public static class Events extends SpecialItem.Events<TeleportScroll> {
		private static final int COOLDOWN = 20 * 30; // 30 secondes
		private static final int MAX_ATTEMPTS = 100;
		private static final int INVULNERABILITY_DURATION = 20; // 1 seconde
		private static final String HEALTH_MODIFIER_NAME = "teleport_scroll_health_reduction";
		private final Random random = new Random();

		public Events(Game game) {
			super(game);
		}

		@Override
		protected void onItemStop() {
			super.onItemStop();

			for (Player player : Role.getPlayersOfRole(AssassinRole.id)) {
				var healthAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
				if (healthAttr == null) continue;
				new ArrayList<>(healthAttr.getModifiers()).stream()
					.filter(m -> HEALTH_MODIFIER_NAME.equals(m.getName()))
					.forEach(healthAttr::removeModifier);
				if (player.getHealth() > healthAttr.getValue()) {
					player.setHealth(healthAttr.getValue());
				}
			}
		}

		@EventHandler
		public void onScrollUse(PlayerInteractEvent event) {
			Player player = event.getPlayer();
			if (! isPlayerValid(player)) return;

			if (!event.getAction().isRightClick()) return;

			ItemStack itemInHand = player.getInventory().getItemInMainHand();
			if (getItem(itemInHand) == null) return;

			if (player.getCooldown(Material.PAPER) > 0) return;

			event.setCancelled(true);

			Location randomLocation = findSafeLocation(player.getWorld());

			if (randomLocation != null) {
				player.teleport(randomLocation);

				// Réduction permanente de 2 coeurs max (4 HP)
				var healthAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
				if (healthAttr != null && healthAttr.getValue() > 4) {
					healthAttr.addModifier(new AttributeModifier(
						UUID.randomUUID(),
						HEALTH_MODIFIER_NAME,
						-4.0,
						AttributeModifier.Operation.ADD_NUMBER
					));
					if (player.getHealth() > healthAttr.getValue()) {
						player.setHealth(healthAttr.getValue());
					}
				}

				// Annuler les dégâts pendant 1 seconde
				player.setInvulnerable(true);
				new BukkitRunnable() {
					@Override
					public void run() {
						player.setInvulnerable(false);
					}
				}.runTaskLater(game.getPlugin(), INVULNERABILITY_DURATION);

				player.setCooldown(Material.PAPER, COOLDOWN);
			}
		}

		private Location findSafeLocation(World world) {
			Location borderCenter = world.getWorldBorder().getCenter();
			double borderSize = world.getWorldBorder().getSize() / 2;

			for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
				int x = random.nextInt((int)(borderCenter.getX() - borderSize), (int)(borderCenter.getX() + borderSize));
				int z = random.nextInt((int)(borderCenter.getZ() - borderSize), (int)(borderCenter.getZ() + borderSize));

				int highestY = world.getHighestBlockYAt(x, z);
				Block blockAtY = world.getBlockAt(x, highestY, z);

				if (blockAtY.getType() == Material.WATER) {
					continue;
				}

				Block block1 = world.getBlockAt(x, highestY + 1, z);
				Block block2 = world.getBlockAt(x, highestY + 2, z);

				if (block1.getType() != Material.AIR || block2.getType() != Material.AIR) {
					continue;
				}

				return new Location(world, x + 0.5, highestY + 1.5, z + 0.5);
			}

			return null;
		}

		@Override
		@Nullable
		public TeleportScroll getItem(ItemStack stack) {
			return TeleportScroll.fromItemStack(stack, game);
		}

		@Override
		public TeleportScroll createItem(Player owner) {
			return TeleportScroll.createItem(owner, game);
		}

		@Override
		protected Boolean isPlayerValidInternal(OfflinePlayer owner) {
			return Role.isPlayerRole(owner, AssassinRole.id);
		}
	}
}
