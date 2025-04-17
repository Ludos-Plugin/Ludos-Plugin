package fr.ludos.game.manhunt;

public enum ManhuntRevealOptions {
	often (100),
	occasional (180),
	rare (360);

	private int duration;
	public int getDuration() {
		return duration;
	}

	private ManhuntRevealOptions(int duration) {
		this.duration = duration;
	}
}