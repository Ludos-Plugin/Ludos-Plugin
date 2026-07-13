package fr.ludos.core.command.ludos;

import java.util.HashMap;

import fr.ludos.core.Ludos;
import fr.ludos.core.config.ConfigOptions;
import fr.ludos.core.config.sectionProvider.ConfigSectionMap;

public class ScopeConfigMap extends ConfigSectionMap {
	public ScopeConfigMap(Ludos plugin, ConfigOptions options) {
		super(options, new HashMap<>() {{
			put("global", new GlobalConfigProvider(plugin));
			put("local", new GroupConfigProvider(plugin));
		}});
	}
}
