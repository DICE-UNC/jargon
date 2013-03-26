/**
 * 
 */
package org.irods.jargon.core.pub.aohelper;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.protovalues.FilePermissionEnum;
import org.irods.jargon.core.protovalues.UserTypeEnum;
import org.irods.jargon.core.pub.UserAO;
import org.irods.jargon.core.pub.domain.Collection;
import org.irods.jargon.core.pub.domain.UserFilePermission;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.BuilderQueryUtils;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType;
import org.irods.jargon.core.query.GenQueryBuilderException;
import org.irods.jargon.core.query.IRODSGenQueryBuilder;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.QueryConditionOperators;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.core.utils.IRODSDataConversionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper clas to support the {@link org.irods.jargon.core.pub.CollectionAO}.
 * This class is primarily for internal use, but does provide helpful methods
 * when creating extensions that work with iRODS collections.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 */
public class CollectionAOHelper extends AOHelper {

	public static final Logger log = LoggerFactory
			.getLogger(CollectionAOHelper.class);

	/**
	 * Create a set of selects for a collection, used in general query
	 * 
	 * @return <code>String</code> with select statements for the domain object.
	 */
	public static String buildSelects() {
		final StringBuilder query = new StringBuilder();
		query.append("SELECT ");
		query.append(RodsGenQueryEnum.COL_COLL_ID.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_COLL_NAME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_COLL_PARENT_NAME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_COLL_OWNER_NAME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_COLL_OWNER_ZONE.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_COLL_MAP_ID.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_COLL_INHERITANCE.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_COLL_COMMENTS.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_COLL_CREATE_TIME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_COLL_MODIFY_TIME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_COLL_INFO1.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_COLL_INFO2.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_COLL_TYPE.getName());
		return query.toString();
	}

	/**
	 * Add appropriate select statements to the provided builder to query the
	 * collection data in the iCAT
	 * 
	 * @param builder
	 *            {@link IRODSGenQueryBuilder} to which the selects will be
	 *            added
	 * @throws GenQueryBuilderException
	 */
	public static void buildSelectsByAppendingToBuilder(
			final IRODSGenQueryBuilder builder) throws GenQueryBuilderException {
		builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_ID)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_NAME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_PARENT_NAME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_OWNER_NAME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_OWNER_ZONE)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_MAP_ID)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_INHERITANCE)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_COMMENTS)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_CREATE_TIME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_MODIFY_TIME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_INFO1)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_INFO2)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_TYPE);
	}

	/**
	 * Build a set of selects for collection metadata. This method does not add
	 * the "select" statement or any trailing delimiter, and is handy when you
	 * want to tack a metadata selection onto the end of a query.
	 * 
	 * @return <code>String</code> with metadata select fragment.
	 */
	public static String buildMetadataSelects() {
		final StringBuilder query = new StringBuilder();
		query.append(RodsGenQueryEnum.COL_META_COLL_ATTR_NAME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_META_COLL_ATTR_VALUE.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_META_COLL_ATTR_UNITS.getName());
		return query.toString();
	}

	/**
	 * Return a <code>Collection</code> domain object given a result row from a
	 * query
	 * 
	 * @param row
	 *            {@link org.irods.jargon.core.query.IRODSQueryResultRow}
	 *            containing the result of a query
	 * @return {@link org.irods.jargon.pub.domain.Collection} that represents
	 *         the data in the row.
	 * @throws JargonException
	 */
	public static Collection buildCollectionFromResultSetRow(
			final IRODSQueryResultRow row) throws JargonException {
		Collection collection = new Collection();
		collection.setCollectionId(IRODSDataConversionUtil
				.getIntOrZeroFromIRODSValue(row.getColumn(0)));
		collection.setCollectionName(row.getColumn(1));
		collection.setCollectionParentName(row.getColumn(2));
		collection.setCollectionOwnerName(row.getColumn(3));
		collection.setCollectionOwnerZone(row.getColumn(4));
		collection.setCollectionMapId(row.getColumn(5));
		collection.setCollectionInheritance(row.getColumn(6));
		collection.setComments(row.getColumn(7));
		collection.setCreatedAt(IRODSDataConversionUtil
				.getDateFromIRODSValue(row.getColumn(8)));
		collection.setModifiedAt(IRODSDataConversionUtil
				.getDateFromIRODSValue(row.getColumn(9)));
		collection.setInfo1(row.getColumn(10));
		collection.setInfo2(row.getColumn(11));
		collection.setInfo2(row.getColumn(11));
		// collection.setCollectionType(row.getColumn(12));
		collection.setLastResult(row.isLastResult());

		if (log.isInfoEnabled()) {
			log.info("collection built \n");
			log.info(collection.toString());
		}

		return collection;
	}

	/**
	 * Given a set of AVU Query parameters, build the appropriate condition to
	 * add to a query
	 * 
	 * @param queryCondition
	 *            <code>List</code> of
	 *            {@link org.irods.jargon.core.query.AVUQueryElement} that
	 *            describes a metadata query
	 * @param queryElement
	 *            <codeStringBuilder</code> with the given AVU query in iquest
	 *            query form.
	 */
	public static StringBuilder buildConditionPart(
			final AVUQueryElement queryElement) {
		StringBuilder queryCondition = new StringBuilder();
		if (queryElement.getAvuQueryPart() == AVUQueryElement.AVUQueryPart.ATTRIBUTE) {
			queryCondition.append(RodsGenQueryEnum.COL_META_COLL_ATTR_NAME
					.getName());
			queryCondition.append(SPACE);
			queryCondition
					.append(queryElement.getOperator().getOperatorValue());
			queryCondition.append(SPACE);
			queryCondition.append(QUOTE);
			queryCondition.append(queryElement.getValue());
			queryCondition.append(QUOTE);
		}

		if (queryElement.getAvuQueryPart() == AVUQueryElement.AVUQueryPart.VALUE) {
			queryCondition.append(RodsGenQueryEnum.COL_META_COLL_ATTR_VALUE
					.getName());
			queryCondition.append(SPACE);
			queryCondition
					.append(queryElement.getOperator().getOperatorValue());
			queryCondition.append(SPACE);
			queryCondition.append(QUOTE);
			queryCondition.append(queryElement.getValue());
			queryCondition.append(QUOTE);
		}

		if (queryElement.getAvuQueryPart() == AVUQueryElement.AVUQueryPart.UNITS) {
			queryCondition.append(RodsGenQueryEnum.COL_META_COLL_ATTR_UNITS
					.getName());
			queryCondition.append(SPACE);
			queryCondition
					.append(queryElement.getOperator().getOperatorValue());
			queryCondition.append(SPACE);
			queryCondition.append(QUOTE);
			queryCondition.append(queryElement.getValue());
			queryCondition.append(QUOTE);
		}

		return queryCondition;
	}

	/**
	 * Append the appropriately formed query condition to the provided builder
	 * for a collection metadata query
	 * 
	 * @param queryElement
	 *            {@link AVUQueryElement} to be added as a condition
	 * @param builder
	 *            {@link IRODSGenQueryBuilder} that will have the derived
	 *            condition appended
	 * @throws JargonQueryException
	 *             if the query cannot be built
	 */
	public static void appendConditionPartToBuilderQuery(
			final AVUQueryElement queryElement,
			final IRODSGenQueryBuilder builder) throws JargonQueryException {

		if (queryElement.getAvuQueryPart() == AVUQueryElement.AVUQueryPart.ATTRIBUTE) {
			builder.addConditionAsGenQueryField(
					RodsGenQueryEnum.COL_META_COLL_ATTR_NAME,
					BuilderQueryUtils
							.translateAVUQueryElementOperatorToBuilderQueryCondition(queryElement),
					queryElement.getValue().trim());

		} else if (queryElement.getAvuQueryPart() == AVUQueryElement.AVUQueryPart.VALUE) {
			builder.addConditionAsGenQueryField(
					RodsGenQueryEnum.COL_META_COLL_ATTR_VALUE,
					BuilderQueryUtils
							.translateAVUQueryElementOperatorToBuilderQueryCondition(queryElement),
					queryElement.getValue().trim());

		} else if (queryElement.getAvuQueryPart() == AVUQueryElement.AVUQueryPart.UNITS) {
			builder.addConditionAsGenQueryField(
					RodsGenQueryEnum.COL_META_COLL_ATTR_UNITS,
					BuilderQueryUtils
							.translateAVUQueryElementOperatorToBuilderQueryCondition(queryElement),
					queryElement.getValue().trim());
		} else {
			throw new JargonQueryException("unable to resolve AVU Query part");
		}

	}

	/**
	 * Build a list of collection results based on the result of a query
	 * 
	 * @param resultSet
	 * @return
	 * @throws JargonException
	 */
	public static List<Collection> buildListFromResultSet(
			final IRODSQueryResultSetInterface resultSet)
			throws JargonException {

		final List<Collection> collections = new ArrayList<Collection>();

		for (IRODSQueryResultRow row : resultSet.getResults()) {
			collections.add(buildCollectionFromResultSetRow(row));
		}

		return collections;
	}

	/**
	 * for a result set row, create a
	 * <code>CollectionAndDataObjectListingEntry</code>
	 * 
	 * @param row
	 *            <code>IRODSQueryResultRow</code> with raw data.
	 * @param totalRecords
	 *            <code>int</code> with the optional total records in the
	 *            database, not always available in the iCAT, this can be set to
	 *            0 if not available
	 * @return {@link org.irods.jargon.core.query.CollectionAndDataObjectListingEntry}
	 * @throws JargonException
	 */
	public static CollectionAndDataObjectListingEntry buildCollectionListEntryFromResultSetRowForCollectionQuery(
			final IRODSQueryResultRow row, final int totalRecords)
			throws JargonException {
		CollectionAndDataObjectListingEntry entry = new CollectionAndDataObjectListingEntry();
		entry.setParentPath(row.getColumn(0));
		entry.setObjectType(ObjectType.COLLECTION);
		entry.setPathOrName(row.getColumn(1));
		entry.setCreatedAt(IRODSDataConversionUtil.getDateFromIRODSValue(row
				.getColumn(2)));
		entry.setModifiedAt(IRODSDataConversionUtil.getDateFromIRODSValue(row
				.getColumn(3)));
		entry.setId(IRODSDataConversionUtil.getIntOrZeroFromIRODSValue(row
				.getColumn(4)));
		entry.setOwnerName(row.getColumn(5));
		entry.setOwnerZone(row.getColumn(6));
		entry.setSpecColType(IRODSDataConversionUtil
				.getCollectionTypeFromIRODSValue(row.getColumn(7)));

		entry.setCount(row.getRecordCount());
		entry.setTotalRecords(totalRecords);
		entry.setLastResult(row.isLastResult());

		log.debug("listing entry built {}", entry.toString());

		return entry;
	}

	/**
	 * for a result set row from a query for data objects in a collection,
	 * create a <code>CollectionAndDataObjectListingEntry</code>
	 * 
	 * @param row
	 *            <code>IRODSQueryResultRow</code> with raw data
	 * @param totalRecords
	 *            <code>int</code> with the optional total records in the
	 *            database, not always available in the iCAT, this can be set to
	 *            0 if not available
	 * @return {@link org.irods.jargon.core.query.CollectionAndDataObjectListingEntry}
	 * @throws JargonException
	 */
	public static CollectionAndDataObjectListingEntry buildCollectionListEntryFromResultSetRowForDataObjectQuery(
			final IRODSQueryResultRow row, final int totalRecords)
			throws JargonException {
		CollectionAndDataObjectListingEntry entry = new CollectionAndDataObjectListingEntry();
		entry.setParentPath(row.getColumn(0));
		entry.setObjectType(ObjectType.DATA_OBJECT);
		entry.setPathOrName(row.getColumn(1));
		entry.setCreatedAt(IRODSDataConversionUtil.getDateFromIRODSValue(row
				.getColumn(2)));
		entry.setModifiedAt(IRODSDataConversionUtil.getDateFromIRODSValue(row
				.getColumn(3)));
		entry.setId(IRODSDataConversionUtil.getIntOrZeroFromIRODSValue(row
				.getColumn(4)));
		entry.setDataSize(IRODSDataConversionUtil
				.getLongOrZeroFromIRODSValue(row.getColumn(5)));
		entry.setOwnerName(row.getColumn(7));
		entry.setCount(row.getRecordCount());
		entry.setLastResult(row.isLastResult());
		entry.setTotalRecords(totalRecords);

		log.debug("listing entry built {}", entry.toString());

		return entry;
	}

	/**
	 * Append selects to the provided builder for collection queries
	 * 
	 * @param builder
	 * @throws GenQueryBuilderException
	 */
	public static void buildSelectsNeededForCollectionsInCollectionsAndDataObjectsListingEntry(
			final IRODSGenQueryBuilder builder) throws GenQueryBuilderException {
		if (builder == null) {
			throw new IllegalArgumentException("null builder");
		}

		builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_PARENT_NAME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_NAME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_CREATE_TIME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_MODIFY_TIME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_ID)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_OWNER_NAME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_OWNER_ZONE)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_TYPE);
	}

	/**
	 * Build an inheritance query for the collection by appending the selects
	 * and conditions to the <code>IRODSGenQueryBuilder</code> provided
	 * 
	 * @param absolutePathToCollection
	 *            <code>String</code> with the absolute path to the iRODS
	 *            collection for which the permission bit will be queried
	 * @param builder
	 *            {@link IRODSGenQueryBuilder}
	 * @throws JargonException
	 */
	public static void buildInheritanceQueryForCollectionAbsolutePath(
			final String absolutePathToCollection,
			final IRODSGenQueryBuilder builder) throws JargonException {

		if (absolutePathToCollection == null
				|| absolutePathToCollection.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty absolutePathToCollection");
		}

		if (builder == null) {
			throw new IllegalArgumentException("null builder");
		}

		try {
			builder.addSelectAsGenQueryValue(
					RodsGenQueryEnum.COL_COLL_INHERITANCE)
					.addConditionAsGenQueryField(
							RodsGenQueryEnum.COL_COLL_NAME,
							QueryConditionOperators.EQUAL,
							absolutePathToCollection);
		} catch (GenQueryBuilderException e) {
			throw new JargonException("error building inheritance query", e);
		}
	}

	/**
	 * @param userFilePermissions
	 * @param row
	 * @param userAO
	 * @throws JargonException
	 */
	public static void buildUserFilePermissionForCollection(
			final List<UserFilePermission> userFilePermissions,
			final IRODSQueryResultRow row, final UserAO userAO,
			final String irodsAbsolutePath) throws JargonException {

		/*
		 * There appears to be a gen query issue with getting user type in the
		 * permissions query, so, unfortunately, I need to do another query to
		 * get the user type
		 */
		UserFilePermission userFilePermission;

		/*
		 * Gracefully ignore a not found for the user name and zone, just set
		 * the type to unknown and return what I have.
		 */
		try {
			/*
			 * User user = userAO .findByIdInZone(row.getColumn(10),
			 * collectionZone);
			 */

			userFilePermission = new UserFilePermission(row.getColumn(8),
					row.getColumn(11),
					FilePermissionEnum.valueOf(IRODSDataConversionUtil
							.getIntOrZeroFromIRODSValue(row.getColumn(10))),
					UserTypeEnum.RODS_UNKNOWN, row.getColumn(9));

		} catch (DataNotFoundException dnf) {
			log.warn(
					"user info not found for permission for user:{}, this permission will not be added",
					row.getColumn(8));
			userFilePermission = new UserFilePermission(row.getColumn(8),
					row.getColumn(11),
					FilePermissionEnum.valueOf(IRODSDataConversionUtil
							.getIntOrZeroFromIRODSValue(row.getColumn(10))),
					UserTypeEnum.RODS_UNKNOWN, row.getColumn(9));
		}
		userFilePermissions.add(userFilePermission);
	}

	/**
	 * @param userFilePermissions
	 * @param row
	 * @throws JargonException
	 */
	public static void buildUserFilePermissionForDataObject(
			final List<UserFilePermission> userFilePermissions,
			final IRODSQueryResultRow row, final String irodsAbsolutePath,
			final String currentZone) throws JargonException {

		/*
		 * There appears to be a gen query issue with getting user type in the
		 * permissions query, so, unfortunately, I need to do another query to
		 * get the user type
		 */
		UserFilePermission userFilePermission;

		userFilePermission = new UserFilePermission(row.getColumn(8),
				row.getColumn(9),
				FilePermissionEnum.valueOf(IRODSDataConversionUtil
						.getIntOrZeroFromIRODSValue(row.getColumn(10))),
				UserTypeEnum.findTypeByString(row.getColumn(11)),
				row.getColumn(12));
		userFilePermissions.add(userFilePermission);
	}

	/**
	 * Build a select for a collection ACL with the given collection absolute
	 * path
	 * 
	 * @param irodsCollectionAbsolutePath
	 * @param builder
	 */

	public static void buildACLQueryForCollectionName(
			final String irodsCollectionAbsolutePath,
			final IRODSGenQueryBuilder builder) throws JargonException {

		if (irodsCollectionAbsolutePath == null
				|| irodsCollectionAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsCollectionAbsolutePath");
		}

		try {
			builder.addSelectAsGenQueryValue(
					RodsGenQueryEnum.COL_COLL_ACCESS_USER_NAME)
					.addSelectAsGenQueryValue(
							RodsGenQueryEnum.COL_COLL_ACCESS_USER_ZONE)
					.addSelectAsGenQueryValue(
							RodsGenQueryEnum.COL_COLL_ACCESS_USER_ID)
					.addSelectAsGenQueryValue(
							RodsGenQueryEnum.COL_COLL_ACCESS_TYPE)
					.addConditionAsGenQueryField(
							RodsGenQueryEnum.COL_COLL_NAME,
							QueryConditionOperators.EQUAL,
							irodsCollectionAbsolutePath);
		} catch (GenQueryBuilderException e) {
			throw new JargonException(e);
		}

	}

}
