package fr.ludos.role;

import fr.ludos.game.Game;

public class NecromancerRole extends Role {

	public NecromancerRole(Builder builder) {
		super(builder);
	}


	public static class Builder extends Role.Builder {

		@Override
		public String getId() {
			return "necromancer";
		}

		@Override
		public NecromancerRole build(Game.Builder builder) {
			return new NecromancerRole(this);
		}
	}
}
