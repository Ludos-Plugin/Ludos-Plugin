package fr.ludos.game.manhunt;

public enum ManhuntRevealOptions {
	frequent ("frequent", 40),
	medium ("medium", 180),
	infrequent ("infrequent", 360);

	private String name;
	public String getName() {
		return name;
	}

	private int duration;
	public int getDuration() {
		return duration;
	}

	private ManhuntRevealOptions(String name, int duration) {
		this.name = name;
		this.duration = duration;
	}
}