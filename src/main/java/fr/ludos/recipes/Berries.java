package fr.ludos.recipes;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.inventory.ShapelessRecipe;


public class Berries extends JavaPlugin {
    @Override
    public void onEnable() {
        // Create a shapeless recipe for a berry
        ItemStack berry = new ItemStack(Material.SWEET_BERRIES, 4);
        ShapelessRecipe berryRecipe = new ShapelessRecipe(berry);
        berryRecipe.addIngredient(1, Material.GRAVEL);
        berryRecipe.addIngredient(2, Material.GRAVEL);
        Bukkit.addRecipe(berryRecipe);
    }
}
      


