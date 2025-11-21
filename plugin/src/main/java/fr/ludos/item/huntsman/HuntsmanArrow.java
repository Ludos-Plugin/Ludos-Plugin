package fr.ludos.item.huntsman;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.scheduler.BukkitRunnable;

import fr.ludos.game.Game;
import fr.ludos.item.SpecialItem;
import fr.ludos.role.HuntsmanRole;
import fr.ludos.role.Role;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;


public class HuntsmanArrow extends SpecialItem {
	private final static String ID = "manhuntHuntsmanArrow";


	public static @Nullable HuntsmanArrow fromItemStack(ItemStack stack, Game game) throws IllegalArgumentException {
		Player owner = SpecialItem.getSpecialItemOwner(stack, ID, game);
		if (owner == null) return null;

		return new HuntsmanArrow(stack, owner, game);
	}
	public static HuntsmanArrow createItem(Player owner, Game game) {
		return new HuntsmanArrow(new ItemStack(Material.ARROW), owner, game);
	}

	protected HuntsmanArrow(ItemStack stack, Player owner, Game game) {
		super(stack, owner, game);
	}


	@Override
	public String getId() {
		return "manhuntHuntsmanArrow";
	}

	@Override
	protected Component getName(){
		return Component.text("Stolen Arrow")
			.decoration(TextDecoration.ITALIC, false);
	}

	@Override
	public List<Component> getLore(){
		return new ArrayList<>();
	}


	public static class Events extends SpecialItem.Events<HuntsmanArrow> {
		private final Integer arrowMagazineSize;
		private final int reloadTime;

		public Events(Game game, Integer arrowMagazineSize, int reloadTime) {
			super(game, 35);
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
			amount += Arrays.stream(player.getInventory().getContents())
				.filter(item -> item != null && item.getType() == Material.CROSSBOW)
				.mapToInt(item -> {
					if (item.getItemMeta() instanceof CrossbowMeta meta) {
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
			return HuntsmanArrow.fromItemStack(stack, game);
		}
		@Override
		protected HuntsmanArrow createItem(Player owner, Game game) {
			HuntsmanArrow arrow = HuntsmanArrow.createItem(owner, game);
			arrow.getStack().setAmount(arrowMagazineSize == null ? 64 : arrowMagazineSize);
			return arrow;
		}
		@Override
		protected Boolean canPlayerHaveItem(HumanEntity owner) {
			return Role.isPlayerRole(owner, HuntsmanRole.id);
		}
	}
}