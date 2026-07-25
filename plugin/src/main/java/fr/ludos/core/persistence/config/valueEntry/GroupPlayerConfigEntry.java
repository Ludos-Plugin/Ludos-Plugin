package fr.ludos.core.persistence.config.valueEntry;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.group.Group;
import fr.ludos.core.group.GroupManager;
import fr.ludos.core.persistence.serializer.PlayerSerializer;
import fr.ludos.core.persistence.serializer.Serializer;

/**
 * {@link ConfigEntry} for a single {@link OfflinePlayer} instance, present in the {@link CommandSender}'s current {@link Group}.
 */
public class GroupPlayerConfigEntry extends ConfigEntry<OfflinePlayer, String> {
	private final GroupManager groupManager;
	private final boolean excludeSelf;

	public GroupPlayerConfigEntry(GroupManager groupManager, @NotNull String name, @NotNull String key, boolean excludeSelf) {
		super(name, key);
		this.groupManager = Objects.requireNonNull(groupManager);
		this.excludeSelf = excludeSelf;
	}
	public GroupPlayerConfigEntry(GroupManager groupManager, @NotNull String name, @NotNull String key) {
		this(groupManager, name, key, false);
	}

	@Override
	public OfflinePlayer getDefaultValue() {
		return null;
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
	public boolean validateValue(OfflinePlayer value, CommandSender sender) {
		if (! (sender instanceof Player player )) return false;

		Group group = groupManager.getGroupOfPlayer(player);
		return (group.isPlayer(value));
	}
	@Override
	protected Serializer<OfflinePlayer, String> getSerializer() {
		return PlayerSerializer.INSTANCE;
	}
}
