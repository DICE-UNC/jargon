/**
 * 
 */
package org.irods.jargon.datautils.shoppingcart;

import org.irods.jargon.core.exception.JargonException;

/**
 * An attempt is made to store an empty cart
 * 
 * @author Mike Conway - NIEHS
 *
 */
public class EmptyCartException extends JargonException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8224329147909746339L;

	/**
	 * @param message
	 */
	public EmptyCartException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public EmptyCartException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public EmptyCartException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public EmptyCartException(String message, Throwable cause, int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public EmptyCartException(Throwable cause, int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param underlyingIRODSExceptionCode
	 */
	public EmptyCartException(String message, int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
