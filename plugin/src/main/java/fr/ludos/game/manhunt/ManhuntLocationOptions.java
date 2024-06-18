package fr.ludos.game.manhunt;

public enum ManhuntLocationOptions {
	here ("here"),
	random ("random");

	private String name;
	public String getName() {
		return name;
	}

	private ManhuntLocationOptions(String name) {
		this.name = name;
	}
}