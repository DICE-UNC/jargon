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
	public static String buildQueryListAllDirs(final String path)
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
	public static String buildQueryListAllFilesWithSizeAndDateInfo(
			final String path) throws JargonException {
		StringBuilder query = new StringBuilder();
		query.append("SELECT DISTINCT ");

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

	public static String buildQueryListAllDirsWithUserAccessInfo(String path,
			String id) {
		StringBuilder query;
		query = new StringBuilder();
		query.append("SELECT ");

		query.append(CollectionAOHelper
				.buildSelectsNeededForCollectionsInCollectionsAndDataObjectsListingEntry());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_COLL_ACCESS_TYPE.getName());
		query.append(" WHERE ");
		query.append(RodsGenQueryEnum.COL_COLL_PARENT_NAME.getName());
		query.append(" = '");
		query.append(IRODSDataConversionUtil.escapeSingleQuotes(path)); 
		query.append("'");
		query.append("  AND ");
		query.append(RodsGenQueryEnum.COL_COLL_ACCESS_USER_ID.getName());
		query.append(" = '");
		query.append(id); 
		query.append("'");

		log.debug("query for dirs:{}", query.toString());
		return query.toString();
	}
}
