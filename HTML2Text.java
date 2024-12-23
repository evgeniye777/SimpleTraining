package com.kamaz.technology.apps.utils;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Stack;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

public class HTML2Text extends HTMLEditorKit.ParserCallback {
	private final StringBuffer stringBuffer = new StringBuffer();

	private final Stack<IndexType> indentStack = new Stack<>();

	public static class IndexType {
		public String type;
		public int counter;

		public IndexType(String type) {
			this.type = type;
		}
	}

	public static String convert(String html) {
		HTML2Text html2Text = new HTML2Text();

		try (Reader reader = new StringReader(html)) {
			html2Text.parse(reader);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return html2Text.getText();
	}

	public void parse(Reader reader) throws IOException {
		ParserDelegator parserDelegator = new ParserDelegator();
		parserDelegator.parse(reader, this, Boolean.TRUE);
	}

	@Override
	public void handleText(char[] text, int pos) {
		stringBuffer.append(text);
	}

	@Override
	public void handleStartTag(HTML.Tag htmlTag, MutableAttributeSet mutableAttributeSet, int position) {
		String htmlTagString = htmlTag.toString();

		if (htmlTagString.equals("p")) {
			if (stringBuffer.length() > 0 && !stringBuffer.substring(stringBuffer.length() - 1).equals("\n")) {
				newLine();
			}
			newLine();
		} else if (htmlTagString.equals("ol")) {
			indentStack.push(new IndexType("ol"));
			newLine();
		} else if (htmlTagString.equals("ul")) {
			indentStack.push(new IndexType("ul"));
			newLine();
		} else if (htmlTagString.equals("li")) {
			IndexType parent = indentStack.peek();

			if (parent.type.equals("ol")) {
				String numberString = "" + (++parent.counter) + ".";

				stringBuffer.append(numberString);

				for (int i = 0; i < 4 - numberString.length(); i++) {
					stringBuffer.append(" ");
				}
			} else {
				stringBuffer.append("*   ");
			}

			indentStack.push(new IndexType("li"));
		} else if (htmlTagString.equals("dl")) {
			newLine();
		} else if (htmlTagString.equals("dt")) {
			newLine();
		} else if (htmlTagString.equals("dd")) {
			indentStack.push(new IndexType("dd"));
			newLine();
		}
	}

	private void newLine() {
		stringBuffer.append("\n");
		for (int i = 0; i < indentStack.size(); i++) {
			stringBuffer.append("    ");
		}
	}

	@Override
	public void handleEndTag(HTML.Tag htmlTag, int position) {
		String htmlTagString = htmlTag.toString();

		if (htmlTagString.equals("p")) {
			newLine();
		} else if (htmlTagString.equals("ol")) {
			indentStack.pop();
			newLine();
		} else if (htmlTagString.equals("ul")) {
			indentStack.pop();
			newLine();
		} else if (htmlTagString.equals("li")) {
			indentStack.pop();
			newLine();
		} else if (htmlTagString.equals("dd")) {
			indentStack.pop();
		}
	}

	@Override
	public void handleSimpleTag(HTML.Tag htmlTag, MutableAttributeSet mutableAttributeSet, int position) {
		String htmlTagString = htmlTag.toString();

		if (htmlTagString.equals("br")) {
			newLine();
		}
	}

	public String getText() {
		return stringBuffer.toString();
	}
}