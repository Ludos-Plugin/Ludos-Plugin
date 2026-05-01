package fr.ludos.game.lobbyController;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import fr.ludos.group.Group;

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