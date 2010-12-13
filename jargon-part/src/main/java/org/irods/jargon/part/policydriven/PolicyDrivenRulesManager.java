package org.irods.jargon.part.policydriven;

import java.util.List;

import org.irods.jargon.part.exception.DataNotFoundException;
import org.irods.jargon.part.exception.DuplicateDataException;

public interface PolicyDrivenRulesManager {

	/**
	 * Give a listing of all rule repositories on this server, as indicated by the rule repository AVU flag
	 * @return
	 * @throws PartException
	 */
	public List<PolicyDrivenServiceListingEntry> findRuleRepositories()
			throws PolicyDrivenServiceConfigException;

	/**
	 * Add a repository (global) of rule mappings to the iRODS server. These mappings are then available across applications
	 * @param policyDrivenServiceListing {@link org.irods.jargon.part.policydriven.PolicyDrivenServiceListing} with properties for the 
	 * listing
	 * @throws PartException
	 */
	public void addRuleRepository(PolicyDrivenServiceListingEntry policyDrivenServiceListingEntry)
			throws DuplicateDataException, PolicyDrivenServiceConfigException;

	/**
	 * Locate basic information about a rule repository using the given name
	 * @param repositoryName <code>String</code> with the repository name as stored in the AVU 'value' field.  This points
	 *  to an iRODS collection with the rule repository marker attribute.
	 * @return
	 * @throws DataNotFoundException when no repository exists with the given name
	 * @throws PartException other exception
	 */
	public PolicyDrivenServiceListingEntry findRuleRepository(String repositoryName)
			throws DataNotFoundException, PolicyDrivenServiceConfigException;

	/**
	 * Given a named rule repository, generate a list of the rules that exist in the repository
	 * @param repositoryName <code>String</code> with name (not path) of a rule repository to query
	 * @return {@link org.irods.jargon.part.policydriven.PolicyDrivenServiceListingEntry} with references to rules in the given repository.
	 * @throws DataNotFoundException thrown when the rule repository does not exist
	 * @throws PartException
	 */
	public List<PolicyDrivenServiceListingEntry> listRulesInRepository(String repositoryName)
			throws DataNotFoundException, PolicyDrivenServiceConfigException;

	
}