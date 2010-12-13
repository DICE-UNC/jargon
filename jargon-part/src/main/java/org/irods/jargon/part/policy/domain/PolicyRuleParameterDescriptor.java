/**
 * 
 */
package org.irods.jargon.part.policy.domain;

/**
 * Describes a parameter to a <code>PolicyRuleDescriptor</code> that describes
 * the type, meaning, and binding parameter for a rule parameter
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class PolicyRuleParameterDescriptor {

	/*
	 * Type information that assists in translation of the parameter value into
	 * an interface element
	 */
	public enum ParameterType {
		LITERAL_NUMBER, LITERAL_STRING, LITERAL_KVP, SYMBOLIC_PARAMETER
	}

	/*
	 * The actual name of the parameter in the IRODS rule
	 */
	private String actualParameterNameInRule = "";

	/*
	 * Textual description of the meaning of the parameter
	 */
	private String textualParameterDescription = "";

	/*
	 * Textual description of the meaning of the parameter in property form
	 * suitable for display in an alternative language
	 */
	private String textualParameterDescriptionAsAni18nProperty = "";

	public String getActualParameterNameInRule() {
		return actualParameterNameInRule;
	}

	public void setActualParameterNameInRule(String actualParameterNameInRule) {
		this.actualParameterNameInRule = actualParameterNameInRule;
	}

	public String getTextualParameterDescription() {
		return textualParameterDescription;
	}

	public void setTextualParameterDescription(
			String textualParameterDescription) {
		this.textualParameterDescription = textualParameterDescription;
	}

	public String getTextualParameterDescriptionAsAni18nProperty() {
		return textualParameterDescriptionAsAni18nProperty;
	}

	public void setTextualParameterDescriptionAsAni18nProperty(
			String textualParameterDescriptionAsAni18nProperty) {
		this.textualParameterDescriptionAsAni18nProperty = textualParameterDescriptionAsAni18nProperty;
	}

}
