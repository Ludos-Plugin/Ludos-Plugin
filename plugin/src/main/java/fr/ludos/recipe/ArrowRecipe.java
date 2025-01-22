package fr.ludos.recipe;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import fr.ludos.Ludos;

import org.bukkit.inventory.ShapelessRecipe;

public class ArrowRecipe extends RecipeRegisterer {
	private static final String RECIPE_NAMESPACE_KEY = "ludos_arrow_recipe";

	private static NamespacedKey recipeKey = null;


	public ArrowRecipe(Ludos plugin) {
		super(plugin);

		recipeKey = new NamespacedKey(plugin, RECIPE_NAMESPACE_KEY);
	}

	@Override
	public void Register() {
		ItemStack arrow = new ItemStack(Material.ARROW, 1);
		ShapelessRecipe recipe = new ShapelessRecipe(recipeKey, arrow);
		recipe.addIngredient(Material.STICK);
		Bukkit.addRecipe(recipe);
	}

	@Override
	public void Unregister() {
		Bukkit.getServer().removeRecipe(recipeKey);
	}
}