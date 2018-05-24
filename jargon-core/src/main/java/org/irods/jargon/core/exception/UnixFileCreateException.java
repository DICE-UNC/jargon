/**
 *
 */
package org.irods.jargon.core.exception;

/**
 * Equivalent to UNIX_FILE_CREATE_ERR -511000 and variants
 *
 * The given file cannot be created
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class UnixFileCreateException extends JargonException {

	/**
	 *
	 */
	private static final long serialVersionUID = -3454704694291976986L;

	public UnixFileCreateException(final String message, final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

	public UnixFileCreateException(final String message, final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	public UnixFileCreateException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public UnixFileCreateException(final String message) {
		super(message);
	}

	public UnixFileCreateException(final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public UnixFileCreateException(final Throwable cause) {
		super(cause);
	}

}
