package fr.ludos.game.alien;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class AlienInteractBlockQuest implements AlienQuest {
	public static final String QUEST_ID = "interact_quest_block";
	public static final String QUEST_NAME = "Quest Block";
	public static final Material QUEST_MATERIAL = Material.BRICKS;

	private final AlienGame game;
	private boolean completed = false;

	public AlienInteractBlockQuest(AlienGame game) {
		this.game = game;
	}

	@Override
	public String getId() {
		return QUEST_ID;
	}

	@Override
	public String getDisplayName() {
		return QUEST_NAME;
	}

	@Override
	public boolean isCompleted() {
		return completed;
	}

	@Override
	public void start() {
		Bukkit.getPluginManager().registerEvents(this, game.getPlugin());

		Bukkit.broadcast(
				Component.text("Quest started: right-click a ")
						.append(Component.text(QUEST_NAME).color(NamedTextColor.GOLD))
						.append(Component.text(" made of "))
						.append(Component.text(QUEST_MATERIAL.name()).color(NamedTextColor.YELLOW)));
	}

	@Override
	public void stop() {
		HandlerList.unregisterAll(this);
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (completed) {
			return;
		}
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}

		Player player = event.getPlayer();
		if (!game.getTeamController().getLivingPlayers().contains(player)) {
			return;
		}

		Block block = event.getClickedBlock();
		if (block == null) {
			return;
		}
		if (block.getType() != QUEST_MATERIAL) {
			return;
		}

		completed = true;

		Bukkit.broadcast(
				Component.text(player.getName() + " completed the quest: ")
						.append(Component.text(QUEST_NAME).color(NamedTextColor.GREEN)));

		game.onAllQuestsCompleted();
	}
}