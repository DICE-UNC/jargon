/**
 * 
 */
package org.irods.jargon.core.exception;

/**
 * -806000 error in iRODS caused by catalog sql exception.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class CatalogSQLException extends JargonException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6150226404159251373L;

	/**
	 * @param message
	 */
	public CatalogSQLException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public CatalogSQLException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public CatalogSQLException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public CatalogSQLException(String message, Throwable cause,
			int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public CatalogSQLException(Throwable cause, int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param underlyingIRODSExceptionCode
	 */
	public CatalogSQLException(String message, int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
