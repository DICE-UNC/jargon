package org.irods.jargon.transfer.dao;

import org.irods.jargon.core.exception.JargonException;

/**
 * Denotes an exception occurring in the DAO layer of TransferEngine
 * @author jdr0887
 * 
 */
public class TransferDAOException extends JargonException {

	private static final long serialVersionUID = 2387712942850423010L;

	public TransferDAOException(String message, int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

	public TransferDAOException(String message, Throwable cause,
			int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	public TransferDAOException(String message, Throwable cause) {
		super(message, cause);
	}

	public TransferDAOException(String message) {
		super(message);
	}

	public TransferDAOException(Throwable cause,
			int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public TransferDAOException(Throwable cause) {
		super(cause);
	}


}
