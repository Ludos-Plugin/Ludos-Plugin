package fr.ludos.core.command.ludos.config.group;

import java.util.Set;

import fr.ludos.core.game.teamController.GameJoinOption;
import fr.ludos.core.group.Group;
import fr.ludos.core.group.GroupJoinOption;
import fr.ludos.core.group.GroupRightsOption;
import fr.ludos.core.lobby.LobbyWaitPlayersOption;
import fr.ludos.core.persistence.config.ConfigEntriesMap;
import fr.ludos.core.persistence.config.valueEntry.EnumConfigEntry;
import fr.ludos.core.persistence.config.valueEntry.IntegerConfigEntry;

/**
 * {@link ConfigEntriesMap} for Group-specific configuration.
 */
public final class GroupConfigMap extends ConfigEntriesMap {
	public static final EnumConfigEntry<GroupRightsOption> MEMBERS_AUTH =
		new EnumConfigEntry<>("Members authorisation", "member_authorisation", null, GroupRightsOption.class, GroupRightsOption.invite);

	public static final EnumConfigEntry<GroupJoinOption> GROUP_JOIN =
		new EnumConfigEntry<>("Group join behaviour", "group_join", null, GroupJoinOption.class, GroupJoinOption.manual_accept);

	public static final EnumConfigEntry<GameJoinOption> GAME_JOIN =
		new EnumConfigEntry<>("Member Game join behaviour", "game_join", null, GameJoinOption.class, GameJoinOption.auto);

	public static final EnumConfigEntry<LobbyWaitPlayersOption> WAIT_PLAYERS =
		new EnumConfigEntry<>("Players to wait in lobby", "wait_players", null, LobbyWaitPlayersOption.class, LobbyWaitPlayersOption.all);

	public static final IntegerConfigEntry START_DELAY =
		new IntegerConfigEntry("Lobby start delay seconds", "start_delay", null, 10, Set.of(5, 10, 30), true);

	public static final GroupConfigMap INSTANCE = new GroupConfigMap();

	private GroupConfigMap() {
		super(Group.NAMESPACE, Set.of(MEMBERS_AUTH, GROUP_JOIN, GAME_JOIN, WAIT_PLAYERS, START_DELAY));
	}
}