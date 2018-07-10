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
	public MetadataQueryException(final String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public MetadataQueryException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public MetadataQueryException(final Throwable cause) {
		super(cause);
		// TODO Auto-genrated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public MetadataQueryException(final String message, final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public MetadataQueryException(final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param underlyingIRODSExceptionCode
	 */
	public MetadataQueryException(final String message, final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
