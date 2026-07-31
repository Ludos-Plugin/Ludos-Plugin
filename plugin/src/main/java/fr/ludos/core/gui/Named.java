package fr.ludos.core.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * Object with a normalized {@link Component} name.
 */
public interface Named {
	/**
	 * Non-normalized Display name of the owner object.
	 * @return A non-normalized (no need for .decoration(TextDecoration.ITALIC, false)) Component name.
	 */
	public Component displayName();
	/**
	 * A normalized version of {@link #displayName()}'s component.
	 * @return A normalized (with .decoration(TextDecoration.ITALIC, false)) Component name.
	 */
	public default Component normalizedDisplayName() {
		return displayName()
			.decoration(TextDecoration.ITALIC, false);
	}
}
