/**
 *
 */
package org.irods.jargon.dataprofile;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.domain.Collection;
import org.irods.jargon.core.pub.domain.DataObject;
import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.core.service.AbstractJargonService;
import org.irods.jargon.core.utils.CollectionAndPath;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.irods.jargon.usertagging.domain.IRODSTagValue;
import org.irods.jargon.usertagging.tags.UserTaggingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service that will gather data associated with a Data Object or Collection
 *
 * @author Mike Conway - DICE
 *
 */
public class DataProfileServiceImpl extends AbstractJargonService implements DataProfileService {

	public static final Logger log = LoggerFactory.getLogger(DataProfileServiceImpl.class);

	private final DataTypeResolutionService dataTypeResolutionService;

	/**
	 * @param irodsAccessObjectFactory
	 * @param irodsAccount
	 */
	public DataProfileServiceImpl(final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount, final DataTypeResolutionService dataTypeResolutionService) {
		super(irodsAccessObjectFactory, irodsAccount);

		if (dataTypeResolutionService == null) {
			throw new IllegalArgumentException("null dataTypeResolutionService");
		}

		this.dataTypeResolutionService = dataTypeResolutionService;

	}

	/**
	 * Null constructor for easy mocking
	 */
	public DataProfileServiceImpl() {
		dataTypeResolutionService = null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.dataprofile.DataProfileService#retrieveDataProfile(java
	 * .lang.String)
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public DataProfile retrieveDataProfile(final String irodsAbsolutePath)
			throws FileNotFoundException, JargonException {

		log.info("retrieveDataProfile()");

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty irodsAbsolutePath");
		}

		log.info("irodsAbsolutePath:{}", irodsAbsolutePath);

		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = getIrodsAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(getIrodsAccount());
		log.info("getting objStat...");

		ObjStat objStat = collectionAndDataObjectListAndSearchAO.retrieveObjectStatForPath(irodsAbsolutePath);

		if (objStat.isSomeTypeOfCollection()) {
			return retrieveDataProfileForCollection(irodsAbsolutePath, objStat);
		} else {
			return retrieveDataProfileForDataObject(irodsAbsolutePath, objStat);
		}

	}

	private DataProfile<DataObject> retrieveDataProfileForDataObject(final String irodsAbsolutePath,
			final ObjStat objStat) throws FileNotFoundException, JargonException {

		log.info("retrieveDataProfileForDataObject()");
		DataObjectAO dataObjectAO = getIrodsAccessObjectFactory().getDataObjectAO(getIrodsAccount());
		DataObject dataObject = dataObjectAO.findByAbsolutePath(irodsAbsolutePath);
		log.info("got dataObject:{}", dataObject);

		DataProfile<DataObject> dataProfile = new DataProfile<DataObject>();
		dataProfile.setDomainObject(dataObject);
		dataProfile.setFile(true);

		log.info("get AVUs");

		dataProfile.setMetadata(dataObjectAO.findMetadataValuesForDataObject(irodsAbsolutePath));

		log.info("get ACLs...");

		dataProfile.setAcls(dataObjectAO.listPermissionsForDataObject(irodsAbsolutePath));

		log.info("look for special AVUs");

		checkIfStarred(dataProfile, getIrodsAccount().getUserName());
		checkIfShared(dataProfile, getIrodsAccount().getUserName());
		extractTags(dataProfile);
		establishDataType(dataProfile);

		dataProfile.setPathComponents(MiscIRODSUtils.breakIRODSPathIntoComponents(irodsAbsolutePath));
		CollectionAndPath collectionAndPath = MiscIRODSUtils
				.separateCollectionAndPathFromGivenAbsolutePath(irodsAbsolutePath);

		dataProfile.setParentPath(collectionAndPath.getCollectionParent());
		dataProfile.setChildName(collectionAndPath.getChildName());

		return dataProfile;

	}

	private void establishDataType(final DataProfile<DataObject> dataProfile) throws JargonException {
		log.info("establishDataType()");

		dataProfile.setMimeType(getDataTypeResolutionService()
				.resolveDataTypeWithProvidedAvuAndDataObject(dataProfile.getDomainObject(), dataProfile.getMetadata()));

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void extractTags(final DataProfile dataProfile) throws JargonException {

		List<IRODSTagValue> resultValues = new ArrayList<IRODSTagValue>();
		List<MetaDataAndDomainData> metadata = dataProfile.getMetadata();

		for (MetaDataAndDomainData metadataAndDomainData : metadata) {

			if (metadataAndDomainData.getAvuUnit().equals(UserTaggingConstants.TAG_AVU_UNIT)
					&& metadataAndDomainData.getAvuValue().equals(getIrodsAccount().getUserName())) {
				resultValues.add(new IRODSTagValue(metadataAndDomainData));

			}

		}

		dataProfile.setIrodsTagValues(resultValues);

	}

	/**
	 * See if this is a shared file or collection
	 *
	 * @param dataProfile
	 */
	@SuppressWarnings("rawtypes")
	private void checkIfShared(final DataProfile dataProfile, final String userName) {
		boolean shared = searchAvusForUnit(dataProfile, UserTaggingConstants.SHARE_AVU_UNIT, userName);
		dataProfile.setShared(shared);

	}

	/**
	 * Look for starred in AVUs
	 *
	 * @param dataProfile
	 */
	@SuppressWarnings("rawtypes")
	private void checkIfStarred(final DataProfile dataProfile, final String userName) {

		boolean starred = searchAvusForUnit(dataProfile, UserTaggingConstants.STAR_AVU_UNIT, userName);
		dataProfile.setStarred(starred);
	}

	/**
	 * Look for a special AVU in the list of retrieved AVUs
	 *
	 * @param dataProfile
	 * @param avuUnit
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private boolean searchAvusForUnit(@SuppressWarnings("rawtypes") final DataProfile dataProfile, final String avuUnit,
			final String userName) {

		if (dataProfile.getMetadata() == null) {
			throw new IllegalStateException("null metadata in dataProfile");
		}

		boolean foundValue = false;
		List<MetaDataAndDomainData> metadata = dataProfile.getMetadata();

		for (MetaDataAndDomainData metadataValue : metadata) {
			if (metadataValue.getAvuUnit().equals(avuUnit) && metadataValue.getAvuValue().equals(userName)) {
				foundValue = true;
				break;
			}
		}
		return foundValue;

	}

	private DataProfile<Collection> retrieveDataProfileForCollection(final String irodsAbsolutePath,
			final ObjStat objStat) throws JargonException {
		log.info("retrieveDataProfileForCollection()");
		CollectionAO collectionAO = getIrodsAccessObjectFactory().getCollectionAO(getIrodsAccount());
		Collection collection = collectionAO.findByAbsolutePath(irodsAbsolutePath);
		log.info("got collection:{}", collection);

		DataProfile<Collection> dataProfile = new DataProfile<Collection>();
		dataProfile.setDomainObject(collection);
		dataProfile.setFile(false);

		log.info("get AVUs");

		try {
			dataProfile.setMetadata(collectionAO.findMetadataValuesForCollection(irodsAbsolutePath));
		} catch (JargonQueryException e) {

			throw new JargonException("error querying for AVU metadata", e);
		}

		log.info("get ACLs...");

		dataProfile.setAcls(collectionAO.listPermissionsForCollection(irodsAbsolutePath));

		log.info("look for special AVUs");

		checkIfStarred(dataProfile, getIrodsAccount().getUserName());
		checkIfShared(dataProfile, getIrodsAccount().getUserName());
		extractTags(dataProfile);
		dataProfile.setMimeType("");
		dataProfile.setPathComponents(MiscIRODSUtils.breakIRODSPathIntoComponents(irodsAbsolutePath));
		CollectionAndPath collectionAndPath = MiscIRODSUtils
				.separateCollectionAndPathFromGivenAbsolutePath(irodsAbsolutePath);

		dataProfile.setParentPath(collectionAndPath.getCollectionParent());
		dataProfile.setChildName(collectionAndPath.getChildName());

		return dataProfile;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.irods.jargon.dataprofile.DataProfileService#getDataTypeResolutionService
	 * ()
	 */
	@Override
	public DataTypeResolutionService getDataTypeResolutionService() {
		return dataTypeResolutionService;
	}
}
