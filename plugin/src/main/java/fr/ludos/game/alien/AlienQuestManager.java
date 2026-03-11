package fr.ludos.game.alien;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;

import fr.ludos.game.GameProcessBase;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class AlienQuestManager extends GameProcessBase {
	private final AlienGame game;
	private final List<AlienQuest> quests = new ArrayList<>();

	public AlienQuestManager(AlienGame game) {
		this.game = game;
		this.quests.add(new AlienInteractBlockQuest(game));
	}

	@Override
	protected org.bukkit.plugin.java.JavaPlugin getPlugin() {
		return game.getPlugin();
	}

	@Override
	protected void onStart() {
		for (AlienQuest quest : quests) {
			quest.start();
		}
		Bukkit.broadcast(Component.text("Alien quests initialized: " + quests.size()).color(NamedTextColor.YELLOW));
	}

	@Override
	protected void onStop() {
		for (AlienQuest quest : quests) {
			quest.stop();
		}
	}

	public void checkCompletion() {
		boolean allCompleted = quests.stream().allMatch(AlienQuest::isCompleted);
		if (allCompleted) {
			game.onAllQuestsCompleted();
		}
	}
}