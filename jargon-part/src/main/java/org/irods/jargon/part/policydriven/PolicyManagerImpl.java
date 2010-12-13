package org.irods.jargon.part.policydriven;

import static org.irods.jargon.core.pub.aohelper.AOHelper.AND;
import static org.irods.jargon.core.pub.aohelper.AOHelper.COMMA;
import static org.irods.jargon.core.pub.aohelper.AOHelper.EQUALS_AND_QUOTE;
import static org.irods.jargon.core.pub.aohelper.AOHelper.QUOTE;
import static org.irods.jargon.core.pub.aohelper.AOHelper.WHERE;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSGenQueryExecutor;
import org.irods.jargon.core.pub.aohelper.DataAOHelper;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.pub.io.IRODSFileReader;
import org.irods.jargon.core.pub.io.IRODSFileWriter;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.AVUQueryOperatorEnum;
import org.irods.jargon.core.query.IRODSQuery;
import org.irods.jargon.core.query.IRODSQueryResultSet;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.core.query.AVUQueryElement.AVUQueryPart;
import org.irods.jargon.part.exception.DataNotFoundException;
import org.irods.jargon.part.exception.DuplicateDataException;
import org.irods.jargon.part.policy.domain.Policy;
import org.irods.jargon.part.policy.xmlserialize.ObjectToXMLMarshaller;
import org.irods.jargon.part.policy.xmlserialize.XMLToObjectUnmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage policies and policy repositories
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public final class PolicyManagerImpl extends AbstractPolicyDrivenManager
		implements PolicyManager {

	public final static Logger log = LoggerFactory
			.getLogger(PolicyManagerImpl.class);

	public PolicyManagerImpl(
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount)
			throws PolicyDrivenServiceConfigException {
		super(irodsAccessObjectFactory, irodsAccount);
	}

	/**
	 * Method simply returns a list of all policies available.
	 * 
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.part.policydriven.PolicyDrivenServiceListingEntry}
	 *         which has identifying data about the policy.
	 * 
	 * @throws PartException
	 */
	public List<PolicyDrivenServiceListingEntry> listAllPolicies()
			throws PolicyDrivenServiceConfigException {
		try {
			final AVUQueryElement element = AVUQueryElement
					.instanceForValueQuery(
							AVUQueryPart.ATTRIBUTE,
							AVUQueryOperatorEnum.EQUAL,
							PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_POLICY_MARKER_ATTRIBUTE);
			final List<AVUQueryElement> elements = new ArrayList<AVUQueryElement>();
			elements.add(element);
			log.debug("policy repositories found: {}", elements);
			return this
					.buildServiceListingBasedOnDataObjectMetadataQuery(elements);
		} catch (JargonQueryException e) {
			log.error("jargon query exception building metadata query", e);
			throw new PolicyDrivenServiceConfigException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.part.policydriven.PolicyManager#listPolicyRepositories()
	 */
	public List<PolicyDrivenServiceListingEntry> listPolicyRepositories()
			throws PolicyDrivenServiceConfigException {
		log.info("listing policy repositories...");
		try {
			final AVUQueryElement element = AVUQueryElement
					.instanceForValueQuery(
							AVUQueryPart.ATTRIBUTE,
							AVUQueryOperatorEnum.EQUAL,
							PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_POLICY_REPOSITORY_MARKER_ATTRIBUTE);
			final List<AVUQueryElement> elements = new ArrayList<AVUQueryElement>();
			elements.add(element);
			log.debug("policy repositories found: {}", elements);
			return this
					.buildServiceListingBasedOnCollectionMetadataQuery(elements);
		} catch (JargonQueryException e) {
			log.error("jargon query exception building metadata query", e);
			throw new PolicyDrivenServiceConfigException(e);
		}
	}

	/**
	 * Define a repository of policies.
	 * 
	 * @param policyDrivenServiceListingEntry
	 * @throws DuplicateDataException
	 * @throws PartException
	 */
	public void addPolicyRepository(
			final PolicyDrivenServiceListingEntry policyDrivenServiceListingEntry)
			throws DuplicateDataException, PolicyDrivenServiceConfigException {
		if (policyDrivenServiceListingEntry == null) {
			throw new PolicyDrivenServiceConfigException(
					"null policyDrivenServiceListingEntry");
		}

		log.info("adding policy repository: {}",
				policyDrivenServiceListingEntry.toString());

		// check for uniqueness
		for (PolicyDrivenServiceListingEntry existingEntry : listPolicyRepositories()) {

			if (existingEntry.getPolicyDrivenServiceName().equals(
					policyDrivenServiceListingEntry
							.getPolicyDrivenServiceName())) {
				log.error("duplicate entry for policy repository name: {}",
						policyDrivenServiceListingEntry
								.getPolicyDrivenServiceName());
				throw new DuplicateDataException(
						"duplicate rule repository name");
			}

			if (existingEntry.getPolicyDrivenServiceAbsolutePath().equals(
					policyDrivenServiceListingEntry
							.getPolicyDrivenServiceAbsolutePath())) {
				log.error("duplicate entry for rule repository path: {}",
						policyDrivenServiceListingEntry
								.getPolicyDrivenServiceAbsolutePath());
				throw new DuplicateDataException(
						"duplicate rule repository path");
			}
		}

		// check and potentially make the given directory (can use an existing
		// dir)
		try {
			final CollectionAO collectionAO = irodsAccessObjectFactory
					.getCollectionAO(irodsAccount);
			final IRODSFile collectionForRepository = collectionAO
					.instanceIRODSFileForCollectionPath(policyDrivenServiceListingEntry
							.getPolicyDrivenServiceAbsolutePath());

			// FIXME: canwrite checks and add to service-driven apps dir
			if (collectionForRepository.exists()) {
				log.debug("collection exists");
				makeSureCollectionIsNotAFile(collectionForRepository);
				log.debug("archive not a file");
			} else {
				log
						.debug("archive does not exist, make dirs up to this archive");
				final boolean dirsMade = collectionForRepository.mkdirs();
				if (!dirsMade) {
					throw new PolicyDrivenServiceConfigException(
							"unable to make directory for policy repository");
				}
			}

			// tag the directory as a service driven application
			markCollectionAsService(policyDrivenServiceListingEntry,
					collectionAO);

		} catch (JargonException e) {
			log.error("jargon query exception building metadata query", e);
			throw new PolicyDrivenServiceConfigException(e);
		}

	}

	/**
	 * Given an absolute path to an iRODS file that should contain an XML
	 * version of a policy, return the <code>Policy</code> object.
	 * 
	 * @param absolutePathOfPolicyFile
	 *            <code>String</code> to a valid XML file in a policy repository
	 *            that contains a serialized XML policy.
	 * @param unmarshaller
	 *            {@link org.irods.jargon.part.policy.xmlserialize.XMLToObjectUnmarshaller}
	 *            that can turn the XML into a <code>Policy</code> object.
	 * @return {@link org.irods.jargon.part.policy.domain.Policy} with the
	 *         policy data.
	 * @throws DataNotFoundException
	 *             if the policy file does not exist or is not marked as a
	 *             policy
	 * @throws PartException
	 */
	public Policy getPolicyFromPolicyRepository(final String policyName,
			final XMLToObjectUnmarshaller unmarshaller)
			throws DataNotFoundException, PolicyDrivenServiceConfigException {

		if (policyName == null || policyName.isEmpty()) {
			throw new PolicyDrivenServiceConfigException(
					"absolutePathOfPolicyFile is null or empty");
		}

		log.info("looking up policy at {}", policyName);

		PolicyDrivenServiceListingEntry policyEntry = this
				.findPolicyBasicData(policyName);
		// look up the policy file and make sure it's a policy
		IRODSFile irodsFile;
		try {
			DataObjectAO dataObjectAO = irodsAccessObjectFactory
					.getDataObjectAO(irodsAccount);
			log.debug("getting irodsFile for the policyFile");
			irodsFile = dataObjectAO.instanceIRODSFileForPath(policyEntry
					.getPolicyDrivenServiceAbsolutePath());

		} catch (Exception e) {
			log.error("error looking for policy file: {}", policyName, e);
			throw new PolicyDrivenServiceConfigException(
					"error looking for policy file named:" + policyName, e);
		}

		// I have a policy, now get the details
		Policy policy;
		try {
			final IRODSFileFactory irodsFileFactory = irodsAccessObjectFactory
					.getIRODSFileFactory(irodsAccount);
			final IRODSFileReader reader = irodsFileFactory
					.instanceIRODSFileReader(irodsFile.getAbsolutePath());
			log.debug("unmarshalling");
			policy = unmarshaller.unmarshallXMLToPolicy(reader);
			reader.close();
		} catch (Exception e) {
			log.error("exception unmarshalling policy", e);
			throw new PolicyDrivenServiceConfigException(e);
		}

		log.debug("successfully retrieved policy: {}", policy);
		return policy;
	}

	/**
	 * Add a policy to a repository
	 * 
	 * @param policyRepositoryName
	 *            <code>String</code> with unique name for the policy repository
	 * @param policy
	 *            {@link org.irods.jargon.part.policy.domain.Policy} that
	 *            encapsulates the policy
	 * @param marshaller
	 *            {@link org.irods.jargon.part.policy.xmlserialize.ObjectToXMLMarshaller}
	 *            that can store the Policy as an XML file.
	 * @throws PartException
	 * @throws DuplicateDataException
	 */
	public void addPolicyToRepository(final String policyRepositoryName,
			final Policy policy, final ObjectToXMLMarshaller marshaller)
			throws PolicyDrivenServiceConfigException, DuplicateDataException {

		if (policyRepositoryName == null || policyRepositoryName.isEmpty()) {
			throw new PolicyDrivenServiceConfigException(
					"policyRepositoryName is null or empty");
		}

		if (policy == null) {
			throw new PolicyDrivenServiceConfigException("policy is empty");
		}

		// TODO: validate policy contents
		log.info("adding policy: {}", policy);
		log.info("policy to be added to {}", policyRepositoryName);

		// see if repository exists

		log.debug("does policy repository exist?");

		PolicyDrivenServiceListingEntry policyRepository;
		try {
			policyRepository = findPolicyRepository(policyRepositoryName);
		} catch (DataNotFoundException e) {
			log.error("policy config repository missing for {}",
					policyRepositoryName);
				throw new PolicyDrivenServiceConfigException(
					"no policy repository with name " + policyRepositoryName
							+ " policy cannot be added");
		}

		// see if policy name is unique
		log
				.debug(
						"policy repository found, look for policy to make sure it is unique for policy name: {}",
						policy.getPolicyName());

		boolean policyUnique = false;

		try {
			findPolicyBasicData(policy.getPolicyName());
		} catch (DataNotFoundException e) {
			log.info("policy entry not found for given policy, it is unique");
			policyUnique = true;
		}

		if (!policyUnique) {
			throw new DuplicateDataException("the policy name is not unique:"
					+ policy.getPolicyName());
		}

		// update the location of the policy
		final StringBuilder policyPath = new StringBuilder();
		policyPath
				.append(policyRepository.getPolicyDrivenServiceAbsolutePath());
		policyPath.append('/');
		policyPath.append(URLEncoder.encode(policy.getPolicyName()));
		policyPath.append(".xml");
		final String policyPathAsString = policyPath.toString();
		log.debug("path for policy config file is: {}", policyPath);

		// store the policy as XML
		try {
			final IRODSFileFactory irodsFileFactory = irodsAccessObjectFactory
					.getIRODSFileFactory(irodsAccount);
			final IRODSFileWriter writer = irodsFileFactory
					.instanceIRODSFileWriter(policyPathAsString);
			marshaller.marshallPolicyToXML(writer, policy);
		} catch (Exception e) {
			log.error("exception storing policy", e);
			throw new PolicyDrivenServiceConfigException(e);
		}

		log.debug("mark file as policy");
		// mark the stored file as a policy
		try {
			this.markFileAsPolicy(policy, policyPathAsString);
		} catch (JargonException e) {
			throw new PolicyDrivenServiceConfigException(e);
		}

		log.info("policy was added");
	}

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
	public PolicyDrivenServiceListingEntry findPolicyRepository(
			final String repositoryName) throws DataNotFoundException,
			PolicyDrivenServiceConfigException {

		if (repositoryName == null || repositoryName.isEmpty()) {
			throw new PolicyDrivenServiceConfigException(
					"null or missing repository name");
		}

		log.info("looking up policy: {}", repositoryName);
		log.debug("does policy exist?");

		try {

			final List<AVUQueryElement> elements = new ArrayList<AVUQueryElement>();

			AVUQueryElement element = AVUQueryElement
					.instanceForValueQuery(
							AVUQueryPart.ATTRIBUTE,
							AVUQueryOperatorEnum.EQUAL,
							PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_POLICY_REPOSITORY_MARKER_ATTRIBUTE);
			elements.add(element);

			element = AVUQueryElement.instanceForValueQuery(AVUQueryPart.VALUE,
					AVUQueryOperatorEnum.EQUAL, repositoryName);
			elements.add(element);

			final List<PolicyDrivenServiceListingEntry> servicesFound = buildServiceListingBasedOnCollectionMetadataQuery(elements);

			if (servicesFound.isEmpty()) {
				log.error("no policy repository found for: {}", repositoryName);
				throw new DataNotFoundException(
						"no policy repository found for:" + repositoryName);
			}

			if (servicesFound.size() > 1) {
				log.error("multiple policy repositories found for: {}",
						repositoryName);
				throw new PolicyDrivenServiceConfigException(
						"duplicate service data found for:" + repositoryName);
			}

			log.debug("found repository: {}", servicesFound.get(0));
			return servicesFound.get(0);

		} catch (DataNotFoundException dnf) {
			log.warn("data was not found for policy repository:{}",
					repositoryName);
			throw dnf;
		} catch (Exception e) {
			log.error("exception caught finding policy repository", e);
			throw new PolicyDrivenServiceConfigException(e);
		}
	}

	/**
	 * Locate basic information about a policy using the given name
	 * 
	 * @param policyName
	 *            <code>String</code> with the policy name as stored in the AVU
	 *            'value' field. This points to an iRODS collection with the
	 *            policy marker attribute.
	 * @return
	 * @throws DataNotFoundException
	 *             when no repository exists with the given name
	 * @throws PartException
	 *             other exception
	 */
	public PolicyDrivenServiceListingEntry findPolicyBasicData(
			final String policyName) throws DataNotFoundException,
			PolicyDrivenServiceConfigException {

		if (policyName == null || policyName.isEmpty()) {
			throw new PolicyDrivenServiceConfigException(
					"null or missing policy name");
		}

		log.info("looking up policy: {}", policyName);
		log.debug("does policy exist?");

		try {

			final List<AVUQueryElement> elements = new ArrayList<AVUQueryElement>();

			AVUQueryElement element = AVUQueryElement
					.instanceForValueQuery(
							AVUQueryPart.ATTRIBUTE,
							AVUQueryOperatorEnum.EQUAL,
							PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_POLICY_MARKER_ATTRIBUTE);
			elements.add(element);
			element = AVUQueryElement.instanceForValueQuery(AVUQueryPart.VALUE,
					AVUQueryOperatorEnum.EQUAL, policyName);
			elements.add(element);

			element = AVUQueryElement.instanceForValueQuery(AVUQueryPart.VALUE,
					AVUQueryOperatorEnum.EQUAL, policyName);
			elements.add(element);

			DataObjectAO dataObjectAO = irodsAccessObjectFactory
					.getDataObjectAO(irodsAccount);

			List<MetaDataAndDomainData> metaDataAndDomainDataList = dataObjectAO
					.findMetadataValuesByMetadataQuery(elements);

			if (metaDataAndDomainDataList.isEmpty()) {
				throw new DataNotFoundException(
						"no policy found for policy name: " + policyName);
			} else if (metaDataAndDomainDataList.size() != 1) {
				throw new PolicyDrivenServiceConfigException(
						"duplicate policies found for: " + policyName);
			}

			MetaDataAndDomainData metaDataAndDomainData = metaDataAndDomainDataList
					.get(0);

			PolicyDrivenServiceListingEntry serviceListingEntry = PolicyDrivenServiceListingEntry
					.instance(metaDataAndDomainData.getAvuValue(),
							metaDataAndDomainData.getDomainObjectUniqueName(),
							metaDataAndDomainData.getAvuUnit());

			log.debug("found policy: {}", serviceListingEntry);
			return serviceListingEntry;
		} catch (DataNotFoundException dnf) {
			log.warn("data was not found for policy:{}", policyName);
			throw dnf;
		} catch (Exception e) {
			log.error("exception caught finding policy", e);
			throw new PolicyDrivenServiceConfigException(e);
		}
	}

	/**
	 * Find the list of the policies that are stored in the given policy
	 * repository.
	 * 
	 * @param policyRepositoryName
	 *            <code>String</code> with the unique name of a policy
	 *            repository
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.part.policydriven.PolicyDrivenServiceListingEntry}
	 *         describing the policies
	 * @throws DataNotFoundException
	 *             thrown if the given policy repository does not exist.
	 * @throws PartException
	 */
	public List<PolicyDrivenServiceListingEntry> listPoliciesInPolicyRepository(
			final String policyRepositoryName) throws DataNotFoundException,
			PolicyDrivenServiceConfigException {

		if (policyRepositoryName == null || policyRepositoryName.isEmpty()) {
			throw new PolicyDrivenServiceConfigException(
					"no pollicy repository found for repository name:"
							+ policyRepositoryName);
		}

		log.info("building a list of the policies in repository: {}",
				policyRepositoryName);
		final PolicyDrivenServiceListingEntry policyRepository = this
				.findPolicyRepository(policyRepositoryName);

		// The policy repository is a collection, so gather the files marked as
		// policy files in the repository collection
		// knowing the collection abs path, find the data objects that are in
		// the collection that have the marker AVU attribute.

		IRODSGenQueryExecutor irodsGenQueryExecutor;
		try {
			irodsGenQueryExecutor = irodsAccessObjectFactory
					.getIRODSGenQueryExecutor(irodsAccount);
		} catch (JargonException e) {
			log.error("jargon exception finding policy files", e);
			throw new PolicyDrivenServiceConfigException(
					"error finding policy files", e);
		}
		DataAOHelper dataAOHelper = new DataAOHelper();

		// build the irods iquest query to get the data
		String query = buildPolicyListQuery(policyRepository, dataAOHelper);
		log.debug("query for marked rules: {}", query);

		IRODSQuery irodsQuery;
		try {
			irodsQuery = IRODSQuery.instance(query, 500);
		} catch (JargonException e) {
			log.error("jargon exception formulating query", e);
			throw new PolicyDrivenServiceConfigException(
					"error generating query to find policy files", e);
		}

		log.debug("executing query to get policies");
		IRODSQueryResultSet resultSet;
		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQuery(irodsQuery, 0);
		} catch (Exception e) {
			log.error("jargon exception formulating query", e);
			throw new PolicyDrivenServiceConfigException(
					"error generating query to find policy files", e);
		}

		return buildPolicyList(resultSet);

	}

	/**
	 * @param policyRepository
	 * @param dataAOHelper
	 * @return
	 */
	String buildPolicyListQuery(
			final PolicyDrivenServiceListingEntry policyRepository,
			final DataAOHelper dataAOHelper) {
		final StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		sb.append(RodsGenQueryEnum.COL_D_DATA_ID.getName());
		sb.append(COMMA);
		sb.append(RodsGenQueryEnum.COL_COLL_NAME.getName());
		sb.append(COMMA);
		sb.append(RodsGenQueryEnum.COL_DATA_NAME.getName());
		sb.append(COMMA);
		sb.append(dataAOHelper.buildMetadataSelects());
		sb.append(WHERE);
		sb.append(RodsGenQueryEnum.COL_COLL_NAME.getName());
		sb.append(EQUALS_AND_QUOTE);
		sb.append(policyRepository.getPolicyDrivenServiceAbsolutePath());
		sb.append(QUOTE);
		sb.append(AND);
		sb.append(RodsGenQueryEnum.COL_META_DATA_ATTR_NAME.getName());
		sb.append(EQUALS_AND_QUOTE);
		sb
				.append(PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_POLICY_MARKER_ATTRIBUTE);
		sb.append(QUOTE);

		String query = sb.toString();
		return query;
	}

	/**
	 * @param resultSet
	 * @return
	 * @throws PartException
	 */
	List<PolicyDrivenServiceListingEntry> buildPolicyList(
			final IRODSQueryResultSet resultSet)
			throws PolicyDrivenServiceConfigException {
		final List<PolicyDrivenServiceListingEntry> policies = new ArrayList<PolicyDrivenServiceListingEntry>();

		DataAOHelper dataAOHelper = new DataAOHelper();

		List<MetaDataAndDomainData> dataObjectMetadata;
		try {   
			dataObjectMetadata = dataAOHelper.buildMetaDataAndDomainDataListFromResultSet(resultSet);
		} catch (Exception e) {
			log.error("exception caught building policy list", e);
			throw new PolicyDrivenServiceConfigException(e);
		}

		PolicyDrivenServiceListingEntry listingEntry;

		for (MetaDataAndDomainData metaData : dataObjectMetadata) {

			listingEntry = PolicyDrivenServiceListingEntry.instance(metaData
					.getAvuValue(), metaData.getDomainObjectUniqueName(),
					metaData.getAvuUnit());

			log.debug("adding entry for policy list:{}", listingEntry);
			policies.add(listingEntry);
		}
		return policies;
	}

	/**
	 * @param serviceListing
	 * @param collectionAO
	 * @param collection
	 * @throws JargonException
	 */
	private void markFileAsPolicy(final Policy policy,
			final String irodsPolicyFileAbsolutePath) throws JargonException {
		AvuData avuMarker = AvuData
				.instance(
						PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_POLICY_MARKER_ATTRIBUTE,
						policy.getPolicyName(), policy
								.getPolicyTextualDescription());
		log.info("adding marker for policy: {}", policy.getPolicyName());
		log.info("on path: {}", irodsPolicyFileAbsolutePath);
		DataObjectAO dataObjectAO = irodsAccessObjectFactory
				.getDataObjectAO(irodsAccount);
		dataObjectAO.addAVUMetadata(irodsPolicyFileAbsolutePath, avuMarker);
	}

	/**
	 * @param serviceListing
	 * @param collectionAO
	 * @param collection
	 * @throws JargonException
	 */
	private void markCollectionAsService(
			final PolicyDrivenServiceListingEntry serviceListing,
			final CollectionAO collectionAO) throws JargonException {
		AvuData avuMarker = AvuData
				.instance(
						PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_POLICY_REPOSITORY_MARKER_ATTRIBUTE,
						serviceListing.getPolicyDrivenServiceName(),
						serviceListing.getComment());
		log.info("adding marker for policy repository: {}", serviceListing
				.getPolicyDrivenServiceName());
		log.info("on path: {}", serviceListing
				.getPolicyDrivenServiceAbsolutePath());
		collectionAO.addAVUMetadata(serviceListing
				.getPolicyDrivenServiceAbsolutePath(), avuMarker);
	}

}
