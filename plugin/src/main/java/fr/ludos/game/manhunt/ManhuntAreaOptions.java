package fr.ludos.game.manhunt;

public enum ManhuntAreaOptions {
	large (350),
	medium (250),
	small (150);

	private int size;
	public int getSize() {
		return size;
	}

	private ManhuntAreaOptions(int size) {
		this.size = size;
	}
}