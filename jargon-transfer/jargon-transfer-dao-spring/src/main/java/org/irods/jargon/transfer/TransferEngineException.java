package org.irods.jargon.transfer;

import org.irods.jargon.core.exception.JargonException;

/**
 * Denotes an exception occurring within the transfer engine code.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class TransferEngineException extends JargonException {

	private static final long serialVersionUID = -4464139285274100789L;

	public TransferEngineException(final String message,
			final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

	public TransferEngineException(final String message, final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	public TransferEngineException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public TransferEngineException(final String message) {
		super(message);
	}

	public TransferEngineException(final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public TransferEngineException(final Throwable cause) {
		super(cause);
	}
}
