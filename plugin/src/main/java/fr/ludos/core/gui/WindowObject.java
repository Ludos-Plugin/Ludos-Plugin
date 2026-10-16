package fr.ludos.core.gui;

import xyz.xenondevs.invui.window.Window;

/**
 * Interface for objects which can be represented inside {@link Window}s, and can provide a {@link Window} to display to the player.
 */
public interface WindowObject extends GuiObject, WindowProvider {}
