package org.irods.jargon.core.exception;

import org.irods.jargon.core.protovalues.ErrorEnum;

/**
 * The iRODS client user being proxied isn't a valid user.
 */
public final class InvalidClientUserException extends JargonException {

	/**
	 *
	 */
	private static final long serialVersionUID = 1642876648704250220L;

	private static String makeMessage(final String explanation) {
		final String baseMessage = "invalid client user";

		if (explanation == null || explanation.isEmpty()) {
			return baseMessage;
		} else {
			return baseMessage + ", " + explanation;
		}
	}

	/**
	 * the constructor
	 *
	 * @param explanation
	 *            <code>String</code> with any additional information
	 */
	public InvalidClientUserException(final String explanation) {
		super(makeMessage(explanation), ErrorEnum.CAT_INVALID_CLIENT_USER
				.getInt());
	}
}
