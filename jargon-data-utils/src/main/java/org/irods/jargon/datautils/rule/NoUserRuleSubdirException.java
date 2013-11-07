/**
 * 
 */
package org.irods.jargon.datautils.rule;

import org.irods.jargon.core.exception.JargonException;

/**
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class NoUserRuleSubdirException extends JargonException {

	private static final long serialVersionUID = 7848619543804101461L;

	/**
	 * @param message
	 */
	public NoUserRuleSubdirException(final String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public NoUserRuleSubdirException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public NoUserRuleSubdirException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public NoUserRuleSubdirException(final String message,
			final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public NoUserRuleSubdirException(final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param underlyingIRODSExceptionCode
	 */
	public NoUserRuleSubdirException(final String message,
			final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
