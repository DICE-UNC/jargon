package org.irods.jargon.usertagging.sharing;

import org.irods.jargon.core.exception.JargonException;

/**
 * A share already exists for the given file or collection
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class ShareAlreadyExistsException extends JargonException {

	private static final long serialVersionUID = -2890110437140911635L;

	public ShareAlreadyExistsException(String message) {
		super(message);
	}

	public ShareAlreadyExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public ShareAlreadyExistsException(Throwable cause) {
		super(cause);
	}

	public ShareAlreadyExistsException(String message, Throwable cause,
			int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	public ShareAlreadyExistsException(Throwable cause,
			int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public ShareAlreadyExistsException(String message,
			int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
