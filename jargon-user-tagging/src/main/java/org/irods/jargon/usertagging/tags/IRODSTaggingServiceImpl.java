/**
 *
 */
package org.irods.jargon.usertagging.tags;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.AVUQueryElement.AVUQueryPart;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.core.query.MetaDataAndDomainData.MetadataDomain;
import org.irods.jargon.core.query.QueryConditionOperators;
import org.irods.jargon.usertagging.AbstractIRODSTaggingService;
import org.irods.jargon.usertagging.domain.IRODSTagValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for processing IRODS free tags. This method provides services on top
 * of Jargon to maintain user-defined tags on iRODS domain objects. The service
 * also allows a single 'comment' or description on a file or collection per
 * user.
 * <p>
 * Note that tags are by user. Various signatures within this service either
 * default to the logged-in user, or utilize the user passed in as part of the
 * method parameters. Please note carefully the comments for each method to
 * ensure that this is appropriately controlled. This service does not attempt
 * to do any edits of which user is updating which tag.
 * <p>
 * The {@code FreeTaggingService} is appropriate for end-user interfaces, and
 * does ensure that tag query/maintenance operations are done as the logged-in
 * user. Generally, the caller of this lower level service is responsible for
 * allowing or preventing updates on behalf of other users.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public final class IRODSTaggingServiceImpl extends AbstractIRODSTaggingService implements IRODSTaggingService {

	public static final Logger log = LoggerFactory.getLogger(IRODSTaggingServiceImpl.class);

	/**
	 * Static initializer used to create instances of the service.
	 *
	 * @param irodsAccessObjectFactory
	 *            {@code IRODSAccessObjectFactory} that can create various iRODS
	 *            Access Objects.
	 * @param irodsAccount
	 *            {@code IRODSAccount} that describes the target server and
	 *            credentials.
	 * @return instance of the {@code IRODSTaggingServiceImpl}
	 */
	public static IRODSTaggingService instance(final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) {
		return new IRODSTaggingServiceImpl(irodsAccessObjectFactory, irodsAccount);
	}

	/**
	 * Private constructor that initializes the service with access to objects that
	 * interact with iRODS.
	 *
	 * @param irodsAccessObjectFactory
	 *            {@code IRODSAccessObjectFactory} that can create various iRODS
	 *            Access Objects.
	 * @param irodsAccount
	 *            {@code IRODSAccount} that describes the target server and
	 *            credentials.
	 * @throws JargonException
	 */
	private IRODSTaggingServiceImpl(final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) {

		super(irodsAccessObjectFactory, irodsAccount);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.usertagging.IRODSTaggingService#addTagToDataObject(java
	 * .lang.String, org.irods.jargon.usertagging.domain.IRODSTagValue)
	 */
	@Override
	public void addTagToDataObject(final String dataObjectAbsolutePath, final IRODSTagValue irodsTagValue)
			throws JargonException, DuplicateDataException, DataNotFoundException {

		if (dataObjectAbsolutePath == null || dataObjectAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty dataObjectAbsolutepath");
		}

		if (irodsTagValue == null) {
			throw new IllegalArgumentException("null irodsTagValue");
		}

		log.info("adding tag:{}", irodsTagValue);
		log.info("to data object:{}", dataObjectAbsolutePath);

		AvuData avuData = AvuData.instance(irodsTagValue.getTagData(), irodsTagValue.getTagUser(),
				UserTaggingConstants.TAG_AVU_UNIT);

		DataObjectAO dataObjectAO = irodsAccessObjectFactory.getDataObjectAO(irodsAccount);
		try {
			dataObjectAO.addAVUMetadata(dataObjectAbsolutePath, avuData);
		} catch (FileNotFoundException fnf) {
			throw new DataNotFoundException("did not find data object in query", fnf);
		}
		log.debug("tag added successfully");

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.usertagging.IRODSTaggingService#
	 * checkAndUpdateDescriptionOnDataObject(java.lang.String,
	 * org.irods.jargon.usertagging.domain.IRODSTagValue)
	 */
	@Override
	public void checkAndUpdateDescriptionOnDataObject(final String dataObjectAbsolutePath,
			final IRODSTagValue irodsDescriptionValue) throws JargonException {
		IRODSTagValue currentIrodsDescriptionValue = getDescriptionOnDataObjectForLoggedInUser(dataObjectAbsolutePath);

		if (currentIrodsDescriptionValue == null) {
			log.info("no existing description, do an add");
			if (!irodsDescriptionValue.getTagData().isEmpty()) {
				addDescriptionToDataObject(dataObjectAbsolutePath, irodsDescriptionValue);
			}
			return;
		}

		/*
		 * See if the description has changed, if now empty, delete, if changed, update
		 */

		if (irodsDescriptionValue == null || irodsDescriptionValue.getTagData().isEmpty()) {
			log.info("description is being deleted:{}", irodsDescriptionValue);
			deleteDescriptionFromDataObject(dataObjectAbsolutePath, currentIrodsDescriptionValue);
			return;
		}

		if (irodsDescriptionValue.getTagData().equals(currentIrodsDescriptionValue.getTagData())) {
			log.info("no change in description, no update");
			return;
		}

		/*
		 * Update metadata
		 */

		try {

			deleteDescriptionFromDataObject(dataObjectAbsolutePath, currentIrodsDescriptionValue);
			addDescriptionToDataObject(dataObjectAbsolutePath, irodsDescriptionValue);
		} catch (FileNotFoundException fnf) {
			throw new DataNotFoundException("did not find data object in query", fnf);
		}
		log.info("update done");

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.usertagging.IRODSTaggingService#
	 * checkAndUpdateDescriptionOnCollection(java.lang.String,
	 * org.irods.jargon.usertagging.domain.IRODSTagValue)
	 */
	@Override
	public void checkAndUpdateDescriptionOnCollection(final String collectionAbsolutePath,
			final IRODSTagValue irodsDescriptionValue) throws JargonException {
		IRODSTagValue currentIrodsDescriptionValue = getDescriptionOnCollectionForLoggedInUser(collectionAbsolutePath);

		if (currentIrodsDescriptionValue == null) {
			log.info("no existing description, do an add");
			if (!irodsDescriptionValue.getTagData().isEmpty()) {
				addDescriptionToCollection(collectionAbsolutePath, irodsDescriptionValue);
			}
			return;
		}

		/*
		 * See if the description has changed, if now empty, delete, if changed, update
		 */

		if (irodsDescriptionValue == null || irodsDescriptionValue.getTagData().isEmpty()) {
			log.info("description is being deleted:{}", irodsDescriptionValue);
			deleteDescriptionFromCollection(collectionAbsolutePath, currentIrodsDescriptionValue);
			return;
		}

		if (irodsDescriptionValue.getTagData().equals(currentIrodsDescriptionValue.getTagData())) {
			log.info("no change in description, no update");
			return;
		}

		/*
		 * Update metadata
		 */

		deleteDescriptionFromCollection(collectionAbsolutePath, currentIrodsDescriptionValue);
		addDescriptionToCollection(collectionAbsolutePath, irodsDescriptionValue);
		log.info("update done");

	}

	@Override
	public void addDescriptionToDataObject(final String dataObjectAbsolutePath,
			final IRODSTagValue irodsDescriptionValue) throws JargonException, DataNotFoundException {

		if (dataObjectAbsolutePath == null || dataObjectAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty dataObjectAbsolutepath");
		}

		if (irodsDescriptionValue == null) {
			throw new IllegalArgumentException("null irodsDescriptionValue");
		}

		log.info("adding description:{}", irodsDescriptionValue);
		log.info("to data object:{}", dataObjectAbsolutePath);
		IRODSTagValue current = getDescriptionOnDataObjectForLoggedInUser(dataObjectAbsolutePath);

		if (irodsDescriptionValue.getTagData().isEmpty()) {
			log.info("empty description will be treated as a deletion, look up existing...");
			if (current != null) {
				deleteDescriptionFromDataObject(dataObjectAbsolutePath, current);
			}
		} else {
			if (current == null) {
				log.info("no previous description, just add");
				doAddOfDescriptionToDataObject(dataObjectAbsolutePath, irodsDescriptionValue);
			} else if (!current.getTagData().equals(irodsDescriptionValue.getTagData())) {
				// description changes, delete then add
				log.info("previous description found, and is changed, so delete and add");
				deleteDescriptionFromDataObject(dataObjectAbsolutePath, current);
				doAddOfDescriptionToDataObject(dataObjectAbsolutePath, irodsDescriptionValue);
			} else {
				log.info("description does not change, ignore");
			}
		}

	}

	/**
	 * @param dataObjectAbsolutePath
	 * @param irodsDescriptionValue
	 * @throws JargonException
	 * @throws DataNotFoundException
	 * @throws DuplicateDataException
	 */
	private void doAddOfDescriptionToDataObject(final String dataObjectAbsolutePath,
			final IRODSTagValue irodsDescriptionValue)
			throws JargonException, DataNotFoundException, DuplicateDataException {
		DataObjectAO dataObjectAO = irodsAccessObjectFactory.getDataObjectAO(irodsAccount);
		AvuData avuData = AvuData.instance(irodsDescriptionValue.getTagData(), irodsDescriptionValue.getTagUser(),
				UserTaggingConstants.DESCRIPTION_AVU_UNIT);
		dataObjectAO.addAVUMetadata(dataObjectAbsolutePath, avuData);
		log.info("description added successfully");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.usertagging.IRODSTaggingService#deleteTagFromDataObject
	 * (java.lang.String, org.irods.jargon.usertagging.domain.IRODSTagValue)
	 */
	@Override
	public void deleteTagFromDataObject(final String dataObjectAbsolutePath, final IRODSTagValue irodsTagValue)
			throws DataNotFoundException, JargonException {

		if (dataObjectAbsolutePath == null || dataObjectAbsolutePath.isEmpty()) {
			throw new JargonException("null or empty dataObjectAbsolutepath");
		}

		if (irodsTagValue == null) {
			throw new JargonException("null irodsTagValue");
		}

		log.info("removing tag:{}", irodsTagValue);
		log.info("from data object:{}", dataObjectAbsolutePath);

		AvuData avuData = AvuData.instance(irodsTagValue.getTagData(), irodsTagValue.getTagUser(),
				UserTaggingConstants.TAG_AVU_UNIT);

		DataObjectAO dataObjectAO = irodsAccessObjectFactory.getDataObjectAO(irodsAccount);

		try {
			dataObjectAO.deleteAVUMetadata(dataObjectAbsolutePath, avuData);
		} catch (FileNotFoundException fnf) {
			log.warn("tag AVU missing when deleting, silently ignore");
		} catch (DataNotFoundException dnf) {
			log.warn("tag AVU missing when deleting, silently ignore");
		}

		log.debug("tag removed successfully");

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.usertagging.IRODSTaggingService#
	 * deleteDescriptionFromDataObject(java.lang.String,
	 * org.irods.jargon.usertagging.domain.IRODSTagValue)
	 */
	@Override
	public void deleteDescriptionFromDataObject(final String dataObjectAbsolutePath,
			final IRODSTagValue irodsDescriptionValue) throws DataNotFoundException, JargonException {

		if (dataObjectAbsolutePath == null || dataObjectAbsolutePath.isEmpty()) {
			throw new JargonException("null or empty dataObjectAbsolutepath");
		}

		if (irodsDescriptionValue == null) {
			throw new JargonException("null irodsDescriptionValue");
		}

		log.info("removing description:{}", irodsDescriptionValue);
		log.info("from data object:{}", dataObjectAbsolutePath);

		AvuData avuData = AvuData.instance(irodsDescriptionValue.getTagData(), irodsDescriptionValue.getTagUser(),
				UserTaggingConstants.DESCRIPTION_AVU_UNIT);

		DataObjectAO dataObjectAO = irodsAccessObjectFactory.getDataObjectAO(irodsAccount);

		try {
			dataObjectAO.deleteAVUMetadata(dataObjectAbsolutePath, avuData);
		} catch (FileNotFoundException fnf) {
			log.warn("tag AVU missing when deleting, silently ignore");
		} catch (DataNotFoundException dnf) {
			log.warn("tag AVU missing when deleting, silently ignore");
		}

		log.debug("description removed successfully");

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.usertagging.IRODSTaggingService#
	 * deleteDescriptionFromCollection(java.lang.String,
	 * org.irods.jargon.usertagging.domain.IRODSTagValue)
	 */
	@Override
	public void deleteDescriptionFromCollection(final String collectionAbsolutePath,
			final IRODSTagValue irodsDescriptionValue) throws DataNotFoundException, JargonException {

		if (collectionAbsolutePath == null || collectionAbsolutePath.isEmpty()) {
			throw new JargonException("null or empty collectionAbsolutePath");
		}

		if (irodsDescriptionValue == null) {
			throw new JargonException("null irodsDescriptionValue");
		}

		log.info("removing description:{}", irodsDescriptionValue);
		log.info("from collection:{}", collectionAbsolutePath);

		AvuData avuData = AvuData.instance(irodsDescriptionValue.getTagData(), irodsDescriptionValue.getTagUser(),
				UserTaggingConstants.DESCRIPTION_AVU_UNIT);

		CollectionAO collectionAO = irodsAccessObjectFactory.getCollectionAO(irodsAccount);

		try {
			collectionAO.deleteAVUMetadata(collectionAbsolutePath, avuData);
		} catch (FileNotFoundException fnf) {
			log.warn("tag AVU missing when deleting, silently ignore");
		} catch (DataNotFoundException dnf) {
			log.warn("tag AVU missing when deleting, silently ignore");
		}

		log.debug("description removed successfully");

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.usertagging.IRODSTaggingService#getTagsOnDataObject(
	 * java.lang.String)
	 */
	@Override
	public List<IRODSTagValue> getTagsOnDataObject(final String dataObjectAbsolutePath)
			throws DataNotFoundException, JargonException {

		if (dataObjectAbsolutePath == null || dataObjectAbsolutePath.isEmpty()) {
			throw new JargonException("null or empty dataObjectAbsolutepath");
		}

		log.info("getTagsOnDataObject:{}", dataObjectAbsolutePath);

		List<AVUQueryElement> avuQueryElements = new ArrayList<>();
		try {
			avuQueryElements.add(AVUQueryElement.instanceForValueQuery(AVUQueryPart.UNITS,
					QueryConditionOperators.EQUAL, UserTaggingConstants.TAG_AVU_UNIT));
			avuQueryElements.add(AVUQueryElement.instanceForValueQuery(AVUQueryPart.VALUE,
					QueryConditionOperators.EQUAL, getIrodsAccount().getUserName()));

		} catch (JargonQueryException e) {
			log.error("error on metadata query, rethrow as JargonException", e);
			throw new JargonException(e);
		}

		DataObjectAO dataObjectAO = irodsAccessObjectFactory.getDataObjectAO(irodsAccount);
		IRODSFile dataFile = irodsAccessObjectFactory.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(dataObjectAbsolutePath);
		List<MetaDataAndDomainData> queryResults;

		try {
			queryResults = dataObjectAO.findMetadataValuesForDataObjectUsingAVUQuery(avuQueryElements,
					dataFile.getParent(), dataFile.getName());
		} catch (FileNotFoundException fnf) {
			throw new DataNotFoundException("file not found querying for data", fnf);
		} catch (JargonQueryException e) {
			log.error("error on metadata query, rethrow as JargonException", e);
			throw new JargonException(e);
		}

		log.debug("got results from query:{}, process as tag objects", queryResults);

		List<IRODSTagValue> resultValues = new ArrayList<>();

		for (MetaDataAndDomainData metadataAndDomainData : queryResults) {
			resultValues.add(new IRODSTagValue(metadataAndDomainData));
		}

		return resultValues;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.usertagging.IRODSTaggingService#
	 * getDescriptionOnDataObjectForLoggedInUser(java.lang.String)
	 */
	@Override
	public IRODSTagValue getDescriptionOnDataObjectForLoggedInUser(final String dataObjectAbsolutePath)
			throws JargonException {

		if (dataObjectAbsolutePath == null || dataObjectAbsolutePath.isEmpty()) {
			throw new JargonException("null or empty dataObjectAbsolutepath");
		}

		log.info("getDescriptionOnDataObjectForLoggedInUser:{}", dataObjectAbsolutePath);

		List<AVUQueryElement> avuQueryElements = new ArrayList<>();
		try {
			avuQueryElements.add(AVUQueryElement.instanceForValueQuery(AVUQueryPart.UNITS,
					QueryConditionOperators.EQUAL, UserTaggingConstants.DESCRIPTION_AVU_UNIT));
			avuQueryElements.add(AVUQueryElement.instanceForValueQuery(AVUQueryPart.VALUE,
					QueryConditionOperators.EQUAL, getIrodsAccount().getUserName()));

		} catch (JargonQueryException e) {
			log.error("error on metadata query, rethrow as JargonException", e);
			throw new JargonException(e);
		}

		DataObjectAO dataObjectAO = irodsAccessObjectFactory.getDataObjectAO(irodsAccount);
		IRODSFile dataFile = irodsAccessObjectFactory.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(dataObjectAbsolutePath);
		List<MetaDataAndDomainData> queryResults;

		try {
			queryResults = dataObjectAO.findMetadataValuesForDataObjectUsingAVUQuery(avuQueryElements,
					dataFile.getParent(), dataFile.getName());
		} catch (FileNotFoundException fnf) {
			throw new DataNotFoundException("did not find data object in query", fnf);
		} catch (JargonQueryException e) {
			log.error("error on metadata query, rethrow as JargonException", e);
			throw new JargonException(e);
		}

		log.debug("got results from query:{}, process as description objects", queryResults);

		IRODSTagValue descriptionValue = null;

		if (!queryResults.isEmpty()) {
			descriptionValue = new IRODSTagValue(queryResults.get(0));
		}

		return descriptionValue;

	}

	@Override
	public IRODSTagValue getDescriptionOnCollectionForLoggedInUser(final String collectionAbsolutePath)
			throws JargonException {

		if (collectionAbsolutePath == null || collectionAbsolutePath.isEmpty()) {
			throw new JargonException("null or empty collectionAbsolutePath");
		}

		log.info("getDescriptionOnCollectionForLoggedInUser:{}", collectionAbsolutePath);

		List<AVUQueryElement> avuQueryElements = new ArrayList<>();
		try {

			avuQueryElements.add(AVUQueryElement.instanceForValueQuery(AVUQueryPart.UNITS,
					QueryConditionOperators.EQUAL, UserTaggingConstants.DESCRIPTION_AVU_UNIT));
			avuQueryElements.add(AVUQueryElement.instanceForValueQuery(AVUQueryPart.VALUE,
					QueryConditionOperators.EQUAL, getIrodsAccount().getUserName()));

		} catch (JargonQueryException e) {
			log.error("error on metadata query, rethrow as JargonException", e);
			throw new JargonException(e);
		}

		CollectionAO collectionAO = irodsAccessObjectFactory.getCollectionAO(irodsAccount);

		List<MetaDataAndDomainData> queryResults;

		try {
			queryResults = collectionAO.findMetadataValuesByMetadataQueryForCollection(avuQueryElements,
					collectionAbsolutePath);
		} catch (FileNotFoundException fnf) {
			throw new DataNotFoundException("did not find data object in query", fnf);
		} catch (JargonQueryException e) {
			log.error("error on metadata query, rethrow as JargonException", e);
			throw new JargonException(e);
		}

		log.debug("got results from query:{}, process as description objects", queryResults);

		IRODSTagValue descriptionValue = null;

		if (!queryResults.isEmpty()) {
			descriptionValue = new IRODSTagValue(queryResults.get(0));
		}

		return descriptionValue;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.usertagging.IRODSTaggingService#addTagToCollection(java
	 * .lang.String, org.irods.jargon.usertagging.domain.IRODSTagValue)
	 */
	@Override
	public void addTagToCollection(final String collectionAbsolutePath, final IRODSTagValue irodsTagValue)
			throws JargonException, DuplicateDataException, DataNotFoundException {

		if (collectionAbsolutePath == null || collectionAbsolutePath.isEmpty()) {
			throw new JargonException("null or empty collectionAbsolutePath");
		}

		if (irodsTagValue == null) {
			throw new JargonException("null irodsTagValue");
		}

		log.info("adding tag:{}", irodsTagValue);
		log.info("to collection:{}", collectionAbsolutePath);

		AvuData avuData = AvuData.instance(irodsTagValue.getTagData(), irodsTagValue.getTagUser(),
				UserTaggingConstants.TAG_AVU_UNIT);

		CollectionAO collectionAO = irodsAccessObjectFactory.getCollectionAO(irodsAccount);
		collectionAO.addAVUMetadata(collectionAbsolutePath, avuData);
		log.debug("tag added successfully");

	}

	@Override
	public void addDescriptionToCollection(final String collectionAbsolutePath,
			final IRODSTagValue irodsDescriptionValue) throws JargonException, DataNotFoundException {

		if (collectionAbsolutePath == null || collectionAbsolutePath.isEmpty()) {
			throw new JargonException("null or empty collectionAbsolutePath");
		}

		if (irodsDescriptionValue == null) {
			throw new JargonException("null irodsDescriptionValue");
		}

		log.info("adding description:{}", irodsDescriptionValue);
		log.info("to collection:{}", collectionAbsolutePath);
		IRODSTagValue current = getDescriptionOnCollectionForLoggedInUser(collectionAbsolutePath);

		if (irodsDescriptionValue.getTagData().isEmpty()) {
			log.info("empty description will be treated as a deletion, look up existing...");
			if (current != null) {
				deleteDescriptionFromCollection(collectionAbsolutePath, current);
			}
		} else {
			if (current == null) {
				log.info("no previous description, just add");
				doAddOfDescriptionToCollection(collectionAbsolutePath, irodsDescriptionValue);
			} else if (!current.getTagData().equals(irodsDescriptionValue.getTagData())) {
				// description changes, delete then add
				log.info("previous description found, and is changed, so delete and add");
				deleteDescriptionFromCollection(collectionAbsolutePath, current);
				doAddOfDescriptionToCollection(collectionAbsolutePath, irodsDescriptionValue);
			} else {
				log.info("description does not change, ignore");
			}
		}
	}

	/**
	 * @param collectionAbsolutePath
	 * @param irodsDescriptionValue
	 * @throws JargonException
	 * @throws DataNotFoundException
	 * @throws DuplicateDataException
	 */
	private void doAddOfDescriptionToCollection(final String collectionAbsolutePath,
			final IRODSTagValue irodsDescriptionValue)
			throws JargonException, DataNotFoundException, DuplicateDataException {
		// add this description now, any previous value will be deleted
		CollectionAO collectionAO = irodsAccessObjectFactory.getCollectionAO(irodsAccount);
		AvuData avuData = AvuData.instance(irodsDescriptionValue.getTagData(), irodsDescriptionValue.getTagUser(),
				UserTaggingConstants.DESCRIPTION_AVU_UNIT);
		collectionAO.addAVUMetadata(collectionAbsolutePath, avuData);
		log.info("description added successfully");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.usertagging.IRODSTaggingService#getTagsOnCollection(
	 * java.lang.String)
	 */
	@Override
	public List<IRODSTagValue> getTagsOnCollection(final String irodsAbsolutePath) throws JargonException {

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new JargonException("null or empty irodsAbsolutePath");
		}

		log.info("getTagsOnCollection:{}", irodsAbsolutePath);

		List<AVUQueryElement> avuQueryElements = new ArrayList<>();
		try {

			avuQueryElements.add(AVUQueryElement.instanceForValueQuery(AVUQueryPart.UNITS,
					QueryConditionOperators.EQUAL, UserTaggingConstants.TAG_AVU_UNIT));
			avuQueryElements.add(AVUQueryElement.instanceForValueQuery(AVUQueryPart.VALUE,
					QueryConditionOperators.EQUAL, getIrodsAccount().getUserName()));

		} catch (JargonQueryException e) {
			log.error("error on metadata query, rethrow as JargonException", e);
			throw new JargonException(e);
		}

		CollectionAO collectionAO = irodsAccessObjectFactory.getCollectionAO(irodsAccount);

		List<MetaDataAndDomainData> queryResults;

		try {
			queryResults = collectionAO.findMetadataValuesByMetadataQueryForCollection(avuQueryElements,
					irodsAbsolutePath);
		} catch (FileNotFoundException fnf) {
			log.error("did not find data object in query, will return empty list");
			return new ArrayList<>();
		} catch (JargonQueryException e) {
			log.error("error on metadata query, rethrow as JargonException", e);
			throw new JargonException(e);
		}

		log.debug("got results from query:{}, process as tag objects", queryResults);

		List<IRODSTagValue> resultValues = new ArrayList<>();

		for (MetaDataAndDomainData metadataAndDomainData : queryResults) {
			resultValues.add(new IRODSTagValue(metadataAndDomainData));
		}

		return resultValues;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.usertagging.IRODSTaggingService#deleteTagFromCollection
	 * (java.lang.String, org.irods.jargon.usertagging.domain.IRODSTagValue)
	 */
	@Override
	public void deleteTagFromCollection(final String irodsAbsolutePath, final IRODSTagValue irodsTagValue)
			throws DataNotFoundException, JargonException {

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new JargonException("null or empty irodsAbsolutePath");
		}

		if (irodsTagValue == null) {
			throw new JargonException("null irodsTagValue");
		}

		log.info("removing tag:{}", irodsTagValue);
		log.info("from collection:{}", irodsAbsolutePath);

		AvuData avuData = AvuData.instance(irodsTagValue.getTagData(), irodsTagValue.getTagUser(),
				UserTaggingConstants.TAG_AVU_UNIT);

		CollectionAO collectionAO = irodsAccessObjectFactory.getCollectionAO(irodsAccount);
		collectionAO.deleteAVUMetadata(irodsAbsolutePath, avuData);
		log.debug("tag removed successfully");

	}

	@Override
	public List<IRODSTagValue> getTagsBasedOnMetadataDomain(final MetadataDomain metadataDomain,
			final String domainUniqueName) throws JargonException {

		if (metadataDomain == null) {
			throw new JargonException("null metadataDomain");
		}

		if (domainUniqueName == null || domainUniqueName.isEmpty()) {
			throw new JargonException("null or empty domainUniqueName");
		}

		log.info("getTagsBasedOnMetadataDomain {}", metadataDomain);
		log.info("domain unique name:{}", domainUniqueName);

		if (metadataDomain == MetadataDomain.COLLECTION) {
			return getTagsOnCollection(domainUniqueName);
		} else if (metadataDomain == MetadataDomain.DATA) {
			return getTagsOnDataObject(domainUniqueName);
		} else {
			throw new JargonException("unsupported metadataDomain");
		}

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.usertagging.IRODSTaggingService#
	 * getDescriptionBasedOnMetadataDomain
	 * (org.irods.jargon.core.query.MetaDataAndDomainData.MetadataDomain,
	 * java.lang.String)
	 */
	@Override
	public IRODSTagValue getDescriptionBasedOnMetadataDomain(final MetadataDomain metadataDomain,
			final String domainUniqueName) throws JargonException {

		if (metadataDomain == null) {
			throw new JargonException("null metadataDomain");
		}

		if (domainUniqueName == null || domainUniqueName.isEmpty()) {
			throw new JargonException("null or empty domainUniqueName");
		}

		log.info("getDescriptionBasedOnMetadataDomain {}", metadataDomain);
		log.info("domain unique name:{}", domainUniqueName);

		if (metadataDomain == MetadataDomain.COLLECTION) {
			return getDescriptionOnCollectionForLoggedInUser(domainUniqueName);
		} else if (metadataDomain == MetadataDomain.DATA) {
			return getDescriptionOnDataObjectForLoggedInUser(domainUniqueName);
		} else {
			throw new JargonException("unsupported metadataDomain");
		}

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.usertagging.IRODSTaggingService#addTagToGivenDomain(
	 * org.irods.jargon.usertagging.domain.IRODSTagValue,
	 * org.irods.jargon.core.query.MetaDataAndDomainData.MetadataDomain,
	 * java.lang.String)
	 */
	@Override
	public void addTagToGivenDomain(final IRODSTagValue irodsTagValue, final MetadataDomain metadataDomain,
			final String domainUniqueName) throws JargonException, DuplicateDataException, DataNotFoundException {

		if (irodsTagValue == null) {
			throw new JargonException("null irodsTagValue");
		}

		if (metadataDomain == null) {
			throw new JargonException("null metadataDomain");
		}

		if (domainUniqueName == null || domainUniqueName.isEmpty()) {
			throw new JargonException("null or empty domainUniqueName");
		}

		log.info("addTagToGivenDomain {}", metadataDomain);
		log.info("domain unique name:{}", domainUniqueName);
		log.info("tag value:{}", irodsTagValue);

		if (metadataDomain == MetadataDomain.COLLECTION) {
			addTagToCollection(domainUniqueName, irodsTagValue);
		} else if (metadataDomain == MetadataDomain.DATA) {
			addTagToDataObject(domainUniqueName, irodsTagValue);
		} else {
			throw new JargonException("unsupported metadataDomain");
		}

		log.info("tag added");

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.irods.jargon.usertagging.IRODSTaggingService#removeTagFromGivenDomain
	 * (org.irods.jargon.usertagging.domain.IRODSTagValue,
	 * org.irods.jargon.core.query.MetaDataAndDomainData.MetadataDomain,
	 * java.lang.String)
	 */
	@Override
	public void removeTagFromGivenDomain(final IRODSTagValue irodsTagValue, final MetadataDomain metadataDomain,
			final String domainUniqueName) throws JargonException {

		if (irodsTagValue == null) {
			throw new JargonException("null irodsTagValue");
		}

		if (metadataDomain == null) {
			throw new JargonException("null metadataDomain");
		}

		if (domainUniqueName == null || domainUniqueName.isEmpty()) {
			throw new JargonException("null or empty domainUniqueName");
		}

		log.info("removeTagFromGivenDomain {}", metadataDomain);
		log.info("domain unique name:{}", domainUniqueName);
		log.info("tag value:{}", irodsTagValue);

		if (metadataDomain == MetadataDomain.COLLECTION) {
			deleteTagFromCollection(domainUniqueName, irodsTagValue);
		} else if (metadataDomain == MetadataDomain.DATA) {
			deleteTagFromDataObject(domainUniqueName, irodsTagValue);
		} else {
			throw new JargonException("unsupported metadataDomain");
		}

		log.info("tag deleted");

	}

}
