package fr.ludos.core.game;

public interface TwoStepGameProcess extends GameProcess {
	void setdown();

	boolean isSetup();
}