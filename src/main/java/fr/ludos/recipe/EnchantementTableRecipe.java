package fr.ludos.recipe;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import fr.ludos.Main;

import org.bukkit.inventory.ShapedRecipe;


public class EnchantementTableRecipe extends Recipe {
	private static final String RECIPE_NAMESPACE_KEY = "ludos_enchantment_recipe";

    private static NamespacedKey recipeKey = null;


    public EnchantementTableRecipe(Main plugin) {
		super(plugin);

		recipeKey = new NamespacedKey(plugin, RECIPE_NAMESPACE_KEY);
	}

    @Override
	public void Register() {
        ItemStack EnchantementTable = new ItemStack(Material.ENCHANTING_TABLE, 1);
        ShapedRecipe EnchantementTablerecipe = new ShapedRecipe(recipeKey, EnchantementTable);
        EnchantementTablerecipe.shape(
            "III", 
            "IDI", 
            "***"
        );
        EnchantementTablerecipe.setIngredient('I', Material.LEATHER);
        EnchantementTablerecipe.setIngredient('D', Material.DIAMOND);
        Bukkit.addRecipe(EnchantementTablerecipe);
    }
}
