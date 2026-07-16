package fr.ludos.core.command.ludos.config;

import java.util.Set;

import fr.ludos.core.config.BooleanConfigOptions;
import fr.ludos.core.config.ConfigOptionsMap;

public final class LudosConfigMap extends ConfigOptionsMap {
	public static final BooleanConfigOptions GUIDEBOOK_MESSAGE =
		new BooleanConfigOptions("Show guidebook message on login", "guidebook_message", true);

	public static final LudosConfigMap INSTANCE = new LudosConfigMap();

	private LudosConfigMap() {
		super("ludos", Set.of(GUIDEBOOK_MESSAGE));
	}
}