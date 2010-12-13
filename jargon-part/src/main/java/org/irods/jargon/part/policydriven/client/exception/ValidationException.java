package org.irods.jargon.part.policydriven.client.exception;

/**
 * Exception in the validation of client data that is required by a policy.
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class ValidationException extends Exception {
	
	private static final long serialVersionUID = 3158700087126219947L;

	/**
	 * @param message
	 */
	public ValidationException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public ValidationException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ValidationException(String message, Throwable cause) {
		super(message, cause);
	}

}
