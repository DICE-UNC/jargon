/**
 * 
 */
package org.irods.jargon.workflow.mso.exception;

import org.irods.jargon.core.exception.JargonException;

/**
 * General exception with WSO support
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class WSOException extends JargonException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2330658077822995313L;

	/**
	 * @param message
	 */
	public WSOException(final String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public WSOException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public WSOException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public WSOException(final String message, final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public WSOException(final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param underlyingIRODSExceptionCode
	 */
	public WSOException(final String message,
			final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
