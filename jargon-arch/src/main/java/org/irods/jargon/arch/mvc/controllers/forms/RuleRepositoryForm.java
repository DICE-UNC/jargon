/**
 * 
 */
package org.irods.jargon.arch.mvc.controllers.forms;

/**
 * Form for rule repository 
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class RuleRepositoryForm {
	private String ruleRepositoryName = "";
	private String ruleRepositoryPath = "";
	private String comment = "";
	
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ruleRepositoryForm:");
		sb.append("\n   ruleRepositoryName:");
		sb.append(ruleRepositoryName);
		sb.append("\n   ruleRepositoryPath:");
		sb.append(ruleRepositoryPath);
		sb.append("\n   comment:");
		sb.append(comment);
		return sb.toString();
	}
	public String getRuleRepositoryName() {
		return ruleRepositoryName;
	}
	public void setRuleRepositoryName(String ruleRepositoryName) {
		this.ruleRepositoryName = ruleRepositoryName;
	}
	public String getRuleRepositoryPath() {
		return ruleRepositoryPath;
	}
	public void setRuleRepositoryPath(String ruleRepositoryPath) {
		this.ruleRepositoryPath = ruleRepositoryPath;
	}
	
	

}
