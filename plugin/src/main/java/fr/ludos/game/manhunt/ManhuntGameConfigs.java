package fr.ludos.game.manhunt;

public enum ManhuntGameConfigs {
	players ("players"),
	prey ("prey"),
	area ("area"),
	location ("location"),
	reveal ("reveal");

	private String name;

	private ManhuntGameConfigs(String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}
}