/**
 * 
 */
package org.irods.jargon.core.pub.io;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonFileOrCollAlreadyExistsException;
import org.irods.jargon.core.packinstr.CollInp;
import org.irods.jargon.core.packinstr.DataObjCloseInp;
import org.irods.jargon.core.packinstr.DataObjCopyInp;
import org.irods.jargon.core.packinstr.DataObjInp;
import org.irods.jargon.core.packinstr.MsgHeader;
import org.irods.jargon.core.pub.IRODSGenQueryExecutor;
import org.irods.jargon.core.pub.IRODSGenQueryExecutorImpl;
import org.irods.jargon.core.pub.IRODSGenericAO;
import org.irods.jargon.core.pub.ResourceAO;
import org.irods.jargon.core.pub.ResourceAOImpl;
import org.irods.jargon.core.pub.domain.Resource;
import org.irods.jargon.core.query.IRODSGenQuery;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSet;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.core.utils.IRODSDataConversionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.sdsc.grid.io.irods.Tag;

/**
 * This is a backing object for IRODSFileImpl, handling all IRODS interactions.
 * This class is usable as API, but methods are more properly called from
 * IRODSFile, which wraps these operations with the <code>java.io.File</code>
 * methods. Methods that back operations not defined in
 * <code>java.io.file</code> are not implemented in this particular access
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

	/**
	 * @param irodsProtocol
	 * @throws JargonException
	 */
	public IRODSFileSystemAOImpl(final IRODSSession irodsSession,
			final IRODSAccount irodsAccount) throws JargonException {
		super(irodsSession, irodsAccount);
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
		boolean readable = false;
		if (irodsFile == null) {
			throw new JargonException("irods file is null");
		}
		if (log.isInfoEnabled()) {
			log.info("checking read permissions on:" + irodsFile);
		}

		int filePermissions = 0;

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
	 * org.irods.jargon.core.pub.io.IRODSFileSystemAO#isFileWriteable(org.irods
	 * .jargon.core.pub.io.IRODSFile)
	 */
	@Override
	public boolean isFileWriteable(final IRODSFile irodsFile)
			throws JargonException {
		boolean writeable = false;
		if (irodsFile == null) {
			throw new JargonException("irods file is null");
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

	/**
	 * @param irodsFile
	 * @return
	 * @throws JargonException
	 */
	int getFilePermissions(final IRODSFile irodsFile) throws JargonException {
		if (irodsFile == null) {
			throw new JargonException("irods file is null");
		}
		if (log.isInfoEnabled()) {
			log.info("checking permissions on:" + irodsFile);
		}
		String zone = this.getIRODSServerProperties().getRodsZone();
		String parent = irodsFile.getParent();
		String fileName = irodsFile.getName();
		String userName = this.getIRODSAccount().getUserName();

		if (log.isDebugEnabled()) {
			log.debug("getting file permissions on file " + fileName);
			log.debug("  parent dir:" + parent);
			log.debug("   for user:" + userName);
			log.debug("   in zone:" + zone);

		}

		IRODSGenQueryExecutor irodsGenQueryExecutor = new IRODSGenQueryExecutorImpl(
				this.getIRODSSession(), this.getIRODSAccount());

		StringBuilder filePermissionQuery = buildPermisionsQueryFile(zone,
				parent, fileName, userName);

		if (log.isDebugEnabled()) {
			log.debug("query for user permissions ="
					+ filePermissionQuery.toString());
		}

		int highestPermissionValue = extractHighestPermission(
				irodsGenQueryExecutor, filePermissionQuery);

		if (log.isDebugEnabled()) {
			log.debug("highest permission value:" + highestPermissionValue);
		}

		return highestPermissionValue;
	}

	/**
	 * @param irodsGenQueryExecutor
	 * @param filePermissionQuery
	 * @return
	 * @throws JargonException
	 */
	private int extractHighestPermission(
			final IRODSGenQueryExecutor irodsGenQueryExecutor,
			final StringBuilder filePermissionQuery) throws JargonException {
		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(
				filePermissionQuery.toString(), 500);

		IRODSQueryResultSetInterface resultSet;
		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQuery(irodsQuery, 0);
		} catch (JargonQueryException e) {
			log.error(
					"query exception for  query:"
							+ filePermissionQuery.toString(), e);
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
	 * @param userName
	 * @return
	 */
	private StringBuilder buildPermisionsQueryFile(final String zone,
			final String parent, final String fileName, final String userName)
			throws JargonException {
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

		filePermissionQuery.append(RodsGenQueryEnum.COL_USER_NAME.getName());
		filePermissionQuery.append(" = '");
		filePermissionQuery.append(userName);
		filePermissionQuery.append("'");

		filePermissionQuery.append(AND_VALUE);
		filePermissionQuery.append(RodsGenQueryEnum.COL_USER_ZONE.getName());
		filePermissionQuery.append(" = '");
		filePermissionQuery.append(zone);
		filePermissionQuery.append("'");
		return filePermissionQuery;
	}

	int getDirectoryPermissions(final IRODSFile irodsFile)
			throws JargonException {
		if (irodsFile == null) {
			throw new JargonException("irods file is null");
		}
		if (log.isInfoEnabled()) {
			log.info("checking directory permissions on:" + irodsFile);
		}

		String zone = this.getIRODSServerProperties().getRodsZone();
		String dir = irodsFile.getAbsolutePath();
		String fileName = irodsFile.getName();
		String userName = this.getIRODSAccount().getUserName();

		if (log.isDebugEnabled()) {
			log.debug("getting directory permissions on: " + fileName);
			log.debug("   dir:" + dir);
			log.debug("   for user:" + userName);
			log.debug("   in zone:" + zone);

		}

		IRODSGenQueryExecutor irodsGenQueryExecutor = new IRODSGenQueryExecutorImpl(
				this.getIRODSSession(), this.getIRODSAccount());
		StringBuilder filePermissionQuery = buildPermissionsQueryDirectory(
				zone, dir, userName);

		if (log.isDebugEnabled()) {
			log.debug("query for user permissions ="
					+ filePermissionQuery.toString());
		}

		int highestPermissionValue = extractHighestPermission(
				irodsGenQueryExecutor, filePermissionQuery);

		return highestPermissionValue;
	}

	/**
	 * @param zone
	 * @param dir
	 * @param userName
	 * @return
	 */
	private StringBuilder buildPermissionsQueryDirectory(final String zone,
			final String dir, final String userName) throws JargonException {
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

		filePermissionQuery.append(RodsGenQueryEnum.COL_COLL_OWNER_NAME
				.getName());
		filePermissionQuery.append(" = '");
		filePermissionQuery.append(userName);
		filePermissionQuery.append("'");

		filePermissionQuery.append(AND_VALUE);
		filePermissionQuery.append(RodsGenQueryEnum.COL_COLL_OWNER_ZONE
				.getName());
		filePermissionQuery.append(" = '");
		filePermissionQuery.append(zone);
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
		if (irodsFile == null) {
			throw new JargonException("irods file is null");
		}

		log.info("checking existence of: {}", irodsFile.getAbsolutePath());

		String parent = irodsFile.getParent();
		String fileName = irodsFile.getName();

		IRODSGenQueryExecutor irodsGenQueryExecutor = new IRODSGenQueryExecutorImpl(
				this.getIRODSSession(), this.getIRODSAccount());
		StringBuilder filePermissionQuery = new StringBuilder();
		filePermissionQuery.append("SELECT ");
		filePermissionQuery.append(RodsGenQueryEnum.COL_DATA_NAME.getName());

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

		if (log.isDebugEnabled()) {
			log.debug("query for exists = {}", filePermissionQuery.toString());
		}

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(
				filePermissionQuery.toString(), 500);

		IRODSQueryResultSetInterface resultSet;
		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQuery(irodsQuery, 0);
		} catch (JargonQueryException e) {
			log.error("query exception for  query: {}",
					filePermissionQuery.toString(), e);
			throw new JargonException("error in exists query");
		}

		// is it a file?
		if (resultSet.getResults().size() > 0) {
			return true;
		}

		log.debug("does not exist as a file, checking as a dir");

		filePermissionQuery = new StringBuilder();
		filePermissionQuery.append("SELECT ");
		filePermissionQuery.append(RodsGenQueryEnum.COL_COLL_NAME.getName());

		filePermissionQuery.append(" WHERE ");

		filePermissionQuery.append(RodsGenQueryEnum.COL_COLL_NAME.getName());
		filePermissionQuery.append(" = '");
		filePermissionQuery.append(IRODSDataConversionUtil
				.escapeSingleQuotes(irodsFile.getAbsolutePath()));
		filePermissionQuery.append("'");
		irodsQuery = IRODSGenQuery.instance(filePermissionQuery.toString(), 500);

		log.debug("query for exists = {}", filePermissionQuery.toString());

		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQuery(irodsQuery, 0);
		} catch (JargonQueryException e) {
			log.error("query exception for  query:{}",
					filePermissionQuery.toString(), e);
			throw new JargonException("error in exists query");
		}

		// is it a dir?
		if (resultSet.getResults().size() > 0) {
			log.info("this is a directory");
			return true;
		}

		return false;

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
			throws JargonException, DataNotFoundException {
		if (irodsFile == null) {
			throw new JargonException("irods file is null");
		}

		if (log.isInfoEnabled()) {
			log.info("checking if directory, file:" + irodsFile);
		}

		IRODSGenQueryExecutor irodsGenQueryExecutor = new IRODSGenQueryExecutorImpl(
				this.getIRODSSession(), this.getIRODSAccount());
		StringBuilder filePermissionQuery = new StringBuilder();
		filePermissionQuery.append("SELECT ");
		filePermissionQuery.append(RodsGenQueryEnum.COL_COLL_NAME.getName());
		filePermissionQuery.append(" WHERE ");
		filePermissionQuery.append(RodsGenQueryEnum.COL_COLL_NAME.getName());
		filePermissionQuery.append(" = '");
		filePermissionQuery.append(IRODSDataConversionUtil
				.escapeSingleQuotes(irodsFile.getAbsolutePath()));
		filePermissionQuery.append("'");

		if (log.isDebugEnabled()) {
			log.debug("query for directory =" + filePermissionQuery.toString());
		}

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(
				filePermissionQuery.toString(), 100); 
		
		IRODSQueryResultSetInterface resultSet;
		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQuery(irodsQuery, 0);

			if (resultSet.isHasMoreRecords()) {
				log.info("I have more records for query:{}",
						irodsQuery.getQueryString());
			}

		} catch (JargonQueryException e) {
			log.error(
					"query exception for  query:"
							+ filePermissionQuery.toString(), e);
			throw new JargonException("error in exists query");
		}

		if (resultSet.getResults().size() == 0) {
			log.debug("not a directory");
			return false;
		} else {
			log.debug("this is a directory");
			return true;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.io.IRODSFileSystemAO#getModificationDate(org
	 * .irods.jargon.core.pub.io.IRODSFile)
	 */
	@Override
	public long getModificationDate(final IRODSFile irodsFile)
			throws JargonException, DataNotFoundException {
		if (irodsFile == null) {
			throw new JargonException("irods file is null");
		}

		log.debug("getting modification date");

		IRODSGenQueryExecutor irodsGenQueryExecutor = new IRODSGenQueryExecutorImpl(
				this.getIRODSSession(), this.getIRODSAccount());
		StringBuilder query = new StringBuilder();
		query.append("SELECT ");

		if (irodsFile.isDirectory()) {
			query.append(RodsGenQueryEnum.COL_COLL_MODIFY_TIME.getName());
			query.append(" WHERE ");
			query.append(RodsGenQueryEnum.COL_COLL_NAME.getName());
			query.append(" = '");
			query.append(IRODSDataConversionUtil.escapeSingleQuotes(irodsFile
					.getAbsolutePath()));
			query.append("'");
		} else {
			query.append(RodsGenQueryEnum.COL_D_MODIFY_TIME.getName());
			query.append(" WHERE ");
			query.append(RodsGenQueryEnum.COL_DATA_NAME.getName());
			query.append(" = '");
			query.append(IRODSDataConversionUtil.escapeSingleQuotes(irodsFile
					.getName()));
			query.append("'");
			query.append(" AND ");
			query.append(RodsGenQueryEnum.COL_COLL_NAME.getName());
			query.append(" = '");
			query.append(IRODSDataConversionUtil.escapeSingleQuotes(irodsFile
					.getParent()));
			query.append("'");
		}

		if (log.isDebugEnabled()) {
			log.debug("query for mod date =" + query.toString());
		}

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(query.toString(), 500);

		IRODSQueryResultSetInterface resultSet;
		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQuery(irodsQuery, 0);
		} catch (JargonQueryException e) {
			log.error("query exception for  query:" + query.toString(), e);
			throw new JargonException("error in exists query");
		}

		long dateVal = Long.parseLong(resultSet.getFirstResult().getColumn(0));
		return dateVal * 1000;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.io.IRODSFileSystemAO#getLength(org.irods.jargon
	 * .core.pub.io.IRODSFile)
	 */
	@Override
	public long getLength(final IRODSFile irodsFile) throws JargonException,
			DataNotFoundException {
		if (irodsFile == null) {
			throw new JargonException("irods file is null");
		}

		if (irodsFile.isDirectory()) {
			log.debug("is directory, length returns as zero");
			return 0;
		}

		log.debug("getting length");

		IRODSGenQueryExecutor irodsGenQueryExecutor = new IRODSGenQueryExecutorImpl(
				this.getIRODSSession(), this.getIRODSAccount());
		StringBuilder query = new StringBuilder();
		query.append("SELECT ");

		query.append(RodsGenQueryEnum.COL_DATA_SIZE.getName());
		query.append(" WHERE ");
		query.append(RodsGenQueryEnum.COL_DATA_NAME.getName());
		query.append(" = '");
		query.append(IRODSDataConversionUtil.escapeSingleQuotes(irodsFile
				.getName()));
		query.append("'");
		query.append(" AND ");
		query.append(RodsGenQueryEnum.COL_COLL_NAME.getName());
		query.append(" = '");
		query.append(IRODSDataConversionUtil.escapeSingleQuotes(irodsFile
				.getParent()));
		query.append("'");

		if (log.isDebugEnabled()) {
			log.debug("query for size =" + query.toString());
		}

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(query.toString(), 500);

		IRODSQueryResultSetInterface resultSet;
		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQuery(irodsQuery, 0);
		} catch (JargonQueryException e) {
			log.error("query exception for  query:" + query.toString(), e);
			throw new JargonException("error in exists query");
		}

		long size = Long.parseLong(resultSet.getFirstResult().getColumn(0));
		return size;
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
			throws JargonException, DataNotFoundException {
		if (irodsFile == null) {
			throw new JargonException("irods file is null");
		}

		List<String> subdirs = new ArrayList<String>();
		String path = "";

		if (irodsFile.isDirectory()) {
			path = irodsFile.getAbsolutePath();
		} else {
			path = irodsFile.getParent();
		}

		if (log.isDebugEnabled()) {
			log.debug("path for query:{}", path);

		}

		// get all the files

		IRODSGenQueryExecutor irodsGenQueryExecutor = new IRODSGenQueryExecutorImpl(
				this.getIRODSSession(), this.getIRODSAccount());

		StringBuilder query = new StringBuilder();
		query.append(IRODSFileSystemAOHelper.buildQueryListAllFiles(path));

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(query.toString(), 5000);

		IRODSQueryResultSet resultSet;

		/*
		 * accumulate all results to return by repeatedly requerying until no
		 * more results
		 */

		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQuery(irodsQuery, 0);
			for (IRODSQueryResultRow row : resultSet.getResults()) {
				subdirs.add(row.getColumn(1));
			}

			while (resultSet.isHasMoreRecords()) {
				log.debug("more results to get for listing files, requerying");
				resultSet = irodsGenQueryExecutor.getMoreResults(resultSet);
				for (IRODSQueryResultRow row : resultSet.getResults()) {
					subdirs.add(row.getColumn(1));
				}
			}

		} catch (JargonQueryException e) {
			log.error("query exception for  query:" + query.toString(), e);
			throw new JargonException("error in exists query");
		}

		// get all the sub-directories

		query = new StringBuilder();
		query.append(IRODSFileSystemAOHelper.buildQueryListAllDirs(path));

		irodsQuery = IRODSGenQuery.instance(query.toString(), 5000);

		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQuery(irodsQuery, 0);
			for (IRODSQueryResultRow row : resultSet.getResults()) {
				processListDirsResultRowForCollection(subdirs, row);
			}

			while (resultSet.isHasMoreRecords()) {
				log.debug("more results to get for listing collections, requerying");
				resultSet = irodsGenQueryExecutor.getMoreResults(resultSet);
				for (IRODSQueryResultRow row : resultSet.getResults()) {
					processListDirsResultRowForCollection(subdirs, row);
				}

			}
		} catch (JargonQueryException e) {
			log.error("query exception for  query:" + query.toString(), e);
			throw new JargonException("error in exists query");
		}

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
		idxLastSlash = row.getColumn(0).lastIndexOf('/');
		subdirs.add(row.getColumn(0).substring(idxLastSlash));
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

		if (log.isDebugEnabled()) {
			log.debug("path for query:" + path);

		}

		// get all the files

		IRODSGenQueryExecutor irodsGenQueryExecutor = new IRODSGenQueryExecutorImpl(
				this.getIRODSSession(), this.getIRODSAccount());
		StringBuilder query = new StringBuilder(
				IRODSFileSystemAOHelper.buildQueryListAllFiles(path));

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(query.toString(), 5000);

		IRODSQueryResultSet resultSet;

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
			log.error("query exception for  query:" + query.toString(), e);
			throw new JargonException("error in exists query");
		}

		// get all the sub-directories

		query = new StringBuilder();
		query.append(IRODSFileSystemAOHelper.buildQueryListAllDirs(path));

		irodsQuery = IRODSGenQuery.instance(query.toString(), 5000);

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
			log.error("query exception for  query:" + query.toString(), e);
			throw new JargonException("error in exists query");
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
	public List<String> getListInDirWithFileFilter(final IRODSFile irodsFile,
			final FileFilter fileFilter) throws JargonException,
			DataNotFoundException {
		if (irodsFile == null) {
			throw new JargonException("irods file is null");
		}

		if (fileFilter == null) {
			throw new JargonException("file filter is null");
		}

		List<String> subdirs = new ArrayList<String>();
		String path = "";

		if (irodsFile.isDirectory()) {
			path = irodsFile.getAbsolutePath();
		} else {
			path = irodsFile.getParent();
		}

		if (log.isDebugEnabled()) {
			log.debug("path for query:" + path);

		}

		IRODSGenQueryExecutor irodsGenQueryExecutor = new IRODSGenQueryExecutorImpl(
				this.getIRODSSession(), this.getIRODSAccount());
		StringBuilder query = new StringBuilder(
				IRODSFileSystemAOHelper.buildQueryListAllFiles(path));

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(query.toString(), 5000);

		IRODSQueryResultSet resultSet;

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
			log.error("query exception for  query:" + query.toString(), e);
			throw new JargonException("error in exists query");
		}

		// get all the sub-directories

		query = new StringBuilder();
		query.append(IRODSFileSystemAOHelper.buildQueryListAllDirs(path));

		irodsQuery = IRODSGenQuery.instance(query.toString(), 5000);

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
			log.error("query exception for  query:" + query.toString(), e);
			throw new JargonException("error in exists query");
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
			final FileFilter fileFilter, final List<String> subdirs,
			final IRODSQueryResultRow row) throws JargonException {
		String thisFileDir;
		// this is a dir, does it pass the file name filter?
		thisFileDir = row.getColumn(0);
		if (fileFilter.accept(new File(thisFileDir))) {
			subdirs.add(row.getColumn(0));
		}
	}

	/**
	 * @param fileFilter
	 * @param subdirs
	 * @param row
	 * @throws JargonException
	 */
	private void processFileRowWhenListFilesWithFileFilter(
			final FileFilter fileFilter, final List<String> subdirs,
			final IRODSQueryResultRow row) throws JargonException {
		// this is a file, does it pass the file name filter?
		String thisFileDir = row.getColumn(0);
		if (fileFilter.accept(new File(thisFileDir))) {
			subdirs.add(row.getColumn(1));
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
	public IRODSFileImpl.DataType getFileDataType(final IRODSFile irodsFile)
			throws JargonException {

		if (irodsFile == null) {
			throw new JargonException("irods file is null");
		}
		if (log.isInfoEnabled()) {
			log.info("checking data type on:" + irodsFile);
		}

		if (irodsFile.isDirectory()) {
			return IRODSFile.DataType.DIRECTORY;
		}

		// not a directory, look up the type
		StringBuilder query = new StringBuilder();
		query.append("SELECT ");
		query.append(RodsGenQueryEnum.COL_DATA_TYPE_NAME.getName());
		query.append(" WHERE ");
		query.append(RodsGenQueryEnum.COL_DATA_NAME.getName());
		query.append(" = '");
		query.append(IRODSDataConversionUtil.escapeSingleQuotes(irodsFile
				.getName()));
		query.append("'");
		query.append(" AND ");
		query.append(RodsGenQueryEnum.COL_COLL_NAME.getName());
		query.append(" = '");
		query.append(IRODSDataConversionUtil.escapeSingleQuotes(irodsFile
				.getParent()));
		query.append("'");

		if (log.isDebugEnabled()) {
			log.debug("query for type =" + query.toString());
		}

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(query.toString(), 500);
		IRODSGenQueryExecutor irodsGenQueryExecutor = new IRODSGenQueryExecutorImpl(
				this.getIRODSSession(), this.getIRODSAccount());

		IRODSQueryResultSetInterface resultSet;
		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQuery(irodsQuery, 0);
		} catch (JargonQueryException e) {
			log.error("query exception for  query:" + query.toString(), e);
			throw new JargonException("error in exists query");
		}

		try {
			String type = resultSet.getFirstResult().getColumn(0);
			if (log.isDebugEnabled()) {
				log.debug("returned data type:" + type);
			}

			if (type.equals("generic")) {
				return IRODSFile.DataType.GENERIC;
			} else {
				return IRODSFile.DataType.UNKNOWN;
			}
		} catch (DataNotFoundException e) {
			log.debug("data not found, returning unknown type");
			return IRODSFile.DataType.UNKNOWN;
		}
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

		if (log.isDebugEnabled()) {
			log.debug("setting resource to account default:" + defaultResource);
		}

		int fileId = 0;

		try {
			fileId = createFileInResource(absolutePath, openFlags, createMode,
					defaultResource);
		} catch (JargonFileOrCollAlreadyExistsException jfcae) {
			log.error("file or collection already exists");
			throw jfcae;
		} catch (JargonException je) {
			log.warn("jargon exception creating file", je);
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

		if (log.isDebugEnabled()) {
			log.debug("file id for opened file:" + fileId);
		}
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

		long offset = 0L;
		long dataSize = 0L;

		int responseFileNbr = 0;

		try {
			DataObjInp dataObjInp = DataObjInp.instance(absolutePath,
					createMode, openFlags, offset, dataSize, resource, null);
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
		if (log.isDebugEnabled()) {
			log.debug("response file nbr:" + responseFileNbr);
		}
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

		if (log.isInfoEnabled()) {
			log.info("making dir for:" + irodsFile.getAbsolutePath());
		}

		CollInp collInp = CollInp.instance(irodsFile.getAbsolutePath(),
				recursiveOpr);

		Tag response = getIRODSProtocol().irodsFunction(CollInp.PI_TAG,
				collInp.getParsedTags(), CollInp.MKDIR_API_NBR);

		if (response != null) {
			log.warn("expected null response to mkdir, logged but not an error, received:"
					+ response.parseTag());
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

		if (log.isInfoEnabled()) {
			log.info("closing file:" + fileDescriptor);
		}

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
			log.warn("expected null response to close, logged but not an error, received:"
					+ response.parseTag());
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

		if (log.isInfoEnabled()) {
			log.info("deleting:" + irodsFile.getAbsolutePath());
		}

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

		if (log.isInfoEnabled()) {
			log.info("deleting without force option:"
					+ irodsFile.getAbsolutePath());
		}

		if (!irodsFile.isFile()) {
			String msg = "file delete, given irodsFile is not a file";
			log.error(msg);
			throw new JargonException(msg);
		}

		DataObjInp dataObjInp = DataObjInp
				.instanceForDeleteWithNoForce(irodsFile.getAbsolutePath());
		Tag response = getIRODSProtocol().irodsFunction(dataObjInp);

		if (response != null) {
			String msg = "unexpected response from irods, expected null message - logged and ignored ";
			log.warn(msg);
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

		if (log.isInfoEnabled()) {
			log.info("deleting:" + irodsFile.getAbsolutePath());
		}

		if (!irodsFile.isDirectory()) {
			String msg = "directory delete, given irodsFile is not a collection";
			log.error(msg);
			throw new JargonException(msg);
		}

		CollInp collInp = CollInp.instance(irodsFile.getAbsolutePath(),
				CollInp.RECURSIVE_OPERATION, CollInp.FORCE_OPERATION);

		Tag response = getIRODSProtocol().irodsFunction(CollInp.PI_TAG,
				collInp.getParsedTags(), CollInp.RMDIR_API_NBR);

		if (response != null) {
			String msg = "unexpected response from irods, expected null message - logged and ignored ";
			log.warn(msg);
		}

		log.info("deletion successful");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.io.IRODSFileSystemAO#directoryDeleteNoForce
	 * (org.irods.jargon.core.pub.io.IRODSFile)
	 */
	public void directoryDeleteNoForce(final IRODSFile irodsFile)
			throws JargonException {
		if (irodsFile == null) {
			throw new JargonException("irodsFile is null");
		}

		if (log.isInfoEnabled()) {
			log.info("deleting:" + irodsFile.getAbsolutePath());
		}

		if (!irodsFile.isDirectory()) {
			String msg = "directory delete, given irodsFile is not a collection";
			log.error(msg);
			throw new JargonException(msg);
		}

		CollInp collInp = CollInp.instance(irodsFile.getAbsolutePath(),
				CollInp.RECURSIVE_OPERATION);

		Tag response = getIRODSProtocol().irodsFunction(CollInp.PI_TAG,
				collInp.getParsedTags(), CollInp.RMDIR_API_NBR);

		if (response != null) {
			String msg = "unexpected response from irods, expected null message - logged and ignored ";
			log.warn(msg);
		}

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

		ResourceAO resourceAO = new ResourceAOImpl(this.getIRODSSession(),
				this.getIRODSAccount());
		Resource resource = resourceAO.getFirstResourceForIRODSFile(irodsFile);
		if (log.isDebugEnabled()) {
			log.debug("found resource for file:" + resource);
		}

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
		if (log.isInfoEnabled()) {
			log.info("renaming directory:" + fromFile + " to:" + toFile);
		}

		if (!fromFile.isDirectory()) {
			String msg = "from file:" + fromFile.getAbsolutePath()
					+ " is not a directory";
			log.error(msg);
			throw new JargonException(msg);
		}

		DataObjCopyInp dataObjCopyInp = DataObjCopyInp.instance(
				fromFile.getAbsolutePath(), toFile.getAbsolutePath(),
				DataObjInp.RENAME_DIRECTORY_OPERATION_TYPE);
		Tag response = getIRODSProtocol().irodsFunction(DataObjInp.PI_TAG,
				dataObjCopyInp.getParsedTags(),
				DataObjCopyInp.RENAME_FILE_API_NBR);

		if (response != null) {
			String msg = "unexpected response from irods, expected null message - logged and ignored ";
			log.warn(msg);
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
		if (log.isInfoEnabled()) {
			log.info("renaming file:" + fromFile.getAbsolutePath() + " to:"
					+ toFile.getAbsolutePath());
		}

		if (!fromFile.isFile()) {
			String msg = "from file:" + fromFile.getAbsolutePath()
					+ " is not a file";
			log.error(msg);
			throw new JargonException(msg);
		}

		DataObjCopyInp dataObjCopyInp = DataObjCopyInp.instance(
				fromFile.getAbsolutePath(), toFile.getAbsolutePath(),
				DataObjInp.RENAME_FILE_OPERATION_TYPE);
		Tag response = getIRODSProtocol().irodsFunction(DataObjInp.PI_TAG,
				dataObjCopyInp.getParsedTags(),
				DataObjCopyInp.RENAME_FILE_API_NBR);

		if (response != null) {
			String msg = "unexpected response from irods, expected null message - logged and ignored ";
			log.warn(msg);
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

		if (log.isInfoEnabled()) {
			log.info("getting resource for:" + irodsFile.getAbsolutePath());
		}

		String resource = "";

		if (!irodsFile.isFile()) {
			String msg = "this is not a file, does not have an associated resource";
			log.error(msg);
			throw new JargonException(msg);
		}

		ResourceAO resourceAO = new ResourceAOImpl(this.getIRODSSession(),
				this.getIRODSAccount());
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

		if (log.isInfoEnabled()) {
			log.info("physical move of file:" + fromFile.getAbsolutePath()
					+ " to resource:" + targetResource);
		}

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
			String msg = "unexpected response from irods, expected null message - logged and ignored ";
			log.warn(msg);
		}

		log.debug("physical move successful");
	}

}
