/*
 * 01/11/2010
 *
 * Copyright (C) 2011 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://fifesoft.com/rsyntaxtextarea
 *
 * This code is licensed under the LGPL.  See the "license.txt" file included
 * with this project.
 */
package org.fife.rsta.ac.common;

import org.fife.rsta.ac.LanguageSupport;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Token;


/**
 * Returns non-whitespace, non-comment tokens from an {@link RSyntaxDocument},
 * one at a time.  This can be used by simplistic {@link LanguageSupport}s to
 * "parse" for simple, easily-identifiable tokens, such as curly braces and
 * {@link Token#VARIABLE}s.  For example, to identify code blocks for languages
 * structured like C and Java, you can use this class in conjunction with
 * {@link CodeBlock} and {@link VariableDeclaration} to create an
 * easily-parsable model of your source code.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class TokenScanner {

	private RSyntaxTextArea textArea;
	private Token t;
	private int line;


	public TokenScanner(RSyntaxTextArea textArea) {
		this.textArea = textArea;
		line = 0;
		t = null;//textArea.getTokenListForLine(line++);
	}


	/**
	 * Returns the next non-whitespace, non-comment token in the text area.
	 *
	 * @return The next token, or <code>null</code> if we are at the end of
	 *         its document.
	 */
	public Token next() {
		Token next = nextRaw();
		while (next!=null && (next.isWhitespace() || next.isComment())) {
			next = nextRaw();
		}
		return next;
	}


	/**
	 * Returns the next token in the text area.
	 *
	 * @return The next token, or <code>null</code> if we are at the end of
	 *         its document.
	 */
	private Token nextRaw() {
		if (t==null || !t.isPaintable()) {
			while (line<textArea.getLineCount() && (t==null || !t.isPaintable())) {
				t = textArea.getTokenListForLine(line++);
			}
			if (line==textArea.getLineCount()) {
				return null;
			}
		}
		Token next = t;
		t = t.getNextToken();
		return next;
	}


}