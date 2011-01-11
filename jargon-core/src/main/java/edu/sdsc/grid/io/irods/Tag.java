//
//  Copyright (c) 2008  San Diego Supercomputer Center (SDSC),
//  University of California, San Diego (UCSD), San Diego, CA, USA.
//
//  Users and possessors of this source code are hereby granted a
//  nonexclusive, royalty-free copyright and design patent license
//  to use this code in individual software.  License is not granted
//  for commercial resale, in whole or in part, without prior written
//  permission from SDSC/UCSD.  This source is provided "AS IS"
//  without express or implied warranty of any kind.
//
//
//  FILE
//  Rule.java  -  edu.sdsc.grid.io.irods.Rule
//
//  CLASS HIERARCHY
//  java.lang.Object
//      |
//      +-edu.sdsc.grid.io.irods.Rule
//
//
//  PRINCIPAL AUTHOR
//  Lucas Gilbert, SDSC/UCSD
//
//
package edu.sdsc.grid.io.irods;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the nested structure of the XML protocol for messages between
 * Jargon and IRODS
 */
public class Tag implements Cloneable {
	static final char OPEN_START_TAG = '<';
	static final char CLOSE_START_TAG = '>';
	static final String OPEN_END_TAG = "</";
	static final char CLOSE_END_TAG = '>';

	private static Logger log = LoggerFactory.getLogger(Tag.class);

	/**
	 * iRODS name of the tag
	 */
	String tagName;

	/**
	 * all the sub tags
	 */
	public Tag[] tags;

	/**
	 * probably a string...
	 */
	String value;

	public Tag(final String tagName) {
		this.tagName = tagName;
	}

	public Tag(final String tagName, final int value) {
		this.tagName = tagName;
		this.value = "" + value;
	}

	public Tag(final String tagName, final long value) {
		this.tagName = tagName;
		this.value = "" + value;
	}

	public Tag(final String tagName, final String value) {
		this.tagName = tagName;
		this.value = value;
	}

	public Tag(final String tagName, final Tag tag) {
		this(tagName, new Tag[] { tag });
	}

	public Tag(final String tagName, final Tag[] tags) {
		this.tagName = tagName;
		this.tags = tags;
	}

	public void setTagName(final String tagName) {
		this.tagName = tagName;
	}

	public void setValue(final int value) {
		this.value = "" + value;
	}

	public void setValue(final long value) {
		this.value = "" + value;
	}

	public void setValue(String value, final boolean decode) {
		if (value == null) {
			this.value = null;
			return;
		}
		if (decode) {
			// decode escaped characters
			value = value.replaceAll("&amp;", "&");
			value = value.replaceAll("&lt;", "<");
			value = value.replaceAll("&gt;", ">");
			value = value.replaceAll("&quot;", "\"");
			value = value.replaceAll("&apos;", "`");
		}
		this.value = value;
	}

	public Object getValue() {
		if (tags != null) {
			return tags.clone();
		} else {
			return value;
		}
	}

	public int getIntValue() {
		return Integer.parseInt(value);
	}

	public long getLongValue() {
		return Long.parseLong(value);
	}

	public String getStringValue() {
		return value;
	}

	public String getName() {
		return tagName;
	}

	public int getLength() {
		return tags.length;
	}

	public Tag getTag(final String tagName) {
		if (tags == null) {
			return null;
		}

		// see if tagName exists in first level
		// if it isn't the toplevel, just leave it.
		for (Tag tag : tags) {
			if (tag.getName().equals(tagName)) {
				return tag;
			}
		}
		return null;
	}

	/**
	 * Get the <code>index</code>-th sub-tag, from the first level down, with
	 * the name of <code>tagName</code>. Index count starts at zero.
	 * 
	 * So if tagname = taggy, and index = 2, get the 3rd subtag with the name of
	 * 'taggy'.
	 */
	public Tag getTag(final String tagName, final int index) {
		if (tags == null) {
			return null;
		}

		// see if tagName exists in first level
		// if it isn't the toplevel, just leave it.
		for (int i = 0, j = 0; i < tags.length; i++) {
			if (tags[i].getName().equals(tagName)) {
				if (index == j) {
					return tags[i];
				} else {
					j++;
				}
			}
		}
		return null;
	}

	public Tag[] getTags() {
		// clone so it can't over write when set value is called?
		if (tags != null) {
			return tags;
		} else {
			return null;
		}
	}

	/**
	 * Returns the values of this tags subtags. Which are probably more tags
	 * unless we've finally reached a leaf.
	 */
	public Object[] getTagValues() {
		if (tags == null) {
			return null;
		}

		Object[] val = new Object[tags.length];
		for (int i = 0; i < tags.length; i++) {
			val[i] = tags[i].getValue();
		}
		return val;
	}

	/**
	 * Convenience for addTag( new Tag(name, val) )
	 */
	public void addTag(final String name, final String val) {
		addTag(new Tag(name, val));
	}

	public void addTag(final Tag add) {
		if (tags != null) {
			Tag[] temp = tags;
			tags = new Tag[temp.length + 1];
			System.arraycopy(temp, 0, tags, 0, temp.length);
			tags[temp.length] = add;
		} else {
			tags = new Tag[] { add };
		}
	}

	public void addTags(final Tag[] add) {
		if (tags != null) {
			Tag[] temp = tags;
			tags = new Tag[temp.length + add.length];
			System.arraycopy(temp, 0, tags, 0, temp.length);
			System.arraycopy(add, 0, tags, temp.length, add.length);
		} else {
			tags = add;
		}
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof Tag) {
			Tag newTag = (Tag) obj;
			if (newTag.getName().equals(tagName)) {
				if (newTag.getValue().equals(value)) {
					if (newTag.getTags() == tags) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return tagName;
	}

	/**
	 * Outputs a string to send communications (function calls) to the iRODS
	 * server. All values are strings
	 */
	public String parseTag() {
		// If something isn't a string and you try to send a
		// non-printable character this way, it will get all messed up.
		// so...not sure if should be converted to Base64
		StringBuffer parsed = new StringBuffer(OPEN_START_TAG + tagName
				+ CLOSE_START_TAG);
		if (tags != null) {
			for (Tag tag : tags) {
				parsed.append(tag.parseTag());
			}
		} else {
			parsed.append(escapeChars(value));
		}
		parsed.append(OPEN_END_TAG + tagName + CLOSE_END_TAG + "\n");

		return parsed.toString();
	}

	String escapeChars(String out) {
		if (out == null) {
			return null;
		}
		out = out.replaceAll("&", "&amp;");
		out = out.replaceAll("<", "&lt;");
		out = out.replaceAll(">", "&gt;");
		out = out.replaceAll("\"", "&quot;");
		return out.replaceAll("`", "&apos;");
	}

	/**
	 * Just a simple message to check if there was an error.
	 */
	public static void status(final Tag message) throws IOException {
		Tag s = message.getTag("status");
		if ((s != null) && (s.getIntValue() < 0)) {
			throw new IRODSException("" + s.getIntValue());
		}
	}

	/**
	 * Read the data buffer to discover the first tag. Fill the values of that
	 * tag according to the above defined static final values.
	 * 
	 * @throws UnsupportedEncodingException
	 *             shouldn't throw, already tested for
	 */
	public static Tag readNextTag(final byte[] data, final String encoding)
			throws UnsupportedEncodingException {
		return readNextTag(data, true, encoding);
	}

	public static Tag readNextTag(final byte[] data, final boolean decode,
			final String encoding) throws UnsupportedEncodingException {
		if (data == null) {
			return null;
		}

		String d = new String(data, encoding);

		if (log.isTraceEnabled()) {
			log.trace(d);
		}
		// remove the random '\n'
		// had to find the end, sometimes '\n' is there, sometimes not.
		d = d.replaceAll(CLOSE_END_TAG + "\n", "" + CLOSE_END_TAG);

		int start = d.indexOf(OPEN_START_TAG), end = d.indexOf(CLOSE_START_TAG,
				start);
		int offset = 0;
		if (start < 0) {
			return null;
		}

		String tagName = d.substring(start + 1, end);
		end = d.lastIndexOf(OPEN_END_TAG + tagName + CLOSE_END_TAG);

		Tag tag = new Tag(tagName);
		offset = start + tagName.length() + 2;

		while (d.indexOf(OPEN_START_TAG, offset) >= 0 && offset >= 0
				&& offset < end) {
			// send the rest of the bytes read
			offset = readSubTag(tag, d, offset, decode);
		}

		return tag;
	}

	/**
	 * Read the data buffer to discover a sub tag. Fill the values of that tag
	 * according to the above defined static final values.
	 * 
	 * @throws UnsupportedEncodingException
	 *             shouldn't throw, already tested for
	 */
	private static int readSubTag(final Tag tag, final String data, int offset,
			final boolean decode) throws UnsupportedEncodingException {
		// easier to just write a second slightly modified method
		// instead of try to mix the two together,
		// even though they are very similar.
		int start = data.indexOf(OPEN_START_TAG, offset);
		if (start < 0) {
			return 1;
		}
		int closeStart = data.indexOf(CLOSE_START_TAG, start);
		String tagName = data.substring(start + 1, closeStart);
		int end = data.indexOf(OPEN_END_TAG + tagName + CLOSE_END_TAG,
				closeStart);
		int subTagStart = data.indexOf(OPEN_START_TAG, closeStart);

		Tag subTag = new Tag(tagName);
		tag.addTag(subTag);
		offset = start + tagName.length() + 2;
		if (subTagStart == end) {
			subTag.setValue(data.substring(offset, end), decode);
			return end + tagName.length() + 3; // endTagLocation + </endTag>
		} else {
			while (data.indexOf(OPEN_START_TAG, offset) >= 0 && offset >= 0
					&& offset < end) {
				// read the subTag, get new offset
				offset = readSubTag(subTag, data, offset, decode);
			}
			return offset + tagName.length() + 3; // endTagLocation + </endTag>
		}
	}

	/**
	 * Creates the KeyValPair_PI tag.
	 */
	public static Tag createKeyValueTag(final String keyword, final String value) {
		return createKeyValueTag(new String[][] { { keyword, value } });
	}

	/**
	 * Creates the KeyValPair_PI tag.
	 */
	public static Tag createKeyValueTag(final String[][] keyValue) {
		/*
		 * Must be like the following: <KeyValPair_PI> <ssLen>3</ssLen>
		 * <keyWord>dataType</keyWord> <keyWord>destRescName</keyWord>
		 * <keyWord>dataIncluded</keyWord> <svalue>generic</svalue>
		 * <svalue>resourceB</svalue> <svalue></svalue> </KeyValPair_PI>
		 */

		Tag pair = new Tag(IRODSConstants.KeyValPair_PI, new Tag(
				IRODSConstants.ssLen, 0));
		int i = 0, ssLength = 0;

		// return the empty Tag
		if (keyValue == null) {
			return pair;
		}

		for (; i < keyValue.length; i++) {
			if (keyValue[i] != null && keyValue[i][0] != null) {
				pair.addTag(IRODSConstants.keyWord, keyValue[i][0]);
				ssLength++;
			}
		}

		// just use index zero because they have to be in order...
		pair.tags[0].setValue(ssLength);
		if (i == 0) {
			return pair;
		}

		for (i = 0; i < keyValue.length; i++) {
			if (keyValue[i] != null && keyValue[i][0] != null) {
				pair.addTag(IRODSConstants.svalue, keyValue[i][1]);
			}
		}

		return pair;
	}

}
