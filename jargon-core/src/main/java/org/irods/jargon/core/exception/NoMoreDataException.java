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

	public NoMoreDataException(String message) {
		super(message);
	}

	public NoMoreDataException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoMoreDataException(Throwable cause) {
		super(cause);
	}

	public NoMoreDataException(String message, Throwable cause, int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	public NoMoreDataException(Throwable cause, int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public NoMoreDataException(String message, int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
