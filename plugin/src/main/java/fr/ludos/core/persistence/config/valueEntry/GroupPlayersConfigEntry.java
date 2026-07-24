package fr.ludos.core.persistence.config.valueEntry;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.group.Group;
import fr.ludos.core.group.GroupManager;
import fr.ludos.core.persistence.serializer.PlayerSerializer;
import fr.ludos.core.persistence.serializer.StringSetSerializer;

/**
 * {@link ValueConfigEntry} for multiple {@link OfflinePlayer} instances, present in the {@link CommandSender}'s current {@link Group}.
 */
public final class GroupPlayersConfigEntry extends SetConfigEntry<OfflinePlayer> {
	private final GroupManager groupManager;
	private final @Nullable Integer limit;
	private final boolean excludeSelf;

	public GroupPlayersConfigEntry(GroupManager groupManager, @NotNull String name, @NotNull String key, @Nullable Integer limit, @Nullable String emptyValue, boolean excludeSelf) {
		super(name, key, new StringSetSerializer<>(PlayerSerializer.INSTANCE), emptyValue);
		this.groupManager = Objects.requireNonNull(groupManager);
		this.limit = limit;
		this.excludeSelf = excludeSelf;
	}
	public GroupPlayersConfigEntry(GroupManager groupManager, @NotNull String name, @NotNull String key, @Nullable Integer limit, @Nullable String emptyValue) {
		this(groupManager, name, key, limit, emptyValue, false);
	}
	public GroupPlayersConfigEntry(GroupManager groupManager, @NotNull String name, @NotNull String key, @Nullable Integer limit, boolean excludeSelf) {
		this(groupManager, name, key, limit, null, excludeSelf);
	}
	public GroupPlayersConfigEntry(GroupManager groupManager, @NotNull String name, @NotNull String key, @Nullable Integer limit) {
		this(groupManager, name, key, limit, null);
	}
	public GroupPlayersConfigEntry(GroupManager groupManager, @NotNull String name, @NotNull String key, @Nullable String emptyValue, boolean excludeSelf) {
		this(groupManager, name, key, null, emptyValue, excludeSelf);
	}
	public GroupPlayersConfigEntry(GroupManager groupManager, @NotNull String name, @NotNull String key, @Nullable String emptyValue) {
		this(groupManager, name, key, null, emptyValue);
	}
	public GroupPlayersConfigEntry(GroupManager groupManager, @NotNull String name, @NotNull String key, boolean excludeSelf) {
		this(groupManager, name, key, null, null, excludeSelf);
	}
	public GroupPlayersConfigEntry(GroupManager groupManager, @NotNull String name, @NotNull String key) {
		this(groupManager, name, key, null, null);
	}

	@Override
	public Set<String> getValidOptions(CommandSender sender) {
		if (! (sender instanceof Player player )) return Collections.emptySet();

		Group group = groupManager.getGroupOfPlayer(player);
		if (group == null) return Collections.emptySet();

		Set<String> res = group.getPlayers().stream()
			.map(OfflinePlayer::getName)
			.collect(Collectors.toCollection(HashSet<String>::new));

		if (excludeSelf) {
			res.remove(player.getName());
		}

		return res;
	}

	@Override
	public boolean validateValue(OfflinePlayer value, CommandSender sender) {
		if (! (sender instanceof Player player )) return false;

		Group group = groupManager.getGroupOfPlayer(player);
		if (group == null) return false;

		boolean res = group.isPlayer(value);
		return res;
	}
}
