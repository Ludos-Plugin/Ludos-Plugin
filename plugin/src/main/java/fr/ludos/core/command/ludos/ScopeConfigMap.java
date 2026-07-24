package fr.ludos.core.command.ludos;

import java.util.HashMap;

import org.apache.commons.lang3.tuple.Pair;

import fr.ludos.core.Ludos;
import fr.ludos.core.command.Subcommand;
import fr.ludos.core.group.Group;
import fr.ludos.core.persistence.config.ConfigEntry;
import fr.ludos.core.persistence.config.sectionProvider.ConfigSectionMap;

/**
 * {@link Subcommand} used to select a given Configuration scope for subsequent Configuration Options.
 */
public class ScopeConfigMap extends ConfigSectionMap {
	public ScopeConfigMap(Ludos ludos, ConfigEntry globalOptions, ConfigEntry groupOptions, ConfigEntry playerOptions) {
		super(new HashMap<>() {{
			put(Ludos.GLOBAL_KEY, Pair.of(new GlobalConfigProvider(ludos), globalOptions));
			put(Group.NAMESPACE, Pair.of(new GroupConfigProvider(ludos.getGroupManager()), groupOptions));
			put(Ludos.PLAYER_NAMESPACE, Pair.of(new PlayerConfigProvider(ludos), playerOptions));
		}});
	}
	public ScopeConfigMap(Ludos ludos, ConfigEntry options) {
		this(ludos, options, options, options);
	}
}
