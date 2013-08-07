/**
 * 
 */
package org.irods.jargon.core.exception;

/**
 * Equivalent to UNIX_FILE_RENAME_ERR -528000 and variants
 * 
 * The given file cannot be renamed
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class UnixFileRenameException extends JargonException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3454704694291976996L;

	public UnixFileRenameException(String message,
			int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

	public UnixFileRenameException(String message, Throwable cause,
			int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	public UnixFileRenameException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnixFileRenameException(String message) {
		super(message);
	}

	public UnixFileRenameException(Throwable cause,
			int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public UnixFileRenameException(Throwable cause) {
		super(cause);
	}

}
