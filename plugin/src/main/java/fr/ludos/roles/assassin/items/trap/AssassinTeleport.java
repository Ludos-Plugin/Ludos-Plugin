package fr.ludos.roles.assassin.items.trap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import fr.ludos.core.game.Game;
import fr.ludos.core.item.ItemSlot;
import fr.ludos.core.item.SpecialItem;
import fr.ludos.roles.assassin.AssassinRole;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

/**
 * An Assassin's teleport beacon: place it once, then use it to return to its location.
 */
public class AssassinTeleport extends SpecialItem<AssassinTeleport> {
	public static final String ID = "marker_teleportation";
	private static final Material MATERIAL = Material.END_ROD;

	AssassinTeleport(SpecialItem.ItemData info, Events events) {
		super(info, events);
	}

	@Override
	public TextComponent displayName() {
		return Component.text("Marker Teleportation");
	}

	@Override
	public List<Component> getLore() {
		return List.of(
			Component.text("Place a marker"),
			Component.text("Move around and come back !"),
			Component.text("Cooldown : " + ((Events) getEvents()).getCooldownSeconds() + " secondes")
		);
	}

	/**
	 * Events for the {@link AssassinTeleport}.
	 */
	public static class Events extends SpecialItem.Events<AssassinTeleport> {
		private static final int TARGET_DISTANCE = 5;

		private final AssassinRole role;
		private final Map<UUID, Location> beacons = new HashMap<>();

		public Events(Game game, AssassinRole role) {
			super(game, new Events.Info(ItemSlot.HOTBAR_4));
			this.role = role;
		}

		@Override
		public String getTypeId() {
			return ID;
		}

		@Override
		public boolean isWeapon() {
			return false;
		}

		public int getCooldownSeconds() {
			return role.config(role.getBuilder().teleportCooldown);
		}

       
		@EventHandler
		public void onTeleportUse(PlayerInteractEvent event) {
			if (event.getHand() != EquipmentSlot.HAND || !event.getAction().isRightClick()) return;

			Player player = event.getPlayer();
			if (!isPlayerValid(player)) return;
			if (getItem(player.getInventory().getItemInMainHand()) == null) return;

			event.setCancelled(true);

			UUID playerId = player.getUniqueId();
			Location beaconLocation = beacons.get(playerId);
			if (beaconLocation != null && beaconLocation.getBlock().getType() != MATERIAL) {
				beacons.remove(playerId);
				beaconLocation = null;
			}

			if (player.getCooldown(MATERIAL) > 0) return;

			if (beaconLocation == null) {
				placeBeacon(player, playerId);
				return;
			}

			Location destination = beaconLocation.clone().add(0.5, 1, 0.5);
			if (!destination.getBlock().isPassable() || !destination.clone().add(0, 1, 0).getBlock().isPassable()) {
				player.sendActionBar(Component.text("Destination of Marker is blocked"));
				return;
			}

			if (!player.teleport(destination, TeleportCause.PLUGIN)) return;

			removeBeacon(playerId, beaconLocation);
			player.setCooldown(MATERIAL, 50 * getCooldownSeconds());
			player.sendActionBar(Component.text("Teleportation succesful"));
		}

		@EventHandler(ignoreCancelled = true)
		public void onBeaconBreak(BlockBreakEvent event) {
			if (event.getBlock().getType() != MATERIAL) return;

			Location brokenLocation = event.getBlock().getLocation();
			for (Location beaconLocation : beacons.values()) {
				if (!beaconLocation.getWorld().equals(brokenLocation.getWorld())
					|| beaconLocation.getBlockX() != brokenLocation.getBlockX()
					|| beaconLocation.getBlockY() != brokenLocation.getBlockY()
					|| beaconLocation.getBlockZ() != brokenLocation.getBlockZ()) {
					continue;
				}

				event.setCancelled(true);
				return;
			}
		}

		private void placeBeacon(Player player, UUID playerId) {
			Block targetBlock = player.getTargetBlockExact(TARGET_DISTANCE);
			if (targetBlock == null) {
				player.sendActionBar(Component.text("Select a block to place the marker"));
				return;
			}

			Block beaconBlock = targetBlock.getRelative(0, 1, 0);
			Block spaceAboveBeacon = beaconBlock.getRelative(0, 1, 0);
			if (!beaconBlock.isPassable() || !spaceAboveBeacon.isPassable()) {
				player.sendActionBar(Component.text("Imposible to put the Marker"));
				return;
			}

			beaconBlock.setType(MATERIAL, false);
			beacons.put(playerId, beaconBlock.getLocation());
			player.sendActionBar(Component.text("Marker Ready. Right Click to Teleport"));
		}

		private void removeBeacon(UUID playerId, Location beaconLocation) {
			Block beaconBlock = beaconLocation.getBlock();
			if (beaconBlock.getType() == MATERIAL) {
				beaconBlock.setType(Material.AIR, false);
			}
			beacons.remove(playerId);
		}

		@Override
		protected void onItemStop() {
			super.onItemStop();
			beacons.forEach((playerId, location) -> {
				if (location.getBlock().getType() == MATERIAL) {
					location.getBlock().setType(Material.AIR, false);
				}
			});
			beacons.clear();
		}

		@Override
		protected AssassinTeleport getItemInternal(SpecialItem.ItemData info) {
			return new AssassinTeleport(info, this);
		}

		@Override
		protected AssassinTeleport createItemInternal(Player owner) {
			return new AssassinTeleport(new SpecialItem.ItemData(new ItemStack(MATERIAL), owner), this);
		}

		@Override
		protected Boolean isPlayerValidInternal(OfflinePlayer owner) {
			return game.ludos().roleManager().isPlayerRole(owner, AssassinRole.ID);
		}
	}
}
