/**
 * 
 */
package org.irods.jargon.mdquery.exception;

import org.irods.jargon.core.exception.JargonException;

/**
 * @author Mike Conway - DICE
 *
 */
public class MetadataQueryException extends JargonException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6215448904296396234L;

	/**
	 * @param message
	 */
	public MetadataQueryException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public MetadataQueryException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public MetadataQueryException(Throwable cause) {
		super(cause);
		// TODO Auto-genrated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public MetadataQueryException(String message, Throwable cause,
			int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public MetadataQueryException(Throwable cause,
			int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param underlyingIRODSExceptionCode
	 */
	public MetadataQueryException(String message,
			int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
