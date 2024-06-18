package fr.ludos.game.manhunt;

public enum ManhuntAreaOptions {
	large ("large", 500),
	medium ("medium", 200),
	small ("small", 100);

	private String name;
	public String getName() {
		return name;
	}

	private int size;
	public int getSize() {
		return size;
	}

	private ManhuntAreaOptions(String name, int size) {
		this.name = name;
		this.size = size;
	}
}