package fr.ludos.item.texture;

import java.util.Arrays;
import java.util.Optional;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public final class TextureApplier {
    private static final NamespacedKey ID_KEY = new NamespacedKey("ludos", "id");

    public static void process(Player player) {
        Arrays.stream(player.getInventory().getContents())
              .filter(TextureApplier::isSpecial)
              .forEach(TextureApplier::refresh);
        player.updateInventory();
    }

    private static boolean isSpecial(ItemStack item) {
        return Optional.ofNullable(item)
                      .filter(ItemStack::hasItemMeta)
                      .map(ItemStack::getItemMeta)
                      .map(meta -> meta.getPersistentDataContainer().has(ID_KEY, PersistentDataType.STRING))
                      .orElse(false);
    }

    private static void refresh(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;

        var meta = item.getItemMeta();

        if (!meta.hasCustomModelData()) return;

        var data = meta.getCustomModelData();

        meta.setCustomModelData(null);

        item.setItemMeta(meta);
        meta.setCustomModelData(data);
        item.setItemMeta(meta);
    }
}