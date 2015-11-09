/**
 *
 */
package org.irods.jargon.core.packinstr;

import org.irods.jargon.core.exception.JargonException;

/**
 * Encapsulates a key/value pair for various xml protocol packing instructions.
 * This object is immutable.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class KeyValuePair {

	private final String key;
	private final String value;

	public static final KeyValuePair instance(final String key,
			final String value) throws JargonException {
		return new KeyValuePair(key, value);
	}

	private KeyValuePair(final String key, final String value)
			throws JargonException {
		if (key == null) {
			throw new JargonException("key is null");
		}

		if (value == null) {
			throw new JargonException("value is null");
		}

		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}

}
