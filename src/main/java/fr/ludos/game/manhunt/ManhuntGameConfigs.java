package fr.ludos.game.manhunt;

public enum ManhuntGameConfigs {
    players ("players"),
    prey ("prey");

    private String name;

    private ManhuntGameConfigs(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }
}