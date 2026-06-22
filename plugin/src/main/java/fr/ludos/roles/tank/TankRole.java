package fr.ludos.roles.tank;

import java.util.LinkedHashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.ludos.core.Ludos;
import fr.ludos.core.game.Game;
import fr.ludos.core.game.GameEvents;
import fr.ludos.core.role.Role;
import fr.ludos.roles.tank.items.TankDashObject;
import fr.ludos.roles.tank.items.TankHelmet;
import fr.ludos.roles.tank.items.TankShield;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

public class TankRole extends Role {
	public static final String id = "tank";

	public TankRole(Builder builder, Game game) {
		super(builder, game);
	}

	@Override
	protected LinkedHashMap<String, GameEvents> createGameEvents(Role.Builder builder, Game game) {
		return new LinkedHashMap<>() {
			{
				put("tankShield", new TankShield.Events(game));
				put("tankHelmet", new TankHelmet.Events(game));
				put("tankDasher", new TankDashObject.Events(game));
			}
		};
	}

	public static void setAttributePlayer(List<Player> players, PotionEffect AbsorbEffect,
			PotionEffect SlownessEffect) {
	}

	@Override
	protected void onRoleStart() {
		super.onRoleStart();

		List<Player> players = getGame().getGroup().getOnlinePlayers().stream()
			.filter(Role.ofRole(id))
			.toList();

		PotionEffect absorbEffect = new PotionEffect(
			PotionEffectType.ABSORPTION,
			Integer.MAX_VALUE,
			0,
			false,
			false
		);

		for (Player player : players) {
			player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(28.0);
			player.addPotionEffect(absorbEffect);
		}
	}

	@Override
	protected void onRoleStop() {
		super.onRoleStop();

		List<Player> players = getGame().getGroup().getOnlinePlayers().stream()
			.filter(Role.ofRole(id))
			.toList();

		for (Player player : players) {
			player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0);
			player.removePotionEffect(PotionEffectType.ABSORPTION);
		}
	}

	@EventHandler
	public void onPlayerHit(EntityDamageByEntityEvent event) {
		if (! (event.getDamager() instanceof Player attacker)) return;

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