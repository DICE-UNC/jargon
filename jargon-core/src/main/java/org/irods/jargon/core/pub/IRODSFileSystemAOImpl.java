/**
 * 
 */
package org.irods.jargon.core.pub;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.CatalogAlreadyHasItemByThatNameException;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonFileOrCollAlreadyExistsException;
import org.irods.jargon.core.packinstr.CollInp;
import org.irods.jargon.core.packinstr.DataObjCloseInp;
import org.irods.jargon.core.packinstr.DataObjCopyInp;
import org.irods.jargon.core.packinstr.DataObjInp;
import org.irods.jargon.core.packinstr.MsgHeader;
import org.irods.jargon.core.packinstr.Tag;
import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.core.pub.domain.Resource;
import org.irods.jargon.core.pub.domain.User;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileSystemAOHelper;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType;
import org.irods.jargon.core.query.IRODSGenQuery;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSet;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.core.utils.CollectionAndPath;
import org.irods.jargon.core.utils.IRODSConstants;
import org.irods.jargon.core.utils.IRODSDataConversionUtil;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a backing object for IRODSFileImpl, handling all IRODS interactions.
 * This class is usable as API, but methods are more properly called from
 * IRODSFile, which wraps these operations with the <code>java.io.File</code>
 * methods. Methods that back operations not defined in
 * <code>java.io.File</code> are not implemented in this particular access
 * object.
 * 
 * 
 * @author Mike Conway - DICE (www.irods.org)
 */
public final class IRODSFileSystemAOImpl extends IRODSGenericAO implements
		IRODSFileSystemAO {

	static Logger log = LoggerFactory.getLogger(IRODSFileSystemAOImpl.class);
	public final char COMMA = ',';
	public final String AND_VALUE = " AND ";
	private final IRODSGenQueryExecutor irodsGenQueryExecutor;
	private final UserAO userAO;
	private final CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO;

	/**
	 * @param irodsProtocol
	 * @throws JargonException
	 */
	public IRODSFileSystemAOImpl(final IRODSSession irodsSession,
			final IRODSAccount irodsAccount) throws JargonException {
		super(irodsSession, irodsAccount);
		irodsGenQueryExecutor = this.getIRODSAccessObjectFactory()
				.getIRODSGenQueryExecutor(this.getIRODSAccount());
		userAO = this.getIRODSAccessObjectFactory().getUserAO(irodsAccount);
		collectionAndDataObjectListAndSearchAO = this
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(getIRODSAccount());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.io.IRODSFileSystemAO#isFileReadable(org.irods
	 * .jargon.core.pub.io.IRODSFile)
	 */
	@Override
	public boolean isFileReadable(final IRODSFile irodsFile)
			throws JargonException {
		log.info("isFileReadable()");
		boolean readable = false;
		if (irodsFile == null) {
			throw new IllegalArgumentException("irods file is null");
		}

		log.info("checking read permissions on:{}", irodsFile);

		int filePermissions = 0;

		log.info("checking if isFile or isDirectory to properly build permissions query...");

		if (irodsFile.isFile()) {
			log.debug("getting file permissions");
			filePermissions = getFilePermissions(irodsFile);
		} else if (irodsFile.isDirectory()) {
			log.debug("getting directory permissions");
			filePermissions = getDirectoryPermissions(irodsFile);
		}

		if (filePermissions >= IRODSFile.READ_PERMISSIONS) {
			readable = true;
		}
		return readable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.IRODSFileSystemAO#isFileExecutable(org.irods
	 * .jargon.core.pub.io.IRODSFile)
	 */
	@Override
	public boolean isFileExecutable(final IRODSFile irodsFile)
			throws JargonException {

		log.info("isFileExecutable()");
		if (irodsFile == null) {
			throw new IllegalArgumentException("irodsFile is null");
		}

		boolean executable = false;

		if (irodsFile.exists()) {
			if (irodsFile.isDirectory()) {
				executable = false;
			} else {
				executable = checkIfDataObjectExecutable(irodsFile);
			}

		}

		log.info("is executable:{}", executable);
		return executable;

	}

	/**
	 * Do a query on the given file to see if it has an executable bit set
	 * 
	 * @param irodsFile
	 * @return <code>boolean</code> of <code>true</code> if file is data object,
	 *         exists, and is executable
	 * @throws JargonException
	 */
	private boolean checkIfDataObjectExecutable(final IRODSFile irodsFile)
			throws JargonException {
		StringBuilder filePermissionQuery = new StringBuilder();
		filePermissionQuery.append("SELECT ");
		filePermissionQuery.append(RodsGenQueryEnum.COL_D_DATA_MODE.getName());

		filePermissionQuery.append(" WHERE ");
		filePermissionQuery.append(RodsGenQueryEnum.COL_COLL_NAME.getName());
		filePermissionQuery.append(" = '");
		filePermissionQuery.append(IRODSDataConversionUtil
				.escapeSingleQuotes(irodsFile.getParent()));
		filePermissionQuery.append("'");
		filePermissionQuery.append(AND_VALUE);

		filePermissionQuery.append(RodsGenQueryEnum.COL_DATA_NAME.getName());
		filePermissionQuery.append(" = '");
		filePermissionQuery.append(IRODSDataConversionUtil
				.escapeSingleQuotes(irodsFile.getName()));
		filePermissionQuery.append("'");

		log.debug("data oject exec query:{}", filePermissionQuery);

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(
				filePermissionQuery.toString(), 100);

		IRODSQueryResultSetInterface resultSet;
		try {
			boolean executable = false;
			resultSet = irodsGenQueryExecutor
					.executeIRODSQueryAndCloseResultInZone(irodsQuery, 0,
							MiscIRODSUtils.getZoneInPath(irodsFile
									.getAbsolutePath()));
			IRODSQueryResultRow resultRow = resultSet.getFirstResult();
			if (resultRow.getColumn(0).equals("33261")) {
				executable = true;
			}
			return executable;
		} catch (DataNotFoundException dnf) {
			log.info("no result found");
			return false;
		} catch (JargonQueryException e) {
			log.error("query exception for  query:{}",
					filePermissionQuery.toString(), e);
			throw new JargonException("error in file permissions query");
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.io.IRODSFileSystemAO#isFileWriteable(org.irods
	 * .jargon.core.pub.io.IRODSFile)
	 */
	@Override
	public boolean isFileWriteable(final IRODSFile irodsFile)
			throws JargonException {
		boolean writeable = false;
		if (irodsFile == null) {
			throw new IllegalArgumentException("irods file is null");
		}
		if (log.isInfoEnabled()) {
			log.info("checking write permissions on:" + irodsFile);
		}

		int filePermissions = 0;

		if (irodsFile.isFile()) {
			log.debug("getting file permissions");
			filePermissions = getFilePermissions(irodsFile);
		} else if (irodsFile.isDirectory()) {
			log.debug("getting directory permissions");
			filePermissions = getDirectoryPermissions(irodsFile);
		}

		if (filePermissions >= IRODSFile.WRITE_PERMISSIONS) {
			writeable = true;
		}
		return writeable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.io.IRODSFileSystemAO#getFilePermissions(org
	 * .irods.jargon.core.pub.io.IRODSFile)
	 */
	@Override
	public int getFilePermissions(final IRODSFile irodsFile)
			throws JargonException {

		if (irodsFile == null) {
			throw new IllegalArgumentException("irods file is null");
		}

		log.info("checking permissions on:{}", irodsFile);

		return getFilePermissionsForGivenUser(irodsFile, this.getIRODSAccount()
				.getUserName());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.IRODSFileSystemAO#getFilePermissionsForGivenUser
	 * (org.irods.jargon.core.pub.io.IRODSFile, java.lang.String)
	 */
	@Override
	public int getFilePermissionsForGivenUser(final IRODSFile irodsFile,
			final String userName) throws JargonException {

		if (irodsFile == null) {
			throw new IllegalArgumentException("irods file is null");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("userName is null or empty");
		}

		log.info("checking permissions on:{}", irodsFile);
		log.info("for userName:{}", userName);

		log.debug("getting file permissions on file:{} ",
				irodsFile.getAbsolutePath());

		/*
		 * See if this is a soft link
		 */

		ObjStat objStat = collectionAndDataObjectListAndSearchAO
				.retrieveObjectStatForPath(irodsFile.getAbsolutePath());

		if (objStat == null) {
			log.error("no file found for path:{}", irodsFile.getAbsolutePath());
			throw new DataNotFoundException("no file found for given path");
		}

		/*
		 * See if jargon supports the given object type
		 */
		MiscIRODSUtils.evaluateSpecCollSupport(objStat);
		String effectiveAbsolutePath = MiscIRODSUtils
				.determineAbsolutePathBasedOnCollTypeInObjectStat(objStat);
		CollectionAndPath collectionAndPath = MiscIRODSUtils
				.separateCollectionAndPathFromGivenAbsolutePath(effectiveAbsolutePath);

		User user = userAO.findByName(userName);

		log.debug("user name translated to id:{}", user.getId());

		StringBuilder filePermissionQuery = buildPermisionsQueryFile(
				collectionAndPath.getCollectionParent(),
				collectionAndPath.getChildName(), user.getId());

		log.debug("query for user permissions = {}",
				filePermissionQuery.toString());

		int highestPermissionValue = extractHighestPermission(
				irodsGenQueryExecutor, filePermissionQuery,
				MiscIRODSUtils.getZoneInPath(effectiveAbsolutePath));

		log.debug("highest permission value:{}", highestPermissionValue);

		return highestPermissionValue;
	}

	/**
	 * @param irodsGenQueryExecutor
	 * @param filePermissionQuery
	 * @param zone
	 * @return
	 * @throws JargonException
	 */
	private int extractHighestPermission(
			final IRODSGenQueryExecutor irodsGenQueryExecutor,
			final StringBuilder filePermissionQuery, final String zone)
			throws JargonException {

		log.info("extractHighestPermission()");

		if (zone == null) {
			throw new IllegalArgumentException("null zone");
		}

		log.info("for zone:{}", zone);

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(
				filePermissionQuery.toString(), 500);

		IRODSQueryResultSetInterface resultSet;
		try {
			resultSet = irodsGenQueryExecutor
					.executeIRODSQueryAndCloseResultInZone(irodsQuery, 0, zone);
		} catch (JargonQueryException e) {
			log.error("query exception for  query:{}",
					filePermissionQuery.toString(), e);
			throw new JargonException("error in file permissions query");
		}

		int highestPermissionValue = -1;
		int resultPermissionValue = 0;
		for (IRODSQueryResultRow result : resultSet.getResults()) {
			try {
				resultPermissionValue = Integer.parseInt(result.getColumn(0));
				if (resultPermissionValue > highestPermissionValue) {
					highestPermissionValue = resultPermissionValue;
				}
			} catch (NumberFormatException nfe) {
				String msg = "number format exception for result:"
						+ result.getColumn(0)
						+ " I expected a numeric value for the access permissions in col 0";
				log.error(msg);
				throw new JargonException(msg);
			}
		}
		return highestPermissionValue;
	}

	/**
	 * @param zone
	 * @param parent
	 * @param fileName
	 * @param userId
	 * @return
	 */
	private StringBuilder buildPermisionsQueryFile(final String parent,
			final String fileName, final String userId) throws JargonException {
		StringBuilder filePermissionQuery = new StringBuilder();
		filePermissionQuery.append("SELECT ");
		filePermissionQuery.append(RodsGenQueryEnum.COL_DATA_ACCESS_TYPE
				.getName());

		filePermissionQuery.append(" WHERE ");

		filePermissionQuery.append(RodsGenQueryEnum.COL_COLL_NAME.getName());
		filePermissionQuery.append(" = '");
		filePermissionQuery.append(IRODSDataConversionUtil
				.escapeSingleQuotes(parent));
		filePermissionQuery.append("'");
		filePermissionQuery.append(AND_VALUE);

		filePermissionQuery.append(RodsGenQueryEnum.COL_DATA_NAME.getName());
		filePermissionQuery.append(" = '");
		filePermissionQuery.append(IRODSDataConversionUtil
				.escapeSingleQuotes(fileName));
		filePermissionQuery.append("'");

		filePermissionQuery.append(AND_VALUE);

		filePermissionQuery.append(RodsGenQueryEnum.COL_DATA_ACCESS_USER_ID
				.getName());
		filePermissionQuery.append(" = '");
		filePermissionQuery.append(userId);
		filePermissionQuery.append("'");

		return filePermissionQuery;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.io.IRODSFileSystemAO#getDirectoryPermissions
	 * (org.irods.jargon.core.pub.io.IRODSFile)
	 */
	@Override
	public int getDirectoryPermissions(final IRODSFile irodsFile)
			throws JargonException {

		return getDirectoryPermissionsForGivenUser(irodsFile, this
				.getIRODSAccount().getUserName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.IRODSAccessObject#
	 * getDirectoryPermissionsForGivenUser
	 * (org.irods.jargon.core.pub.io.IRODSFile, java.lang.String)
	 */
	@Override
	public int getDirectoryPermissionsForGivenUser(final IRODSFile irodsFile,
			final String userName) throws FileNotFoundException, JargonException {

		if (irodsFile == null) {
			throw new IllegalArgumentException("irods file is null");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("userName is null or empty");
		}

		log.info("checking directory permissions on:{}", irodsFile);
		log.info("for userName:{}", userName);

		ObjStat objStat = collectionAndDataObjectListAndSearchAO
				.retrieveObjectStatForPath(irodsFile.getAbsolutePath());

		if (objStat == null) {
			log.error("no file found for path:{}", irodsFile.getAbsolutePath());
			throw new DataNotFoundException("no file found for given path");
		}

		if (!objStat.isSomeTypeOfCollection()) {
			log.error(
					"cannot get directory permissions, this is not a colleciton:{}",
					objStat);
			throw new JargonException(
					"given file is not a collection, cannot get directory permissions");
		}

		/*
		 * See if jargon supports the given object type
		 */
		MiscIRODSUtils.evaluateSpecCollSupport(objStat);
		String effectiveAbsolutePath = MiscIRODSUtils
				.determineAbsolutePathBasedOnCollTypeInObjectStat(objStat);

		String fileName = irodsFile.getName();

		// get the id for the user name, GenQuery can't do this join

		User user = userAO.findByName(userName);

		log.debug("getting directory permissions on:{} ", fileName);
		log.debug("user name translated to id:{}", user.getId());

		StringBuilder filePermissionQuery = buildPermissionsQueryDirectory(
				effectiveAbsolutePath,
				user.getId());

		log.debug("query for user permissions = {}",
				filePermissionQuery.toString());

		int highestPermissionValue = extractHighestPermission(
				irodsGenQueryExecutor, filePermissionQuery,
				objStat.getOwnerZone());

		return highestPermissionValue;
	}

	/**
	 * @param zone
	 * @param dir
	 * @param userName
	 * @return
	 */
	private StringBuilder buildPermissionsQueryDirectory(final String dir,
			final String userId) throws JargonException {
		StringBuilder filePermissionQuery = new StringBuilder();
		filePermissionQuery.append("SELECT ");
		filePermissionQuery.append(RodsGenQueryEnum.COL_COLL_ACCESS_TYPE
				.getName());

		filePermissionQuery.append(" WHERE ");

		filePermissionQuery.append(RodsGenQueryEnum.COL_COLL_NAME.getName());
		filePermissionQuery.append(" = '");
		filePermissionQuery.append(IRODSDataConversionUtil
				.escapeSingleQuotes(dir));
		filePermissionQuery.append("'");
		filePermissionQuery.append(AND_VALUE);

		filePermissionQuery.append(RodsGenQueryEnum.COL_COLL_ACCESS_USER_ID
				.getName());
		filePermissionQuery.append(" = '");
		filePermissionQuery.append(userId);
		filePermissionQuery.append("'");

		return filePermissionQuery;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.io.IRODSFileSystemAO#isFileExists(org.irods
	 * .jargon.core.pub.io.IRODSFile)
	 */
	@Override
	public boolean isFileExists(final IRODSFile irodsFile)
			throws JargonException {

		boolean exists = false;
		if (irodsFile == null) {
			throw new IllegalArgumentException("irods file is null");
		}

		log.info("checking existence of: {}", irodsFile.getAbsolutePath());
		try {
			irodsFile.initializeObjStatForFile();
			// no error means it exists
			exists = true;
		} catch (FileNotFoundException e) {
			log.info("file not found, will treat as not exists");
		}

		return exists;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.io.IRODSFileSystemAO#isDirectory(org.irods.
	 * jargon.core.pub.io.IRODSFile)
	 */
	@Override
	public boolean isDirectory(final IRODSFile irodsFile)
			throws JargonException {

		if (irodsFile == null) {
			throw new IllegalArgumentException("irods file is null");
		}

		boolean isDir = false;
		log.info("checking is dir for: {}", irodsFile.getAbsolutePath());

		try {
			ObjStat objStat = irodsFile.initializeObjStatForFile();
			// no error means it exists
			if (objStat.getObjectType() == ObjectType.COLLECTION
					|| objStat.getObjectType() == ObjectType.LOCAL_DIR) {
				isDir = true;
			}
		} catch (FileNotFoundException e) {
			log.info("file not found, will treat as not dir");
		}

		return isDir;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.IRODSFileSystemAO#isFile(org.irods.jargon.core
	 * .pub.io.IRODSFile)
	 */
	@Override
	public boolean isFile(final IRODSFile irodsFile) throws JargonException {

		log.info("isFile()");
		if (irodsFile == null) {
			throw new IllegalArgumentException("irods file is null");
		}

		boolean isFile = false;

		log.info("checking is file for: {}", irodsFile.getAbsolutePath());

		try {
			ObjStat objStat = irodsFile.initializeObjStatForFile();
			// no error means it exists
			if (objStat.getObjectType() == ObjectType.DATA_OBJECT
					|| objStat.getObjectType() == ObjectType.LOCAL_FILE) {
				isFile = true;
			}
		} catch (FileNotFoundException e) {
			log.info("file not found, will treat as not file");
		}

		return isFile;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.IRODSFileSystemAO#getObjStat(java.lang.String)
	 */
	@Override
	public ObjStat getObjStat(final String irodsAbsolutePath)
			throws FileNotFoundException, JargonException {
		return collectionAndDataObjectListAndSearchAO
				.retrieveObjectStatForPath(irodsAbsolutePath);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.io.IRODSFileSystemAO#getListInDir(org.irods
	 * .jargon.core.pub.io.IRODSFile)
	 */
	@Override
	public List<String> getListInDir(final IRODSFile irodsFile)
			throws FileNotFoundException, JargonException {

		if (irodsFile == null) {
			throw new JargonException("irods file is null");
		}


		ObjStat objStat = irodsFile.initializeObjStatForFile();
		// no error means it exists
		/*
		 * See if jargon supports the given object type
		 */
		MiscIRODSUtils.evaluateSpecCollSupport(objStat);
		String effectiveAbsolutePath = MiscIRODSUtils
				.determineAbsolutePathBasedOnCollTypeInObjectStat(objStat);
		CollectionAndPath collectionAndPath = MiscIRODSUtils
				.separateCollectionAndPathFromGivenAbsolutePath(effectiveAbsolutePath);

		log.info("getListInDir for parent:{}", irodsFile.getAbsolutePath());

		List<String> subdirs = new ArrayList<String>();
		String path = "";

		if (irodsFile.isDirectory()) {
			path = effectiveAbsolutePath;
		} else {
			path = collectionAndPath.getCollectionParent();
		}

		log.debug("path for query:{}", path);

		StringBuilder query;
		IRODSGenQuery irodsQuery;
		IRODSQueryResultSet resultSet = null;

		// get all the subdirs

		query = new StringBuilder();
		query.append(IRODSFileSystemAOHelper.buildQueryListAllCollections(path));

		irodsQuery = IRODSGenQuery.instance(query.toString(), this
				.getJargonProperties().getMaxFilesAndDirsQueryMax());

		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQueryInZone(
					irodsQuery, 0, objStat.getOwnerZone());
			for (IRODSQueryResultRow row : resultSet.getResults()) {
				processListDirsResultRowForCollection(subdirs, row);
			}

			while (resultSet.isHasMoreRecords()) {
				log.debug("more results to get for listing collections, requerying");
				resultSet = irodsGenQueryExecutor.getMoreResultsInZone(
						resultSet, objStat.getOwnerZone());
				for (IRODSQueryResultRow row : resultSet.getResults()) {
					processListDirsResultRowForCollection(subdirs, row);
				}

			}
		} catch (JargonQueryException e) {
			log.error("query exception for  query:{}", query.toString(), e);
			throw new JargonException("error in exists query");
		} finally {
			if (resultSet != null) {
				irodsGenQueryExecutor.closeResults(resultSet);
			}
		}

		resultSet = null;

		// get all files
		query = new StringBuilder();
		query.append(IRODSFileSystemAOHelper.buildQueryListAllFiles(path));

		irodsQuery = IRODSGenQuery.instance(query.toString(), this
				.getJargonProperties().getMaxFilesAndDirsQueryMax());

		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQueryInZone(
					irodsQuery, 0, objStat.getOwnerZone());

			for (IRODSQueryResultRow row : resultSet.getResults()) {
				subdirs.add(row.getColumn(1));
			}

			while (resultSet.isHasMoreRecords()) {
				log.debug("more results to get for listing files, requerying");
				resultSet = irodsGenQueryExecutor.getMoreResultsInZone(
						resultSet, objStat.getOwnerZone());
				for (IRODSQueryResultRow row : resultSet.getResults()) {
					subdirs.add(row.getColumn(1));
				}
			}

		} catch (JargonQueryException e) {
			log.error("query exception for  query:{}", query.toString(), e);
			throw new JargonException("error in exists query");
		} finally {
			if (resultSet != null) {
				irodsGenQueryExecutor.closeResults(resultSet);
			}
		}

		log.info("length of subdirs after gathering all results:{}",
				subdirs.size());

		return subdirs;
	}

	/**
	 * @param subdirs
	 * @param row
	 * @throws JargonException
	 */
	private void processListDirsResultRowForCollection(
			final List<String> subdirs, final IRODSQueryResultRow row)
			throws JargonException {
		int idxLastSlash;
		idxLastSlash = row.getColumn(1).lastIndexOf('/');

		/*
		 * Special case when root is the parent, don't repeat root from query
		 * results
		 */

		if (!(row.getColumn(1).equals("/"))) {
			subdirs.add(row.getColumn(1).substring(idxLastSlash + 1));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.io.IRODSFileSystemAO#getListInDirWithFilter
	 * (org.irods.jargon.core.pub.io.IRODSFile, java.io.FilenameFilter)
	 */
	@Override
	public List<String> getListInDirWithFilter(final IRODSFile irodsFile,
			final FilenameFilter fileNameFilter) throws JargonException,
			DataNotFoundException {

		if (irodsFile == null) {
			throw new JargonException("irods file is null");
		}

		if (fileNameFilter == null) {
			throw new JargonException("file name filter is null");
		}

		List<String> subdirs = new ArrayList<String>();
		String path = "";

		if (irodsFile.isDirectory()) {
			path = irodsFile.getAbsolutePath();
		} else {
			path = irodsFile.getParent();
		}

		StringBuilder query;
		IRODSGenQuery irodsQuery;
		IRODSQueryResultSet resultSet = null;

		log.debug("path for query:{}", path);

		// get all the sub-directories

		query = new StringBuilder();
		query.append(IRODSFileSystemAOHelper.buildQueryListAllCollections(path));
		irodsQuery = IRODSGenQuery.instance(query.toString(), this
				.getJargonProperties().getMaxFilesAndDirsQueryMax());

		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQuery(irodsQuery, 0);
			for (IRODSQueryResultRow row : resultSet.getResults()) {
				// this is a dir, does it pass the file name filter?
				processRowForSubdirWhenListDirWithFilter(fileNameFilter,
						subdirs, row);
			}

			// could be more...

			while (resultSet.isHasMoreRecords()) {
				log.debug("more results to get for listing files, requerying");
				resultSet = irodsGenQueryExecutor.getMoreResults(resultSet);
				for (IRODSQueryResultRow row : resultSet.getResults()) {
					processRowForSubdirWhenListDirWithFilter(fileNameFilter,
							subdirs, row);
				}
			}

		} catch (JargonQueryException e) {
			log.error("query exception for  query:{}", query.toString(), e);
			throw new JargonException("error in exists query");
		} finally {
			if (resultSet != null) {
				irodsGenQueryExecutor.closeResults(resultSet);
			}
		}

		resultSet = null;

		// get all the files

		query = new StringBuilder(
				IRODSFileSystemAOHelper.buildQueryListAllFiles(path));

		irodsQuery = IRODSGenQuery.instance(query.toString(), this
				.getJargonProperties().getMaxFilesAndDirsQueryMax());

		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQuery(irodsQuery, 0);

			for (IRODSQueryResultRow row : resultSet.getResults()) {
				processRowWhenListDirWithFilter(fileNameFilter, subdirs, row);
			}

			// could be more...

			while (resultSet.isHasMoreRecords()) {
				log.debug("more results to get for listing files, requerying");
				resultSet = irodsGenQueryExecutor.getMoreResults(resultSet);
				for (IRODSQueryResultRow row : resultSet.getResults()) {
					processRowWhenListDirWithFilter(fileNameFilter, subdirs,
							row);
				}
			}
		} catch (JargonQueryException e) {
			log.error("query exception for  query:{}", query.toString(), e);
			throw new JargonException("error in exists query");
		} finally {
			if (resultSet != null) {
				irodsGenQueryExecutor.closeResults(resultSet);
			}
		}

		return subdirs;
	}

	/**
	 * @param fileNameFilter
	 * @param subdirs
	 * @param row
	 * @throws JargonException
	 */
	private void processRowForSubdirWhenListDirWithFilter(
			final FilenameFilter fileNameFilter, final List<String> subdirs,
			final IRODSQueryResultRow row) throws JargonException {
		String thisFileDir;
		thisFileDir = row.getColumn(0);
		if (fileNameFilter.accept(new File(thisFileDir), "")) {
			subdirs.add(row.getColumn(0));
		}
	}

	/**
	 * @param fileNameFilter
	 * @param subdirs
	 * @param row
	 * @throws JargonException
	 */
	private void processRowWhenListDirWithFilter(
			final FilenameFilter fileNameFilter, final List<String> subdirs,
			final IRODSQueryResultRow row) throws JargonException {

		String thisFileName;
		String thisFileDir;
		// this is a file, does it pass the file name filter?
		thisFileName = row.getColumn(1);
		thisFileDir = row.getColumn(0);
		if (fileNameFilter.accept(new File(thisFileDir), thisFileName)) {
			subdirs.add(row.getColumn(1));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.io.IRODSFileSystemAO#getListInDirWithFileFilter
	 * (org.irods.jargon.core.pub.io.IRODSFile, java.io.FileFilter)
	 */
	@Override
	public List<File> getListInDirWithFileFilter(final IRODSFile irodsFile,
			final FileFilter fileFilter) throws JargonException,
			DataNotFoundException {

		if (irodsFile == null) {
			throw new JargonException("irods file is null");
		}

		if (fileFilter == null) {
			throw new JargonException("file filter is null");
		}

		List<File> subdirs = new ArrayList<File>();
		String path = "";

		if (irodsFile.isDirectory()) {
			path = irodsFile.getAbsolutePath();
		} else {
			path = irodsFile.getParent();
		}

		StringBuilder query;
		IRODSGenQuery irodsQuery;
		IRODSQueryResultSet resultSet = null;

		// get all the sub-directories

		query = new StringBuilder();
		query.append(IRODSFileSystemAOHelper.buildQueryListAllCollections(path));

		irodsQuery = IRODSGenQuery.instance(query.toString(), this
				.getJargonProperties().getMaxFilesAndDirsQueryMax());

		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQuery(irodsQuery, 0);
			for (IRODSQueryResultRow row : resultSet.getResults()) {
				processSubdirRowWhenListFilesWithFileFilter(fileFilter,
						subdirs, row);
			}

			// could be more...

			while (resultSet.isHasMoreRecords()) {
				log.debug("more results to get for listing files, requerying");
				resultSet = irodsGenQueryExecutor.getMoreResults(resultSet);
				for (IRODSQueryResultRow row : resultSet.getResults()) {
					processSubdirRowWhenListFilesWithFileFilter(fileFilter,
							subdirs, row);
				}
			}

		} catch (JargonQueryException e) {
			log.error("query exception for  query:{}", query.toString(), e);
			throw new JargonException("error in exists query");
		} finally {
			if (resultSet != null) {
				irodsGenQueryExecutor.closeResults(resultSet);
			}
		}

		resultSet = null;

		log.debug("path for query:{}", path);

		query = new StringBuilder(
				IRODSFileSystemAOHelper.buildQueryListAllFiles(path));

		irodsQuery = IRODSGenQuery.instance(query.toString(), this
				.getJargonProperties().getMaxFilesAndDirsQueryMax());

		try {

			resultSet = irodsGenQueryExecutor.executeIRODSQuery(irodsQuery, 0);
			for (IRODSQueryResultRow row : resultSet.getResults()) {
				processFileRowWhenListFilesWithFileFilter(fileFilter, subdirs,
						row);
			}

			// could be more...

			while (resultSet.isHasMoreRecords()) {
				log.debug("more results to get for listing files, requerying");
				resultSet = irodsGenQueryExecutor.getMoreResults(resultSet);
				for (IRODSQueryResultRow row : resultSet.getResults()) {
					processFileRowWhenListFilesWithFileFilter(fileFilter,
							subdirs, row);
				}
			}

		} catch (JargonQueryException e) {
			log.error("query exception for  query:{}", query.toString(), e);
			throw new JargonException("error in exists query");
		} finally {
			if (resultSet != null) {
				irodsGenQueryExecutor.closeResults(resultSet);
			}
		}

		return subdirs;
	}

	/**
	 * @param fileFilter
	 * @param subdirs
	 * @param row
	 * @throws JargonException
	 */
	private void processSubdirRowWhenListFilesWithFileFilter(
			final FileFilter fileFilter, final List<File> subdirs,
			final IRODSQueryResultRow row) throws JargonException {
		String thisFileDir = row.getColumn(1);
		File irodsFile = (File) this.getIRODSFileFactory().instanceIRODSFile(
				thisFileDir);

		if (fileFilter.accept(irodsFile)) {
			subdirs.add(irodsFile);
		}
	}

	/**
	 * @param fileFilter
	 * @param subdirs
	 * @param row
	 * @throws JargonException
	 */
	private void processFileRowWhenListFilesWithFileFilter(
			final FileFilter fileFilter, final List<File> subdirs,
			final IRODSQueryResultRow row) throws JargonException {
		// this is a file, does it pass the file name filter?
		String thisFileDir = row.getColumn(0);
		String thisFileName = row.getColumn(1);
		File irodsFile = (File) this.getIRODSFileFactory().instanceIRODSFile(
				thisFileDir, thisFileName);

		if (fileFilter.accept(irodsFile)) {
			subdirs.add(irodsFile);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.io.IRODSFileSystemAO#getFileDataType(org.irods
	 * .jargon.core.pub.io.IRODSFile)
	 */
	@Override
	public ObjectType getFileDataType(final IRODSFile irodsFile)
			throws FileNotFoundException, JargonException {

		if (irodsFile == null) {
			throw new JargonException("irods file is null");
		}

		ObjStat objStat = irodsFile.initializeObjStatForFile();
		return objStat.getObjectType();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.io.IRODSFileSystemAO#createFile(java.lang.String
	 * , org.irods.jargon.core.packinstr.DataObjInp.OpenFlags, int)
	 */
	@Override
	public int createFile(final String absolutePath,
			final DataObjInp.OpenFlags openFlags, final int createMode)
			throws JargonException, JargonFileOrCollAlreadyExistsException {

		// find the correct resource and call the method with the resource
		// signature
		String defaultResource = this.getIRODSAccount()
				.getDefaultStorageResource();

		log.debug("setting resource to account default:{}", defaultResource);

		log.debug("checking if parent exists");
		IRODSFile target = this.getIRODSFileFactory().instanceIRODSFile(
				absolutePath);
		IRODSFile parent = (IRODSFile) target.getParentFile();
		if (!parent.exists()) {
			log.warn(
					"creating an output stream for parent:{} that does not exist, making parent dirs..",
					parent.getAbsolutePath());
			parent.mkdirs();
		}

		int fileId = 0;

		try {
			fileId = createFileInResource(absolutePath, openFlags, createMode,
					defaultResource);
		} catch (JargonFileOrCollAlreadyExistsException jfcae) {
			log.error("file or collection already exists");
			throw jfcae;
		}
		log.info("file created and closed");
		return fileId;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.io.IRODSFileSystemAO#openFile(org.irods.jargon
	 * .core.pub.io.IRODSFile,
	 * org.irods.jargon.core.packinstr.DataObjInp.OpenFlags)
	 */
	@Override
	public int openFile(final IRODSFile irodsFile,
			final DataObjInp.OpenFlags openFlags) throws JargonException {

		if (irodsFile == null) {
			throw new JargonException("irodsFile is null");
		}

		DataObjInp dataObjInp = DataObjInp.instanceForOpen(
				irodsFile.getAbsolutePath(), openFlags);

		if (log.isInfoEnabled()) {
			log.info("opening file:" + irodsFile.getAbsolutePath());
		}

		Tag response = getIRODSProtocol().irodsFunction(DataObjInp.PI_TAG,
				dataObjInp.getParsedTags(), DataObjInp.OPEN_FILE_API_NBR);

		if (response == null) {
			String msg = "null response from IRODS call";
			log.error(msg);
			throw new JargonException(msg);
		}

		// parse out the response
		int fileId = response.getTag(MsgHeader.PI_NAME)
				.getTag(MsgHeader.INT_INFO).getIntValue();

		log.debug("file id for opened file:{}", fileId);

		return fileId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.io.IRODSFileSystemAO#createFile(java.lang.String
	 * , org.irods.jargon.core.packinstr.DataObjInp.OpenFlags, int)
	 */
	@Override
	public int createFileInResource(final String absolutePath,
			final DataObjInp.OpenFlags openFlags, final int createMode,
			final String resource) throws JargonException,
			JargonFileOrCollAlreadyExistsException {

		if (absolutePath == null || absolutePath.length() == 0) {
			throw new JargonException("absolute path is null or empty");
		}

		if (openFlags == null) {
			throw new JargonException("open flags are null");
		}

		if (resource == null) {
			throw new JargonException(
					"resource is null, set to blank to automatically have the irods system select the default storage resource by rule");
		}

		String thisResource = null;
		if (!MiscIRODSUtils.isFileInThisZone(absolutePath, getIRODSAccount())) {
			thisResource = "";
		} else {
			thisResource = resource;
		}

		long offset = 0L;
		long dataSize = 0L;

		int responseFileNbr = 0;

		try {
			DataObjInp dataObjInp = DataObjInp
					.instance(absolutePath, createMode, openFlags, offset,
							dataSize, thisResource, null);
			Tag response = getIRODSProtocol().irodsFunction(DataObjInp.PI_TAG,
					dataObjInp.getParsedTags(), DataObjInp.CREATE_FILE_API_NBR);
			if (response == null) {
				String msg = "null response from IRODS call";
				log.error(msg);
				throw new JargonException(msg);
			}
			// parse out the response
			responseFileNbr = response.getTag(MsgHeader.PI_NAME)
					.getTag(MsgHeader.INT_INFO).getIntValue();

		} catch (JargonException e) {
			// differentiate between an attempt to add a duplicate file and a
			// general exception
			if (e.getMessage().indexOf("-312000") > -1) {
				throw new JargonFileOrCollAlreadyExistsException(e.getMessage());
			} else {
				log.error("jargon exception trying to create new file", e);
				throw e;
			}
		}
		log.debug("response file nbr:{}", responseFileNbr);

		return responseFileNbr;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.io.IRODSFileSystemAO#mkdir(org.irods.jargon
	 * .core.pub.io.IRODSFile, boolean)
	 */
	@Override
	public void mkdir(final IRODSFile irodsFile, final boolean recursiveOpr)
			throws JargonException {

		if (irodsFile == null) {
			throw new JargonException("irodsFile is null");
		}

		log.info("making dir for:{}", irodsFile.getAbsolutePath());

		CollInp collInp = CollInp.instance(irodsFile.getAbsolutePath(),
				recursiveOpr);

		try {
			Tag response = getIRODSProtocol().irodsFunction(CollInp.PI_TAG,
					collInp.getParsedTags(), CollInp.MKDIR_API_NBR);

			if (response != null) {
				log.warn(
						"expected null response to mkdir, logged but not an error, received:{}",
						response.parseTag());
			}
		} catch (CatalogAlreadyHasItemByThatNameException e) {
			log.info("directory already exists in mkdir, log and ignore");
		}

		log.debug("mkdir succesful");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFileSystemAO#fileClose(int)
	 */
	@Override
	public void fileClose(final int fileDescriptor) throws JargonException {

		log.info("closing file:{}", fileDescriptor);

		if (fileDescriptor <= 0) {
			throw new JargonException(
					"attempting to close file with no valid descriptor");
		}

		DataObjCloseInp dataObjCloseInp = DataObjCloseInp.instance(
				fileDescriptor, 0L);

		Tag response = getIRODSProtocol().irodsFunction(DataObjCloseInp.PI_TAG,
				dataObjCloseInp.getParsedTags(),
				DataObjCloseInp.FILE_CLOSE_API_NBR);

		if (response != null) {
			log.warn(
					"expected null response to close, logged but not an error, received:{}",
					response.parseTag());
		}

		log.debug("file close succesful");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.io.IRODSFileSystemAO#fileDeleteForce(org.irods
	 * .jargon.core.pub.io.IRODSFile)
	 */
	@Override
	public void fileDeleteForce(final IRODSFile irodsFile)
			throws JargonException {

		if (irodsFile == null) {
			throw new JargonException("irodsFile is null");
		}

		log.info("deleting:{}", irodsFile.getAbsolutePath());
		irodsFile.reset();

		if (!irodsFile.isFile()) {
			String msg = "file delete, given irodsFile is not a file";
			log.error(msg);
			throw new JargonException(msg);
		}

		DataObjInp dataObjInp = DataObjInp.instanceForDeleteWithForce(irodsFile
				.getAbsolutePath());
		Tag response = getIRODSProtocol().irodsFunction(DataObjInp.PI_TAG,
				dataObjInp.getParsedTags(), DataObjInp.DELETE_FILE_API_NBR);

		if (response != null) {
			String msg = "unexpected response from irods, expected null message - logged and ignored ";
			log.warn(msg);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.io.IRODSFileSystemAO#fileDeleteNoForce(org.
	 * irods.jargon.core.pub.io.IRODSFile)
	 */
	@Override
	public void fileDeleteNoForce(final IRODSFile irodsFile)
			throws JargonException {

		if (irodsFile == null) {
			throw new JargonException("irodsFile is null");
		}

		log.info("deleting without force option:{}",
				irodsFile.getAbsolutePath());

		irodsFile.reset();

		if (!irodsFile.isFile()) {
			String msg = "file delete, given irodsFile is not a file";
			log.error(msg);
			throw new JargonException(msg);
		}

		DataObjInp dataObjInp = DataObjInp
				.instanceForDeleteWithNoForce(irodsFile.getAbsolutePath());

		try {
			Tag response = getIRODSProtocol().irodsFunction(dataObjInp);

			if (response != null) {
				log.warn("unexpected response from irods, expected null message - logged and ignored ");
			}
		} catch (DuplicateDataException dde) {
			log.warn("duplicate data exception logged and ignored, see GForge: [#639] 809000 errors on delete operations when trash file already exists");
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.io.IRODSFileSystemAO#directoryDeleteForce(org
	 * .irods.jargon.core.pub.io.IRODSFile)
	 */
	@Override
	public void directoryDeleteForce(final IRODSFile irodsFile)
			throws JargonException {

		if (irodsFile == null) {
			throw new JargonException("irodsFile is null");
		}

		log.info("deleting:{}", irodsFile.getAbsolutePath());

		irodsFile.reset();

		if (!irodsFile.isDirectory()) {
			String msg = "directory delete, given irodsFile is not a collection";
			log.error(msg);
			throw new JargonException(msg);
		}

		CollInp collInp = CollInp.instance(irodsFile.getAbsolutePath(),
				CollInp.RECURSIVE_OPERATION, CollInp.FORCE_OPERATION);

		Tag response = getIRODSProtocol().irodsFunction(CollInp.PI_TAG,
				collInp.getParsedTags(), CollInp.RMDIR_API_NBR);

		processClientStatusMessages(response);

		log.info("deletion successful");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.io.IRODSFileSystemAO#directoryDeleteNoForce
	 * (org.irods.jargon.core.pub.io.IRODSFile)
	 */
	@Override
	public void directoryDeleteNoForce(final IRODSFile irodsFile)
			throws JargonException {

		if (irodsFile == null) {
			throw new JargonException("irodsFile is null");
		}

		log.info("deleting:{}", irodsFile.getAbsolutePath());

		irodsFile.reset();

		if (!irodsFile.isDirectory()) {
			String msg = "directory delete, given irodsFile is not a collection";
			log.error(msg);
			throw new JargonException(msg);
		}

		CollInp collInp = CollInp.instance(irodsFile.getAbsolutePath(),
				CollInp.RECURSIVE_OPERATION);

		Tag response = getIRODSProtocol().irodsFunction(CollInp.PI_TAG,
				collInp.getParsedTags(), CollInp.RMDIR_API_NBR);

		processClientStatusMessages(response);

		log.info("deletion successful");

	}

	/**
	 * @param irodsFile
	 * @return
	 * @throws JargonException
	 * @throws DataNotFoundException
	 */
	protected Resource getFileResource(final IRODSFile irodsFile)
			throws JargonException, DataNotFoundException {

		log.info("looking up resource");

		if (!irodsFile.isFile()) {
			String msg = "IRODSFileImpl is not a file, and has no associated resource, file:"
					+ irodsFile.getAbsolutePath();
			log.warn(msg);
			throw new DataNotFoundException(msg);
		}

		ResourceAO resourceAO = this.getIRODSAccessObjectFactory()
				.getResourceAO(getIRODSAccount());
		Resource resource = resourceAO.getFirstResourceForIRODSFile(irodsFile);
		log.debug("found resource for file:{}", resource);

		return resource;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.io.IRODSFileSystemAO#renameDirectory(org.irods
	 * .jargon.core.pub.io.IRODSFile, org.irods.jargon.core.pub.io.IRODSFile)
	 */
	@Override
	public void renameDirectory(final IRODSFile fromFile, final IRODSFile toFile)
			throws JargonException {
		log.info("renaming directory:{}", fromFile);
		log.info(" to:{}", toFile);

		if (!fromFile.isDirectory()) {
			String msg = "from file:" + fromFile.getAbsolutePath()
					+ " is not a directory";
			log.error(msg);
			throw new JargonException(msg);
		}

		DataObjCopyInp dataObjCopyInp = DataObjCopyInp
				.instanceForRenameCollection(fromFile.getAbsolutePath(),
						toFile.getAbsolutePath());
		Tag response = getIRODSProtocol().irodsFunction(DataObjInp.PI_TAG,
				dataObjCopyInp.getParsedTags(),
				DataObjCopyInp.RENAME_FILE_API_NBR);

		if (response != null) {
			log.warn("unexpected response from irods, expected null message - logged and ignored ");
		}

		log.debug("rename successful");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.io.IRODSFileSystemAO#renameFile(org.irods.jargon
	 * .core.pub.io.IRODSFile, org.irods.jargon.core.pub.io.IRODSFile)
	 */
	@Override
	public void renameFile(final IRODSFile fromFile, final IRODSFile toFile)
			throws JargonException {

		log.info("renaming file:{}", fromFile.getAbsolutePath());
		log.info(" to:{}", toFile.getAbsolutePath());

		if (!fromFile.isFile()) {
			String msg = "from file:" + fromFile.getAbsolutePath()
					+ " is not a file";
			log.error(msg);
			throw new JargonException(msg);
		}

		DataObjCopyInp dataObjCopyInp = DataObjCopyInp.instanceForRenameFile(
				fromFile.getAbsolutePath(), toFile.getAbsolutePath());
		Tag response = getIRODSProtocol().irodsFunction(DataObjInp.PI_TAG,
				dataObjCopyInp.getParsedTags(),
				DataObjCopyInp.RENAME_FILE_API_NBR);

		if (response != null) {
			log.warn("unexpected response from irods, expected null message - logged and ignored ");
		}

		log.debug("rename successful");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.io.IRODSFileSystemAO#getResourceNameForFile
	 * (org.irods.jargon.core.pub.io.IRODSFile)
	 */
	@Override
	public String getResourceNameForFile(final IRODSFile irodsFile)
			throws JargonException {

		if (irodsFile == null) {
			String msg = "null irodsFile";
			log.error(msg);
			throw new JargonException(msg);
		}

		log.info("getting resource for:{}", irodsFile.getAbsolutePath());

		String resource = "";

		if (!irodsFile.isFile()) {
			String msg = "this is not a file, does not have an associated resource";
			log.error(msg);
			throw new JargonException(msg);
		}

		ResourceAO resourceAO = this.getIRODSAccessObjectFactory()
				.getResourceAO(getIRODSAccount());
		try {
			resource = resourceAO.getFirstResourceForIRODSFile(irodsFile)
					.getName();
		} catch (DataNotFoundException e) {
			log.info("no resource found");
		}

		return resource;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.io.IRODSFileSystemAO#physicalMove(org.irods
	 * .jargon.core.pub.io.IRODSFile, java.lang.String)
	 */
	@Override
	public void physicalMove(final IRODSFile fromFile,
			final String targetResource) throws JargonException {

		if (fromFile == null) {
			throw new JargonException("from file is null");
		}

		if (targetResource == null || targetResource.length() == 0) {
			throw new JargonException("to resource is null or blank");
		}

		log.info("physical move of file:{}", fromFile.getAbsolutePath());
		log.info(" to resource:{}", targetResource);

		if (!fromFile.isFile()) {
			String msg = "from file:" + fromFile.getAbsolutePath()
					+ " is not a file";
			log.error(msg);
			throw new JargonException(msg);
		}

		physicalMove(fromFile.getAbsolutePath(), targetResource);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.io.IRODSFileSystemAO#physicalMove(java.lang
	 * .String, java.lang.String)
	 */
	@Override
	public void physicalMove(final String absolutePathToSourceFile,
			final String targetResource) throws JargonException {

		if (absolutePathToSourceFile == null
				|| absolutePathToSourceFile.isEmpty()) {
			throw new JargonException("null or empy absolutePathToSourceFile");
		}

		if (targetResource == null || targetResource.isEmpty()) {
			throw new JargonException("null or empty targetResource");
		}

		DataObjInp dataObjCopyInp = DataObjInp
				.instanceForPhysicalMoveSpecifyingResource(
						absolutePathToSourceFile, targetResource);
		Tag response = getIRODSProtocol()
				.irodsFunction(DataObjInp.PI_TAG,
						dataObjCopyInp.getParsedTags(),
						DataObjInp.PHYMOVE_FILE_API_NBR);

		if (response != null) {
			log.warn("unexpected response from irods, expected null message - logged and ignored ");
		}

		log.info("physical move successful");
	}

	/**
	 * Respond to client status messages for an operation until exhausted.
	 * FIXME: clean up and constants
	 * 
	 * @param reply
	 *            <code>Tag</code> containing status messages from IRODS
	 * @throws IOException
	 */
	private void processClientStatusMessages(final Tag reply)
			throws JargonException {

		boolean done = false;
		Tag ackResult = reply;

		while (!done) {
			if (ackResult.getLength() > 0) {
				if (ackResult.getName().equals(IRODSConstants.CollOprStat_PI)) {
					// formulate an answer status reply

					// if the total file count is 0, then I will continue and
					// send
					// the coll stat reply, otherwise, just ignore and
					// don't send the reply.

					Tag fileCountTag = ackResult.getTag("filesCnt");
					int fileCount = Integer.parseInt((String) fileCountTag
							.getValue());

					if (fileCount < IRODSConstants.SYS_CLI_TO_SVR_COLL_STAT_SIZE) {
						done = true;
					} else {
						this.getIRODSProtocol().sendInNetworkOrder(
								IRODSConstants.SYS_CLI_TO_SVR_COLL_STAT_REPLY);
						ackResult = getIRODSProtocol().readMessage();
					}
				}
			}
		}

	}

}
