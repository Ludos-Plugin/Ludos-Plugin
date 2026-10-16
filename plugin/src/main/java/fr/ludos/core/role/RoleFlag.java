package fr.ludos.core.role;

import java.util.EnumSet;

/**
 * Enumeration of available feature/type flags for {@link Role}s, used in the system.
 */
public enum RoleFlag {
	SUPPORT;

	public static final EnumSet<RoleFlag> ALL_OPTS = EnumSet.allOf(RoleFlag.class);
}