package fr.ludos.core.game;

/**
 * A {@link GameProcess} that can be setup and tore down, separately from being started or stopped, as well as report its initialization status.
 */
public interface TwoStepGameProcess extends GameProcess {
	void setUp();
	void tearDown();

	boolean isSetup();
}