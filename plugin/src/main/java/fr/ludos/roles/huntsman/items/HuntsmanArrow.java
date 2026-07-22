package fr.ludos.roles.huntsman.items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.scheduler.BukkitRunnable;

import fr.ludos.core.game.Game;
import fr.ludos.core.item.ItemSlot;
import fr.ludos.core.item.SpecialItem;
import fr.ludos.roles.huntsman.HuntsmanRole;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * A simple non-throwable, infinite source of arrows, notably for {@link HuntsmanBow}s and {@link HuntsmanCrossbow}s.
 */
public class HuntsmanArrow extends SpecialItem<HuntsmanArrow> {
	public final static String ID = "huntsman_arrow";


	protected HuntsmanArrow(SpecialItem.ItemData info, Events events) {
		super(info, events);
	}


	@Override
	public Component getName(){
		return Component.text("Stolen Arrow")
			.decoration(TextDecoration.ITALIC, false);
	}

	@Override
	public List<Component> getLore(){
		return new ArrayList<>();
	}

	/**
	 * Events for the {@link HuntsmanArrow}.
	 */
	public static class Events extends SpecialItem.Events<HuntsmanArrow> {
		private final Integer arrowMagazineSize;
		private final int reloadTime;

		public Events(Game game, Integer arrowMagazineSize, int reloadTime) {
			super(game, new Events.Info(ItemSlot.BOT_9));
			this.arrowMagazineSize = arrowMagazineSize;
			this.reloadTime = reloadTime;
		}

		public Events(Game game) {
			this(game, null, 20 * 3);
		}

		@Override
		public String getTypeId() {
			return ID;
		}


		@EventHandler
		public void onShootArrow(EntityShootBowEvent event) {
			if (! (event.getEntity() instanceof Player player) ) return;
			if (! isPlayerValid(player)) return;

			ItemStack launcher = event.getBow();
			List<HuntsmanArrow> arrows = findAllIn(player.getInventory(), (ItemStack stack) -> getItem(stack));

			// prevent arrow consumption or restore count if not possible
			if (arrowMagazineSize == null) {
				event.setConsumeItem(false);
				player.updateInventory();

				if (launcher.getType() == Material.CROSSBOW) {
					if (arrows.size() == 0) {
						HuntsmanArrow newArrow = createItem(player);
						newArrow.getStack().setAmount(1);
						player.getInventory().addItem(newArrow.getStack());
					}
					else {
						ItemStack firstStack = arrows.get(0).getStack();
						firstStack.setAmount(firstStack.getAmount() + 1);
					}
				}

				return;
			}

			// count arrow stacks
			int amount = arrows.stream()
				.mapToInt(item -> item.getStack().getAmount())
				.sum();
			// count loaded arrows in crossbow
			amount += Arrays.stream(player.getInventory().getContents())
				.filter(item -> item != null && item.getType() == Material.CROSSBOW)
				.mapToInt(item -> {
					if (item.getItemMeta() instanceof CrossbowMeta meta) {
						return (int) meta.getChargedProjectiles().stream()
							.filter(charged -> getItem(charged) != null)
							.count();
					}
					return 0;
				})
				.sum();

			if (amount - 1 > 0) return;

			// reload
			if (player.getCooldown(Material.BOW) < reloadTime) player.setCooldown(Material.BOW, reloadTime);
			if (player.getCooldown(Material.CROSSBOW) < reloadTime) player.setCooldown(Material.CROSSBOW, reloadTime);
			if (player.getCooldown(Material.ARROW) < reloadTime) player.setCooldown(Material.ARROW, reloadTime);

			new BukkitRunnable() {
				@Override
				public void run() {
					HuntsmanArrow arrow = createItem(player);
					arrow.getStack().setAmount(arrowMagazineSize);

					player.getInventory().addItem(arrow.getStack());
				}
			}.runTaskLater(game.getPlugin(), reloadTime);
		}

		@Override
		protected HuntsmanArrow getItemInternal(ItemData info) {
			return new HuntsmanArrow(info, this);
		}

		@Override
		protected HuntsmanArrow createItemInternal(Player owner) {
			return new HuntsmanArrow(new ItemData(new ItemStack(Material.ARROW), owner), this);
		}

		@Override
		protected Boolean isPlayerValidInternal(OfflinePlayer owner) {
			return game.getLudos().getRoleManager().isPlayerRole(owner, HuntsmanRole.ID);
		}
	}
}
