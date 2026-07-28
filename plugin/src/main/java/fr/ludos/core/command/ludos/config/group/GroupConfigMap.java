package fr.ludos.core.command.ludos.config.group;

import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import fr.ludos.core.game.teamController.GameJoinOption;
import fr.ludos.core.group.Group;
import fr.ludos.core.group.GroupJoinOption;
import fr.ludos.core.group.GroupRightsOption;
import fr.ludos.core.lobby.LobbyWaitPlayersOption;
import fr.ludos.core.persistence.config.ConfigNodeMap;
import fr.ludos.core.persistence.config.valueEntry.EnumConfigEntry;
import fr.ludos.core.persistence.config.valueEntry.IntegerConfigEntry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import xyz.xenondevs.invui.item.builder.AbstractItemBuilder;
import xyz.xenondevs.invui.item.builder.ItemBuilder;

/**
 * {@link ConfigNodeMap} for Group-specific configuration.
 */
public final class GroupConfigMap extends ConfigNodeMap {
	public static final EnumConfigEntry<GroupRightsOption> MEMBERS_AUTH =
		new EnumConfigEntry<>(
			Component.text("Members authorisation"),
			"member_authorisation",
			GroupRightsOption.class, GroupRightsOption.invite
		) {
			@Override
			public AbstractItemBuilder<?> createItem(Player player) {
				return new ItemBuilder(Material.SHIELD);
			}
			public ItemBuilder getItemForEnumValue(GroupRightsOption value) {
				return value.getDisplayItem();
			}
		};

	public static final EnumConfigEntry<GroupJoinOption> GROUP_JOIN =
		new EnumConfigEntry<>(
			Component.text("Players can join the group"),
			"group_join",
			GroupJoinOption.class, GroupJoinOption.manual_accept
		) {
			@Override
			public AbstractItemBuilder<?> createItem(Player player) {
				return new ItemBuilder(Material.BLUE_BANNER);
			}
			@Override
			public ItemBuilder getItemForEnumValue(GroupJoinOption value) {
				return value.getDisplayItem();
			}
		};

	public static final EnumConfigEntry<GameJoinOption> GAME_JOIN =
		new EnumConfigEntry<>(
			Component.text("Member can join ongoing Games"),
			"game_join",
			GameJoinOption.class, GameJoinOption.yes
		) {
			@Override
			public AbstractItemBuilder<?> createItem(Player player) {
				return new ItemBuilder(Material.BLUE_BED);
			}
			@Override
			public ItemBuilder getItemForEnumValue(GameJoinOption value) {
				return value.getDisplayItem();
			}
		};

	public static final EnumConfigEntry<LobbyWaitPlayersOption> WAIT_PLAYERS =
		new EnumConfigEntry<>(
			Component.text("Players to wait in lobby"),
			"wait_players",
			LobbyWaitPlayersOption.class, LobbyWaitPlayersOption.all
		) {
			@Override
			public AbstractItemBuilder<?> createItem(Player player) {
				return new ItemBuilder(Material.PLAYER_HEAD);
			}
			@Override
			public ItemBuilder getItemForEnumValue(LobbyWaitPlayersOption value) {
				return value.getDisplayItem();
			}
		};

	public static final IntegerConfigEntry START_DELAY =
		new IntegerConfigEntry(
			Component.text("Lobby start delay seconds"),
			"start_delay",
			10, Set.of(5, 10, 30),
			true
		) {
			@Override
			public AbstractItemBuilder<?> createItem(Player player) {
				return new ItemBuilder(Material.CLOCK);
			}
			@Override
			protected String getValueLabel(String value) {
				return super.getValueLabel(value) + 's';
			}
		};

	public static final TextComponent WINDOW_TITLE = Component.text("Group Configuration");
	public static final GroupConfigMap INSTANCE = new GroupConfigMap();

	private GroupConfigMap() {
		super(
			WINDOW_TITLE,
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

	@Override
	public AbstractItemBuilder<?> createItem(Player player) {
		return new ItemBuilder(Material.BLUE_BANNER);
	}
}