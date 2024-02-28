package fr.ludos.recipe;

import fr.ludos.Main;

public abstract class RecipeRegisterer {

	public static void RegisterRecipe(RecipeRegisterer recipe) {
		recipe.Register();
	}

	public RecipeRegisterer(Main plugin) {}


	public abstract void Register();
	public abstract void Unregister();
}