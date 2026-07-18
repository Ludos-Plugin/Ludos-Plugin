package fr.ludos.core.command.ludos;

import java.util.HashMap;

import org.apache.commons.lang3.tuple.Pair;

import fr.ludos.core.Ludos;
import fr.ludos.core.config.ConfigOptions;
import fr.ludos.core.config.sectionProvider.ConfigSectionMap;

public class ScopeConfigMap extends ConfigSectionMap {
	public ScopeConfigMap(Ludos ludos, ConfigOptions globalOptions, ConfigOptions groupOptions, ConfigOptions playerOptions) {
		super(new HashMap<>() {{
			put("global", Pair.of(new GlobalConfigProvider(ludos), globalOptions));
			put("group", Pair.of(new GroupConfigProvider(ludos), groupOptions));
			put("player", Pair.of(new PlayerConfigProvider(ludos), playerOptions));
		}});
	}
	public ScopeConfigMap(Ludos ludos, ConfigOptions options) {
		this(ludos, options, options, options);
	}
}
