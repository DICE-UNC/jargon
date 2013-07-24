/**
 * 
 */
package org.irods.jargon.usertagging.starring;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.AVUQueryElement.AVUQueryPart;
import org.irods.jargon.core.query.AVUQueryOperatorEnum;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.irods.jargon.usertagging.AbstractIRODSTaggingService;
import org.irods.jargon.usertagging.domain.IRODSStarredFileOrCollection;
import org.irods.jargon.usertagging.tags.UserTaggingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A service to star or favorite files or folders
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class IRODSStarringServiceImpl extends AbstractIRODSTaggingService
		implements IRODSStarringService {

	public static final Logger log = LoggerFactory
			.getLogger(IRODSStarringServiceImpl.class);

	/**
	 * @param irodsAccessObjectFactory
	 *            {@link IRODSAccessObjectFactory} to create iRODS services
	 * @param irodsAccount
	 *            {@link IRODSAccount} that describes the iRODS server and user
	 */
	public IRODSStarringServiceImpl(
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.usertagging.starring.IRODSStarringService#
	 * findStarredForAbsolutePath(java.lang.String)
	 */
	@Override
	public IRODSStarredFileOrCollection findStarredForAbsolutePath(
			final String irodsAbsolutePath) throws FileNotFoundException,
			JargonException {

		log.info("findStarredForAbsolutePath");

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePath");
		}

		log.info("absolute path:{}", irodsAbsolutePath);

		log.info("deciding whether a file or collection...");
		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = getIrodsAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(getIrodsAccount());

		ObjStat objStat = collectionAndDataObjectListAndSearchAO
				.retrieveObjectStatForPath(irodsAbsolutePath);

		return findStarredGivenObjStat(irodsAbsolutePath, objStat);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.usertagging.starring.IRODSStarringService#
	 * starFileOrCollection(java.lang.String, java.lang.String)
	 */
	@Override
	public void starFileOrCollection(final String irodsAbsolutePath,
			final String description) throws FileNotFoundException,
			JargonException {

		log.info("starFileOrCollection()");

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePath");
		}

		if (description == null) {
			throw new IllegalArgumentException("null description");
		}

		log.info("irodsAbsolutePath:{}", irodsAbsolutePath);
		log.info("description:{}", description);
		log.info("for user:{}", irodsAccount.getUserName());

		String myDescr;
		if (description.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			sb.append("Starred at:");
			sb.append(new Date());
			myDescr = sb.toString();
		} else {
			myDescr = description;
		}

		log.info("deciding whether a file or collection...");
		ObjStat objStat = getObjStatForAbsolutePath(irodsAbsolutePath);

		/*
		 * If I find a starred folder already, then update the AVU data, which
		 * will just be the description, otherwise add one.
		 */
		IRODSStarredFileOrCollection irodsStarredFileOrCollection = findStarredGivenObjStat(
				irodsAbsolutePath, objStat);

		log.info("no starring already, so add...");
		AvuData avuData = AvuData.instance(myDescr, getIrodsAccount()
				.getUserName(), UserTaggingConstants.STAR_AVU_UNIT);

		if (irodsStarredFileOrCollection == null) {
			log.info("this is an add");
			addMetadataForStarring(irodsAbsolutePath, objStat, avuData);
		} else {
			log.info("starred data found, so update description");
			modifyMetadataForStarringGivenCurrent(irodsStarredFileOrCollection,
					irodsAbsolutePath, objStat, myDescr);
		}

		log.info("updated");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.usertagging.starring.IRODSStarringService#
	 * unstarFileOrCollection(java.lang.String)
	 */
	@Override
	public void unstarFileOrCollection(final String irodsAbsolutePath)
			throws FileNotFoundException, JargonException {

		log.info("unstarFileOrCollection()");

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePath");
		}

		log.info("irodsAbsolutePath:{}", irodsAbsolutePath);
		log.info("deciding whether a file or collection...");
		ObjStat objStat = getObjStatForAbsolutePath(irodsAbsolutePath);

		/*
		 * If I find a starred folder already, then update the AVU data, which
		 * will just be the description, otherwise add one.
		 */
		IRODSStarredFileOrCollection irodsStarredFileOrCollection = findStarredGivenObjStat(
				irodsAbsolutePath, objStat);

		if (irodsStarredFileOrCollection == null) {
			log.info("no star found for path, treat as successful delete");
			return;
		}

		AvuData avuData = AvuData.instance(
				irodsStarredFileOrCollection.getDescription(),
				getIrodsAccount().getUserName(),
				UserTaggingConstants.STAR_AVU_UNIT);

		log.info("deleting AVU:{}", avuData);

		if (objStat.isSomeTypeOfCollection()) {
			CollectionAO collectionAO = getIrodsAccessObjectFactory()
					.getCollectionAO(getIrodsAccount());
			collectionAO.deleteAVUMetadata(irodsAbsolutePath, avuData);
		} else {
			DataObjectAO dataObjectAO = irodsAccessObjectFactory
					.getDataObjectAO(irodsAccount);
			dataObjectAO.deleteAVUMetadata(irodsAbsolutePath, avuData);
		}

		log.info("unstarred");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.usertagging.starring.IRODSStarringService#
	 * listStarredCollections(int)
	 */
	@Override
	public List<IRODSStarredFileOrCollection> listStarredCollections(
			final int pagingOffset) throws JargonException {

		log.info("listStarredCollections()");

		List<AVUQueryElement> avuQueryElements = buildAVUQueryForStarred();

		List<IRODSStarredFileOrCollection> irodsStarredFiles = new ArrayList<IRODSStarredFileOrCollection>();

		// Do collections, then do files

		log.info("querying metadata as a collection to look for starred");
		CollectionAO collectionAO = getIrodsAccessObjectFactory()
				.getCollectionAO(getIrodsAccount());
		try {
			List<MetaDataAndDomainData> metadata = collectionAO
					.findMetadataValuesByMetadataQuery(avuQueryElements);

			for (MetaDataAndDomainData metadataAndDomainData : metadata) {
				log.debug("adding starred file:{}", metadataAndDomainData);
				irodsStarredFiles
						.add(this
								.transformMetadataValueToStarValue(metadataAndDomainData));
			}

		} catch (JargonQueryException e) {
			throw new JargonException("error querying for metadata", e);
		}

		return irodsStarredFiles;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.usertagging.starring.IRODSStarringService#
	 * listStarredDataObjects(int)
	 */
	@Override
	public List<IRODSStarredFileOrCollection> listStarredDataObjects(
			final int pagingOffset) throws JargonException {

		log.info("listStarredDataObjects()");

		List<AVUQueryElement> avuQueryElements = buildAVUQueryForStarred();

		List<IRODSStarredFileOrCollection> irodsStarredFiles = new ArrayList<IRODSStarredFileOrCollection>();

		// Do collections, then do files

		log.info("querying metadata as a data object to look for starred");
		DataObjectAO dataObjectAO = getIrodsAccessObjectFactory()
				.getDataObjectAO(getIrodsAccount());
		try {
			List<MetaDataAndDomainData> metadata = dataObjectAO
					.findMetadataValuesByMetadataQuery(avuQueryElements);

			for (MetaDataAndDomainData metadataAndDomainData : metadata) {
				log.debug("adding starred file:{}", metadataAndDomainData);
				irodsStarredFiles
						.add(this
								.transformMetadataValueToStarValue(metadataAndDomainData));
			}

		} catch (JargonQueryException e) {
			throw new JargonException("error querying for metadata", e);
		}

		return irodsStarredFiles;

	}

	/**
	 * @return
	 * @throws JargonException
	 */
	private List<AVUQueryElement> buildAVUQueryForStarred()
			throws JargonException {
		List<AVUQueryElement> avuQueryElements = new ArrayList<AVUQueryElement>();
		try {
			avuQueryElements.add(AVUQueryElement.instanceForValueQuery(
					AVUQueryPart.UNITS, AVUQueryOperatorEnum.EQUAL,
					UserTaggingConstants.STAR_AVU_UNIT));
			avuQueryElements.add(AVUQueryElement.instanceForValueQuery(
					AVUQueryPart.VALUE, AVUQueryOperatorEnum.EQUAL,
					getIrodsAccount().getUserName()));
		} catch (JargonQueryException e) {
			log.error("error on metadata query, rethrow as JargonException", e);
			throw new JargonException(e);
		}
		return avuQueryElements;
	}

	/**
	 * Given a current value of a 'starred' file or folder, and the desired
	 * description, update the starred metadata. This works for data objects or
	 * collections.
	 * 
	 * @param irodsStarredFileOrCollection
	 * @param irodsAbsolutePath
	 * @param objStat
	 */
	private void modifyMetadataForStarringGivenCurrent(
			final IRODSStarredFileOrCollection irodsStarredFileOrCollection,
			final String irodsAbsolutePath, final ObjStat objStat,
			final String description) throws JargonException {

		// construct the 'prior AVU'

		AvuData avuData = AvuData.instance(
				irodsStarredFileOrCollection.getDescription(),
				getIrodsAccount().getUserName(),
				UserTaggingConstants.STAR_AVU_UNIT);

		AvuData newAvuData = AvuData.instance(description, getIrodsAccount()
				.getUserName(), UserTaggingConstants.STAR_AVU_UNIT);

		if (objStat.isSomeTypeOfCollection()) {
			log.info("modifying metadata for collection");
			CollectionAO collectionAO = getIrodsAccessObjectFactory()
					.getCollectionAO(getIrodsAccount());
			collectionAO.modifyAVUMetadata(irodsAbsolutePath, avuData,
					newAvuData);
		} else {
			log.info("modifying metadata for data object");
			DataObjectAO dataObjectAO = irodsAccessObjectFactory
					.getDataObjectAO(irodsAccount);
			dataObjectAO.modifyAVUMetadata(irodsAbsolutePath, avuData,
					newAvuData);
		}

		log.info("modified");
	}

	/**
	 * @param irodsAbsolutePath
	 * @param objStat
	 * @param avuData
	 * @throws JargonException
	 * @throws DataNotFoundException
	 * @throws DuplicateDataException
	 */
	private void addMetadataForStarring(final String irodsAbsolutePath,
			final ObjStat objStat, final AvuData avuData)
			throws JargonException, DataNotFoundException,
			DuplicateDataException {
		if (objStat.isSomeTypeOfCollection()) {
			CollectionAO collectionAO = getIrodsAccessObjectFactory()
					.getCollectionAO(getIrodsAccount());
			collectionAO.addAVUMetadata(irodsAbsolutePath, avuData);
		} else {
			DataObjectAO dataObjectAO = irodsAccessObjectFactory
					.getDataObjectAO(irodsAccount);
			dataObjectAO.addAVUMetadata(irodsAbsolutePath, avuData);
		}
	}

	/**
	 * Given a metadata value (from iRODS AVUs), return the corresponding
	 * starred object
	 * 
	 * @param metadataAndDomainData
	 *            {@link MetaDataAndDomainData} with the AVU data
	 * @return {@link IRODSStarredFileOrCollection} with the transformed data
	 * @throws JargonException
	 */

	private IRODSStarredFileOrCollection transformMetadataValueToStarValue(
			final MetaDataAndDomainData metadataAndDomainData)
			throws JargonException {

		log.info("transformMetadataValueToStarValue()");

		if (metadataAndDomainData == null) {
			throw new IllegalArgumentException("null metadataAndDomainData");
		}

		log.info("metadataAndDomainData:{}", metadataAndDomainData);

		if (!metadataAndDomainData.getAvuUnit().equals(
				UserTaggingConstants.STAR_AVU_UNIT)) {
			throw new IllegalArgumentException("metadata is not a starred item");
		}

		IRODSStarredFileOrCollection irodsStarredFileOrCollection = new IRODSStarredFileOrCollection(
				metadataAndDomainData.getMetadataDomain(),
				metadataAndDomainData.getDomainObjectUniqueName(),
				metadataAndDomainData.getAvuAttribute(),
				metadataAndDomainData.getAvuValue());

		irodsStarredFileOrCollection.setCount(metadataAndDomainData.getCount());
		irodsStarredFileOrCollection.setLastResult(metadataAndDomainData
				.isLastResult());
		return irodsStarredFileOrCollection;

	}

	/**
	 * @param irodsAbsolutePath
	 * @param objStat
	 * @return
	 * @throws JargonException
	 */
	private IRODSStarredFileOrCollection findStarredGivenObjStat(
			final String irodsAbsolutePath, final ObjStat objStat)
			throws JargonException {

		MiscIRODSUtils.evaluateSpecCollSupport(objStat);

		List<MetaDataAndDomainData> queryResults;

		List<AVUQueryElement> avuQueryElements = buildAVUQueryForStarred();

		if (objStat.isSomeTypeOfCollection()) {
			log.info("querying metadata as a collection to look for starred");
			CollectionAO collectionAO = getIrodsAccessObjectFactory()
					.getCollectionAO(getIrodsAccount());
			try {
				queryResults = collectionAO
						.findMetadataValuesByMetadataQueryForCollection(
								avuQueryElements, irodsAbsolutePath);
			} catch (JargonQueryException e) {
				throw new JargonException("error querying for metadata", e);
			}
		} else {
			log.info("querying metadata as a data object to look for starred");
			DataObjectAO dataObjectAO = irodsAccessObjectFactory
					.getDataObjectAO(irodsAccount);
			IRODSFile dataFile = irodsAccessObjectFactory.getIRODSFileFactory(
					irodsAccount).instanceIRODSFile(irodsAbsolutePath);
			try {
				queryResults = dataObjectAO
						.findMetadataValuesForDataObjectUsingAVUQuery(
								avuQueryElements, dataFile.getParent(),
								dataFile.getName());
			} catch (JargonQueryException e) {
				throw new JargonException("error querying for metadata", e);
			}
		}

		if (queryResults.isEmpty()) {
			return null;
		} else {
			return transformMetadataValueToStarValue(queryResults.get(0));
		}
	}

}
