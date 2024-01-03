package fr.ludos.recipes;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.inventory.ShapedRecipe;


public class EnchantementTable extends JavaPlugin {
    public static void main(String[] args) {
        ItemStack EnchantementTable = new ItemStack(Material.ENCHANTING_TABLE, 1);
        ShapedRecipe EnchantementTablerecipe = new ShapedRecipe(EnchantementTable);
        EnchantementTablerecipe.shape(
            "III", 
            "IDI", 
            "***"
        );
        EnchantementTablerecipe.setIngredient('I', Material.LEATHER);
        EnchantementTablerecipe.setIngredient('D', Material.DIAMOND);
        Bukkit.addRecipe(EnchantementTablerecipe);
    }

    private static void craftEnchantmentTable() {}
}
