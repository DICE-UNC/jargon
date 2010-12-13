/**
 * 
 */
package org.irods.jargon.part.policy.domain;

/**
 * Represents a series, or collection of related items, in a policy driven
 * application. Currently a nominal placeholder for further development.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class Series {

	private String name = "";
	private String description = "";
	private String collectionAbsolutePath = "";
	private String containingServiceName = "";
	private String boundPolicyName = "";

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCollectionAbsolutePath() {
		return collectionAbsolutePath;
	}

	public void setCollectionAbsolutePath(String collectionAbsolutePath) {
		this.collectionAbsolutePath = collectionAbsolutePath;
	}

	public String getContainingServiceName() {
		return containingServiceName;
	}

	public void setContainingServiceName(String containingServiceName) {
		this.containingServiceName = containingServiceName;
	}

	public String getBoundPolicyName() {
		return boundPolicyName;
	}

	public void setBoundPolicyName(String boundPolicyName) {
		this.boundPolicyName = boundPolicyName;
	}

}
