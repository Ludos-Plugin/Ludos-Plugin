package fr.ludos.recipes;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.inventory.ShapelessRecipe;

public class Arrow extends JavaPlugin {
    @Override
    public void onEnable() {
        ItemStack arrow = new ItemStack(Material.ARROW, 1);
        ShapelessRecipe recipe = new ShapelessRecipe(arrow);
        recipe.addIngredient(Material.STICK);
        Bukkit.addRecipe(recipe);
    }
}