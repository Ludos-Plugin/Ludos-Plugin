package fr.ludos.core.command;

public interface CommandInfoProvider extends CommandUsageProvider {
	public abstract String getDescription();
	public abstract boolean requireOp();
}
