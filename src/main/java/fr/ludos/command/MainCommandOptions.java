package fr.ludos.command;

public enum MainCommandOptions {
    config ("config"),
    start ("start");

    private String name;

    private MainCommandOptions(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }
}