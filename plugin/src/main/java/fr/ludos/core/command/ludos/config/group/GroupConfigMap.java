package fr.ludos.core.command.ludos.config.group;

import java.util.List;
import java.util.Set;

import org.bukkit.Material;

import fr.ludos.core.game.teamController.GameJoinOption;
import fr.ludos.core.group.Group;
import fr.ludos.core.group.GroupJoinOption;
import fr.ludos.core.group.GroupRightsOption;
import fr.ludos.core.lobby.LobbyWaitPlayersOption;
import fr.ludos.core.persistence.config.ConfigNodeMap;
import fr.ludos.core.persistence.config.valueEntry.EnumConfigEntry;
import fr.ludos.core.persistence.config.valueEntry.IntegerConfigEntry;
import net.kyori.adventure.text.Component;
import xyz.xenondevs.invui.item.builder.ItemBuilder;

/**
 * {@link ConfigNodeMap} for Group-specific configuration.
 */
public final class GroupConfigMap extends ConfigNodeMap {
	public static final EnumConfigEntry<GroupRightsOption> MEMBERS_AUTH =
		new EnumConfigEntry<>(
			Component.text("Members authorisation"),
			new ItemBuilder(Material.SHIELD),
			"member_authorisation",
			GroupRightsOption.class, GroupRightsOption.invite
		) {
			public ItemBuilder getItemForEnumValue(GroupRightsOption value) {
				return value.getDisplayItem();
			}
		};

	public static final EnumConfigEntry<GroupJoinOption> GROUP_JOIN =
		new EnumConfigEntry<>(
			Component.text("Players can join the group"),
			new ItemBuilder(Material.BLUE_BANNER),
			"group_join",
			GroupJoinOption.class, GroupJoinOption.manual_accept
		) {
			@Override
			public ItemBuilder getItemForEnumValue(GroupJoinOption value) {
				return value.getDisplayItem();
			}
		};

	public static final EnumConfigEntry<GameJoinOption> GAME_JOIN =
		new EnumConfigEntry<>(
			Component.text("Member can join ongoing Games"),
			new ItemBuilder(Material.BLUE_BED),
			"game_join",
			GameJoinOption.class, GameJoinOption.yes
		) {
			@Override
			public ItemBuilder getItemForEnumValue(GameJoinOption value) {
				return value.getDisplayItem();
			}
		};

	public static final EnumConfigEntry<LobbyWaitPlayersOption> WAIT_PLAYERS =
		new EnumConfigEntry<>(
			Component.text("Players to wait in lobby"),
			new ItemBuilder(Material.PLAYER_HEAD),
			"wait_players",
			LobbyWaitPlayersOption.class, LobbyWaitPlayersOption.all
		) {
			@Override
			public ItemBuilder getItemForEnumValue(LobbyWaitPlayersOption value) {
				return value.getDisplayItem();
			}
		};

	public static final IntegerConfigEntry START_DELAY =
		new IntegerConfigEntry(
			Component.text("Lobby start delay seconds"),
			new ItemBuilder(Material.CLOCK),
			"start_delay",
			10, Set.of(5, 10, 30),
			true
		) {
			@Override
			protected String getValueLabel(String value) {
				return super.getValueLabel(value) + 's';
			}
		};

	public static final GroupConfigMap INSTANCE = new GroupConfigMap();

	private GroupConfigMap() {
		super(
			Component.text("Group Configuration"),
			new ItemBuilder(Material.BLUE_BANNER),
			Group.NAMESPACE,
			List.of(
				MEMBERS_AUTH,
				GROUP_JOIN,
				GAME_JOIN,
				WAIT_PLAYERS,
				START_DELAY
			)
		);
	}
}