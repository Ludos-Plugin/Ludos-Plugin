package fr.ludos.item.assassin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.ludos.item.LevelItem;
import fr.ludos.item.SpecialItem;
import fr.ludos.role.Role;
import fr.ludos.role.AssassinRole;
import fr.ludos.game.Game;


public class AssassinDagger extends LevelItem<AssassinDaggerLevels> {
	public static final String ID = "assassin_dagger";

	public static @Nullable AssassinDagger fromItemStack(ItemStack stack, Game game) {
		UUID itemId = SpecialItem.getSpecialItemId(stack, ID, game);
		if (itemId == null) return null;

		Player owner = SpecialItem.getSpecialItemOwner(stack, game);
		if (owner == null) return null;

		LevelState levelState = LevelItem.levelFromItemStack(stack, game);
		if (levelState == null) levelState = new LevelState();

		return new AssassinDagger(stack, owner, levelState, game);
	}

	public static AssassinDagger createItem(Player owner, LevelState level, Game game) {
		AssassinDagger dagger = new AssassinDagger(new ItemStack(Material.IRON_SWORD), owner, level, game);
		dagger.initializeItem();
		return dagger;
	}

	public AssassinDagger(ItemStack stack, Player player, LevelState level, Game game) {
		super(AssassinDaggerLevels.class, stack, player, level, game);
	}

	@Override
	public String getTypeId() {
		return ID;
	}

	@Override
	protected Component getName() {
		return Component.text("Dague d'Assassin")
			.decoration(TextDecoration.ITALIC, false);
	}

	@Override
	public List<Component> getLore() {
		List<Component> lore = new ArrayList<>(Arrays.asList(
			Component.text("Inflige des dégâts augmentés").decoration(TextDecoration.ITALIC, false),
			Component.text("Ralentit l'ennemi frappé").decoration(TextDecoration.ITALIC, false)
		));
		lore.addAll(super.getLore());
		return lore;
	}


	public static class Events extends LevelItem.Events<AssassinDagger, AssassinDaggerLevels> {
		public Events(Game game) {
			super(game);
		}

		@EventHandler
		public void onDaggerHit(EntityDamageByEntityEvent event) {
			if (!(event.getDamager() instanceof Player player)) return;
			if (!Role.isPlayerRole(player, AssassinRole.id)) return;

			ItemStack itemInHand = player.getInventory().getItemInMainHand();
			AssassinDagger dagger = getItem(itemInHand, game);
			if (dagger == null) return;

			if (!(event.getEntity() instanceof LivingEntity target)) return;

			// Dégâts augmentés
			event.setDamage(event.getDamage() * 1.5);

			// Ralentissement si joueur
			if (target instanceof HumanEntity humanTarget) {
				humanTarget.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 3, 1));
			}

			// Poison si niveau suffisant
			if (dagger.getLvlObject().appliesPoison()) {
				target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 20 * 3, 0));
			}

			// Gain d'XP
			dagger.addXp(1.0);
		}

		@Override
		@Nullable
		protected AssassinDagger getItem(ItemStack stack, Game game) {
			return AssassinDagger.fromItemStack(stack, game);
		}

		@Override
		protected AssassinDagger createItem(Player owner, LevelState level, Game game) {
			return AssassinDagger.createItem(owner, level, game);
		}

		@Override
		protected Boolean canPlayerHaveItem(HumanEntity owner) {
			return Role.isPlayerRole(owner, AssassinRole.id);
		}
	}
}
