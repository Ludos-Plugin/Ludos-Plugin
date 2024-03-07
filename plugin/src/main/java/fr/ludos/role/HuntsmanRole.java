package fr.ludos.role;

// import fr.ludos.item.huntsman.crossbow.HuntsmanCrossbow;
// import fr.ludos.item.huntsman.spear.HuntsmanSpear;
import fr.ludos.Main;
import fr.ludos.item.huntsman.HuntsmanBow;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;


public class HuntsmanRole extends Role {

	private final HuntsmanBow.Events bowEvents;
	// private final HuntsmanCrossbow.Events crossbowEvents;
	// private final HuntsmanSpear.Events spearEvents;

	public static final String id = "huntsman";


	public HuntsmanRole(Builder builder) {
		super(builder);
		PluginManager manager = Bukkit.getPluginManager();

		bowEvents = new HuntsmanBow.Events();
		manager.registerEvents((Listener)bowEvents, Main.getInstance());

		// crossbowEvents = new HuntsmanCrossbow.Events();
		// manager.registerEvents((Listener)crossbowEvents, Main.getInstance());

		// spearEvents = new HuntsmanSpear.Events();
		// manager.registerEvents((Listener)spearEvents, Main.getInstance());
	}

	@Override
	public void stop() {
		super.stop();
		HandlerList.unregisterAll(bowEvents);
		// HandlerList.unregisterAll(crossbowEvents);
		// HandlerList.unregisterAll(spearEvents);
	}


	// @EventHandler
	// public void upgradeStuff(Player player) {
	//     if (player.getTotalExperience() >= 100/*  && ! HuntsmanCrossbow.playerOwns(player) */) {
	//         // HuntsmanCrossbow.createNew(player);
	//     }

	//     if (player.getTotalExperience() >= 300/*  && ! HuntsmanSpear.playerOwns(player) */) {
	//         // HuntsmanSpear.createNew(player);
	//     }
	// }


	public static class Builder extends Role.Builder {

		@Override
		public String getId() {
			return id;
		}

		@Override
		public HuntsmanRole build(String gameId) {
			return new HuntsmanRole(this);
		}
	}
}