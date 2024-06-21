package fr.ludos.recipe;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import fr.ludos.Ludos;

import org.bukkit.inventory.ShapelessRecipe;


public class BerriesRecipe extends RecipeRegisterer {
	private static final String RECIPE_NAMESPACE_KEY = "ludos_berries_recipe";

	private static NamespacedKey recipeKey = null;


	public BerriesRecipe(Ludos plugin) {
		super(plugin);

		recipeKey = new NamespacedKey(plugin, RECIPE_NAMESPACE_KEY);
	}

	@Override
	public void Register() {
		ItemStack berries = new ItemStack(Material.SWEET_BERRIES, 4);
		ShapelessRecipe berryRecipe = new ShapelessRecipe(recipeKey, berries);
		berryRecipe.addIngredient(0, Material.GRAVEL);
		berryRecipe.addIngredient(1, Material.GRAVEL);
		Bukkit.addRecipe(berryRecipe);
	}

	@Override
	public void Unregister() {
		Bukkit.getServer().removeRecipe(recipeKey);
	}
}



