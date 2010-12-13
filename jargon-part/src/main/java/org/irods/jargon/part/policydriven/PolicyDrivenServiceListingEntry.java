/**
 * 
 */
package org.irods.jargon.part.policydriven;

/**
 * A simple data object that describes the location of a policy driven service.  This object is immutable.
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class PolicyDrivenServiceListingEntry {
	
	private final String policyDrivenServiceName;
	private final String policyDrivenServiceAbsolutePath;
	private final String comment;
	
	public static PolicyDrivenServiceListingEntry instance(final String policyDrivenServiceName, final String policyDrivenServiceAbsolutePath, final String comment) throws PolicyDrivenServiceConfigException {
		return new PolicyDrivenServiceListingEntry(policyDrivenServiceName, policyDrivenServiceAbsolutePath, comment);
	}
	
	private PolicyDrivenServiceListingEntry(final String policyDrivenServiceName, final String policyDrivenServiceAbsolutePath, final String comment) throws PolicyDrivenServiceConfigException {
		if (policyDrivenServiceName == null || policyDrivenServiceName.isEmpty()) {
			throw new PolicyDrivenServiceConfigException("null or empty policy driven service name");
		}
		
		if (policyDrivenServiceAbsolutePath == null || policyDrivenServiceAbsolutePath.isEmpty()) {
			throw new PolicyDrivenServiceConfigException("null or empty policyDrivenServiceAbsolutePath");
		}
		
		if (comment == null) {
			throw new PolicyDrivenServiceConfigException("null comment. please leave as blank if unused");
		}
		
		this.policyDrivenServiceName = policyDrivenServiceName;
		this.policyDrivenServiceAbsolutePath = policyDrivenServiceAbsolutePath;
		this.comment = comment;
	}

	public String getPolicyDrivenServiceName() {
		return policyDrivenServiceName;
	}

	public String getPolicyDrivenServiceAbsolutePath() {
		return policyDrivenServiceAbsolutePath;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("PolicyDrivenServiceListingEntry:");
		sb.append("\n   serviceName:");
		sb.append(policyDrivenServiceName);
		sb.append("\n    absolutePath:");
		sb.append(policyDrivenServiceAbsolutePath);
		sb.append("\n    comment:");
		sb.append(comment);
		return sb.toString();
	}

	public String getComment() {
		return comment;
	}
	
	
	
}
