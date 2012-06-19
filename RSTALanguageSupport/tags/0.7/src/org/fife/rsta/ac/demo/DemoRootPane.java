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
package org.fife.rsta.ac.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.fife.rsta.ac.LanguageSupport;
import org.fife.rsta.ac.LanguageSupportFactory;
import org.fife.rsta.ac.java.JavaLanguageSupport;
import org.fife.rsta.ac.java.rjc.ast.ASTNode;
import org.fife.rsta.ac.java.tree.JavaOutlineTree;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;


/**
 * The root pane used by the demos.  This allows both the applet and the
 * stand-alone application to share the same UI. 
 *
 * @author Robert Futrell
 * @version 1.0
 */
class DemoRootPane extends JRootPane implements HyperlinkListener,
							SyntaxConstants, Actions {

	private JavaOutlineTree tree;
	private RTextScrollPane scrollPane;
	private RSyntaxTextArea textArea;
	private Listener listener;


	public DemoRootPane() {

		listener = new Listener();

		LanguageSupportFactory lsf = LanguageSupportFactory.get();
		LanguageSupport support = lsf.getSupportFor(SYNTAX_STYLE_JAVA);
		JavaLanguageSupport jls = (JavaLanguageSupport)support;
		// TODO: This API will change!  It will be easier to do per-editor
		// changes to the build path.
		try {
			jls.getJarManager().addJar(null);
org.fife.rsta.ac.java.JarInfo ji = new org.fife.rsta.ac.java.JarInfo(new File("./bin"));
ji.setSourceLocation(new File("./src"));
jls.getJarManager().addJar(ji);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		textArea = createTextArea();
		setText("CExample.txt", SYNTAX_STYLE_C);
		scrollPane = new RTextScrollPane(textArea, true);
		scrollPane.setIconRowHeaderEnabled(true);
		scrollPane.getGutter().setBookmarkingEnabled(true);

		tree = new JavaOutlineTree();
		tree.addTreeSelectionListener(listener);
		tree.listenTo(textArea);
		JScrollPane treeSP = new JScrollPane(tree);

		//JPanel cp = new JPanel(new BorderLayout());
		JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
										treeSP, scrollPane);
		setContentPane(sp);

		setJMenuBar(createMenuBar());

	}


	private void addItem(Action a, ButtonGroup bg, JMenu menu) {
		JRadioButtonMenuItem item = new JRadioButtonMenuItem(a);
		bg.add(item);
		menu.add(item);
	}


	private JMenuBar createMenuBar() {

		JMenuBar mb = new JMenuBar();

		JMenu menu = new JMenu("File");
		menu.add(new JMenuItem(new OpenAction(this)));
		menu.addSeparator();
		menu.add(new JMenuItem(new ExitAction()));
		mb.add(menu);

		menu = new JMenu("Language");
		ButtonGroup bg = new ButtonGroup();
		addItem(new StyleAction(this, "C",      "CExample.txt",      SYNTAX_STYLE_C), bg, menu);
		addItem(new StyleAction(this, "Groovy", "GroovyExample.txt", SYNTAX_STYLE_GROOVY), bg, menu);
		addItem(new StyleAction(this, "Java",   "JavaExample.txt",   SYNTAX_STYLE_JAVA), bg, menu);
		addItem(new StyleAction(this, "Perl",   "PerlExample.txt",   SYNTAX_STYLE_PERL), bg, menu);
		addItem(new StyleAction(this, "HTML",   "HtmlExample.txt",   SYNTAX_STYLE_HTML), bg, menu);
		addItem(new StyleAction(this, "PHP",    "PhpExample.txt",    SYNTAX_STYLE_PHP), bg, menu);
		addItem(new StyleAction(this, "sh",     "ShellExample.txt",  SYNTAX_STYLE_UNIX_SHELL), bg, menu);
		menu.getItem(0).setSelected(true);
		mb.add(menu);

		menu = new JMenu("LookAndFeel");
		bg = new ButtonGroup();
		LookAndFeelInfo[] infos = UIManager.getInstalledLookAndFeels();
		for (int i=0; i<infos.length; i++) {
			addItem(new LookAndFeelAction(this, infos[i]), bg, menu);
		}
		mb.add(menu);

		menu = new JMenu("Help");
		menu.add(new JMenuItem(new AboutAction(this)));
		mb.add(menu);

		return mb;

	}


	/**
	 * Creates the text area for this application.
	 *
	 * @return The text area.
	 */
	private RSyntaxTextArea createTextArea() {
		RSyntaxTextArea textArea = new RSyntaxTextArea(25, 70);
		LanguageSupportFactory.get().register(textArea);
		textArea.setCaretPosition(0);
		textArea.addHyperlinkListener(this);
		textArea.requestFocusInWindow();
		textArea.setMarkOccurrences(true);
		textArea.setTextAntiAliasHint("VALUE_TEXT_ANTIALIAS_ON");
		ToolTipManager.sharedInstance().registerComponent(textArea);
		return textArea;
	}


	/**
	 * Focuses the text area.
	 */
	void focusTextArea() {
		textArea.requestFocusInWindow();
	}


	/**
	 * Called when a hyperlink is clicked in the text area.
	 *
	 * @param e The event.
	 */
	public void hyperlinkUpdate(HyperlinkEvent e) {
		if (e.getEventType()==HyperlinkEvent.EventType.ACTIVATED) {
			URL url = e.getURL();
			if (url==null) {
				UIManager.getLookAndFeel().provideErrorFeedback(null);
			}
			else {
				JOptionPane.showMessageDialog(this,
									"URL clicked:\n" + url.toString());
			}
		}
	}


	/**
	 * Opens a file in the editor (as opposed to one of the pre-defined
	 * code examples).
	 *
	 * @param file The file to open.
	 */
	public void openFile(File file) {
		try {
			BufferedReader r = new BufferedReader(new FileReader(file));
			textArea.read(r, null);
			textArea.setCaretPosition(0);
			r.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			UIManager.getLookAndFeel().provideErrorFeedback(this);
			return;
		}
	}


	/**
	 * Sets the content in the text area to that in the specified resource.
	 *
	 * @param resource The resource to load.
	 * @param style The syntax style to use when highlighting the text.
	 */
	void setText(String resource, String style) {

		textArea.setSyntaxEditingStyle(style);

		ClassLoader cl = getClass().getClassLoader();
		BufferedReader r = null;
		try {
			r = new BufferedReader(new InputStreamReader(
					cl.getResourceAsStream("examples/" + resource), "UTF-8"));
			textArea.read(r, null);
			r.close();
			textArea.setCaretPosition(0);
		} catch (RuntimeException re) {
			throw re; // FindBugs
		} catch (Exception e) {
			textArea.setText("Type here to see syntax highlighting");
		}

	}


	/**
	 * Listens for events in the text editor.
	 */
	private class Listener implements TreeSelectionListener {

		public void valueChanged(TreeSelectionEvent e) {
			// Select the item clicked in the tree in the editor.
			TreePath path = e.getNewLeadSelectionPath();
			if (path != null) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
						.getLastPathComponent();
				Object obj = node.getUserObject();
				if (obj instanceof ASTNode) {
					ASTNode astNode = (ASTNode) obj;
					int start = astNode.getNameStartOffset();
					int end = astNode.getNameEndOffset();
					textArea.select(start, end);
				}
			}
		}

	}


}