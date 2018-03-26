/**
 *
 */
package org.irods.jargon.core.pub.io;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.aohelper.AOHelper;
import org.irods.jargon.core.pub.aohelper.CollectionAOHelper;
import org.irods.jargon.core.query.GenQueryBuilderException;
import org.irods.jargon.core.query.IRODSGenQueryBuilder;
import org.irods.jargon.core.query.QueryConditionOperators;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper functions for the {@code IRODSFileSystemAO}, essentially to make
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
	 *            {@code String} with the absolute path to the iRODS parent
	 *            collection. If a data object is described by the path, the
	 *            parent collection of the data object will be used.
	 * @param builder
	 *            {@link IRODSGenQueryBuilder} that will be augmented with
	 *            necessary conditions and selects
	 * @throws GenQueryBuilderException
	 */
	public static void buildQueryListAllCollections(final String path,
			final IRODSGenQueryBuilder builder) throws GenQueryBuilderException {

		if (path == null || path.isEmpty()) {
			throw new IllegalArgumentException("null or empty path");
		}

		if (builder == null) {
			throw new IllegalArgumentException("null builder");
		}

		CollectionAOHelper
		.buildSelectsNeededForCollectionsInCollectionsAndDataObjectsListingEntry(builder);
		builder.addConditionAsGenQueryField(
				RodsGenQueryEnum.COL_COLL_PARENT_NAME,
				QueryConditionOperators.EQUAL, path);
	}

	/**
	 * Build a query for all files under a path, adding extra information. Note
	 * that this query will return a list of all replicas.
	 *
	 * @param path
	 *            {@code String} with the absolute path to the iRODS parent
	 *            collection.
	 * @param builder
	 *            {@link IRODSGenQueryBuilder} that will be augmented with
	 *            necessary conditions and selects
	 * @throws JargonException
	 */
	public static void buildQueryListAllDataObjectsWithSizeAndDateInfo(
			final String path, final IRODSGenQueryBuilder builder)
					throws JargonException {

		if (path == null || path.isEmpty()) {
			throw new IllegalArgumentException("null or empty path");
		}

		if (builder == null) {
			throw new IllegalArgumentException("null builder");
		}

		try {
			buildDataObjectQuerySelects(builder);
		} catch (GenQueryBuilderException e) {
			throw new JargonException("exception building query", e);
		}

		builder.addConditionAsGenQueryField(RodsGenQueryEnum.COL_COLL_NAME,
				QueryConditionOperators.EQUAL, path);

	}

	/**
	 * Build the gen query to list all data objects, including user access
	 * information. The selects and conditions are appended to the provided
	 * builder
	 *
	 * @param path
	 *            {@code String} with the parent directory absolute path
	 * @param builder
	 *            {@link IRODSGenQueryBuilder}
	 * @throws JargonException
	 */
	public static void buildQueryListAllDataObjectsWithUserAccessInfo(
			final String path, final IRODSGenQueryBuilder builder)
					throws JargonException {

		if (path == null || path.isEmpty()) {
			throw new IllegalArgumentException("null or empty path");
		}

		if (builder == null) {
			throw new IllegalArgumentException("null builder");
		}

		try {
			buildDataObjectQuerySelects(builder);
			builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_USER_NAME)
			.addSelectAsGenQueryValue(
					RodsGenQueryEnum.COL_DATA_ACCESS_USER_ID)
					.addSelectAsGenQueryValue(
							RodsGenQueryEnum.COL_DATA_ACCESS_TYPE)
							.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_USER_TYPE)
							.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_USER_ZONE)
							.addConditionAsGenQueryField(
									RodsGenQueryEnum.COL_COLL_NAME,
									QueryConditionOperators.EQUAL, path);
		} catch (GenQueryBuilderException e) {
			throw new JargonException("query exception", e);
		}
	}

	/**
	 * Build the necessary GenQuery selects to query data objects for
	 * information. Used in many common queries for listing data objects, as in
	 * an ils-like command.
	 *
	 * @param builder
	 *            {@link IRODSGenQueryBuilder} that will be augmented with the
	 *            necessary selects
	 *
	 */
	public static void buildDataObjectQuerySelects(
			final IRODSGenQueryBuilder builder) throws GenQueryBuilderException {

		if (builder == null) {
			throw new IllegalArgumentException("null builder");
		}

		builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_NAME)
		.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_DATA_NAME)
		.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_D_CREATE_TIME)
		.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_D_MODIFY_TIME)
		.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_D_DATA_ID)
		.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_DATA_SIZE)
		.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_DATA_REPL_NUM)
		.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_D_OWNER_NAME)
		.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_D_OWNER_ZONE);

	}

	/**
	 * Build a query for all files under a path. Note that files that are
	 * replicated are only returned once.
	 *
	 * @param path
	 *            {@code String} with the absolute path to a parent
	 *            directory
	 * @param builder
	 *            {@link IRODSGenQueryBuilder} that will be augmented with
	 *            selects and conditions
	 * @throws JargonException
	 */
	public static void buildQueryListAllFiles(final String path,
			final IRODSGenQueryBuilder builder) throws JargonException {

		if (path == null || path.isEmpty()) {
			throw new IllegalArgumentException("null or empty path");
		}

		if (builder == null) {
			throw new IllegalArgumentException("null builder");
		}

		try {
			builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_NAME)
			.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_DATA_NAME)
			.addConditionAsGenQueryField(
					RodsGenQueryEnum.COL_COLL_NAME,
					QueryConditionOperators.EQUAL, path);
		} catch (GenQueryBuilderException e) {
			throw new JargonException(e);
		}

	}

	/**
	 * Append to the provided {@code IRODSGenQueryBuilder} the selects and
	 * conditions necessary to list all directories under a parent path
	 * including permissions.
	 *
	 * @param path
	 * @param builder
	 * @throws GenQueryBuilderException
	 */
	public static void buildQueryListAllDirsWithUserAccessInfo(
			final String path, final IRODSGenQueryBuilder builder)
					throws GenQueryBuilderException {
		if (path == null || path.isEmpty()) {
			throw new IllegalArgumentException("null or empty path");
		}

		if (builder == null) {
			throw new IllegalArgumentException("null builder");
		}

		CollectionAOHelper
		.buildSelectsNeededForCollectionsInCollectionsAndDataObjectsListingEntry(builder);
		builder.addSelectAsGenQueryValue(
				RodsGenQueryEnum.COL_COLL_ACCESS_USER_NAME)
				.addSelectAsGenQueryValue(
						RodsGenQueryEnum.COL_COLL_ACCESS_USER_ZONE)
						.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_ACCESS_TYPE)
						.addSelectAsGenQueryValue(
								RodsGenQueryEnum.COL_COLL_ACCESS_USER_ID)
								.addConditionAsGenQueryField(
										RodsGenQueryEnum.COL_COLL_PARENT_NAME,
										QueryConditionOperators.EQUAL, path);
	}

}
