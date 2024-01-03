package fr.ludos.recipes;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.inventory.ShapelessRecipe;

public class goldenApple extends JavaPlugin {

    @Override
    public void onEnable() {
        ItemStack goldenApple = new ItemStack(Material.GOLDEN_APPLE, 1);
        ShapelessRecipe goldenAppleRecipe = new ShapelessRecipe(goldenApple);
        goldenAppleRecipe.addIngredient(1, Material.GOLD_INGOT);
        goldenAppleRecipe.addIngredient(2, Material.GOLD_INGOT);
        Bukkit.getServer().addRecipe(goldenAppleRecipe);
    }
}