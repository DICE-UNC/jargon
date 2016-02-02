/**
 * 
 */
package org.irods.jargon.mdquery;

import org.irods.jargon.core.exception.JargonRuntimeException;

/**
 * General unchecked exception for metadata queries
 * 
 * @author Mike Conway - DICE
 *
 */
public class MetadataQueryRuntimeException extends JargonRuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5850439092572959832L;

	/**
	 * 
	 */
	public MetadataQueryRuntimeException() {
	}

	/**
	 * @param message
	 */
	public MetadataQueryRuntimeException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public MetadataQueryRuntimeException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public MetadataQueryRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

}
