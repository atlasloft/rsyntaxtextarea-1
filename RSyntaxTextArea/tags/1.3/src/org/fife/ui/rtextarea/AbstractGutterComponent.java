/*
 * 02/17/2009
 *
 * AbstractGutterComponent.java - A component that can be displayed in a Gutter.
 * Copyright (C) 2009 Robert Futrell
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
package org.fife.ui.rtextarea;

import java.awt.Rectangle;
import java.awt.Shape;
import javax.swing.JComponent;
import javax.swing.text.View;


/**
 * A component that can be displayed in a {@link Gutter}.
 *
 * @author Robert Futrell
 * @version 1.0
 */
abstract class AbstractGutterComponent extends JComponent {

	/**
	 * The text area whose lines we are marking with icons.
	 */
	protected RTextArea textArea;


	/**
	 * Constructor.
	 *
	 * @param textArea The text area.
	 */
	public AbstractGutterComponent(RTextArea textArea) {
		setTextArea(textArea);
	}


	/**
	 * Returns the bounds of a child view as a rectangle, since
	 * <code>View</code>s tend to use <code>Shape</code>.
	 *
	 * @param parent The parent view of the child whose bounds we're getting.
	 * @param line The index of the child view.
	 * @param editorRect Returned from the text area's
	 *        <code>getVisibleEditorRect</code> method.
	 * @return The child view's bounds.
	 */
	protected static final Rectangle getChildViewBounds(View parent, int line,
										Rectangle editorRect) {
		Shape alloc = parent.getChildAllocation(line, editorRect);
		return alloc instanceof Rectangle ? (Rectangle)alloc :
										alloc.getBounds();
	}


	/**
	 * Sets the text area being displayed.  Subclasses can override.
	 *
	 * @param textArea The text area.
	 */
	public void setTextArea(RTextArea textArea) {
		this.textArea = textArea;
	}


}