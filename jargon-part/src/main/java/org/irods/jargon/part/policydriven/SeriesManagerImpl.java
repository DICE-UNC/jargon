/**
 * 
 */
package org.irods.jargon.part.policydriven;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.AVUQueryOperatorEnum;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.core.query.AVUQueryElement.AVUQueryPart;
import org.irods.jargon.part.exception.DataNotFoundException;
import org.irods.jargon.part.policy.domain.Policy;
import org.irods.jargon.part.policy.domain.Series;
import org.irods.jargon.part.policy.xmlserialize.XMLToObjectUnmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manager of series, which is a collection of related objects. The series may
 * have a policy applied to it, and then objects can be added to the series.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public final class SeriesManagerImpl extends AbstractPolicyDrivenManager
		implements SeriesManager {

	public static Logger log = LoggerFactory.getLogger(SeriesManagerImpl.class);
	private CollectionAO collectionAO = null;

	/**
	 * Constructor that includes a reference to a service that can unmarshall
	 * XML to an object
	 * 
	 * @param irodsAccessObjectFactory
	 *            <code>IRODSAccessObjectFactoryImpl</code> to access basic iRODS
	 *            services.
	 * @param irodsAccount
	 *            <code>IRODSAccount</code> with information about the
	 *            connected-to service and identity
	 * 
	 * @throws PartException
	 */
	public SeriesManagerImpl(
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount)
			throws PolicyDrivenServiceConfigException {
		super(irodsAccessObjectFactory, irodsAccount);
		
		try {
			collectionAO = irodsAccessObjectFactory.getCollectionAO(irodsAccount);
		} catch (JargonException e) {
			log.error("jargon exception getting a collectionAO", e);
			throw new PolicyDrivenServiceConfigException(e);
		}
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.part.policydriven.SeriesManager#listSeries(java.lang
	 * .String)
	 */
	public List<PolicyDrivenServiceListingEntry> listSeries(
			final String serviceDrivenApplicationName)
			throws PolicyDrivenServiceConfigException {

		if (serviceDrivenApplicationName == null
				|| serviceDrivenApplicationName.isEmpty()) {
			throw new PolicyDrivenServiceConfigException(
					"null or missing serviceDrivenApplicationName");
		}

		/*
		 * Each series will have a mapping that will point to the service driven
		 * app name. Find avu's that indicate a series, and map to the given
		 * series name. The AVU's are on the collection level
		 * FIXME: series name is not included here, need to get the series name as well
		 */

		try {
			final List<AVUQueryElement> elements = new ArrayList<AVUQueryElement>();

			elements
					.add(AVUQueryElement
							.instanceForValueQuery(
									AVUQueryPart.ATTRIBUTE,
									AVUQueryOperatorEnum.EQUAL,
									PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_SERIES_TO_APP_MARKER_ATTRIBUTE));

			elements.add(AVUQueryElement.instanceForValueQuery(
					AVUQueryPart.VALUE, AVUQueryOperatorEnum.EQUAL,
					serviceDrivenApplicationName));

			log.info("querying for series in application: {}",
					serviceDrivenApplicationName);

			return this
					.buildServiceListingBasedOnCollectionMetadataQuery(elements);

		} catch (JargonQueryException e) {
			log.error("jargon query exception building metadata query", e);
			throw new PolicyDrivenServiceConfigException(e);
		}
	}

	/**
	 * Add the given series to the application based on the given data in the
	 * <code>Series</code> object. The series must be: uniquely named, part of
	 * an existing policy driven service, bound to an existing policy, for a
	 * collection (that will be created if non-existent) that is not already
	 * associated with a series.
	 * 
	 * This method will create a directory as the root of the series, and set up
	 * the appropriate AVU's to link the series to the policy and application.
	 * 
	 * @param series
	 *            {@link org.irods.jargon.part.policydriven.domain.Series}
	 *            containing a specification for the series to be added.
	 * @param unmarshaller
	 *            <code>XmlToObjectUnmarshaller</code> that can convert XML to
	 *            domain objects.
	 * @throws PartException
	 */
	public void addSeriesToApplication(final Series series,
			final XMLToObjectUnmarshaller unmarshaller)
			throws PolicyDrivenServiceConfigException {

		if (series == null) {
			throw new PolicyDrivenServiceConfigException("series was null");
		}

		if (unmarshaller == null) {
			throw new PolicyDrivenServiceConfigException(
					"unmarshaller was null");
		}

		// TODO: other series edits

		// verify the service driven app name
		final PolicyDrivenServiceManager policyDrivenService = new PolicyDrivenServiceManagerImpl(
				irodsAccessObjectFactory, irodsAccount);

		final PolicyDrivenServiceConfig policyDrivenServiceListingEntry = policyDrivenService
				.getPolicyDrivenServiceConfigFromServiceName(series
						.getContainingServiceName());

		if (policyDrivenServiceListingEntry == null) {
			log.error("no policy driven service found with name: {}", series
					.getContainingServiceName());
		}

		final boolean isUnique = checkIfSeriesIsUnique(series.getName(), series
				.getCollectionAbsolutePath());

		if (!isUnique) {
			throw new PolicyDrivenServiceConfigException("The series name: "
					+ series.getName() + " is not unique");
		}

		// verify the policy bound to the series
		final PolicyManager policyManager = new PolicyManagerImpl(
				irodsAccessObjectFactory, irodsAccount);

		Policy boundPolicy;
		try {
			boundPolicy = policyManager.getPolicyFromPolicyRepository(series
					.getBoundPolicyName(), unmarshaller);
		} catch (DataNotFoundException e1) {
			log.error("no policy found for {}", series.getBoundPolicyName());
			throw new PolicyDrivenServiceConfigException(
					"no policy found for name:" + series.getBoundPolicyName());
		}

		log.debug("bound policy is: {}", boundPolicy);

		// TODO: do stuff with the policy later

		// I have the series info, now create the series collection (if needed)
		// and mark it

		try {
			final IRODSFile collectionForSeries = collectionAO
					.instanceIRODSFileForCollectionPath(series
							.getCollectionAbsolutePath());

			if (collectionForSeries.exists()) {
				log.debug("series directory exists");
				makeSureCollectionIsNotAFile(collectionForSeries);
				log.debug("archive not a file");
			} else {
				log
						.debug("archive does not exist, make dirs up to this archive");
				boolean dirsMade = collectionForSeries.mkdirs();
				if (!dirsMade) {
					throw new PolicyDrivenServiceConfigException(
							"unable to make directory for a series");
				}
			}

			markCollectionAsSeries(series);
			
			// add AVU's from policy to series collection
			bindMetadataToSeriesForPolicyElements(boundPolicy, series);

		} catch (JargonException e) {
			log.error("jargon query exception building metadata query", e);
			throw new PolicyDrivenServiceConfigException(e);
		}

	}

	private void markCollectionAsSeries(final Series series)
			throws PolicyDrivenServiceConfigException {
		AvuData avuMarkerSeriesToService;
		AvuData avuMarkerSeriesToPolicy;

		try {
			
			avuMarkerSeriesToService = AvuData
					.instance(
							PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_SERIES_TO_APP_MARKER_ATTRIBUTE,
							series.getContainingServiceName(), series
									.getDescription());

			avuMarkerSeriesToPolicy = AvuData
					.instance(
							PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_SERIES_TO_POLICY_MARKER_ATTRIBUTE,
							series.getBoundPolicyName(), series
									.getDescription());

		} catch (JargonException e) {
			log
					.error(
							"An error occurred marking the collection as a series",
							e);
			throw new PolicyDrivenServiceConfigException(e);
		}
		log.info("adding marker for file as a series: {}", series.getName());
		log.info("on path: {}", series.getCollectionAbsolutePath());
		
		try {
			
			collectionAO.addAVUMetadata(series.getCollectionAbsolutePath(),
					avuMarkerSeriesToService);
			collectionAO.addAVUMetadata(series.getCollectionAbsolutePath(),
					avuMarkerSeriesToPolicy);
			

		} catch (Exception e) {
			log
					.error(
							"An error occurred marking the collection as a series",
							e);
			throw new PolicyDrivenServiceConfigException(e);
		}
	}

	/**
	 * Verify that a series is unique in name, and that a series does not exist
	 * for the given collection.
	 * 
	 * @param name
	 *            <code>String</code> with the name of the series.
	 * @param collectionAbsolutePath
	 *            <code>String</code> with the absolute path to a collection in
	 *            iRODS that represents the series
	 * @return <code>boolean</code> that will be <code>true</code> if the series
	 *         is unique;
	 * @throws PartException
	 */
	public boolean checkIfSeriesIsUnique(final String name,
			final String collectionAbsolutePath)
			throws PolicyDrivenServiceConfigException {

		boolean unique = true;

		// see if another series with the same name
		try {
			final List<AVUQueryElement> elements = new ArrayList<AVUQueryElement>();

			elements
					.add(AVUQueryElement
							.instanceForValueQuery(
									AVUQueryPart.ATTRIBUTE,
									AVUQueryOperatorEnum.EQUAL,
									PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_SERIES_MARKER_ATTRIBUTE));

			elements.add(AVUQueryElement.instanceForValueQuery(
					AVUQueryPart.VALUE, AVUQueryOperatorEnum.EQUAL, name));

			log.info("querying for series named: {}", name);

			final List<PolicyDrivenServiceListingEntry> seriesEntries = buildServiceListingBasedOnCollectionMetadataQuery(elements);

			if (!seriesEntries.isEmpty()) {
				log.error("a duplicate series already exists");
				unique = false;
			}

		} catch (JargonQueryException e) {
			log.error("jargon query exception building metadata query", e);
			throw new PolicyDrivenServiceConfigException(e);
		}

		// see if the collection is already a series

		try {

			log
					.info(
							"checking to make sure there is not another series tied to collection: {}",
							collectionAbsolutePath);

			final List<AVUQueryElement> elements = new ArrayList<AVUQueryElement>();

			elements
					.add(AVUQueryElement
							.instanceForValueQuery(
									AVUQueryPart.ATTRIBUTE,
									AVUQueryOperatorEnum.EQUAL,
									PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_SERIES_MARKER_ATTRIBUTE));

			
			final List<MetaDataAndDomainData> seriesForCollection = collectionAO
					.findMetadataValuesByMetadataQueryForCollection(elements,
							collectionAbsolutePath);

			if (seriesForCollection.size() > 0) {
				log.error("A series already exists for the collection: {}",
						collectionAbsolutePath);
				unique = false;
			}

		} catch (Exception e) {
			log.error("Exception verifying that a series collection is unique",
					e);
			throw new PolicyDrivenServiceConfigException(e);
		}

		log.info("returning from checkIfSeriesIsUnique: {}", unique);
		return unique;
	}
	
	protected void bindMetadataToSeriesForPolicyElements(final Policy policy, final Series series) throws JargonException {
		
		// TODO: demo code
		
		if (policy.isRequireChecksum()) {
			log.info("adding AVU for requireChecksum to collection:{}", series.getCollectionAbsolutePath());
			AvuData avuData = AvuData.instance(SeriesManager.SERIES_ATTRIBUTE_MARKER_ATTRIBUTE, "requireChecksum", "true");
			collectionAO.addAVUMetadata(series.getCollectionAbsolutePath(), avuData);
		}
		
		if (policy.isRequireVirusScan()) {
			log.info("adding AVU for requireVirusScan to collection:{}", series.getCollectionAbsolutePath());
			AvuData avuData = AvuData.instance(SeriesManager.SERIES_ATTRIBUTE_MARKER_ATTRIBUTE, "requireVirusScan", "true");
			collectionAO.addAVUMetadata(series.getCollectionAbsolutePath(), avuData);
		}
		
		if (policy.getRetentionDays().length() > 0) {
			log.info("adding AVU for retentionDays to collection:{}", series.getCollectionAbsolutePath());
			AvuData avuData = AvuData.instance(SeriesManager.SERIES_ATTRIBUTE_MARKER_ATTRIBUTE, "retentionDays", String.valueOf(policy.getRetentionDays()));
			collectionAO.addAVUMetadata(series.getCollectionAbsolutePath(), avuData);
		}
		
		if (policy.getNumberOfReplicas() > 0) {
			log.info("adding AVU for replication to collection:{}", series.getCollectionAbsolutePath());
			AvuData avuData = AvuData.instance(SeriesManager.SERIES_ATTRIBUTE_MARKER_ATTRIBUTE, "requireReplication", String.valueOf(policy.getRetentionDays()));
			collectionAO.addAVUMetadata(series.getCollectionAbsolutePath(), avuData);
		}
		
	}

}
