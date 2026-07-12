package fr.ludos.core.group;

import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.config.ConfigMap;
import fr.ludos.core.config.ConfigOptions;
import fr.ludos.core.config.EnumConfigOptions;
import fr.ludos.core.config.NumberConfigOption;
import fr.ludos.core.game.teamController.GameJoinOption;
import fr.ludos.core.lobby.LobbyWaitPlayersOption;

public final class GroupConfigMap extends ConfigMap {
	public static final String membersAuthOptionsKey = "member_authorisation";
	public static final EnumConfigOptions<GroupRightsOption> membersAuthOptions = new EnumConfigOptions<>("Members authorisation", GroupRightsOption.class, GroupRightsOption.invite);

	public static final String groupJoinOptionsKey = "group_join";
	public static final EnumConfigOptions<GroupJoinOption> groupJoinOptions = new EnumConfigOptions<>("Group join behaviour", GroupJoinOption.class, GroupJoinOption.auto_accept);

	public static final String gameJoinOptionsKey = "game_join";
	public static final EnumConfigOptions<GameJoinOption> gameJoinOptions = new EnumConfigOptions<>("Member Game join behaviour", GameJoinOption.class, GameJoinOption.auto);

	public static final String waitPlayersOptionsKey = "wait_players";
	public static final EnumConfigOptions<LobbyWaitPlayersOption> waitPlayersOptions = new EnumConfigOptions<>("Players to wait in lobby", LobbyWaitPlayersOption.class, LobbyWaitPlayersOption.all);

	public static final String startDelayOptionsKey = "start_delay";
	public static final NumberConfigOption startDelayOptions = new NumberConfigOption("Lobby start delay seconds", Set.of(5, 10, 30), 10, true);

	public static final GroupConfigMap instance = new GroupConfigMap("group");

	private GroupConfigMap(String namespace) {
		super(namespace);
	}

	@Override
	public @NotNull Set<@NotNull String> getValues() {
		return Set.of(membersAuthOptionsKey, groupJoinOptionsKey, gameJoinOptionsKey, waitPlayersOptionsKey, startDelayOptionsKey);
	}

	@Override
	public ConfigOptions<?> getOptions(String name) {
		switch (name) {
			case membersAuthOptionsKey: return membersAuthOptions;
			case groupJoinOptionsKey: return groupJoinOptions;
			case gameJoinOptionsKey: return gameJoinOptions;
			case waitPlayersOptionsKey: return waitPlayersOptions;
			case startDelayOptionsKey: return startDelayOptions;
			default: return null;
		}
	}

	public GroupRightsOption getMembersAuth(ConfigurationSection container) {
		return getTypedOptionValue(membersAuthOptionsKey, membersAuthOptions, container);
	}
	public GroupJoinOption getGroupJoinMode(ConfigurationSection container) {
		return getTypedOptionValue(groupJoinOptionsKey, groupJoinOptions, container);
	}
	public GameJoinOption getGameJoinMode(ConfigurationSection container) {
		return getTypedOptionValue(gameJoinOptionsKey, gameJoinOptions, container);
	}
	public LobbyWaitPlayersOption getLobbyWaitPlayers(ConfigurationSection container) {
		return getTypedOptionValue(waitPlayersOptionsKey, waitPlayersOptions, container);
	}
	public int getStartDelaySeconds(ConfigurationSection container) {
		return getTypedOptionValue(startDelayOptionsKey, startDelayOptions, container);
	}
}