package fr.ludos.core.group;

import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;

import fr.ludos.core.config.TypedConfigEntry;
import fr.ludos.core.config.ConfigHashMap;
import fr.ludos.core.config.EnumConfigOptions;
import fr.ludos.core.config.NumberConfigOption;
import fr.ludos.core.game.teamController.GameJoinOption;
import fr.ludos.core.lobby.LobbyWaitPlayersOption;

public final class GroupConfigMap extends ConfigHashMap {
	public static final TypedConfigEntry<GroupRightsOption> membersAuthEntry = new TypedConfigEntry<>(
		"member_authorisation",
		new EnumConfigOptions<>("Members authorisation", GroupRightsOption.class, GroupRightsOption.invite)
	);

	public static final TypedConfigEntry<GroupJoinOption> groupJoinEntry = new TypedConfigEntry<>(
		"group_join",
		new EnumConfigOptions<>("Group join behaviour", GroupJoinOption.class, GroupJoinOption.auto_accept)
	);

	public static final TypedConfigEntry<GameJoinOption> gameJoinEntry = new TypedConfigEntry<>(
		"game_join",
		new EnumConfigOptions<>("Member Game join behaviour", GameJoinOption.class, GameJoinOption.auto)
	);

	public static final TypedConfigEntry<LobbyWaitPlayersOption> waitPlayersEntry = new TypedConfigEntry<>(
		"wait_players",
		new EnumConfigOptions<>("Players to wait in lobby", LobbyWaitPlayersOption.class, LobbyWaitPlayersOption.all)
	);

	public static final TypedConfigEntry<Integer> startDelayEntry = new TypedConfigEntry<>(
		"start_delay",
		new NumberConfigOption("Lobby start delay seconds", 10, Set.of(5, 10, 30), true)
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