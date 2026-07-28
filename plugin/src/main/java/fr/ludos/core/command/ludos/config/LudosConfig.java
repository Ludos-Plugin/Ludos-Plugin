package fr.ludos.core.command.ludos.config;

import fr.ludos.core.Ludos;
import fr.ludos.core.persistence.config.scope.ScopeConfigMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

/**
 * Subcommand for all Ludos configuration.
 */
public class LudosConfig extends ConfigSubcommand {
	public static final TextComponent WINDOW_TITLE = Component.text("Ludos configuration");
	public LudosConfig(Ludos ludos) {
		super(ludos, "Configure Ludos.", new ScopeConfigMap(
			WINDOW_TITLE,
			ludos,
			ludos.globalConfigMap,
			ludos.groupConfigMap,
			ludos.playerConfigMap
		));
	}
}