/**
 *
 */
package org.irods.jargon.ruleservice.composition;

import org.irods.jargon.core.exception.JargonException;

/**
 * Missing or malformed rule exception
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class MissingOrInvalidRuleException extends JargonException {

	private static final long serialVersionUID = -8753017280976736868L;

	public MissingOrInvalidRuleException(final String message) {
		super(message);
	}

	public MissingOrInvalidRuleException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public MissingOrInvalidRuleException(final Throwable cause) {
		super(cause);
	}

	public MissingOrInvalidRuleException(final String message, final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	public MissingOrInvalidRuleException(final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public MissingOrInvalidRuleException(final String message, final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
