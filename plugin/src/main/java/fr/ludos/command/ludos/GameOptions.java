package fr.ludos.command.ludos;

public enum GameOptions {
	start ("start"),
	config ("config"),
	help ("help");

	private String name;
	public String toString() {
		return name;
	}


	private GameOptions(String name) {
		this.name = name;
	}
}