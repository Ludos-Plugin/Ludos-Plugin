package fr.ludos.game.manhunt;

public enum ManhuntRevealOptions {
	frequent (40),
	regular (180),
	infrequent (360);

	private int duration;
	public int getDuration() {
		return duration;
	}

	private ManhuntRevealOptions(int duration) {
		this.duration = duration;
	}
}