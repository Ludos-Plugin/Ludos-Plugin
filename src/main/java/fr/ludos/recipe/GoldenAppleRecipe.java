package fr.ludos.recipe;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import fr.ludos.Main;

import org.bukkit.inventory.ShapelessRecipe;

public class GoldenAppleRecipe extends RecipeRegisterer {
	private static final String RECIPE_NAMESPACE_KEY = "ludos_goldapple_recipe";

	private static NamespacedKey recipeKey = null;


	public GoldenAppleRecipe(Main plugin) {
		super(plugin);

		recipeKey = new NamespacedKey(plugin, RECIPE_NAMESPACE_KEY);
	}

	@Override
	public void Register() {
		ItemStack goldenApple = new ItemStack(Material.GOLDEN_APPLE, 1);
		ShapelessRecipe goldenAppleRecipe = new ShapelessRecipe(recipeKey, goldenApple);
		goldenAppleRecipe.addIngredient(1, Material.GOLD_INGOT);
		goldenAppleRecipe.addIngredient(2, Material.GOLD_INGOT);
		Bukkit.getServer().addRecipe(goldenAppleRecipe);
	}

	@Override
	public void Unregister() {
		Bukkit.getServer().removeRecipe(recipeKey);
	}
}