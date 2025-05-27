package fr.ludos.command.ludos;

public enum RoleOptions {
	set ("set"),
	config ("config"),
	help ("help");

	private String name;
	public String toString() {
		return name;
	}


	private RoleOptions(String name) {
		this.name = name;
	}
}