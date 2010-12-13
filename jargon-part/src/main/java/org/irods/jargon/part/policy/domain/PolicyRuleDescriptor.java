/**
 * 
 */
package org.irods.jargon.part.policy.domain;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.irods.jargon.part.policy.ArchivalProcessSteps;

/**
 * Represents an iRODS rule, including metadata that describes the rule as a policy, and that describes the types and meaning of the various parameters.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class PolicyRuleDescriptor {
	
	/**
	 * Absolute path to the rule descriptor
	 */
	private String ruleDescriptorAbsolutePath = "";
	
	/**
	 * Textual description of this rule as a policy for display to a user;
	 */
	private  String descriptionAsAPolicy = "";
	
	/**
	 * Actual name of rule as defined in IRODS
	 */
	private  String ruleName = "";
	
	/**
	 * Version number of the rule description
	 */
	private int ruleVersionNumber = 0;
	
	/**
	 * Textual description of the rule as a property name suitable for display in alternative languages.
	 */
	private String descriptionAsAPolicyAsAni18nProperty = "";
	
	/**
	 * The archival step the policy is naturally applied to.
	 */
	private ArchivalProcessSteps archivalProcessStep;
	
	/**
	 * The date this policy was modifed.
	 */
	private Date policyModifyDate;
	
	/**
	 * A mapping of the parameters for this rule, by actual parameter name, that descibes in plan language the meaning of the 
	 * parameter, as well as type information.
	 */
	private Map<String, PolicyRuleParameterDescriptor> ruleParameterDescriptors = new HashMap<String, PolicyRuleParameterDescriptor>();

	public String getDescriptionAsAPolicy() {
		return descriptionAsAPolicy;
	}

	public void setDescriptionAsAPolicy(String descriptionAsAPolicy) {
		this.descriptionAsAPolicy = descriptionAsAPolicy;
	}

	public String getRuleName() {
		return ruleName;
	}

	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}

	public int getRuleVersionNumber() {
		return ruleVersionNumber;
	}

	public void setRuleVersionNumber(int ruleVersionNumber) {
		this.ruleVersionNumber = ruleVersionNumber;
	}

	public String getDescriptionAsAPolicyAsAni18nProperty() {
		return descriptionAsAPolicyAsAni18nProperty;
	}

	public void setDescriptionAsAPolicyAsAni18nProperty(
			String descriptionAsAPolicyAsAni18nProperty) {
		this.descriptionAsAPolicyAsAni18nProperty = descriptionAsAPolicyAsAni18nProperty;
	}

	public ArchivalProcessSteps getArchivalProcessStep() {
		return archivalProcessStep;
	}

	public void setArchivalProcessStep(ArchivalProcessSteps archivalProcessStep) {
		this.archivalProcessStep = archivalProcessStep;
	}

	public Date getPolicyModifyDate() {
		return policyModifyDate;
	}

	public void setPolicyModifyDate(Date policyModifyDate) {
		this.policyModifyDate = policyModifyDate;
	}

	public Map<String, PolicyRuleParameterDescriptor> getRuleParameterDescriptors() {
		return ruleParameterDescriptors;
	}

	public void setRuleParameterDescriptors(
			Map<String, PolicyRuleParameterDescriptor> ruleParameterDescriptors) {
		this.ruleParameterDescriptors = ruleParameterDescriptors;
	}

	public String getRuleDescriptorAbsolutePath() {
		return ruleDescriptorAbsolutePath;
	}

	public void setRuleDescriptorAbsolutePath(String ruleDescriptorAbsolutePath) {
		this.ruleDescriptorAbsolutePath = ruleDescriptorAbsolutePath;
	}

	
}
