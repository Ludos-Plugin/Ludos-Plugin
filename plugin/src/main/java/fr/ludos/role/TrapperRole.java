package fr.ludos.role;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import fr.ludos.Ludos;
import fr.ludos.game.Game;
import fr.ludos.item.SpecialItem;
import fr.ludos.item.trapper.TrapperSnareDevice;
import fr.ludos.item.trapper.TrapperDagger;


public class TrapperRole extends Role {
	public static final String id = "trapper";

	private static final ArrayList<Player> sneakingPlayers = new ArrayList<>();

	private BukkitTask invisibilityTask;


	public TrapperRole(Builder builder, Game game) {
		super(builder, game);
	}

	@Override
	protected void onStart() {
		invisibilityTask = new BukkitRunnable() {
			@Override
			public void run() {
				for (Player sneakingPlayer : sneakingPlayers) {
					sneakingPlayer.addPotionEffect(PotionEffectType.INVISIBILITY.createEffect(10,0));
				}
			}
		}.runTaskTimer(getGame().getPlugin(), 0, 1);
	}

	@Override
	protected void onStop() {
		sneakingPlayers.clear();

		if (invisibilityTask != null) {
			invisibilityTask.cancel();
		}
		invisibilityTask = null;
	}

	@Override
	protected LinkedHashMap<String, SpecialItem.Events<?>> createItemEvents(Role.Builder builder, Game game) {
		switch (builder.getId()) {
			default:
				return new LinkedHashMap<>() {{
					put("snare", new TrapperSnareDevice.Events(game));
					put("dagger", new TrapperDagger.Events(game));
				}};
		}
	}



	@EventHandler
	public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
		if (! Role.isPlayerRole(event.getPlayer(), id)) {
			return;
		}

		if (event.isSneaking() && (event.getPlayer().getVelocity().getY() <= 0)) {
			sneakingPlayers.add(event.getPlayer());
		} else {
			sneakingPlayers.remove(event.getPlayer());
		}
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
			return new TrapperRole(this, game);
		}

		@Override
		public TextComponent getDisplayName() {
			return Component.text("Trapper");
		}

		@Override
		public TextComponent getDescription() {
			return Component.text("");
		}
	}
}

