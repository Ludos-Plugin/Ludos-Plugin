// package fr.ludos.role;

// import java.util.Collections;
// import java.util.Map;

// import fr.ludos.game.Game;
// import fr.ludos.item.SpecialItem.Events;

// public class StalkerRole extends Role {
// 	public static final String id = "stalker";

// 	public StalkerRole(Builder builder, Game game) {
// 		super(builder, game);
// 	}


// 	@Override
// 	protected Map<String, Events<?>> createItemEvents(Role.Builder builder, Game game) {
// 		return Collections.emptyMap();
// 	}


// 	public static class Builder extends Role.Builder {

// 		@Override
// 		public String getId() {
// 			return id;
// 		}

// 		@Override
// 		public StalkerRole build(Game.Builder builder, Game game) {
// 			return new StalkerRole(this, game);
// 		}
// 	}
// }