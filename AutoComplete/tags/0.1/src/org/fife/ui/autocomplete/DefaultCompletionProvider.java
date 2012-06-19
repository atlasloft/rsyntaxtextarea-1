/*
 * 12/21/2008
 *
 * DefaultCompletionProvider.java - A basic completion provider implementation.
 * Copyright (C) 2008 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://fifesoft.com/rsyntaxtextarea
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA.
 */
package org.fife.ui.autocomplete;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Segment;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;


/**
 * A basic completion provider implementation.  This provider has no
 * understanding of language semantics.  It simply checks the text entered up
 * to the caret position for a match against known completions.  This is all
 * that is needed in the majority of cases.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class DefaultCompletionProvider extends AbstractCompletionProvider {

	protected Segment seg;


	/**
	 * Constructor.  The returned provider will not be aware of any completions.
	 *
	 * @see #addCompletion(Completion)
	 */
	public DefaultCompletionProvider() {
		init();
	}


	/**
	 * Creates a completion provider that provides completion for a simple
	 * list of words.
	 *
	 * @param words The words to offer as completion suggestions.  If this is
	 *        <code>null</code>, no completions will be known.
	 * @see WordCompletion
	 */
	public DefaultCompletionProvider(String[] words) {
		init();
		addWordCompletions(words);
	}


	/**
	 * Returns the text just before the current caret position that could be
	 * the start of something auto-completable.<p>
	 *
	 * This method returns all characters before the caret that are matched
	 * by  {@link #isValidChar(char)}.
	 *
	 * @param comp The text component.
	 * @return The text.
	 */
	public String getAlreadyEnteredText(JTextComponent comp) {
		
		Document doc = comp.getDocument();

		int dot = comp.getCaretPosition();
		Element root = doc.getDefaultRootElement();
		int index = root.getElementIndex(dot);
		Element elem = root.getElement(index);
		int start = elem.getStartOffset();
		int len = dot-start;
		try {
			doc.getText(start, len, seg);
		} catch (BadLocationException ble) {
			ble.printStackTrace();
			return EMPTY_STRING;
		}

		int segEnd = seg.offset + len;
		start = segEnd - 1;
		while (start>=seg.offset && isValidChar(seg.array[start])) {
			start--;
		}
		start++;

		len = segEnd - start;
		return len==0 ? EMPTY_STRING : new String(seg.array, start, len);

	}


	/**
	 * {@inheritDoc}
	 */
	public List getParameterizedCompletionsAt(JTextComponent tc) {

		List list = null;

		// If this provider doesn't support parameterized completions,
		// bail out now.
		char paramListStart = getParameterListStart();
		if (paramListStart==0) {
			return list; // null
		}

		int dot = tc.getCaretPosition();
		Segment s = new Segment();
		Document doc = tc.getDocument();
		Element root = doc.getDefaultRootElement();
		int line = root.getElementIndex(dot);
		Element elem = root.getElement(line);
		int offs = elem.getStartOffset();
		int len = dot - offs - 1/*paramListStart.length()*/;
		if (len<=0) { // Not enough chars on line for a method.
			return list; // null
		}

		try {

			doc.getText(offs, len, s);

			// Get the identifier preceding the '(', ignoring any whitespace
			// between them.
			offs = s.offset + len - 1;
			while (offs>=s.offset && Character.isWhitespace(s.array[offs])) {
				offs--;
			}
			int end = offs;
			while (offs>=s.offset && isValidChar(s.array[offs])) {
				offs--;
			}

			String text = new String(s.array, offs+1, end-offs);

			// Get a list of all Completions matching the text, but then
			// narrow it down to just the ParameterizedCompletions.
			List l = getCompletionByInputText(text);
			if (l!=null && !l.isEmpty()) {
				for (int i=0; i<l.size(); i++) {
					Object o = l.get(i);
					if (o instanceof ParameterizedCompletion) {
						if (list==null) {
							list = new ArrayList(1);
						}
						list.add(o);
					}
				}
			}

		} catch (BadLocationException ble) {
			ble.printStackTrace(); // Never happens
		}

		return list;

	}


	/**
	 * Initializes this completion provider.
	 */
	protected void init() {
		completions = new ArrayList();
		seg = new Segment();
	}


	/**
	 * Returns whether the specified character is valid in an auto-completion.
	 * The default implementation is equivalent to
	 * "<code>Character.isLetterOrDigit(ch) || ch=='_'</code>".  Subclasses
	 * can override this method to change what characters are matched.
	 *
	 * @param ch The character.
	 * @return Whether the character is valid.
	 */
	protected boolean isValidChar(char ch) {
		return Character.isLetterOrDigit(ch) || ch=='_';
	}


	/**
	 * Loads completions from an XML file.  The XML should validate against
	 * the completion XML schema.
	 *
	 * @param file An XML file to load from.
	 * @throws IOException If an IO error occurs.
	 */
	public void loadFromXML(File file) throws IOException {
		BufferedInputStream bin = new BufferedInputStream(
										new FileInputStream(file));
		try {
			loadFromXML(bin);
		} finally {
			bin.close();
		}
	}


	/**
	 * Loads completions from an XML input stream.  The XML should validate
	 * against the completion XML schema.
	 *
	 * @param in The input stream to read from.
	 * @throws IOException If an IO error occurs.
	 */
	public void loadFromXML(InputStream in) throws IOException {

		long start = System.currentTimeMillis();

		SAXParserFactory factory = SAXParserFactory.newInstance();
		CompletionXMLParser handler = new CompletionXMLParser(this);
		BufferedInputStream bin = new BufferedInputStream(in);
		try {
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(bin, handler);
			List completions =  handler.getCompletions();
			addCompletions(completions);
			char startChar = handler.getParamStartChar();
			if (startChar!=0) {
				char endChar = handler.getParamEndChar();
				String sep = handler.getParamSeparator();
				if (endChar!=0 && sep!=null && sep.length()>0) { // Sanity
					setParameterizedCompletionParams(startChar, sep, endChar);
				}
			}
		} catch (SAXException se) {
			throw new IOException(se.toString());
		} catch (ParserConfigurationException pce) {
			throw new IOException(pce.toString());
		} finally {
			long time = System.currentTimeMillis() - start;
			System.out.println("XML loaded in: " + time + "ms");
			bin.close();
		}

	}


	/**
	 * Loads completions from an XML file.  The XML should validate against
	 * the completion XML schema.
	 *
	 * @param resource A resource the current ClassLoader can get to.
	 * @throws IOException If an IO error occurs.
	 */
	public void loadFromXML(String resource) throws IOException {
		ClassLoader cl = getClass().getClassLoader();
		InputStream in = cl.getResourceAsStream(resource);
		if (in==null) {
			throw new IOException("No such resource: " + resource);
		}
		BufferedInputStream bin = new BufferedInputStream(in);
		try {
			loadFromXML(bin);
		} finally {
			bin.close();
		}
	}


}