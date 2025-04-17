package fr.ludos.item.huntsman;

import fr.ludos.role.HuntsmanRole;
import fr.ludos.role.Role;
import fr.ludos.game.Game;
import fr.ludos.item.SpecialItem;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;

import java.util.List;
import javax.annotation.Nullable;

public class HuntsmanArrow extends SpecialItem {
	public HuntsmanArrow(ItemStack stack, Game game) {
		super(stack, game);
	}
	public HuntsmanArrow(Player owner, Game game) {
		super(new ItemStack(Material.ARROW), owner, game);
	}

	@Override
	public String getId() {
		return "manhuntHuntsmanArrow";
	}

	@Override
	protected String getName(){
		return "Stolen Arrow";
	}

	@Override
	public List<String> getLore(){
		return null;
	}


	public static class Events extends SpecialItem.Events<HuntsmanArrow> {
		private final Integer arrowMagazineSize;
		private final int reloadTime;

		public Events(Game game, Integer arrowMagazineSize, int reloadTime) {
			super(game);
			this.arrowMagazineSize = arrowMagazineSize;
			this.reloadTime = reloadTime;
		}

		public Events(Game game) {
			this(game, null, 20 * 3);
		}


		@EventHandler
		public void onShootArrow(EntityShootBowEvent event) {
			if (! (event.getEntity() instanceof Player player) ) return;
			if (! Role.isPlayerRole(player, HuntsmanRole.id)) return;

			ItemStack launcher = event.getBow();
			List<HuntsmanArrow> arrows = findAllIn(player.getInventory(), (ItemStack stack) -> getItem(stack, game));

			// prevent arrow consumption or restore count if not possible
			if (arrowMagazineSize == null) {
				event.setConsumeItem(false);
				player.updateInventory();

				if (launcher.getType() == Material.CROSSBOW) {
					if (arrows.size() == 0) {
						HuntsmanArrow newArrow = createItem(player, game);
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
			amount += java.util.Arrays.stream(player.getInventory().getContents())
				.filter(item -> item != null && item.getType() == Material.CROSSBOW)
				.mapToInt(item -> {
					if (item.getItemMeta() instanceof org.bukkit.inventory.meta.CrossbowMeta meta) {
						return (int) meta.getChargedProjectiles().stream()
							.filter(charged -> getItem(charged, game) != null)
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
					HuntsmanArrow arrow = createItem(player, game);
					arrow.getStack().setAmount(arrowMagazineSize);

					player.getInventory().addItem(arrow.getStack());
				}
			}.runTaskLater(game.getPlugin(), reloadTime);
		}


		@Override
		@Nullable
		protected HuntsmanArrow getItem(ItemStack stack, Game game) {
			try {
				HuntsmanArrow arrow = new HuntsmanArrow(stack, game);
				return arrow;
			} catch (IllegalArgumentException e) {
				return null;
			}
		}
		@Override
		protected HuntsmanArrow createItem(Player owner, Game game) {
			HuntsmanArrow arrow = new HuntsmanArrow(owner, game);
			arrow.getStack().setAmount(arrowMagazineSize == null ? 64 : arrowMagazineSize);
			return arrow;
		}
		@Override
		protected Boolean canPlayerHaveItem(HumanEntity owner) {
			return Role.isPlayerRole(owner, HuntsmanRole.id);
		}
	}
}