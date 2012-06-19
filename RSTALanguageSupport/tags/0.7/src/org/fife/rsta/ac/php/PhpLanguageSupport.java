/*
 * 03/21/2010
 *
 * Copyright (C) 2010 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://fifesoft.com/rsyntaxtextarea
 *
 * This code is licensed under the LGPL.  See the "license.txt" file included
 * with this project.
 */
package org.fife.rsta.ac.php;

import javax.swing.ListCellRenderer;

import org.fife.rsta.ac.AbstractLanguageSupport;
import org.fife.rsta.ac.html.HtmlCellRenderer;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;


/**
 * Language support for PHP.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class PhpLanguageSupport extends AbstractLanguageSupport {

	/**
	 * The completion provider.  This is shared amongst all PHP text areas.
	 */
	private PhpCompletionProvider provider;


	/**
	 * Constructor.
	 */
	public PhpLanguageSupport() {
		setAutoActivationEnabled(true);
		setParameterAssistanceEnabled(true);
		setShowDescWindow(true);
	}


	/**
	 * {@inheritDoc}
	 */
	protected ListCellRenderer createDefaultCompletionCellRenderer() {
		return new HtmlCellRenderer();
	}


	/**
	 * Lazily creates the shared completion provider instance for PHP.
	 *
	 * @return The completion provider.
	 */
	private PhpCompletionProvider getProvider() {
		if (provider==null) {
			provider = new PhpCompletionProvider();
		}
		return provider;
	}


	/**
	 * {@inheritDoc}
	 */
	public void install(RSyntaxTextArea textArea) {

		PhpCompletionProvider provider = getProvider();
		AutoCompletion ac = createAutoCompletion(provider);
		ac.install(textArea);
		installImpl(textArea, ac);

		textArea.setToolTipSupplier(null);

	}


	/**
	 * {@inheritDoc}
	 */
	public void uninstall(RSyntaxTextArea textArea) {
		uninstallImpl(textArea);
	}


}