// package fr.ludos.item.huntsman;

// import org.bukkit.Material;
// import org.bukkit.entity.Player;
// import org.bukkit.inventory.ItemStack;
// import org.bukkit.inventory.meta.BookMeta;
// import net.md_5.bungee.api.ChatColor;
// import net.md_5.bungee.api.chat.ClickEvent;
// import net.md_5.bungee.api.chat.ComponentBuilder;
// import net.md_5.bungee.api.chat.HoverEvent;
// import net.md_5.bungee.api.chat.hover.content.Text;
// import fr.ludos.item.SpecialItem;

// import javax.annotation.Nullable;

// public class HuntsmanLevelSelector extends SpecialItem {
// 	public HuntsmanLevelSelector(ItemStack stack) throws IllegalArgumentException {
// 		super(stack);
// 	}

// 	public HuntsmanLevelSelector(Player owner) {
// 		super(createBook(), owner);

// 	}

// 	private static ItemStack createBook() {
// 		ItemStack book = new ItemStack(Material.WRITTEN_BOOK);

// 		BookMeta meta = (BookMeta) book.getItemMeta();

// 		meta.setAuthor("");
// 		meta.setTitle("Bow Evolution Grimoire");
// 		ComponentBuilder builder = new ComponentBuilder();
// 		for (HuntsmanBowBranches bookLevel : HuntsmanBowBranches.values()) {
// 			builder
// 				.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/setbowbranch " + Integer.toString(bookLevel.index())))
// 				.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(bookLevel.getDescription())))
// 				.append(bookLevel.getName())
// 				.append("\n");
// 		}
// 		meta.spigot().addPage( builder.create() );

// 		book.setItemMeta(meta);

// 		return book;
// 	}

// 	@Override
// 	public String getId() {
// 		return "manhunt_huntsman_grimoire";
// 	}

// 	@Override
// 	protected String getName() {
// 		return "Bow Evolution Grimoire";
// 	}


// 	@Nullable
// 	public static HuntsmanLevelSelector getItem(ItemStack stack) {
// 		try {
// 			HuntsmanLevelSelector selector = new HuntsmanLevelSelector(stack);
// 			return selector;
// 		} catch (IllegalArgumentException e) {
// 			return null;
// 		}
// 	}

// 	public static HuntsmanLevelSelector createItem(Player owner) {
// 		HuntsmanBow bow = HuntsmanBow.findIn(owner.getInventory(), HuntsmanBow::getItem);
// 		if (bow == null) {
// 			return null;
// 		}

// 		return new HuntsmanLevelSelector(owner);
// 	}



// 	public static class Events extends SpecialItem.Events<HuntsmanLevelSelector> {
// 		public Events() {
// 			super(null);
// 		}

// 		@Override
// 		@Nullable
// 		protected HuntsmanLevelSelector getItem(ItemStack stack) {
// 			return HuntsmanLevelSelector.getItem(stack);
// 		}

// 		@Override
// 		protected HuntsmanLevelSelector createItem(Player owner) {
// 			return HuntsmanLevelSelector.createItem(owner);
// 		}
// 	}

// }