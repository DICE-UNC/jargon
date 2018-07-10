/**
 *
 */
package org.irods.jargon.core.exception;

/**
 * Exception caused by paging or requesting data when no more data is available
 *
 * @author Mike Conway - DICE
 *
 */
public class NoMoreDataException extends JargonException {

	private static final long serialVersionUID = -2079619325335253964L;

	public NoMoreDataException(final String message) {
		super(message);
	}

	public NoMoreDataException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public NoMoreDataException(final Throwable cause) {
		super(cause);
	}

	public NoMoreDataException(final String message, final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	public NoMoreDataException(final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public NoMoreDataException(final String message, final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
