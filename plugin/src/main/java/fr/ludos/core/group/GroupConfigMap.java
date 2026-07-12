package fr.ludos.core.group;

import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;

import fr.ludos.core.config.ConfigEntry;
import fr.ludos.core.config.ConfigHashMap;
import fr.ludos.core.config.EnumConfigOptions;
import fr.ludos.core.config.NumberConfigOption;
import fr.ludos.core.game.teamController.GameJoinOption;
import fr.ludos.core.lobby.LobbyWaitPlayersOption;

public final class GroupConfigMap extends ConfigHashMap {
	public static final ConfigEntry<GroupRightsOption> membersAuthEntry = new ConfigEntry<>(
		"member_authorisation",
		new EnumConfigOptions<>("Members authorisation", GroupRightsOption.class, GroupRightsOption.invite)
	);

	public static final ConfigEntry<GroupJoinOption> groupJoinEntry = new ConfigEntry<>(
		"group_join",
		new EnumConfigOptions<>("Group join behaviour", GroupJoinOption.class, GroupJoinOption.auto_accept)
	);

	public static final ConfigEntry<GameJoinOption> gameJoinEntry = new ConfigEntry<>(
		"game_join",
		new EnumConfigOptions<>("Member Game join behaviour", GameJoinOption.class, GameJoinOption.auto)
	);

	public static final ConfigEntry<LobbyWaitPlayersOption> waitPlayersEntry = new ConfigEntry<>(
		"wait_players",
		new EnumConfigOptions<>("Players to wait in lobby", LobbyWaitPlayersOption.class, LobbyWaitPlayersOption.all)
	);

	public static final ConfigEntry<Integer> startDelayEntry = new ConfigEntry<>(
		"start_delay",
		new NumberConfigOption("Lobby start delay seconds", Set.of(5, 10, 30), 10, true)
	);

	public static final GroupConfigMap instance = new GroupConfigMap("group");

	private GroupConfigMap(String namespace) {
		super(namespace, Set.of(membersAuthEntry, groupJoinEntry, gameJoinEntry, waitPlayersEntry, startDelayEntry));
	}

	public GroupRightsOption getMembersAuth(ConfigurationSection container) {
		return getTypedOptionValue(membersAuthEntry, container);
	}
	public GroupJoinOption getGroupJoinMode(ConfigurationSection container) {
		return getTypedOptionValue(groupJoinEntry, container);
	}
	public GameJoinOption getGameJoinMode(ConfigurationSection container) {
		return getTypedOptionValue(gameJoinEntry, container);
	}
	public LobbyWaitPlayersOption getLobbyWaitPlayers(ConfigurationSection container) {
		return getTypedOptionValue(waitPlayersEntry, container);
	}
	public int getStartDelaySeconds(ConfigurationSection container) {
		return getTypedOptionValue(startDelayEntry, container);
	}
}