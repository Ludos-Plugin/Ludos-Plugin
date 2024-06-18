package fr.ludos.role;

// import fr.ludos.item.huntsman.crossbow.HuntsmanCrossbow;
// import fr.ludos.item.huntsman.spear.HuntsmanSpear;
import fr.ludos.game.Game;
// import fr.ludos.command.SetBowLevelCommand;
import fr.ludos.item.huntsman.HuntsmanBow;
// import fr.ludos.item.huntsman.HuntsmanLevelSelector;


public class HuntsmanRole extends Role {

	private final HuntsmanBow.Events bowEvents;
	// private final HuntsmanLevelSelector.Events bowGrimoireEvents;
	// private final HuntsmanCrossbow.Events crossbowEvents;
	// private final HuntsmanSpear.Events spearEvents;

	public static final String id = "huntsman";


	public HuntsmanRole(Builder builder) {
		super(builder);

		bowEvents = new HuntsmanBow.Events();
		// bowGrimoireEvents = new HuntsmanLevelSelector.Events();
		// crossbowEvents = new HuntsmanCrossbow.Events();
		// spearEvents = new HuntsmanTrident.Events();


		// PluginCommand cmd = Main.getInstance().getCommand("setbowbranch");
		// SetBowLevelCommand command = new SetBowLevelCommand();
		// cmd.setExecutor(command);
	}

	@Override
	public void stop() {
		super.stop();
		bowEvents.stop();
		// bowGrimoireEvents.stop();
		// HandlerList.unregisterAll(crossbowEvents);
		// HandlerList.unregisterAll(spearEvents);
	}


	public static class Builder extends Role.Builder {

		@Override
		public String getId() {
			return id;
		}

		@Override
		public HuntsmanRole build(Game.Builder builder) {
			return new HuntsmanRole(this);
		}
	}
}