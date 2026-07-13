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

public final class SingleGroupPlayerConfigOptions extends TypedConfigOptions<OfflinePlayer> {
	private final String defaultOption;
	private final boolean excludeSelf;

	public SingleGroupPlayerConfigOptions(@NotNull String name, @Nullable String defaultOption, boolean excludeSelf) {
		super(name);
		this.defaultOption = defaultOption;
		this.excludeSelf = excludeSelf;
	}
	public SingleGroupPlayerConfigOptions(@NotNull String name, @Nullable String defaultOption) {
		this(name, defaultOption, false);
	}
	public SingleGroupPlayerConfigOptions(@NotNull String name, boolean excludeSelf) {
		this(name, null, excludeSelf);
	}
	public SingleGroupPlayerConfigOptions(@NotNull String name) {
		this(name, null);
	}

	@Override
	public OfflinePlayer getDefaultTypedValue() {
		return null;
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
	protected OfflinePlayer fromString(String value) {
		if (value == defaultOption) return null;
		return Bukkit.getOfflinePlayer(value);
	}
	@Override
	protected String toString(OfflinePlayer value) {
		if (value == null) return defaultOption;
		return value.getName();
	}
}
