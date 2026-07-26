package fr.ludos.core.persistence.config.valueEntry;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.persistence.PersistentEntry;
import fr.ludos.core.persistence.config.ConfigNode;
import fr.ludos.core.persistence.config.ConfigNodeOperation;
import fr.ludos.core.persistence.config.sectionProvider.ConfigSectionContext;
import fr.ludos.core.persistence.serializer.Serializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.item.builder.AbstractItemBuilder;

/**
 * {@link ConfigNode} for flat, typed values.
 * @param <TComplex> The type of values, natively supported by this instance. Parsed to and from a String during command running.
 * @param <TPrimitive> The backing type that the value is converted to, before being set in the give {@link ConfigurationSection}
 */
public abstract class ConfigEntry<TComplex, TPrimitive> implements ConfigNode, PersistentEntry<TComplex> {
	public static final String DEFAULT = "default";

	private final @NotNull String key;
	private final @NotNull TextComponent name;
	private final @NotNull AbstractItemBuilder<?> displayItem;

	public ConfigEntry(@NotNull TextComponent name, AbstractItemBuilder<?> displayItem, @NotNull String key) {
		this.name = ObjectUtils.requireNonEmpty(name);
		this.displayItem = ObjectUtils.requireNonEmpty(displayItem);
		this.key = ObjectUtils.requireNonEmpty(key);
	}

	@Override
	public @NotNull String key() {
		return key;
	}
	@Override
	public @NotNull TextComponent name() {
		return name;
	}
	@Override
	public AbstractItemBuilder<?> item(Player player, ConfigSectionContext context) {
		return displayItem;
	}

	public abstract Serializer<TComplex, TPrimitive> getSerializer();


	public boolean execute(@NotNull String[] args, CommandSender sender, ConfigSectionContext context, ConfigNodeOperation op) {
		if (! context.isAuthorized(sender, op)) return false;
		ConfigurationSection config = context.getConfig(sender);
		switch (op) {
			case get:
				return get(args, sender, config);
			case set:
				if (args.length == 0) {
					if (! (sender instanceof Player player)) return false;
					openConfigWindow(player, context);
					return true;
				}
				return set(args, sender, config);
			case reset:
				return unset(args, sender, config);
			default:
				return false;
		}
	}

	protected boolean get(String[] args, CommandSender sender, ConfigurationSection config) {
		if (args.length == 0) {
			sender.sendMessage(getterMessage(config));
			return true;
		}
		return false;
	}
	protected boolean set(String[] args, CommandSender sender, ConfigurationSection config) {
		TComplex parsed = parseValueFromArgs(args, sender);
		if (parsed == null) return false;

		if (getSerializer().set(key, parsed, config)) {
			notifySet(parsed, sender);
			return true;
		}
		return false;
	}
	protected boolean unset(String[] args, CommandSender sender, ConfigurationSection config) {
		if (args.length == 0) {
			if (getSerializer().unset(key, config)) {
				notifyUnset(sender);
				return true;
			}
		}
		return false;
	}

	@Override
	public List<@NotNull String> tabComplete(@NotNull String[] args, CommandSender sender, ConfigNodeOperation mode) {
		switch (mode) {
			case get:
			case reset:
				return Collections.emptyList();
			case set:
				return setTabComplete(args, sender);
			default:
				return null;
		}
	}

	protected List<@NotNull String> setTabComplete(String[] args, CommandSender sender) {
		if (args.length <= 1) {
			return options(sender).stream().toList();
		}
		return null;
	}

	/**
	 * Parse the given args as a native T type.
	 * @param args The arguments passed for the command performed
	 * @param sender The Command Sender who performed the command
	 * @return A valid instance of T if the args were valid, or null.
	 */
	public TComplex parseValueFromArgs(@NotNull String[] args, CommandSender sender) {
		String val = args[0];
		TComplex parsed = getSerializer().fromString(val);
		if (! validateValue(parsed, sender)) return null;
		return parsed;
	}
	public boolean validateValue(TComplex value, CommandSender sender) {
		return true;
	}

	protected void notifyUnset(CommandSender sender) {
		sender.sendMessage(name().content() + " reset");
	}
	protected void notifySet(TComplex value, CommandSender sender) {
		String parsed = getSerializer().toString(value);
		if (parsed == null) {
			sender.sendMessage(name().content() + " set to irrepresentable value");
		}

		sender.sendMessage(name().content() + " set to " + parsed);
	}

	/**
	 * The message sent to the command sender, when no option value was given.<br>
	 * We use this to give the current set value to the user.<br>
	 * Must not be null.
	 * @param config The Configuration section to use as a root path for the fetching.
	 * @return A more detailed, if necessary, value to return to the player.
	 */
	public @NotNull String getterMessage(ConfigurationSection config) {
		String valueString = getSerializer().toString(getValueOrNull(config));
		return getterMessage(valueString);
	}
	/**
	 * The message sent to the command sender, when no option value was given.<br>
	 * We use this to give the current set value to the user.
	 * @param value The parsed String value that was fetched.
	 * @return A more detailed, if necessary, value to return to the player.
	 */
	public String getterMessage(String value) {
		if (value == null) {
			String defaultValue = getSerializer().toString(defaultValue());
			return defaultValue != null
				? DEFAULT + " (" + defaultValue + ')'
				: DEFAULT;
		}
		return value;
	}

	public TPrimitive serialize(TComplex value) {
		return getSerializer().serialize(value);
	}
	public TComplex parse(TPrimitive primitive) {
		return getSerializer().parse(primitive);
	}
	public String toString(TComplex value) {
		return getSerializer().toString(value);
	}
	public TComplex fromString(String string) {
		return getSerializer().fromString(string);
	}

	/**
	 * The default value that will be returned when fetching the value, when the option was not set, or after it was reset, using {@link #placeholderValue}.<br>
	 * Using {@link #defaultValue} with {@link #defaultValue} should NEVER result in null, for the sake of sender messages.
	 * @return The default value. Do not return null, unless the {@link #getValueOrDefault} call-sites are null-proof.
	 */
	public abstract @NotNull TComplex defaultValue();

	@Override
	public AbstractItemBuilder<?> displayItem(Player player, ConfigSectionContext context) {
		AbstractItemBuilder<?> builder = ConfigNode.super.displayItem(player, context);
		builder.setLore(List.of(
			new AdventureComponentWrapper(
				Component.text(getValueLabel(toString(getValueOrDefault(context.getConfig(player)))))
					.color(NamedTextColor.GRAY)
			)
		));
		return builder;
	}
	protected String getValueLabel(String value) {
		if (value == null) value = getNullValue();
		return "Current value : " + value;
	}
	protected String getNullValue() {
		return null;
	}
}
