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
import fr.ludos.core.persistence.serializer.Serializer;

/**
 * {@link ValueConfigEntry} for a single {@link OfflinePlayer} instance, present in the {@link CommandSender}'s current {@link Group}.
 */
public final class GroupPlayerConfigEntry extends ValueConfigEntry<OfflinePlayer, String> {
	private final GroupManager groupManager;
	private final boolean excludeSelf;

	public GroupPlayerConfigEntry(GroupManager groupManager, @NotNull String name, @NotNull String key, @Nullable String emptyValue, boolean excludeSelf) {
		super(name, key, emptyValue);
		this.groupManager = Objects.requireNonNull(groupManager);
		this.excludeSelf = excludeSelf;
	}
	public GroupPlayerConfigEntry(GroupManager groupManager, @NotNull String name, @NotNull String key, @Nullable String emptyValue) {
		this(groupManager, name, key, emptyValue, false);
	}

	public String getterMessage(String value) {
		if (value == null) return placeholderValue();
		return value;
	}

	@Override
	public OfflinePlayer getDefaultValue() {
		return null;
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
	protected Serializer<OfflinePlayer, String> getSerializer() {
		return PlayerSerializer.INSTANCE;
	}
}
