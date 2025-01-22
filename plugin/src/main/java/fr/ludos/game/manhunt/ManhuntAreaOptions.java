package fr.ludos.game.manhunt;

public enum ManhuntAreaOptions {
	large (500),
	medium (200),
	small (100);

	private int size;
	public int getSize() {
		return size;
	}

	private ManhuntAreaOptions(int size) {
		this.size = size;
	}
}