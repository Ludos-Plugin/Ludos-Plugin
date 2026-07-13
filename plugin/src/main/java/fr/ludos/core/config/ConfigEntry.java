package fr.ludos.core.config;

public final record ConfigEntry(String key, ConfigOptions options) implements ConfigEntryInterface {}
