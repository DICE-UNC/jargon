/**
 * 
 */
package org.irods.jargon.arch.mvc.controllers.forms;

/**
 * Represents a form to add/update a policy
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class PolicyForm {
	
	private String policyName = "";
	private String comment = "";
	private boolean requireStaging = false;
	private boolean requireVirusScan = false;
	private boolean requireChecksum = false;
	private int numberOfReplicas = 0;
	private String retentionDays = "";
	private String policyRepositoryName = "";

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Policy Form:");
		sb.append("\n   policyName:");
		sb.append(policyName);
		sb.append("\n   policyRepositoryName:");
		sb.append(policyRepositoryName);
		sb.append("\n   comment:");
		sb.append(comment);
		sb.append("\n   requireStaging:");
		sb.append(requireStaging);
		sb.append("\n   requireVirusScan:");
		sb.append(requireVirusScan);
		sb.append("\n   requireChecksum:");
		sb.append(requireChecksum);
		sb.append("\n   numberOfReplicas:");
		sb.append(numberOfReplicas);
		sb.append("\n   retentionDays:");
		sb.append(retentionDays);
		return sb.toString();
	}
	
	public String getPolicyName() {
		return policyName;
	}

	public void setPolicyName(String policyName) {
		this.policyName = policyName;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public boolean isRequireStaging() {
		return requireStaging;
	}

	public void setRequireStaging(boolean requireStaging) {
		this.requireStaging = requireStaging;
	}

	public boolean isRequireVirusScan() {
		return requireVirusScan;
	}

	public void setRequireVirusScan(boolean requireVirusScan) {
		this.requireVirusScan = requireVirusScan;
	}

	public boolean isRequireChecksum() {
		return requireChecksum;
	}

	public void setRequireChecksum(boolean requireChecksum) {
		this.requireChecksum = requireChecksum;
	}

	public int getNumberOfReplicas() {
		return numberOfReplicas;
	}

	public void setNumberOfReplicas(int numberOfReplicas) {
		this.numberOfReplicas = numberOfReplicas;
	}

	public String getPolicyRepositoryName() {
		return policyRepositoryName;
	}

	public void setPolicyRepositoryName(String policyRepositoryName) {
		this.policyRepositoryName = policyRepositoryName;
	}

	public String getRetentionDays() {
		return retentionDays;
	}

	public void setRetentionDays(String retentionDays) {
		this.retentionDays = retentionDays;
	}

}
