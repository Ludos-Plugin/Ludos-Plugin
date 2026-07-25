package fr.ludos.core.command.ludos;

import java.util.HashMap;

import org.apache.commons.lang3.tuple.Pair;

import fr.ludos.core.Ludos;
import fr.ludos.core.command.Subcommand;
import fr.ludos.core.group.Group;
import fr.ludos.core.persistence.config.ConfigRoot;
import fr.ludos.core.persistence.config.sectionProvider.ConfigSectionMap;

/**
 * {@link Subcommand} used to select a given Configuration scope for subsequent Configuration Options.
 */
public class ScopeConfigMap extends ConfigSectionMap {
	public ScopeConfigMap(Ludos ludos, ConfigRoot globalRoot, ConfigRoot groupRoot, ConfigRoot playerRoot) {
		super(new HashMap<>() {{
			put(Ludos.GLOBAL_KEY, Pair.of(new GlobalConfigProvider(ludos), globalRoot));
			put(Group.NAMESPACE, Pair.of(new GroupConfigProvider(ludos.getGroupManager()), groupRoot));
			put(Ludos.PLAYER_NAMESPACE, Pair.of(new PlayerConfigProvider(ludos), playerRoot));
		}});
	}
	public ScopeConfigMap(Ludos ludos, ConfigRoot root) {
		this(ludos, root, root, root);
	}
}
