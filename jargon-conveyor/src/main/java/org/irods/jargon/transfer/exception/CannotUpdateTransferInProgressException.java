package org.irods.jargon.transfer.exception;

import org.irods.jargon.core.exception.JargonException;

/**
 * Exception caused by an attempt to update transfer manager data for transfers that are in progress.
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class CannotUpdateTransferInProgressException extends JargonException {

	
	private static final long serialVersionUID = 7777520801522885436L;

	public CannotUpdateTransferInProgressException(String message,
			int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

	public CannotUpdateTransferInProgressException(String message,
			Throwable cause, int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	public CannotUpdateTransferInProgressException(String message,
			Throwable cause) {
		super(message, cause);
	}

	public CannotUpdateTransferInProgressException(String message) {
		super(message);
	}

	public CannotUpdateTransferInProgressException(Throwable cause,
			int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public CannotUpdateTransferInProgressException(Throwable cause) {
		super(cause);
	}

}
