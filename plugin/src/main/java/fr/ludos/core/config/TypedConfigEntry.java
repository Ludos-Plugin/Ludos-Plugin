package fr.ludos.core.config;

public final record TypedConfigEntry<T>(String key, TypedConfigOptions<T> options) implements ConfigEntryInterface {}
