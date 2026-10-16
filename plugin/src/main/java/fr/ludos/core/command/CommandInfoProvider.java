package fr.ludos.core.command;

/**
 * A command that provides extra information about iself.
 */
public interface CommandInfoProvider extends CommandUsageProvider {
	public abstract String getDescription();
	public abstract boolean requireOp();
}
