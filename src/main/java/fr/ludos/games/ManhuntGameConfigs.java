package fr.ludos.games;

public enum ManhuntGameConfigs {
    players ("players"),
    hunted ("hunted");

    private String name;

    private ManhuntGameConfigs(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }
}