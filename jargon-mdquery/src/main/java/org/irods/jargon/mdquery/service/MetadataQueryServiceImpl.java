/**
 *
 */
package org.irods.jargon.mdquery.service;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.ListAndCount;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType;
import org.irods.jargon.core.query.GenQueryBuilderException;
import org.irods.jargon.core.query.GenQueryField.SelectFieldTypes;
import org.irods.jargon.core.query.IRODSGenQueryBuilder;
import org.irods.jargon.core.query.IRODSGenQueryFromBuilder;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.core.query.MetaDataAndDomainData.MetadataDomain;
import org.irods.jargon.core.query.PagingAwareCollectionListing;
import org.irods.jargon.core.query.QueryConditionOperators;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.core.service.AbstractJargonService;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.irods.jargon.mdquery.MetadataQuery;
import org.irods.jargon.mdquery.MetadataQuery.QueryType;
import org.irods.jargon.mdquery.MetadataQueryElement;
import org.irods.jargon.mdquery.exception.MetadataQueryException;
import org.irods.jargon.mdquery.serialization.MetadataQueryJsonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a service to generate metadata queries on iRODS. These are
 * actually GenQuery under the covers, but have been abstracted to make higher
 * level services simpler, and to centralize functional testing (e.g. iRODS gen
 * query limitiations) at this layer. Thus this class can enforce any necessary
 * restrictions on queries (number of elements, etc)
 *
 * @author Mike Conway - DICE
 *
 */
public class MetadataQueryServiceImpl extends AbstractJargonService implements MetadataQueryService {

	static Logger log = LoggerFactory.getLogger(MetadataQueryServiceImpl.class);

	/**
	 * Constructor takes dependencies
	 *
	 * @param irodsAccessObjectFactory
	 *            {@link IRODSAccessObjectFactory} to create various connected
	 *            services
	 * @param irodsAccount
	 *            {@link IRODSAccount} with authentication credentials
	 */
	public MetadataQueryServiceImpl(final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
	}

	/**
	 *
	 */
	public MetadataQueryServiceImpl() {
	}

	@Override
	public PagingAwareCollectionListing executeQuery(final String jsonString) throws MetadataQueryException {
		MetadataQueryJsonService metadataQueryJsonService = new MetadataQueryJsonService();
		MetadataQuery query = metadataQueryJsonService.metadataQueryFromJson(jsonString);
		return this.executeQuery(query);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.mdquery.service.MetadataQueryService#executeQuery(org
	 * .irods.jargon.mdquery.MetadataQuery)
	 */
	@Override
	public PagingAwareCollectionListing executeQuery(final MetadataQuery metadataQuery) throws MetadataQueryException {

		log.info("executeQuery()");
		if (metadataQuery == null) {
			throw new IllegalArgumentException("null metadataQuery");
		}

		log.info("metadataQuery:{}", metadataQuery);

		PagingAwareCollectionListing listing = new PagingAwareCollectionListing();

		try {
			listing.getPagingAwareCollectionListingDescriptor().setPageSizeUtilized(
					getIrodsAccessObjectFactory().getJargonProperties().getMaxFilesAndDirsQueryMax());
		} catch (JargonException e) {
			log.error("jargon exception in query", e);
			throw new MetadataQueryException(e);
		}

		if (metadataQuery.getQueryType() == QueryType.BOTH || metadataQuery.getQueryType() == QueryType.COLLECTIONS) {

			ListAndCount collections = queryCollections(metadataQuery);
			listing.setCollectionAndDataObjectListingEntries(collections.getCollectionAndDataObjectListingEntries());
			listing.getPagingAwareCollectionListingDescriptor().setCollectionsComplete(collections.isEndOfRecords());
			listing.getPagingAwareCollectionListingDescriptor().setCount(collections.getCountThisPage());

		} else {
			listing.getPagingAwareCollectionListingDescriptor().setCollectionsComplete(true);

		}

		if (metadataQuery.getQueryType() == QueryType.BOTH || metadataQuery.getQueryType() == QueryType.DATA) {

			log.info("querying data objects");
			ListAndCount dataObjects = queryDataObjects(metadataQuery);
			listing.getCollectionAndDataObjectListingEntries()
					.addAll(dataObjects.getCollectionAndDataObjectListingEntries());
			listing.getPagingAwareCollectionListingDescriptor().setDataObjectsComplete(dataObjects.isEndOfRecords());
			listing.getPagingAwareCollectionListingDescriptor().setDataObjectsCount(dataObjects.getCountThisPage());

		} else {
			listing.getPagingAwareCollectionListingDescriptor().setDataObjectsComplete(true);

		}

		log.info("listing generated:{}", listing);
		return listing;

	}

	private ListAndCount queryDataObjects(final MetadataQuery metadataQuery) throws MetadataQueryException {
		log.info("queryDataObjects()");

		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);
		IRODSQueryResultSetInterface resultSet = null;

		try {
			builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_NAME)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_DATA_NAME)
					.addSelectAsAgregateGenQueryValue(RodsGenQueryEnum.COL_DATA_SIZE, SelectFieldTypes.MAX)

					// .addSelectAsGenQueryValue(RodsGenQueryEnum.COL_DATA_SIZE)
					.addSelectAsAgregateGenQueryValue(RodsGenQueryEnum.COL_D_CREATE_TIME, SelectFieldTypes.MAX)
					.addSelectAsAgregateGenQueryValue(RodsGenQueryEnum.COL_D_MODIFY_TIME, SelectFieldTypes.MAX)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_D_OWNER_NAME)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_D_OWNER_ZONE);
		} catch (GenQueryBuilderException e) {
			log.error("error building query for data objects:{}", metadataQuery, e);
			throw new MetadataQueryException("gen query error", e);
		}

		if (!metadataQuery.getPathHint().isEmpty()) {
			log.info("adding path hint for :{}", metadataQuery.getPathHint());
			builder.addConditionAsGenQueryField(RodsGenQueryEnum.COL_COLL_NAME, QueryConditionOperators.LIKE,
					metadataQuery.getPathHint().trim() + "%");
		}

		/**
		 * Add an AVU query for each element
		 */
		for (MetadataQueryElement element : metadataQuery.getMetadataQueryElements()) {
			log.info("element:{}", element);

			builder.addConditionAsGenQueryField(RodsGenQueryEnum.COL_META_DATA_ATTR_NAME, QueryConditionOperators.EQUAL,
					element.getAttributeName().trim());

			if (element.getAttributeValue().size() > 1) {
				throw new UnsupportedOperationException("in and between not coded yet");
			}

			if (element.getAttributeValue().size() == 1) {
				log.info("single value operation");
				builder.addConditionAsGenQueryField(RodsGenQueryEnum.COL_META_DATA_ATTR_VALUE,
						QueryConditionOperators.getOperatorFromEnumStringValue(element.getOperator().toString()),
						element.getAttributeValue().get(0).trim());
			}

		}

		IRODSGenQueryFromBuilder irodsQuery;
		try {
			irodsQuery = builder.exportIRODSQueryFromBuilder(
					getIrodsAccessObjectFactory().getJargonProperties().getMaxFilesAndDirsQueryMax());
			String targetZone = deriveTargetZone(metadataQuery);

			resultSet = getIrodsAccessObjectFactory().getIRODSGenQueryExecutor(getIrodsAccount())
					.executeIRODSQueryAndCloseResultInZone(irodsQuery, 0, targetZone);

			List<CollectionAndDataObjectListingEntry> entries = buildListFromQueryResult(MetadataDomain.DATA,
					resultSet);
			ListAndCount listAndCount = new ListAndCount();
			listAndCount.setCollectionAndDataObjectListingEntries(entries);

			/*
			 * see if the query had total records, if it did not, do a separate query to
			 * establish total records
			 */

			if (listAndCount.getCollectionAndDataObjectListingEntries().isEmpty()) {
				listAndCount.setCountTotal(0);
				log.info("empty results returned");
				listAndCount.setEndOfRecords(true);
				return listAndCount;
			}

			int lastEntryIdx = listAndCount.getCollectionAndDataObjectListingEntries().size() - 1;
			CollectionAndDataObjectListingEntry lastEntry = listAndCount.getCollectionAndDataObjectListingEntries()
					.get(lastEntryIdx);
			listAndCount.setCountThisPage(lastEntry.getCount());
			listAndCount.setEndOfRecords(lastEntry.isLastResult());
			listAndCount.setOffsetStart(listAndCount.getCollectionAndDataObjectListingEntries().get(0).getCount());
			return listAndCount;

		} catch (GenQueryBuilderException | JargonException | JargonQueryException e) {
			log.error("error in query for collections:{}", metadataQuery, e);
			throw new MetadataQueryException("gen query error", e);
		}
	}

	private ListAndCount queryCollections(final MetadataQuery metadataQuery) throws MetadataQueryException {
		log.info("queryCollections()");
		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);
		IRODSQueryResultSetInterface resultSet = null;

		try {
			builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_PARENT_NAME)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_NAME)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_CREATE_TIME)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_MODIFY_TIME)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_OWNER_NAME)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_OWNER_ZONE);
		} catch (GenQueryBuilderException e) {
			log.error("error building query for collections:{}", metadataQuery, e);
			throw new MetadataQueryException("gen query error", e);
		}

		if (!metadataQuery.getPathHint().isEmpty()) {
			log.info("adding path hint for :{}", metadataQuery.getPathHint());
			builder.addConditionAsGenQueryField(RodsGenQueryEnum.COL_COLL_NAME, QueryConditionOperators.LIKE,
					metadataQuery.getPathHint().trim() + "%");
		}

		/**
		 * Add an AVU query for each element
		 */
		for (MetadataQueryElement element : metadataQuery.getMetadataQueryElements()) {
			log.info("element:{}", element);

			builder.addConditionAsGenQueryField(RodsGenQueryEnum.COL_META_COLL_ATTR_NAME, QueryConditionOperators.EQUAL,
					element.getAttributeName().trim());

			if (element.getAttributeValue().size() > 1) {
				throw new UnsupportedOperationException("in and between not coded yet");
			}

			if (element.getAttributeValue().size() == 1) {
				log.info("single value operation");
				builder.addConditionAsGenQueryField(RodsGenQueryEnum.COL_META_COLL_ATTR_VALUE,
						QueryConditionOperators.getOperatorFromEnumStringValue(element.getOperator().toString()),
						element.getAttributeValue().get(0).trim());
			}

		}

		IRODSGenQueryFromBuilder irodsQuery;
		try {
			irodsQuery = builder.exportIRODSQueryFromBuilder(
					getIrodsAccessObjectFactory().getJargonProperties().getMaxFilesAndDirsQueryMax());
			String targetZone = deriveTargetZone(metadataQuery);

			resultSet = getIrodsAccessObjectFactory().getIRODSGenQueryExecutor(getIrodsAccount())
					.executeIRODSQueryAndCloseResultInZone(irodsQuery, 0, targetZone);

			List<CollectionAndDataObjectListingEntry> entries = buildListFromQueryResult(MetadataDomain.COLLECTION,
					resultSet);
			ListAndCount listAndCount = new ListAndCount();
			listAndCount.setCollectionAndDataObjectListingEntries(entries);

			/*
			 * see if the query had total records, if it did not, do a separate query to
			 * establish total records
			 */

			if (listAndCount.getCollectionAndDataObjectListingEntries().isEmpty()) {
				listAndCount.setCountTotal(0);
				log.info("empty results returned");
				listAndCount.setEndOfRecords(true);
				return listAndCount;
			}

			int lastEntryIdx = listAndCount.getCollectionAndDataObjectListingEntries().size() - 1;
			CollectionAndDataObjectListingEntry lastEntry = listAndCount.getCollectionAndDataObjectListingEntries()
					.get(lastEntryIdx);
			listAndCount.setCountThisPage(lastEntry.getCount());
			listAndCount.setEndOfRecords(lastEntry.isLastResult());
			listAndCount.setOffsetStart(listAndCount.getCollectionAndDataObjectListingEntries().get(0).getCount());
			return listAndCount;

		} catch (GenQueryBuilderException | JargonException | JargonQueryException e) {
			log.error("error in query for collections:{}", metadataQuery, e);
			throw new MetadataQueryException("gen query error", e);
		}

	}

	private static List<CollectionAndDataObjectListingEntry> buildListFromQueryResult(
			final MetadataDomain metaDataDomain, final IRODSQueryResultSetInterface irodsQueryResultSet)
			throws JargonException {
		if (metaDataDomain == null) {
			throw new JargonException("null metaDataDomain");
		}

		if (irodsQueryResultSet == null) {
			throw new JargonException("null irodsQueryResultSet");
		}

		List<CollectionAndDataObjectListingEntry> metaDataResults = new ArrayList<CollectionAndDataObjectListingEntry>();
		for (IRODSQueryResultRow row : irodsQueryResultSet.getResults()) {
			metaDataResults
					.add(buildListingFromResultSetRow(metaDataDomain, row, irodsQueryResultSet.getTotalRecords()));
		}

		return metaDataResults;
	}

	private static CollectionAndDataObjectListingEntry buildListingFromResultSetRow(
			final MetaDataAndDomainData.MetadataDomain metadataDomain, final IRODSQueryResultRow row,
			final int totalRecordCount) throws JargonException {

		CollectionAndDataObjectListingEntry entry = new CollectionAndDataObjectListingEntry();
		entry.setCount(row.getRecordCount());
		entry.setLastResult(row.isLastResult());
		entry.setTotalRecords(totalRecordCount);

		if (metadataDomain == MetadataDomain.COLLECTION) {
			entry.setCount(row.getRecordCount());
			entry.setObjectType(ObjectType.COLLECTION);
			entry.setParentPath(row.getColumn(0));
			entry.setPathOrName(row.getColumn(1));
			entry.setCreatedAt(row.getColumnAsDateOrNull(2));
			entry.setModifiedAt(row.getColumnAsDateOrNull(3));
			entry.setOwnerName(row.getColumn(4));
			entry.setOwnerZone(row.getColumn(5));

		} else {

			entry.setCount(row.getRecordCount());
			entry.setObjectType(ObjectType.DATA_OBJECT);
			entry.setParentPath(row.getColumn(0));
			entry.setPathOrName(row.getColumn(1));
			entry.setDataSize(row.getColumnAsLongOrZero(2));
			entry.setCreatedAt(row.getColumnAsDateOrNull(3));
			entry.setModifiedAt(row.getColumnAsDateOrNull(4));
			entry.setOwnerName(row.getColumn(5));
			entry.setOwnerZone(row.getColumn(6));
		}

		return entry;
	}

	private String deriveTargetZone(final MetadataQuery metadataQuery) {
		if (!metadataQuery.getTargetZone().isEmpty()) {
			return metadataQuery.getTargetZone();
		} else if (!metadataQuery.getPathHint().isEmpty()) {
			return MiscIRODSUtils.getZoneInPath(metadataQuery.getPathHint());
		} else {
			return getIrodsAccount().getZone();
		}
	}
}
