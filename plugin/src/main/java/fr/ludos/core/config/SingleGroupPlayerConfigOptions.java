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

public final class SingleGroupPlayerConfigOptions extends ValueConfigOptions<OfflinePlayer> {
	private final boolean excludeSelf;

	public SingleGroupPlayerConfigOptions(@NotNull String name, @NotNull String key, @Nullable String emptyValue, boolean excludeSelf) {
		super(name, key, emptyValue);
		this.excludeSelf = excludeSelf;
	}
	public SingleGroupPlayerConfigOptions(@NotNull String name, @NotNull String key, @Nullable String emptyValue) {
		this(name, key, emptyValue, false);
	}

	@Override
	public OfflinePlayer getDefaultValue() {
		return null;
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
	protected OfflinePlayer fromString(String value) {
		if (value == null || value.equals(emptyValue())) return null;
		return Bukkit.getOfflinePlayer(value);
	}
	@Override
	protected String toString(OfflinePlayer value) {
		if (value == null) return emptyValue();
		return value.getName();
	}
}
