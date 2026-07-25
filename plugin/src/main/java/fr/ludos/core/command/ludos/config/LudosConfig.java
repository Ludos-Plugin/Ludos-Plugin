package fr.ludos.core.command.ludos.config;

import fr.ludos.core.Ludos;
import fr.ludos.core.command.ludos.ScopeConfigMap;

/**
 * Subcommand for all Ludos configuration.
 */
public class LudosConfig extends ConfigSubcommand {
	public LudosConfig(Ludos ludos) {
		super("Configure Ludos.", new ScopeConfigMap(
			ludos,
			ludos.globalConfigMap,
			ludos.groupConfigMap,
			ludos.playerConfigMap
		));
	}
}