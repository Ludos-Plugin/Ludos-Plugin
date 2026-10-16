package fr.ludos.core.wave;

import javax.annotation.Nullable;

import org.bukkit.scoreboard.Scoreboard;

import fr.ludos.core.game.Game;
import fr.ludos.core.group.Group;

/**
 * Represents a game that progresses in waves.
 */
public abstract class WaveGame extends Game {
	public abstract WaveController getWaveController();

	@Override
	public boolean isClear() {
		return super.isClear() && getWaveController().isClear();
	}

	protected WaveGame(Builder builder, Group group, @Nullable Scoreboard scoreboard) {
		super(builder, group, scoreboard);
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
