package fr.ludos.core.command.ludos.config;

import java.util.Set;

import fr.ludos.core.config.BooleanConfigOptions;
import fr.ludos.core.config.ConfigOptionsMap;

public final class LudosConfigMap extends ConfigOptionsMap {
	public static final BooleanConfigOptions guidebookMessage =
		new BooleanConfigOptions("Show guidebook message on login", "guidebook_message", true);

	public static final LudosConfigMap instance = new LudosConfigMap();

	private LudosConfigMap() {
		super("ludos", Set.of(guidebookMessage));
	}
}