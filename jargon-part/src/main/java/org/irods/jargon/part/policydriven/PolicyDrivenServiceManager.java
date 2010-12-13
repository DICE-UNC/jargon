package org.irods.jargon.part.policydriven;

import java.util.List;

public interface PolicyDrivenServiceManager {


	public static final String POLICY_DRIVEN_SERVICE_MARKER_ATTRIBUTE = "PolicyDrivenService";
	public static final String POLICY_DRIVEN_SERVICE_RULE_REPOSITORY_MARKER_ATTRIBUTE = "PolicyDrivenService:RuleRepository";
	public static final String POLICY_DRIVEN_SERVICE_RULE_DEFINITION_MARKER_ATTRIBUTE = "PolicyDrivenService:RuleRepository:RuleDefinition";
	public static final String POLICY_DRIVEN_SERVICE_RULE_MAPPING_MARKER_ATTRIBUTE = "PolicyDrivenService:PolicyToRuleMapping";
	public static final String POLICY_DRIVEN_SERVICE_POLICY_REPOSITORY_MARKER_ATTRIBUTE = "PolicyDrivenService:PolicyRepository";
	public static final String POLICY_DRIVEN_SERVICE_POLICY_MARKER_ATTRIBUTE = "PolicyDrivenService:Policy";
	public static final String POLICY_DRIVEN_SERVICE_SERIES_MARKER_ATTRIBUTE = "PolicyDrivenService:Series";
	public static final String POLICY_DRIVEN_SERVICE_SERIES_TO_POLICY_MARKER_ATTRIBUTE = "PolicyDrivenService:SeriesToPolicyMapping";
	public static final String POLICY_DRIVEN_SERVICE_SERIES_TO_APP_MARKER_ATTRIBUTE = "PolicyDrivenService:SeriesToAppMapping";
	
	public static final String POLICY_DRIVEN_SERVICE_PROCESSING_VIRUS_SCAN_RESULT = "PolicyDrivenService:PolicyProcessingResultAttribute:VirusScan";
	public static final String POLICY_DRIVEN_SERVICE_PROCESSING_FIXITY_CHECK_RESULT = "PolicyDrivenService:PolicyProcessingResultAttribute:FixityCheck";


	
	/**
	 * Given a service name (a unique name given to an overall policy-driven application, find the configuration information
	 * that describes the application.
	 * 
	 * Note that the method will return null if no config was found
	 * 
	 * @param serviceName <code>String</code> that gives the global unique name for this service
	 * @return {@link org.irods.jargon.part.policydriven.PolicyDrivenServiceConfig}
	 * @throws PartException
	 */
	public abstract PolicyDrivenServiceConfig getPolicyDrivenServiceConfigFromServiceName(
			final String serviceName) throws PolicyDrivenServiceConfigException;

	/**
	 * For a string that is used to denote policy driven services, give back a
	 * listing of the policy driven services installed in iRODS
	 * 
	 * @param serviceFlag
	 *            <code>String</code> that gives teh AVU attribute that marks a
	 *            policy-driven service
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.part.policydriven.PolicyDrivenServiceListingEntry}
	 * @throws PartException
	 */
	public abstract List<PolicyDrivenServiceListingEntry> findPolicyDrivenServiceNames(
			final String serviceFlag) throws PolicyDrivenServiceConfigException;
		
	/**
	 * Find the policy definitions that desribe available policies defined for this application that may be applied in
	 * given situations.
	 * @param serviceName <code>String</code> containing the global unique name for this service
	 * @return {@link org.irods.jargon.part.policydriven.PolicyDrivenServiceListingEntry} that gives summary data for the
	 * mapped policy repository.
	 * @throws PartException
	 */
	public abstract List<PolicyDrivenServiceListingEntry> findServicePolicyRepositories(
			final String serviceName) throws PolicyDrivenServiceConfigException;

	/**
	 * Add the given config to iRODS, which effectively creates a policy driven service.  The service top-level directory is created,
	 * as well the XML policy configuration.
	 * @param policyDrivenServiceConfig {@link org.irods.jargon.part.policydriven.PolicyDrivenServiceConfig}
	 * @throws PartException
	 */
	public void addPolicyDrivenService(PolicyDrivenServiceConfig policyDrivenServiceConfig)
			throws PolicyDrivenServiceConfigException;

	public List<PolicyDrivenServiceListingEntry> findServiceRuleRepositories(String serviceName)
			throws PolicyDrivenServiceConfigException;

}