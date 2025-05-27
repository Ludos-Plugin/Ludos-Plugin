package fr.ludos.command.ludos;

public enum GameSubcommandOptions {
	stop ("stop"),
	help ("help");

	private String name;
	public String toString() {
		return name;
	}


	private GameSubcommandOptions(String name) {
		this.name = name;
	}
}