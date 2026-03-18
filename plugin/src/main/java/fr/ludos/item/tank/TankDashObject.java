package fr.ludos.item.tank;

import java.util.List;
import java.util.ArrayList;
import javax.annotation.Nullable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import org.bukkit.util.Vector;

import fr.ludos.item.SpecialItem;
import fr.ludos.game.Game;
import fr.ludos.role.Role;
import fr.ludos.role.TankRole;

public class TankDashObject extends SpecialItem {
	private static final String ID = "tank_dasher";
	private static final int COOLDOWN_DURATION = 30;
	public static final double dashpower = 2.0;

	protected TankDashObject(ItemStack stack, Player owner, Game game) {
		super(stack, owner, game);
	}

	public static @Nullable TankDashObject fromItemStack(ItemStack stack, Game game) throws IllegalArgumentException {
		Player owner = SpecialItem.getSpecialItemOwner(stack, ID, game);
		if (owner == null)
			return null;

		return new TankDashObject(stack, owner, game);
	}

	public static TankDashObject createItem(Player owner, Game game) {
		TankDashObject dasher = new TankDashObject(createItemStack(), owner, game);
		dasher.initializeItem();

		return dasher;
	}

	public void useDash(Vector direction) {
		Player player = this.getOwner();
		player.setVelocity(direction.normalize().multiply(dashpower));
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public Component getName() {
		return Component.text("Tank Dasher")
				.decoration(TextDecoration.ITALIC, false);
	}

	private static ItemStack createItemStack() {
		ItemStack stack = new ItemStack(Material.FIREWORK_ROCKET);

		return stack;
	}

	public static class Events extends SpecialItem.Events<TankDashObject> {

		public Events(Game game) {
			super(game, 2, false);
		}

		@Override
		@Nullable
		protected TankDashObject getItem(ItemStack stack, Game game) {
			return TankDashObject.fromItemStack(stack, game);
		}

		@Override
		protected TankDashObject createItem(Player owner, Game game) {
			return TankDashObject.createItem(owner, game);
		}

		@Override
		protected Boolean canPlayerHaveItem(HumanEntity owner) {
			return Role.isPlayerRole(owner, TankRole.ID);
		}

		@EventHandler
		public void onPlayerUseDash(PlayerInteractEvent event) {
			if (event.getHand() != EquipmentSlot.HAND)
				return;
			if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
				return;
			event.setCancelled(true);
			Player player = event.getPlayer();
			ItemStack item = player.getInventory().getItemInMainHand();

			TankDashObject dasher = TankDashObject.fromItemStack(item, game);
			if (dasher == null)
				return;

			if (player.getCooldown(Material.FIREWORK_ROCKET) > 0)
				return;

			dasher.useDash(player.getLocation().getDirection());

			player.setCooldown(Material.FIREWORK_ROCKET, COOLDOWN_DURATION * 20);
		}
	}

}