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

	public InvalidDataException(final String message) {
		super(message);
	}

	public InvalidDataException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public InvalidDataException(final Throwable cause) {
		super(cause);
	}

	public InvalidDataException(final String message, final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	public InvalidDataException(final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public InvalidDataException(final String message, final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
