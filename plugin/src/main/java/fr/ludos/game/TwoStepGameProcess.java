package fr.ludos.game;

public interface TwoStepGameProcess extends GameProcess {
	void setdown();

	boolean isSetup();
}