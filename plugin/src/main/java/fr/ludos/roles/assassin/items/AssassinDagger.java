package fr.ludos.roles.assassin.items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.ludos.core.game.Game;
import fr.ludos.core.item.ItemSlot;
import fr.ludos.core.item.SpecialItem;
import fr.ludos.core.item.level.LevelItem;
import fr.ludos.roles.assassin.AssassinRole;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * Implementation of the Assassin Dagger, for use by any Player with {@link AssassinRole}.
 */
public class AssassinDagger extends LevelItem<AssassinDagger, AssassinDaggerLevels> {
	public static final String ID = "assassin_dagger";


	AssassinDagger(LevelItem.ItemData<AssassinDaggerLevels> info, Events events) {
		super(info, events);
	}

	@Override
	public Component getName() {
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

	/**
	 * Events for the {@link AssassinDagger}.
	 */
	public static class Events extends LevelItem.Events<AssassinDagger, AssassinDaggerLevels> {
		private static final List<AssassinDaggerLevels> LEVELS = List.of(AssassinDaggerLevels.values());

		public Events(Game game) {
			super(game, new Events.Info(ItemSlot.HOTBAR_1));
		}

		@Override
		public String getTypeId() {
			return ID;
		}

		@Override
		public List<AssassinDaggerLevels> getLevels() {
			return LEVELS;
		}

		@EventHandler
		public void onDaggerHit(EntityDamageByEntityEvent event) {
			if (! (event.getDamager() instanceof Player player)) return;
			if (! isPlayerValid(player)) return;

			ItemStack itemInHand = player.getInventory().getItemInMainHand();
			AssassinDagger dagger = getItem(itemInHand);
			if (dagger == null) return;

			if (!(event.getEntity() instanceof LivingEntity target)) return;

			// Dégâts augmentés
			event.setDamage(event.getDamage() * 1.5);

			// Ralentissement si joueur
			if (target instanceof HumanEntity humanTarget) {
				humanTarget.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 3, 1));
			}

			// Poison si niveau suffisant
			if (dagger.lvlObject().appliesPoison()) {
				target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 20 * 3, 0));
			}

			// Gain d'XP
			dagger.addXp(1.0);
		}

		@Override
		protected AssassinDagger getItemInternal(LevelItem.ItemData<AssassinDaggerLevels> info) {
			return new AssassinDagger(info, this);
		}

		@Override
		protected AssassinDagger createItemInternal(LevelData<AssassinDaggerLevels> data, Player owner) {
			return new AssassinDagger(new LevelItem.ItemData<>(data, new SpecialItem.ItemData(new ItemStack(Material.IRON_SWORD), owner)), this);
		}

		@Override
		protected Boolean isPlayerValidInternal(OfflinePlayer owner) {
			return game.getLudos().getRoleManager().isPlayerRole(owner, AssassinRole.ID);
		}
	}
}
