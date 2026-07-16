package fr.ludos.core.book;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.internal.parser.node.TagNode;
import net.kyori.adventure.text.minimessage.internal.parser.node.TagPart;
import net.kyori.adventure.text.minimessage.internal.parser.node.TextNode;
import net.kyori.adventure.text.minimessage.internal.parser.node.ValueNode;
import net.kyori.adventure.text.minimessage.tree.Node;
import net.kyori.adventure.text.minimessage.tree.Node.Root;


final class PageTruncator {
	private final MiniMessage mm = MiniMessage.miniMessage();

	protected final int maxLineWidth;
	protected int currentLineWidth = 0;
	protected final StringBuilder currentLine = new StringBuilder();
	protected final ArrayList<String> lineParts = new ArrayList<>();

	private final int maxLineCount;
	private int currentPageHeight = 0;
	private TextComponent.Builder currentPage = Component.text();
	protected final ArrayList<TextComponent> pages = new ArrayList<>();

	protected PageTruncator(int maxLineWidth, int maxLineCount) {
		this.maxLineWidth = maxLineWidth;
		this.maxLineCount = maxLineCount;
	}

	public static TextComponent[] truncate(TextComponent text) {
		return truncate(text, BookUtility.MC_BOOK_LINE_WIDTH, BookUtility.MC_BOOK_LINE_COUNT);
	}
	public static TextComponent[] truncate(TextComponent text, int maxLineWidth, int maxLineCount) {
		PageTruncator trunc = new PageTruncator(maxLineWidth, maxLineCount);
		return trunc.process(text);
	}

	public TextComponent[] process(TextComponent component) {
		Root tree = mm.deserializeToTree(mm.serialize(component));

		truncateNode(tree, false, false);
		if (currentLineWidth > 0) {
			flushCurrentLine();
		}
		if (lineParts.size() > 0) {
			endCurrentLine();
		}
		if (currentPageHeight > 0) {
			flushCurrentPage();
		}

		return pages.toArray(new TextComponent[pages.size()]);
	}

	private void truncateNode(Node node, boolean isInsideTag, boolean isBold) {
		if (node instanceof ValueNode text) {
			truncateValueNode(text, isInsideTag, isBold);
		}
		else if (node instanceof TagNode tag) {
			truncateTagNode(tag, isInsideTag, isBold);
		}
		else {
			for (Node child : node.children()) {
				truncateNode(child, isInsideTag, isBold);
			}
		}
	}

	private void truncateValueNode(ValueNode node, boolean isInsideTag, boolean isBold) {
		final String value = node.value();

		int wordStart = 0;
		int wordEnd = -1;

		boolean shouldBreakWideWord = currentLineWidth == 0;

		for (int i = 0; i < value.length(); i++) {
			final char character = value.charAt(i);
			final String word;
			final int charPixelWidth;

			switch (character) {
				case ' ':
					if (wordEnd >= wordStart) {
						word = value.substring(wordStart, wordEnd + 1);
						appendWord(word, isBold);
					}

					charPixelWidth = BookUtility.getPixelWidth(character, isBold);
					if (currentLineWidth + charPixelWidth + 1 > maxLineWidth) {
						flushCurrentLine();
						shouldBreakWideWord = true;
					} else {
						currentLine.append(character);
						currentLineWidth += charPixelWidth + 1;
						shouldBreakWideWord = false;
					}

					wordStart = i + 1;

					break;
				case '\n':
					if (wordEnd >= wordStart) {
						word = value.substring(wordStart, wordEnd + 1);
						appendWord(word, isBold);
					}

					flushCurrentLine();
					shouldBreakWideWord = true;
					if (!isInsideTag) {
						endCurrentLine();
					}

					wordStart = i + 1;

					break;
				default:
					if (shouldBreakWideWord && wordEnd >= wordStart) {
						word = value.substring(wordStart, wordEnd + 1);
						final int currentWordWidth = BookUtility.getPixelWidth(word, isBold);
						charPixelWidth = BookUtility.getPixelWidth(character, isBold);
						if (currentWordWidth + 1 + charPixelWidth > maxLineWidth) {
							currentLine.append(word);
							flushCurrentLine();
							wordStart = i;
						}
					}
					wordEnd = i;
					break;
			}
		}

		if (wordEnd >= wordStart) {
			final String word = value.substring(wordStart, wordEnd + 1);
			appendWord(word, isBold);
		}
	}

	private void truncateTagNode(TagNode tagNode, boolean isInsideTag, boolean isBold) {
		final String tagName = tagNode.name();

		if (tagName.equals("newline") || tagName.equals("br")) {
			flushCurrentLine();
			return;
		}
		isBold = BookUtility.isTagNodeBold(tagNode, isBold);

		currentLine.append('<').append(tagName);

		final int partsSize = tagNode.parts().size();
		for (int i = 1; i < partsSize; i++) {
			final TagPart part = tagNode.parts().get(i);
			currentLine.append(':');
			currentLine.append('\'').append(part.value()).append('\'');
		}
		currentLine.append('>');

		for (Node child : tagNode.children()) {
			truncateNode(child, true, isBold);
		}

		currentLine.append("</").append(tagName).append('>');
	}

	private void flushCurrentPage() {
		pages.add(currentPage.build());
		currentPage = Component.text();
		currentPageHeight = 0;
	}

	private void flushCurrentLine() {
		appendLinePart(currentLine.toString());
		currentLine.setLength(0);
		currentLineWidth = 0;
	}
	private void endCurrentLine() {
		StringBuilder sb = new StringBuilder();
		for (String lp : lineParts) {
			sb.append(lp).append('\n');
		}
		currentPage.append(mm.deserialize(sb.toString()));
		currentPageHeight += lineParts.size();
		lineParts.clear();
	}

	private void appendLinePart(String line) {
		if (currentPageHeight > 0 && currentPageHeight + lineParts.size() + 1 > maxLineCount) {
			flushCurrentPage();
		}

		lineParts.add(line);
	}

	private void appendWord(String word, boolean isBold) {
		final int wordPixelWidth = BookUtility.getPixelWidth(word, isBold);

		if (currentLineWidth > 0 && currentLineWidth + wordPixelWidth + 1 > maxLineWidth) {
			flushCurrentLine();
		}

		currentLine.append(word);
		currentLineWidth += wordPixelWidth + 1;
	}
}

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
	public static final int MC_LOWERCASE_F_CHAR_WIDTH = 4;
	public static final int MC_LOWERCASE_I_CHAR_WIDTH = 1;
	public static final int MC_LOWERCASE_K_CHAR_WIDTH = 4;
	public static final int MC_LOWERCASE_L_CHAR_WIDTH = 2;
	public static final int MC_LOWERCASE_T_CHAR_WIDTH = 3;
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
		put('f', MC_LOWERCASE_F_CHAR_WIDTH);
		put('i', MC_LOWERCASE_I_CHAR_WIDTH);
		put('k', MC_LOWERCASE_K_CHAR_WIDTH);
		put('l', MC_LOWERCASE_L_CHAR_WIDTH);
		put('t', MC_LOWERCASE_T_CHAR_WIDTH);
		put('{', MC_OPEN_CURLY_BRACKET_CHAR_WIDTH);
		put('}', MC_CLOSE_CURLY_BRACKET_CHAR_WIDTH);
		put('|', MC_PIPE_CHAR_WIDTH);
		put('~', MC_TILDE_CHAR_WIDTH);
	}};

	public static final int MC_BOOK_LINE_WIDTH = 114; // Width of a book line in pixels
	public static final int MC_BOOK_LINE_COUNT = 14; // Number of lines in a book page


	public static TextComponent createPaddingSpaces(int totalPadding) {
		return createPaddingSpaces((double) totalPadding);
	}
	public static TextComponent createPaddingSpaces(double totalPadding) {
		int spaceWidth = MC_SPACE_CHAR_WIDTH + 1;

		double totalSpaceWidths = totalPadding / spaceWidth;

		if (totalSpaceWidths < 1) {
			return Component.text(' ').decoration(TextDecoration.BOLD, totalSpaceWidths > 0.5);
		}

		int totalSpacesCount = (int) Math.floor(totalSpaceWidths);
		int boldSpacesCount = (int) ((totalSpaceWidths - totalSpacesCount) * spaceWidth);
		int normalSpacesCount = Math.max(totalSpacesCount - boldSpacesCount, 0);

		StringBuilder spaces = new StringBuilder(normalSpacesCount);
		for (int i = 0; i < normalSpacesCount; i++) {
			spaces.append(' ');
		}
		StringBuilder boldSpaces = new StringBuilder(boldSpacesCount);
		for (int i = 0; i < boldSpacesCount; i++) {
			boldSpaces.append(' ');
		}

		return Component.text()
			.append(Component.text(spaces.toString()))
			.append(Component.text(boldSpaces.toString()).decorate(TextDecoration.BOLD))
			.build();
	}

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
		int pixelWidth = 0;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);

			pixelWidth += getPixelWidth(c, bold) + 1;
		}
		return pixelWidth > 0 ? pixelWidth - 1 : 0; // Remove last spacing
	}

	public static boolean isTagNodeBold(TagNode tagNode, boolean isBold) {
		if (tagNode.name().equals("bold") || tagNode.name().equals("b")) {
			return tagNode.parts().size() == 1 || Boolean.parseBoolean(tagNode.parts().get(1).value());
		}
		else if (tagNode.name().equals("!bold") || tagNode.name().equals("!b")) {
			return false;
		}
		else if (tagNode.name().equals("reset")) {
			return false;
		}
		return isBold;
	}

	private static int getTagChildrenPixelWidth(Node node, boolean isBold) {
		int pixelWidth = 0;
		for (Node child : node.children()) {
			pixelWidth += getNodePixelWidth(child, isBold) + 1;
		}
		return pixelWidth > 0 ? pixelWidth - 1 : 0; // Remove last spacing
	}
	private static int getTagNodePixelWidth(TagNode tagNode, boolean isBold) {
		isBold = isTagNodeBold(tagNode, isBold);

		return getTagChildrenPixelWidth(tagNode, isBold);
	}
	private static int getNodePixelWidth(Node node, boolean isBold) {
		if (node instanceof TextNode text) {
			return getPixelWidth(text.value(), isBold);
		}
		else if (node instanceof TagNode tag) {
			return getTagNodePixelWidth(tag, isBold);
		}
		else {
			return getTagChildrenPixelWidth(node, isBold);
		}
	}

	public static int getPixelWidth(TextComponent component) {
		return getPixelWidth(component, false);
	}
	public static int getPixelWidth(TextComponent component, boolean isBold) {
		MiniMessage mm = MiniMessage.miniMessage();

		Root tree = mm.deserializeToTree(mm.serialize(component));

		return getNodePixelWidth(tree, isBold);
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

		int totalPadding = lineWidth - totalWidth - 2;
		return Component.text()
			.append(leftComponent)
			.append(createPaddingSpaces(totalPadding))
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

		int totalPadding = lineWidth - textWidth - 1;

		return Component.text()
			.append(createPaddingSpaces(totalPadding))
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

		double totalPadding = (lineWidth - textWidth - 1) / 2 - 1;

		return Component.text()
			.append(createPaddingSpaces(totalPadding))
			.append(component)
			.append(Component.text('\n'))
			.build();
	}

	public static TextComponent[] truncatePage(TextComponent component) {
		return truncatePage(component, MC_BOOK_LINE_WIDTH, MC_BOOK_LINE_COUNT);
	}
	public static TextComponent[] truncatePage(TextComponent component, int lineWidth, int lineCount) {
		return PageTruncator.truncate(component, lineWidth, lineCount);
	}
}
