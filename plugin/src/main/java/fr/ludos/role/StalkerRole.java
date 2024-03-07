package fr.ludos.role;

public class StalkerRole extends Role {

	public StalkerRole(Builder builder) {
		super(builder);
	}


	public static class Builder extends Role.Builder {

		@Override
		public String getId() {
			return "stalker";
		}

		@Override
		public StalkerRole build(String gameId) {
			return new StalkerRole(this);
		}
	}
}