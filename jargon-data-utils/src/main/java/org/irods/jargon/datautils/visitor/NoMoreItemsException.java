package org.irods.jargon.datautils.visitor;

import org.irods.jargon.core.exception.JargonException;

/**
 * Error attempting to process or visit more items when none exist
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class NoMoreItemsException extends JargonException {

	/**
	 *
	 */
	private static final long serialVersionUID = -5175804272699432666L;

	/**
	 * @param message
	 */
	public NoMoreItemsException(final String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public NoMoreItemsException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public NoMoreItemsException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public NoMoreItemsException(final String message, final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public NoMoreItemsException(final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param underlyingIRODSExceptionCode
	 */
	public NoMoreItemsException(final String message,
			final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
