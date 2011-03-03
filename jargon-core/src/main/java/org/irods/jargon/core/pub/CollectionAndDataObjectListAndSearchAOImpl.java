package org.irods.jargon.core.pub;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.aohelper.CollectionAOHelper;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.pub.io.IRODSFileFactoryImpl;
import org.irods.jargon.core.pub.io.IRODSFileSystemAOHelper;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.IRODSGenQuery;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.core.query.UserFilePermission;
import org.irods.jargon.core.utils.IRODSDataConversionUtil;
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
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class CollectionAndDataObjectListAndSearchAOImpl extends IRODSGenericAO
		implements CollectionAndDataObjectListAndSearchAO {

	private static final String QUERY_EXCEPTION_FOR_QUERY = "query exception for  query:";
	public static final Logger LOG = LoggerFactory
			.getLogger(CollectionAndDataObjectListAndSearchAOImpl.class);
	private transient final IRODSFileFactory irodsFileFactory = new IRODSFileFactoryImpl(
			getIRODSSession(), getIRODSAccount());

	protected CollectionAndDataObjectListAndSearchAOImpl(
			final IRODSSession irodsSession, final IRODSAccount irodsAccount)
			throws JargonException {
		super(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO#
	 * listDataObjectsAndCollectionsUnderPath(java.lang.String)
	 */
	@Override
	public List<CollectionAndDataObjectListingEntry> listDataObjectsAndCollectionsUnderPath(
			final String absolutePathToParent) throws JargonException {
		List<CollectionAndDataObjectListingEntry> entries = listCollectionsUnderPath(
				absolutePathToParent, 0);
		entries.addAll(listDataObjectsUnderPath(absolutePathToParent, 0));
		return entries;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO#
	 * listDataObjectsAndCollectionsUnderPathWithPermissions(java.lang.String)
	 */
	@Override
	public List<CollectionAndDataObjectListingEntry> listDataObjectsAndCollectionsUnderPathWithPermissions(
			final String absolutePathToParent) throws JargonException {
		List<CollectionAndDataObjectListingEntry> entries = listCollectionsUnderPathWithPermissions(
				absolutePathToParent, 0);
		entries.addAll(listDataObjectsUnderPathWithPermissions(
				absolutePathToParent, 0));
		return entries;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO#
	 * countDataObjectsAndCollectionsUnderPath(java.lang.String)
	 */
	@Override
	public int countDataObjectsAndCollectionsUnderPath(
			final String absolutePathToParent) throws JargonException {

		if (absolutePathToParent == null) {
			throw new JargonException("absolutePathToParent is null");
		}

		LOG.info("countDataObjectsAndCollectionsUnder: {}",
				absolutePathToParent);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(absolutePathToParent);

		// I cannot get children if this is not a directory (a file has no
		// children)
		if (!irodsFile.isDirectory()) {
			LOG.error(
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
				.escapeSingleQuotes(absolutePathToParent));
		query.append("'");

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(query.toString(), 1);
		IRODSQueryResultSetInterface resultSet;

		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(
					irodsQuery, 0);
		} catch (JargonQueryException e) {
			LOG.error(QUERY_EXCEPTION_FOR_QUERY + query.toString(), e);
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
				.escapeSingleQuotes(absolutePathToParent));
		query.append("'");

		irodsQuery = IRODSGenQuery.instance(query.toString(), 1);

		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQuery(irodsQuery, 0);
		} catch (JargonQueryException e) {
			LOG.error(QUERY_EXCEPTION_FOR_QUERY + query.toString(), e);
			throw new JargonException("error in exists query", e);
		}

		int collCtr = 0;
		if (resultSet.getResults().size() > 0) {
			collCtr = IRODSDataConversionUtil
					.getIntOrZeroFromIRODSValue(resultSet.getFirstResult()
							.getColumn(0));
		}

		int total = fileCtr + collCtr;

		LOG.debug("computed count = {}", total);
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

		LOG.info("searchCollectionsBasedOnName:{}", searchTerm);

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

		StringBuilder query = new StringBuilder(
				IRODSFileSystemAOHelper.buildQueryListAllCollections(sb
						.toString()));
		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(sb.toString(),
				getIRODSSession().getJargonProperties()
						.getMaxFilesAndDirsQueryMax());
		IRODSQueryResultSetInterface resultSet;

		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQueryWithPaging(
					irodsQuery, partialStartIndex);
		} catch (JargonQueryException e) {
			LOG.error(QUERY_EXCEPTION_FOR_QUERY + query.toString(), e);
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
	 */
	@Override
	public List<CollectionAndDataObjectListingEntry> listCollectionsUnderPath(
			final String absolutePathToParent, final int partialStartIndex)
			throws JargonException {

		if (absolutePathToParent == null) {
			throw new JargonException("absolutePathToParent is null");
		}

		String path;
		List<CollectionAndDataObjectListingEntry> subdirs = new ArrayList<CollectionAndDataObjectListingEntry>();

		if (absolutePathToParent.isEmpty()) {
			path = "/";
		} else {
			path = absolutePathToParent;
		}

		String query = IRODSFileSystemAOHelper
				.buildQueryListAllCollections(path);

		IRODSQueryResultSetInterface resultSet = queryForPathAndReturnResultSet(
				path, query, partialStartIndex);

		CollectionAndDataObjectListingEntry collectionAndDataObjectListingEntry = null;

		for (IRODSQueryResultRow row : resultSet.getResults()) {
			collectionAndDataObjectListingEntry = CollectionAOHelper
					.buildCollectionListEntryFromResultSetRowForCollectionQuery(row);

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
			throws DataNotFoundException, JargonException {

		if (absolutePathToParent == null) {
			throw new IllegalArgumentException("absolutePathToParent is null");
		}

		String path;
		List<CollectionAndDataObjectListingEntry> subdirs = new ArrayList<CollectionAndDataObjectListingEntry>();

		if (absolutePathToParent.isEmpty()) {
			path = "/";
		} else {
			path = absolutePathToParent;
		}

		LOG.info("listCollectionsUnderPathWithPermissionsForUser for: {}", path);

		String query = IRODSFileSystemAOHelper
				.buildQueryListAllDirsWithUserAccessInfo(path);

		IRODSQueryResultSetInterface resultSet = queryForPathAndReturnResultSet(
				path, query, partialStartIndex);

		CollectionAndDataObjectListingEntry collectionAndDataObjectListingEntry = null;
		List<UserFilePermission> userFilePermissions = new ArrayList<UserFilePermission>();
		String lastPath = "";
		boolean isAtEndOfQueryResults = false;

		for (IRODSQueryResultRow row : resultSet.getResults()) {

			isAtEndOfQueryResults = row.isLastResult();
			// compare to the previous path and see if I break, in which case
			// add the last collection entry to the result
			String thisPath = row.getColumn(0);

			if (thisPath.equals(lastPath)) {
				// parse out the file permission and continue,
				CollectionAOHelper.buildUserFilePermissionForCollection(
						userFilePermissions, row);
				continue;
			} else {
				// is a break on path, put out the info for the last path if
				// it's there

				if (collectionAndDataObjectListingEntry != null) {
					collectionAndDataObjectListingEntry
							.setUserFilePermission(userFilePermissions);
					subdirs.add(collectionAndDataObjectListingEntry);
				}

				// on break in path, initialize the data for a new entry
				collectionAndDataObjectListingEntry = CollectionAOHelper
						.buildCollectionListEntryFromResultSetRowForCollectionQuery(row);
				lastPath = collectionAndDataObjectListingEntry.getPathOrName();
				userFilePermissions = new ArrayList<UserFilePermission>();
				CollectionAOHelper.buildUserFilePermissionForCollection(
						userFilePermissions, row);
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
				LOG.debug("adding last entry");
				collectionAndDataObjectListingEntry
						.setUserFilePermission(userFilePermissions);
				subdirs.add(collectionAndDataObjectListingEntry);
			} else {
				LOG.debug("ignoring last entry, as it might carry over to the next page of results");
			}
		}

		return subdirs;

	}

	private IRODSQueryResultSetInterface queryForPathAndReturnResultSet(
			final String irodsAbsolutePath, final String queryString,
			final int partialStartIndex) throws JargonException {
		
		LOG.info("listCollectionsAndDataObjectsUnderPath for: {}",
				irodsAbsolutePath);
		IRODSGenQueryExecutor irodsGenQueryExecutor = new IRODSGenQueryExecutorImpl(
				this.getIRODSSession(), this.getIRODSAccount());

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString,
				getIRODSSession().getJargonProperties()
						.getMaxFilesAndDirsQueryMax());
		IRODSQueryResultSetInterface resultSet;

		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQueryWithPaging(
					irodsQuery, partialStartIndex);
		} catch (JargonQueryException e) {
			LOG.error(QUERY_EXCEPTION_FOR_QUERY + queryString, e);
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
	public List<CollectionAndDataObjectListingEntry> listDataObjectsUnderPath(
			final String absolutePathToParent, final int partialStartIndex)
			throws JargonException {

		if (absolutePathToParent == null) {
			throw new JargonException("absolutePathToParent is null");
		}

		String path;
		List<CollectionAndDataObjectListingEntry> files = new ArrayList<CollectionAndDataObjectListingEntry>();

		if (absolutePathToParent.isEmpty()) {
			return files;
		}

		path = absolutePathToParent;

		LOG.info("listDataObjectsUnderPath for: {}", path);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(absolutePathToParent);

		if (irodsFile.isDirectory()) {
			LOG.debug("is directory");
			path = irodsFile.getAbsolutePath();
		} else {
			path = irodsFile.getParent();
			LOG.debug("is file, using parent path: {}", path);
		}

		IRODSGenQueryExecutor irodsGenQueryExecutor = new IRODSGenQueryExecutorImpl(
				this.getIRODSSession(), this.getIRODSAccount());

		StringBuilder query = new StringBuilder(
				IRODSFileSystemAOHelper
						.buildQueryListAllDataObjectsWithSizeAndDateInfo(path));
		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(query.toString(),
				getIRODSSession().getJargonProperties()
						.getMaxFilesAndDirsQueryMax());
		IRODSQueryResultSetInterface resultSet;

		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQueryWithPaging(
					irodsQuery, partialStartIndex);
		} catch (JargonQueryException e) {
			LOG.error(QUERY_EXCEPTION_FOR_QUERY + query.toString(), e);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO#
	 * listDataObjectsUnderPathWithPermissions(java.lang.String, int)
	 */
	@Override
	public List<CollectionAndDataObjectListingEntry> listDataObjectsUnderPathWithPermissions(
			final String absolutePathToParent, final int partialStartIndex)
			throws JargonException {

		if (absolutePathToParent == null) {
			throw new JargonException("absolutePathToParent is null");
		}

		LOG.info("listDataObjectsUnderPathWithPermissions for: {}",
				absolutePathToParent);

		List<CollectionAndDataObjectListingEntry> files = new ArrayList<CollectionAndDataObjectListingEntry>();

		if (absolutePathToParent.isEmpty()) {
			return files;
		}

		String queryString = IRODSFileSystemAOHelper
				.buildQueryListAllDataObjectsWithUserAccessInfo(absolutePathToParent);
		IRODSQueryResultSetInterface resultSet = this
				.queryForPathAndReturnResultSet(absolutePathToParent,
						queryString, partialStartIndex);
		LOG.debug("got result set:{}}, resultSet");

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
							userFilePermissions, row);
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
				files.add(entry);
			}

			// clear and reinitialize for new entry set
			entry = CollectionAOHelper
					.buildCollectionListEntryFromResultSetRowForDataObjectQuery(row);
			lastPath = currentPath;
			lastReplNumber = currentReplNumber;
			userFilePermissions = new ArrayList<UserFilePermission>();
			CollectionAOHelper.buildUserFilePermissionForDataObject(
					userFilePermissions, row);

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
				files.add(entry);
			} else {
				LOG.debug("skipping last entry as it may carry over to the next query page");
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

		LOG.info("searchDataObjectsBasedOnName:{}", searchTerm);

		StringBuilder sb = new StringBuilder();
		sb.append("SELECT DISTINCT ");
		sb.append(IRODSFileSystemAOHelper.buildDataObjectQuerySelects());
		sb.append(" WHERE ");
		sb.append(RodsGenQueryEnum.COL_DATA_NAME.getName());
		sb.append(" LIKE '%");
		sb.append(searchTerm.trim());
		sb.append("%'");

		IRODSGenQueryExecutor irodsGenQueryExecutor = new IRODSGenQueryExecutorImpl(
				this.getIRODSSession(), this.getIRODSAccount());

		StringBuilder query = new StringBuilder(
				IRODSFileSystemAOHelper.buildQueryListAllCollections(sb
						.toString()));
		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(sb.toString(),
				getIRODSSession().getJargonProperties()
						.getMaxFilesAndDirsQueryMax());
		IRODSQueryResultSetInterface resultSet;

		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQueryWithPaging(
					irodsQuery, partialStartIndex);
		} catch (JargonQueryException e) {
			LOG.error(QUERY_EXCEPTION_FOR_QUERY + query.toString(), e);
			throw new JargonException("error in exists query", e);
		}

		List<CollectionAndDataObjectListingEntry> entries = new ArrayList<CollectionAndDataObjectListingEntry>();
		for (IRODSQueryResultRow row : resultSet.getResults()) {
			entries.add(CollectionAOHelper
					.buildCollectionListEntryFromResultSetRowForDataObjectQuery(row));
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

		LOG.info(
				"searchCollectionsAndDataObjectsBasedOnName for search term:{}, starting with collections",
				searchTerm);
		List<CollectionAndDataObjectListingEntry> entries = searchCollectionsBasedOnName(searchTerm);
		LOG.info("adding data objects to search results");
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
	public Object getFullObjectForType(final String objectAbsolutePath)
			throws DataNotFoundException, JargonException {

		if (objectAbsolutePath == null || objectAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty object absolute path");
		}

		LOG.info("getFullObjectForType for path:{}", objectAbsolutePath);

		// see if file or coll
		Object returnObject = null;

		DataObjectAO dataObjectAO = new DataObjectAOImpl(getIRODSSession(),
				getIRODSAccount());

		LOG.debug("looking for as a data object");

		try {
			returnObject = dataObjectAO.findByAbsolutePath(objectAbsolutePath);
		} catch (DataNotFoundException dnf) {
			LOG.debug("not a data object, look as collection");

		}

		if (returnObject == null) {
			try {
				CollectionAO collectionAO = new CollectionAOImpl(
						getIRODSSession(), getIRODSAccount());
				returnObject = collectionAO
						.findByAbsolutePath(objectAbsolutePath);
			} catch (DataNotFoundException dnf) {
				LOG.debug("not found as collection");
			}
		}

		if (returnObject == null) {
			LOG.debug("not found as a collection, data not found");
			throw new DataNotFoundException(
					"no object found for absolute path:" + objectAbsolutePath);
		}

		// get appropriate domain object and return
		return returnObject;
	}

}
