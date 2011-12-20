/**
 * 
 */
package org.irods.jargon.core.pub.io;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.aohelper.AOHelper;
import org.irods.jargon.core.pub.aohelper.CollectionAOHelper;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.core.utils.IRODSDataConversionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper functions for the <code>IRODSFileSystemAO</code>, essentially to make
 * that class a bit more compact.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class IRODSFileSystemAOHelper extends AOHelper {

	static Logger log = LoggerFactory.getLogger(IRODSFileSystemAOHelper.class);

	/**
	 * List all directories under the path.
	 * 
	 * @param path
	 *            <code>String</code> with the absolute path to the iRODS parent
	 *            collection. If a data object is described by the path, the
	 *            parent collection of the data object will be used.
	 * @return
	 * @throws JargonException
	 */
	public static String buildQueryListAllCollections(final String path)
			throws JargonException {
		StringBuilder query;
		query = new StringBuilder();
		query.append("SELECT ");

		query.append(CollectionAOHelper
				.buildSelectsNeededForCollectionsInCollectionsAndDataObjectsListingEntry());

		query.append(" WHERE ");
		query.append(RodsGenQueryEnum.COL_COLL_PARENT_NAME.getName());
		query.append(" = '");
		query.append(IRODSDataConversionUtil.escapeSingleQuotes(path));
		query.append("'");

		if (log.isDebugEnabled()) {
			log.debug("query for dirs:" + query.toString());

		}
		return query.toString();
	}

	/**
	 * Build a query for all files under a path, adding extra information. Note
	 * that this query will return a list of all replicas.
	 * 
	 * @param path
	 *            <code>String</code> with the absolute path to a parent
	 *            directory
	 * @return <code>StringBuilder<code> that contains the query information, useful when building up queries with other elements.
	 * @throws JargonException
	 */
	public static String buildQueryListAllDataObjectsWithSizeAndDateInfo(
			final String path) throws JargonException {
		StringBuilder query = new StringBuilder();
		query.append("SELECT DISTINCT ");
		query.append(buildDataObjectQuerySelects());
		query.append(" WHERE ");
		query.append(RodsGenQueryEnum.COL_COLL_NAME.getName());
		query.append(" = '");
		query.append(IRODSDataConversionUtil.escapeSingleQuotes(path));
		query.append("'");

		if (log.isDebugEnabled()) {
			log.debug("query for files:" + query.toString());
		}

		return query.toString();

	}

	/**
	 * Build the GenQuery that lists all data objects and user access
	 * information.
	 * 
	 * @param path
	 *            <code>String</code> iwtht he
	 * @return
	 * @throws JargonException
	 */
	public static String buildQueryListAllDataObjectsWithUserAccessInfo(
			final String path) throws JargonException {
		StringBuilder query = new StringBuilder();
		query.append("SELECT DISTINCT ");
		query.append(buildDataObjectQuerySelects());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_USER_NAME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_DATA_ACCESS_USER_ID.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_DATA_ACCESS_TYPE.getName());
		query.append(",");
		query.append(RodsGenQueryEnum.COL_USER_TYPE.getName());
		query.append(" WHERE ");
		query.append(RodsGenQueryEnum.COL_COLL_NAME.getName());
		query.append(" = '");
		query.append(IRODSDataConversionUtil.escapeSingleQuotes(path));
		query.append("'");

		if (log.isDebugEnabled()) {
			log.debug("query for files:" + query.toString());
		}

		return query.toString();

	}

	/**
	 * Build the necessary GenQuery selects (the select statement is not added
	 * here) to query data objects for information. Used in many common queries
	 * for listing data objects, as in an ils-like command.
	 * 
	 * @return <code>String</code> with GenQuery select values. Note that the
	 *         'SELECT' statement itself is not appended here
	 */
	public static String buildDataObjectQuerySelects() {
		StringBuilder query = new StringBuilder();
		query.append(RodsGenQueryEnum.COL_COLL_NAME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_DATA_NAME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_D_CREATE_TIME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_D_MODIFY_TIME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_D_DATA_ID.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_DATA_SIZE.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_DATA_REPL_NUM.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_D_OWNER_NAME.getName());
		return query.toString();

	}

	/**
	 * Build a query for all files under a path. Note that files that are
	 * replicated are only returned once.
	 * 
	 * @param path
	 *            <code>String</code> with the absolute path to a parent
	 *            directory
	 * @return <code>StringBuilder<code> that contains the query information, useful when building up queries with other elements.
	 * @throws JargonException
	 */
	public static String buildQueryListAllFiles(final String path)
			throws JargonException {
		StringBuilder query = new StringBuilder();
		query.append("SELECT DISTINCT ");

		query.append(RodsGenQueryEnum.COL_COLL_NAME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_DATA_NAME.getName());

		query.append(" WHERE ");
		query.append(RodsGenQueryEnum.COL_COLL_NAME.getName());
		query.append(" = '");
		query.append(IRODSDataConversionUtil.escapeSingleQuotes(path));
		query.append("'");

		log.debug("query for files:{}", query.toString());

		return query.toString();

	}

	/**
	 * Build the appropriate GenQuery when listing collections under a given
	 * path, including the user access information
	 * 
	 * @param path
	 *            <code>String</code> with the absolute path to the iRODS
	 *            collection
	 * @return
	 */
	public static String buildQueryListAllDirsWithUserAccessInfo(
			final String path) {
		StringBuilder query;
		query = new StringBuilder();
		query.append("SELECT ");
		// 7 columns from the method
		query.append(CollectionAOHelper
				.buildSelectsNeededForCollectionsInCollectionsAndDataObjectsListingEntry());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_COLL_ACCESS_TYPE.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_COLL_ACCESS_USER_ID.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_COLL_ACCESS_USER_NAME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_COLL_ACCESS_USER_ZONE.getName());
		query.append(" WHERE ");
		query.append(RodsGenQueryEnum.COL_COLL_PARENT_NAME.getName());
		query.append(" = '");
		query.append(IRODSDataConversionUtil.escapeSingleQuotes(path));
		query.append("'");

		log.debug("query for dirs:{}", query.toString());
		return query.toString();
	}
}
