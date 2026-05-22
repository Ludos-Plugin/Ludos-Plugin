package fr.ludos.command;

public interface CommandInfoProvider extends CommandUsageProvider {
	public abstract String getDescription();
	public abstract boolean requireOp();
}
