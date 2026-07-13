package fr.ludos.core.config;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class SubConfigOptions extends ConfigOptions {
	private final ConfigMap map;

	public SubConfigOptions(String namespace, ConfigMap map) {
		super(namespace);
		this.map = map;
	}
	public SubConfigOptions(ConfigMap map) {
		this(map.namespace(), map);
	}
	public SubConfigOptions(String namespace, Map<String, ConfigOptions> values) {
		this(new ConfigHashMap(namespace, values));
	}
	public SubConfigOptions(String namespace, Collection<ConfigEntryInterface> entries) {
		this(new ConfigHashMap(namespace, entries));
	}

	@Override
	public @NotNull Set<@NotNull String> getOptions(CommandSender sender) {
		return map.getKeys();
	}

	@Override
	public String getDefaultValue() {
		return null;
	}

	@Override
	public boolean setValue(String key, @NotNull String[] args, CommandSender sender, ConfigurationSection container) {
		if (args.length == 1) return false;

		String optionsKey = args[0];
		ConfigOptions options = map.getOptions(optionsKey);
		if (options == null) return false;

		return options.setValue(key + "." + optionsKey, Arrays.copyOfRange(args, 1, args.length), sender, container);
	}

	@Override
	public List<@NotNull String> tabComplete(@NotNull String[] args, CommandSender sender) {
		if (args.length <= 1) {
			return map.getKeys().stream().toList();
		}

		ConfigOptions options = map.getOptions(args[0]);
		if (options == null) return Collections.emptyList();

		return options.tabComplete(Arrays.copyOfRange(args, 1, args.length), sender);
	}
}
