/**
 * 
 */
package org.irods.jargon.core.rule;

/**
 * @author conwaymc
 *
 */
public class UnknownRuleTypeException extends JargonRuleException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4368555332583145062L;

	/**
	 * @param message
	 */
	public UnknownRuleTypeException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public UnknownRuleTypeException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public UnknownRuleTypeException(String message, Throwable cause) {
		super(message, cause);
	}

}
