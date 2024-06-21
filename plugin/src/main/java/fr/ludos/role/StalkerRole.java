package fr.ludos.role;

import fr.ludos.game.Game;

public class StalkerRole extends Role {
	public static final String id = "stalker";

	public StalkerRole(Builder builder, Game game) {
		super(builder, game);
	}


	public static class Builder extends Role.Builder {

		@Override
		public String getId() {
			return id;
		}

		@Override
		public StalkerRole build(Game.Builder builder, Game game) {
			return new StalkerRole(this, game);
		}
	}
}