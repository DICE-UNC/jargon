package org.irods.jargon.core.connection.auth;

import org.irods.jargon.core.exception.JargonException;

/**
 * Represents an exception where the given auth mechanism is not available
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class AuthUnavailableException extends JargonException {

	private static final long serialVersionUID = -7178590696091208081L;

	/**
	 * @param message
	 *            {@link String} that is the underlying error message
	 */
	public AuthUnavailableException(final String message) {
		super(message);
	}

	/**
	 * @param message
	 *            {@link String} that is the underlying error message
	 * @param cause
	 *            {@link Throwable} that is the underlying cause
	 */
	public AuthUnavailableException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 *            {@link Throwable} that is the underlying cause
	 */
	public AuthUnavailableException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 *            {@link String} that is the underlying error message
	 * @param cause
	 *            {@link Throwable} that is the underlying cause
	 * @param underlyingIRODSExceptionCode
	 *            {@code int} with an iRODS response code
	 */
	public AuthUnavailableException(final String message, final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param cause
	 *            {@link Throwable} that is the underlying cause
	 * @param underlyingIRODSExceptionCode
	 *            {@code int} with an iRODS response code
	 */
	public AuthUnavailableException(final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 *            {@link String} that is the underlying error message
	 * @param underlyingIRODSExceptionCode
	 *            {@code int}
	 */
	public AuthUnavailableException(final String message, final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
