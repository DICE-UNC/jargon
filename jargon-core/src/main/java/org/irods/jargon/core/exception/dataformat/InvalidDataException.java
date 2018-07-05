/**
 * 
 */
package org.irods.jargon.core.exception.dataformat;

import org.irods.jargon.core.exception.JargonException;

/**
 * General error with the format of encoded data (e.g. JSON, XML) coming out of
 * iRODS operations
 * 
 * @author Mike Conway - NIEHS
 *
 */
public class InvalidDataException extends JargonException {

	private static final long serialVersionUID = -5261028590640346561L;

	public InvalidDataException(String message) {
		super(message);
	}

	public InvalidDataException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidDataException(Throwable cause) {
		super(cause);
	}

	public InvalidDataException(String message, Throwable cause, int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	public InvalidDataException(Throwable cause, int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public InvalidDataException(String message, int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
