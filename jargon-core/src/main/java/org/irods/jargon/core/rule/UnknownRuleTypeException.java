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

	public UnknownRuleTypeException(String message) {
		super(message);
	}

	public UnknownRuleTypeException(Throwable cause) {
		super(cause);
	}

	public UnknownRuleTypeException(String message, Throwable cause) {
		super(message, cause);
	}

}
