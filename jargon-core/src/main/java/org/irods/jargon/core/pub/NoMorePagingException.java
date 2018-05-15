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

	public NoMorePagingException(final String message) {
		super(message);
	}

	public NoMorePagingException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public NoMorePagingException(final Throwable cause) {
		super(cause);
	}

	public NoMorePagingException(final String message, final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	public NoMorePagingException(final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public NoMorePagingException(final String message, final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
