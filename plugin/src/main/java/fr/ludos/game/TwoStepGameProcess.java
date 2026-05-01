package fr.ludos.game;

public interface TwoStepGameProcess extends GameProcess {
	void setup();
	void setdown();

	boolean isSetup();
}