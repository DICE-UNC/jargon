/**
 * 
 */
package org.irods.jargon.part.policydriven;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.AVUQueryOperatorEnum;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.core.query.AVUQueryElement.AVUQueryPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service to manage policy driven service configurations
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public final class PolicyDrivenServiceManagerImpl extends
		AbstractPolicyDrivenManager implements PolicyDrivenServiceManager {

	public Logger log = LoggerFactory.getLogger(this.getClass());

	public PolicyDrivenServiceManagerImpl(
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount)
			throws PolicyDrivenServiceConfigException {

		super(irodsAccessObjectFactory, irodsAccount);

	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.part.policydriven.PolicyDrivenServiceManager#getPolicyDrivenServiceConfigFromServiceName(java.lang.String)
	 */
	public PolicyDrivenServiceConfig getPolicyDrivenServiceConfigFromServiceName(
			final String serviceName) throws PolicyDrivenServiceConfigException {

		if (serviceName == null || serviceName.isEmpty()) {
			throw new PolicyDrivenServiceConfigException(
					"serviceName is null or blank");
		}

		log.info("getting PolicyDrivenServiceConfig for service name: {}",
				serviceName);

		final PolicyDrivenServiceConfig policyDrivenServiceConfig = new PolicyDrivenServiceConfig();

		// locate the service config top-level collection

		try {
			final CollectionAO collectionAO = irodsAccessObjectFactory
					.getCollectionAO(irodsAccount);

			// query for specific policy-driven service entry
			final List<AVUQueryElement> elements = new ArrayList<AVUQueryElement>();

			elements.add(AVUQueryElement.instanceForValueQuery(
					AVUQueryPart.ATTRIBUTE, AVUQueryOperatorEnum.EQUAL,
					POLICY_DRIVEN_SERVICE_MARKER_ATTRIBUTE));
			elements.add(AVUQueryElement
					.instanceForValueQuery(AVUQueryPart.VALUE,
							AVUQueryOperatorEnum.EQUAL, serviceName));

			// get the metadata for the archive service
			final List<MetaDataAndDomainData> metadataList = collectionAO
					.findMetadataValuesByMetadataQuery(elements);

			if (metadataList.isEmpty()) {
				return null;
			}

			if (metadataList.size() > 1) {
				log.error("duplicate service data for: {}", serviceName);
				throw new PolicyDrivenServiceConfigException(
						"multiple services found for service name: "
								+ serviceName);
			}

			final MetaDataAndDomainData metaDataAndDomainData = metadataList.get(0);

			policyDrivenServiceConfig.setServiceRootPath(metaDataAndDomainData
					.getDomainObjectUniqueName());
			policyDrivenServiceConfig.setServiceName(metaDataAndDomainData
					.getAvuValue());
			policyDrivenServiceConfig
					.setServiceDescription(metaDataAndDomainData.getAvuUnit());
			
			// find mapped rule repositories and policy repositories
			policyDrivenServiceConfig.setRuleMetadataRepositoryPath(this.findServiceRuleRepositories(serviceName));
			policyDrivenServiceConfig.setPolicyDefinitionRepositoryPath(this.findServicePolicyRepositories(serviceName));

		} catch (JargonQueryException jqe) {
			log.error("jargon query exception caught", jqe);
			throw new PolicyDrivenServiceConfigException(
					"Jargon query exception caught and rethrown", jqe);
		} catch (JargonException e) {
			log.error("jargon exception caught", e);
			throw new PolicyDrivenServiceConfigException(
					"Jargon exception caught and rethrown", e);
		}

		log
				.debug("policy driven service config: {}",
						policyDrivenServiceConfig);
		return policyDrivenServiceConfig;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.part.policydriven.PolicyDrivenServiceManager#findPolicyDrivenServiceNames(java.lang.String)
	 */
	public List<PolicyDrivenServiceListingEntry> findPolicyDrivenServiceNames(
			final String serviceFlag) throws PolicyDrivenServiceConfigException {

		if (serviceFlag == null || serviceFlag.isEmpty()) {
			throw new PolicyDrivenServiceConfigException(
					"null or missing serviceFlag");
		}

		try {
			AVUQueryElement element = AVUQueryElement.instanceForValueQuery(
					AVUQueryPart.ATTRIBUTE, AVUQueryOperatorEnum.EQUAL,
					POLICY_DRIVEN_SERVICE_MARKER_ATTRIBUTE);
			List<AVUQueryElement> elements = new ArrayList<AVUQueryElement>();
			elements.add(element);
			log.debug("building service listing");
			return this.buildServiceListingBasedOnCollectionMetadataQuery(elements);
		} catch (JargonQueryException e) {
			log.error("jargon query exception building metadata query", e);
			throw new PolicyDrivenServiceConfigException(e);
		}

	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.part.policydriven.PolicyDrivenRulesManager#findServicePolicyRepositories(java.lang.String)
	 */
	// FIXME: this method shows policy repositories, which is useful, but probably should not be mapped in the PolicyDrivenServiceConfig, that link is not needed?
	public List<PolicyDrivenServiceListingEntry> findServicePolicyRepositories(
			final String serviceName) throws PolicyDrivenServiceConfigException {
		if (serviceName == null || serviceName.isEmpty()) {
			throw new PolicyDrivenServiceConfigException(
					"null or missing serviceName");
		}

		try {
			AVUQueryElement element = AVUQueryElement.instanceForValueQuery(
					AVUQueryPart.ATTRIBUTE, AVUQueryOperatorEnum.EQUAL,
					POLICY_DRIVEN_SERVICE_POLICY_REPOSITORY_MARKER_ATTRIBUTE);
			List<AVUQueryElement> elements = new ArrayList<AVUQueryElement>();
			elements.add(element);
			return this.buildServiceListingBasedOnCollectionMetadataQuery(elements);
		} catch (JargonQueryException e) {
			log.error("jargon query exception building metadata query", e);
			throw new PolicyDrivenServiceConfigException(e);
		}
	}
	
	/**
	 * Add the given config to iRODS, which effectively creates a policy driven service.  The service top-level directory is created,
	 * as well the XML policy configuration.
	 * @param policyDrivenServiceConfig {@link org.irods.jargon.part.policydriven.PolicyDrivenServiceConfig}
	 * @throws PartException
	 */
	public void addPolicyDrivenService(PolicyDrivenServiceConfig policyDrivenServiceConfig) throws PolicyDrivenServiceConfigException {
		
		if (policyDrivenServiceConfig == null) {
			throw new PolicyDrivenServiceConfigException("null policyDrivenServiceConfig");
		}
		
		log.info("adding policy driven service: {}", policyDrivenServiceConfig.toString());
		policyDrivenServiceConfig.validate();
		
		// check for uniqueness
		PolicyDrivenServiceConfig checkCurrentConfig = getPolicyDrivenServiceConfigFromServiceName(policyDrivenServiceConfig.getServiceName());
		
		if (checkCurrentConfig != null) {
			throw new PolicyDrivenServiceConfigException("policy driven service config already exists");
		}
		
		// check and potentially make the given directory (can use an existing dir)
		try {
			CollectionAO collectionAO = irodsAccessObjectFactory.getCollectionAO(irodsAccount);
			IRODSFile collectionForArchive = collectionAO.instanceIRODSFileForCollectionPath(policyDrivenServiceConfig.getServiceRootPath());
			
			if (collectionForArchive.exists()) {
				log.debug("archive exists");
				makeSureCollectionIsNotAFile(collectionForArchive);
				log.debug("archive not a file");
			} else {
				log.debug("archive does not exist, make dirs up to this archive");
				boolean dirsMade = collectionForArchive.mkdirs();
				if (!dirsMade) {
					throw new PolicyDrivenServiceConfigException("unable to make directory for policy driven service");
				}
			}
			
			// transform the config into XML and put into the collection
			//FIXME: next step
			
			// tag the directory as a service driven application
			markCollectionAsService(policyDrivenServiceConfig, collectionAO,
					collectionForArchive);
			
		} catch (JargonException e) {
			log.error("jargon query exception building metadata query", e);
			throw new PolicyDrivenServiceConfigException(e);
		}		
	}
	
	/**
	 * @param policyDrivenServiceConfig
	 * @param collectionAO
	 * @param collectionForArchive
	 * @throws JargonException
	 */
	void markCollectionAsService(
			PolicyDrivenServiceConfig policyDrivenServiceConfig,
			CollectionAO collectionAO, IRODSFile collectionForArchive)
			throws JargonException {
		AvuData avuMarker = AvuData.instance(PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_MARKER_ATTRIBUTE,
				policyDrivenServiceConfig.getServiceName(), policyDrivenServiceConfig.getServiceDescription());
		collectionAO.addAVUMetadata(collectionForArchive.getAbsolutePath(), avuMarker);
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.part.policydriven.PolicyDrivenServiceManager#findServiceRuleRepositories(java.lang.String)
	 */
	@Override
	public List<PolicyDrivenServiceListingEntry> findServiceRuleRepositories(
			final String serviceName) throws PolicyDrivenServiceConfigException {
		if (serviceName == null || serviceName.isEmpty()) {
			throw new PolicyDrivenServiceConfigException(
					"null or missing serviceName");
		}

		try {
			AVUQueryElement element = AVUQueryElement.instanceForValueQuery(
					AVUQueryPart.ATTRIBUTE, AVUQueryOperatorEnum.EQUAL,
					POLICY_DRIVEN_SERVICE_RULE_MAPPING_MARKER_ATTRIBUTE);
			List<AVUQueryElement> elements = new ArrayList<AVUQueryElement>();
			elements.add(element);

			return this.buildServiceListingBasedOnCollectionMetadataQuery(elements);
		} catch (JargonQueryException e) {
			log.error("jargon query exception building metadata query", e);
			throw new PolicyDrivenServiceConfigException(e);
		}
	}

}
