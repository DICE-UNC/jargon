/**
 *
 */
package org.irods.jargon.core.exception;

/**
 * Exception in specific query processing. (-853000)
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class SpecificQueryException extends JargonException {

	private static final long serialVersionUID = 7553446240365967481L;

	public SpecificQueryException(final String message) {
		super(message);
	}

	public SpecificQueryException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public SpecificQueryException(final Throwable cause) {
		super(cause);
	}

	public SpecificQueryException(final String message, final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	public SpecificQueryException(final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public SpecificQueryException(final String message, final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
