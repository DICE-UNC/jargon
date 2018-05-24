/**
 *
 */
package org.irods.jargon.core.rule;

/**
 * @author conwaymc
 *
 */
public class UnknownRuleTypeException extends JargonRuleException {

	private static final long serialVersionUID = -4368555332583145062L;

	public UnknownRuleTypeException(final String message) {
		super(message);
	}

	public UnknownRuleTypeException(final Throwable cause) {
		super(cause);
	}

	public UnknownRuleTypeException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
