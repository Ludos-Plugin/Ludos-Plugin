package fr.ludos.roles.assassin.items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.ludos.core.Utility;
import fr.ludos.core.game.Game;
import fr.ludos.core.game.teamController.GameTeamController;
import fr.ludos.core.item.ItemSlot;
import fr.ludos.core.item.SpecialItem;
import fr.ludos.roles.assassin.AssassinRole;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

/**
 * Implementation of the Assassin Smoke Bomb, for use by any Player with {@link AssassinRole}.
 */
public class SmokeBomb extends SpecialItem<SmokeBomb> {
	public static final String ID = "smoke_bomb";

	SmokeBomb(SpecialItem.ItemData info, Events events) {
		super(info, events);
	}

	@Override
	public TextComponent displayName() {
		return Component.text("Bombe fumigène");
	}

	@Override
	public List<Component> getLore() {
		return new ArrayList<>(Arrays.asList(
			Component.text("Aveugle les ennemis proches"),
			Component.text("Te rend invisible, même en mouvement"),
			Component.text("Cooldown : " + ((Events) getEvents()).getCooldownSeconds() + " secondes")
		));
	}

	/**
	 * Events for the {@link SmokeBomb}.
	 */
	public static class Events extends SpecialItem.Events<SmokeBomb> {
		private static final Material MATERIAL = Material.FIREWORK_STAR;

		private final AssassinRole role;

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
			return role.config(role.getBuilder().smokeCooldown);
		}

		@EventHandler
		public void onSmokeBombUse(PlayerInteractEvent event) {
			Player player = event.getPlayer();
			if (! isPlayerValid(player)) return;

			if (!event.getAction().isRightClick()) return;

			ItemStack itemInHand = player.getInventory().getItemInMainHand();
			if (getItem(itemInHand) == null) return;

			if (player.getCooldown(MATERIAL) > 0) return;

			event.setCancelled(true);

			int radius = role.config(role.getBuilder().smokeRadius);
			int duration = 20 * role.config(role.getBuilder().smokeDuration);
			Location center = player.getLocation();
			World world = player.getWorld();

			// Nuage de fumée
			Location cloud = center.clone().add(0, 1, 0);
			world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, cloud, 15 * Math.max(1, radius), radius / 2.0, 1, radius / 2.0, 0.01);
			world.spawnParticle(Particle.SMOKE_LARGE, cloud, 10 * Math.max(1, radius), radius / 2.0, 0.8, radius / 2.0, 0.02);
			world.playSound(center, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1f, 0.6f);
			world.playSound(center, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.8f, 0.5f);

			// Aveuglement des ennemis
			GameTeamController teamController = game.teamController();
			center.getNearbyLivingEntities(radius).stream()
				.filter(Utility.IS_ENTITY_ALIVE)
				.filter(target -> teamController != null
					? teamController.isEntityEnemyOfEntity(player).test(target)
					: ! target.equals(player))
				.forEach(target -> target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, duration, 0)));

			role.grantInvisibility(player, duration);
			player.setCooldown(MATERIAL, 20 * getCooldownSeconds());
		}

		@Override
		protected SmokeBomb getItemInternal(SpecialItem.ItemData info) {
			return new SmokeBomb(info, this);
		}

		@Override
		protected SmokeBomb createItemInternal(Player owner) {
			return new SmokeBomb(new SpecialItem.ItemData(new ItemStack(MATERIAL), owner), this);
		}

		@Override
		protected Boolean isPlayerValidInternal(OfflinePlayer owner) {
			return game.ludos().roleManager().isPlayerRole(owner, AssassinRole.ID);
		}
	}
}
