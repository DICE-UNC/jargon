package org.irods.jargon.part.policydriven;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.AVUQueryOperatorEnum;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.AVUQueryElement.AVUQueryPart;
import org.irods.jargon.part.exception.DataNotFoundException;
import org.irods.jargon.part.exception.DuplicateDataException;
import org.irods.jargon.part.policy.domain.Policy;
import org.irods.jargon.part.policy.xmlserialize.ObjectToXMLMarshaller;
import org.irods.jargon.part.policy.xmlserialize.XMLToObjectUnmarshaller;

public interface PolicyManager {

	/**
	 * List the policy repositories on this iRODS server.
	 * @return <code>List</code> of {@link org.irods.jargon.part.policydriven.PolicyDrivenServiceListingEntry} describing the policies stored
	 * on the server
	 * @throws PartException
	 */
	public abstract List<PolicyDrivenServiceListingEntry> listPolicyRepositories()
			throws PolicyDrivenServiceConfigException;

	/**
	 * Define a repository of policies.
	 * 
	 * @param policyDrivenServiceListingEntry
	 * object and write it to iRODS.
	 * @throws DuplicateDataException
	 * @throws PartException
	 */
	public void addPolicyRepository(PolicyDrivenServiceListingEntry policyDrivenServiceListingEntry)
			throws DuplicateDataException, PolicyDrivenServiceConfigException;

	/**
	 * Locate basic information about a policy repository using the given name
	 * 
	 * @param repositoryName
	 *            <code>String</code> with the repository name as stored in the
	 *            AVU 'value' field. This points to an iRODS collection with the
	 *            policy repository marker attribute.
	 * @return
	 * @throws DataNotFoundException
	 *             when no repository exists with the given name
	 * @throws PartException
	 *             other exception
	 */
	public PolicyDrivenServiceListingEntry findPolicyRepository(final String repositoryName)
			throws DataNotFoundException, PolicyDrivenServiceConfigException;

	/**
	 * Locate basic information about a policy using the given name
	 * 
	 * @param policyName
	 *            <code>String</code> with the policy name as stored in the
	 *            AVU 'value' field. This points to an iRODS collection with the
	 *            policy  marker attribute.
	 * @return
	 * @throws DataNotFoundException
	 *             when no repository exists with the given name
	 * @throws PartException
	 *             other exception
	 */
	public PolicyDrivenServiceListingEntry findPolicyBasicData(final String policyName)
			throws DataNotFoundException, PolicyDrivenServiceConfigException;

	/**
	 * Add a policy to a repository
	 * @param policyRepositoryName <code>String</code> with unique name for the policy repository
	 * @param policy {@link org.irods.jargon.part.policy.domain.Policy} that encapsulates the policy
	 * @param marshaller {@link org.irods.jargon.part.policy.xmlserialize.ObjectToXMLMarshaller} that can store the Policy as an XML file.   
	 * @throws PartException
	 * @throws DuplicateDataException
	 */
	public void addPolicyToRepository(final String policyRepositoryName, final Policy policy, final ObjectToXMLMarshaller marshaller)
			throws PolicyDrivenServiceConfigException, DuplicateDataException;

	/**
	 * Find the list of the policies that are stored in the given policy repository.
	 * @param policyRepositoryName <code>String</code> with the unique name of a policy repository
	 * @return <code>List</code> of {@link org.irods.jargon.part.policydriven.PolicyDrivenServiceListingEntry} describing the policies
	 * @throws DataNotFoundException thrown if the given policy repository does not exist.
	 * @throws PartException
	 */
	public List<PolicyDrivenServiceListingEntry> listPoliciesInPolicyRepository(final String policyRepositoryName)
			throws DataNotFoundException, PolicyDrivenServiceConfigException;

	/**
	 * Given an absolute path to an iRODS file that should contain an XML version of a policy, return the <code>Policy</code> object.
	 * @param absolutePathOfPolicyFile <code>String</code> to a valid XML file in a policy repository that contains a serialized XML policy.
	 * @param unmarshaller {@link org.irods.jargon.part.policy.xmlserialize.XMLToObjectUnmarshaller} that can turn the XML into a <code>Policy</code> object.
	 * @return {@link org.irods.jargon.part.policy.domain.Policy} with the policy data.
	 * @throws DataNotFoundException if the policy file does not exist or is not marked as a policy
	 * @throws PartException
	 */
	public Policy getPolicyFromPolicyRepository(
			final String policyName, final XMLToObjectUnmarshaller unmarshaller)
			throws DataNotFoundException, PolicyDrivenServiceConfigException;

	/**
	 * Method simply returns a list of all policies available.
	 * 
	 * @return <code>List</code> of {@link org.irods.jargon.part.policydriven.PolicyDrivenServiceListingEntry} which has identifying
	 * data about the policy.
	 * 
	 * @throws PartException
	 */
	public List<PolicyDrivenServiceListingEntry> listAllPolicies() throws PolicyDrivenServiceConfigException;
}