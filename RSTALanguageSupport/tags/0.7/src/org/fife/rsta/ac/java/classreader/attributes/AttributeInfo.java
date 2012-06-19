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
package org.fife.rsta.ac.java.classreader.attributes;

import java.io.*;

import org.fife.rsta.ac.java.classreader.ClassFile;


public abstract class AttributeInfo {

	private ClassFile cf;
	public int attributeNameIndex; // u2


	protected AttributeInfo(ClassFile cf) {
		this.cf = cf;
	}


	public ClassFile getClassFile() {
		return cf;
	}


	/**
	 * Returns the name of this attribute.
	 *
	 * @return The name of this attribute.
	 */
	public String getName() {
		return cf.getUtf8ValueFromConstantPool(attributeNameIndex);
	}


	/**
	 * Reads an unknown/unsupported attribute from an input stream.
	 *
	 * @param cf The class file containing the attribute.
	 * @param in The input stream to read from.
	 * @param attrName The name of the attribute.
	 * @param attrLength The length of the data to read from <code>in</code>,
	 *        in bytes.
	 * @return The attribute.
	 * @throws IOException If an IO error occurs.
	 */
	public static UnsupportedAttribute readUnsupportedAttribute(ClassFile cf,
										DataInputStream in, String attrName,
										int attrLength) throws IOException {
		int[] info = new int[attrLength];
		for (int i=0; i<attrLength; i++) {
			info[i] = in.readUnsignedByte();
		}
		return  new UnsupportedAttribute(cf, attrName, info);
	}


	/**
	 * Fully skips a given number of bytes in an input stream.
	 *
	 * @param in The input stream.
	 * @param count The number of bytes to skip.
	 * @throws IOException If an IO error occurs.
	 */
	protected static void skipBytes(DataInputStream in, int count)
												throws IOException {
		int skipped = 0;
		while (skipped<count) {
			skipped += in.skipBytes(count-skipped);
		}
	}


}