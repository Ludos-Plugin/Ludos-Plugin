// package fr.ludos.role;

// import java.util.Collections;
// import java.util.Map;

// import fr.ludos.game.Game;
// import fr.ludos.item.SpecialItem;

// public class NecromancerRole extends Role {
// 	public static final String id = "necromancer";

// 	public NecromancerRole(Builder builder, Game game) {
// 		super(builder, game);
// 	}

// 	@Override
// 	protected Map<String, SpecialItem.Events<?>> createItemEvents(Role.Builder builder, Game game) {
// 		return Collections.emptyMap();
// 	}


// 	public static class Builder extends Role.Builder {

// 		@Override
// 		public String getId() {
// 			return id;
// 		}

// 		@Override
// 		public NecromancerRole build(Game.Builder builder, Game game) {
// 			return new NecromancerRole(this, game);
// 		}
// 	}
// }
