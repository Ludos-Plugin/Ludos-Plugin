package fr.ludos.command.ludos;

public enum RoleSubcommandOptions {
	get ("get"),
	set ("set"),
	config ("config"),
	guidebook ("guidebook"),
	reset ("reset"),
	help ("help");

	private String name;

	private RoleSubcommandOptions(String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}
}