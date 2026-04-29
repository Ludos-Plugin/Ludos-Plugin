package fr.ludos.role;

import java.util.LinkedHashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffect;

import fr.ludos.Ludos;
import fr.ludos.game.Game;
import fr.ludos.game.GameEvents;
import fr.ludos.item.SpecialItem.Events;
import fr.ludos.role.Role;
import fr.ludos.role.Role.Builder;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.Component;

public class TankRole extends Role {
	public static final String id = "tank";

	public TankRole(Builder builder, Game game) {
		super(builder, game);
	}

	@Override
	protected LinkedHashMap<String, GameEvents> createGameEvents(Role.Builder builder, Game game) {
		return new LinkedHashMap<>() {
			{
				put("tankShield", new fr.ludos.item.tank.TankShield.Events(game));
				put("tankHelmet", new fr.ludos.item.tank.TankHelmet.Events(game));
				put("tankDasher", new fr.ludos.item.tank.TankDashObject.Events(game));
			}
		};
	}

	public static void setAttributePlayer(List<Player> players, PotionEffect AbsorbEffect,
			PotionEffect SlownessEffect) {
	}

	@Override
	protected void onRoleStart() {
		List<Player> players = Role.getPlayersOfRole(id);

		PotionEffect absordEffect = new PotionEffect(
				PotionEffectType.ABSORPTION,
				Integer.MAX_VALUE,
				0,
				false,
				false);

		for (Player player : players) {
			player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(28.0);
			player.addPotionEffect(absordEffect);
		}
	}

	@Override
	protected void onRoleStop() {
		for (Player player : Role.getPlayersOfRole(id)) {
			player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0);
			for (PotionEffect potion : player.getActivePotionEffects()) {
				player.removePotionEffect(potion.getType());
			}
		}
	}

	@EventHandler
	public void onPlayerHit(EntityDamageByEntityEvent event) {
		if (!(event.getEntity() instanceof Player defender)) return;
		if (!(event.getDamager() instanceof Player attacker)) return;

		if (!Role.getPlayersOfRole(id).contains(attacker)) return;

		Material material = attacker.getInventory().getItemInMainHand().getType();

		double multiplier = switch (material) {
			case WOODEN_SWORD -> 0.9;
			case IRON_SWORD -> 0.7;
			case STONE_SWORD -> 0.8;
			case DIAMOND_SWORD -> 0.6;
			default -> 1;
		};

		event.setDamage(event.getDamage() * multiplier);
	}

	public static class Builder extends Role.Builder {
		@Override
		public String getId() {
			return id;
		}

		public Builder(Ludos plugin) {
			super(plugin);
		}

		@Override
		public Role build(Game game) {
			return new TankRole(this, game);
		}

		@Override
		public TextComponent getDisplayName() {
			return Component.text("Tank");
		}

		@Override
		public TextComponent getDescription() {
			return Component.text("");
		}
	}
}