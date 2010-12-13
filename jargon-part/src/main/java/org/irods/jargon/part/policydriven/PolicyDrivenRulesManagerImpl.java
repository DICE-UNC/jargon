/**
 * 
 */
package org.irods.jargon.part.policydriven;

import static org.irods.jargon.core.pub.aohelper.AOHelper.AND;
import static org.irods.jargon.core.pub.aohelper.AOHelper.COMMA;
import static org.irods.jargon.core.pub.aohelper.AOHelper.EQUALS_AND_QUOTE;
import static org.irods.jargon.core.pub.aohelper.AOHelper.QUOTE;
import static org.irods.jargon.core.pub.aohelper.AOHelper.WHERE;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSGenQueryExecutor;
import org.irods.jargon.core.pub.aohelper.DataAOHelper;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.AVUQueryOperatorEnum;
import org.irods.jargon.core.query.IRODSQuery;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSet;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.core.query.AVUQueryElement.AVUQueryPart;
import org.irods.jargon.part.exception.DataNotFoundException;
import org.irods.jargon.part.exception.DuplicateDataException;
import org.irods.jargon.part.policy.domain.Policy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage mappings of metadata to rules so that they may be used in developing
 * policies. This class handles the configuration and storage of RuleMappings
 * and the management of Rule repository collections.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public final class PolicyDrivenRulesManagerImpl extends
		AbstractPolicyDrivenManager implements PolicyDrivenRulesManager {
	
	public final static Logger log = LoggerFactory.getLogger(PolicyDrivenRulesManagerImpl.class);

	public PolicyDrivenRulesManagerImpl(
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount)
			throws PolicyDrivenServiceConfigException {
		super(irodsAccessObjectFactory, irodsAccount);
	}

	/**
	 * Give a listing of all rule repositories on this server, as indicated by
	 * the rule repository AVU flag
	 * 
	 * @return
	 * @throws PartException
	 */
	public List<PolicyDrivenServiceListingEntry> findRuleRepositories()
			throws PolicyDrivenServiceConfigException {

		try {
			final AVUQueryElement element = AVUQueryElement
					.instanceForValueQuery(
							AVUQueryPart.ATTRIBUTE,
							AVUQueryOperatorEnum.EQUAL,
							PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_RULE_REPOSITORY_MARKER_ATTRIBUTE);
			final List<AVUQueryElement> elements = new ArrayList<AVUQueryElement>();
			elements.add(element);

			return this.buildServiceListingBasedOnCollectionMetadataQuery(elements);
		} catch (JargonQueryException e) {
			log.error("jargon query exception building metadata query", e);
			throw new PolicyDrivenServiceConfigException(e);
		}
	}

	/**
	 * Add a repository (global) of rule mappings to the iRODS server. These
	 * mappings are then available across applications
	 * 
	 * @param policyDrivenServiceListing
	 *            {@link org.irods.jargon.part.policydriven.PolicyDrivenServiceListing}
	 *            with properties for the listing
	 * @throws PartException
	 */
	public void addRuleRepository(
			final PolicyDrivenServiceListingEntry policyDrivenServiceListingEntry)
			throws DuplicateDataException, PolicyDrivenServiceConfigException {

		if (policyDrivenServiceListingEntry == null) {
			throw new PolicyDrivenServiceConfigException(
					"null policyDrivenServiceConfig");
		}

		log.info("adding rule mapping: {}", policyDrivenServiceListingEntry
				.toString());

		// check for uniqueness
		for (PolicyDrivenServiceListingEntry existingEntry : findRuleRepositories()) {

			if (existingEntry.getPolicyDrivenServiceName().equals(
					policyDrivenServiceListingEntry
							.getPolicyDrivenServiceName())) {
				log.error("duplicate entry for rule repository name: {}",
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
							"unable to make directory for rule repository");
				}
			}

			// tag the directory as a service driven application
			markCollectionAsService(policyDrivenServiceListingEntry,
					collectionAO, collectionForRepository);

		} catch (JargonException e) {
			log.error("jargon query exception building metadata query", e);
			throw new PolicyDrivenServiceConfigException(e);
		}
	}

	/**
	 * Given a named rule repository, generate a list of the rules that exist in
	 * the repository
	 * 
	 * @param repositoryName
	 *            <code>String</code> with name (not path) of a rule repository
	 *            to query
	 * @return {@link org.irods.jargon.part.policydriven.PolicyDrivenServiceListingEntry}
	 *         with references to rules in the given repository.
	 * @throws DataNotFoundException
	 *             thrown when the rule repository does not exist
	 * @throws PartException
	 */
	public List<PolicyDrivenServiceListingEntry> listRulesInRepository(
			final String repositoryName) throws DataNotFoundException,
			PolicyDrivenServiceConfigException {

		if (repositoryName == null || repositoryName.isEmpty()) {
			throw new PolicyDrivenServiceConfigException(
					"null or missing repository name");
		}

		PolicyDrivenServiceListingEntry ruleRepository;
		try {

			// find the rule repository directory, the rules will be in there
			ruleRepository = findRuleRepository(repositoryName);

			log.debug("found rule repository located at: {}", ruleRepository
					.getPolicyDrivenServiceAbsolutePath());
			
			// knowing the collection abs path, find the data objects that are in the collection that have the marker AVU attribute.
			
			IRODSGenQueryExecutor irodsGenQueryExecutor = irodsAccessObjectFactory
					.getIRODSGenQueryExecutor(irodsAccount);
			DataAOHelper dataAOHelper = new DataAOHelper();

			final StringBuilder sb = new StringBuilder();
			sb.append("SELECT ");
			sb.append(RodsGenQueryEnum.COL_D_DATA_ID.getName());
			sb.append(COMMA);
			sb.append(RodsGenQueryEnum.COL_DATA_NAME.getName());
			sb.append(COMMA);
			sb.append(dataAOHelper.buildMetadataSelects());
			sb.append(WHERE);
			sb.append(RodsGenQueryEnum.COL_COLL_NAME.getName());
			sb.append(EQUALS_AND_QUOTE);
			sb.append(ruleRepository.getPolicyDrivenServiceAbsolutePath());
			sb.append(QUOTE);
			sb.append(AND);
			sb.append(RodsGenQueryEnum.COL_META_DATA_ATTR_NAME.getName());
			sb.append(EQUALS_AND_QUOTE);
			sb
					.append(PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_RULE_DEFINITION_MARKER_ATTRIBUTE);
			sb.append(QUOTE);

			String query = sb.toString();
			log.debug("query for marked rules: {}", query);
			IRODSQuery irodsQuery = IRODSQuery.instance(query, 500);

			IRODSQueryResultSet resultSet = irodsGenQueryExecutor
					.executeIRODSQuery(irodsQuery, 0);
			
			final List<PolicyDrivenServiceListingEntry> ruleData = new ArrayList<PolicyDrivenServiceListingEntry>();

			PolicyDrivenServiceListingEntry listingEntry;
			for (IRODSQueryResultRow row : resultSet.getResults()) {
				listingEntry = PolicyDrivenServiceListingEntry.instance(row.getColumn(2), row.getColumn(1), row.getColumn(4));
				log.debug("adding entry  for rule mapping:{}", listingEntry);
				ruleData.add(listingEntry);
			}

		return ruleData;

		} catch (DataNotFoundException e) {
			log.error("no repository found");
			throw e;
		} catch (Exception e) {
			log.error("exception finding rule mappings", e);
			throw new PolicyDrivenServiceConfigException(
					"error finding rule mappings", e);
		}

	}

	/**
	 * Locate basic information about a rule repository using the given name
	 * 
	 * @param repositoryName
	 *            <code>String</code> with the repository name as stored in the
	 *            AVU 'value' field. This points to an iRODS collection with the
	 *            rule repository marker attribute.
	 * @return
	 * @throws DataNotFoundException
	 *             when no repository exists with the given name
	 * @throws PartException
	 *             other exception
	 */
	public PolicyDrivenServiceListingEntry findRuleRepository(
			final String repositoryName) throws DataNotFoundException,
			PolicyDrivenServiceConfigException {

		if (repositoryName == null || repositoryName.isEmpty()) {
			throw new PolicyDrivenServiceConfigException(
					"null or missing repository name");
		}

		log.info("listing rules in repository: {}", repositoryName);
		log.debug("does policy exist?");

		try {

			List<AVUQueryElement> elements = new ArrayList<AVUQueryElement>();

			AVUQueryElement element = AVUQueryElement
					.instanceForValueQuery(
							AVUQueryPart.ATTRIBUTE,
							AVUQueryOperatorEnum.EQUAL,
							PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_RULE_REPOSITORY_MARKER_ATTRIBUTE);
			elements.add(element);

			element = AVUQueryElement.instanceForValueQuery(AVUQueryPart.VALUE,
					AVUQueryOperatorEnum.EQUAL, repositoryName);
			elements.add(element);

			List<PolicyDrivenServiceListingEntry> servicesFound = buildServiceListingBasedOnCollectionMetadataQuery(elements);

			if (servicesFound.isEmpty()) {
				log.error("no ruleRepository found for: {}", repositoryName);
				throw new DataNotFoundException("no rule repository found for:"
						+ repositoryName);
			}

			if (servicesFound.size() > 1) {
				log.error("multiple rule repositories found for: {}",
						repositoryName);
				throw new PolicyDrivenServiceConfigException(
						"duplicate service data found for:" + repositoryName);
			}

			log.debug("found repository: {}", servicesFound.get(0));
			return servicesFound.get(0);

		} catch (DataNotFoundException dnf) {
			log.warn("data was not found for rule repository:{}",
					repositoryName);
			throw dnf;
		} catch (Exception e) {
			log.error("exception caught finding rule repository", e);
			throw new PolicyDrivenServiceConfigException(e);
		}
	}
	


	/**
	 * @param serviceListing
	 * @param collectionAO
	 * @param collection
	 * @throws JargonException
	 */
	private void markCollectionAsService(
			 final PolicyDrivenServiceListingEntry serviceListing,
			final CollectionAO collectionAO, final IRODSFile collection)
			throws JargonException {
		AvuData avuMarker = AvuData
				.instance(
						PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_RULE_REPOSITORY_MARKER_ATTRIBUTE,
						serviceListing.getPolicyDrivenServiceName(),
						serviceListing.getComment());
		log.info("adding marker for rule repository: {}", serviceListing
				.getPolicyDrivenServiceName());
		log.info("on path: {}", serviceListing
				.getPolicyDrivenServiceAbsolutePath());
		collectionAO.addAVUMetadata(serviceListing
				.getPolicyDrivenServiceAbsolutePath(), avuMarker);
	}

}
