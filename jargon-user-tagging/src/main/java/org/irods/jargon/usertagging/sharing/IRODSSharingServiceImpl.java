/**
 * 
 */
package org.irods.jargon.usertagging.sharing;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.OperationNotSupportedByIRODSVersionException;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.SpecificQueryAO;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.core.pub.domain.UserFilePermission;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.AVUQueryElement.AVUQueryPart;
import org.irods.jargon.core.query.AVUQueryOperatorEnum;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.core.query.SpecificQuery;
import org.irods.jargon.core.query.SpecificQueryResultSet;
import org.irods.jargon.core.query.MetaDataAndDomainData.MetadataDomain;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.irods.jargon.usertagging.AbstractIRODSTaggingService;
import org.irods.jargon.usertagging.domain.IRODSSharedFileOrCollection;
import org.irods.jargon.usertagging.domain.ShareUser;
import org.irods.jargon.usertagging.tags.UserTaggingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A service to share Collections and Data Objects
 * <p/>
 * Like the star and tagging facility, a share is a special metadata tag on a
 * Collection or Data Object, naming that item as shared. In the process of
 * declaring the share, the proper ACL settings are done.
 * <p/>
 * Sharing using a special tag at the 'root' of the share avoids representing
 * every file or collection in a deeply nested shared collection as 'shared', as
 * it would be based purely on the ACL settings. As a first class object, a
 * share can have an alias name, and is considered one unit.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class IRODSSharingServiceImpl extends AbstractIRODSTaggingService
		implements IRODSSharingService {

	public static final Logger log = LoggerFactory
			.getLogger(IRODSSharingServiceImpl.class);

	/**
	 * @param irodsAccessObjectFactory
	 *            {@link IRODSAccessObjectFactory} to create iRODS services
	 * @param irodsAccount
	 *            {@link IRODSAccount} that describes the iRODS server and user
	 */
	public IRODSSharingServiceImpl(
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
	}
	
	/* (non-Javadoc)
	 * @see org.irods.jargon.usertagging.sharing.IRODSSharingService#removeShare(java.lang.String)
	 */
	@Override
	public void removeShare(final String irodsAbsolutePath) throws FileNotFoundException, JargonException {
		log.info("removeShare()");
		
		if(irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty irodsAbsolutePath");
		}
	
		log.info("irodsAbsolutePath:[]", irodsAbsolutePath);

		/*
		 * Find objStat (will get file not found exception if abs path does not exist.
		 * 
		 * Look for an already existing share, if null, no delete is required
		 */
		ObjStat objStat = getObjStatForAbsolutePath(irodsAbsolutePath);

		IRODSSharedFileOrCollection irodsSharedFileOrCollection = this.findSharedGivenObjStat(irodsAbsolutePath, objStat);
		
		if (irodsSharedFileOrCollection == null) {
			log.warn("no share exists, delete action ignored...");
			return;
		}
		
		/*
		 * share exists, do the delete
		 */
		
		// TODO: should I remove acls, etc?  right now just delete the AVU
		
		AvuData avuData = buildAVUBasedOnShare(irodsSharedFileOrCollection);

		if (objStat.isSomeTypeOfCollection()) {
			log.info("calling delete on a Collection");
			CollectionAO collectionAO = this.getIrodsAccessObjectFactory().getCollectionAO(getIrodsAccount());
			collectionAO.deleteAVUMetadata(irodsSharedFileOrCollection.getDomainUniqueName(), avuData);
		} else {
			DataObjectAO dataObjectAO = this.getIrodsAccessObjectFactory().getDataObjectAO(getIrodsAccount());
			dataObjectAO.deleteAVUMetadata(irodsSharedFileOrCollection.getDomainUniqueName(), avuData);
		}
		
		log.info("delete action successful");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.usertagging.sharing.IRODSSharingService#createShare(
	 * org.irods.jargon.usertagging.domain.IRODSSharedFileOrCollection)
	 */
	@Override
	public void createShare(
			final IRODSSharedFileOrCollection irodsSharedFileOrCollection)
			throws ShareAlreadyExistsException, FileNotFoundException, JargonException {

		log.info("createShare()");

		if (irodsSharedFileOrCollection == null) {
			throw new IllegalArgumentException(
					"null irodsSharedFileOrCollection");
		}

		log.info("irodsSharedFileOrCollection:{}", irodsSharedFileOrCollection);

		log.info("deciding whether a file or collection...");
		ObjStat objStat = getObjStatForAbsolutePath(irodsSharedFileOrCollection
				.getDomainUniqueName());

		log.info("seeing if share already present..");
		IRODSSharedFileOrCollection currentSharedFile = this.findSharedGivenObjStat(irodsSharedFileOrCollection.getDomainUniqueName(), objStat);
		if (currentSharedFile != null) {
			throw new ShareAlreadyExistsException("share already exists");
		}
		
		/*
		 * OK, I can tag this as a share
		 */

		log.info("adding share tag");
		AvuData avuData = buildAVUBasedOnShare(irodsSharedFileOrCollection);

		log.info("setting inheritance and ACL");

		if (objStat.isSomeTypeOfCollection()) {
			setPermissionsForCollection(irodsSharedFileOrCollection, objStat,
					avuData);

		} else {
			setPermissionsForDataObject(irodsSharedFileOrCollection, objStat,
					avuData);
		}

		log.info("share created");
	}

	/**
	 * @param irodsSharedFileOrCollection
	 * @return
	 * @throws JargonException
	 */
	private AvuData buildAVUBasedOnShare(
			final IRODSSharedFileOrCollection irodsSharedFileOrCollection)
			throws JargonException {
		AvuData avuData = AvuData.instance(irodsSharedFileOrCollection
				.getShareName(), getIrodsAccount().getUserName(),
				UserTaggingConstants.SHARE_AVU_UNIT);
		return avuData;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.usertagging.sharing.IRODSSharingService#findShareByAbsolutePath(java.lang.String)
	 */
	@Override
	public IRODSSharedFileOrCollection findShareByAbsolutePath(
			final String irodsAbsolutePath) throws ShareAlreadyExistsException, FileNotFoundException,
			JargonException {

		log.info("findShareByAbsolutePath()");
		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePath");
		}

		log.info("irodsAbsolutePath:()", irodsAbsolutePath);

		/*
		 * Look for the special AVU, if it exists, then build the share with it,
		 * and the recorded ACLs
		 */

		log.info("deciding whether a file or collection...");
		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = getIrodsAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(getIrodsAccount());

		ObjStat objStat = collectionAndDataObjectListAndSearchAO
				.retrieveObjectStatForPath(irodsAbsolutePath);
		return findSharedGivenObjStat(irodsAbsolutePath, objStat);

	}
	
	

	/**
	 * Given an objStat, look for the share AVU marker and create the
	 * <code>IRODSSharedFileOrCollection</code> from the AVU and ACL data.
	 * <p/>
	 * Note that null is returned if no share exists.
	 * 
	 * @param irodsAbsolutePath
	 * @param objStat
	 * @return
	 * @throws JargonException
	 */
	private IRODSSharedFileOrCollection findSharedGivenObjStat(
			final String irodsAbsolutePath, final ObjStat objStat)
			throws JargonException {

		MiscIRODSUtils.evaluateSpecCollSupport(objStat);

		List<AVUQueryElement> avuQueryElements = buildAVUQueryForShared();

		List<MetaDataAndDomainData> queryResults = retrieveAVUsForShare(
				irodsAbsolutePath, objStat, avuQueryElements);

		if (queryResults.isEmpty()) {
			return null;
		}

		/*
		 * I have a shared AVU, so build the response. First I need to gather
		 * the AVUs
		 */

		MetaDataAndDomainData avuValue = queryResults.get(0);
		log.info("found AVU:{}", avuValue);

		log.info("gathering shareUsers...");
		List<UserFilePermission> userFilePermissions = new ArrayList<UserFilePermission>();
		MetadataDomain metadataDomain;

		if (objStat.isSomeTypeOfCollection()) {
			metadataDomain = MetadataDomain.COLLECTION;
			CollectionAO collectionAO = getIrodsAccessObjectFactory()
					.getCollectionAO(getIrodsAccount());
			userFilePermissions = collectionAO
					.listPermissionsForCollection(irodsAbsolutePath);
		} else {
			metadataDomain = MetadataDomain.DATA;
			DataObjectAO dataObjectAO = getIrodsAccessObjectFactory()
					.getDataObjectAO(getIrodsAccount());
			userFilePermissions = dataObjectAO
					.listPermissionsForDataObject(irodsAbsolutePath);
		}

		log.info("got shareUsers...processing");

		List<ShareUser> shareUsers = new ArrayList<ShareUser>(
				userFilePermissions.size());

		for (UserFilePermission userFilePermission : userFilePermissions) {
			shareUsers.add(new ShareUser(userFilePermission.getUserName(),
					userFilePermission.getUserZone(), userFilePermission
							.getFilePermissionEnum()));
		}

		return new IRODSSharedFileOrCollection(metadataDomain,
				irodsAbsolutePath, avuValue.getAvuAttribute(), objStat.getOwnerName(), objStat.getOwnerZone(),
				shareUsers);

	}

	/**
	 * @param irodsAbsolutePath
	 * @param objStat
	 * @param avuQueryElements
	 * @return
	 * @throws JargonException
	 */
	private List<MetaDataAndDomainData> retrieveAVUsForShare(
			final String irodsAbsolutePath, final ObjStat objStat,
			final List<AVUQueryElement> avuQueryElements)
			throws JargonException {

		List<MetaDataAndDomainData> queryResults = new ArrayList<MetaDataAndDomainData>();
		if (objStat.isSomeTypeOfCollection()) {
			log.info("querying metadata as a collection to look for shared");
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
			log.info("querying metadata as a data object to look for shared");
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
		return queryResults;
	}

	/**
	 * Build the query that will look for shared data
	 * 
	 * @return
	 * @throws JargonException
	 */
	private List<AVUQueryElement> buildAVUQueryForShared()
			throws JargonException {
		List<AVUQueryElement> avuQueryElements = new ArrayList<AVUQueryElement>();
		try {
			avuQueryElements.add(AVUQueryElement.instanceForValueQuery(
					AVUQueryPart.UNITS, AVUQueryOperatorEnum.EQUAL,
					UserTaggingConstants.SHARE_AVU_UNIT));
		} catch (JargonQueryException e) {
			log.error("error on metadata query, rethrow as JargonException", e);
			throw new JargonException(e);
		}
		return avuQueryElements;
	}

	/**
	 * @param irodsSharedFileOrCollection
	 * @param objStat
	 * @param avuData
	 * @throws JargonException
	 * @throws DataNotFoundException
	 * @throws DuplicateDataException
	 */
	private void setPermissionsForCollection(
			final IRODSSharedFileOrCollection irodsSharedFileOrCollection,
			final ObjStat objStat, final AvuData avuData)
			throws JargonException, DataNotFoundException,
			DuplicateDataException {
		CollectionAO collectionAO = getIrodsAccessObjectFactory()
				.getCollectionAO(getIrodsAccount());
		log.info("setting metadata for share:{}", avuData);
		collectionAO.addAVUMetadata(
				irodsSharedFileOrCollection.getDomainUniqueName(), avuData);
		log.info("...metadata tag saved, set inheritance...");
		collectionAO.setAccessPermissionInherit(objStat.getOwnerZone(),
				irodsSharedFileOrCollection.getDomainUniqueName(), true);
		log.info("inheritance set, now setting the ACLs desired...");
		for (ShareUser shareUser : irodsSharedFileOrCollection.getShareUsers()) {
			log.info("shareUser:{}", shareUser);
			// FIXMME: need objstat lookup optimization
			// assume recursive is true..
			collectionAO.setAccessPermission(shareUser.getZone(),
					irodsSharedFileOrCollection.getDomainUniqueName(),
					shareUser.getUserName(), true,
					shareUser.getFilePermission());
		}
	}

	/**
	 * @param irodsSharedFileOrCollection
	 * @param objStat
	 * @param avuData
	 * @throws JargonException
	 * @throws DataNotFoundException
	 * @throws DuplicateDataException
	 */
	private void setPermissionsForDataObject(
			final IRODSSharedFileOrCollection irodsSharedFileOrCollection,
			final ObjStat objStat, final AvuData avuData)
			throws JargonException, DataNotFoundException,
			DuplicateDataException {
		DataObjectAO dataObjectAO = getIrodsAccessObjectFactory()
				.getDataObjectAO(getIrodsAccount());
		log.info("setting metadata for share:{}", avuData);
		dataObjectAO.addAVUMetadata(
				irodsSharedFileOrCollection.getDomainUniqueName(), avuData);
		log.info("inheritance set, now setting the ACLs desired...");
		for (ShareUser shareUser : irodsSharedFileOrCollection.getShareUsers()) {
			log.info("shareUser:{}", shareUser);
			// FIXMME: need objstat lookup optimization
			// assume recursive is true..
			dataObjectAO.setAccessPermission(shareUser.getZone(),
					irodsSharedFileOrCollection.getDomainUniqueName(),
					shareUser.getUserName(), shareUser.getFilePermission());
		}
	}
	
	/* (non-Javadoc)
	 * @see org.irods.jargon.usertagging.sharing.IRODSSharingService#listSharedCollectionsOwnedByAUser(java.lang.String, java.lang.String)
	 */
	@Override
	public List<IRODSSharedFileOrCollection> listSharedCollectionsOwnedByAUser(final String userName, final String userZone) throws JargonException {
		log.info("listSharedCollectionsByAUser()");
		
		if (userName == null | userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}
		
		if (userZone == null) {
			throw new IllegalArgumentException("null userZone");
		}
		
		log.info("userName:{}", userName);
		
		/*
		 * Use current zone if one not set
		 */
		
		String myZone;
		if (userZone.isEmpty()) {
			myZone = this.getIrodsAccount().getZone();
		} else {
			myZone = userZone;
		}
		
		log.info("zone used:{}", myZone);
		
		
		/*
		 * Runs the listSharedCollectionsOwnedByUser specific query, which must be loaded on the the iRODS server
		 * arguments are userName and userZone
		 */
		List<String> arguments = new ArrayList<String>();
		arguments.add(userName);
		arguments.add(userZone);
		
		SpecificQuery specificQuery = SpecificQuery.instanceArguments("listSharedCollectionsOwnedByUser", arguments, 0);
		SpecificQueryResultSet specificQueryResultSet = runSpecificQuery(specificQuery);
		
		List<IRODSSharedFileOrCollection> irodsSharedFileOrCollections = new ArrayList<IRODSSharedFileOrCollection>();
		IRODSSharedFileOrCollection irodsSharedFileOrCollection;
		StringBuilder sb;
		
		for (IRODSQueryResultRow row : specificQueryResultSet.getResults()) {
			sb = new StringBuilder();
			sb.append(row.getColumn(1));
			sb.append(row.getColumn(2));
			irodsSharedFileOrCollection = new IRODSSharedFileOrCollection(MetadataDomain.COLLECTION, 
					sb.toString(), row.getColumn(7), row.getColumn(3), 
					row.getColumn(4), new ArrayList<ShareUser>());
			augmentRowWithCountData(specificQueryResultSet,
					irodsSharedFileOrCollection, row);
			irodsSharedFileOrCollections.add(irodsSharedFileOrCollection);	
		}
		
		return irodsSharedFileOrCollections;

	}

	/**
	 * @param specificQueryResultSet
	 * @param irodsSharedFileOrCollection
	 * @param row
	 */
	private void augmentRowWithCountData(
			SpecificQueryResultSet specificQueryResultSet,
			IRODSSharedFileOrCollection irodsSharedFileOrCollection,
			IRODSQueryResultRow row) {
		// add count info
		irodsSharedFileOrCollection.setCount(row.getRecordCount());
		irodsSharedFileOrCollection.setLastResult(row.isLastResult());
		irodsSharedFileOrCollection.setTotalRecords(specificQueryResultSet.getTotalRecords());
	}

	/**
	 * @param specificQuery
	 * @throws JargonException
	 * @throws DataNotFoundException
	 */
	private SpecificQueryResultSet runSpecificQuery(SpecificQuery specificQuery)
			throws JargonException, DataNotFoundException {
		
		checkSpecificQuerySupport();
		try {
			SpecificQueryAO queryAO = this.getIrodsAccessObjectFactory()
					.getSpecificQueryAO(this.getIrodsAccount());
			
			
			return queryAO
					.executeSpecificQueryUsingAlias(specificQuery, this.getIrodsAccessObjectFactory()
							.getJargonProperties().getMaxFilesAndDirsQueryMax());
		} catch (JargonQueryException e) {
			log.error("error in specific query", e);
			throw new JargonException("error in specific query", e);
		}
	}

	/**
	 * @throws JargonException
	 * @throws OperationNotSupportedByIRODSVersionException
	 */
	private void checkSpecificQuerySupport() throws JargonException,
			OperationNotSupportedByIRODSVersionException {
		if (!this.getIrodsAccessObjectFactory().getIRODSServerProperties(getIrodsAccount()).isSupportsSpecificQuery()) {
			log.error("specific query is not supported by this iRODS server:{}", this.getIrodsAccessObjectFactory().getIRODSServerProperties(getIrodsAccount()));
			throw new OperationNotSupportedByIRODSVersionException("specific query not supported");
		}
	}

}
