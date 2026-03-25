package fr.ludos.game.arena;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import fr.ludos.role.Role;

public final class ArenaLoadoutService {
	private final ArenaGame game;
	private final Random random;

	public ArenaLoadoutService(ArenaGame game) {
		this.game = game;
		this.random = new Random();
	}

	public void applyCombatLoadout(Player player) {
		applyBaseCombatKit(player);

		Role.Builder role = ensureRole(player);
		applyRoleItems(player, role);
	}

	private void applyBaseCombatKit(Player player) {
		PlayerInventory inventory = player.getInventory();
		inventory.clear();

		ItemStack sword = enchantedItem(Material.DIAMOND_SWORD, Enchantment.DAMAGE_ALL, 3);
		inventory.setItem(0, sword);

		ItemStack helmet = enchantedArmor(Material.DIAMOND_HELMET);
		ItemStack chestplate = enchantedArmor(Material.DIAMOND_CHESTPLATE);
		ItemStack leggings = enchantedArmor(Material.DIAMOND_LEGGINGS);
		ItemStack boots = enchantedArmor(Material.DIAMOND_BOOTS);

		inventory.setArmorContents(new ItemStack[] { boots, leggings, chestplate, helmet });
	}

	private ItemStack enchantedArmor(Material type) {
		return enchantedItem(type, Enchantment.PROTECTION_ENVIRONMENTAL, 3);
	}

	private ItemStack enchantedItem(Material type, Enchantment enchantment, int level) {
		ItemStack stack = new ItemStack(type);
		stack.addUnsafeEnchantment(enchantment, level);
		return stack;
	}

	private Role.Builder ensureRole(Player player) {
		Role.Builder role = Role.getRole(player);
		if (role != null) return role;

		List<String> roleIds = new ArrayList<>(Role.getRegistered().keySet());
		if (roleIds.isEmpty()) return null;

		Collections.sort(roleIds);
		String chosen = roleIds.get(random.nextInt(roleIds.size()));
		Role.setRole(player, chosen);
		return Role.getRoleById(chosen);
	}

	private void applyRoleItems(Player player, Role.Builder role) {
		if (role == null) return;

		List<ItemStack> loadout = role.createArenaLoadout(player, game);
		if (loadout != null && !loadout.isEmpty()) {
			player.getInventory().addItem(loadout.toArray(new ItemStack[0]));
		}

		role.onArenaLoadoutApplied(player, game);
	}
}
