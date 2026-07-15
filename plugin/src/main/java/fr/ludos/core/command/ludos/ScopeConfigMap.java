package fr.ludos.core.command.ludos;

import java.util.HashMap;

import org.apache.commons.lang3.tuple.Pair;

import fr.ludos.core.Ludos;
import fr.ludos.core.config.ConfigOptions;
import fr.ludos.core.config.sectionProvider.ConfigSectionMap;

public class ScopeConfigMap extends ConfigSectionMap {
	public ScopeConfigMap(Ludos plugin, ConfigOptions globalOptions, ConfigOptions localOptions) {
		super(new HashMap<>() {{
			put("global", Pair.of(new GlobalConfigProvider(plugin), globalOptions));
			put("local", Pair.of(new GroupConfigProvider(plugin), localOptions));
		}});
	}
	public ScopeConfigMap(Ludos plugin, ConfigOptions options) {
		this(plugin, options, options);
	}
}
