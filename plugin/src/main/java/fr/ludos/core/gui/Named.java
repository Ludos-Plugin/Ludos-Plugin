package fr.ludos.core.gui;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * Object with a normalized {@link TextComponent} name.
 */
public interface Named {
	/**
	 * Non-normalized Display name of the owner object.
	 * @return A non-normalized (no need for .decoration(TextDecoration.ITALIC, false)) TextComponent name.
	 */
	public TextComponent displayName();
	/**
	 * A normalized version of {@link #displayName()}'s component.
	 * @return A normalized (with .decoration(TextDecoration.ITALIC, false)) TextComponent name.
	 */
	public default TextComponent normalizedDisplayName() {
		return displayName()
			.decoration(TextDecoration.ITALIC, false);
	}
}
