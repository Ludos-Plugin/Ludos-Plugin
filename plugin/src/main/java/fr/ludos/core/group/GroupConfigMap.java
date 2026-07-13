package fr.ludos.core.group;

import java.util.Set;

import fr.ludos.core.config.ConfigOptionsMap;
import fr.ludos.core.config.EnumConfigOptions;
import fr.ludos.core.config.NumberConfigOptions;
import fr.ludos.core.config.ValueConfigOptions;
import fr.ludos.core.game.teamController.GameJoinOption;
import fr.ludos.core.lobby.LobbyWaitPlayersOption;

public final class GroupConfigMap extends ConfigOptionsMap {
	public static final ValueConfigOptions<GroupRightsOption> membersAuth =
		new EnumConfigOptions<>("Members authorisation", "member_authorisation", null, GroupRightsOption.class, GroupRightsOption.invite);

	public static final ValueConfigOptions<GroupJoinOption> groupJoin =
		new EnumConfigOptions<>("Group join behaviour", "group_join", null, GroupJoinOption.class, GroupJoinOption.auto_accept);

	public static final ValueConfigOptions<GameJoinOption> gameJoin =
		new EnumConfigOptions<>("Member Game join behaviour", "game_join", null, GameJoinOption.class, GameJoinOption.auto);

	public static final ValueConfigOptions<LobbyWaitPlayersOption> waitPlayers =
		new EnumConfigOptions<>("Players to wait in lobby", "wait_players", null, LobbyWaitPlayersOption.class, LobbyWaitPlayersOption.all);

	public static final ValueConfigOptions<Integer> startDelay =
		new NumberConfigOptions("Lobby start delay seconds", "start_delay", null, 10, Set.of(5, 10, 30), true);

	public static final GroupConfigMap instance = new GroupConfigMap();

	private GroupConfigMap() {
		super("group", Set.of(membersAuth, groupJoin, gameJoin, waitPlayers, startDelay));
	}
}