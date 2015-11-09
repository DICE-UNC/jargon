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
import org.irods.jargon.core.query.IRODSGenQueryBuilder;
import org.irods.jargon.core.query.IRODSGenQueryFromBuilder;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSet;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.QueryResultProcessingUtils;
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

	private final CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO;
	public static final String QUERY_EXCEPTION_FOR_QUERY = "query exception for  query:";

	public static final Logger log = LoggerFactory
			.getLogger(CollectionIteratorAOImpl.class);

	/**
	 * Convenience method to get the access object factory
	 *
	 * @return
	 * @throws JargonException
	 */
	IRODSAccessObjectFactory getIrodsAccessObjectFactory()
			throws JargonException {
		return collectionAndDataObjectListAndSearchAO
				.getIRODSAccessObjectFactory();
	}

	/**
	 * Convenience method to get irodsAccount
	 *
	 * @return
	 * @throws JargonException
	 */
	IRODSAccount getIrodsAccount() throws JargonException {
		return collectionAndDataObjectListAndSearchAO.getIRODSAccount();
	}

	/**
	 *
	 */
	CollectionListingUtils(
			final CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO) {
		if (collectionAndDataObjectListAndSearchAO == null) {
			throw new IllegalArgumentException(
					"null collectionAndDataObjectListAndSearchAO");
		}

		this.collectionAndDataObjectListAndSearchAO = collectionAndDataObjectListAndSearchAO;
	}

	/**
	 * This is a compensating method used to deal with the top of the tree when
	 * permissions do not allow listing 'into' the tree to get to things the
	 * user actually has access to.
	 * <p/>
	 * Phase 1 - when path is / - obtain a listing of zones and display as
	 * subdirectories
	 * <p/>
	 * Phase 2 when path is /zone - interpolate a 'home' directory
	 * <p/>
	 * Phase 3 when path is /zone/home - for current zone - add a dir for the
	 * user name, see if a public dir. For foreign zone, add a dir for
	 * user#homeZone and see if a public dir
	 *
	 *
	 *
	 * @param absolutePathToParent
	 *            <code>String</code> with the current parent
	 * @return <code>List</code> of {@link CollectionAndDataObjectListingEntry}
	 *         that has the children under the parent. These children may be
	 *         simulated per the given rules
	 * @throws JargonException
	 */
	List<CollectionAndDataObjectListingEntry> handleNoListingUnderRootOrHomeByLookingForPublicAndHome(
			final String absolutePathToParent) throws FileNotFoundException,
			JargonException {

		log.info("handleNoListingUnderRootOrHomeByLookingForPublicAndHome()");

		String path = absolutePathToParent;

		List<CollectionAndDataObjectListingEntry> collectionAndDataObjectListingEntries = new ArrayList<CollectionAndDataObjectListingEntry>();

		/*
		 * Do I have compensating actions configured? If not, it's just a file
		 * not found
		 */
		if (!collectionAndDataObjectListAndSearchAO.getJargonProperties()
				.isDefaultToPublicIfNothingUnderRootWhenListing()) {
			log.info("not configured in jargon.properties to look for public and user home, throw the FileNotFoundException");
			throw new FileNotFoundException("the collection cannot be found");
		}

		/*
		 * Phase1 - under root
		 *
		 * Get a list of zones as subdirs
		 */

		if (path.equals("/")) {
			log.info("phase1 - under root, add zones");
			collectionAndDataObjectListingEntries
			.addAll(createStandInForZoneDir());
			return collectionAndDataObjectListingEntries;
		}

		/*
		 * Phase2 - under a zone, add a home
		 */

		List<String> components = MiscIRODSUtils
				.breakIRODSPathIntoComponents(path);
		if (components.size() == 2) {
			log.info("under zone, add a home");
			log.info("assume this is a zone name, look for a home dir under the zone name");
			collectionAndDataObjectListingEntries
			.add(createStandInForHomeDir(path));
			return collectionAndDataObjectListingEntries;
		}

		/*
		 * Phase 3, under home, see if home zone
		 */
		components = MiscIRODSUtils.breakIRODSPathIntoComponents(path);
		if (components.size() == 3 && components.get(2).equals("home")) {
			log.info("under home, see if same zone as login");
			if (collectionAndDataObjectListAndSearchAO.getIRODSAccount()
					.getZone().equals(components.get(1))) {
				log.info("under logged in zone, add user and public dirs");
				collectionAndDataObjectListingEntries
				.addAll(createStandInsUnderHomeInLoggedInZone(path));
			} else {
				log.info("under federated zone, add federated user");
				collectionAndDataObjectListingEntries
				.addAll(createStandInsUnderHomeInFederatedZone(components
						.get(1)));
			}
			return collectionAndDataObjectListingEntries;
		}

		/*
		 * Fall through is a legit file not found exception
		 */

		log.info("really is a not found for file:{}", path);
		throw new FileNotFoundException("unable to find file under path");

	}

	/**
	 * Heuristic processing allows ObjStats to be returned (though fake) at
	 * points in the hierarchy were strict ACLs would otherwise preclude
	 *
	 * @param irodsAbsolutePath
	 * @return
	 * @throws FileNotFoundException
	 * @throws JargonException
	 */
	ObjStat handleNoObjStatUnderRootOrHomeByLookingForPublicAndHome(
			final String irodsAbsolutePath) throws FileNotFoundException,
			JargonException {

		log.info("handleNoObjStatUnderRootOrHomeByLookingForPublicAndHome()");
		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePath");
		}

		ObjStat objStat = null;

		/*
		 * Do I have compensating actions configured? If not, it's just a file
		 * not found
		 */
		if (!collectionAndDataObjectListAndSearchAO.getJargonProperties()
				.isDefaultToPublicIfNothingUnderRootWhenListing()) {
			log.info("not configured in jargon.properties to look for public and user home, throw the FileNotFoundException");
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
			objStat.setObjectType(ObjectType.COLLECTION);
			objStat.setSpecColType(SpecColType.NORMAL);
			objStat.setStandInGeneratedObjStat(true);
			objStat.setModifiedAt(new Date());
			objStat.setCreatedAt(new Date());
			log.info("return a fake objStat for root:{}", objStat);
			return objStat;
		}

		List<String> components = MiscIRODSUtils
				.breakIRODSPathIntoComponents(irodsAbsolutePath);

		/*
		 * Phase2 - first path under root should be a zone
		 */
		if (components.size() == 2) {
			ZoneAO zoneAO = getIrodsAccessObjectFactory().getZoneAO(
					getIrodsAccount());
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
				log.error("not a valid zone, cannot interpolate this path:{}",
						irodsAbsolutePath);
				throw new FileNotFoundException("path does not exist");
			}
			objStat = new ObjStat();
			objStat.setAbsolutePath(irodsAbsolutePath);
			objStat.setObjectType(ObjectType.COLLECTION);
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
				objStat.setObjectType(ObjectType.COLLECTION);
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

	private List<CollectionAndDataObjectListingEntry> createStandInsUnderHomeInLoggedInZone(
			final String path) throws FileNotFoundException, JargonException {
		List<CollectionAndDataObjectListingEntry> collectionAndDataObjectListingEntries = new ArrayList<CollectionAndDataObjectListingEntry>();
		// if same zone, look for public and home, if cross zone, look for a
		// user dir in zone and public
		log.info("under home, look for public and user dir");
		StringBuilder sb = new StringBuilder(path);
		sb.append("/public");
		ObjStat statForPublic;
		try {
			statForPublic = collectionAndDataObjectListAndSearchAO
					.retrieveObjectStatForPath(sb.toString());
			collectionAndDataObjectListingEntries
			.add(createStandInForPublicDir(statForPublic));
		} catch (FileNotFoundException fnf) {
			log.info("no public dir");
		}

		log.info("see if a user home dir applies");

		ObjStat statForUserHome;
		try {
			statForUserHome = collectionAndDataObjectListAndSearchAO
					.retrieveObjectStatForPath(MiscIRODSUtils
							.computeHomeDirectoryForIRODSAccount(collectionAndDataObjectListAndSearchAO
									.getIRODSAccount()));
			collectionAndDataObjectListingEntries
			.add(createStandInForUserDir(statForUserHome));
		} catch (FileNotFoundException fnf) {
			log.info("no home dir");
		}
		return collectionAndDataObjectListingEntries;
	}

	private List<CollectionAndDataObjectListingEntry> createStandInsUnderHomeInFederatedZone(
			final String zone) throws FileNotFoundException, JargonException {
		List<CollectionAndDataObjectListingEntry> collectionAndDataObjectListingEntries = new ArrayList<CollectionAndDataObjectListingEntry>();
		// if same zone, look for public and home, if cross zone, look for a
		// user dir in zone and public
		log.info("under home in federated zone, look for public and home dir");

		StringBuilder sb = new StringBuilder();
		sb.append("/");
		sb.append(zone);
		sb.append("/home/public");

		try {
			ObjStat statForPublic = collectionAndDataObjectListAndSearchAO
					.retrieveObjectStatForPath(sb.toString());
			collectionAndDataObjectListingEntries
			.add(createStandInForPublicDir(statForPublic));
		} catch (FileNotFoundException fnf) {
			log.info("no public dir");
		}

		log.info("see if a user home dir applies");

		try {
			ObjStat homeStat = collectionAndDataObjectListAndSearchAO
					.retrieveObjectStatForPath(MiscIRODSUtils
							.computeHomeDirectoryForIRODSAccountInFederatedZone(
									collectionAndDataObjectListAndSearchAO
									.getIRODSAccount(), zone));
			collectionAndDataObjectListingEntries
			.add(createStandInForUserDir(homeStat));
		} catch (FileNotFoundException fnf) {
			log.info("no user dir");
		}
		return collectionAndDataObjectListingEntries;
	}

	private List<CollectionAndDataObjectListingEntry> createStandInForZoneDir()
			throws FileNotFoundException, JargonException {
		log.info("under root, put out zone as an entry");
		CollectionAndDataObjectListingEntry entry;
		List<CollectionAndDataObjectListingEntry> entries = new ArrayList<CollectionAndDataObjectListingEntry>();
		StringBuilder sb;
		ZoneAO zoneAO = collectionAndDataObjectListAndSearchAO
				.getIRODSAccessObjectFactory().getZoneAO(
						collectionAndDataObjectListAndSearchAO
						.getIRODSAccount());
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
			entries.add(entry);
		}

		return entries;

	}

	/**
	 * @param collectionAndDataObjectListingEntries
	 */
	private CollectionAndDataObjectListingEntry createStandInForHomeDir(
			final String path) {
		log.info("under a zone, put out home as an entry");
		CollectionAndDataObjectListingEntry entry = new CollectionAndDataObjectListingEntry();
		entry.setCount(0);
		entry.setLastResult(true);
		entry.setObjectType(ObjectType.COLLECTION);
		entry.setOwnerZone(collectionAndDataObjectListAndSearchAO
				.getIRODSAccount().getZone());
		entry.setParentPath(path);
		StringBuilder sb = new StringBuilder();
		sb.append(path);
		sb.append("/home");
		entry.setPathOrName(sb.toString());
		entry.setSpecColType(SpecColType.NORMAL);
		entry.setLastResult(true);

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
		entry.setOwnerZone(collectionAndDataObjectListAndSearchAO
				.getIRODSAccount().getZone());
		entry.setOwnerName(objStat.getOwnerName());
		entry.setPathOrName(objStat.getAbsolutePath());
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
		entry.setOwnerZone(collectionAndDataObjectListAndSearchAO
				.getIRODSAccount().getZone());
		entry.setPathOrName(objStat.getAbsolutePath());
		entry.setSpecColType(objStat.getSpecColType());
		entry.setCreatedAt(objStat.getCreatedAt());
		entry.setId(objStat.getDataId());
		entry.setObjectType(objStat.getObjectType());
		entry.setOwnerZone(collectionAndDataObjectListAndSearchAO
				.getIRODSAccount().getZone());
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
	List<CollectionAndDataObjectListingEntry> listCollectionsUnderPath(
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

		if (objStat.getSpecColType() == SpecColType.STRUCT_FILE_COLL
				|| objStat.getSpecColType() == SpecColType.MOUNTED_COLL) {
			return listUnderPathWhenSpecColl(objStat, effectiveAbsolutePath,
					true);
		} else {
			return listCollectionsUnderPathViaGenQuery(objStat,
					partialStartIndex, effectiveAbsolutePath);
		}

	}

	private List<CollectionAndDataObjectListingEntry> listUnderPathWhenSpecColl(
			final ObjStat objStat, final String effectiveAbsolutePath,
			final boolean isCollection) throws JargonException {

		log.info("listCollectionsUnderPathWhenSpecColl()");

		List<CollectionAndDataObjectListingEntry> entries = new ArrayList<CollectionAndDataObjectListingEntry>();

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

		if (collectionAndDataObjectListAndSearchAO.getIRODSServerProperties()
				.isEirods()) {
			specColInfo.setUseResourceHierarchy(true);
		}

		DataObjInpForQuerySpecColl dataObjInp = null;

		if (isCollection) {
			dataObjInp = DataObjInpForQuerySpecColl.instanceQueryCollections(
					effectiveAbsolutePath, specColInfo);
		} else {
			dataObjInp = DataObjInpForQuerySpecColl.instanceQueryDataObj(
					effectiveAbsolutePath, specColInfo);
		}
		Tag response;

		try {
			response = collectionAndDataObjectListAndSearchAO
					.getIRODSProtocol().irodsFunction(dataObjInp);

			log.debug("response from function: {}", response.parseTag());

			int totalRecords = response.getTag("totalRowCount").getIntValue();
			log.info("total records:{}", totalRecords);
			int continueInx = response.getTag("continueInx").getIntValue();

			List<IRODSQueryResultRow> results = QueryResultProcessingUtils
					.translateResponseIntoResultSet(response,
							new ArrayList<String>(), 0, 0);

			int ctr = 1;
			CollectionAndDataObjectListingEntry entry = null;
			for (IRODSQueryResultRow row : results) {
				entry = createListingEntryFromResultRow(objStat, row,
						isCollection);
				entry.setCount(ctr++);
				entry.setLastResult(continueInx <= 0);
				entries.add(entry);
			}

			while (continueInx > 0) {
				dataObjInp = DataObjInpForQuerySpecColl
						.instanceQueryCollections(effectiveAbsolutePath,
								specColInfo, continueInx);

				if (isCollection) {
					dataObjInp = DataObjInpForQuerySpecColl
							.instanceQueryCollections(effectiveAbsolutePath,
									specColInfo, continueInx);
				} else {
					dataObjInp = DataObjInpForQuerySpecColl
							.instanceQueryDataObj(effectiveAbsolutePath,
									specColInfo, continueInx);
				}

				response = collectionAndDataObjectListAndSearchAO
						.getIRODSProtocol().irodsFunction(dataObjInp);

				log.debug("response from function: {}", response.parseTag());

				totalRecords = response.getTag("totalRowCount").getIntValue();
				log.info("total records:{}", totalRecords);
				continueInx = response.getTag("continueInx").getIntValue();

				results = QueryResultProcessingUtils
						.translateResponseIntoResultSet(response,
								new ArrayList<String>(), 0, entries.size());
				for (IRODSQueryResultRow row : results) {
					entry = createListingEntryFromResultRow(objStat, row,
							isCollection);
					entry.setCount(ctr++);
					entry.setLastResult(continueInx <= 0);
					entries.add(entry);
				}
			}

			return entries;

		} catch (FileDriverError fde) {
			log.warn("file driver error listing empty spec coll is ignored, just act as no data found");
			return new ArrayList<CollectionAndDataObjectListingEntry>();
		} catch (DataNotFoundException dnf) {
			log.info("end of data");
		}

		return entries;

	}

	private CollectionAndDataObjectListingEntry createListingEntryFromResultRow(
			final ObjStat objStat, final IRODSQueryResultRow row,
			final boolean isCollection) throws JargonException {
		CollectionAndDataObjectListingEntry listingEntry;
		CollectionAndPath collectionAndPath;
		listingEntry = new CollectionAndDataObjectListingEntry();
		listingEntry.setCreatedAt(IRODSDataConversionUtil
				.getDateFromIRODSValue(row.getColumn(2)));
		listingEntry.setDataSize(IRODSDataConversionUtil
				.getLongOrZeroFromIRODSValue(row.getColumn(4)));
		listingEntry.setModifiedAt(IRODSDataConversionUtil
				.getDateFromIRODSValue(row.getColumn(3)));
		row.getColumn(1);

		if (isCollection) {
			listingEntry.setObjectType(ObjectType.COLLECTION);
		} else {
			listingEntry.setObjectType(ObjectType.DATA_OBJECT);
		}

		listingEntry.setOwnerName(objStat.getOwnerName());
		listingEntry.setOwnerZone(objStat.getOwnerZone());

		if (isCollection) {

			collectionAndPath = MiscIRODSUtils
					.separateCollectionAndPathFromGivenAbsolutePath(row
							.getColumn(0));
			listingEntry.setParentPath(collectionAndPath.getCollectionParent());
			listingEntry.setPathOrName(row.getColumn(0));

		} else {
			listingEntry.setParentPath(row.getColumn(0));
			listingEntry.setPathOrName(row.getColumn(1));
		}
		listingEntry.setSpecColType(objStat.getSpecColType());
		return listingEntry;
	}

	private List<CollectionAndDataObjectListingEntry> listCollectionsUnderPathViaGenQuery(
			final ObjStat objStat, final int partialStartIndex,
			final String effectiveAbsolutePath) throws JargonException {

		List<CollectionAndDataObjectListingEntry> subdirs;

		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, false,
				true, null);
		try {
			IRODSFileSystemAOHelper.buildQueryListAllCollections(
					effectiveAbsolutePath, builder);
		} catch (GenQueryBuilderException e) {
			log.error("query builder exception", e);
			throw new JargonException("error building query", e);
		}

		IRODSQueryResultSet resultSet = queryForPathAndReturnResultSet(
				objStat.getAbsolutePath(), builder, partialStartIndex, objStat);

		subdirs = new ArrayList<CollectionAndDataObjectListingEntry>(resultSet
				.getResults().size());
		CollectionAndDataObjectListingEntry collectionAndDataObjectListingEntry = null;

		for (IRODSQueryResultRow row : resultSet.getResults()) {
			collectionAndDataObjectListingEntry = CollectionAOHelper
					.buildCollectionListEntryFromResultSetRowForCollectionQuery(
							row, resultSet.getTotalRecords());

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

	IRODSQueryResultSet queryForPathAndReturnResultSet(
			final String absolutePath, final IRODSGenQueryBuilder builder,
			final int partialStartIndex, final ObjStat objStat)
					throws JargonException {

		log.info("queryForPathAndReturnResultSet for: {}", absolutePath);
		IRODSGenQueryExecutor irodsGenQueryExecutor = collectionAndDataObjectListAndSearchAO
				.getIRODSAccessObjectFactory().getIRODSGenQueryExecutor(
						collectionAndDataObjectListAndSearchAO
						.getIRODSAccount());

		IRODSGenQueryFromBuilder irodsQuery;
		IRODSQueryResultSet resultSet;

		try {
			irodsQuery = builder
					.exportIRODSQueryFromBuilder(collectionAndDataObjectListAndSearchAO
							.getJargonProperties().getMaxFilesAndDirsQueryMax());
			resultSet = irodsGenQueryExecutor
					.executeIRODSQueryWithPagingInZone(irodsQuery,
							partialStartIndex,
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

	/**
	 * List the data objects underneath the given path given an already obtained
	 * <code>ObjStat</code>
	 *
	 * @param objStat
	 * @param partialStartIndex
	 * @return
	 * @throws JargonException
	 */
	List<CollectionAndDataObjectListingEntry> listDataObjectsUnderPath(
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

		log.info("listDataObjectsUnderPath for: {}", objStat);

		List<CollectionAndDataObjectListingEntry> files;
		if (objStat.getSpecColType() == SpecColType.STRUCT_FILE_COLL
				|| objStat.getSpecColType() == SpecColType.MOUNTED_COLL) {

			files = listUnderPathWhenSpecColl(objStat, effectiveAbsolutePath,
					false);
		} else {

			files = listDataObjectsUnderPathViaGenQuery(objStat,
					partialStartIndex, effectiveAbsolutePath);
		}

		return files;

	}

	private List<CollectionAndDataObjectListingEntry> listDataObjectsUnderPathViaGenQuery(
			final ObjStat objStat, final int partialStartIndex,
			final String effectiveAbsolutePath) throws JargonException {
		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, false,
				true, null);

		IRODSFileSystemAOHelper
		.buildQueryListAllDataObjectsWithSizeAndDateInfo(
				effectiveAbsolutePath, builder);
		IRODSQueryResultSet resultSet;

		try {
			resultSet = queryForPathAndReturnResultSet(effectiveAbsolutePath,
					builder, partialStartIndex, objStat);
		} catch (JargonException e) {
			log.error("exception querying for data objects:{}", builder, e);
			throw new JargonException("error in query", e);
		}

		List<CollectionAndDataObjectListingEntry> files = new ArrayList<CollectionAndDataObjectListingEntry>(
				resultSet.getResults().size());

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
					.buildCollectionListEntryFromResultSetRowForDataObjectQuery(
							row, resultSet.getTotalRecords());

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
	void augmentCollectionEntryForSpecialCollections(final ObjStat objStat,
			final String effectiveAbsolutePath,
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

}
