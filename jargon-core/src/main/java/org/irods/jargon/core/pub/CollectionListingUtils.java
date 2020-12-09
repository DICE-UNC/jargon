/**
 *
 */
package org.irods.jargon.core.pub;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.FileDriverError;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.DataObjInpForObjStat;
import org.irods.jargon.core.packinstr.DataObjInpForQuerySpecColl;
import org.irods.jargon.core.packinstr.SpecColInfo;
import org.irods.jargon.core.packinstr.Tag;
import org.irods.jargon.core.pub.aohelper.CollectionAOHelper;
import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.core.pub.domain.ObjStat.SpecColType;
import org.irods.jargon.core.pub.domain.Zone;
import org.irods.jargon.core.pub.io.IRODSFileSystemAOHelper;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType;
import org.irods.jargon.core.query.GenQueryBuilderException;
import org.irods.jargon.core.query.GenQueryField.SelectFieldTypes;
import org.irods.jargon.core.query.IRODSGenQueryBuilder;
import org.irods.jargon.core.query.IRODSGenQueryFromBuilder;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSet;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.QueryConditionOperators;
import org.irods.jargon.core.query.QueryResultProcessingUtils;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.core.utils.CollectionAndPath;
import org.irods.jargon.core.utils.IRODSDataConversionUtil;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic utils (for the package) to do collection listings
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
class CollectionListingUtils {

	private final IRODSAccount irodsAccount;
	private final IRODSAccessObjectFactory irodsAccessObjectFactory;
	public static final String QUERY_EXCEPTION_FOR_QUERY = "query exception for  query:";

	static final Logger log = LoggerFactory.getLogger(CollectionListingUtils.class);

	/**
	 * This is a compensating method used to deal with the top of the tree when
	 * permissions do not allow listing 'into' the tree to get to things the user
	 * actually has access to.
	 * <p>
	 * Phase 1 - when path is / - obtain a listing of zones and display as
	 * subdirectories
	 * <p>
	 * Phase 2 when path is /zone - interpolate a 'home' directory
	 * <p>
	 * Phase 3 when path is /zone/home - for current zone - add a dir for the user
	 * name, see if a public dir. For foreign zone, add a dir for user#homeZone and
	 * see if a public dir
	 *
	 *
	 *
	 * @param absolutePathToParent {@code String} with the current parent
	 * @return {@code List} of {@link CollectionAndDataObjectListingEntry} that has
	 *         the children under the parent. These children may be simulated per
	 *         the given rules
	 * @throws JargonException {@link JargonException}
	 */
	List<CollectionAndDataObjectListingEntry> handleNoListingUnderRootOrHome(final String absolutePathToParent)
			throws FileNotFoundException, JargonException {

		log.info("handleNoListingUnderRootOrHomeByLookingForPublicAndHome()");

		String path = absolutePathToParent;

		List<CollectionAndDataObjectListingEntry> collectionAndDataObjectListingEntries = new ArrayList<>();

		/*
		 * Do I have compensating actions configured? If not, it's just a file not found
		 */
		if (!irodsAccessObjectFactory.getJargonProperties().isDefaultToPublicIfNothingUnderRootWhenListing()) {
			log.info(
					"not configured in jargon.properties to look for public and user home, throw the FileNotFoundException");
			throw new FileNotFoundException("the collection cannot be found");
		}

		/*
		 * Phase1 - under root
		 *
		 * Get a list of zones as subdirs
		 */

		if (path.equals("/")) {
			log.info("phase1 - under root, add zones");
			collectionAndDataObjectListingEntries.addAll(createStandInForZoneDir());
			return collectionAndDataObjectListingEntries;
		}

		/*
		 * Phase2 - under a zone, add a home
		 */

		List<String> components = MiscIRODSUtils.breakIRODSPathIntoComponents(path);
		if (components.size() == 2) {
			log.info("under zone, add a home");
			log.info("assume this is a zone name, look for a home dir under the zone name");
			collectionAndDataObjectListingEntries.add(createStandInForHomeDir(path));
			return collectionAndDataObjectListingEntries;
		}

		/*
		 * Phase 3, under home, go ahead and list!
		 */

		final List<CollectionAndDataObjectListingEntry> entries = new ArrayList<>();
		ObjStat homeStat = new ObjStat();
		homeStat.setAbsolutePath(path);
		homeStat.setObjectType(ObjectType.COLLECTION_HEURISTIC_STANDIN);

		entries.addAll(listCollectionsUnderPath(homeStat, 0));
		entries.addAll(listDataObjectsUnderPath(homeStat, 0));

		if (entries.isEmpty()) {
			log.info("final fallback...I can't find anything!");
			components = MiscIRODSUtils.breakIRODSPathIntoComponents(path);
			if (components.size() == 3 && components.get(2).equals("home")) {
				log.info("under home, see if same zone as login");
				if (irodsAccount.getZone().equals(components.get(1))) {
					log.info("under logged in zone, add user and public dirs");
					collectionAndDataObjectListingEntries.addAll(createStandInsUnderHomeInLoggedInZone(path));
				} else {
					log.info("under federated zone, add federated user");
					collectionAndDataObjectListingEntries
							.addAll(createStandInsUnderHomeInFederatedZone(components.get(1)));
				}
				return collectionAndDataObjectListingEntries;
			}
		} else {
			return entries;
		}

		/*
		 * Fall through is a legit file not found exception
		 */

		log.info("really is a not found for file:{}", path);
		throw new FileNotFoundException("unable to find file under path");

	}

	/**
	 * Heuristic processing allows ObjStats to be returned (though fake) at points
	 * in the hierarchy were strict ACLs would otherwise preclude
	 *
	 * @param irodsAbsolutePath {@code String} with the iRODS path
	 * @return {@link ObjStat}
	 * @throws FileNotFoundException {@link FileNotFoundException}
	 * @throws JargonException       {@link JargonException}
	 */
	ObjStat handleNoObjStatUnderRootOrHomeByLookingForPublicAndHome(final String irodsAbsolutePath)
			throws FileNotFoundException, JargonException {

		log.info("handleNoObjStatUnderRootOrHomeByLookingForPublicAndHome()");
		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty irodsAbsolutePath");
		}

		ObjStat objStat = null;

		/*
		 * Do I have compensating actions configured? If not, it's just a file not found
		 */
		if (!irodsAccessObjectFactory.getJargonProperties().isDefaultToPublicIfNothingUnderRootWhenListing()) {
			log.info(
					"not configured in jargon.properties to look for public and user home, throw the FileNotFoundException");
			throw new FileNotFoundException("the object cannot be found");
		}

		/*
		 * Phase1 - under root
		 *
		 * Generate an objStat for root
		 */

		if (irodsAbsolutePath.equals("/")) {
			log.info("phase1 - under root");
			objStat = new ObjStat();
			objStat.setAbsolutePath(irodsAbsolutePath);
			objStat.setObjectType(ObjectType.COLLECTION_HEURISTIC_STANDIN);
			objStat.setSpecColType(SpecColType.NORMAL);
			objStat.setStandInGeneratedObjStat(true);
			objStat.setModifiedAt(new Date());
			objStat.setCreatedAt(new Date());
			log.info("return a fake objStat for root:{}", objStat);
			return objStat;
		}

		List<String> components = MiscIRODSUtils.breakIRODSPathIntoComponents(irodsAbsolutePath);

		/*
		 * Phase2 - first path under root should be a zone
		 */
		if (components.size() == 2) {
			ZoneAO zoneAO = irodsAccessObjectFactory.getZoneAO(irodsAccount);
			List<String> zones = zoneAO.listZoneNames();
			boolean found = false;
			for (String zone : zones) {
				if (zone.equals(components.get(1))) {
					log.info("zone matches:{}", zone);
					found = true;
					break;
				}
			}
			if (!found) {
				log.error("not a valid zone, cannot interpolate this path:{}", irodsAbsolutePath);
				throw new FileNotFoundException("path does not exist");
			}
			objStat = new ObjStat();
			objStat.setAbsolutePath(irodsAbsolutePath);
			objStat.setObjectType(ObjectType.COLLECTION_HEURISTIC_STANDIN);
			objStat.setSpecColType(SpecColType.NORMAL);
			objStat.setStandInGeneratedObjStat(true);
			objStat.setModifiedAt(new Date());
			objStat.setCreatedAt(new Date());
			log.info("return a fake objStat for zone:{}", objStat);
			return objStat;
		}

		/*
		 * Phase3 - create stand in for home under zone
		 */
		if (components.size() == 3) {
			log.info("under zone, add a home obj stat");
			if (components.get(2).equals("home")) {
				objStat = new ObjStat();
				objStat.setAbsolutePath(irodsAbsolutePath);
				objStat.setObjectType(ObjectType.COLLECTION_HEURISTIC_STANDIN);
				objStat.setSpecColType(SpecColType.NORMAL);
				objStat.setStandInGeneratedObjStat(true);
				objStat.setModifiedAt(new Date());
				objStat.setCreatedAt(new Date());
				log.info("return a fake objStat for zone/home:{}", objStat);
				return objStat;
			}
		}
		/*
		 * Fall through is a legit file not found exception
		 */

		log.info("really is a not found for file:{}", irodsAbsolutePath);
		throw new FileNotFoundException("unable to find file under path");

	}

	private List<CollectionAndDataObjectListingEntry> createStandInsUnderHomeInLoggedInZone(final String path)
			throws FileNotFoundException, JargonException {
		List<CollectionAndDataObjectListingEntry> collectionAndDataObjectListingEntries = new ArrayList<>();
		// if same zone, look for public and home, if cross zone, look for a
		// user dir in zone and public
		log.info("under home, look for public and user dir");
		StringBuilder sb = new StringBuilder(path);
		sb.append("/public");
		ObjStat statForPublic;
		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = irodsAccessObjectFactory
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
		try {
			statForPublic = collectionAndDataObjectListAndSearchAO.retrieveObjectStatForPath(sb.toString());
			collectionAndDataObjectListingEntries.add(createStandInForPublicDir(statForPublic));
		} catch (FileNotFoundException fnf) {
			log.info("no public dir");
		}

		log.info("see if a user home dir applies");

		ObjStat statForUserHome;
		try {
			statForUserHome = collectionAndDataObjectListAndSearchAO.retrieveObjectStatForPath(MiscIRODSUtils
					.computeHomeDirectoryForIRODSAccount(collectionAndDataObjectListAndSearchAO.getIRODSAccount()));
			collectionAndDataObjectListingEntries.add(createStandInForUserDir(statForUserHome));
		} catch (FileNotFoundException fnf) {
			log.info("no home dir");
		}
		return collectionAndDataObjectListingEntries;
	}

	private List<CollectionAndDataObjectListingEntry> createStandInsUnderHomeInFederatedZone(final String zone)
			throws FileNotFoundException, JargonException {
		List<CollectionAndDataObjectListingEntry> collectionAndDataObjectListingEntries = new ArrayList<>();
		// if same zone, look for public and home, if cross zone, look for a
		// user dir in zone and public
		log.info("under home in federated zone, look for public and home dir");

		StringBuilder sb = new StringBuilder();
		sb.append("/");
		sb.append(zone);
		sb.append("/home/public");
		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = irodsAccessObjectFactory
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);

		try {
			ObjStat statForPublic = collectionAndDataObjectListAndSearchAO.retrieveObjectStatForPath(sb.toString());
			collectionAndDataObjectListingEntries.add(createStandInForPublicDir(statForPublic));
		} catch (FileNotFoundException fnf) {
			log.info("no public dir");
		}

		log.info("see if a user home dir applies");

		try {
			ObjStat homeStat = collectionAndDataObjectListAndSearchAO.retrieveObjectStatForPath(
					MiscIRODSUtils.computeHomeDirectoryForIRODSAccountInFederatedZone(irodsAccount, zone));

			collectionAndDataObjectListingEntries.add(createStandInForUserDir(homeStat));
		} catch (FileNotFoundException fnf) {
			log.info("no user dir");
		}
		return collectionAndDataObjectListingEntries;
	}

	private List<CollectionAndDataObjectListingEntry> createStandInForZoneDir()
			throws FileNotFoundException, JargonException {
		log.info("under root, put out zone as an entry");

		CollectionAndDataObjectListingEntry entry;
		List<CollectionAndDataObjectListingEntry> entries = new ArrayList<>();
		StringBuilder sb;
		ZoneAO zoneAO = irodsAccessObjectFactory.getZoneAO(irodsAccount);

		List<Zone> zones = zoneAO.listZones();

		int count = 1;
		for (Zone zone : zones) {
			entry = new CollectionAndDataObjectListingEntry();
			entry.setParentPath("/");
			sb = new StringBuilder();
			sb.append("/");
			sb.append(zone.getZoneName());
			entry.setPathOrName(sb.toString());
			entry.setObjectType(ObjectType.COLLECTION);
			entry.setCount(count++);
			entry.setLastResult(true);
			entry.setCreatedAt(new Date());
			entry.setModifiedAt(new Date());
			entries.add(entry);
		}

		return entries;

	}

	private CollectionAndDataObjectListingEntry createStandInForHomeDir(final String path) {
		log.info("under a zone, put out home as an entry");
		CollectionAndDataObjectListingEntry entry = new CollectionAndDataObjectListingEntry();
		entry.setCount(0);
		entry.setLastResult(true);
		entry.setObjectType(ObjectType.COLLECTION);

		entry.setOwnerZone(irodsAccount.getZone());
		entry.setParentPath(path);
		StringBuilder sb = new StringBuilder();
		sb.append(path);
		sb.append("/home");
		entry.setPathOrName(sb.toString());
		entry.setCreatedAt(new Date());
		entry.setModifiedAt(new Date());
		entry.setSpecColType(SpecColType.NORMAL);
		entry.setLastResult(true);

		return entry;
	}

	private CollectionAndDataObjectListingEntry createStandInForPublicDir(final ObjStat objStat) {
		log.info("under root, put out home as an entry");
		CollectionAndDataObjectListingEntry entry = new CollectionAndDataObjectListingEntry();
		entry.setCount(0);
		entry.setLastResult(true);
		entry.setOwnerZone(irodsAccount.getZone());
		entry.setOwnerName(objStat.getOwnerName());
		entry.setPathOrName(objStat.getAbsolutePath());
		entry.setParentPath(objStat.getCollectionPath());
		entry.setSpecColType(objStat.getSpecColType());
		entry.setCreatedAt(new Date());
		entry.setModifiedAt(new Date());
		entry.setId(objStat.getDataId());
		entry.setObjectType(objStat.getObjectType());
		return entry;
	}

	/**
	 * Create a collection and listing entry for the home dir
	 *
	 * @return
	 */
	private CollectionAndDataObjectListingEntry createStandInForUserDir(final ObjStat objStat) {
		log.info("put a user dir entry out there");
		CollectionAndDataObjectListingEntry entry = new CollectionAndDataObjectListingEntry();
		entry.setCount(0);
		entry.setLastResult(true);
		entry.setObjectType(ObjectType.COLLECTION);

		entry.setOwnerZone(irodsAccount.getZone());
		entry.setPathOrName(objStat.getAbsolutePath());
		entry.setParentPath(objStat.getCollectionPath());
		entry.setSpecColType(objStat.getSpecColType());
		entry.setCreatedAt(new Date());
		entry.setModifiedAt(new Date());
		entry.setId(objStat.getDataId());
		entry.setObjectType(objStat.getObjectType());
		entry.setOwnerZone(irodsAccount.getZone());
		entry.setOwnerName(objStat.getOwnerName());
		return entry;
	}

	/**
	 * List the collections underneath the given path
	 * <p>
	 * Works with soft links
	 *
	 * @param objStat           {@link ObjStat} from iRODS that details the nature
	 *                          of the collection
	 * @param partialStartIndex {@code int} with an offset
	 * @return {@code List} of {@link CollectionAndDataObjectListingEntry}
	 * @throws FileNotFoundException {@link FileNotFoundException}
	 * @throws JargonException       {@link JargonException}
	 *
	 *
	 */
	List<CollectionAndDataObjectListingEntry> listCollectionsUnderPath(final ObjStat objStat,
			final int partialStartIndex) throws FileNotFoundException, JargonException {

		log.info("listCollectionsUnderPath()");

		if (objStat == null) {
			throw new IllegalArgumentException("objStat is null");
		}

		/*
		 * See if jargon supports the given object type
		 */
		MiscIRODSUtils.evaluateSpecCollSupport(objStat);

		if (objStat.isStandInGeneratedObjStat()) {
			log.info("this objStat was heuristically generated, create stand-in subdirs if needed");
			return handleNoListingUnderRootOrHome(objStat.getAbsolutePath());
		}

		/*
		 * Special collections are processed in different ways.
		 *
		 * Listing for soft links substitutes the source path for the target path in the
		 * query
		 */
		String effectiveAbsolutePath = MiscIRODSUtils.determineAbsolutePathBasedOnCollTypeInObjectStat(objStat);

		if (objStat.getSpecColType() == SpecColType.STRUCT_FILE_COLL
				|| objStat.getSpecColType() == SpecColType.MOUNTED_COLL) {
			return listUnderPathWhenSpecColl(objStat, effectiveAbsolutePath, true, partialStartIndex);
		} else {
			return listCollectionsUnderPathViaGenQuery(objStat, partialStartIndex, effectiveAbsolutePath);
		}

	}

	private List<CollectionAndDataObjectListingEntry> listUnderPathWhenSpecColl(final ObjStat objStat,
			final String effectiveAbsolutePath, final boolean isCollection, final long offset) throws JargonException {

		log.info("listCollectionsUnderPathWhenSpecColl()");

		List<CollectionAndDataObjectListingEntry> entries = new ArrayList<>();

		SpecColInfo specColInfo = new SpecColInfo();
		specColInfo.setCacheDir(objStat.getCacheDir());

		if (objStat.isCacheDirty()) {
			specColInfo.setCacheDirty(1);
		}

		specColInfo.setCollClass(1);
		specColInfo.setCollection(objStat.getAbsolutePath());
		specColInfo.setObjPath(objStat.getObjectPath());
		specColInfo.setPhyPath(objStat.getObjectPath());
		specColInfo.setReplNum(objStat.getReplNumber());
		specColInfo.setType(2);

		if (irodsAccessObjectFactory.getIRODSServerProperties(irodsAccount).isAtLeastIrods410()) {
			specColInfo.setUseResourceHierarchy(true);
		}

		DataObjInpForQuerySpecColl dataObjInp = null;

		if (isCollection) {
			dataObjInp = DataObjInpForQuerySpecColl.instanceQueryCollectionsWithOffset(effectiveAbsolutePath,
					specColInfo, offset);
		} else {
			dataObjInp = DataObjInpForQuerySpecColl.instanceQueryDataObjWithOffset(effectiveAbsolutePath, specColInfo,
					offset);
		}
		Tag response;

		try {
			response = irodsAccessObjectFactory.getIrodsSession().currentConnection(irodsAccount)
					.irodsFunction(dataObjInp);

			log.debug("response from function: {}", response.parseTag());

			int totalRecords = response.getTag("totalRowCount").getIntValue();
			log.info("total records:{}", totalRecords);
			int continueInx = response.getTag("continueInx").getIntValue();

			List<IRODSQueryResultRow> results = QueryResultProcessingUtils.translateResponseIntoResultSet(response,
					new ArrayList<String>(), 0, 0);

			int ctr = (int) offset + 1;
			CollectionAndDataObjectListingEntry entry = null;
			for (IRODSQueryResultRow row : results) {
				entry = createListingEntryFromResultRow(objStat, row, isCollection);
				entry.setCount(ctr++);
				entry.setLastResult(continueInx <= 0);
				entries.add(entry);
			}

			while (continueInx > 0) {
				dataObjInp = DataObjInpForQuerySpecColl.instanceQueryCollections(effectiveAbsolutePath, specColInfo,
						continueInx);

				if (isCollection) {
					dataObjInp = DataObjInpForQuerySpecColl.instanceQueryCollections(effectiveAbsolutePath, specColInfo,
							continueInx);
				} else {
					dataObjInp = DataObjInpForQuerySpecColl.instanceQueryDataObj(effectiveAbsolutePath, specColInfo,
							continueInx);
				}

				response = irodsAccessObjectFactory.getIrodsSession().currentConnection(irodsAccount)
						.irodsFunction(dataObjInp);

				log.debug("response from function: {}", response.parseTag());

				totalRecords = response.getTag("totalRowCount").getIntValue();
				log.info("total records:{}", totalRecords);
				continueInx = response.getTag("continueInx").getIntValue();

				results = QueryResultProcessingUtils.translateResponseIntoResultSet(response, new ArrayList<String>(),
						0, entries.size());
				for (IRODSQueryResultRow row : results) {
					entry = createListingEntryFromResultRow(objStat, row, isCollection);
					entry.setCount(ctr++);
					entry.setLastResult(continueInx <= 0);
					entries.add(entry);
				}
			}

			return entries;

		} catch (FileDriverError fde) {
			log.warn("file driver error listing empty spec coll is ignored, just act as no data found");
			return new ArrayList<>();
		} catch (DataNotFoundException dnf) {
			log.info("end of data");
		}

		return entries;

	}

	private CollectionAndDataObjectListingEntry createListingEntryFromResultRow(final ObjStat objStat,
			final IRODSQueryResultRow row, final boolean isCollection) throws JargonException {
		CollectionAndDataObjectListingEntry listingEntry;
		CollectionAndPath collectionAndPath;
		listingEntry = new CollectionAndDataObjectListingEntry();
		listingEntry.setCreatedAt(IRODSDataConversionUtil.getDateFromIRODSValue(row.getColumn(2)));
		listingEntry.setDataSize(IRODSDataConversionUtil.getLongOrZeroFromIRODSValue(row.getColumn(4)));
		listingEntry.setModifiedAt(IRODSDataConversionUtil.getDateFromIRODSValue(row.getColumn(3)));
		row.getColumn(1);

		if (isCollection) {
			listingEntry.setObjectType(ObjectType.COLLECTION);
		} else {
			listingEntry.setObjectType(ObjectType.DATA_OBJECT);
		}

		listingEntry.setOwnerName(objStat.getOwnerName());
		listingEntry.setOwnerZone(objStat.getOwnerZone());

		if (isCollection) {

			collectionAndPath = MiscIRODSUtils.separateCollectionAndPathFromGivenAbsolutePath(row.getColumn(0));
			listingEntry.setParentPath(collectionAndPath.getCollectionParent());
			listingEntry.setPathOrName(row.getColumn(0));

		} else {
			listingEntry.setParentPath(row.getColumn(0));
			listingEntry.setPathOrName(row.getColumn(1));
		}
		listingEntry.setSpecColType(objStat.getSpecColType());
		return listingEntry;
	}

	private List<CollectionAndDataObjectListingEntry> listCollectionsUnderPathViaGenQuery(final ObjStat objStat,
			final int partialStartIndex, final String effectiveAbsolutePath) throws JargonException {

		List<CollectionAndDataObjectListingEntry> subdirs;

		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, false, true, null);
		try {
			IRODSFileSystemAOHelper.buildQueryListAllCollections(effectiveAbsolutePath, builder);
		} catch (GenQueryBuilderException e) {
			log.error("query builder exception", e);
			throw new JargonException("error building query", e);
		}

		IRODSQueryResultSet resultSet = queryForPathAndReturnResultSet(objStat.getAbsolutePath(), builder,
				partialStartIndex, objStat);

		subdirs = new ArrayList<>(resultSet.getResults().size());
		CollectionAndDataObjectListingEntry collectionAndDataObjectListingEntry = null;

		for (IRODSQueryResultRow row : resultSet.getResults()) {
			collectionAndDataObjectListingEntry = CollectionAOHelper
					.buildCollectionListEntryFromResultSetRowForCollectionQuery(row, resultSet.getTotalRecords());

			adjustEntryFromRowInCaseOfSpecialCollection(objStat, effectiveAbsolutePath,
					collectionAndDataObjectListingEntry);

			/*
			 * for some reason, a query for collections with a parent of '/' returns the
			 * root as a result, which creates weird situations when trying to show
			 * collections in a tree structure. This test papers over that idiosyncrasy and
			 * discards that extraneous result.
			 */
			if (!collectionAndDataObjectListingEntry.getPathOrName().equals("/")) {
				subdirs.add(collectionAndDataObjectListingEntry);
			}
		}

		return subdirs;
	}

	IRODSQueryResultSet queryForPathAndReturnResultSet(final String absolutePath, final IRODSGenQueryBuilder builder,
			final int partialStartIndex, final ObjStat objStat) throws JargonException {

		log.info("queryForPathAndReturnResultSet for: {}", absolutePath);
		IRODSGenQueryExecutor irodsGenQueryExecutor = irodsAccessObjectFactory.getIRODSGenQueryExecutor(irodsAccount);

		IRODSGenQueryFromBuilder irodsQuery;
		IRODSQueryResultSet resultSet;

		try {
			irodsQuery = builder.exportIRODSQueryFromBuilder(
					irodsAccessObjectFactory.getJargonProperties().getMaxFilesAndDirsQueryMax());
			resultSet = irodsGenQueryExecutor.executeIRODSQueryWithPagingInZone(irodsQuery, partialStartIndex,
					MiscIRODSUtils.getZoneInPath(absolutePath));
		} catch (JargonQueryException e) {
			log.error(QUERY_EXCEPTION_FOR_QUERY, e);
			throw new JargonException(e);
		} catch (GenQueryBuilderException e) {
			log.error(QUERY_EXCEPTION_FOR_QUERY, e);
			throw new JargonException(e);
		}

		return resultSet;
	}

	private void adjustEntryFromRowInCaseOfSpecialCollection(final ObjStat objStat, final String effectiveAbsolutePath,
			final CollectionAndDataObjectListingEntry collectionAndDataObjectListingEntry) {
		if (objStat.getSpecColType() == SpecColType.LINKED_COLL) {
			log.info("adjusting paths in entry to reflect linked collection info");
			StringBuilder sb = new StringBuilder();
			sb.append(objStat.getObjectPath());
			sb.append('/');
			sb.append(MiscIRODSUtils
					.getLastPathComponentForGivenAbsolutePath(collectionAndDataObjectListingEntry.getPathOrName()));
			collectionAndDataObjectListingEntry.setSpecialObjectPath(sb.toString());

			sb = new StringBuilder();
			sb.append(objStat.getAbsolutePath());
			sb.append('/');
			sb.append(MiscIRODSUtils
					.getLastPathComponentForGivenAbsolutePath(collectionAndDataObjectListingEntry.getPathOrName()));

			collectionAndDataObjectListingEntry.setPathOrName(sb.toString());

			collectionAndDataObjectListingEntry.setParentPath(objStat.getAbsolutePath());
			collectionAndDataObjectListingEntry.setSpecColType(SpecColType.LINKED_COLL);
		}
	}

	/**
	 * List the data objects underneath the given path given an already obtained
	 * {@code ObjStat}
	 *
	 * @param objStat           {@link ObjStat} describing the collection
	 * @param partialStartIndex {@code int} wiht an offset
	 * @return {@code List} of {@link CollectionAndDataObjectListingEntry}
	 * @throws JargonException {@link JargonException}
	 */
	List<CollectionAndDataObjectListingEntry> listDataObjectsUnderPath(final ObjStat objStat,
			final int partialStartIndex) throws JargonException {

		log.info("listDataObjectsUnderPath(objStat, partialStartIndex)");

		if (objStat == null) {
			throw new IllegalArgumentException("collectionAndDataObjectListingEntry is null");
		}

		String effectiveAbsolutePath = MiscIRODSUtils.determineAbsolutePathBasedOnCollTypeInObjectStat(objStat);
		log.info("determined effectiveAbsolutePathToBe:{}", effectiveAbsolutePath);

		log.info("listDataObjectsUnderPath for: {}", objStat);

		List<CollectionAndDataObjectListingEntry> files;
		if (objStat.getSpecColType() == SpecColType.STRUCT_FILE_COLL
				|| objStat.getSpecColType() == SpecColType.MOUNTED_COLL) {

			files = listUnderPathWhenSpecColl(objStat, effectiveAbsolutePath, false, partialStartIndex);
		} else {

			files = listDataObjectsUnderPathViaGenQuery(objStat, partialStartIndex, effectiveAbsolutePath);
		}

		return files;

	}

	private List<CollectionAndDataObjectListingEntry> listDataObjectsUnderPathViaGenQuery(final ObjStat objStat,
			final int partialStartIndex, final String effectiveAbsolutePath) throws JargonException {
		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, false, true, null);

		IRODSFileSystemAOHelper.buildQueryListAllDataObjectsWithSizeAndDateInfo(effectiveAbsolutePath, builder);
		IRODSQueryResultSet resultSet;

		try {
			resultSet = queryForPathAndReturnResultSet(effectiveAbsolutePath, builder, partialStartIndex, objStat);
		} catch (JargonException e) {
			log.error("exception querying for data objects:{}", builder, e);
			throw new JargonException("error in query", e);
		}

		List<CollectionAndDataObjectListingEntry> files = new ArrayList<>(resultSet.getResults().size());

		/*
		 * the query that gives the necessary data will cause duplication when there are
		 * replicas, so discard duplicates. This is the nature of GenQuery.
		 */
		String lastPath = "";
		String currentPath = "";
		CollectionAndDataObjectListingEntry entry;
		for (IRODSQueryResultRow row : resultSet.getResults()) {
			entry = CollectionAOHelper.buildCollectionListEntryFromResultSetRowForDataObjectQuery(row,
					resultSet.getTotalRecords());

			/**
			 * Use the data in the objStat, in the case of special collections, to augment
			 * the data returned
			 */
			augmentCollectionEntryForSpecialCollections(objStat, effectiveAbsolutePath, entry);

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
	 * Use the data in the objStat, in the case of special collections, to augment
	 * the entry for a collection
	 *
	 * @param objStat               {@link ObjStat} retreived for the parent
	 *                              directory
	 * @param effectiveAbsolutePath {@code String} with the path used to query, this
	 *                              will be the canonical path for the parent
	 *                              collection, and should correspond to the
	 *                              absolute path information in the given
	 *                              {@code entry}.
	 * @param entry                 {@link CollectionAndDataObjectListingEntry}
	 *                              which is the raw data returned from querying the
	 *                              iCat based on the {@code effectiveAbsolutePath}.
	 *                              This information is from the perspective of the
	 *                              canonical path, and the given method will
	 *                              reframe the {@code entry} from the perspective
	 *                              of the requested path This means that a query on
	 *                              children of a soft link carry the data from the
	 *                              perspective of the soft linked directory, even
	 *                              though the iCAT carries the information based on
	 *                              the 'source path' of the soft link. This gets
	 *                              pretty confusing otherwise.
	 */
	void augmentCollectionEntryForSpecialCollections(final ObjStat objStat, final String effectiveAbsolutePath,
			final CollectionAndDataObjectListingEntry entry) {

		if (objStat.getSpecColType() == SpecColType.LINKED_COLL) {
			log.info("adjusting paths in entry to reflect linked collection info");
			entry.setSpecialObjectPath(objStat.getObjectPath());
			CollectionAndPath collectionAndPathForAbsPath = MiscIRODSUtils
					.separateCollectionAndPathFromGivenAbsolutePath(entry.getPathOrName());

			if (entry.isCollection()) {
				entry.setPathOrName(objStat.getAbsolutePath() + "/" + collectionAndPathForAbsPath.getChildName());
				entry.setParentPath(objStat.getAbsolutePath());
			} else {
				entry.setParentPath(objStat.getAbsolutePath());
			}

		}

	}

	int countDataObjectsUnderPath(final ObjStat objStat) throws FileNotFoundException, JargonException {

		log.info("countDataObjectsUnderPath()");

		if (objStat == null) {
			throw new IllegalArgumentException("objStat is null");
		}

		/*
		 * See if jargon supports the given object type
		 */
		MiscIRODSUtils.evaluateSpecCollSupport(objStat);

		String effectiveAbsolutePath = MiscIRODSUtils.determineAbsolutePathBasedOnCollTypeInObjectStat(objStat);
		log.info("determined effectiveAbsolutePathToBe:{}", effectiveAbsolutePath);

		// I cannot get children if this is not a directory (a file has no
		// children)
		if (!objStat.isSomeTypeOfCollection()) {
			log.error("this is a file, not a directory, and therefore I cannot get a count of the children: {}",
					objStat.getAbsolutePath());
			throw new JargonException("attempting to count children under a file at path:" + objStat.getAbsolutePath());
		}

		return queryDataObjectCountsUnderPath(effectiveAbsolutePath);

	}

	long totalDataObjectSizesUnderPath(final ObjStat objStat) throws FileNotFoundException, JargonException {

		log.info("totalDataObjectsSizeUnderPath()");

		if (objStat == null) {
			throw new IllegalArgumentException("objStat is null");
		}

		/*
		 * See if jargon supports the given object type
		 */
		MiscIRODSUtils.evaluateSpecCollSupport(objStat);

		String effectiveAbsolutePath = MiscIRODSUtils.determineAbsolutePathBasedOnCollTypeInObjectStat(objStat);
		log.info("determined effectiveAbsolutePathToBe:{}", effectiveAbsolutePath);

		// I cannot get children if this is not a directory (a file has no
		// children)
		if (!objStat.isSomeTypeOfCollection()) {
			log.error("this is a file, not a directory: {}", objStat.getAbsolutePath());
			throw new JargonException("attempting to total children under a file at path:" + objStat.getAbsolutePath());
		}

		return queryDataObjectSizesUnderPath(effectiveAbsolutePath);

	}

	long queryDataObjectSizesUnderPath(final String effectiveAbsolutePath)
			throws JargonException, DataNotFoundException {
		IRODSGenQueryExecutor irodsGenQueryExecutor = irodsAccessObjectFactory.getIRODSGenQueryExecutor(irodsAccount);

		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);
		IRODSQueryResultSet resultSet;

		try {
			builder.addSelectAsAgregateGenQueryValue(RodsGenQueryEnum.COL_DATA_SIZE, SelectFieldTypes.SUM)
					.addConditionAsGenQueryField(RodsGenQueryEnum.COL_COLL_NAME, QueryConditionOperators.LIKE,
							effectiveAbsolutePath.trim() + "%")
					.addConditionAsGenQueryField(RodsGenQueryEnum.COL_DATA_REPL_NUM, QueryConditionOperators.EQUAL,
							"0");

			;
			IRODSGenQueryFromBuilder irodsQuery = builder.exportIRODSQueryFromBuilder(1);

			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResultInZone(irodsQuery, 0,
					MiscIRODSUtils.getZoneInPath(effectiveAbsolutePath));
		} catch (JargonQueryException e) {
			log.error(CollectionListingUtils.QUERY_EXCEPTION_FOR_QUERY, e);
			throw new JargonException("error in exists query", e);
		} catch (GenQueryBuilderException e) {
			log.error(CollectionListingUtils.QUERY_EXCEPTION_FOR_QUERY, e);
			throw new JargonException("error in exists query", e);
		}

		long fileCtr = 0;

		if (resultSet.getResults().size() > 0) {
			fileCtr = IRODSDataConversionUtil.getLongOrZeroFromIRODSValue(resultSet.getFirstResult().getColumn(0));
		}
		log.info("got total size of:{}", fileCtr);

		return fileCtr;
	}

	/**
	 * Given an objStat, get the count of collections under the path
	 *
	 * @param objStat {@link ObjStat}
	 * @return <code>int</code> with the total collections under a given path
	 * @throws FileNotFoundException {@link FileNotFoundException}
	 * @throws JargonException       {@link JargonException}
	 */
	int countCollectionsUnderPath(final ObjStat objStat) throws FileNotFoundException, JargonException {

		log.info("countCollectionsUnderPath()");

		if (objStat == null) {
			throw new IllegalArgumentException("objStat is null");
		}

		/*
		 * See if jargon supports the given object type
		 */
		MiscIRODSUtils.evaluateSpecCollSupport(objStat);

		String effectiveAbsolutePath = MiscIRODSUtils.determineAbsolutePathBasedOnCollTypeInObjectStat(objStat);
		log.info("determined effectiveAbsolutePathToBe:{}", effectiveAbsolutePath);

		// I cannot get children if this is not a directory (a file has no
		// children)
		if (!objStat.isSomeTypeOfCollection()) {
			log.error("this is a file, not a directory, and therefore I cannot get a count of the children: {}",
					objStat.getAbsolutePath());
			throw new JargonException("attempting to count children under a file at path:" + objStat);
		}

		IRODSGenQueryExecutor irodsGenQueryExecutor = irodsAccessObjectFactory.getIRODSGenQueryExecutor(irodsAccount);

		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);
		IRODSQueryResultSet resultSet;

		try {
			builder.addSelectAsAgregateGenQueryValue(RodsGenQueryEnum.COL_COLL_NAME, SelectFieldTypes.COUNT)
					.addConditionAsGenQueryField(RodsGenQueryEnum.COL_COLL_PARENT_NAME, QueryConditionOperators.EQUAL,
							effectiveAbsolutePath);

			IRODSGenQueryFromBuilder irodsQuery = builder.exportIRODSQueryFromBuilder(1);
			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResultInZone(irodsQuery, 0,
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
			collCtr = IRODSDataConversionUtil.getIntOrZeroFromIRODSValue(resultSet.getFirstResult().getColumn(0));
		}

		return collCtr;

	}

	int queryDataObjectCountsUnderPath(final String effectiveAbsolutePath)
			throws JargonException, DataNotFoundException {
		IRODSGenQueryExecutor irodsGenQueryExecutor = irodsAccessObjectFactory.getIRODSGenQueryExecutor(irodsAccount);

		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);
		IRODSQueryResultSet resultSet;

		try {
			builder.addSelectAsAgregateGenQueryValue(RodsGenQueryEnum.COL_COLL_NAME, SelectFieldTypes.COUNT)
					.addSelectAsAgregateGenQueryValue(RodsGenQueryEnum.COL_DATA_NAME, SelectFieldTypes.COUNT)
					.addConditionAsGenQueryField(RodsGenQueryEnum.COL_COLL_NAME, QueryConditionOperators.EQUAL,
							effectiveAbsolutePath)
					.addConditionAsGenQueryField(RodsGenQueryEnum.COL_DATA_REPL_NUM, QueryConditionOperators.EQUAL,
							"0");

			;
			IRODSGenQueryFromBuilder irodsQuery = builder.exportIRODSQueryFromBuilder(1);

			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResultInZone(irodsQuery, 0,
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
			fileCtr = IRODSDataConversionUtil.getIntOrZeroFromIRODSValue(resultSet.getFirstResult().getColumn(0));
		}

		return fileCtr;
	}

	/**
	 * @param irodsAccount             {@link IRODSAccount}
	 * @param irodsAccessObjectFactory {@link IRODSAccessObjectFactory}
	 * @throws JargonException {@link JargonException}
	 */
	CollectionListingUtils(final IRODSAccount irodsAccount, final IRODSAccessObjectFactory irodsAccessObjectFactory)
			throws JargonException {
		super();

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		if (irodsAccessObjectFactory == null) {
			throw new IllegalArgumentException("null irodsAccessObjectFactory");
		}

		this.irodsAccount = irodsAccount;
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;

	}

	/**
	 * Retrieve an iRODS ObjStat object for the given iRODS path
	 *
	 * @param irodsAbsolutePath <code>String</code> with an absolute path to an
	 *                          irods object
	 * @return {@link ObjStat} from iRODS
	 * @throws FileNotFoundException if the file does not exist
	 * @throws JargonException       {@link JargonException}
	 */
	public ObjStat retrieveObjectStatForPath(final String irodsAbsolutePath)
			throws FileNotFoundException, JargonException {

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("irodsAbsolutePath is null or empty");
		}

		String myPath = MiscIRODSUtils.normalizeIrodsPath(irodsAbsolutePath);

		DataObjInpForObjStat dataObjInp = DataObjInpForObjStat.instance(myPath);
		Tag response;
		ObjStat objStat;
		try {
			response = irodsAccessObjectFactory.getIrodsSession().currentConnection(irodsAccount)
					.irodsFunction(dataObjInp);
		} catch (FileNotFoundException e) {
			log.info("got a file not found, try to heuristically produce an objstat");
			return handleNoObjStatUnderRootOrHomeByLookingForPublicAndHome(myPath);
		}

		log.debug("response from objStat: {}", response.parseTag());

		/**
		 * For spec cols - soft link - phyPath = parent canonical dir -objPath =
		 * canonical path
		 */
		objStat = new ObjStat();
		objStat.setAbsolutePath(myPath);
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
		 * Look for the specColl tag (it is expected to be there) and see if there are
		 * any special collection types (e.g. mounted or soft links) to deal with
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
				objStat.setObjectPath(specColl.getTag("phyPath").getStringValue());
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
				 * physical path will hold the canonical source dir where it was linked. The
				 * collection path will hold the top level of the soft link target. This does
				 * not 'follow' by incrementing the path as you descend into subdirs, so I use
				 * the collection path to chop off the absolute path, and use the remainder
				 * appended to the collection path to arrive at equivalent canonical source path
				 * fo rthis soft linked directory. This is all rather confusing, so instead of
				 * worrying about it, Jargon has the headache, you can just trust the objStat
				 * objectPath to point to the equivalent canonical source path to the soft
				 * linked path.
				 */
				String canonicalSourceDirForSoftLink = specColl.getTag("phyPath").getStringValue();
				String softLinkTargetDir = specColl.getTag("collection").getStringValue();
				if (softLinkTargetDir.length() > objStat.getAbsolutePath().length()) {
					throw new JargonException("cannot properly compute path for soft link");
				}

				String additionalPath = objStat.getAbsolutePath().substring(softLinkTargetDir.length());
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
		objStat.setCreatedAt(IRODSDataConversionUtil.getDateFromIRODSValue(createdDate));
		objStat.setModifiedAt(IRODSDataConversionUtil.getDateFromIRODSValue(modifiedDate));

		log.info(objStat.toString());
		return objStat;

	}

}
