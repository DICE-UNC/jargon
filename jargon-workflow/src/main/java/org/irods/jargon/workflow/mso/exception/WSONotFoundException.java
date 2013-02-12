/**
 * 
 */
package org.irods.jargon.workflow.mso.exception;

/**
 * Unable to find a WSO specified
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class WSONotFoundException extends WSOException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -527003010047931769L;

	/**
	 * @param message
	 */
	public WSONotFoundException(final String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public WSONotFoundException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public WSONotFoundException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public WSONotFoundException(final String message, final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public WSONotFoundException(final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param underlyingIRODSExceptionCode
	 */
	public WSONotFoundException(final String message,
			final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
