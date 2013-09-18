/**
 * 
 */
package org.irods.jargon.core.utils;


/**
 * Helper methods for dealing with rules
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class RuleUtils {

	/**
	 * 
	 */
	private RuleUtils() {
	}
	
	public static String buildDelayParamForMinutes(final int delayInMinutes) {
		if (delayInMinutes <= 0) {
			throw new IllegalArgumentException("delayInMinutes must be > 0");
		}
		
		StringBuilder sbBuilder = new StringBuilder();
		sbBuilder.append("<PLUSET>");
		sbBuilder.append(delayInMinutes);
		sbBuilder.append("m</PLUSET><EF>24h</EF>");
		return sbBuilder.toString();
	}

}
