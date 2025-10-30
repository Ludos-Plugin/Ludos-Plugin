package fr.ludos.command.ludos;

public enum GameSubcommandOptions {
	start ("start"),
	stop ("stop"),
	config ("config"),
	guidebook ("guidebook"),
	help ("help");

	private String name;
	public String toString() {
		return name;
	}


	private GameSubcommandOptions(String name) {
		this.name = name;
	}
}