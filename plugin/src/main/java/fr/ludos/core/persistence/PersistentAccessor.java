package fr.ludos.core.persistence;

import java.util.Objects;

import javax.annotation.Nullable;

import org.bukkit.command.CommandSender;

import fr.ludos.core.persistence.config.sectionProvider.ConfigSectionProvider;

/**
 * An interface for easier Persistence management for a type T value.<br>
 * Does not need a {@link ConfigSectionProvider} when calling methods, as one is already specified.
 * @param <T> The type of value to persist
 */
public final class PersistentAccessor<T> {
	private final PersistentEntry<T> entry;
	private final ConfigSectionProvider provider;
	private final CommandSender sender;

	public PersistentAccessor(PersistentEntry<T> entry, ConfigSectionProvider provider, @Nullable CommandSender sender) {
		this.entry = Objects.requireNonNull(entry);
		this.provider = Objects.requireNonNull(provider);
		this.sender = Objects.requireNonNull(sender);
	}

	public final ConfigSectionProvider provider() {
		return this.provider;
	}


	public @Nullable T getOrNull() {
		return entry.getOrNull(provider.getConfig(sender));
	}
	public @Nullable T getOrDefault() {
		return entry.getOrDefault(provider.getConfig(sender));
	}

	public void set(T value) {
		entry.set(value, provider.getConfig(sender));
	}

	public void unset() {
		entry.unset(provider.getConfig(sender));
	}

	public boolean save() {
		return provider.saveConfig();
	}
}
