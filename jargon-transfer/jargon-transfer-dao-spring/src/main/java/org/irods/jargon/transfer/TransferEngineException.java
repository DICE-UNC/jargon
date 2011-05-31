package org.irods.jargon.transfer;

import org.irods.jargon.core.exception.JargonException;

/**
 * Denotes an exception occurring within the transfer engine code.
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class TransferEngineException extends JargonException {
	
	private static final long serialVersionUID = -4464139285274100789L;

	public TransferEngineException(String message,
			int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

	public TransferEngineException(String message, Throwable cause,
			int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	public TransferEngineException(String message, Throwable cause) {
		super(message, cause);
	}

	public TransferEngineException(String message) {
		super(message);
	}

	public TransferEngineException(Throwable cause,
			int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public TransferEngineException(Throwable cause) {
		super(cause);
	}
}
