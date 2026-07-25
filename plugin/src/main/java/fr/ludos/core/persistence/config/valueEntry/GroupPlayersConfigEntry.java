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
 * {@link ConfigEntry} for multiple {@link OfflinePlayer} instances, present in the {@link CommandSender}'s current {@link Group}.
 */
public class GroupPlayersConfigEntry extends SetConfigEntry<OfflinePlayer> {
	private final GroupManager groupManager;
	private final @Nullable Integer limit;
	private final boolean excludeSelf;

	public GroupPlayersConfigEntry(GroupManager groupManager, @NotNull String name, @NotNull String key, String placeholder, @Nullable Integer limit, boolean excludeSelf) {
		super(name, key, new StringSetSerializer<>(PlayerSerializer.INSTANCE), placeholder);
		this.groupManager = Objects.requireNonNull(groupManager);
		this.limit = limit;
		this.excludeSelf = excludeSelf;
	}
	public GroupPlayersConfigEntry(GroupManager groupManager, @NotNull String name, @NotNull String key, String placeholder, @Nullable Integer limit) {
		this(groupManager, name, key, placeholder, limit, false);
	}
	public GroupPlayersConfigEntry(GroupManager groupManager, @NotNull String name, @NotNull String key, String placeholder, boolean excludeSelf) {
		this(groupManager, name, key, placeholder, null, excludeSelf);
	}
	public GroupPlayersConfigEntry(GroupManager groupManager, @NotNull String name, @NotNull String key, String placeholder) {
		this(groupManager, name, key, placeholder, false);
	}

	@Override
	public Set<String> options(CommandSender sender) {
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
	public boolean validateSingleValue(OfflinePlayer value, CommandSender sender) {
		if (! (sender instanceof Player player )) return false;

		Group group = groupManager.getGroupOfPlayer(player);
		if (group == null) return false;

		boolean res = group.isPlayer(value);
		return res;
	}
}
