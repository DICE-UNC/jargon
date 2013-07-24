/**
 * 
 */
package org.irods.jargon.datautils.rule;

import org.irods.jargon.core.pub.domain.IRODSDomainObject;

/**
 * Represents a file that contains a user rule
 * @author  Mike Conway - DICE (www.irods.org)
 *
 */
public class UserRuleDefinition extends IRODSDomainObject {
	
	public enum RuleAproposTo  {COLLECTION, DATA_OBJECT, ANY}
	private String ruleName = "";
	private String ruleAbsolutePath = "";
	private String ruleDescription = "";
	private RuleAproposTo ruleAproposTo = RuleAproposTo.ANY;
	
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("UserRuleDefinition");
		sb.append("\n\t ruleName:");
		sb.append(ruleName);
		sb.append("\n\t ruleAbsolutePath:");
		sb.append(ruleAbsolutePath);
		sb.append("\n\t ruleDescription:");
		sb.append(ruleDescription);
		sb.append("\n\t ruleAproposTo:");
		sb.append(ruleAproposTo);
		return sb.toString();
	}
	
	public String getRuleName() {
		return ruleName;
	}
	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}
	public String getRuleAbsolutePath() {
		return ruleAbsolutePath;
	}
	public void setRuleAbsolutePath(String ruleAbsolutePath) {
		this.ruleAbsolutePath = ruleAbsolutePath;
	}
	public String getRuleDescription() {
		return ruleDescription;
	}
	public void setRuleDescription(String ruleDescription) {
		this.ruleDescription = ruleDescription;
	}

	public RuleAproposTo getRuleAproposTo() {
		return ruleAproposTo;
	}

	public void setRuleAproposTo(RuleAproposTo ruleAproposTo) {
		this.ruleAproposTo = ruleAproposTo;
	}

}
