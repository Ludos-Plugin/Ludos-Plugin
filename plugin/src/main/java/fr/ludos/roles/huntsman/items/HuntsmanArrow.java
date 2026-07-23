package fr.ludos.roles.huntsman.items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
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
			super(game, new Events.Info(arrowMagazineSize == null
				? ItemSlot.TOP_9
				: ItemSlot.OFFHAND
			));
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


		private void reload(Player player, List<HuntsmanArrow> arrows) {
			if (player.getCooldown(Material.BOW) < reloadTime) player.setCooldown(Material.BOW, reloadTime);
			if (player.getCooldown(Material.CROSSBOW) < reloadTime) player.setCooldown(Material.CROSSBOW, reloadTime);
			if (player.getCooldown(Material.ARROW) < reloadTime) player.setCooldown(Material.ARROW, reloadTime);

			new BukkitRunnable() {
				@Override
				public void run() {
					PlayerInventory inventory = player.getInventory();
					for (HuntsmanArrow arrow : arrows) {
						inventory.remove(arrow.getStack());
						arrow.uncache();
					}
					createItem(player).give(player);
				}
			}.runTaskLater(game.getPlugin(), reloadTime);
		}

		@EventHandler
		public void onShootArrow(EntityShootBowEvent event) {
			if (! (event.getEntity() instanceof Player player) ) return;
			if (! isPlayerValid(player)) return;

			ItemStack launcher = event.getBow();
			List<HuntsmanArrow> arrows = findAll(player.getInventory(), this::getItem, ItemSlot.ARROW_ORDER);

			// prevent arrow consumption or restore count if not possible
			if (arrowMagazineSize == null) {
				event.setConsumeItem(false);
				player.updateInventory();

				if (launcher.getItemMeta() instanceof CrossbowMeta meta) {
					if (arrows.size() == 0) {
						createItem(player).give(player);
					}
					else {
						ItemStack firstStack = arrows.get(0).getStack();
						firstStack.setAmount(firstStack.getAmount() + 1);
					}

					HuntsmanArrow shotArrow = getItem(meta.getChargedProjectiles().get(0));
					// Uncache arrow that is about to be deleted, if its the last of its ItemId
					if (shotArrow != null && ! arrows.stream().map(SpecialItem::getItemId).collect(Collectors.toSet()).contains(shotArrow.getItemId())) {
						shotArrow.uncache();
					}
				}

				return;
			}

			consumeArrow(player, arrows);
		}

		private void consumeArrow(Player player, List<HuntsmanArrow> arrows) {
			int amount = countArrows(player, arrows);

			if (arrows.size() != 0) {
				HuntsmanArrow nextConsumed = arrows.get(0);
				if (nextConsumed.getStack().getAmount() <= 1) {
					nextConsumed.uncache();
					arrows.remove(0);
				}
			}

			if (amount - 1 > 0) return;

			reload(player, arrows);
		}
		private void manualReload(Player player) {
			PlayerInventory inventory = player.getInventory();
			List<HuntsmanArrow> arrows = findAll(inventory, this::getItem);

			int amount = countArrows(player, arrows);

			if (arrowMagazineSize != null && amount >= arrowMagazineSize) return;

			reload(player, arrows);
		}

		@EventHandler
		public void onPlayerReload(PlayerDropItemEvent event) {
			Player player = event.getPlayer();
			if (! isPlayerValid(player)) return;

			if (arrowMagazineSize == null) return;

			switch (event.getItemDrop().getItemStack().getType()) {
				case BOW:
				case CROSSBOW:
				case ARROW:
					event.setCancelled(true);
					manualReload(player);
					break;
				default:
					break;
			}
		}

		@Override
		protected HuntsmanArrow getItemInternal(ItemData info) {
			return new HuntsmanArrow(info, this);
		}

		@Override
		protected HuntsmanArrow createItemInternal(Player owner) {
			return new HuntsmanArrow(new ItemData(createStack(), owner), this);
		}

		private final ItemStack createStack() {
			if (arrowMagazineSize == null) {
				return new ItemStack(Material.ARROW, 64);
			}
			return new ItemStack(Material.ARROW, arrowMagazineSize);
		}

		@Override
		protected Boolean isPlayerValidInternal(OfflinePlayer owner) {
			return game.getLudos().getRoleManager().isPlayerRole(owner, HuntsmanRole.ID);
		}

		private int countArrows(Player player, List<HuntsmanArrow> arrows) {
			// count arrow stacks
			int amount = arrows.stream()
				.mapToInt(item -> item.getStack().getAmount())
				.sum();
			// count loaded arrows in crossbows
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
			return amount;
		}
	}
}
