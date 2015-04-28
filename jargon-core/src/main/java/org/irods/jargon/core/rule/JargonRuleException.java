/**
 *
 */
package org.irods.jargon.core.rule;

import org.irods.jargon.core.exception.JargonException;

/**
 * Represents a rule translation exception due to the structure or parameters of
 * a rule.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class JargonRuleException extends JargonException {

	private static final long serialVersionUID = 2763090457875223403L;

	/**
	 * @param message
	 */
	public JargonRuleException(final String message) {
		super(message);

	}

	/**
	 * @param cause
	 */
	public JargonRuleException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public JargonRuleException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
