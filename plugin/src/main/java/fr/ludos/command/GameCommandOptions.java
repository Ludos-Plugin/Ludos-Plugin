package fr.ludos.command;

public enum GameCommandOptions {
	config ("config"),
	start ("start"),
	stop ("stop");

	private String name;

	private GameCommandOptions(String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}
}