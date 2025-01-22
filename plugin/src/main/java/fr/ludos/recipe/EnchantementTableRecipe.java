package fr.ludos.recipe;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import fr.ludos.Ludos;

import org.bukkit.inventory.ShapedRecipe;


public class EnchantementTableRecipe extends RecipeRegisterer {
	private static final String RECIPE_NAMESPACE_KEY = "ludos_enchantment_recipe";

	private static NamespacedKey recipeKey = null;


	public EnchantementTableRecipe(Ludos plugin) {
		super(plugin);

		recipeKey = new NamespacedKey(plugin, RECIPE_NAMESPACE_KEY);
	}

	@Override
	public void Register() {
		ItemStack EnchantementTable = new ItemStack(Material.ENCHANTING_TABLE, 1);
		ShapedRecipe EnchantementTableRecipe = new ShapedRecipe(recipeKey, EnchantementTable);
		EnchantementTableRecipe.shape(
			"III",
			"IDI",
			"***"
		);
		EnchantementTableRecipe.setIngredient('I', Material.LEATHER);
		EnchantementTableRecipe.setIngredient('D', Material.DIAMOND);
		Bukkit.addRecipe(EnchantementTableRecipe);
	}

	@Override
	public void Unregister() {
		Bukkit.getServer().removeRecipe(recipeKey);
	}
}
