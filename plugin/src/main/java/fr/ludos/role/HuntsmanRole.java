package fr.ludos.role;

import fr.ludos.item.huntsman.bow.HuntsmanBowEvents;
// import fr.ludos.item.huntsman.crossbow.HuntsmanCrossbowEvents;
// import fr.ludos.item.huntsman.spear.HuntsmanSpearEvents;
import fr.ludos.Main;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;


public class HuntsmanRole extends Role {

	private final HuntsmanBowEvents bowEvents;
	// private final HuntsmanCrossbowEvents crossbowEvents;
	// private final HuntsmanSpearEvents spearEvents;

	public static final String id = "huntsman";


	public HuntsmanRole(Builder builder) {
		super(builder);
		PluginManager manager = Bukkit.getPluginManager();

		bowEvents = new HuntsmanBowEvents();
		manager.registerEvents((Listener)bowEvents, Main.getInstance());

		// bowEvents = new HuntsmanCrossbowEvents();
		// manager.registerEvents((Listener)bowEvents, Main.getInstance());

		// bowEvents = new HuntsmanSpearEvents();
		// manager.registerEvents((Listener)bowEvents, Main.getInstance());
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