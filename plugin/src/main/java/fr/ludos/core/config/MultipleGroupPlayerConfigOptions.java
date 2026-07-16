package fr.ludos.core.config;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.group.Group;

public final class MultipleGroupPlayerConfigOptions extends SetConfigOptions<OfflinePlayer> {
	private final @Nullable Integer limit;
	private final boolean excludeSelf;

	public MultipleGroupPlayerConfigOptions(@NotNull String name, @NotNull String key, @Nullable Integer limit, @Nullable String emptyValue, boolean excludeSelf) {
		super(name, key, emptyValue);
		this.limit = limit;
		this.excludeSelf = excludeSelf;
	}
	public MultipleGroupPlayerConfigOptions(@NotNull String name, @NotNull String key, @Nullable Integer limit, @Nullable String emptyValue) {
		this(name, key, limit, emptyValue, false);
	}
	public MultipleGroupPlayerConfigOptions(@NotNull String name, @NotNull String key, @Nullable Integer limit, boolean excludeSelf) {
		this(name, key, limit, null, excludeSelf);
	}
	public MultipleGroupPlayerConfigOptions(@NotNull String name, @NotNull String key, @Nullable Integer limit) {
		this(name, key, limit, null);
	}
	public MultipleGroupPlayerConfigOptions(@NotNull String name, @NotNull String key, @Nullable String emptyValue, boolean excludeSelf) {
		this(name, key, null, emptyValue, excludeSelf);
	}
	public MultipleGroupPlayerConfigOptions(@NotNull String name, @NotNull String key, @Nullable String emptyValue) {
		this(name, key, null, emptyValue);
	}
	public MultipleGroupPlayerConfigOptions(@NotNull String name, @NotNull String key, boolean excludeSelf) {
		this(name, key, null, null, excludeSelf);
	}
	public MultipleGroupPlayerConfigOptions(@NotNull String name, @NotNull String key) {
		this(name, key, null, null);
	}

	@Override
	public Set<String> getValidOptions(CommandSender sender) {
		if (! (sender instanceof Player player )) return Collections.emptySet();

		Group group = Group.getGroupOfPlayer(player);
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
	public String parseSingleValueToString(OfflinePlayer value) {
		return value.getName();
	}
}
