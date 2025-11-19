package fr.ludos.game.sheepwars;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum SheepwarsTeamOptions {
	two_teams ("2", 2),
	three_teams ("3", 3),
	four_teams ("4", 4);

	private String name;
	public String toString() {
		return name;
	}

	private int teamCount;
	public int getTeamCount() {
		return teamCount;
	}

	private SheepwarsTeamOptions(String name, int teamCount) {
		this.name = name;
		this.teamCount = teamCount;
	}

	public static String getUsage() {
		StringBuilder sb = new StringBuilder();

		sb.append("<");
		sb.append(
			Arrays.stream(SheepwarsTeamOptions.values()).map(SheepwarsTeamOptions::toString)
				.collect(Collectors.joining( " | "))
		);
		sb.append(">");

		return sb.toString();
	}
}