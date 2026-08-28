package fr.ludos.core.gui;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import fr.ludos.core.persistence.config.ConfigNode;
import fr.ludos.core.persistence.config.ConfigNodeCollection;
import fr.ludos.core.persistence.config.ConfigNodeMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import xyz.xenondevs.invui.item.builder.AbstractItemBuilder;
import xyz.xenondevs.invui.item.builder.ItemBuilder;

/**
 *
 */
public interface ConfigHolder {
	ConfigNodeCollection getConfig();
	public default ConfigNodeMap getConfigMap(String id, GuiObject object, List<ConfigNode> configNodes) {
		return new ConfigNodeMap(
			object.displayName(),
			id,
			configNodes
		) {
			@Override
			public AbstractItemBuilder<?> createItem(Player player) {
				return object.createItem(player);
			}
		};
	}

	public static final GuiObject CONFIG_OBJECT = new GuiObject() {
		@Override
		public AbstractItemBuilder<?> createItem(Player player) {
			return new ItemBuilder(Material.LEVER);
		}
		@Override
		public TextComponent displayName() {
			return Component.text("Config");
		}
	};
}
