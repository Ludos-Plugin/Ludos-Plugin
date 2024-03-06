package fr.ludos.item.huntsman.bow;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.AbstractArrow.PickupStatus;

import fr.ludos.Main;
import fr.ludos.item.LevelItemEvents;

import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;

import fr.ludos.role.HuntsmanRole;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;


public class HuntsmanBowEvents extends LevelItemEvents<HuntsmanBow, HuntsmanBowLevels> {

	private static final String OWNER_NAMESPACE_KEY = "ludos_archer_bow_owner";
	private static final String XP_NAMESPACE_KEY = "ludos_archer_bow_xp";
	private static final String LVL_NAMESPACE_KEY = "ludos_archer_bow_lvl";

	private static NamespacedKey ownerKey = null;
	private static NamespacedKey xpKey = null;
	private static NamespacedKey lvlKey = null;



	static NamespacedKey getOwnerkey() {
		return ownerKey;
	}
	static NamespacedKey getXpKey() {
		return xpKey;
	}
	static NamespacedKey getLvlKey() {
		return lvlKey;
	}


	public HuntsmanBowEvents() {
		Main plugin = Main.getInstance();
		ownerKey = new NamespacedKey(plugin, OWNER_NAMESPACE_KEY);
		xpKey = new NamespacedKey(plugin, XP_NAMESPACE_KEY);
		lvlKey = new NamespacedKey(plugin, LVL_NAMESPACE_KEY);
	}


	public void poisonArrow1(Player player) {
		Location eyeLocation = player.getEyeLocation();
		World world = player.getWorld();

		Arrow poisonArrow = (Arrow) world.spawnEntity(eyeLocation, EntityType.ARROW);
		poisonArrow.setPickupStatus(PickupStatus.DISALLOWED);
		poisonArrow.setGravity(true);
		poisonArrow.setShooter(player);
		poisonArrow.setVelocity(eyeLocation.getDirection().multiply(2));

		PotionEffect poisoneffect = new PotionEffect(PotionEffectType.POISON, 100, 1);
		poisonArrow.addCustomEffect(poisoneffect, true);
	}


		//Arrow arrowProjectile = (Arrow) event.getProjectile();
        // arrowProjectile.setPickupStatus(PickupStatus.DISALLOWED);
        // if (arrowProjectile.isShotFromCrossbow()) {
        //     arrowProjectile.setGravity(false);
        //     arrowProjectile.setDamage(0.5);
        // }

	public void poisonArrow2(Player player) {

		Location eyeLocation = player.getEyeLocation();
		World world = player.getWorld();
		Arrow poisonArrow = (Arrow) world.spawnEntity(eyeLocation, EntityType.ARROW);
		poisonArrow.setPickupStatus(PickupStatus.DISALLOWED);
		poisonArrow.setShooter(player);
		poisonArrow.setVelocity(eyeLocation.getDirection().multiply(2));

		PotionEffect poisonEffect = new PotionEffect(PotionEffectType.POISON, 60, 2);
		poisonArrow.addCustomEffect(poisonEffect, true);

	}

	public void fireArrow(Player player, int level) {

		Location eyeLocation = player.getEyeLocation();
		World world = player.getWorld();

		Arrow fireArrow = (Arrow) world.spawnEntity(eyeLocation, EntityType.ARROW);
		fireArrow.setShooter(player);
		fireArrow.setVelocity(eyeLocation.getDirection().multiply(2));
	}

	@EventHandler
	public void onShootArrow(EntityShootBowEvent event) {
		if ( ! (event.getEntity() instanceof Player) ) {
			return;
		}
		Player player = (Player) event.getEntity();

		HuntsmanBow bow = getItem(event.getBow());
		if (bow == null) {
			return;
		}

		Arrow arrowProjectile = (Arrow)event.getProjectile();
		arrowProjectile.setPickupStatus(PickupStatus.DISALLOWED);
		updateArrowCount(player);
		if (bow.getLevel().getType() == HuntsmanBowLevels.LevelBranch.POISON) {

		}
		if (bow.getLevel() == HuntsmanBowLevels.POISON1) {
			poisonArrow1(player);
		}
		if (bow.getLevel() == HuntsmanBowLevels.POISON2) {
			poisonArrow2(player);
		}
	}

	private void updateArrowCount(Player player) {
		Inventory inventory = player.getInventory();

		ItemStack arrowItem = new ItemStack(Material.ARROW);
		inventory.remove(Material.ARROW);
		inventory.addItem(arrowItem);
	}

	@Override
	protected String getRoleId() {
		return HuntsmanRole.id;
	}

	@Override
	@Nullable
	protected HuntsmanBow getItem(ItemStack stack) {
		try {
			HuntsmanBow bow = new HuntsmanBow(stack);
			return bow;
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	@Override
	protected HuntsmanBow createItem(Player owner, HuntsmanBowLevels level) {
		return new HuntsmanBow(owner, level);
	}
	@Override
	protected HuntsmanBowLevels getDefaultLevel() {
		return HuntsmanBowLevels.BASE;
	}

}