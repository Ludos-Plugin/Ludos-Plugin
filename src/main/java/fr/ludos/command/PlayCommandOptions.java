package fr.ludos.command;

public enum PlayCommandOptions {
    config ("config"),
    start ("start"),
    stop ("stop");

    private String name;

    private PlayCommandOptions(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }
}