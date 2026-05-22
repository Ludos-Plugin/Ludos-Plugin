package fr.ludos.role;

import java.util.EnumSet;

public enum RoleFlag {
	SUPPORT;

	public static final EnumSet<RoleFlag> ALL_OPTS = EnumSet.allOf(RoleFlag.class);
}