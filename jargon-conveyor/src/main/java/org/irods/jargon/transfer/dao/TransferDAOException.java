package org.irods.jargon.transfer.dao;

import org.irods.jargon.core.exception.JargonException;

/**
 * Denotes an exception occurring in the DAO layer of TransferEngine
 * 
 * @author jdr0887
 * 
 */
public class TransferDAOException extends JargonException {

	private static final long serialVersionUID = 2387712942850423010L;

	public TransferDAOException(final String message,
			final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

	public TransferDAOException(final String message, final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	public TransferDAOException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public TransferDAOException(final String message) {
		super(message);
	}

	public TransferDAOException(final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public TransferDAOException(final Throwable cause) {
		super(cause);
	}

}
