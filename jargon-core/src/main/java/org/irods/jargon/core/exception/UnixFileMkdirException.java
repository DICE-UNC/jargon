/**
 * 
 */
package org.irods.jargon.core.exception;

/**
 * Equivalent to UNIX_FILE_MKDIR_ERR -520000 and variants
 * 
 * The given collection cannot be created
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class UnixFileMkdirException extends JargonException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9091779739237845919L;

	public UnixFileMkdirException(final String message,
			final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

	public UnixFileMkdirException(final String message, final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	public UnixFileMkdirException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public UnixFileMkdirException(final String message) {
		super(message);
	}

	public UnixFileMkdirException(final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public UnixFileMkdirException(final Throwable cause) {
		super(cause);
	}

}
