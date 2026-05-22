package fr.ludos.game.lobbyController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum LobbyStartDelayOption {
	five_seconds (5),
	ten_seconds (10),
	thirty_seconds (30);

	private final int duration;
	public int getDuration() {
		return duration;
	}

	private LobbyStartDelayOption(int duration) {
		this.duration = duration;
	}


	public static List<String> getOptions() {
		return Arrays.stream(LobbyStartDelayOption.values())
			.map(LobbyStartDelayOption::toString)
			.collect(Collectors.toList());
	}

	public static String getUsage() {
		StringBuilder sb = new StringBuilder();

		sb.append("<");
		sb.append(
			Arrays.stream(LobbyStartDelayOption.values()).map(LobbyStartDelayOption::toString)
				.collect(Collectors.joining( " | "))
		);
		sb.append(">");

		return sb.toString();
	}
}