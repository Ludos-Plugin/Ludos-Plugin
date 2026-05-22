package fr.ludos.item.berserker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

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

import fr.ludos.game.Game;
import fr.ludos.item.SpecialItem;
import fr.ludos.role.BerserkerRole;
import fr.ludos.role.Role;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;


public class BerserkerRageBrew extends SpecialItem {
	public static final String ID = "berserkerRageBrew";

	@Override
	public String getTypeId() {
		return ID;
	}

	// Used to read/detect an existing ItemStack
	protected BerserkerRageBrew(ItemStack stack, Player owner, Game game) throws IllegalArgumentException {
		super(stack, owner, game);
	}

	// Used to create a new brew
	protected BerserkerRageBrew(Player owner, Game game) {
		super(new ItemStack(Material.SUSPICIOUS_STEW), owner, game);
	}


	@Nullable
	public static BerserkerRageBrew getItem(ItemStack stack, Game game) {
		UUID itemId = SpecialItem.getSpecialItemId(stack, ID, game);
		if (itemId == null) return null;

		// BerserkerRageBrew cached = cachedItems.get(itemId);
		// if (cached != null) return cached;

		Player owner = SpecialItem.getSpecialItemOwner(stack, game);
		if (owner == null) return null;

		BerserkerRageBrew brew = new BerserkerRageBrew(stack, owner, game);
		// cachedItems.put(itemId, brew);

		return brew;
	}

	public static BerserkerRageBrew createItem(Player owner, Game game) {
		BerserkerRageBrew brew = new BerserkerRageBrew(owner, game);
		UUID itemId = brew.initializeItem();

		// cachedItems.put(itemId, brew);

		return brew;
	}


	@Override
	public Component getName() {
		return Component.text("Rage Brew")
			.decoration(TextDecoration.ITALIC, false);
	}

	@Override
	public List<Component> getLore() {
		return List.of(
			Component.text("Clic droit : déchaîner la rage pendant 10s.")
				.decoration(TextDecoration.ITALIC, false),
			Component.text("Recharge : 30s, puis courte fatigue.")
				.decoration(TextDecoration.ITALIC, false)
		);
	}


	public static class Events extends SpecialItem.Events<BerserkerRageBrew> {
		private final Map<UUID, Long> cooldowns = new HashMap<>();
		private static final long COOLDOWN_MS = 30_000;
		private static final int RAGE_DURATION_TICKS = 20 * 10;
		private static final int FATIGUE_TICKS = 20 * 5;

		private final BerserkerRole role;

		public Events(Game game, BerserkerRole role) {
			super(game);
			this.role = role;
		}

		@Override
		protected void onItemStop() {
			super.onItemStop();

			cooldowns.clear();
			for (Player player : Bukkit.getOnlinePlayers()) {
				role.setRage(player, false);
			}
		}

		@Override
		@Nullable
		public BerserkerRageBrew getItem(ItemStack stack) {
			return BerserkerRageBrew.getItem(stack, game);
		}

		@Override
		public BerserkerRageBrew createItem(Player owner) {
			return BerserkerRageBrew.createItem(owner, game);
		}

		@Override
		protected Boolean isPlayerValidInternal(OfflinePlayer owner) {
			return Role.isPlayerRole(owner, BerserkerRole.ID);
		}

		@EventHandler
		public void onPlayerInteract(PlayerItemConsumeEvent event) {
			Player player = event.getPlayer();
			if (! isPlayerValid(player)) return;

			BerserkerRageBrew brew = getItem(event.getItem());
			if (brew == null) return;

			event.setReplacement(brew.getStack());

			long now = System.currentTimeMillis();
			long availableAt = cooldowns.getOrDefault(player.getUniqueId(), 0L);
			if (availableAt > now) {
				long remaining = (availableAt - now) / 1000;
				player.sendMessage(
					Component.text("Rage Brew", NamedTextColor.DARK_RED)
						.append(Component.text(" en cooldown (", NamedTextColor.RED))
						.append(Component.text(remaining + "s", NamedTextColor.YELLOW))
						.append(Component.text(").", NamedTextColor.RED))
				);
				return;
			}

			triggerRage(player);
			cooldowns.put(player.getUniqueId(), now + COOLDOWN_MS);
			player.setCooldown(brew.getStack().getType(), (int) (COOLDOWN_MS / 50));
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
