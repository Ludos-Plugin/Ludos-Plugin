package fr.ludos.core.command.ludos.config.group;

import java.util.Set;

import fr.ludos.core.config.ConfigOptionsMap;
import fr.ludos.core.config.valueOptions.EnumConfigOptions;
import fr.ludos.core.config.valueOptions.NumberConfigOptions;
import fr.ludos.core.config.valueOptions.ValueConfigOptions;
import fr.ludos.core.game.teamController.GameJoinOption;
import fr.ludos.core.group.GroupJoinOption;
import fr.ludos.core.group.GroupRightsOption;
import fr.ludos.core.lobby.LobbyWaitPlayersOption;

public final class GroupConfigMap extends ConfigOptionsMap {
	public static final ValueConfigOptions<GroupRightsOption> MEMBERS_AUTH =
		new EnumConfigOptions<>("Members authorisation", "member_authorisation", null, GroupRightsOption.class, GroupRightsOption.invite);

	public static final ValueConfigOptions<GroupJoinOption> GROUP_JOIN =
		new EnumConfigOptions<>("Group join behaviour", "group_join", null, GroupJoinOption.class, GroupJoinOption.auto_accept);

	public static final ValueConfigOptions<GameJoinOption> GAME_JOIN =
		new EnumConfigOptions<>("Member Game join behaviour", "game_join", null, GameJoinOption.class, GameJoinOption.auto);

	public static final ValueConfigOptions<LobbyWaitPlayersOption> WAIT_PLAYERS =
		new EnumConfigOptions<>("Players to wait in lobby", "wait_players", null, LobbyWaitPlayersOption.class, LobbyWaitPlayersOption.all);

	public static final ValueConfigOptions<Integer> START_DELAY =
		new NumberConfigOptions("Lobby start delay seconds", "start_delay", null, 10, Set.of(5, 10, 30), true);

	public static final GroupConfigMap INSTANCE = new GroupConfigMap();

	private GroupConfigMap() {
		super("group", Set.of(MEMBERS_AUTH, GROUP_JOIN, GAME_JOIN, WAIT_PLAYERS, START_DELAY));
	}
}