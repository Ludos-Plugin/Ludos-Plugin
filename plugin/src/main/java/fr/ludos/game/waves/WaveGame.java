package fr.ludos.game.waves;

import org.bukkit.Bukkit;

import fr.ludos.game.Game;
import fr.ludos.group.Group;

public abstract class WaveGame extends Game {
	public abstract WaveController getWaveController();

	@Override
	public boolean isClear() {
		return super.isClear() && getWaveController().isClear();
	}

	protected WaveGame(Builder builder, Group group) {
		super(builder, group, Bukkit.getServer().getScoreboardManager().getNewScoreboard());
	}


	@Override
	protected void onGameStart() {
		super.onGameStart();

		WaveController waveController = getWaveController();
		waveController.start();
		waveController.startWave();
	}

	@Override
	protected void onGameStop() {
		super.onGameStop();

		getWaveController().stop();
	}
}
