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

public final class MultipleGroupPlayerConfigOptions extends TypedConfigOptions<Set<OfflinePlayer>> {
	private final @Nullable Integer limit;
	private final String defaultOption;
	private final boolean excludeSelf;

	public MultipleGroupPlayerConfigOptions(@NotNull String name, @Nullable Integer limit, @Nullable String defaultOption, boolean excludeSelf) {
		super(name);
		this.limit = limit;
		this.defaultOption = defaultOption;
		this.excludeSelf = excludeSelf;
	}
	public MultipleGroupPlayerConfigOptions(@NotNull String name, @Nullable Integer limit, @Nullable String defaultOption) {
		this(name, limit, defaultOption, false);
	}
	public MultipleGroupPlayerConfigOptions(@NotNull String name, @Nullable Integer limit, boolean excludeSelf) {
		this(name, limit, null, excludeSelf);
	}
	public MultipleGroupPlayerConfigOptions(@NotNull String name, @Nullable Integer limit) {
		this(name, limit, null);
	}
	public MultipleGroupPlayerConfigOptions(@NotNull String name, @Nullable String defaultOption, boolean excludeSelf) {
		this(name, null, defaultOption, excludeSelf);
	}
	public MultipleGroupPlayerConfigOptions(@NotNull String name, @Nullable String defaultOption) {
		this(name, null, defaultOption);
	}
	public MultipleGroupPlayerConfigOptions(@NotNull String name, boolean excludeSelf) {
		this(name, null, null, excludeSelf);
	}
	public MultipleGroupPlayerConfigOptions(@NotNull String name) {
		this(name, null, null);
	}

	@Override
	public Set<OfflinePlayer> getDefaultTypedValue() {
		return Collections.emptySet();
	}

	@Override
	public Set<String> getOptions(CommandSender sender) {
		if (! (sender instanceof Player player )) return Collections.emptySet();

		Group group = Group.getGroupOfPlayer(player);
		if (group == null) return Collections.emptySet();

		Set<String> res = group.getPlayers().stream()
			.map(OfflinePlayer::getName)
			.collect(Collectors.toCollection(HashSet<String>::new));
		if (excludeSelf) {
			res.remove(player.getName());
		}
		if (defaultOption != null) {
			res.add(defaultOption);
		}

		return res;
	}

	@Override
	public boolean setValue(String key, @NotNull String[] args, CommandSender sender, ConfigurationSection container) {
		if (args.length == 1 && args[0].equals(defaultOption)) {
			container.set(key, null);
			sender.sendMessage(getName() + " set to " + defaultOption);
			return true;
		}

		List<String> vals = Arrays.stream(args)
			.filter(a -> isValidOption(a, sender))
			.toList();

		container.set(key, vals);
		sender.sendMessage(getName() + " set to " + vals.stream().collect(Collectors.joining(", ")));
		return true;
	}

	@Override
	public @Nullable List<@NotNull String> tabComplete(@NotNull String[] args, CommandSender sender) {
		Set<String> options = getOptions(sender);
		if (args.length <= 1) {
			return options.stream().toList();
		}

		if (args[0].equals(defaultOption)) {
			return Collections.emptyList();
		}

		options.remove(defaultOption);

		for (int i = 0; i < args.length - 1; i++) {
			options.remove(args[i]);
		}
		return options.stream().toList();
	}

	@Override
	protected Set<OfflinePlayer> fromString(String value) {
		if (value.equals(defaultOption)) return Collections.emptySet();
		return Arrays.stream(value.split(" "))
			.map(Bukkit::getOfflinePlayer)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
	}
	@Override
	protected String toString(Set<OfflinePlayer> value) {
		if (value == null) return defaultOption;
		return value.stream()
			.map(OfflinePlayer::getName)
			.filter(Objects::nonNull)
			.collect(Collectors.joining(" "));
	}
}
