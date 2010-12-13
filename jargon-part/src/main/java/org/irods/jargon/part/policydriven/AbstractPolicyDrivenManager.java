package org.irods.jargon.part.policydriven;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract superclass for services that manage a PolicyDrivenService.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class AbstractPolicyDrivenManager {

	protected IRODSAccessObjectFactory irodsAccessObjectFactory = null;
	protected IRODSAccount irodsAccount = null;

	Logger log = LoggerFactory.getLogger(this.getClass());

	public AbstractPolicyDrivenManager(
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount)
			throws PolicyDrivenServiceConfigException {

		if (irodsAccessObjectFactory == null) {
			throw new PolicyDrivenServiceConfigException(
					"no access object factory available");
		}

		if (irodsAccount == null) {
			throw new PolicyDrivenServiceConfigException("IRODSAccount is null");
		}

		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
		this.irodsAccount = irodsAccount;

	}

	/**
	 * Common method to return a <code>PolicyDrivenServiceListingEntry</code> based on a passed in
	 * metadata query to a collection.  The listing contains AVU's that match the query, and information that describes the domain
	 * object that the metadata applies to.
	 * @param avuQueryElements
	 * @return
	 * @throws PartException
	 */
	public List<PolicyDrivenServiceListingEntry> buildServiceListingBasedOnCollectionMetadataQuery(
			List<AVUQueryElement> avuQueryElements)
			throws PolicyDrivenServiceConfigException {

		if (avuQueryElements == null || avuQueryElements.isEmpty()) {
			throw new PolicyDrivenServiceConfigException("null or empty query");
		}

		log.info("building service listing entries for query: {}",
				avuQueryElements);

		try {
			CollectionAO collectionAO = irodsAccessObjectFactory
					.getCollectionAO(irodsAccount);
		
			List<PolicyDrivenServiceListingEntry> policyDrivenServiceListingEntries = new ArrayList<PolicyDrivenServiceListingEntry>();

			List<MetaDataAndDomainData> metadataList = collectionAO
					.findMetadataValuesByMetadataQuery(avuQueryElements);
			for (MetaDataAndDomainData metaDataAndDomainData : metadataList) {
				policyDrivenServiceListingEntries
						.add(PolicyDrivenServiceListingEntry.instance(
								metaDataAndDomainData.getAvuValue(),
								metaDataAndDomainData
										.getDomainObjectUniqueName(),
								metaDataAndDomainData.getAvuUnit()));
			}

			log.debug("returning service listing: {}",
					policyDrivenServiceListingEntries);

			return policyDrivenServiceListingEntries;

		} catch (JargonException je) {
			log.error("JargonException when finding service names", je);
			throw new PolicyDrivenServiceConfigException(je);
		} catch (JargonQueryException jqe) {
			log.error("JargonQueryException when finding service names", jqe);
			throw new PolicyDrivenServiceConfigException(jqe);
		}
	}
	
	/**
	 * Common method to return a <code>PolicyDrivenServiceListingEntry</code> based on a passed in
	 * metadata query to a collection.  The listing contains AVU's that match the query, and information that describes the domain
	 * object that the metadata applies to.
	 * @param avuQueryElements
	 * @return
	 * @throws PartException
	 */
	public List<PolicyDrivenServiceListingEntry> buildServiceListingBasedOnDataObjectMetadataQuery(
			List<AVUQueryElement> avuQueryElements)
			throws PolicyDrivenServiceConfigException {

		if (avuQueryElements == null || avuQueryElements.isEmpty()) {
			throw new PolicyDrivenServiceConfigException("null or empty query");
		}

		log.info("building service listing based on DataObjectMetadata - entries for query: {}",
				avuQueryElements);

		try {
			DataObjectAO dataObjectAO = irodsAccessObjectFactory
					.getDataObjectAO(irodsAccount);
		
			List<PolicyDrivenServiceListingEntry> policyDrivenServiceListingEntries = new ArrayList<PolicyDrivenServiceListingEntry>();

			List<MetaDataAndDomainData> metadataList = dataObjectAO
					.findMetadataValuesByMetadataQuery(avuQueryElements);
			
			log.debug("metadata list for data object metadata query: {}", metadataList);
			
			for (MetaDataAndDomainData metaDataAndDomainData : metadataList) {
				policyDrivenServiceListingEntries
						.add(PolicyDrivenServiceListingEntry.instance(
								metaDataAndDomainData.getAvuValue(),
								metaDataAndDomainData
										.getDomainObjectUniqueName(),
								metaDataAndDomainData.getAvuUnit()));
			}

			log.debug("returning service listing: {}",
					policyDrivenServiceListingEntries);

			return policyDrivenServiceListingEntries;

		} catch (JargonException je) {
			log.error("JargonException when finding service names", je);
			throw new PolicyDrivenServiceConfigException(je);
		} catch (JargonQueryException jqe) {
			log.error("JargonQueryException when finding service names", jqe);
			throw new PolicyDrivenServiceConfigException(jqe);
		}
	}


	/**
	 * @param collectionForArchive
	 * @throws PartException
	 */
	protected void makeSureCollectionIsNotAFile(IRODSFile collectionForArchive)
			throws PolicyDrivenServiceConfigException {
				if (!collectionForArchive.isDirectory()) {
					String msg = "Given path {} exists and is not a collection:" + collectionForArchive.getAbsolutePath();
					log.error(msg);
					throw new PolicyDrivenServiceConfigException(msg);
				}
			}

}