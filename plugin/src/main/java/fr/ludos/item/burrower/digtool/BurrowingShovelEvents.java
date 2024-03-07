package fr.ludos.item.burrower.digtool;

import org.bukkit.NamespacedKey;

import fr.ludos.Main;
import fr.ludos.item.SpecialItemEvents;
import fr.ludos.role.BurrowerRole;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.event.block.Action;

import javax.annotation.Nullable;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;



/**
 * BurrowingClaw is a class that represents a special item, "The Burrower's Pickaxe," in Minecraft.
 * This item allows the miner player to improve their own pickaxe based on the ores they collect.
 * <br><br>
 * Features:
 * <br><br>
 * - Provides methods to create a miner pickaxe, give it to the player and level up the pickaxe based on XP.
 * <br><br>
 * - Automatically updates the pickaxe's material and enchantments as it levels up.
 * <br><br>
 * - Defines an evolution path from wood to stone, iron, gold, diamond and finally netherite pickaxe.
 * <br><br>
 * Usage:
 * Example:
 * @author feur25 & Ganon358
 * @version 1.0
 * @see org.bukkit.entity.Player
 * @see org.bukkit.inventory.ItemStack
 * @see org.bukkit.Material
 * @see org.bukkit.inventory.meta.ItemMeta
 * @see java.util.Collections
 */

public class BurrowingShovelEvents extends SpecialItemEvents<BurrowingShovel> {
	private static final String OWNER_NAMESPACE_KEY = "ludos_miner_claw_owner";
	private static final String USAGES_NAMESPACE_KEY = "ludos_miner_claw_usages";

	private static NamespacedKey ownerKey = null;
	private static NamespacedKey usagesKey = null;




	static NamespacedKey getOwnerKey() {
		return ownerKey;
	}
	static NamespacedKey getUsagesKey() {
		return usagesKey;
	}


	public BurrowingShovelEvents() {
		Main plugin = Main.getInstance();
		ownerKey = new NamespacedKey(plugin, OWNER_NAMESPACE_KEY);
		usagesKey = new NamespacedKey(plugin, USAGES_NAMESPACE_KEY);
	}


	public static int counterSpell = 3;

	public class BlockBreak {
		Location location;
		Material material;

		public BlockBreak(Location location, Material material){
			this.location = location;
			this.material = material;
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		player.getInventory().addItem(new BurrowingShovel(player).getStack());
	}

	private void sendHUDIcon(Player player, Boolean hudActive) {
		if (! hudActive){
			player.removePotionEffect(PotionEffectType.LUCK);
		} else {
			player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, Integer.MAX_VALUE, 1, false, true));
		}
    }

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}


		Player player = event.getPlayer();
		ItemStack mainItem = event.getItem();

		BurrowingShovel claw = getItem(mainItem);
		if (claw == null) {
			return;
		}

		if (player.hasCooldown(mainItem.getType())) {
			return;
		}

		List<BlockState> playerBlocks = BurrowingShovel.tunnelBlocks.get(player);


		if (playerBlocks != null) {
			sendHUDIcon(player, false);
			claw.revertTunnel(player);
		} else {
			sendHUDIcon(player, true);
			claw.digTunnel(player);
		}
	}

	// private void reduceUsage(Player player) {
	// 	int remainingUsages = usages.getOrDefault(player, MAX_USAGES);
	// 	if (remainingUsages > 0) {
	// 		usages.put(player, remainingUsages - 1);
	// 	}
	// }


	@Override
	@Nullable
	protected BurrowingShovel getItem(ItemStack stack) {
		try {
			BurrowingShovel bow = new BurrowingShovel(stack);
			return bow;
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
	@Override
	protected BurrowingShovel createItem(Player owner) {
		return new BurrowingShovel(owner);
	}

	@Override
	protected String getRoleId() {
		return BurrowerRole.id;
	}
}