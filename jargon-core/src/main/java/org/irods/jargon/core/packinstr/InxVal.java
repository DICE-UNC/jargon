/**
 * 
 */
package org.irods.jargon.core.packinstr;

import org.irods.jargon.core.exception.JargonException;

/**
 * Immutable data structure represents the name/value in an InxVal pair. Useful
 * in passing data to a packing instruction
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class InxVal {
	private final Integer name;
	private final String value;

	/**
	 * return a new instance
	 * 
	 * @param name
	 *            <code>String</code> with the 'key' value for this protocol
	 *            element
	 * @param value
	 *            <code>String</code> with the value part for this protocol
	 *            element
	 * @return <code>InxVal</code> as an immutable data object.
	 * @throws JargonException
	 */
	public static InxVal instance(final Integer name, final String value)
			throws JargonException {
		return new InxVal(name, value);
	}

	private InxVal(final Integer name, final String value)
			throws JargonException {
		if (name == null) {
			throw new JargonException("name is null");
		}

		if (value == null) {
			throw new JargonException("value is null");
		}

		this.name = name;
		this.value = value;
	}

	public Integer getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

}
