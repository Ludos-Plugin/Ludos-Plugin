package fr.ludos.book;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.bukkit.plugin.java.JavaPlugin;

import fr.ludos.Ludos;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.internal.parser.node.TagNode;
import net.kyori.adventure.text.minimessage.internal.parser.node.TagPart;
import net.kyori.adventure.text.minimessage.internal.parser.node.TextNode;
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslator;
import net.kyori.adventure.text.minimessage.tree.Node;
import net.kyori.adventure.text.minimessage.tree.Node.Root;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class BookUtility {
	public static final int MC_CHAR_WIDTH_DEFAULT = 5; // Default width for unknown characters
	public static final int MC_SPACE_CHAR_WIDTH = 3;
	public static final int MC_EXCLAMATION_CHAR_WIDTH = 1;
	public static final int MC_QUOTE_CHAR_WIDTH = 3;
	public static final int MC_APOSTROPHE_CHAR_WIDTH = 1;
	public static final int MC_OPEN_PARENTHESIS_CHAR_WIDTH = 3;
	public static final int MC_CLOSE_PARENTHESIS_CHAR_WIDTH = 3;
	public static final int MC_ASTERISK_CHAR_WIDTH = 3;
	public static final int MC_COMMA_CHAR_WIDTH = 1;
	public static final int MC_PERIOD_CHAR_WIDTH = 1;
	public static final int MC_COLON_CHAR_WIDTH = 1;
	public static final int MC_SEMICOLON_CHAR_WIDTH = 1;
	public static final int MC_OPEN_ANGLE_BRACKET_CHAR_WIDTH = 4;
	public static final int MC_CLOSE_ANGLE_BRACKET_CHAR_WIDTH = 4;
	public static final int MC_AT_CHAR_WIDTH = 6;
	public static final int MC_I_CHAR_WIDTH = 3;
	public static final int MC_OPEN_BRACKET_CHAR_WIDTH = 3;
	public static final int MC_CLOSE_BRACKET_CHAR_WIDTH = 3;
	public static final int MC_BACKTICK_CHAR_WIDTH = 2;
	public static final int MC_f_CHAR_WIDTH = 4;
	public static final int MC_i_CHAR_WIDTH = 1;
	public static final int MC_k_CHAR_WIDTH = 4;
	public static final int MC_l_CHAR_WIDTH = 2;
	public static final int MC_t_CHAR_WIDTH = 3;
	public static final int MC_OPEN_CURLY_BRACKET_CHAR_WIDTH = 3;
	public static final int MC_CLOSE_CURLY_BRACKET_CHAR_WIDTH = 3;
	public static final int MC_PIPE_CHAR_WIDTH = 1;
	public static final int MC_TILDE_CHAR_WIDTH = 6;

	// Utility: Character width map for Minecraft's default font
	private static final Map<Character, Integer> MC_CHAR_WIDTH = new HashMap<>() {{
		put(' ', MC_SPACE_CHAR_WIDTH);
		put('!', MC_EXCLAMATION_CHAR_WIDTH);
		put('"', MC_QUOTE_CHAR_WIDTH);
		put('\'', MC_APOSTROPHE_CHAR_WIDTH);
		put('(', MC_OPEN_PARENTHESIS_CHAR_WIDTH);
		put(')', MC_CLOSE_PARENTHESIS_CHAR_WIDTH);
		put('*', MC_ASTERISK_CHAR_WIDTH);
		put(',', MC_COMMA_CHAR_WIDTH);
		put('.', MC_PERIOD_CHAR_WIDTH);
		put(':', MC_COLON_CHAR_WIDTH);
		put(';', MC_SEMICOLON_CHAR_WIDTH);
		put('<', MC_OPEN_ANGLE_BRACKET_CHAR_WIDTH);
		put('>', MC_CLOSE_ANGLE_BRACKET_CHAR_WIDTH);
		put('@', MC_AT_CHAR_WIDTH);
		put('I', MC_I_CHAR_WIDTH);
		put('[', MC_OPEN_BRACKET_CHAR_WIDTH);
		put(']', MC_CLOSE_BRACKET_CHAR_WIDTH);
		put('`', MC_BACKTICK_CHAR_WIDTH);
		put('f', MC_f_CHAR_WIDTH);
		put('i', MC_i_CHAR_WIDTH);
		put('k', MC_k_CHAR_WIDTH);
		put('l', MC_l_CHAR_WIDTH);
		put('t', MC_t_CHAR_WIDTH);
		put('{', MC_OPEN_CURLY_BRACKET_CHAR_WIDTH);
		put('}', MC_CLOSE_CURLY_BRACKET_CHAR_WIDTH);
		put('|', MC_PIPE_CHAR_WIDTH);
		put('~', MC_TILDE_CHAR_WIDTH);
	}};

	public static final int MC_BOOK_LINE_WIDTH = 114; // Width of a book line in pixels
	public static final int MC_BOOK_LINE_COUNT = 14; // Number of lines in a book page


	public static final int getPixelWidth(char c) {
		return getPixelWidth(c, false);
	}
	public static final int getPixelWidth(char c, boolean bold) {
		return MC_CHAR_WIDTH.getOrDefault(c, MC_CHAR_WIDTH_DEFAULT) + (bold ? 1 : 0); // +1 for bold
	}

	// Utility: Calculate pixel width of a string
	public static final int getPixelWidth(String s) {
		return getPixelWidth(s, false);
	}
	public static final int getPixelWidth(String s, boolean bold) {
		int width = 0;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			int charWidth = getPixelWidth(c, bold);
			width += charWidth + 1; // +1 for spacing
		}
		return width > 0 ? width - 1 : 0; // Remove last spacing
	}

	private static int getTagNodePixelWidth(TagNode tagNode, boolean bold) {
		boolean isBold = tagNode.name().equals("bold");

		int pixelWidth = 0;

		for (Node child : tagNode.children()) {
			pixelWidth += getNodePixelWidth(child, bold || isBold);
		}

		return pixelWidth;
	}

	private static int getNodePixelWidth(Node node, boolean bold) {
		if (node instanceof TextNode text) {
			return getPixelWidth(text.value(), bold);
		}
		else if (node instanceof TagNode tag) {
			return getTagNodePixelWidth(tag, bold);
		}
		else {
			int pixelWidth = 0;
			for (Node child : node.children()) {
				pixelWidth += getNodePixelWidth(child, bold);
			}
			return pixelWidth;
		}
	}

	public static int getPixelWidth(TextComponent component) {
		return getPixelWidth(component, false);
	}
	public static int getPixelWidth(TextComponent component, boolean parentBold) {
		MiniMessage mm = MiniMessage.miniMessage();

		Root tree = mm.deserializeToTree(mm.serialize(component));

		return getNodePixelWidth(tree, parentBold);
	}

	public static TextComponent spaceBookLine(TextComponent leftComponent, TextComponent rightComponent) {
		return spaceBookLine(leftComponent, rightComponent, MC_BOOK_LINE_WIDTH);
	}
	public static TextComponent spaceBookLine(TextComponent leftComponent, TextComponent rightComponent, int lineWidth) {
		int leftWidth = getPixelWidth(leftComponent);
		int rightWidth = getPixelWidth(rightComponent);
		int totalWidth = leftWidth + rightWidth;

		if (totalWidth >= lineWidth) {
			return Component.text()
				.append(leftComponent)
				.append(Component.text(" "))
				.append(rightComponent)
				.append(Component.text('\n'))
				.build();
		}

		int spaceWidth = MC_SPACE_CHAR_WIDTH + 1;
		int totalPadding = lineWidth - totalWidth - 2;
		double spaces = (double) totalPadding / spaceWidth;
		int fullSpaces = (int) (spaces);

		int extraSpacePixels = (int) ((spaces - fullSpaces) * spaceWidth);

		StringBuilder normalSpaces = new StringBuilder();
		for (int i = 0; i < fullSpaces - extraSpacePixels; i++) {
			normalSpaces.append(' ');
		}
		StringBuilder boldSpaces = new StringBuilder();
		for (int i = 0; i < extraSpacePixels; i++) {
			boldSpaces.append(' ');
		}
		return Component.text()
			.append(leftComponent)
			.append(Component.text(normalSpaces.toString()))
			.append(Component.text(boldSpaces.toString()).decorate(TextDecoration.BOLD))
			.append(rightComponent)
			.append(Component.text('\n'))
			.build();
	}

	public static TextComponent alignRightBookLine(TextComponent component) {
		return alignRightBookLine(component, MC_BOOK_LINE_WIDTH);
	}

	public static TextComponent alignRightBookLine(TextComponent component, int lineWidth) {
		int textWidth = getPixelWidth(component);
		if (textWidth >= lineWidth) return component;

		int spaceWidth = MC_SPACE_CHAR_WIDTH + 1;
		int totalPadding = lineWidth - textWidth - 1;
		double spaces = (double) totalPadding / spaceWidth;
		int fullSpaces = (int) (spaces);

		int extraSpacePixels = (int) ((spaces - fullSpaces) * spaceWidth);

		StringBuilder normalSpaces = new StringBuilder();
		for (int i = 0; i < fullSpaces - extraSpacePixels; i++) {
			normalSpaces.append(' ');
		}
		StringBuilder boldSpaces = new StringBuilder();
		for (int i = 0; i < extraSpacePixels; i++) {
			boldSpaces.append(' ');
		}
		return Component.text()
			.append(Component.text(normalSpaces.toString()))
			.append(Component.text(boldSpaces.toString()).decorate(TextDecoration.BOLD))
			.append(component)
			.append(Component.text('\n'))
			.build();
	}

	// Center a TextComponent line, considering decorations (bold, italic)
	public static TextComponent centerBookLine(TextComponent component) {
		return centerBookLine(component, MC_BOOK_LINE_WIDTH);
	}

	public static TextComponent centerBookLine(TextComponent component, int lineWidth) {
		int textWidth = getPixelWidth(component);
		if (textWidth >= lineWidth) return component;

		int spaceWidth = MC_SPACE_CHAR_WIDTH + 1;
		int totalPadding = lineWidth - textWidth;
		double padding = totalPadding / 2.0;
		double spaces = padding / spaceWidth;
		int fullSpaces = (int) (spaces);

		int extraSpacePixels = (int) ((spaces - fullSpaces) * spaceWidth);

		StringBuilder normalSpaces = new StringBuilder();
		for (int i = 0; i < fullSpaces - extraSpacePixels; i++) {
			normalSpaces.append(' ');
		}
		StringBuilder boldSpaces = new StringBuilder();
		for (int i = 0; i < extraSpacePixels; i++) {
			boldSpaces.append(' ');
		}
		return Component.text()
			.append(Component.text(normalSpaces.toString()))
			.append(Component.text(boldSpaces.toString()).decorate(TextDecoration.BOLD))
			.append(component)
			.append(Component.text('\n'))
			.build();
	}

	private static int truncateTextNode(StringBuilder builder, List<String> lines, TextNode textNode, int currentLineLength, int maxLineLength) {
		String text = textNode.value();

		String[] textLines = text.split("\n");

		for (int i = 0; i < textLines.length; i++) {
			String line = textLines[i];

			String[] words = line.split(" ");

			if (words.length == 0) {
				builder.append(line);
				currentLineLength += getPixelWidth(line);
				continue;
			}

			boolean firstWord = true;
			for (String word : words) {
				int wordLength = getPixelWidth(word);
				if (!firstWord) {
					wordLength += MC_SPACE_CHAR_WIDTH + 1;
				}

				if (currentLineLength + wordLength <= maxLineLength) {
					if (!firstWord) {
						builder.append(' ');
						currentLineLength += MC_SPACE_CHAR_WIDTH + 1;
					}
					else {
						firstWord = false;
					}
				} else {
					if (builder.length() > 0) {
						lines.add(builder.toString());
						builder.setLength(0);
						currentLineLength = 0;
					}
				}

				builder.append(word);
				currentLineLength += wordLength;
			}

			if (i < textLines.length - 1) {
				lines.add(builder.toString());
				builder.setLength(0);
				currentLineLength = 0;
			}
		}

		return currentLineLength;
	}

	private static int truncateTagNode(StringBuilder builder, List<String> lines, TagNode tagNode, int currentLineLength, int maxLineLength) {
		if (tagNode.name().equals("br")) {
			lines.add(builder.toString());
			builder.setLength(0);
			return 0;
		}

		builder.append('<').append(tagNode.name());

		final int partsSize = tagNode.parts().size();
		for (int i = 1; i < partsSize; i++) {
			final TagPart part = tagNode.parts().get(i);
			builder.append(':');
			builder.append('\'').append(part.value()).append('\'');
		}
		builder.append('>');

		for (Node child : tagNode.children()) {
			currentLineLength = truncateNode(builder, lines, child, currentLineLength, maxLineLength);
		}

		builder.append("</").append(tagNode.name()).append('>');
		return currentLineLength;
	}

	private static int truncateNode(StringBuilder builder, List<String> lines, Node node, int currentLineLength, int maxLineLength) {
		if (node instanceof TextNode text) {
			return truncateTextNode(builder, lines, text, currentLineLength, maxLineLength);
		}
		else if (node instanceof TagNode tag) {
			return truncateTagNode(builder, lines, tag, currentLineLength, maxLineLength);
		}
		else {
			for (Node child : node.children()) {
				currentLineLength = truncateNode(builder, lines, child, currentLineLength, maxLineLength);
			}
			return currentLineLength;
		}
	}

	public static TextComponent[] truncatePage(TextComponent component, int lineWidth, int lineCount) {
		MiniMessage mm = MiniMessage.miniMessage();

		Root tree = mm.deserializeToTree(mm.serialize(component));

		StringBuilder builder = new StringBuilder();
		List<String> finalLines = new ArrayList<>();

		truncateNode(builder, finalLines, tree, 0, lineWidth);

		List<TextComponent> pages = new ArrayList<>();
		TextComponent.Builder pageBuilder = Component.text();
		int currentLineCount = 0;
		for (String line : finalLines) {
			if (currentLineCount > lineCount) {
				pages.add(pageBuilder.build());
				pageBuilder = Component.text();
				currentLineCount = 0;
			}
			pageBuilder.append(mm.deserialize(line)).append(Component.text("\n"));
			currentLineCount++;
		}

		if (currentLineCount > 0) {
			pages.add(pageBuilder.build());
		}

		return pages.toArray(new TextComponent[0]);
	}

	public static TextComponent[] truncatePage(TextComponent component) {
		return truncatePage(component, MC_BOOK_LINE_WIDTH, MC_BOOK_LINE_COUNT);
	}
}
