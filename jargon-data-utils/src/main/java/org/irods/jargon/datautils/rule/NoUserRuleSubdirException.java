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

	public NoUserRuleSubdirException(final String message) {
		super(message);
	}

	public NoUserRuleSubdirException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public NoUserRuleSubdirException(final Throwable cause) {
		super(cause);
	}

	public NoUserRuleSubdirException(final String message, final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	public NoUserRuleSubdirException(final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public NoUserRuleSubdirException(final String message, final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
