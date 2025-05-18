package fr.ludos.command.ludos;

public enum LudosSubcommands {
	game ("game"),
	role ("role"),
	help ("help");

	private String name;

	private LudosSubcommands(String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}
}