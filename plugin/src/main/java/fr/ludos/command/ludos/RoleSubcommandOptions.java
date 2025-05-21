package fr.ludos.command.ludos;

public enum RoleSubcommandOptions {
	get ("get"),
	reset ("reset"),
	set ("set"),
	guidebook ("guidebook"),
	help ("help");

	private String name;

	private RoleSubcommandOptions(String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}
}