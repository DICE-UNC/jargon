/**
 *
 */
package org.irods.jargon.core.rule;

/**
 * Value class used internally when parsing input parameters for rules
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
class RuleInputParameter {
	private String paramName = "";
	private String paramValue = "";

	/**
	 * @param paramName
	 * @param paramValue
	 */
	RuleInputParameter(final String paramName, final String paramValue) {
		this.paramName = paramName;
		this.paramValue = paramValue;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("RuleInputParameter:");
		sb.append("\n   paramName:");
		sb.append(paramName);
		sb.append("\n   paramValue:");
		sb.append(paramValue);
		return sb.toString();
	}

	/**
	 * @return the paramName
	 */
	String getParamName() {
		return paramName;
	}

	/**
	 * @param paramName
	 *            the paramName to set
	 */
	void setParamName(final String paramName) {
		this.paramName = paramName;
	}

	/**
	 * @return the paramValue
	 */
	String getParamValue() {
		return paramValue;
	}

	/**
	 * @param paramValue
	 *            the paramValue to set
	 */
	void setParamValue(final String paramValue) {
		this.paramValue = paramValue;
	}

}
