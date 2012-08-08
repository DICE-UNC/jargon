/**
 * 
 */
package org.irods.jargon.userprofile;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.exception.JargonException;

/**
 * Exception in the validation of the <code>UserProfile</code>
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class UserProfileValidationException extends JargonException {

	private static final long serialVersionUID = -1188160240484261453L;

	/**
	 * Per-field validation messages
	 */
	private List<String> validationMessages = new ArrayList<String>();

	/**
	 * @param message
	 * @param underlyingIRODSExceptionCode
	 */
	public UserProfileValidationException(String message,
			int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public UserProfileValidationException(String message, Throwable cause,
			int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public UserProfileValidationException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public UserProfileValidationException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public UserProfileValidationException(Throwable cause,
			int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param cause
	 */
	public UserProfileValidationException(Throwable cause) {
		super(cause);
	}

	/**
	 * @return the validationMessages
	 */
	public List<String> getValidationMessages() {
		return validationMessages;
	}

	/**
	 * @param validationMessages
	 *            the validationMessages to set
	 */
	public void setValidationMessages(List<String> validationMessages) {
		this.validationMessages = validationMessages;
	}

}
