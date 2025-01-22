package fr.ludos.recipe;

import fr.ludos.Ludos;

public abstract class RecipeRegisterer {

	public static void RegisterRecipe(RecipeRegisterer recipe) {
		recipe.Register();
	}

	public RecipeRegisterer(Ludos plugin) {}


	public abstract void Register();
	public abstract void Unregister();
}