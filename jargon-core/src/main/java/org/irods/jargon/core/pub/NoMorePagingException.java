/**
 *
 */
package org.irods.jargon.core.pub;

import org.irods.jargon.core.exception.JargonException;

/**
 * Exception overrunning available paging actions
 *
 * @author Mike Conway - DICE
 *
 */
public class NoMorePagingException extends JargonException {

	/**
	 *
	 */
	private static final long serialVersionUID = 310797184277101472L;

	/**
	 * @param message
	 */
	public NoMorePagingException(final String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public NoMorePagingException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public NoMorePagingException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public NoMorePagingException(final String message, final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public NoMorePagingException(final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param underlyingIRODSExceptionCode
	 */
	public NoMorePagingException(final String message,
			final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
