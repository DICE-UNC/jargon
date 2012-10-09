package org.irods.jargon.core.pub;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.DataObjInpForObjStat;
import org.irods.jargon.core.packinstr.Tag;
import org.irods.jargon.core.pub.aohelper.CollectionAOHelper;
import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.core.pub.domain.ObjStat.SpecColType;
import org.irods.jargon.core.pub.domain.UserFilePermission;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileSystemAOHelper;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType;
import org.irods.jargon.core.query.IRODSGenQuery;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.core.utils.CollectionAndPath;
import org.irods.jargon.core.utils.FederationEnabled;
import org.irods.jargon.core.utils.IRODSDataConversionUtil;
import org.irods.jargon.core.utils.MiscIRODSUtils;
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
 * methods across federations, and with special collections (e.g. soft links,
 * mounted collections) supported.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class CollectionAndDataObjectListAndSearchAOImpl extends IRODSGenericAO
		implements CollectionAndDataObjectListAndSearchAO {

	private static final String QUERY_EXCEPTION_FOR_QUERY = "query exception for  query:";
	public static final Logger log = LoggerFactory
			.getLogger(CollectionAndDataObjectListAndSearchAOImpl.class);
	private static final char COMMA = ',';

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

		ObjStat objStat = retrieveObjectStatForPath(absolutePath);

		/*
		 * See if jargon supports the given object type
		 */
		MiscIRODSUtils.evaluateSpecCollSupport(objStat);

		IRODSFile entryFile = this.getIRODSFileFactory().instanceIRODSFile(
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
		entry.setOwnerZone(objStat.getOwnerZone());
		entry.setSpecColType(objStat.getSpecColType());
		entry.setSpecialObjectPath(objStat.getObjectPath());
		log.info("created entry for path as: {}", entry);
		return entry;

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

		try {
			objStat = retrieveObjectStatForPath(absolutePathToParent);
		} catch (FileNotFoundException fnf) {
			log.info("didnt find an objStat for the path, account for cases where there are strict acls and give Jargon a chance to drill down to a place where the user has permissions");
			return handleNoListingUnderRootOrHomeByLookingForPublicAndHome(absolutePathToParent);
		}

		/*
		 * See if jargon supports the given object type
		 */
		MiscIRODSUtils.evaluateSpecCollSupport(objStat);

		List<CollectionAndDataObjectListingEntry> entries = new ArrayList<CollectionAndDataObjectListingEntry>();

		entries.addAll(listCollectionsUnderPath(objStat, 0));
		entries.addAll(listDataObjectsUnderPath(objStat, 0));
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

		try {
			objStat = retrieveObjectStatForPath(absolutePathToParent);
		} catch (FileNotFoundException fnf) {
			log.info("didnt find an objStat for the path, account for cases where there are strict acls and give Jargon a chance to drill down to a place where the user has permissions");
			return handleNoListingUnderRootOrHomeByLookingForPublicAndHome(absolutePathToParent);
		}

		/*
		 * See if jargon supports the given object type
		 */
		MiscIRODSUtils.evaluateSpecCollSupport(objStat);

		List<CollectionAndDataObjectListingEntry> entries = listCollectionsUnderPathWithPermissions(
				absolutePathToParent, 0, objStat);
		entries.addAll(listDataObjectsUnderPathWithPermissions(
				absolutePathToParent, 0, objStat));
		return entries;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO#
	 * countDataObjectsAndCollectionsUnderPath(java.lang.String)
	 * 
	 * softlink
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
				this.getIRODSSession(), this.getIRODSAccount());

		StringBuilder query = new StringBuilder();
		query.append("SELECT COUNT(");

		query.append(RodsGenQueryEnum.COL_COLL_NAME.getName());
		query.append("), COUNT(");
		query.append(RodsGenQueryEnum.COL_DATA_NAME.getName());

		query.append(") WHERE ");
		query.append(RodsGenQueryEnum.COL_COLL_NAME.getName());
		query.append(" = '");
		query.append(IRODSDataConversionUtil
				.escapeSingleQuotes(effectiveAbsolutePath));
		query.append("'");

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(query.toString(), 1);
		IRODSQueryResultSetInterface resultSet;
		String zone = MiscIRODSUtils.getZoneInPath(effectiveAbsolutePath);

		try {
			resultSet = irodsGenQueryExecutor
					.executeIRODSQueryAndCloseResultInZone(irodsQuery, 0, zone);
		} catch (JargonQueryException e) {
			log.error(QUERY_EXCEPTION_FOR_QUERY + query.toString(), e);
			throw new JargonException(e);
		}

		int fileCtr = 0;

		if (resultSet.getResults().size() > 0) {
			fileCtr = IRODSDataConversionUtil
					.getIntOrZeroFromIRODSValue(resultSet.getFirstResult()
							.getColumn(0));
		}

		query = new StringBuilder();
		query.append("SELECT COUNT(");

		query.append(RodsGenQueryEnum.COL_COLL_TYPE.getName());
		query.append(") , COUNT(");
		query.append(RodsGenQueryEnum.COL_COLL_NAME.getName());

		query.append(") WHERE ");
		query.append(RodsGenQueryEnum.COL_COLL_PARENT_NAME.getName());
		query.append(" = '");
		query.append(IRODSDataConversionUtil
				.escapeSingleQuotes(effectiveAbsolutePath));
		query.append("'");

		irodsQuery = IRODSGenQuery.instance(query.toString(), 1);

		try {
			resultSet = irodsGenQueryExecutor
					.executeIRODSQueryAndCloseResultInZone(irodsQuery, 0, zone);
		} catch (JargonQueryException e) {
			log.error(QUERY_EXCEPTION_FOR_QUERY + query.toString(), e);
			throw new JargonException("error in exists query", e);
		}

		int collCtr = 0;
		if (resultSet.getResults().size() > 0) {
			collCtr = IRODSDataConversionUtil
					.getIntOrZeroFromIRODSValue(resultSet.getFirstResult()
							.getColumn(0));
		}

		int total = fileCtr + collCtr;

		log.debug("computed count = {}", total);
		return total;

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
		return searchCollectionsBasedOnName(searchTerm, 0);
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

		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		sb.append(CollectionAOHelper
				.buildSelectsNeededForCollectionsInCollectionsAndDataObjectsListingEntry());
		sb.append(" WHERE ");
		sb.append(RodsGenQueryEnum.COL_COLL_NAME.getName());
		sb.append(" LIKE '%");
		sb.append(searchTerm.trim());
		sb.append("%'");

		IRODSGenQueryExecutor irodsGenQueryExecutor = new IRODSGenQueryExecutorImpl(
				this.getIRODSSession(), this.getIRODSAccount());

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(sb.toString(),
				getIRODSSession().getJargonProperties()
						.getMaxFilesAndDirsQueryMax());
		IRODSQueryResultSetInterface resultSet;

		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQueryWithPaging(
					irodsQuery, partialStartIndex);
		} catch (JargonQueryException e) {
			log.error("query exception", e);
			throw new JargonException("error in exists query", e);
		}

		List<CollectionAndDataObjectListingEntry> entries = new ArrayList<CollectionAndDataObjectListingEntry>();
		for (IRODSQueryResultRow row : resultSet.getResults()) {
			entries.add(CollectionAOHelper
					.buildCollectionListEntryFromResultSetRowForCollectionQuery(row));
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
	public List<CollectionAndDataObjectListingEntry> listCollectionsUnderPath(
			final String absolutePathToParent, final int partialStartIndex)
			throws FileNotFoundException, JargonException {

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

		try {
			objStat = retrieveObjectStatForPath(path);
		} catch (FileNotFoundException fnf) {
			log.info("didnt find an objStat for the path, account for cases where there are strict acls and give Jargon a chance to drill down to a place where the user has permissions");
			return handleNoListingUnderRootOrHomeByLookingForPublicAndHome(path);
		}

		/*
		 * See if jargon supports the given object type
		 */
		MiscIRODSUtils.evaluateSpecCollSupport(objStat);
		return listCollectionsUnderPath(objStat, partialStartIndex);

	}

	/**
	 * This is a compensating method used to deal with the top of the tree when
	 * permissions do not allow listing'into' the tree to get to things the user
	 * actually has access to.
	 * 
	 * @param absolutePathToParent
	 * @return
	 * @throws JargonException
	 */
	private List<CollectionAndDataObjectListingEntry> handleNoListingUnderRootOrHomeByLookingForPublicAndHome(
			final String absolutePathToParent) throws FileNotFoundException,
			JargonException {

		log.info("handleNoListingUnderRootOrHomeByLookingForPublicAndHome()");

		String path = absolutePathToParent;
		List<CollectionAndDataObjectListingEntry> collectionAndDataObjectListingEntries = new ArrayList<CollectionAndDataObjectListingEntry>();

		/*
		 * This is somewhat convoluted, note the return statements in the
		 * various conditions
		 */

		if (!this.getJargonProperties()
				.isDefaultToPublicIfNothingUnderRootWhenListing()) {
			log.info("not configured in jargon.properties to look for public and user home, throw the FileNotFoundException");
			throw new FileNotFoundException("the collection cannot be found");
		}

		// check if under '/' and infer that there is a '/zone' subdir to return
		// this time

		if (path.equals("/")) {
			collectionAndDataObjectListingEntries
					.add(createStandInForZoneDir());
			return collectionAndDataObjectListingEntries;
		}

		// check if under '/zone' and if so infer that there is a home dir

		StringBuilder sb = new StringBuilder();
		sb.append("/");
		sb.append(this.getIRODSAccount().getZone());

		String comparePath = sb.toString();

		if (path.equals(comparePath)) {
			log.info("under zone, create stand-in home dir");
			collectionAndDataObjectListingEntries
					.add(createStandInForHomeDir());
			return collectionAndDataObjectListingEntries;
		}

		/*
		 * check if I am under /zone/home, look for public and user dir. In this
		 * situation I should be able to list them via obj stat
		 */

		sb = new StringBuilder();
		sb.append("/");
		sb.append(this.getIRODSAccount().getZone());
		sb.append("/home");

		comparePath = sb.toString();

		if (!path.equals(comparePath)) {
			log.info("I am not unde /, /zone/, or /zohe/home/ so I cannot do anything but throw the original exception");
			log.info("not configured in jargon.properties to look for public and user home, throw the FileNotFoundException");
			throw new FileNotFoundException("the collection cannot be found");

		}

		log.info("under home, look for public and home dir");
		sb.append("/public");
		ObjStat statForPublic;
		try {
			statForPublic = this.retrieveObjectStatForPath(sb.toString());
			collectionAndDataObjectListingEntries
					.add(createStandInForPublicDir(statForPublic));
		} catch (FileNotFoundException fnf) {
			log.info("no public dir");
		}

		log.info("see if a user home dir applies");

		if (this.getIRODSAccount().isAnonymousAccount()) {
			log.info("is anonymous account, no home directory applies");
		} else {
			ObjStat statForUserHome;
			try {
				statForUserHome = this
						.retrieveObjectStatForPath(MiscIRODSUtils
								.computeHomeDirectoryForIRODSAccount(getIRODSAccount()));
				collectionAndDataObjectListingEntries
						.add(createStandInForUserDir(statForUserHome));
			} catch (FileNotFoundException fnf) {
				log.info("no home dir");
			}
		}

		// I was under /zone/home/ looking for public and user dirs, return what
		// I have, it could be empty
		return collectionAndDataObjectListingEntries;

	}

	private CollectionAndDataObjectListingEntry createStandInForZoneDir() {
		log.info("under root, put out zone as an entry");
		CollectionAndDataObjectListingEntry entry = new CollectionAndDataObjectListingEntry();
		entry.setCount(0);
		entry.setLastResult(true);
		entry.setObjectType(ObjectType.COLLECTION);
		entry.setOwnerZone(this.getIRODSAccount().getZone());
		StringBuilder sb = new StringBuilder();
		sb.append("/");
		entry.setParentPath(sb.toString());
		sb.append(this.getIRODSAccount().getZone());
		entry.setPathOrName(sb.toString());
		entry.setSpecColType(SpecColType.NORMAL);
		return entry;
	}

	/**
	 * @param collectionAndDataObjectListingEntries
	 */
	private CollectionAndDataObjectListingEntry createStandInForHomeDir() {
		log.info("under root, put out home as an entry");
		CollectionAndDataObjectListingEntry entry = new CollectionAndDataObjectListingEntry();
		entry.setCount(0);
		entry.setLastResult(true);
		entry.setObjectType(ObjectType.COLLECTION);
		entry.setOwnerZone(this.getIRODSAccount().getZone());
		entry.setParentPath("/");
		StringBuilder sb = new StringBuilder();
		sb.append("/");
		sb.append(this.getIRODSAccount().getZone());
		sb.append("/home");
		entry.setPathOrName(sb.toString());
		entry.setSpecColType(SpecColType.NORMAL);
		return entry;
	}

	/**
	 * Create a collection and listing entry for the home dir
	 * 
	 * @return
	 */
	private CollectionAndDataObjectListingEntry createStandInForPublicDir(
			final ObjStat objStat) {
		log.info("under root, put out home as an entry");
		CollectionAndDataObjectListingEntry entry = new CollectionAndDataObjectListingEntry();
		entry.setCount(0);
		entry.setLastResult(true);
		entry.setOwnerZone(this.getIRODSAccount().getZone());
		entry.setOwnerName(objStat.getOwnerName());
		StringBuilder sb = new StringBuilder();
		sb.append("/");
		sb.append(this.getIRODSAccount().getZone());
		sb.append("/home");
		entry.setParentPath(sb.toString());
		sb.append("/public");
		entry.setPathOrName(sb.toString());
		entry.setSpecColType(objStat.getSpecColType());
		entry.setCreatedAt(objStat.getCreatedAt());
		entry.setId(objStat.getDataId());
		entry.setObjectType(objStat.getObjectType());
		return entry;
	}

	/**
	 * Create a collection and listing entry for the home dir
	 * 
	 * @return
	 */
	private CollectionAndDataObjectListingEntry createStandInForUserDir(
			final ObjStat objStat) {
		log.info("put a user dir entry out there");
		CollectionAndDataObjectListingEntry entry = new CollectionAndDataObjectListingEntry();
		entry.setCount(0);
		entry.setLastResult(true);
		entry.setObjectType(ObjectType.COLLECTION);
		entry.setOwnerZone(this.getIRODSAccount().getZone());
		StringBuilder sb = new StringBuilder();
		sb.append("/");
		sb.append(this.getIRODSAccount().getZone());
		sb.append("/home");
		entry.setParentPath(sb.toString());
		sb.append(this.getIRODSAccount().getUserName());
		entry.setPathOrName(sb.toString());
		entry.setSpecColType(objStat.getSpecColType());
		entry.setCreatedAt(objStat.getCreatedAt());
		entry.setId(objStat.getDataId());
		entry.setObjectType(objStat.getObjectType());
		entry.setOwnerZone(this.getIRODSAccount().getZone());
		entry.setOwnerName(objStat.getOwnerName());
		return entry;
	}

	/**
	 * List the collections underneath the given path
	 * <p/>
	 * Works with soft links
	 * 
	 * @param objStat
	 *            {@link ObjStat} from iRODS that details the nature of the
	 *            collection
	 * @param partialStartIndex
	 * @return
	 * @throws FileNotFoundException
	 * @throws JargonException
	 * 
	 * 
	 */
	private List<CollectionAndDataObjectListingEntry> listCollectionsUnderPath(
			final ObjStat objStat, final int partialStartIndex)
			throws FileNotFoundException, JargonException {

		log.info("listCollectionsUnderPath()");

		if (objStat == null) {
			throw new IllegalArgumentException("objStat is null");
		}

		/*
		 * See if jargon supports the given object type
		 */
		MiscIRODSUtils.evaluateSpecCollSupport(objStat);

		/*
		 * Special collections are processed in different ways.
		 * 
		 * Listing for soft links substitutes the source path for the target
		 * path in the query
		 */

		String effectiveAbsolutePath = MiscIRODSUtils
				.determineAbsolutePathBasedOnCollTypeInObjectStat(objStat);

		List<CollectionAndDataObjectListingEntry> subdirs = new ArrayList<CollectionAndDataObjectListingEntry>();

		String query = IRODSFileSystemAOHelper
				.buildQueryListAllCollections(effectiveAbsolutePath);

		IRODSQueryResultSetInterface resultSet = queryForPathAndReturnResultSet(
				objStat.getAbsolutePath(), query, partialStartIndex, objStat);

		CollectionAndDataObjectListingEntry collectionAndDataObjectListingEntry = null;

		for (IRODSQueryResultRow row : resultSet.getResults()) {
			collectionAndDataObjectListingEntry = CollectionAOHelper
					.buildCollectionListEntryFromResultSetRowForCollectionQuery(row);

			adjustEntryFromRowInCaseOfSpecialCollection(objStat,
					effectiveAbsolutePath, collectionAndDataObjectListingEntry);

			/*
			 * for some reason, a query for collections with a parent of '/'
			 * returns the root as a result, which creates weird situations when
			 * trying to show collections in a tree structure. This test papers
			 * over that idiosyncrasy and discards that extraneous result.
			 */
			if (!collectionAndDataObjectListingEntry.getPathOrName()
					.equals("/")) {
				subdirs.add(collectionAndDataObjectListingEntry);
			}
		}

		return subdirs;

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

		return listCollectionsUnderPathWithPermissions(absolutePathToParent,
				partialStartIndex, objStat);

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
	private List<CollectionAndDataObjectListingEntry> listCollectionsUnderPathWithPermissions(
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

		List<CollectionAndDataObjectListingEntry> subdirs = new ArrayList<CollectionAndDataObjectListingEntry>();

		log.info("listCollectionsUnderPathWithPermissionsForUser for: {}",
				effectiveAbsolutePath);

		String query = IRODSFileSystemAOHelper
				.buildQueryListAllDirsWithUserAccessInfo(effectiveAbsolutePath);

		IRODSQueryResultSetInterface resultSet = queryForPathAndReturnResultSet(
				effectiveAbsolutePath, query, partialStartIndex, objStat);

		CollectionAndDataObjectListingEntry collectionAndDataObjectListingEntry = null;
		List<UserFilePermission> userFilePermissions = new ArrayList<UserFilePermission>();
		String lastPath = "";
		boolean isAtEndOfQueryResults = false;
		UserAO userAO = this.getIRODSAccessObjectFactory().getUserAO(
				getIRODSAccount());

		for (IRODSQueryResultRow row : resultSet.getResults()) {

			isAtEndOfQueryResults = row.isLastResult();
			// compare to the previous path and see if I break, in which case
			// add the last collection entry to the result
			String thisPath = row.getColumn(1);

			if (thisPath.equals(lastPath)) {
				// parse out the file permission and continue,
				CollectionAOHelper
						.buildUserFilePermissionForCollection(
								userFilePermissions, row, userAO,
								effectiveAbsolutePath);
				continue;
			} else {
				// is a break on path, put out the info for the last path if
				// it's there

				if (collectionAndDataObjectListingEntry != null) {
					collectionAndDataObjectListingEntry
							.setUserFilePermission(userFilePermissions);
					augmentCollectionEntryForSpecialCollections(objStat,
							effectiveAbsolutePath,
							collectionAndDataObjectListingEntry);
					collectionAndDataObjectListingEntry.setCount(row
							.getRecordCount() - 1);
					subdirs.add(collectionAndDataObjectListingEntry);
				}

				// on break in path, initialize the data for a new entry
				collectionAndDataObjectListingEntry = CollectionAOHelper
						.buildCollectionListEntryFromResultSetRowForCollectionQuery(row);
				lastPath = collectionAndDataObjectListingEntry.getPathOrName();
				userFilePermissions = new ArrayList<UserFilePermission>();
				CollectionAOHelper
						.buildUserFilePermissionForCollection(
								userFilePermissions, row, userAO,
								effectiveAbsolutePath);
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
				augmentCollectionEntryForSpecialCollections(objStat,
						effectiveAbsolutePath,
						collectionAndDataObjectListingEntry);
				subdirs.add(collectionAndDataObjectListingEntry);
			} else {
				log.debug("ignoring last entry, as it might carry over to the next page of results");
			}
		}

		return subdirs;

	}

	private IRODSQueryResultSetInterface queryForPathAndReturnResultSet(
			final String irodsAbsolutePath, final String queryString,
			final int partialStartIndex, final ObjStat objStat)
			throws JargonException {

		log.info("queryForPathAndReturnResultSet for: {}", irodsAbsolutePath);
		IRODSGenQueryExecutor irodsGenQueryExecutor = new IRODSGenQueryExecutorImpl(
				this.getIRODSSession(), this.getIRODSAccount());

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString,
				getIRODSSession().getJargonProperties()
						.getMaxFilesAndDirsQueryMax());
		IRODSQueryResultSetInterface resultSet;

		try {
			resultSet = irodsGenQueryExecutor
					.executeIRODSQueryWithPagingInZone(irodsQuery,
							partialStartIndex,
							MiscIRODSUtils.getZoneInPath(irodsAbsolutePath));
		} catch (JargonQueryException e) {
			log.error(QUERY_EXCEPTION_FOR_QUERY + queryString, e);
			throw new JargonException(e);
		}

		return resultSet;

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

		if (objStat == null) {
			log.error("unable to find objStat for collection path:{}",
					absolutePathToParent);
			throw new FileNotFoundException(
					"unable to find objStat for collection");
		}

		return listDataObjectsUnderPath(objStat, partialStartIndex);

	}

	private List<CollectionAndDataObjectListingEntry> listDataObjectsUnderPath(
			final ObjStat objStat, final int partialStartIndex)
			throws JargonException {

		log.info("listDataObjectsUnderPath(objStat, partialStartIndex)");

		if (objStat == null) {
			throw new IllegalArgumentException(
					"collectionAndDataObjectListingEntry is null");
		}

		String effectiveAbsolutePath = MiscIRODSUtils
				.determineAbsolutePathBasedOnCollTypeInObjectStat(objStat);
		log.info("determined effectiveAbsolutePathToBe:{}",
				effectiveAbsolutePath);

		List<CollectionAndDataObjectListingEntry> files = new ArrayList<CollectionAndDataObjectListingEntry>();

		log.info("listDataObjectsUnderPath for: {}", objStat);

		StringBuilder query = new StringBuilder(
				IRODSFileSystemAOHelper
						.buildQueryListAllDataObjectsWithSizeAndDateInfo(effectiveAbsolutePath));
		IRODSQueryResultSetInterface resultSet;

		try {
			resultSet = queryForPathAndReturnResultSet(effectiveAbsolutePath,
					query.toString(), partialStartIndex, objStat);
		} catch (JargonException e) {
			log.error("exception querying for data objects:{}",
					query.toString(), e);
			throw new JargonException("error in query", e);
		}

		/*
		 * the query that gives the necessary data will cause duplication when
		 * there are replicas, so discard duplicates. This is the nature of
		 * GenQuery.
		 */
		String lastPath = "";
		String currentPath = "";
		CollectionAndDataObjectListingEntry entry;
		for (IRODSQueryResultRow row : resultSet.getResults()) {
			entry = CollectionAOHelper
					.buildCollectionListEntryFromResultSetRowForDataObjectQuery(row);

			/**
			 * Use the data in the objStat, in the case of special collections,
			 * to augment the data returned
			 */
			augmentCollectionEntryForSpecialCollections(objStat,
					effectiveAbsolutePath, entry);

			StringBuilder sb = new StringBuilder();
			sb.append(entry.getParentPath());
			sb.append('/');
			sb.append(entry.getPathOrName());
			currentPath = sb.toString();
			if (currentPath.equals(lastPath)) {
				continue;
			}

			lastPath = currentPath;
			files.add(entry);
		}

		return files;

	}

	/**
	 * Use the data in the objStat, in the case of special collections, to
	 * augment the entry for a collection
	 * 
	 * @param objStat
	 *            {@link ObjStat} retreived for the parent directory
	 * @param effectiveAbsolutePath
	 *            <code>String</code> with the path used to query, this will be
	 *            the canonical path for the parent collection, and should
	 *            correspond to the absolute path information in the given
	 *            <code>entry</code>.
	 * @param entry
	 *            {@link CollectionAndDataObjectListingEntry} which is the raw
	 *            data returned from querying the iCat based on the
	 *            <code>effectiveAbsolutePath</code>. This information is from
	 *            the perspective of the canonical path, and the given method
	 *            will reframe the <code>entry</code> from the perspective of
	 *            the requested path This means that a query on children of a
	 *            soft link carry the data from the perspective of the soft
	 *            linked directory, even though the iCAT carries the information
	 *            based on the 'source path' of the soft link. This gets pretty
	 *            confusing otherwise.
	 */
	private void augmentCollectionEntryForSpecialCollections(
			final ObjStat objStat, final String effectiveAbsolutePath,
			final CollectionAndDataObjectListingEntry entry) {

		if (objStat.getSpecColType() == SpecColType.LINKED_COLL) {
			log.info("adjusting paths in entry to reflect linked collection info");
			entry.setSpecialObjectPath(objStat.getObjectPath());
			CollectionAndPath collectionAndPathForAbsPath = MiscIRODSUtils
					.separateCollectionAndPathFromGivenAbsolutePath(entry
							.getPathOrName());

			if (entry.isCollection()) {
				entry.setPathOrName(objStat.getAbsolutePath() + "/"
						+ collectionAndPathForAbsPath.getChildName());
				entry.setParentPath(objStat.getAbsolutePath());
			} else {
				entry.setParentPath(objStat.getAbsolutePath());
			}

		}

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

		return listDataObjectsUnderPathWithPermissions(absolutePathToParent,
				partialStartIndex, objStat);
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
	private List<CollectionAndDataObjectListingEntry> listDataObjectsUnderPathWithPermissions(
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
		String effectiveAbsolutePath = MiscIRODSUtils
				.determineAbsolutePathBasedOnCollTypeInObjectStat(objStat);
		log.info("determined effectiveAbsolutePathToBe:{}",
				effectiveAbsolutePath);

		List<CollectionAndDataObjectListingEntry> files = new ArrayList<CollectionAndDataObjectListingEntry>();

		String queryString = IRODSFileSystemAOHelper
				.buildQueryListAllDataObjectsWithUserAccessInfo(effectiveAbsolutePath);
		IRODSQueryResultSetInterface resultSet = this
				.queryForPathAndReturnResultSet(effectiveAbsolutePath,
						queryString, partialStartIndex, objStat);
		log.debug("got result set:{}}, resultSet");

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
							this.getIRODSAccount().getZone());
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
				augmentCollectionEntryForSpecialCollections(objStat,
						effectiveAbsolutePath, entry);
				files.add(entry);
			}

			// clear and reinitialize for new entry set
			entry = CollectionAOHelper
					.buildCollectionListEntryFromResultSetRowForDataObjectQuery(row);
			lastPath = currentPath;
			lastReplNumber = currentReplNumber;
			userFilePermissions = new ArrayList<UserFilePermission>();
			CollectionAOHelper.buildUserFilePermissionForDataObject(
					userFilePermissions, row, effectiveAbsolutePath, this
							.getIRODSAccount().getZone());

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
				augmentCollectionEntryForSpecialCollections(objStat,
						effectiveAbsolutePath, entry);
				files.add(entry);
			} else {
				log.debug("skipping last entry as it may carry over to the next query page");
			}
		}

		return files;

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

		StringBuilder sb = new StringBuilder();
		sb.append("SELECT  DISTINCT ");
		sb.append(RodsGenQueryEnum.COL_COLL_NAME.getName());
		sb.append(COMMA);
		sb.append(RodsGenQueryEnum.COL_DATA_NAME.getName());
		sb.append(COMMA);
		sb.append(RodsGenQueryEnum.COL_D_CREATE_TIME.getName());
		sb.append(COMMA);
		sb.append(RodsGenQueryEnum.COL_D_MODIFY_TIME.getName());
		sb.append(COMMA);
		sb.append(RodsGenQueryEnum.COL_DATA_SIZE.getName());

		sb.append(COMMA);
		sb.append(RodsGenQueryEnum.COL_D_OWNER_NAME.getName());
		sb.append(" WHERE ");
		sb.append(RodsGenQueryEnum.COL_DATA_NAME.getName());
		sb.append(" LIKE '%");
		sb.append(searchTerm.trim());
		sb.append("%'");

		IRODSGenQueryExecutor irodsGenQueryExecutor = new IRODSGenQueryExecutorImpl(
				this.getIRODSSession(), this.getIRODSAccount());

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(sb.toString(),
				getIRODSSession().getJargonProperties()
						.getMaxFilesAndDirsQueryMax());
		IRODSQueryResultSetInterface resultSet;

		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQueryWithPaging(
					irodsQuery, partialStartIndex);
		} catch (JargonQueryException e) {
			log.error(QUERY_EXCEPTION_FOR_QUERY + sb.toString(), e);
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

		if (objStat.isSomeTypeOfCollection()) {
			CollectionAO collectionAO = new CollectionAOImpl(getIRODSSession(),
					getIRODSAccount());
			returnObject = collectionAO.findGivenObjStat(objStat);
		} else {
			DataObjectAO dataObjectAO = new DataObjectAOImpl(getIRODSSession(),
					getIRODSAccount());
			returnObject = dataObjectAO.findGivenObjStat(objStat);
			log.debug("looking for as a data object");
		}

		// get appropriate domain object and return
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
				objStat.setCacheDirty(tag.getStringValue().equals(1));
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
		log.info(objStat.toString());
		return objStat;

	}

	/**
	 * For a collection based on a row from a collection query, evaluate against
	 * the provided objStat and decide whether to modify the resulting listing
	 * entry to reflect special collection status
	 * 
	 * @param objStat
	 * @param effectiveAbsolutePath
	 * @param collectionAndDataObjectListingEntry
	 */
	private void adjustEntryFromRowInCaseOfSpecialCollection(
			final ObjStat objStat,
			final String effectiveAbsolutePath,
			final CollectionAndDataObjectListingEntry collectionAndDataObjectListingEntry) {
		if (objStat.getSpecColType() == SpecColType.LINKED_COLL) {
			log.info("adjusting paths in entry to reflect linked collection info");
			StringBuilder sb = new StringBuilder();
			sb.append(objStat.getObjectPath());
			sb.append('/');
			sb.append(MiscIRODSUtils
					.getLastPathComponentForGiveAbsolutePath(collectionAndDataObjectListingEntry
							.getPathOrName()));
			collectionAndDataObjectListingEntry.setSpecialObjectPath(sb
					.toString());

			sb = new StringBuilder();
			sb.append(objStat.getAbsolutePath());
			sb.append('/');
			sb.append(MiscIRODSUtils
					.getLastPathComponentForGiveAbsolutePath(collectionAndDataObjectListingEntry
							.getPathOrName()));

			collectionAndDataObjectListingEntry.setPathOrName(sb.toString());

			collectionAndDataObjectListingEntry.setParentPath(objStat
					.getAbsolutePath());
			collectionAndDataObjectListingEntry
					.setSpecColType(SpecColType.LINKED_COLL);
		}
	}

}
