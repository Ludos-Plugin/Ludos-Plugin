package fr.ludos.core.command.ludos.config;

import fr.ludos.core.Ludos;

/**
 * Subcommand for all Ludos configuration.
 */
public class LudosConfig extends ConfigSubcommand {
	public LudosConfig(Ludos ludos) {
		super(ludos, "Configure Ludos.", ludos.scopeConfigMap);
	}
}