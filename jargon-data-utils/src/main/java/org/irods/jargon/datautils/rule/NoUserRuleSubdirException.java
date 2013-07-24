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
	public NoUserRuleSubdirException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public NoUserRuleSubdirException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public NoUserRuleSubdirException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public NoUserRuleSubdirException(String message, Throwable cause,
			int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public NoUserRuleSubdirException(Throwable cause,
			int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param underlyingIRODSExceptionCode
	 */
	public NoUserRuleSubdirException(String message,
			int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
