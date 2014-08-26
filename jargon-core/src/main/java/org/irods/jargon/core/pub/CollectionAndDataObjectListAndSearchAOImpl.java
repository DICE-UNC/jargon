package org.irods.jargon.core.pub;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.SpecificQueryException;
import org.irods.jargon.core.packinstr.DataObjInpForObjStat;
import org.irods.jargon.core.packinstr.Tag;
import org.irods.jargon.core.pub.aohelper.CollectionAOHelper;
import org.irods.jargon.core.pub.domain.DataObject;
import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.core.pub.domain.ObjStat.SpecColType;
import org.irods.jargon.core.pub.domain.UserFilePermission;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileSystemAOHelper;
import org.irods.jargon.core.query.AbstractIRODSQueryResultSet;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType;
import org.irods.jargon.core.query.GenQueryBuilderException;
import org.irods.jargon.core.query.GenQueryField.SelectFieldTypes;
import org.irods.jargon.core.query.IRODSGenQueryBuilder;
import org.irods.jargon.core.query.IRODSGenQueryFromBuilder;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSet;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.PagingAwareCollectionListing;
import org.irods.jargon.core.query.QueryConditionOperators;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.core.query.SpecificQuery;
import org.irods.jargon.core.query.SpecificQueryResultSet;
import org.irods.jargon.core.utils.FederationEnabled;
import org.irods.jargon.core.utils.IRODSDataConversionUtil;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.irods.jargon.core.utils.Overheaded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This access object contains methods that can assist in searching across
 * Collections and Data Objects, and in listing across Collections And Data
 * Objects.
 * <p/>
 * It is very common to create interfaces with search boxes, and with tree views
 * of the iRODS hierarchy. This class is meant to contain such methods. Note
 * that there are specific search and query methods for Data Objects
 * {@link DataObjectAO} and Collections {@link CollectionAO} that are useful for
 * general development.
 * <p/>
 * Note the comments in individual methods for details on behavior of these
 * methods across s federation, and with special collections (e.g. soft links,
 * mounted collections) supported.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class CollectionAndDataObjectListAndSearchAOImpl extends IRODSGenericAO
		implements CollectionAndDataObjectListAndSearchAO {

	private SpecificQueryAO specificQueryAO;
	public static final Logger log = LoggerFactory
			.getLogger(CollectionAndDataObjectListAndSearchAOImpl.class);

	/**
	 * Constructor to be called by the {@link IRODSAccessObjectFactory}
	 * 
	 * @param irodsSession
	 * @param irodsAccount
	 * @throws JargonException
	 */
	protected CollectionAndDataObjectListAndSearchAOImpl(
			final IRODSSession irodsSession, final IRODSAccount irodsAccount)
			throws JargonException {
		super(irodsSession, irodsAccount);
		try {
			specificQueryAO = getIRODSAccessObjectFactory().getSpecificQueryAO(
					getIRODSAccount());
		} catch (SpecificQueryException sqe) {
			log.warn("specific query is not supported on this server");
			specificQueryAO = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO#
	 * getCollectionAndDataObjectListingEntryAtGivenAbsolutePath
	 * (java.lang.String)
	 * 
	 * 
	 * softlink
	 */
	@Override
	public CollectionAndDataObjectListingEntry getCollectionAndDataObjectListingEntryAtGivenAbsolutePath(
			final String absolutePath) throws FileNotFoundException,
			JargonException {

		log.info("getCollectionAndDataObjectListingEntryAtGivenAbsolutePath()");

		if (absolutePath == null || absolutePath.isEmpty()) {
			throw new IllegalArgumentException("absolutePath is null or empty");
		}

		ObjStat objStat = retrieveObjectStatForPath(absolutePath.trim());

		/*
		 * See if jargon supports the given object type
		 */
		MiscIRODSUtils.evaluateSpecCollSupport(objStat);

		IRODSFile entryFile = getIRODSFileFactory().instanceIRODSFile(
				absolutePath);

		CollectionAndDataObjectListingEntry entry = new CollectionAndDataObjectListingEntry();
		entry.setParentPath(entryFile.getParent());

		if (objStat.getObjectType() == ObjectType.DATA_OBJECT
				|| objStat.getObjectType() == ObjectType.LOCAL_FILE) {
			entry.setPathOrName(entryFile.getName());
		} else {
			entry.setPathOrName(absolutePath);
		}

		entry.setCreatedAt(objStat.getCreatedAt());
		entry.setModifiedAt(objStat.getModifiedAt());
		entry.setDataSize(objStat.getObjSize());
		entry.setId(objStat.getDataId());
		entry.setObjectType(objStat.getObjectType());
		entry.setOwnerName(objStat.getOwnerName());
		entry.setOwnerZone(MiscIRODSUtils.getZoneInPath(absolutePath));
		entry.setSpecColType(objStat.getSpecColType());
		entry.setSpecialObjectPath(objStat.getObjectPath());
		log.info("created entry for path as: {}", entry);
		return entry;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO#
	 * listDataObjectsAndCollectionsUnderPathProducingPagingAwareCollectionListing
	 * (java.lang.String)
	 */
	@Override
	public PagingAwareCollectionListing listDataObjectsAndCollectionsUnderPathProducingPagingAwareCollectionListing(
			final String absolutePathToParent) throws FileNotFoundException,
			JargonException {

		log.info("listDataObjectsAndCollectionsUnderPathProducingPagingAwareCollectionListing()");
		if (absolutePathToParent == null || absolutePathToParent.isEmpty()) {
			throw new IllegalArgumentException(
					"absolutePathToParent is null or empty");
		}

		log.info("absolutePath:{}", absolutePathToParent);

		PagingAwareCollectionListing pagingAwareCollectionListing = new PagingAwareCollectionListing();
		pagingAwareCollectionListing.setPageSizeUtilized(getJargonProperties()
				.getMaxFilesAndDirsQueryMax());
		pagingAwareCollectionListing
				.setParentAbsolutePath(absolutePathToParent);
		pagingAwareCollectionListing.setPathComponents(MiscIRODSUtils
				.breakIRODSPathIntoComponents(absolutePathToParent));

		List<CollectionAndDataObjectListingEntry> entries = null;
		ObjStat objStat = null;
		CollectionListingUtils collectionListingUtils = new CollectionListingUtils(
				this);

		try {
			objStat = retrieveObjectStatForPath(absolutePathToParent);
		} catch (FileNotFoundException fnf) {
			log.info("didnt find an objStat for the path, account for cases where there are strict acls and give Jargon a chance to drill down to a place where the user has permissions");
			entries = collectionListingUtils
					.handleNoListingUnderRootOrHomeByLookingForPublicAndHome(absolutePathToParent);
			pagingAwareCollectionListing
					.setCollectionAndDataObjectListingEntries(entries);
			pagingAwareCollectionListing.setCollectionsComplete(true);
			pagingAwareCollectionListing.setCount(entries.size());
			return pagingAwareCollectionListing;
		}

		// I can actually get the objStat and do a real listing...otherwise
		// would have returned

		/*
		 * See if jargon supports the given object type
		 */
		MiscIRODSUtils.evaluateSpecCollSupport(objStat);

		List<CollectionAndDataObjectListingEntry> queriedEntries = collectionListingUtils
				.listCollectionsUnderPath(objStat, 0);

		/*
		 * characterize the collections listing by looking at the returned data
		 */
		if (queriedEntries.isEmpty()) {
			log.info("no child collections");
			pagingAwareCollectionListing.setCollectionsComplete(true);
			pagingAwareCollectionListing.setCount(0);
			pagingAwareCollectionListing.setOffset(0);
		} else {
			log.info("adding child collections");
			pagingAwareCollectionListing.setCollectionsComplete(queriedEntries
					.get(queriedEntries.size() - 1).isLastResult());
			pagingAwareCollectionListing.setCount(queriedEntries.get(
					queriedEntries.size() - 1).getCount());
			pagingAwareCollectionListing.setTotalRecords(queriedEntries.get(0)
					.getTotalRecords());
			pagingAwareCollectionListing
					.getCollectionAndDataObjectListingEntries().addAll(
							queriedEntries);
		}

		queriedEntries = collectionListingUtils.listDataObjectsUnderPath(
				objStat, 0);

		/*
		 * characterize the data objects listing
		 */
		if (queriedEntries.isEmpty()) {
			log.info("no child data objects");
			pagingAwareCollectionListing.setDataObjectsComplete(true);
			pagingAwareCollectionListing.setDataObjectsCount(0);
			pagingAwareCollectionListing.setDataObjectsOffset(0);
		} else {
			log.info("adding child data objects");
			pagingAwareCollectionListing.setDataObjectsComplete(queriedEntries
					.get(queriedEntries.size() - 1).isLastResult());
			pagingAwareCollectionListing.setDataObjectsCount(queriedEntries
					.get(queriedEntries.size() - 1).getCount());
			pagingAwareCollectionListing
					.setDataObjectsTotalRecords(queriedEntries.get(0)
							.getTotalRecords());
			pagingAwareCollectionListing
					.getCollectionAndDataObjectListingEntries().addAll(
							queriedEntries);
		}

		log.info("pagingAwareCollectionListing:{}",
				pagingAwareCollectionListing);
		return pagingAwareCollectionListing;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO#
	 * listDataObjectsAndCollectionsUnderPath(java.lang.String)
	 * 
	 * softlink
	 */
	@Override
	public List<CollectionAndDataObjectListingEntry> listDataObjectsAndCollectionsUnderPath(
			final String absolutePathToParent) throws FileNotFoundException,
			JargonException {

		if (absolutePathToParent == null || absolutePathToParent.isEmpty()) {
			throw new IllegalArgumentException(
					"absolutePathToParent is null or empty");
		}

		ObjStat objStat;
		CollectionListingUtils collectionListingUtils = new CollectionListingUtils(
				this);

		try {
			objStat = retrieveObjectStatForPath(absolutePathToParent);
		} catch (FileNotFoundException fnf) {
			log.info("didn't find an objStat for the path, account for cases where there are strict acls and give Jargon a chance to drill down to a place where the user has permissions");
			return collectionListingUtils
					.handleNoListingUnderRootOrHomeByLookingForPublicAndHome(absolutePathToParent);
		}

		/*
		 * See if jargon supports the given object type
		 */
		MiscIRODSUtils.evaluateSpecCollSupport(objStat);

		List<CollectionAndDataObjectListingEntry> entries = new ArrayList<CollectionAndDataObjectListingEntry>();

		entries.addAll(collectionListingUtils.listCollectionsUnderPath(objStat,
				0));
		entries.addAll(collectionListingUtils.listDataObjectsUnderPath(objStat,
				0));

		return entries;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO#
	 * listDataObjectsAndCollectionsUnderPathWithPermissions(java.lang.String)
	 * 
	 * softlink
	 */
	@Override
	@FederationEnabled
	public List<CollectionAndDataObjectListingEntry> listDataObjectsAndCollectionsUnderPathWithPermissions(
			final String absolutePathToParent) throws FileNotFoundException,
			JargonException {

		if (absolutePathToParent == null) {
			throw new IllegalArgumentException("absolutePathToParent is null");
		}

		if (absolutePathToParent.isEmpty()) {
			throw new IllegalArgumentException("absolutePathToParent is null");
		}

		ObjStat objStat;
		CollectionListingUtils collectionListingUtils = new CollectionListingUtils(
				this);

		try {
			objStat = retrieveObjectStatForPath(absolutePathToParent);
		} catch (FileNotFoundException fnf) {
			log.info("didnt find an objStat for the path, account for cases where there are strict acls and give Jargon a chance to drill down to a place where the user has permissions");
			return collectionListingUtils
					.handleNoListingUnderRootOrHomeByLookingForPublicAndHome(absolutePathToParent);
		}

		/*
		 * See if jargon supports the given object type
		 */
		MiscIRODSUtils.evaluateSpecCollSupport(objStat);

		List<CollectionAndDataObjectListingEntry> entries = listCollectionsUnderPathWithPermissionsCheckingIfSpecQueryUsed(
				absolutePathToParent, 0, objStat);
		entries.addAll(listDataObjectsUnderPathWithPermissionsCheckingIfSpecQueryUsed(
				absolutePathToParent, 0, objStat));
		return entries;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO#
	 * countDataObjectsAndCollectionsUnderPath(java.lang.String)
	 */
	@Override
	@FederationEnabled
	public int countDataObjectsAndCollectionsUnderPath(
			final String absolutePathToParent) throws FileNotFoundException,
			JargonException {

		if (absolutePathToParent == null) {
			throw new IllegalArgumentException("absolutePathToParent is null");
		}

		log.info("countDataObjectsAndCollectionsUnder: {}",
				absolutePathToParent);

		MiscIRODSUtils.checkPathSizeForMax(absolutePathToParent);
		ObjStat objStat = retrieveObjectStatForPath(absolutePathToParent);

		/*
		 * See if jargon supports the given object type
		 */
		MiscIRODSUtils.evaluateSpecCollSupport(objStat);

		String effectiveAbsolutePath = MiscIRODSUtils
				.determineAbsolutePathBasedOnCollTypeInObjectStat(objStat);
		log.info("determined effectiveAbsolutePathToBe:{}",
				effectiveAbsolutePath);

		// I cannot get children if this is not a directory (a file has no
		// children)
		if (!objStat.isSomeTypeOfCollection()) {
			log.error(
					"this is a file, not a directory, and therefore I cannot get a count of the children: {}",
					absolutePathToParent);
			throw new JargonException(
					"attempting to count children under a file at path:"
							+ absolutePathToParent);
		}

		return queryDataObjectCountsUnderPath(effectiveAbsolutePath)
				+ queryCollectionCountsUnderPath(effectiveAbsolutePath);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO#
	 * countDataObjectsUnderPath(java.lang.String)
	 */
	@Override
	public int countDataObjectsUnderPath(final String absolutePathToParent)
			throws FileNotFoundException, JargonException {

		if (absolutePathToParent == null) {
			throw new IllegalArgumentException("absolutePathToParent is null");
		}

		log.info("countDataObjectsAndCollectionsUnder: {}",
				absolutePathToParent);

		MiscIRODSUtils.checkPathSizeForMax(absolutePathToParent);
		ObjStat objStat = retrieveObjectStatForPath(absolutePathToParent);

		/*
		 * See if jargon supports the given object type
		 */
		MiscIRODSUtils.evaluateSpecCollSupport(objStat);

		String effectiveAbsolutePath = MiscIRODSUtils
				.determineAbsolutePathBasedOnCollTypeInObjectStat(objStat);
		log.info("determined effectiveAbsolutePathToBe:{}",
				effectiveAbsolutePath);

		// I cannot get children if this is not a directory (a file has no
		// children)
		if (!objStat.isSomeTypeOfCollection()) {
			log.error(
					"this is a file, not a directory, and therefore I cannot get a count of the children: {}",
					absolutePathToParent);
			throw new JargonException(
					"attempting to count children under a file at path:"
							+ absolutePathToParent);
		}

		return queryDataObjectCountsUnderPath(effectiveAbsolutePath);

	}

	private int queryDataObjectCountsUnderPath(
			final String effectiveAbsolutePath) throws JargonException,
			DataNotFoundException {
		IRODSGenQueryExecutor irodsGenQueryExecutor = new IRODSGenQueryExecutorImpl(
				getIRODSSession(), getIRODSAccount());

		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);
		IRODSQueryResultSet resultSet;

		try {
			builder.addSelectAsAgregateGenQueryValue(
					RodsGenQueryEnum.COL_COLL_NAME, SelectFieldTypes.COUNT)
					.addSelectAsAgregateGenQueryValue(
							RodsGenQueryEnum.COL_DATA_NAME,
							SelectFieldTypes.COUNT)
					.addConditionAsGenQueryField(
							RodsGenQueryEnum.COL_COLL_NAME,
							QueryConditionOperators.EQUAL,
							effectiveAbsolutePath)
					.addConditionAsGenQueryField(
							RodsGenQueryEnum.COL_DATA_REPL_NUM,
							QueryConditionOperators.EQUAL, "0");

			;
			IRODSGenQueryFromBuilder irodsQuery = builder
					.exportIRODSQueryFromBuilder(1);

			resultSet = irodsGenQueryExecutor
					.executeIRODSQueryAndCloseResultInZone(irodsQuery, 0,
							MiscIRODSUtils.getZoneInPath(effectiveAbsolutePath));
		} catch (JargonQueryException e) {
			log.error(CollectionListingUtils.QUERY_EXCEPTION_FOR_QUERY, e);
			throw new JargonException("error in exists query", e);
		} catch (GenQueryBuilderException e) {
			log.error(CollectionListingUtils.QUERY_EXCEPTION_FOR_QUERY, e);
			throw new JargonException("error in exists query", e);
		}

		int fileCtr = 0;

		if (resultSet.getResults().size() > 0) {
			fileCtr = IRODSDataConversionUtil
					.getIntOrZeroFromIRODSValue(resultSet.getFirstResult()
							.getColumn(0));
		}

		return fileCtr;
	}

	private int queryCollectionCountsUnderPath(
			final String effectiveAbsolutePath) throws JargonException,
			DataNotFoundException {
		IRODSGenQueryExecutor irodsGenQueryExecutor = new IRODSGenQueryExecutorImpl(
				getIRODSSession(), getIRODSAccount());

		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);
		IRODSQueryResultSet resultSet;

		builder = new IRODSGenQueryBuilder(true, null);

		try {
			builder.addSelectAsAgregateGenQueryValue(
					RodsGenQueryEnum.COL_COLL_TYPE, SelectFieldTypes.COUNT)
					.addSelectAsAgregateGenQueryValue(
							RodsGenQueryEnum.COL_COLL_NAME,
							SelectFieldTypes.COUNT)
					.addConditionAsGenQueryField(
							RodsGenQueryEnum.COL_COLL_PARENT_NAME,
							QueryConditionOperators.EQUAL,
							effectiveAbsolutePath);

			IRODSGenQueryFromBuilder irodsQuery = builder
					.exportIRODSQueryFromBuilder(1);
			resultSet = irodsGenQueryExecutor
					.executeIRODSQueryAndCloseResultInZone(irodsQuery, 0,
							MiscIRODSUtils.getZoneInPath(effectiveAbsolutePath));
		} catch (JargonQueryException e) {
			log.error(CollectionListingUtils.QUERY_EXCEPTION_FOR_QUERY, e);
			throw new JargonException("error in exists query", e);
		} catch (GenQueryBuilderException e) {
			log.error(CollectionListingUtils.QUERY_EXCEPTION_FOR_QUERY, e);
			throw new JargonException("error in exists query", e);
		}

		int collCtr = 0;
		if (resultSet.getResults().size() > 0) {
			collCtr = IRODSDataConversionUtil
					.getIntOrZeroFromIRODSValue(resultSet.getFirstResult()
							.getColumn(0));
		}

		return collCtr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO#
	 * countCollectionsUnderPath(java.lang.String)
	 */
	@Override
	public int countCollectionsUnderPath(final String absolutePathToParent)
			throws FileNotFoundException, JargonException {

		if (absolutePathToParent == null) {
			throw new IllegalArgumentException("absolutePathToParent is null");
		}

		log.info("countDataObjectsAndCollectionsUnder: {}",
				absolutePathToParent);

		MiscIRODSUtils.checkPathSizeForMax(absolutePathToParent);
		ObjStat objStat = retrieveObjectStatForPath(absolutePathToParent);

		/*
		 * See if jargon supports the given object type
		 */
		MiscIRODSUtils.evaluateSpecCollSupport(objStat);

		String effectiveAbsolutePath = MiscIRODSUtils
				.determineAbsolutePathBasedOnCollTypeInObjectStat(objStat);
		log.info("determined effectiveAbsolutePathToBe:{}",
				effectiveAbsolutePath);

		// I cannot get children if this is not a directory (a file has no
		// children)
		if (!objStat.isSomeTypeOfCollection()) {
			log.error(
					"this is a file, not a directory, and therefore I cannot get a count of the children: {}",
					absolutePathToParent);
			throw new JargonException(
					"attempting to count children under a file at path:"
							+ absolutePathToParent);
		}

		IRODSGenQueryExecutor irodsGenQueryExecutor = new IRODSGenQueryExecutorImpl(
				getIRODSSession(), getIRODSAccount());

		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);
		IRODSQueryResultSet resultSet;

		try {
			builder.addSelectAsAgregateGenQueryValue(
					RodsGenQueryEnum.COL_COLL_TYPE, SelectFieldTypes.COUNT)
					.addSelectAsAgregateGenQueryValue(
							RodsGenQueryEnum.COL_COLL_NAME,
							SelectFieldTypes.COUNT)
					.addConditionAsGenQueryField(
							RodsGenQueryEnum.COL_COLL_PARENT_NAME,
							QueryConditionOperators.EQUAL,
							effectiveAbsolutePath);

			IRODSGenQueryFromBuilder irodsQuery = builder
					.exportIRODSQueryFromBuilder(1);
			resultSet = irodsGenQueryExecutor
					.executeIRODSQueryAndCloseResultInZone(irodsQuery, 0,
							MiscIRODSUtils.getZoneInPath(effectiveAbsolutePath));
		} catch (JargonQueryException e) {
			log.error(CollectionListingUtils.QUERY_EXCEPTION_FOR_QUERY, e);
			throw new JargonException("error in exists query", e);
		} catch (GenQueryBuilderException e) {
			log.error(CollectionListingUtils.QUERY_EXCEPTION_FOR_QUERY, e);
			throw new JargonException("error in exists query", e);
		}

		int collCtr = 0;
		if (resultSet.getResults().size() > 0) {
			collCtr = IRODSDataConversionUtil
					.getIntOrZeroFromIRODSValue(resultSet.getFirstResult()
							.getColumn(0));
		}

		return collCtr;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO#
	 * searchCollectionsBasedOnName(java.lang.String)
	 */
	@Override
	public List<CollectionAndDataObjectListingEntry> searchCollectionsBasedOnName(
			final String searchTerm) throws JargonException {
		if (searchTerm == null) {
			throw new IllegalArgumentException("null searchTerm");
		}
		return searchCollectionsBasedOnName(searchTerm.trim(), 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO#
	 * searchCollectionsBasedOnName(java.lang.String, int)
	 */
	@Override
	public List<CollectionAndDataObjectListingEntry> searchCollectionsBasedOnName(
			final String searchTerm, final int partialStartIndex)
			throws JargonException {

		if (searchTerm == null || searchTerm.isEmpty()) {
			throw new IllegalArgumentException("null or empty search term");
		}

		if (partialStartIndex < 0) {
			throw new IllegalArgumentException("partialStartIndex is < 0");
		}

		log.info("searchCollectionsBasedOnName:{}", searchTerm);

		IRODSGenQueryExecutor irodsGenQueryExecutor = new IRODSGenQueryExecutorImpl(
				getIRODSSession(), getIRODSAccount());

		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);
		IRODSQueryResultSet resultSet;

		try {
			CollectionAOHelper
					.buildSelectsNeededForCollectionsInCollectionsAndDataObjectsListingEntry(builder);
			builder.addConditionAsGenQueryField(RodsGenQueryEnum.COL_COLL_NAME,
					QueryConditionOperators.LIKE, "%" + searchTerm);
			IRODSGenQueryFromBuilder irodsQuery = builder
					.exportIRODSQueryFromBuilder(getJargonProperties()
							.getMaxFilesAndDirsQueryMax());
			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(
					irodsQuery, partialStartIndex);
		} catch (JargonQueryException e) {
			log.error(CollectionListingUtils.QUERY_EXCEPTION_FOR_QUERY, e);
			throw new JargonException("error in exists query", e);
		} catch (GenQueryBuilderException e) {
			log.error(CollectionListingUtils.QUERY_EXCEPTION_FOR_QUERY, e);
			throw new JargonException("error in exists query", e);
		}

		List<CollectionAndDataObjectListingEntry> entries = new ArrayList<CollectionAndDataObjectListingEntry>();
		for (IRODSQueryResultRow row : resultSet.getResults()) {
			entries.add(CollectionAOHelper
					.buildCollectionListEntryFromResultSetRowForCollectionQuery(
							row, resultSet.getTotalRecords()));
		}

		return entries;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO#
	 * listCollectionsUnderPath(java.lang.String, int)
	 * 
	 * softlink
	 */
	@Override
	@Overheaded
	// Bug [#1606] inconsistent objstat semantics for mounted collections
	public List<CollectionAndDataObjectListingEntry> listCollectionsUnderPath(
			final String absolutePathToParent, final int partialStartIndex)
			throws FileNotFoundException, JargonException {

		log.info("listCollectionsUnderPath()");

		if (absolutePathToParent == null) {
			throw new JargonException("absolutePathToParent is null");
		}

		String path;

		if (absolutePathToParent.isEmpty()) {
			path = "/";
		} else {
			path = absolutePathToParent;
		}

		ObjStat objStat;
		CollectionListingUtils collectionListingUtils = new CollectionListingUtils(
				this);

		try {
			objStat = retrieveObjectStatForPath(path);
		} catch (FileNotFoundException fnf) {
			log.info("didnt find an objStat for the path, account for cases where there are strict acls and give Jargon a chance to drill down to a place where the user has permissions");
			return collectionListingUtils
					.handleNoListingUnderRootOrHomeByLookingForPublicAndHome(path);
		}

		/*
		 * See if jargon supports the given object type
		 */
		MiscIRODSUtils.evaluateSpecCollSupport(objStat);
		return collectionListingUtils.listCollectionsUnderPath(objStat,
				partialStartIndex);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO#
	 * listCollectionsUnderPathWithPermissions(java.lang.String, int)
	 */
	@Override
	public List<CollectionAndDataObjectListingEntry> listCollectionsUnderPathWithPermissions(
			final String absolutePathToParent, final int partialStartIndex)
			throws FileNotFoundException, JargonException {

		if (absolutePathToParent == null) {
			throw new IllegalArgumentException("absolutePathToParent is null");
		}

		ObjStat objStat = retrieveObjectStatForPath(absolutePathToParent);

		if (objStat == null) {
			log.error("not objStat found for collection:{}",
					absolutePathToParent);
			throw new FileNotFoundException("no ObjStat found for collection");
		}

		/*
		 * See if jargon supports the given object type
		 */
		MiscIRODSUtils.evaluateSpecCollSupport(objStat);

		return listCollectionsUnderPathWithPermissionsCheckingIfSpecQueryUsed(
				absolutePathToParent, partialStartIndex, objStat);

	}

	private List<CollectionAndDataObjectListingEntry> listCollectionsUnderPathWithPermissionsViaSpecQuery(
			final ObjStat objStat, final int offset) throws JargonException,
			JargonQueryException {

		log.info("listCollectionsUnderPathWithPermissionsViaSpecQuery()");
		String effectiveAbsolutePath = objStat
				.determineAbsolutePathBasedOnCollTypeInObjectStat();

		List<String> arguments = new ArrayList<String>(3);
		arguments.add(effectiveAbsolutePath);
		arguments.add(String.valueOf(getJargonProperties()
				.getMaxFilesAndDirsQueryMax()));
		arguments.add(String.valueOf(offset));

		SpecificQuery specificQuery = SpecificQuery.instanceArguments(
				SHOW_COLL_ACLS, arguments, 0,
				MiscIRODSUtils.getZoneInPath(effectiveAbsolutePath));

		SpecificQueryResultSet specificQueryResultSet = specificQueryAO
				.executeSpecificQueryUsingAlias(specificQuery,
						getJargonProperties().getMaxFilesAndDirsQueryMax(),
						offset);
		log.info("got result set:{}", specificQueryResultSet);

		return buildCollectionListingWithAccessInfoFromResultSet(
				specificQueryResultSet, objStat);

	}

	private List<CollectionAndDataObjectListingEntry> listCollectionsUnderPathWithPermissionsCheckingIfSpecQueryUsed(
			final String absolutePathToParent, final int partialStartIndex,
			final ObjStat objStat) throws FileNotFoundException,
			JargonException {

		log.info("listCollectionsUnderPathWithPermissionsCheckingIfSpecQueryUsed()");

		if (absolutePathToParent == null) {
			throw new IllegalArgumentException("absolutePathToParent is null");
		}

		if (objStat == null) {
			throw new IllegalArgumentException("null objStat");
		}

		log.info("checking to see if I can list via specific query");

		if (getJargonProperties()
				.isUsingSpecificQueryForCollectionListingsWithPermissions()) {

			log.info("we are using spec query in jargon.properties...");

			if (specificQueryAO == null) {
				log.info("...we are bypassing spec query...");
			} else if (specificQueryAO.isSpecificQueryToBeBypassed()) {
				log.info("...we are bypassing spec query...");
			} else {
				log.info("attemting to list via specQuery...");
				try {
					return listCollectionsUnderPathWithPermissionsViaSpecQuery(
							objStat, partialStartIndex);
				} catch (JargonException je) {
					log.error("error executing spec query will do the genQuery fallback");
					// TODO: signal to spec query ao to bypass? do this in
					// specQueryAO?

					// fall thru to genquery
				} catch (JargonQueryException e) {
					log.error("query exception error executing spec query will do the genQuery fallback");
				}
			}

		}

		return listCollectionsUnderPathWithPermissionsUsingGenQuery(
				absolutePathToParent, partialStartIndex, objStat);

	}

	/**
	 * List collections under a path, given that the objStat is known.
	 * <p/>
	 * Handles soft links
	 * 
	 * @param absolutePathToParent
	 * @param partialStartIndex
	 * @param objStat
	 * @return
	 * @throws FileNotFoundException
	 * @throws JargonException
	 */
	private List<CollectionAndDataObjectListingEntry> listCollectionsUnderPathWithPermissionsUsingGenQuery(
			final String absolutePathToParent, final int partialStartIndex,
			final ObjStat objStat) throws FileNotFoundException,
			JargonException {

		if (absolutePathToParent == null) {
			throw new IllegalArgumentException("absolutePathToParent is null");
		}

		if (objStat == null) {
			throw new IllegalArgumentException("null objStat");
		}

		/**
		 * This may be a soft link, in which case the canonical path is used for
		 * the query
		 */
		String effectiveAbsolutePath = MiscIRODSUtils
				.determineAbsolutePathBasedOnCollTypeInObjectStat(objStat);
		log.info("determined effectiveAbsolutePathToBe:{}",
				effectiveAbsolutePath);

		log.info("listCollectionsUnderPathWithPermissionsForUser for: {}",
				effectiveAbsolutePath);

		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);
		CollectionListingUtils collectionListingUtils = new CollectionListingUtils(
				this);
		try {
			IRODSFileSystemAOHelper.buildQueryListAllDirsWithUserAccessInfo(
					effectiveAbsolutePath, builder);
		} catch (GenQueryBuilderException e) {
			log.error("query builder exception", e);
			throw new JargonException("error building query", e);
		}

		IRODSQueryResultSet resultSet = collectionListingUtils
				.queryForPathAndReturnResultSet(effectiveAbsolutePath, builder,
						partialStartIndex, objStat);

		return buildCollectionListingWithAccessInfoFromResultSet(resultSet,
				objStat);

	}

	private List<CollectionAndDataObjectListingEntry> buildDataObjectListingWithAccessInfoFromResultSet(
			final AbstractIRODSQueryResultSet resultSet, final ObjStat objStat)
			throws JargonException {

		String effectiveAbsolutePath = objStat
				.determineAbsolutePathBasedOnCollTypeInObjectStat();
		CollectionListingUtils collectionListingUtils = new CollectionListingUtils(
				this);

		List<CollectionAndDataObjectListingEntry> files = new ArrayList<CollectionAndDataObjectListingEntry>();

		/*
		 * the query that gives the necessary data will cause duplication when
		 * there are replicas, but the data is necessary to get, so discard
		 * duplicates.
		 */

		String currentPath = null;
		String lastPath = "";
		String currentReplNumber = null;
		String lastReplNumber = "";
		CollectionAndDataObjectListingEntry entry = null;
		int lastCount = 0;
		boolean lastRecord = false;
		List<UserFilePermission> userFilePermissions = new ArrayList<UserFilePermission>();

		for (IRODSQueryResultRow row : resultSet.getResults()) {

			StringBuilder sb = new StringBuilder();
			sb.append(row.getColumn(0));
			sb.append('/');
			sb.append(row.getColumn(1));
			currentPath = sb.toString();

			currentReplNumber = row.getColumn(6);
			lastCount = row.getRecordCount();
			lastRecord = row.isLastResult();

			// first look for break in path

			if (currentPath.equals(lastPath)) {

				// look for break in repl number

				if (currentReplNumber.equals(lastReplNumber)) {
					// accumulate a permissions entry
					CollectionAOHelper.buildUserFilePermissionForDataObject(
							userFilePermissions, row, effectiveAbsolutePath,
							getIRODSAccount().getZone());
				} else {
					// ignore, is a replica
				}

				continue;
			}

			// a break has occurred on path

			if (entry != null) {
				// put out previous entry
				entry.setUserFilePermission(userFilePermissions);
				// adjust the 'last count' so that it accurately reflects the
				// actual query result row that caused the break, used in
				// requery to not reread the same data
				entry.setCount(lastCount - 1);
				collectionListingUtils
						.augmentCollectionEntryForSpecialCollections(objStat,
								effectiveAbsolutePath, entry);
				files.add(entry);
			}

			// clear and reinitialize for new entry set
			entry = CollectionAOHelper
					.buildCollectionListEntryFromResultSetRowForDataObjectQuery(
							row, resultSet.getTotalRecords());
			lastPath = currentPath;
			lastReplNumber = currentReplNumber;
			userFilePermissions = new ArrayList<UserFilePermission>();
			CollectionAOHelper.buildUserFilePermissionForDataObject(
					userFilePermissions, row, effectiveAbsolutePath,
					getIRODSAccount().getZone());

		}

		/*
		 * process the last entry, if needed. If more data is coming, then skip
		 * the last entry. This is so the first entry of the next data object
		 * will include this data.
		 */

		if (entry != null) {
			if (lastRecord) {
				// put out previous entry
				entry.setUserFilePermission(userFilePermissions);
				// adjust the 'last count' so that it accurately reflects the
				// actual query result row that caused the break, used in
				// requery to not reread the same data
				entry.setCount(lastCount);
				entry.setLastResult(lastRecord);
				collectionListingUtils
						.augmentCollectionEntryForSpecialCollections(objStat,
								effectiveAbsolutePath, entry);
				files.add(entry);
			} else {
				log.debug("skipping last entry as it may carry over to the next query page");
			}
		}

		return files;

	}

	private List<CollectionAndDataObjectListingEntry> buildCollectionListingWithAccessInfoFromResultSet(
			final AbstractIRODSQueryResultSet resultSet, final ObjStat objStat)
			throws JargonException {

		CollectionListingUtils collectionListingUtils = new CollectionListingUtils(
				this);

		String effectiveAbsolutePath = objStat
				.determineAbsolutePathBasedOnCollTypeInObjectStat();

		List<CollectionAndDataObjectListingEntry> subdirs = new ArrayList<CollectionAndDataObjectListingEntry>(
				resultSet.getResults().size());

		CollectionAndDataObjectListingEntry collectionAndDataObjectListingEntry = null;
		List<UserFilePermission> userFilePermissions = new ArrayList<UserFilePermission>();
		String lastPath = "";
		boolean isAtEndOfQueryResults = false;

		for (IRODSQueryResultRow row : resultSet.getResults()) {

			isAtEndOfQueryResults = row.isLastResult();
			// compare to the previous path and see if I break, in which case
			// add the last collection entry to the result
			String thisPath = row.getColumn(1);

			if (thisPath.equals(lastPath)) {
				// parse out the file permission and continue,
				CollectionAOHelper.buildUserFilePermissionForCollection(
						userFilePermissions, row, effectiveAbsolutePath);
				continue;
			} else {
				// is a break on path, put out the info for the last path if
				// it's there

				if (collectionAndDataObjectListingEntry != null) {
					collectionAndDataObjectListingEntry
							.setUserFilePermission(userFilePermissions);
					collectionListingUtils
							.augmentCollectionEntryForSpecialCollections(
									objStat, effectiveAbsolutePath,
									collectionAndDataObjectListingEntry);
					collectionAndDataObjectListingEntry.setCount(row
							.getRecordCount() - 1);
					subdirs.add(collectionAndDataObjectListingEntry);
				}

				// on break in path, initialize the data for a new entry
				collectionAndDataObjectListingEntry = CollectionAOHelper
						.buildCollectionListEntryFromResultSetRowForCollectionQuery(
								row, resultSet.getTotalRecords());
				lastPath = collectionAndDataObjectListingEntry.getPathOrName();
				userFilePermissions = new ArrayList<UserFilePermission>();
				CollectionAOHelper.buildUserFilePermissionForCollection(
						userFilePermissions, row, effectiveAbsolutePath);
			}
		}

		/*
		 * Put out the last entry, which I had been caching. I want to avoid
		 * breaking an entry across requests, so if the last entry in the
		 * results is not the last entry returned from the query, ignore it. On
		 * the next read the entire permissions for the file in question should
		 * be read.
		 */

		if (collectionAndDataObjectListingEntry != null) {
			if (isAtEndOfQueryResults) {
				log.debug("adding last entry");
				collectionAndDataObjectListingEntry
						.setUserFilePermission(userFilePermissions);
				collectionAndDataObjectListingEntry.setLastResult(true);
				collectionListingUtils
						.augmentCollectionEntryForSpecialCollections(objStat,
								effectiveAbsolutePath,
								collectionAndDataObjectListingEntry);
				subdirs.add(collectionAndDataObjectListingEntry);
			} else {
				log.debug("ignoring last entry, as it might carry over to the next page of results");
			}
		}

		return subdirs;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO#
	 * listDataObjectsUnderPath(java.lang.String, int)
	 */
	@Override
	@FederationEnabled
	public List<CollectionAndDataObjectListingEntry> listDataObjectsUnderPath(
			final String absolutePathToParent, final int partialStartIndex)
			throws JargonException {

		if (absolutePathToParent == null) {
			throw new JargonException("absolutePathToParent is null");
		}

		ObjStat objStat = retrieveObjectStatForPath(absolutePathToParent);
		CollectionListingUtils collectionListingUtils = new CollectionListingUtils(
				this);

		if (objStat == null) {
			log.error("unable to find objStat for collection path:{}",
					absolutePathToParent);
			throw new FileNotFoundException(
					"unable to find objStat for collection");
		}

		return collectionListingUtils.listDataObjectsUnderPath(objStat,
				partialStartIndex);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO#
	 * listDataObjectsUnderPathWithPermissions(java.lang.String, int)
	 */
	@Override
	@FederationEnabled
	public List<CollectionAndDataObjectListingEntry> listDataObjectsUnderPathWithPermissions(
			final String absolutePathToParent, final int partialStartIndex)
			throws FileNotFoundException, JargonException {

		if (absolutePathToParent == null) {
			throw new JargonException("absolutePathToParent is null");
		}

		log.info("listDataObjectsUnderPathWithPermissions for: {}",
				absolutePathToParent);

		ObjStat objStat = retrieveObjectStatForPath(absolutePathToParent);

		if (objStat == null) {
			log.error("unable to find objStat for collection path:{}",
					absolutePathToParent);
			throw new FileNotFoundException(
					"unable to find objStat for collection");
		}

		log.info("doing listing using genquery...");
		return listDataObjectsUnderPathWithPermissionsCheckingIfSpecQueryUsed(
				absolutePathToParent, partialStartIndex, objStat);
	}

	private List<CollectionAndDataObjectListingEntry> listDataObjectsUnderPathWithPermissionsCheckingIfSpecQueryUsed(
			final String absolutePathToParent, final int partialStartIndex,
			final ObjStat objStat) throws FileNotFoundException,
			JargonException {

		if (absolutePathToParent == null) {
			throw new JargonException("absolutePathToParent is null");
		}

		if (objStat == null) {
			throw new IllegalArgumentException("null objStat");
		}

		/**
		 * This may be a soft link, in which case the canonical path is used for
		 * the query
		 */
		String effectiveAbsolutePath = objStat
				.determineAbsolutePathBasedOnCollTypeInObjectStat();
		log.info("determined effectiveAbsolutePathToBe:{}",
				effectiveAbsolutePath);

		log.info("checking to see if I can list via specific query");

		if (getJargonProperties()
				.isUsingSpecificQueryForCollectionListingsWithPermissions()) {

			log.info("we are using spec query in jargon.properties...");

			if (specificQueryAO == null) {
				log.info("...we are bypassing spec query...");
			} else if (specificQueryAO.isSpecificQueryToBeBypassed()) {
				log.info("...we are bypassing spec query...");
			} else {
				log.info("attemting to list via specQuery...");
				try {
					return listDataObjectsUnderPathWithPermissionsViaSpecQuery(
							objStat, partialStartIndex);
				} catch (JargonException je) {
					log.error("error executing spec query will do the genQuery fallback");
					// TODO: signal to spec query ao to bypass? do this in
					// specQueryAO?

					// fall thru to genquery
				} catch (JargonQueryException e) {
					log.error("query exception error executing spec query will do the genQuery fallback");
				}
			}
		}

		log.info("not using specific query, fall back to genQuery");
		return listDataObjectsUnderPathWithPermissionsUsingGenQuery(
				absolutePathToParent, partialStartIndex, objStat);
	}

	/**
	 * Given the objStat, list the data objects under the path and the
	 * associated file permissions
	 * 
	 * @param absolutePathToParent
	 *            <code>String</code> with the original absolute path as
	 *            requested. This may not be the canonical path if this is a
	 *            special collection (e.g. soft links)
	 * @param partialStartIndex
	 * @param objStat
	 *            {@link ObjStat} with the information (including special
	 *            collection information) used to adjust the entry
	 * @return
	 * @throws FileNotFoundException
	 * @throws JargonException
	 */
	private List<CollectionAndDataObjectListingEntry> listDataObjectsUnderPathWithPermissionsUsingGenQuery(
			final String absolutePathToParent, final int partialStartIndex,
			final ObjStat objStat) throws FileNotFoundException,
			JargonException {

		/**
		 * This may be a soft link, in which case the canonical path is used for
		 * the query
		 */
		String effectiveAbsolutePath = objStat
				.determineAbsolutePathBasedOnCollTypeInObjectStat();
		log.info("determined effectiveAbsolutePathToBe:{}",
				effectiveAbsolutePath);

		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);
		CollectionListingUtils collectionListingUtils = new CollectionListingUtils(
				this);

		IRODSFileSystemAOHelper.buildQueryListAllDataObjectsWithUserAccessInfo(
				effectiveAbsolutePath, builder);
		IRODSQueryResultSet resultSet = collectionListingUtils
				.queryForPathAndReturnResultSet(effectiveAbsolutePath, builder,
						partialStartIndex, objStat);
		log.debug("got result set:{}}, resultSet");

		return buildDataObjectListingWithAccessInfoFromResultSet(resultSet,
				objStat);

	}

	private List<CollectionAndDataObjectListingEntry> listDataObjectsUnderPathWithPermissionsViaSpecQuery(
			final ObjStat objStat, final int offset)
			throws JargonQueryException, JargonException {

		log.info("listDataObjectsUnderPathWithPermissionsViaSpecQuery()");

		/**
		 * This may be a soft link, in which case the canonical path is used for
		 * the query
		 */
		String effectiveAbsolutePath = objStat
				.determineAbsolutePathBasedOnCollTypeInObjectStat();
		log.info("determined effectiveAbsolutePathToBe:{}",
				effectiveAbsolutePath);

		List<String> arguments = new ArrayList<String>(3);
		arguments.add(effectiveAbsolutePath);
		arguments.add(String.valueOf(getJargonProperties()
				.getMaxFilesAndDirsQueryMax()));
		arguments.add(String.valueOf(offset));

		SpecificQuery specificQuery = SpecificQuery.instanceArguments(
				SHOW_DATA_OBJ_ACLS, arguments, 0,
				MiscIRODSUtils.getZoneInPath(effectiveAbsolutePath));

		SpecificQueryResultSet specificQueryResultSet = specificQueryAO
				.executeSpecificQueryUsingAlias(specificQuery,
						getJargonProperties().getMaxFilesAndDirsQueryMax(),
						offset);
		log.info("got result set:{}", specificQueryResultSet);

		return buildDataObjectListingWithAccessInfoFromResultSet(
				specificQueryResultSet, objStat);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO#
	 * searchDataObjectsBasedOnName(java.lang.String)
	 */
	@Override
	public List<CollectionAndDataObjectListingEntry> searchDataObjectsBasedOnName(
			final String searchTerm) throws JargonException {
		return searchDataObjectsBasedOnName(searchTerm, 0);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO#
	 * searchDataObjectsBasedOnName(java.lang.String, int)
	 */
	@Override
	public List<CollectionAndDataObjectListingEntry> searchDataObjectsBasedOnName(
			final String searchTerm, final int partialStartIndex)
			throws JargonException {

		if (searchTerm == null || searchTerm.isEmpty()) {
			throw new IllegalArgumentException("null or empty search term");
		}

		if (partialStartIndex < 0) {
			throw new IllegalArgumentException("partialStartIndex is < 0");
		}

		log.info("searchDataObjectsBasedOnName:{}", searchTerm);

		IRODSGenQueryExecutor irodsGenQueryExecutor = new IRODSGenQueryExecutorImpl(
				getIRODSSession(), getIRODSAccount());

		IRODSQueryResultSet resultSet;

		try {
			IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);
			builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_NAME)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_DATA_NAME)
					.addSelectAsGenQueryValue(
							RodsGenQueryEnum.COL_D_CREATE_TIME)
					.addSelectAsGenQueryValue(
							RodsGenQueryEnum.COL_D_MODIFY_TIME)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_DATA_SIZE)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_D_OWNER_NAME)
					.addConditionAsGenQueryField(
							RodsGenQueryEnum.COL_DATA_NAME,
							QueryConditionOperators.LIKE,
							"%" + searchTerm + "%");
			IRODSGenQueryFromBuilder irodsQuery = builder
					.exportIRODSQueryFromBuilder(getJargonProperties()
							.getMaxFilesAndDirsQueryMax());
			resultSet = irodsGenQueryExecutor.executeIRODSQueryWithPaging(
					irodsQuery, partialStartIndex);
		} catch (JargonQueryException e) {
			log.error(CollectionListingUtils.QUERY_EXCEPTION_FOR_QUERY, e);
			throw new JargonException("error in exists query", e);
		} catch (GenQueryBuilderException e) {
			log.error(CollectionListingUtils.QUERY_EXCEPTION_FOR_QUERY, e);
			throw new JargonException("error in exists query", e);
		}

		List<CollectionAndDataObjectListingEntry> entries = new ArrayList<CollectionAndDataObjectListingEntry>();
		for (IRODSQueryResultRow row : resultSet.getResults()) {
			CollectionAndDataObjectListingEntry entry = new CollectionAndDataObjectListingEntry();
			entry.setParentPath(row.getColumn(0));
			entry.setObjectType(ObjectType.DATA_OBJECT);
			entry.setPathOrName(row.getColumn(1));
			entry.setCreatedAt(IRODSDataConversionUtil
					.getDateFromIRODSValue(row.getColumn(2)));
			entry.setModifiedAt(IRODSDataConversionUtil
					.getDateFromIRODSValue(row.getColumn(3)));
			entry.setDataSize(IRODSDataConversionUtil
					.getLongOrZeroFromIRODSValue(row.getColumn(4)));
			entry.setOwnerName(row.getColumn(5));
			entry.setCount(row.getRecordCount());
			entry.setLastResult(row.isLastResult());

			log.info("listing entry built {}", entry.toString());
			entries.add(entry);
		}

		return entries;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO#
	 * searchCollectionsAndDataObjectsBasedOnName(java.lang.String)
	 */
	@Override
	public List<CollectionAndDataObjectListingEntry> searchCollectionsAndDataObjectsBasedOnName(
			final String searchTerm) throws JargonException {

		log.info(
				"searchCollectionsAndDataObjectsBasedOnName for search term:{}, starting with collections",
				searchTerm);
		List<CollectionAndDataObjectListingEntry> entries = searchCollectionsBasedOnName(searchTerm);
		log.info("adding data objects to search results");
		List<CollectionAndDataObjectListingEntry> dataObjectEntries = searchDataObjectsBasedOnName(searchTerm);

		for (CollectionAndDataObjectListingEntry entry : dataObjectEntries) {
			entries.add(entry);
		}

		return entries;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO#
	 * getFullObjectForType(java.lang.String)
	 */
	@Override
	@FederationEnabled
	public Object getFullObjectForType(final String objectAbsolutePath)
			throws FileNotFoundException, JargonException {

		if (objectAbsolutePath == null || objectAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty object absolute path");
		}

		log.info("getFullObjectForType for path:{}", objectAbsolutePath);
		ObjStat objStat = retrieveObjectStatForPath(objectAbsolutePath);

		String effectiveAbsolutePath = MiscIRODSUtils
				.determineAbsolutePathBasedOnCollTypeInObjectStat(objStat);
		log.info("determined effectiveAbsolutePathToBe:{}",
				effectiveAbsolutePath);

		/*
		 * See if jargon supports the given object type
		 */
		MiscIRODSUtils.evaluateSpecCollSupport(objStat);

		// see if file or coll
		Object returnObject = null;

		/*
		 * objStat: absolutePath:/test1/home/test1/jargon-scratch/
		 * CollectionAndDataObjectListAndSearchAOImplForMSSOTest
		 * /testGetFullObjectForTypeInTestWorkflow
		 * /testGetFullObjectForTypeInTestWorkflowMounted/eCWkflow.run
		 * dataId:10043 specColType:STRUCT_FILE_COLL objectType:DATA_OBJECT
		 * collectionPath:/test1/home/test1/jargon-scratch/
		 * CollectionAndDataObjectListAndSearchAOImplForMSSOTest
		 * /testGetFullObjectForTypeInTestWorkflow
		 * /testGetFullObjectForTypeInTestWorkflowMounted objectPath: checksum:
		 * ownerName:test1 ownerZone:test1 objSize:33554412
		 * cacheDir:/opt/iRODS/iRODS3.2/Vault1/home/test1/jargon-scratch/
		 * CollectionAndDataObjectListAndSearchAOImplForMSSOTest
		 * /testGetFullObjectForTypeInTestWorkflow/eCWkflow.mss.cacheDir0
		 * cacheDirty:false createdAt:replNumber:0Mon Feb 11 17:32:17 EST 2013
		 * modifiedAt:Mon Feb 11 17:32:17 EST 2013
		 */

		if (objStat.isSomeTypeOfCollection()) {
			CollectionAO collectionAO = new CollectionAOImpl(getIRODSSession(),
					getIRODSAccount());
			returnObject = collectionAO.findGivenObjStat(objStat);
		} else {

			if (objStat.getSpecColType() == SpecColType.STRUCT_FILE_COLL) {
				returnObject = buildDataObjectFromObjStatIfStructuredCollection(objStat);
			} else {

				returnObject = buildDataObjectFromICAT(objStat);
			}
		}

		// get appropriate domain object and return
		return returnObject;
	}

	private Object buildDataObjectFromICAT(final ObjStat objStat)
			throws JargonException, DataNotFoundException {
		Object returnObject;
		DataObjectAO dataObjectAO = new DataObjectAOImpl(getIRODSSession(),
				getIRODSAccount());
		returnObject = dataObjectAO.findGivenObjStat(objStat);
		return returnObject;
	}

	private Object buildDataObjectFromObjStatIfStructuredCollection(
			final ObjStat objStat) {
		Object returnObject;
		DataObject dataObject = new DataObject();
		dataObject.setChecksum(objStat.getChecksum());
		dataObject.setCollectionName(objStat.getCollectionPath());
		dataObject.setCreatedAt(objStat.getCreatedAt());
		dataObject.setDataName(MiscIRODSUtils
				.getLastPathComponentForGiveAbsolutePath(objStat
						.getAbsolutePath()));
		dataObject.setDataOwnerName(objStat.getOwnerName());
		dataObject.setDataOwnerZone(objStat.getOwnerZone());
		dataObject.setDataPath(objStat.getObjectPath());
		dataObject.setDataReplicationNumber(objStat.getReplNumber());
		dataObject.setDataSize(objStat.getObjSize());
		dataObject.setSpecColType(objStat.getSpecColType());
		dataObject.setUpdatedAt(objStat.getModifiedAt());
		returnObject = dataObject;
		return returnObject;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO#
	 * retrieveObjectStatForPathAndDataObjectName(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public ObjStat retrieveObjectStatForPathAndDataObjectName(
			final String parentPath, final String fileName)
			throws FileNotFoundException, JargonException {

		log.info("retrieveObjectStatForPathAndDataObjectName()");
		if (parentPath == null || parentPath.isEmpty()) {
			throw new IllegalArgumentException("null or empty parentPath");
		}
		if (fileName == null || fileName.isEmpty()) {
			throw new IllegalArgumentException("null or empty fileName");
		}
		StringBuilder sb = new StringBuilder();
		sb.append(parentPath);
		sb.append('/');
		sb.append(fileName);
		return retrieveObjectStatForPath(sb.toString());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO#
	 * retrieveObjectStatForPath(java.lang.String)
	 */
	@Override
	public ObjStat retrieveObjectStatForPath(final String irodsAbsolutePath)
			throws FileNotFoundException, JargonException {

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"irodsAbsolutePath is null or empty");
		}

		/*
		 * StopWatch stopWatch = null;
		 * 
		 * if (this.isInstrumented()) { stopWatch = new
		 * Log4JStopWatch("retrieveObjectStatForPath"); }
		 */

		MiscIRODSUtils.checkPathSizeForMax(irodsAbsolutePath);

		DataObjInpForObjStat dataObjInp = DataObjInpForObjStat
				.instance(irodsAbsolutePath);
		Tag response;
		try {
			response = getIRODSProtocol().irodsFunction(dataObjInp);
		} catch (DataNotFoundException e) {
			log.info("rethrow DataNotFound as FileNotFound per contract");
			throw new FileNotFoundException(e);
		}

		log.debug("response from objStat: {}", response.parseTag());

		/**
		 * For spec cols - soft link - phyPath = parent canonical dir -objPath =
		 * canonical path
		 */
		ObjStat objStat = new ObjStat();
		objStat.setAbsolutePath(irodsAbsolutePath);
		objStat.setChecksum(response.getTag("chksum").getStringValue());
		objStat.setDataId(response.getTag("dataId").getIntValue());
		int objType = response.getTag("objType").getIntValue();
		objStat.setObjectType(ObjectType.values()[objType]);
		objStat.setObjSize(response.getTag("objSize").getLongValue());
		objStat.setOwnerName(response.getTag("ownerName").getStringValue());
		objStat.setOwnerZone(response.getTag("ownerZone").getStringValue());
		objStat.setSpecColType(SpecColType.NORMAL);
		Tag specColl = response.getTag("SpecColl_PI");

		/*
		 * Look for the specColl tag (it is expected to be there) and see if
		 * there are any special collection types (e.g. mounted or soft links)
		 * to deal with
		 */
		if (specColl != null) {

			Tag tag = specColl.getTag("collection");

			if (tag != null) {
				objStat.setCollectionPath(tag.getStringValue());
			}

			tag = specColl.getTag("cacheDir");

			if (tag != null) {
				objStat.setCacheDir(tag.getStringValue());
			}

			tag = specColl.getTag("cacheDirty");

			if (tag != null) {
				objStat.setCacheDirty(tag.getStringValue().equals("1"));
			}

			int collClass = specColl.getTag("collClass").getIntValue();
			objStat.setReplNumber(specColl.getTag("replNum").getIntValue());

			switch (collClass) {
			case 0:
				objStat.setSpecColType(SpecColType.NORMAL);
				objStat.setObjectPath(specColl.getTag("phyPath")
						.getStringValue());
				break;
			case 1:
				objStat.setSpecColType(SpecColType.STRUCT_FILE_COLL);
				break;
			case 2:
				objStat.setSpecColType(SpecColType.MOUNTED_COLL);
				break;
			case 3:
				objStat.setSpecColType(SpecColType.LINKED_COLL);

				/*
				 * physical path will hold the canonical source dir where it was
				 * linked. The collection path will hold the top level of the
				 * soft link target. This does not 'follow' by incrementing the
				 * path as you descend into subdirs, so I use the collection
				 * path to chop off the absolute path, and use the remainder
				 * appended to the collection path to arrive at equivalent
				 * canonical source path fo rthis soft linked directory. This is
				 * all rather confusing, so instead of worrying about it, Jargon
				 * has the headache, you can just trust the objStat objectPath
				 * to point to the equivalent canonical source path to the soft
				 * linked path.
				 */
				String canonicalSourceDirForSoftLink = specColl.getTag(
						"phyPath").getStringValue();
				String softLinkTargetDir = specColl.getTag("collection")
						.getStringValue();
				if (softLinkTargetDir.length() > objStat.getAbsolutePath()
						.length()) {
					throw new JargonException(
							"cannot properly compute path for soft link");
				}

				String additionalPath = objStat.getAbsolutePath().substring(
						softLinkTargetDir.length());
				StringBuilder sb = new StringBuilder();
				sb.append(canonicalSourceDirForSoftLink);
				sb.append(additionalPath);
				objStat.setObjectPath(sb.toString());

				break;
			default:
				throw new JargonException("unknown special coll type:");
			}

		}

		String createdDate = response.getTag("createTime").getStringValue();
		String modifiedDate = response.getTag("modifyTime").getStringValue();
		objStat.setCreatedAt(IRODSDataConversionUtil
				.getDateFromIRODSValue(createdDate));
		objStat.setModifiedAt(IRODSDataConversionUtil
				.getDateFromIRODSValue(modifiedDate));

		/*
		 * if (this.isInstrumented()) { stopWatch.stop(); }
		 */

		log.info(objStat.toString());
		return objStat;

	}

}
