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

import fr.ludos.item.LevelItem;
import fr.ludos.item.assassin.AssassinBoots;
import fr.ludos.item.assassin.AssassinDagger;
import fr.ludos.item.assassin.TeleportScroll;
import fr.ludos.item.harvester.HarvesterPick;
import fr.ludos.item.harvester.HarvesterPickLevels;
import fr.ludos.item.harvester.HarvesterScythe;
import fr.ludos.item.harvester.HarvesterScytheLevels;
import fr.ludos.item.harvester.HarvesterSpade;
import fr.ludos.item.harvester.HarvesterSpadeLevels;
import fr.ludos.item.huntsman.HuntsmanArrow;
import fr.ludos.item.huntsman.HuntsmanBow;
import fr.ludos.item.huntsman.HuntsmanCrossbow;
import fr.ludos.item.huntsman.HuntsmanCrossbowBranches;
import fr.ludos.item.trapper.TrapperDagger;
import fr.ludos.item.trapper.TrapperDaggerBranches;
import fr.ludos.item.trapper.TrapperSnareDevice;
import fr.ludos.role.AssassinRole;
import fr.ludos.role.HarvesterRole;
import fr.ludos.role.HuntsmanRole;
import fr.ludos.role.Role;
import fr.ludos.role.TrapperRole;

public final class ArenaLoadoutService {
	private final ArenaGame game;
	private final Random random;

	public ArenaLoadoutService(ArenaGame game) {
		this.game = game;
		this.random = new Random();
	}

	public void applyCombatLoadout(Player player) {
		applyBaseCombatKit(player);

		String roleId = ensureRole(player);
		applyRoleItems(player, roleId);

		if (HarvesterRole.id.equals(roleId)) {
			ItemStack chestplate = player.getInventory().getChestplate();
			HarvesterRole.setExplosiveChestplateMode(chestplate, true);
		}
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

	private String ensureRole(Player player) {
		Role.Builder role = Role.getRole(player);
		if (role != null) return role.getId();

		List<String> roleIds = new ArrayList<>(Role.getRegistered().keySet());
		if (roleIds.isEmpty()) return "";

		Collections.sort(roleIds);
		String chosen = roleIds.get(random.nextInt(roleIds.size()));
		Role.setRole(player, chosen);
		return chosen;
	}

	private void applyRoleItems(Player player, String roleId) {
		if (roleId == null || roleId.isBlank()) return;

		switch (roleId) {
			case HarvesterRole.id -> giveHarvester(player);
			case HuntsmanRole.id -> giveHuntsman(player);
			case TrapperRole.id -> giveTrapper(player);
			case AssassinRole.id -> giveAssassin(player);
			default -> {
			}
		}
	}

	private void giveHarvester(Player player) {
		player.getInventory().addItem(
			HarvesterPick.createItem(player, maxLevelState(HarvesterPickLevels.values()), game).getStack(),
			HarvesterScythe.createItem(player, maxLevelState(HarvesterScytheLevels.values()), game).getStack(),
			HarvesterSpade.createItem(player, maxLevelState(HarvesterSpadeLevels.values()), game).getStack()
		);
	}

	private void giveHuntsman(Player player) {
		ItemStack arrows = HuntsmanArrow.createItem(player, game).getStack();
		arrows.setAmount(64);

		player.getInventory().addItem(
			HuntsmanBow.createItem(player, game).getStack(),
			HuntsmanCrossbow.createItem(player, maxMultiLevels(HuntsmanCrossbowBranches.values(), 2), game).getStack(),
			arrows
		);
	}

	private void giveTrapper(Player player) {
		player.getInventory().addItem(
			TrapperDagger.createItem(player, maxMultiLevels(TrapperDaggerBranches.values(), 3), game).getStack(),
			TrapperSnareDevice.createItem(player, game).getStack()
		);
	}

	private void giveAssassin(Player player) {
		player.getInventory().addItem(
			AssassinDagger.createItem(player, game).getStack(),
			AssassinBoots.createItem(player, game).getStack(),
			TeleportScroll.createItem(player, game).getStack()
		);
	}

	private <TLevel extends Enum<TLevel>> LevelItem.LevelState maxLevelState(TLevel[] levels) {
		int maxLevel = Math.max(0, levels.length - 1);
		return new LevelItem.LevelState(maxLevel, 0.0);
	}

	private <TBranch extends Enum<TBranch>> LevelItem.LevelState[] maxMultiLevels(TBranch[] branches, int level) {
		int resolvedLevel = Math.max(0, level);
		LevelItem.LevelState[] values = new LevelItem.LevelState[branches.length];
		for (int i = 0; i < values.length; i++) {
			values[i] = new LevelItem.LevelState(resolvedLevel, 0.0);
		}
		return values;
	}
}
