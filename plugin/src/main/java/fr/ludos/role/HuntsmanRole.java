package fr.ludos.role;

// import fr.ludos.item.huntsman.crossbow.HuntsmanCrossbow;
// import fr.ludos.item.huntsman.spear.HuntsmanSpear;
import fr.ludos.Main;
import fr.ludos.command.SetBowLevelCommand;
import fr.ludos.item.huntsman.HuntsmanBow;
import fr.ludos.item.huntsman.HuntsmanLevelSelector;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.command.PluginCommand;


public class HuntsmanRole extends Role {

	private final HuntsmanBow.Events bowEvents;
	private final HuntsmanLevelSelector.Events bowGrimoireEvents;
	// private final HuntsmanCrossbow.Events crossbowEvents;
	// private final HuntsmanSpear.Events spearEvents;

	public static final String id = "huntsman";


	public HuntsmanRole(Builder builder) {
		super(builder);
		PluginManager manager = Bukkit.getPluginManager();

		bowEvents = new HuntsmanBow.Events();
		manager.registerEvents((Listener)bowEvents, Main.getInstance());

		bowGrimoireEvents = new HuntsmanLevelSelector.Events();
		manager.registerEvents((Listener)bowGrimoireEvents, Main.getInstance());



		// crossbowEvents = new HuntsmanCrossbow.Events();
		// manager.registerEvents((Listener)crossbowEvents, Main.getInstance());

		// spearEvents = new HuntsmanTrident.Events();
		// manager.registerEvents((Listener)spearEvents, Main.getInstance());

		PluginCommand cmd = Main.getInstance().getCommand("setbowbranch");
		SetBowLevelCommand command = new SetBowLevelCommand();
		cmd.setExecutor(command);


		for (Player player : Role.getPlayersOfRole(id)) {
			bowEvents.updateItemInInventory(player);
			bowGrimoireEvents.updateItemInInventory(player);
		}
	}

	@Override
	public void stop() {
		super.stop();
		HandlerList.unregisterAll(bowEvents);
		// HandlerList.unregisterAll(crossbowEvents);
		// HandlerList.unregisterAll(spearEvents);

		// PluginCommand cmd = Main.getInstance().getCommand("setbowbranch");
		// cmd.unregister
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