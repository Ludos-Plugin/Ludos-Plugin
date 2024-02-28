package fr.ludos.recipe;

import fr.ludos.Main;

public abstract class Recipe {

	public static void RegisterRecipe(Recipe recipe) {
		recipe.Register();
	}

	public Recipe(Main plugin) {}


	public abstract void Register();
}