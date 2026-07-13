package fr.ludos.core.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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

public final class MultipleGroupPlayerConfigOptions extends ValueConfigOptions<Set<OfflinePlayer>> {
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
	public Set<OfflinePlayer> getEmptyValue() {
		return Collections.emptySet();
	}

	@Override
	public Set<String> getActualOptions(CommandSender sender) {
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
	public boolean set(@NotNull String[] args, CommandSender sender, ConfigurationSection config) {
		if (args.length == 0) {
			sender.sendMessage(getStringValueOrDefault(config));
			return false;
		}
		if (args.length == 1 && args[0].equals(emptyValue())) {
			config.set(key(), null);
			notifyUnset(sender);
			return true;
		}

		List<String> vals = Arrays.stream(args)
			.filter(a -> isValidOption(a, sender))
			.toList();

		config.set(key(), vals);
		notifySet(vals.stream().collect(Collectors.joining(", ")), sender);
		return true;
	}

	@Override
	public @Nullable List<@NotNull String> tabComplete(@NotNull String[] args, CommandSender sender) {
		Set<String> options = getOptions(sender);
		if (args.length <= 1) {
			return options.stream().toList();
		}

		if (args[0].equals(emptyValue())) {
			return Collections.emptyList();
		}

		options.remove(emptyValue());

		for (int i = 0; i < args.length - 1; i++) {
			options.remove(args[i]);
		}
		return options.stream().toList();
	}

	@Override
	protected Set<OfflinePlayer> fromString(String value) {
		if (value == null || value.equals(emptyValue())) return Collections.emptySet();
		return Arrays.stream(value.split(" "))
			.map(Bukkit::getOfflinePlayer)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
	}
	@Override
	protected String toString(Set<OfflinePlayer> value) {
		if (value == null) return emptyValue();
		return value.stream()
			.map(OfflinePlayer::getName)
			.filter(Objects::nonNull)
			.collect(Collectors.joining(" "));
	}
}
