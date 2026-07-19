package fr.ludos.core.command.ludos.cheats;

import java.util.ArrayList;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import fr.ludos.core.command.HelpSubcommand;
import fr.ludos.core.command.Subcommand;
import fr.ludos.core.command.SubcommandHandler;
import fr.ludos.core.game.Game;
import fr.ludos.core.item.SpecialItem;

/**
 * Subcommand meant for Admins to cheat during Ludos games, for debugging purposes.
 */
public final class CheatsSubcommand extends SubcommandHandler {
	public CheatsSubcommand() {
		super("cheats", "Use Ludos cheats", true, getSubcommands());
	}

	private static final ArrayList<Subcommand> getSubcommands() {
		ArrayList<Subcommand> subcommands = new ArrayList<>() {{
			add(new CheatsLevel());
			add(new CheatsXp());
		}};
		HelpSubcommand help = new HelpSubcommand("cheats", subcommands);
		subcommands.add(help);
		return subcommands;
	}

	public static @Nullable Integer parsePositiveInt(String raw) {
		try {
			int value = Integer.parseInt(raw);
			return value > 0 ? value : null;
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public static @Nullable SpecialItem findHeldSpecialItem(Game game, ItemStack stack) {
		for (SpecialItem.Events<?> event : game.getActiveItems()) {
			SpecialItem item = event.getItem(stack);
			if (item != null) {
				return item;
			}
		}
		return null;
	}
}
