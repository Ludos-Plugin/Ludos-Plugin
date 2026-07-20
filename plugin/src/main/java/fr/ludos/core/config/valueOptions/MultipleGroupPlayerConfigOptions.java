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
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.group.Group;
import fr.ludos.core.group.GroupManager;

/**
 * {@link ValueConfigOptions} for multiple {@link OfflinePlayer} instances, present in the {@link CommandSender}'s current {@link Group}.
 */
public final class MultipleGroupPlayerConfigOptions extends SetConfigOptions<OfflinePlayer> {
	private final GroupManager groupManager;
	private final @Nullable Integer limit;
	private final boolean excludeSelf;

	public MultipleGroupPlayerConfigOptions(GroupManager groupManager, @NotNull String name, @NotNull String key, @Nullable Integer limit, @Nullable String emptyValue, boolean excludeSelf) {
		super(name, key, emptyValue);
		this.groupManager = Objects.requireNonNull(groupManager);
		this.limit = limit;
		this.excludeSelf = excludeSelf;
	}
	public MultipleGroupPlayerConfigOptions(GroupManager groupManager, @NotNull String name, @NotNull String key, @Nullable Integer limit, @Nullable String emptyValue) {
		this(groupManager, name, key, limit, emptyValue, false);
	}
	public MultipleGroupPlayerConfigOptions(GroupManager groupManager, @NotNull String name, @NotNull String key, @Nullable Integer limit, boolean excludeSelf) {
		this(groupManager, name, key, limit, null, excludeSelf);
	}
	public MultipleGroupPlayerConfigOptions(GroupManager groupManager, @NotNull String name, @NotNull String key, @Nullable Integer limit) {
		this(groupManager, name, key, limit, null);
	}
	public MultipleGroupPlayerConfigOptions(GroupManager groupManager, @NotNull String name, @NotNull String key, @Nullable String emptyValue, boolean excludeSelf) {
		this(groupManager, name, key, null, emptyValue, excludeSelf);
	}
	public MultipleGroupPlayerConfigOptions(GroupManager groupManager, @NotNull String name, @NotNull String key, @Nullable String emptyValue) {
		this(groupManager, name, key, null, emptyValue);
	}
	public MultipleGroupPlayerConfigOptions(GroupManager groupManager, @NotNull String name, @NotNull String key, boolean excludeSelf) {
		this(groupManager, name, key, null, null, excludeSelf);
	}
	public MultipleGroupPlayerConfigOptions(GroupManager groupManager, @NotNull String name, @NotNull String key) {
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
	public OfflinePlayer parseSingleValueFromArg(@NotNull String arg) {
		return Bukkit.getOfflinePlayer(arg);
	}
	@Override
	public boolean validateParsedValueFromArg(OfflinePlayer argValue, CommandSender sender) {
		if (! (sender instanceof Player player)) return false;

		Group group = groupManager.getGroupOfPlayer(player);
		if (group == null) return false;

		return group.isPlayer(argValue);
	}
	@Override
	public String parseSingleValueToString(OfflinePlayer value) {
		return value.getName();
	}
}
