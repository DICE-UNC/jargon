/**
 * 
 */
package org.irods.jargon.part.policydriven;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines a configuration for a policy-driven service running within IRODS. A
 * policy driven service
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class PolicyDrivenServiceConfig {

	private String serviceConfigRootPath = "";
	private String serviceName = "";
	private String serviceRootPath = "";
	private String serviceDescription = "";
	private List<PolicyDrivenServiceListingEntry> ruleMetadataRepositoryPath = new ArrayList<PolicyDrivenServiceListingEntry>();
	private List<PolicyDrivenServiceListingEntry> policyDefinitionRepositoryPath = new ArrayList<PolicyDrivenServiceListingEntry>();

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("policy driven service config:");
		sb.append("\n   serviceName:");
		sb.append(serviceName);
		sb.append("\n   serviceRootPath:");
		sb.append(serviceRootPath);
		sb.append("\n   serviceDescription:");
		sb.append(serviceDescription);
		sb.append("\n  ruleMetadataRepositories:");
		sb.append(ruleMetadataRepositoryPath);
		sb.append("\n   policyDefinitionRepositoryPath:");
		sb.append(policyDefinitionRepositoryPath);
		return sb.toString();
	}
	
	/**
	 * Do basic checks of the configj to make sure it has valid data
	 * @throws PartException if validation errors occur
	 */
	public void validate() throws PolicyDrivenServiceConfigException {
		if (serviceName == null || serviceName.isEmpty()) {
			throw new PolicyDrivenServiceConfigException("serviceName is null or missing");
		}
		if (serviceRootPath == null || serviceRootPath.isEmpty()) {
			throw new PolicyDrivenServiceConfigException("serviceRootPath is null or missing");
		}
	}
	
	public String getServiceConfigRootPath() {
		return serviceConfigRootPath;
	}

	public void setServiceConfigRootPath(String serviceConfigRootPath) {
		this.serviceConfigRootPath = serviceConfigRootPath;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getServiceRootPath() {
		return serviceRootPath;
	}

	public void setServiceRootPath(String serviceRootPath) {
		this.serviceRootPath = serviceRootPath;
	}

	public String getServiceDescription() {
		return serviceDescription;
	}

	public void setServiceDescription(String serviceDescription) {
		this.serviceDescription = serviceDescription;
	}

	public List<PolicyDrivenServiceListingEntry> getRuleMetadataRepositoryPath() {
		return ruleMetadataRepositoryPath;
	}

	public void setRuleMetadataRepositoryPath(
			List<PolicyDrivenServiceListingEntry> ruleMetadataRepositoryPath) {
		this.ruleMetadataRepositoryPath = ruleMetadataRepositoryPath;
	}

	public List<PolicyDrivenServiceListingEntry> getPolicyDefinitionRepositoryPath() {
		return policyDefinitionRepositoryPath;
	}

	public void setPolicyDefinitionRepositoryPath(
			List<PolicyDrivenServiceListingEntry> policyDefinitionRepositoryPath) {
		this.policyDefinitionRepositoryPath = policyDefinitionRepositoryPath;
	}

}
