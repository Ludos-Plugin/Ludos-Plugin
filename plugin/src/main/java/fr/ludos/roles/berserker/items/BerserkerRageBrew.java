package fr.ludos.roles.berserker.items;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.ludos.core.game.Game;
import fr.ludos.core.item.ItemSlot;
import fr.ludos.core.item.SpecialItem;
import fr.ludos.roles.berserker.BerserkerRole;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * Implementation of the Berserker Rage Brew, for use by any Player with {@link BerserkerRole}.
 */
public class BerserkerRageBrew extends SpecialItem<BerserkerRageBrew> {
	public static final String ID = "berserker_rage_brew";

	BerserkerRageBrew(SpecialItem.ItemData info, Events events) {
		super(info, events);
	}


	@Override
	public TextComponent displayName() {
		return Component.text("Rage Brew");
	}

	@Override
	public List<Component> getLore() {
		return List.of(
			Component.text("Consume to unleash rage for ")
			.append(Component.text("10s.")
				.color(NamedTextColor.GOLD)
			)
				.decoration(TextDecoration.ITALIC, false)
		);
	}

	/**
	 * Events for the {@link BerserkerRageBrew}.
	 */
	public static class Events extends SpecialItem.Events<BerserkerRageBrew> {
		private static final int RAGE_DURATION_S = 10;
		private static final int RAGE_DURATION_TICKS = RAGE_DURATION_S * 20;
		private static final int FATIGUE_S = 5;
		private static final int FATIGUE_TICKS = FATIGUE_S * 20;
		private static final int COOLDOWN_S = RAGE_DURATION_S + 20;
		private static final int COOLDOWN_TICKS = COOLDOWN_S * 20;

		private final BerserkerRole role;

		public Events(Game game, BerserkerRole role) {
			super(game, new Events.Info(ItemSlot.HOTBAR_3));
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

		@Override
		protected void onItemStop() {
			super.onItemStop();

			for (Player player : Bukkit.getOnlinePlayers()) {
				role.setRage(player, false);
			}
		}

		@Override
		protected BerserkerRageBrew getItemInternal(SpecialItem.ItemData info) {
			return new BerserkerRageBrew(info, this);
		}

		@Override
		protected BerserkerRageBrew createItemInternal(Player owner) {
			return new BerserkerRageBrew(new SpecialItem.ItemData(new ItemStack(Material.SUSPICIOUS_STEW), owner), this);
		}

		@Override
		protected Boolean isPlayerValidInternal(OfflinePlayer owner) {
			return game.ludos().getRoleManager().isPlayerRole(owner, BerserkerRole.ID);
		}

		@EventHandler
		public void onPlayerInteract(PlayerItemConsumeEvent event) {
			Player player = event.getPlayer();
			if (! isPlayerValid(player)) return;

			BerserkerRageBrew brew = getItem(event.getItem());
			if (brew == null) return;

			event.setReplacement(brew.getStack());

			triggerRage(player);
			player.setCooldown(brew.getStack().getType(), COOLDOWN_TICKS);
		}

		private void triggerRage(Player player) {
			role.setRage(player, true);

			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, RAGE_DURATION_TICKS, 1, false, false));
			player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, RAGE_DURATION_TICKS, 0, false, true));

			player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1f, 0.8f);

			game.getPlugin().getServer().getScheduler().runTaskLater(game.getPlugin(), () -> {
				role.setRage(player, false);

				player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, FATIGUE_TICKS, 0, false, false));
				player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, FATIGUE_TICKS, 0, false, true));
			}, RAGE_DURATION_TICKS);
		}
	}
}
