package fr.ludos.item.harvester;

import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.ludos.item.BranchItem;
import fr.ludos.item.SpecialItem;
import fr.ludos.role.HarvesterRole;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public enum HarvesterPickBranches implements BranchItem.Branch<HarvesterPickBranches> {
	Pickaxe (
		Component.text("Pickaxe").color(NamedTextColor.AQUA),
		Component.text("Mines blocks normally.")
	) {
		@Override
		public void onBreakBlock(HarvesterPick pick, BlockBreakEvent event) {
			Block targetBlock = event.getBlock();

			HarvesterRole.awardBreak(event.getPlayer(), targetBlock, pick.getGame());
		}

		@Override
		public void onEquip(SpecialItem item) { }
		@Override
		public void onUnequip(SpecialItem item) { }
		@Override
		public void onSelectBranch(SpecialItem item) { }
		@Override
		public void onDeselectBranch(SpecialItem item) { }
	},
	Hammer (
		Component.text("Hammer").color(NamedTextColor.RED),
		Component.text("Mines a 3x3 area.")
	) {
		@Override
		public void onBreakBlock(HarvesterPick pick, BlockBreakEvent event) {
			Player player = pick.getOwner();

			List<Block> lastTwoTargetBlocks = player.getLastTwoTargetBlocks(null, 100);
			if (lastTwoTargetBlocks.size() != 2) return;

			Block targetBlock = lastTwoTargetBlocks.get(1);
			Block adjacentBlock = lastTwoTargetBlocks.get(0);


			BlockFace face = targetBlock.getFace(adjacentBlock);

			pick.breakRadius(targetBlock, face, player);
		}


		@Override
		public void onEquip(SpecialItem item) {
			Player owner = item.getOwner();
			owner.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, Integer.MAX_VALUE, 0, false, false));
		}
		@Override
		public void onUnequip(SpecialItem item) {
			Player owner = item.getOwner();
			owner.removePotionEffect(PotionEffectType.SLOW_DIGGING);
		}

		@Override
		public void onSelectBranch(SpecialItem item) {
			Player owner = item.getOwner();
			owner.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, Integer.MAX_VALUE, 0, false, false));
		}
		@Override
		public void onDeselectBranch(SpecialItem item) {
			Player owner = item.getOwner();
			owner.removePotionEffect(PotionEffectType.SLOW_DIGGING);
		}
	};

	private final Component name;
	@Override
	public Component getName() {
		return name;
	}

	private final Component description;
	@Override
	public Component getDescription() {
		return description;
	}

	public abstract void onBreakBlock(HarvesterPick pick, BlockBreakEvent event);


	private HarvesterPickBranches(Component name, Component description) {
		this.name = name;
		this.description = description;
	}
}