package org.irods.jargon.usertagging.sharing;

import org.irods.jargon.core.exception.JargonException;

/**
 * A share already exists for the given file or collection
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class ShareAlreadyExistsException extends JargonException {

	private static final long serialVersionUID = -2890110437140911635L;

	public ShareAlreadyExistsException(final String message) {
		super(message);
	}

	public ShareAlreadyExistsException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public ShareAlreadyExistsException(final Throwable cause) {
		super(cause);
	}

	public ShareAlreadyExistsException(final String message, final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	public ShareAlreadyExistsException(final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public ShareAlreadyExistsException(final String message, final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
