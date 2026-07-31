package fr.ludos.core.gui.item;

import org.bukkit.Material;

import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.SimpleItem;

/**
 * Simple filler item.
 */
public class BorderItem extends SimpleItem {
	public static final BorderItem INSTANCE = new BorderItem();

	private BorderItem() {
		super(
			new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
				.setDisplayName("")
		);
	}

}
