package fr.ludos.core.config.valueOptions;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.group.Group;
import fr.ludos.core.group.GroupManager;

/**
 * {@link ValueConfigOptions} for a single {@link OfflinePlayer} instance, present in the {@link CommandSender}'s current {@link Group}.
 */
public final class SingleGroupPlayerConfigOptions extends ValueConfigOptions<OfflinePlayer> {
	private final GroupManager groupManager;
	private final boolean excludeSelf;

	public SingleGroupPlayerConfigOptions(GroupManager groupManager, @NotNull String name, @NotNull String key, @Nullable String emptyValue, boolean excludeSelf) {
		super(name, key, emptyValue);
		this.groupManager = Objects.requireNonNull(groupManager);
		this.excludeSelf = excludeSelf;
	}
	public SingleGroupPlayerConfigOptions(GroupManager groupManager, @NotNull String name, @NotNull String key, @Nullable String emptyValue) {
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
	public OfflinePlayer getValueOrNull(ConfigurationSection config) {
		return config.getOfflinePlayer(key());
	}

	@Override
	public OfflinePlayer fromString(String value) {
		if (value == null) return null;
		return Bukkit.getOfflinePlayer(value).getPlayer();
	}
	@Override
	public String toString(OfflinePlayer value) {
		if (value == null) return null;
		return value.getName();
	}
}
