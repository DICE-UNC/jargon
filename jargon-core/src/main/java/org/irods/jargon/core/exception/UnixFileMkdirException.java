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

	public UnixFileMkdirException(String message,
			int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

	public UnixFileMkdirException(String message, Throwable cause,
			int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	public UnixFileMkdirException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnixFileMkdirException(String message) {
		super(message);
	}

	public UnixFileMkdirException(Throwable cause,
			int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public UnixFileMkdirException(Throwable cause) {
		super(cause);
	}

}
