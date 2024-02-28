package fr.ludos.role;

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
		public NecromancerRole build(String gameId) {
			return new NecromancerRole(this);
		}
	}
}
