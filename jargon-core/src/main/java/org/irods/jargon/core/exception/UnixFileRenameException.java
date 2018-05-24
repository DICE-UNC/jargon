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

	public UnixFileRenameException(final String message, final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

	public UnixFileRenameException(final String message, final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	public UnixFileRenameException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public UnixFileRenameException(final String message) {
		super(message);
	}

	public UnixFileRenameException(final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public UnixFileRenameException(final Throwable cause) {
		super(cause);
	}

}
