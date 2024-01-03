package fr.ludos.command;

public enum RoleCommandOptions {
    get ("get"),
    reset ("reset"),
    set ("set");

    private String name;

    private RoleCommandOptions(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }
}