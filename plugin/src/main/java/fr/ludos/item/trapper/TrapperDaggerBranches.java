package fr.ludos.item.trapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import fr.ludos.item.MultiLevelBranchItem;
import fr.ludos.item.SpecialItem;
import net.kyori.adventure.text.Component;


public enum TrapperDaggerBranches implements MultiLevelBranchItem.Branch<TrapperDaggerBranches> {
	SHARPNESS (Enchantment.DAMAGE_ALL, 3) {
		@Override
		public Component getName() {
			return Component.text("Sharpness");
		}

		@Override
		public Component getDescription() {
			return Component.text("");
		}

		@Override
		public void onEquip(SpecialItem item) { }
		@Override
		public void onUnequip(SpecialItem item) { }

		@Override
		public void onSelectBranch(SpecialItem item) {
			item.getStack().addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 3);
		}
		@Override
		public void onDeselectBranch(SpecialItem item) {
			item.getStack().removeEnchantment(Enchantment.DAMAGE_ALL);
		}
	},
	LOOTING (Enchantment.LOOT_BONUS_MOBS, 1) {
		@Override
		public Component getName() {
			return Component.text("Looting");
		}

		@Override
		public Component getDescription() {
			return Component.text("");
		}

		@Override
		public void onEquip(SpecialItem item) { }
		@Override
		public void onUnequip(SpecialItem item) { }

		@Override
		public void onSelectBranch(SpecialItem item) {
			item.getStack().addUnsafeEnchantment(Enchantment.LOOT_BONUS_MOBS, 1);
		}
		@Override
		public void onDeselectBranch(SpecialItem item) {
			item.getStack().removeEnchantment(Enchantment.LOOT_BONUS_MOBS);
		}
	},
	FIRE_ASPECT (Enchantment.FIRE_ASPECT, 1) {
		@Override
		public Component getName() {
			return Component.text("Fire Aspect");
		}

		@Override
		public Component getDescription() {
			return Component.text("");
		}

		@Override
		public void onEquip(SpecialItem item) { }
		@Override
		public void onUnequip(SpecialItem item) { }

		@Override
		public void onSelectBranch(SpecialItem item) {
			item.getStack().addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 1);
		}
		@Override
		public void onDeselectBranch(SpecialItem item) {
			item.getStack().removeEnchantment(Enchantment.FIRE_ASPECT);
		}
	};

	private final static TrapperDaggerBranches[] values = TrapperDaggerBranches.values();

	public double getXpThreshold(int level) {
		switch (level) {
			case 0: return 25;
			case 1: return 55;
			case 2: return 175;
			case 3: return 500;
			default: return Double.MAX_VALUE;
		}
	}

	private Map<Enchantment, Integer> enchantments;
	public Map<Enchantment, Integer> getEnchantments() {
		return enchantments;
	}

	private int depth;
	public int getDepth(){
		return depth;
	}

	public int getLevelByTimer() {
		// int minutes = (int) (ManhuntTimer.totalSeconds / 60);
		// return minutes / 10 < 3 ? minutes / 10 : 2;
		return 0;
	}

	private TrapperDaggerBranches(Enchantment enchantment, int level) {
		this.enchantments = new HashMap<>();
		this.enchantments.put(enchantment, getLevelByTimer() + level);
	}

	@Override
	public void onSetLevel(int level, SpecialItem item) {
		Material material;
		switch (level) {
			case 0 -> material = Material.STONE_SWORD;
			case 1 -> material = Material.IRON_SWORD;
			case 2 -> material = Material.DIAMOND_SWORD;
			case 3 -> material = Material.NETHERITE_SWORD;
			default -> material = Material.NETHERITE_SWORD;
		}

		item.getStack().setType(material);
	}
	@Override
	public void onUnsetLevel(int level, SpecialItem item) { }
}