/**
 *
 */
package org.irods.jargon.userprofile;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.exception.JargonException;

/**
 * Exception in the validation of the {@code UserProfile}
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
	public UserProfileValidationException(final String message, final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public UserProfileValidationException(final String message, final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public UserProfileValidationException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public UserProfileValidationException(final String message) {
		super(message);
	}

	/**
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public UserProfileValidationException(final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param cause
	 */
	public UserProfileValidationException(final Throwable cause) {
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
	public void setValidationMessages(final List<String> validationMessages) {
		this.validationMessages = validationMessages;
	}

}
