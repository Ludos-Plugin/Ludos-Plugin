package fr.ludos.item.berserker;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.ludos.item.SpecialItem;
import fr.ludos.game.Game;
import fr.ludos.role.BerserkerRole;
import fr.ludos.role.Role;


public class BerserkerRageBrew extends SpecialItem {
	private static final UUID ATTACK_SPEED_MODIFIER_ID = UUID.fromString("d32dc47e-95f6-4491-b3a7-6f3be8336b48");

	public BerserkerRageBrew(ItemStack stack, Game game) throws IllegalArgumentException {
		super(stack, game);
	}

	public BerserkerRageBrew(Player owner, Game game) {
		super(new ItemStack(Material.SUSPICIOUS_STEW), owner, game);
	}

	@Override
	public String getId() {
		return "berserkerRageBrew";
	}

	@Override
	protected Component getName() {
		return Component.text("Rage Brew")
			.decoration(TextDecoration.ITALIC, false);
	}

	@Override
	protected java.util.List<Component> getLore() {
		return java.util.List.of(
			Component.text("Right Click: unleash rage for 10s.")
				.decoration(TextDecoration.ITALIC, false),
			Component.text("Cooldown: 30s, then short fatigue.")
				.decoration(TextDecoration.ITALIC, false)
		);
	}


	@Nullable
	public static BerserkerRageBrew getItem(ItemStack stack, Game game) {
		try {
			return new BerserkerRageBrew(stack, game);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	public static BerserkerRageBrew createItem(Player owner, Game game) {
		return new BerserkerRageBrew(owner, game);
	}


	public static class Events extends SpecialItem.Events<BerserkerRageBrew> {
		private final Map<UUID, Long> cooldowns = new HashMap<>();
		private static final long COOLDOWN_MS = 30_000;
		private static final int RAGE_DURATION_TICKS = 20 * 10;
		private static final int FATIGUE_TICKS = 20 * 5;

		public Events(Game game) {
			super(game);
		}

		@Override
		@Nullable
		protected BerserkerRageBrew getItem(ItemStack stack, Game game) {
			return BerserkerRageBrew.getItem(stack, game);
		}

		@Override
		protected BerserkerRageBrew createItem(Player owner, Game game) {
			return BerserkerRageBrew.createItem(owner, game);
		}

		@Override
		protected Boolean canPlayerHaveItem(HumanEntity owner) {
			return Role.isPlayerRole(owner, BerserkerRole.id);
		}

		@EventHandler
		public void onPlayerInteract(PlayerInteractEvent event) {
			Action action = event.getAction();
			if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

			BerserkerRageBrew brew = getItem(event.getItem(), game);
			if (brew == null) return;

			Player player = event.getPlayer();
			if (!canPlayerHaveItem(player)) return;

			event.setCancelled(true);

			long now = System.currentTimeMillis();
			long availableAt = cooldowns.getOrDefault(player.getUniqueId(), 0L);
			if (availableAt > now) {
				long remaining = (availableAt - now) / 1000;
				player.sendMessage("Rage Brew on cooldown (" + remaining + "s).");
				return;
			}

			triggerRage(player);
			cooldowns.put(player.getUniqueId(), now + COOLDOWN_MS);
			player.setCooldown(brew.getStack().getType(), (int) (COOLDOWN_MS / 50));
		}

		private void triggerRage(Player player) {
			BerserkerRole.setRage(player, true);

			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, RAGE_DURATION_TICKS, 1, false, false));
			player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, RAGE_DURATION_TICKS, 1, false, false));
			applyAttackSpeedBuff(player);

			player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1f, 0.8f);

			game.getPlugin().getServer().getScheduler().runTaskLater(game.getPlugin(), () -> {
				removeAttackSpeedBuff(player);
				BerserkerRole.setRage(player, false);

				player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, FATIGUE_TICKS, 0, false, false));
				player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, FATIGUE_TICKS, 0, false, false));
			}, RAGE_DURATION_TICKS);
		}

		private void applyAttackSpeedBuff(Player player) {
			AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
			if (attribute == null) return;

			removeAttackSpeedBuff(player);
			AttributeModifier modifier = new AttributeModifier(
				ATTACK_SPEED_MODIFIER_ID,
				"berserker_rage_attack_speed",
				0.5,
				Operation.MULTIPLY_SCALAR_1
			);
			attribute.addTransientModifier(modifier);
		}

		private void removeAttackSpeedBuff(Player player) {
			AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
			if (attribute == null) return;
			attribute.getModifiers().stream()
				.filter(modifier -> modifier.getUniqueId().equals(ATTACK_SPEED_MODIFIER_ID))
				.findFirst()
				.ifPresent(attribute::removeModifier);
		}
	}
}
