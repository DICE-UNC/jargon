package org.irods.jargon.core.pub;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.ModAccessControlInp;
import org.irods.jargon.core.packinstr.ModAvuMetadataInp;
import org.irods.jargon.core.protovalues.FilePermissionEnum;
import org.irods.jargon.core.pub.aohelper.CollectionAOHelper;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.domain.Collection;
import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.core.pub.domain.ObjStat.SpecColType;
import org.irods.jargon.core.pub.domain.User;
import org.irods.jargon.core.pub.domain.UserFilePermission;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.pub.io.IRODSFileFactoryImpl;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.AVUQueryOperatorEnum;
import org.irods.jargon.core.query.GenQueryBuilderException;
import org.irods.jargon.core.query.IRODSGenQuery;
import org.irods.jargon.core.query.IRODSGenQueryBuilder;
import org.irods.jargon.core.query.IRODSGenQueryFromBuilder;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.core.query.MetaDataAndDomainData.MetadataDomain;
import org.irods.jargon.core.query.QueryConditionOperators;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.core.utils.AccessObjectQueryProcessingUtils;
import org.irods.jargon.core.utils.CollectionAndPath;
import org.irods.jargon.core.utils.FederationEnabled;
import org.irods.jargon.core.utils.IRODSDataConversionUtil;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Access object handles various operations for an IRODS Collection.
 * <p/>
 * Note that traditional file io per the java.io.* interfaces is handled through
 * the objects in the <code>org.irods.jargon.core.pub.io</code> package. This
 * class represents operations that are outside of the contracts one would
 * expect from an <code>java.io.File</code> object or the various streams.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public final class CollectionAOImpl extends FileCatalogObjectAOImpl implements
		CollectionAO {

	public static final String ERROR_IN_COLECTION_QUERY = "An error occurred in the query for the collection";
	private IRODSFileFactory irodsFileFactory = new IRODSFileFactoryImpl(
			getIRODSSession(), getIRODSAccount());
	private IRODSGenQueryExecutor irodsGenQueryExecutor = new IRODSGenQueryExecutorImpl(
			getIRODSSession(), getIRODSAccount());
	public static final Logger log = LoggerFactory
			.getLogger(CollectionAOImpl.class);

	/**
	 * Default constructor
	 * 
	 * @param irodsSession
	 * @param irodsAccount
	 * @throws JargonException
	 */
	protected CollectionAOImpl(final IRODSSession irodsSession,
			final IRODSAccount irodsAccount) throws JargonException {
		super(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.CollectionAO#instanceIRODSFileForCollectionPath
	 * (java.lang.String)
	 */
	@Override
	public IRODSFile instanceIRODSFileForCollectionPath(
			final String collectionPath) throws JargonException {
		log.info("returning a collection for path: {}", collectionPath);
		final IRODSFile collection = irodsFileFactory
				.instanceIRODSFile(collectionPath);
		return collection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.CollectionAO#findDomainByMetadataQuery(java
	 * .util.List)
	 */
	@Override
	public List<Collection> findDomainByMetadataQuery(
			final List<AVUQueryElement> avuQueryElements)
			throws JargonQueryException, JargonException {

		return findDomainByMetadataQuery(avuQueryElements, 0);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.CollectionAO#findDomainByMetadataQuery(java
	 * .util.List, int)
	 */
	@Override
	public List<Collection> findDomainByMetadataQuery(
			final List<AVUQueryElement> avuQueryElements,
			final int partialStartIndex) throws JargonQueryException,
			JargonException {

		return findDomainByMetadataQuery(avuQueryElements, partialStartIndex,
				false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.CollectionAO#findDomainByMetadataQuery(java
	 * .util.List, int, boolean)
	 */
	@Override
	public List<Collection> findDomainByMetadataQuery(
			final List<AVUQueryElement> avuQueryElements,
			final int partialStartIndex, final boolean caseInsensitive)
			throws JargonQueryException, JargonException {

		log.info("findDomainByMetadataQuery()");

		if (avuQueryElements == null || avuQueryElements.isEmpty()) {
			throw new IllegalArgumentException("null or empty avuQueryElements");
		}

		if (caseInsensitive) {
			if (!this.getIRODSServerProperties()
					.isSupportsCaseInsensitiveQueries()) {
				throw new JargonException(
						"case insensitive queries not supported on this iRODS version");
			}
		}

		log.info("avuQueryElements:{}", avuQueryElements);
		log.info("partialStartIndex:}{}", partialStartIndex);
		log.info("caseInsensitive:{}", caseInsensitive);

		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true,
				caseInsensitive, null);
		IRODSQueryResultSetInterface resultSet;

		try {
			CollectionAOHelper.buildSelectsByAppendingToBuilder(builder);
			builder.addSelectAsGenQueryValue(
					RodsGenQueryEnum.COL_META_COLL_ATTR_NAME)
					.addSelectAsGenQueryValue(
							RodsGenQueryEnum.COL_META_COLL_ATTR_VALUE)
					.addSelectAsGenQueryValue(
							RodsGenQueryEnum.COL_META_COLL_ATTR_UNITS);

			for (AVUQueryElement queryElement : avuQueryElements) {
				CollectionAOHelper.appendConditionPartToBuilderQuery(
						queryElement, builder);
			}

			IRODSGenQueryFromBuilder irodsQuery = builder
					.exportIRODSQueryFromBuilder(this.getJargonProperties()
							.getMaxFilesAndDirsQueryMax());

			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(
					irodsQuery, partialStartIndex);
		} catch (GenQueryBuilderException e) {
			log.error("error building query", e);
			throw new JargonException("error building query", e);
		} catch (JargonQueryException jqe) {
			log.error("error executing query", jqe);
			throw new JargonException("error executing query", jqe);
		}

		return CollectionAOHelper.buildListFromResultSet(resultSet);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.CollectionAO#findWhere(java.lang.String,
	 * int)
	 */
	@Override
	public List<Collection> findWhere(final String whereClause,
			final int partialStartIndex) throws JargonException {

		if (whereClause == null) {
			throw new IllegalArgumentException("null where clause");
		}

		final StringBuilder query = new StringBuilder();
		query.append(CollectionAOHelper.buildSelects());
		query.append(" WHERE ");
		query.append(whereClause);
		final String queryString = query.toString();

		log.info("coll query:{}", queryString);

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString,
				getIRODSSession().getJargonProperties()
						.getMaxFilesAndDirsQueryMax());

		IRODSQueryResultSetInterface resultSet;

		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(
					irodsQuery, partialStartIndex);
		} catch (JargonQueryException e) {
			log.error("query exception for:" + queryString, e);
			throw new JargonException(ERROR_IN_COLECTION_QUERY);
		}

		return CollectionAOHelper.buildListFromResultSet(resultSet);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.CollectionAO#findWhereInZone(java.lang.String,
	 * int, java.lang.String)
	 */
	@Override
	public List<Collection> findWhereInZone(final String whereClause,
			final int partialStartIndex, final String zone)
			throws JargonException {

		log.info("findWhereInZone()");

		if (whereClause == null) {
			throw new IllegalArgumentException("null where clause");
		}

		if (zone == null) {
			throw new IllegalArgumentException(
					"null zone, set to blank if not needed");
		}

		log.info("whereClause:{}", whereClause);
		log.info("zone:{}", zone);

		final StringBuilder query = new StringBuilder();
		query.append(CollectionAOHelper.buildSelects());
		query.append(" WHERE ");
		query.append(whereClause);
		final String queryString = query.toString();

		log.info("coll query:{}", queryString);

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString,
				getIRODSSession().getJargonProperties()
						.getMaxFilesAndDirsQueryMax());

		IRODSQueryResultSetInterface resultSet;

		try {
			resultSet = irodsGenQueryExecutor
					.executeIRODSQueryAndCloseResultInZone(irodsQuery,
							partialStartIndex, zone);
		} catch (JargonQueryException e) {
			log.error("query exception for:" + queryString, e);
			throw new JargonException(ERROR_IN_COLECTION_QUERY);
		}

		return CollectionAOHelper.buildListFromResultSet(resultSet);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.CollectionAO#findMetadataValuesByMetadataQuery
	 * (java.util.List)
	 */
	@Override
	public List<MetaDataAndDomainData> findMetadataValuesByMetadataQuery(
			final List<AVUQueryElement> avuQuery) throws JargonQueryException,
			JargonException {
		return findMetadataValuesByMetadataQueryForCollection(avuQuery, "");
	}

	@Override
	public List<MetaDataAndDomainData> findMetadataValuesByMetadataQuery(
			final List<AVUQueryElement> avuQuery, final boolean caseInsensitive)
			throws JargonQueryException, JargonException {
		return findMetadataValuesByMetadataQueryForCollection(avuQuery, "", 0,
				caseInsensitive);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.irods.jargon.core.pub.CollectionAO#
	 * findMetadataValuesByMetadataQueryForCollection(java.util.List,
	 * java.lang.String)
	 */
	@Override
	public List<MetaDataAndDomainData> findMetadataValuesByMetadataQueryForCollection(
			final List<AVUQueryElement> avuQuery,
			final String collectionAbsolutePath) throws JargonQueryException,
			JargonException {

		return findMetadataValuesByMetadataQueryForCollection(avuQuery,
				collectionAbsolutePath, 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.CollectionAO#
	 * findMetadataValuesByMetadataQueryForCollection(java.util.List,
	 * java.lang.String, int)
	 */
	@Override
	public List<MetaDataAndDomainData> findMetadataValuesByMetadataQueryForCollection(
			final List<AVUQueryElement> avuQuery,
			final String collectionAbsolutePath, final int partialStartIndex)
			throws JargonQueryException, JargonException {

		return findMetadataValuesByMetadataQueryForCollection(avuQuery,
				collectionAbsolutePath, partialStartIndex, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.CollectionAO#
	 * findMetadataValuesByMetadataQueryForCollection(java.util.List,
	 * java.lang.String, int, boolean)
	 */
	@Override
	public List<MetaDataAndDomainData> findMetadataValuesByMetadataQueryForCollection(
			final List<AVUQueryElement> avuQuery,
			final String collectionAbsolutePath, final int partialStartIndex,
			final boolean caseInsensitive) throws FileNotFoundException,
			JargonQueryException, JargonException {

		if (avuQuery == null || avuQuery.isEmpty()) {
			throw new IllegalArgumentException("null or empty query");
		}

		if (collectionAbsolutePath == null) {
			throw new IllegalArgumentException(
					"Null absolutePath for collection");
		}

		if (caseInsensitive) {
			if (!this.getIRODSServerProperties()
					.isSupportsCaseInsensitiveQueries()) {
				throw new JargonException(
						"case insensitive queries not supported on this iRODS version");
			}
		}

		log.info("absPath for querying iCAT:{}", collectionAbsolutePath);

		log.info("building a metadata query for: {}", avuQuery);

		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true,
				caseInsensitive, null);
		IRODSQueryResultSetInterface resultSet;

		try {
			builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_ID)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_NAME)
					.addSelectAsGenQueryValue(
							RodsGenQueryEnum.COL_META_COLL_ATTR_NAME)
					.addSelectAsGenQueryValue(
							RodsGenQueryEnum.COL_META_COLL_ATTR_VALUE)
					.addSelectAsGenQueryValue(
							RodsGenQueryEnum.COL_META_COLL_ATTR_UNITS);

			if (!collectionAbsolutePath.isEmpty()) {
				builder.addConditionAsGenQueryField(
						RodsGenQueryEnum.COL_COLL_NAME,
						QueryConditionOperators.EQUAL, collectionAbsolutePath);
			}

			for (AVUQueryElement queryElement : avuQuery) {
				CollectionAOHelper.appendConditionPartToBuilderQuery(
						queryElement, builder);
			}

			IRODSGenQueryFromBuilder irodsQuery = builder
					.exportIRODSQueryFromBuilder(this.getJargonProperties()
							.getMaxFilesAndDirsQueryMax());

			if (collectionAbsolutePath.isEmpty()) {

				resultSet = irodsGenQueryExecutor
						.executeIRODSQueryAndCloseResult(irodsQuery,
								partialStartIndex);
			} else {

				ObjStat objStat = this
						.getObjectStatForAbsolutePath(collectionAbsolutePath);
				resultSet = irodsGenQueryExecutor
						.executeIRODSQueryAndCloseResultInZone(irodsQuery,
								partialStartIndex, objStat.getOwnerZone());
			}
		} catch (GenQueryBuilderException e) {
			log.error("error building query", e);
			throw new JargonException("error building query", e);
		} catch (JargonQueryException jqe) {
			log.error("error executing query", jqe);
			throw new JargonException("error executing query", jqe);
		}

		return AccessObjectQueryProcessingUtils
				.buildMetaDataAndDomainDatalistFromResultSet(
						MetadataDomain.COLLECTION, resultSet);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.CollectionAO#addAVUMetadata(java.lang.String,
	 * org.irods.jargon.core.pub.domain.AvuData)
	 */
	@Override
	public void addAVUMetadata(final String absolutePath, final AvuData avuData)
			throws DataNotFoundException, DuplicateDataException,
			JargonException {

		if (absolutePath == null || absolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolutePath");
		}

		if (avuData == null) {
			throw new IllegalArgumentException("null AVU data");
		}

		log.info("adding avu metadata to collection: {}", avuData);
		log.info("absolute path: {}", absolutePath);

		final ModAvuMetadataInp modifyAvuMetadataInp = ModAvuMetadataInp
				.instanceForAddCollectionMetadata(absolutePath, avuData);

		log.debug("sending avu request");

		try {

			getIRODSProtocol().irodsFunction(modifyAvuMetadataInp);

		} catch (JargonException je) {

			if (je.getMessage().indexOf("-814000") > -1) {
				throw new DataNotFoundException(
						"Target collection was not found, could not add AVU");
			} else if (je.getMessage().indexOf("-809000") > -1) {
				throw new DuplicateDataException(
						"Duplicate AVU exists, cannot add");
			}

			log.error("jargon exception adding AVU metadata", je);
			throw je;
		}

		log.debug("metadata added");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.CollectionAO#deleteAVUMetadata(java.lang.String
	 * , org.irods.jargon.core.pub.domain.AvuData)
	 */
	@Override
	public void deleteAVUMetadata(final String absolutePath,
			final AvuData avuData) throws DataNotFoundException,
			JargonException {
		if (absolutePath == null || absolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolutePath");
		}

		if (avuData == null) {
			throw new IllegalArgumentException("null AVU data");
		}

		log.info("deleting avu metadata from collection: {}", avuData);
		log.info("absolute path: {}", absolutePath);

		final ModAvuMetadataInp modifyAvuMetadataInp = ModAvuMetadataInp
				.instanceForDeleteCollectionMetadata(absolutePath, avuData);

		log.debug("sending avu request");

		try {
			getIRODSProtocol().irodsFunction(modifyAvuMetadataInp);
		} catch (JargonException je) {

			if (je.getMessage().indexOf("-814000") > -1) {
				throw new DataNotFoundException(
						"Target collection was not found, could not remove AVU");
			}

			log.error("jargon exception removing AVU metadata", je);
			throw je;
		}

		log.debug("metadata removed");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.CollectionAO#
	 * modifyAvuValueBasedOnGivenAttributeAndUnit(java.lang.String,
	 * org.irods.jargon.core.pub.domain.AvuData)
	 */
	@Override
	public void modifyAvuValueBasedOnGivenAttributeAndUnit(
			final String absolutePath, final AvuData avuData)
			throws DataNotFoundException, JargonException {

		if (absolutePath == null || absolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolutePath");
		}

		if (avuData == null) {
			throw new IllegalArgumentException("null avuData");
		}

		log.info("setting avu metadata value for collection");
		log.info("with  avu metadata:{}", avuData);
		log.info("absolute path: {}", absolutePath);

		// avu is distinct based on attrib and value, so do an attrib/unit
		// query, can only be one result
		List<AVUQueryElement> queryElements = new ArrayList<AVUQueryElement>();
		List<MetaDataAndDomainData> result;

		try {
			queryElements.add(AVUQueryElement.instanceForValueQuery(
					AVUQueryElement.AVUQueryPart.ATTRIBUTE,
					AVUQueryOperatorEnum.EQUAL, avuData.getAttribute()));
			queryElements.add(AVUQueryElement.instanceForValueQuery(
					AVUQueryElement.AVUQueryPart.UNITS,
					AVUQueryOperatorEnum.EQUAL, avuData.getUnit()));
			result = this.findMetadataValuesByMetadataQueryForCollection(
					queryElements, absolutePath);
		} catch (JargonQueryException e) {
			log.error("error querying data for avu", e);
			throw new JargonException("error querying data for AVU");
		}

		if (result.isEmpty()) {
			throw new DataNotFoundException("no avu data found");
		} else if (result.size() > 1) {
			throw new JargonException(
					"more than one AVU found with given attribute and unit, cannot modify non-unique AVU's in this way");
		}

		AvuData currentAvuData = new AvuData(result.get(0).getAvuAttribute(),
				result.get(0).getAvuValue(), result.get(0).getAvuUnit());

		AvuData modAvuData = new AvuData(result.get(0).getAvuAttribute(),
				avuData.getValue(), result.get(0).getAvuUnit());
		modifyAVUMetadata(absolutePath, currentAvuData, modAvuData);
		log.info("metadata modified to:{}", modAvuData);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.CollectionAO#modifyAVUMetadata(java.lang.String
	 * , org.irods.jargon.core.pub.domain.AvuData,
	 * org.irods.jargon.core.pub.domain.AvuData)
	 */
	@Override
	public void modifyAVUMetadata(final String absolutePath,
			final AvuData currentAvuData, final AvuData newAvuData)
			throws DataNotFoundException, JargonException {

		if (absolutePath == null || absolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolutePath");
		}

		if (currentAvuData == null) {
			throw new IllegalArgumentException("null currentAvuData");
		}

		if (newAvuData == null) {
			throw new IllegalArgumentException("null newAvuData");
		}

		log.info("overwrite avu metadata for collection: {}", currentAvuData);
		log.info("with new avu metadata:{}", newAvuData);
		log.info("absolute path: {}", absolutePath);

		final ModAvuMetadataInp modifyAvuMetadataInp = ModAvuMetadataInp
				.instanceForModifyCollectionMetadata(absolutePath,
						currentAvuData, newAvuData);

		log.debug("sending avu request");

		try {

			getIRODSProtocol().irodsFunction(modifyAvuMetadataInp);

		} catch (JargonException je) {

			if (je.getMessage().indexOf("-814000") > -1) {
				throw new DataNotFoundException(
						"Target collection was not found, could not modify AVU");
			}

			log.error("jargon exception modifying AVU metadata", je);
			throw je;
		}

		log.debug("metadata rewritten");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.CollectionAO#findMetadataValuesForCollection
	 * (java.lang.String, int)
	 */
	@Override
	public List<MetaDataAndDomainData> findMetadataValuesForCollection(
			final String collectionAbsolutePath, final int partialStartIndex)
			throws FileNotFoundException, JargonException, JargonQueryException {

		if (collectionAbsolutePath == null || collectionAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty collectionAbsolutePath");
		}

		if (partialStartIndex < 0) {
			throw new IllegalArgumentException(
					"partialStartIndex must be 0 or greater, set to 0 if no offset desired");
		}

		log.info("find metadata values for collection:{}",
				collectionAbsolutePath);
		log.info("with partial start of:{}", partialStartIndex);

		String absPath;
		ObjStat objStat = null;

		if (collectionAbsolutePath.isEmpty()) {
			absPath = "";
		} else {
			objStat = getObjectStatForAbsolutePath(collectionAbsolutePath);

			// get absolute path to use for querying iCAT (could be a soft link)
			absPath = IRODSDataConversionUtil.escapeSingleQuotes(MiscIRODSUtils
					.determineAbsolutePathBasedOnCollTypeInObjectStat(objStat));
		}

		log.info("absPath for querying iCAT:{}", absPath);

		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);
		IRODSQueryResultSetInterface resultSet;

		try {
			builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_ID)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_NAME)
					.addSelectAsGenQueryValue(
							RodsGenQueryEnum.COL_META_COLL_ATTR_NAME)
					.addSelectAsGenQueryValue(
							RodsGenQueryEnum.COL_META_COLL_ATTR_VALUE)
					.addSelectAsGenQueryValue(
							RodsGenQueryEnum.COL_META_COLL_ATTR_UNITS)
					.addConditionAsGenQueryField(
							RodsGenQueryEnum.COL_COLL_NAME,
							QueryConditionOperators.EQUAL, absPath);

			IRODSGenQueryFromBuilder irodsQuery = builder
					.exportIRODSQueryFromBuilder(this.getJargonProperties()
							.getMaxFilesAndDirsQueryMax());

			resultSet = irodsGenQueryExecutor
					.executeIRODSQueryAndCloseResultInZone(irodsQuery,
							partialStartIndex, objStat.getOwnerZone());

		} catch (GenQueryBuilderException e) {
			log.error("error building query", e);
			throw new JargonException("error building query", e);
		} catch (JargonQueryException jqe) {
			log.error("error executing query", jqe);
			throw new JargonException("error executing query", jqe);
		}

		return AccessObjectQueryProcessingUtils
				.buildMetaDataAndDomainDatalistFromResultSet(
						MetadataDomain.COLLECTION, resultSet);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.CollectionAO#findMetadataValuesForCollection
	 * (java.lang.String)
	 */
	@Override
	public List<MetaDataAndDomainData> findMetadataValuesForCollection(
			final String collectionAbsolutePath) throws JargonException,
			JargonQueryException {

		return findMetadataValuesForCollection(collectionAbsolutePath, 0);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.CollectionAO#findGivenObjStat(org.irods.jargon
	 * .core.pub.domain.ObjStat)
	 */
	@Override
	public Collection findGivenObjStat(final ObjStat objStat)
			throws DataNotFoundException, JargonException {

		log.info("findGivenObjStat()");

		if (objStat == null) {
			throw new IllegalArgumentException("null objStat");
		}

		log.info("objStat:{}", objStat);

		if (!objStat.isSomeTypeOfCollection()) {
			log.error(
					"objStat is not for a collection, wrong method called:{}",
					objStat);
			throw new JargonException("object is not a collection");
		}

		// make sure this special coll type has support
		MiscIRODSUtils.evaluateSpecCollSupport(objStat);

		// get absolute path to use for querying iCAT (could be a soft link)
		String absPath = MiscIRODSUtils
				.determineAbsolutePathBasedOnCollTypeInObjectStat(objStat);

		log.info("absPath for querying iCAT:{}", absPath);

		StringBuilder sb = new StringBuilder();
		sb.append(RodsGenQueryEnum.COL_COLL_NAME.getName());
		sb.append(" = '");
		sb.append(IRODSDataConversionUtil.escapeSingleQuotes(absPath));
		sb.append("'");
		List<Collection> collectionList = this.findWhere(sb.toString(), 0);

		if (collectionList.size() == 0) {
			log.error("No collection found for path:{}", absPath);
			throw new DataNotFoundException("no collection found for path");
		}

		Collection collection = collectionList.get(0);
		if (objStat.getSpecColType() == SpecColType.LINKED_COLL) {
			log.info("this is a special collection,so update the paths and add an object path");
		}

		collection.setObjectPath(objStat.getObjectPath());
		CollectionAndPath collectionAndPath = MiscIRODSUtils
				.separateCollectionAndPathFromGivenAbsolutePath(objStat
						.getAbsolutePath());
		sb = new StringBuilder();
		sb.append(collectionAndPath.getCollectionParent());
		sb.append("/");
		collection.setCollectionParentName(sb.toString());
		collection.setCollectionName(objStat.getAbsolutePath());
		collection.setSpecColType(objStat.getSpecColType());

		return collection;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.CollectionAO#findByAbsolutePath(java.lang.String
	 * )
	 */
	@Override
	public Collection findByAbsolutePath(
			final String irodsCollectionAbsolutePath)
			throws DataNotFoundException, JargonException {

		log.info("findByAbsolutePath()");

		if (irodsCollectionAbsolutePath == null
				|| irodsCollectionAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsCollectionAbsolutePath");
		}

		log.info("irodsCollectionAbsolutePath:{}", irodsCollectionAbsolutePath);

		ObjStat objStat = this.retrieveObjStat(irodsCollectionAbsolutePath);
		return findGivenObjStat(objStat);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.irods.jargon.core.pub.CollectionAO#
	 * countAllFilesUnderneathTheGivenCollection(java.lang.String)
	 */
	@Override
	public int countAllFilesUnderneathTheGivenCollection(
			final String irodsCollectionAbsolutePath)
			throws FileNotFoundException, JargonException {

		if (irodsCollectionAbsolutePath == null) {
			throw new IllegalArgumentException(
					"irodsCollectionAbsolutePath is null");
		}

		log.info("countAllFilesUnderneathTheGivenCollection: {}",
				irodsCollectionAbsolutePath);

		// not found exception will occur in retrieval of objStat, also checks
		// if I can handle this coll type
		ObjStat objStat = getObjectStatForAbsolutePath(irodsCollectionAbsolutePath);

		// I cannot get children if this is not a directory (a file has no
		// children)
		if (!objStat.isSomeTypeOfCollection()) {
			log.error(
					"this is a file, not a directory, and therefore I cannot get a count of the children: {}",
					irodsCollectionAbsolutePath);
			throw new JargonException(
					"attempting to count children under a file at path:"
							+ irodsCollectionAbsolutePath);
		}

		String absPath = null;
		if (objStat.getSpecColType() == SpecColType.LINKED_COLL) {
			absPath = objStat.getObjectPath();
		} else {
			absPath = irodsCollectionAbsolutePath;
		}

		IRODSGenQueryExecutor irodsGenQueryExecutor = getIRODSAccessObjectFactory()
				.getIRODSGenQueryExecutor(getIRODSAccount());

		StringBuilder query = new StringBuilder();
		query.append("SELECT ");
		query.append(RodsGenQueryEnum.COL_DATA_REPL_NUM.getName());
		query.append(", COUNT(");
		query.append(RodsGenQueryEnum.COL_DATA_NAME.getName());
		query.append(") WHERE ");
		query.append(RodsGenQueryEnum.COL_COLL_NAME.getName());
		query.append(" LIKE '");
		query.append(IRODSDataConversionUtil.escapeSingleQuotes(absPath));
		query.append("%'");
		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(query.toString(), 1);
		IRODSQueryResultSetInterface resultSet;

		try {
			resultSet = irodsGenQueryExecutor
					.executeIRODSQueryAndCloseResultInZone(irodsQuery, 0,
							objStat.getOwnerZone());
		} catch (JargonQueryException e) {
			log.error("query exception for  query:" + query.toString(), e);
			throw new JargonException("error in exists query");
		}

		int queryWithLikeCtr = 0;

		if (resultSet.getResults().size() > 0) {
			queryWithLikeCtr = IRODSDataConversionUtil
					.getIntOrZeroFromIRODSValue(resultSet.getFirstResult()
							.getColumn(1));
		}

		return queryWithLikeCtr;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.CollectionAO#setAccessPermissionInherit(java
	 * .lang.String, java.lang.String, boolean)
	 */
	@Override
	public void setAccessPermissionInherit(final String zone,
			final String absolutePath, final boolean recursive)
			throws JargonException {

		// pi tests parameters
		log.info("setAccessPermissionInherit on absPath:{}", absolutePath);

		String effectiveAbsPath = this
				.resolveAbsolutePathViaObjStat(absolutePath);
		boolean collNeedsRecursive = adjustRecursiveOption(effectiveAbsPath,
				recursive);

		ModAccessControlInp modAccessControlInp = ModAccessControlInp
				.instanceForSetInheritOnACollection(collNeedsRecursive, zone,
						effectiveAbsPath);
		getIRODSProtocol().irodsFunction(modAccessControlInp);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.CollectionAO#setAccessPermissionToNotInherit
	 * (java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public void setAccessPermissionToNotInherit(final String zone,
			final String absolutePath, final boolean recursive)
			throws JargonException {

		// pi tests parameters
		log.info("setAccessPermissionToNotInherit on absPath:{}", absolutePath);

		String effectiveAbsPath = this
				.resolveAbsolutePathViaObjStat(absolutePath);

		boolean collNeedsRecursive = adjustRecursiveOption(effectiveAbsPath,
				recursive);

		ModAccessControlInp modAccessControlInp = ModAccessControlInp
				.instanceForSetNoInheritOnACollection(collNeedsRecursive, zone,
						effectiveAbsPath);
		getIRODSProtocol().irodsFunction(modAccessControlInp);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.CollectionAO#setAccessPermissionRead(java.lang
	 * .String, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	@FederationEnabled
	public void setAccessPermissionRead(final String zone,
			final String absolutePath, final String userName,
			final boolean recursive) throws JargonException {

		// pi tests parameters
		log.info("setAccessPermissionRead on absPath:{}", absolutePath);

		String effectiveAbsPath = this
				.resolveAbsolutePathViaObjStat(absolutePath);

		boolean collNeedsRecursive = adjustRecursiveOption(effectiveAbsPath,
				recursive);

		ModAccessControlInp modAccessControlInp = ModAccessControlInp
				.instanceForSetPermission(collNeedsRecursive, zone,
						effectiveAbsPath, userName,
						ModAccessControlInp.READ_PERMISSION);
		getIRODSProtocol().irodsFunction(modAccessControlInp);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.CollectionAO#setAccessPermissionReadAsAdmin
	 * (java.lang.String, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public void setAccessPermissionReadAsAdmin(final String zone,
			final String absolutePath, final String userName,
			final boolean recursive) throws JargonException {

		// pi tests parameters
		log.info("setAccessPermissionReadAsAdmin on absPath:{}", absolutePath);

		String effectiveAbsPath = this
				.resolveAbsolutePathViaObjStat(absolutePath);

		boolean collNeedsRecursive = adjustRecursiveOption(effectiveAbsPath,
				recursive);

		ModAccessControlInp modAccessControlInp = ModAccessControlInp
				.instanceForSetPermissionInAdminMode(collNeedsRecursive, zone,
						effectiveAbsPath, userName,
						ModAccessControlInp.READ_PERMISSION);
		getIRODSProtocol().irodsFunction(modAccessControlInp);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.CollectionAO#setAccessPermissionWrite(java.
	 * lang.String, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public void setAccessPermissionWrite(final String zone,
			final String absolutePath, final String userName,
			final boolean recursive) throws JargonException {

		// pi tests parameters
		log.info("setAccessPermissionWrite on absPath:{}", absolutePath);
		// overhead iRODS behavior, if you set perm with recursive when no
		// children, then won't take

		String effectiveAbsPath = this
				.resolveAbsolutePathViaObjStat(absolutePath);

		boolean collNeedsRecursive = adjustRecursiveOption(effectiveAbsPath,
				recursive);

		ModAccessControlInp modAccessControlInp = ModAccessControlInp
				.instanceForSetPermission(collNeedsRecursive, zone,
						effectiveAbsPath, userName,
						ModAccessControlInp.WRITE_PERMISSION);
		getIRODSProtocol().irodsFunction(modAccessControlInp);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.CollectionAO#setAccessPermissionWriteAsAdmin
	 * (java.lang.String, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public void setAccessPermissionWriteAsAdmin(final String zone,
			final String absolutePath, final String userName,
			final boolean recursive) throws JargonException {

		// pi tests parameters
		log.info("setAccessPermissionWriteAsAdmin on absPath:{}", absolutePath);
		String effectiveAbsPath = this
				.resolveAbsolutePathViaObjStat(absolutePath);

		// overhead iRODS behavior, if you set perm with recursive when no
		// children, then won't take
		boolean collNeedsRecursive = adjustRecursiveOption(effectiveAbsPath,
				recursive);

		ModAccessControlInp modAccessControlInp = ModAccessControlInp
				.instanceForSetPermissionInAdminMode(collNeedsRecursive, zone,
						effectiveAbsPath, userName,
						ModAccessControlInp.WRITE_PERMISSION);
		getIRODSProtocol().irodsFunction(modAccessControlInp);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.CollectionAO#setAccessPermissionOwn(java.lang
	 * .String, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public void setAccessPermissionOwn(final String zone,
			final String absolutePath, final String userName,
			final boolean recursive) throws JargonException {

		// pi tests parameters
		log.info("setAccessPermissionOwn on absPath:{}", absolutePath);

		String effectiveAbsPath = this
				.resolveAbsolutePathViaObjStat(absolutePath);

		// overhead iRODS behavior, if you set perm with recursive when no
		// children, then won't take
		boolean collNeedsRecursive = adjustRecursiveOption(effectiveAbsPath,
				recursive);

		ModAccessControlInp modAccessControlInp = ModAccessControlInp
				.instanceForSetPermission(collNeedsRecursive, zone,
						effectiveAbsPath, userName,
						ModAccessControlInp.OWN_PERMISSION);
		getIRODSProtocol().irodsFunction(modAccessControlInp);

	}

	@Override
	public void setAccessPermissionOwnAsAdmin(final String zone,
			final String absolutePath, final String userName,
			final boolean recursive) throws JargonException {

		// pi tests parameters
		log.info("setAccessPermissionOwnAsAdmin on absPath:{}", absolutePath);
		// overhead iRODS behavior, if you set perm with recursive when no
		// children, then won't take
		String effectiveAbsPath = this
				.resolveAbsolutePathViaObjStat(absolutePath);

		boolean collNeedsRecursive = adjustRecursiveOption(effectiveAbsPath,
				recursive);

		ModAccessControlInp modAccessControlInp = ModAccessControlInp
				.instanceForSetPermissionInAdminMode(collNeedsRecursive, zone,
						effectiveAbsPath, userName,
						ModAccessControlInp.OWN_PERMISSION);
		getIRODSProtocol().irodsFunction(modAccessControlInp);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.CollectionAO#removeAccessPermissionForUser(
	 * java.lang.String, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public void removeAccessPermissionForUser(final String zone,
			final String absolutePath, final String userName,
			final boolean recursive) throws JargonException {

		// pi tests parameters
		log.info("removeAccessPermission on absPath:{}", absolutePath);
		log.info("for user:{}", userName);
		// overhead iRODS behavior, if you set perm with recursive when no
		// children, then won't take
		String effectiveAbsPath = this
				.resolveAbsolutePathViaObjStat(absolutePath);

		boolean collNeedsRecursive = adjustRecursiveOption(effectiveAbsPath,
				recursive);

		ModAccessControlInp modAccessControlInp = ModAccessControlInp
				.instanceForSetPermission(collNeedsRecursive, zone,
						effectiveAbsPath, userName,
						ModAccessControlInp.NULL_PERMISSION);
		getIRODSProtocol().irodsFunction(modAccessControlInp);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.CollectionAO#removeAccessPermissionForUserAsAdmin
	 * (java.lang.String, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public void removeAccessPermissionForUserAsAdmin(final String zone,
			final String absolutePath, final String userName,
			final boolean recursive) throws JargonException {

		// pi tests parameters
		log.info("removeAccessPermissionAsAdmin on absPath:{}", absolutePath);
		log.info("for user:{}", userName);
		// overhead iRODS behavior, if you set perm with recursive when no
		// children, then won't take

		String effectiveAbsPath = this
				.resolveAbsolutePathViaObjStat(absolutePath);
		boolean collNeedsRecursive = adjustRecursiveOption(effectiveAbsPath,
				recursive);

		ModAccessControlInp modAccessControlInp = ModAccessControlInp
				.instanceForSetPermissionInAdminMode(collNeedsRecursive, zone,
						effectiveAbsPath, userName,
						ModAccessControlInp.NULL_PERMISSION);
		getIRODSProtocol().irodsFunction(modAccessControlInp);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.CollectionAO#
	 * isCollectionSetForPermissionInheritance(java.lang.String)
	 */
	@Override
	public boolean isCollectionSetForPermissionInheritance(
			final String absolutePath) throws FileNotFoundException,
			JargonException {

		if (absolutePath == null || absolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty absolutePathToCollection");
		}

		ObjStat objStat = this.getObjectStatForAbsolutePath(absolutePath);
		String absPath = this.resolveAbsolutePathGivenObjStat(objStat);

		IRODSGenQueryExecutor irodsGenQueryExecutor = getIRODSAccessObjectFactory()
				.getIRODSGenQueryExecutor(getIRODSAccount());

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(CollectionAOHelper
				.buildInheritanceQueryForCollectionAbsolutePath(absPath), 1);
		IRODSQueryResultSetInterface resultSet;

		try {
			resultSet = irodsGenQueryExecutor
					.executeIRODSQueryAndCloseResultInZone(irodsQuery, 0,
							objStat.getOwnerZone());
		} catch (JargonQueryException e) {
			throw new JargonException("error querying for inheritance flag", e);
		}

		String inheritanceFlag = resultSet.getFirstResult().getColumn(0);
		boolean returnInheritanceVal = false;

		if (inheritanceFlag.trim().equals("1")) {
			returnInheritanceVal = true;
		}

		return returnInheritanceVal;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.CollectionAO#getPermissionForCollection(java
	 * .lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public FilePermissionEnum getPermissionForCollection(
			final String irodsAbsolutePath, final String userName,
			final String zone) throws FileNotFoundException, JargonException {

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePath");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		if (zone == null) {
			throw new IllegalArgumentException("null zone");
		}

		log.info("getPermissionForCollection for absPath:{}", irodsAbsolutePath);
		log.info("userName:{}", userName);

		IRODSFileSystemAO irodsFileSystemAO = getIRODSAccessObjectFactory()
				.getIRODSFileSystemAO(getIRODSAccount());
		IRODSFileFactory irodsFileFactory = this.getIRODSFileFactory();
		int permissionVal = irodsFileSystemAO
				.getDirectoryPermissionsForGivenUser(
						irodsFileFactory.instanceIRODSFile(irodsAbsolutePath),
						userName);
		FilePermissionEnum filePermissionEnum = FilePermissionEnum
				.valueOf(permissionVal);
		return filePermissionEnum;

	}

	/**
	 * Method overheads an iRODS protocol issue where recursive flag when
	 * collection has no children causes no permissions to be set.
	 * 
	 * @param absolutePath
	 * @param recursive
	 * @return
	 * @throws FileNotFoundException
	 *             if the underlying file is not found by the absolute path
	 * @throws JargonException
	 */
	private boolean adjustRecursiveOption(final String absolutePath,
			final boolean recursive) throws FileNotFoundException,
			JargonException {

		IRODSFile collFile = this.getIRODSFileFactory().instanceIRODSFile(
				absolutePath);

		if (!collFile.exists()) {
			throw new JargonException(
					"irodsFile does not exist for given path, cannot set permissions on it");
		}

		// soft links accounted for in collectionAndDataObjectListAndSearchAO
		boolean collNeedsRecursive = recursive;
		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = this
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(
						this.getIRODSAccount());
		int countFilesUnderParent = collectionAndDataObjectListAndSearchAO
				.countDataObjectsAndCollectionsUnderPath(absolutePath);

		if (recursive) {
			if (countFilesUnderParent == 0) {
				log.info("overridding recursive flag, file has no children");
				collNeedsRecursive = false;
			}
		}
		return collNeedsRecursive;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.CollectionAO#getPermissionForUserName(java.
	 * lang.String, java.lang.String)
	 */
	@Override
	@FederationEnabled
	public UserFilePermission getPermissionForUserName(
			final String irodsCollectionAbsolutePath, final String userName)
			throws FileNotFoundException, JargonException {

		UserFilePermission userFilePermission = null;

		if (irodsCollectionAbsolutePath == null
				|| irodsCollectionAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsCollectionAbsolutePath");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		log.info(
				"getPermissionForUserName with irodsCollectionAbsolutePath: {}",
				irodsCollectionAbsolutePath);
		log.info("   userName:{}", userName);

		ObjStat objStat = this
				.getObjectStatForAbsolutePath(irodsCollectionAbsolutePath);
		String absPath = this.resolveAbsolutePathGivenObjStat(objStat);

		String theUser = MiscIRODSUtils.getUserInUserName(userName);
		String theZone = MiscIRODSUtils.getZoneInUserName(userName);
		IRODSGenQueryExecutor irodsGenQueryExecutor = getIRODSAccessObjectFactory()
				.getIRODSGenQueryExecutor(getIRODSAccount());

		StringBuilder query = new StringBuilder(
				CollectionAOHelper.buildACLQueryForCollectionName(absPath));
		query.append(" AND ");
		query.append(RodsGenQueryEnum.COL_COLL_ACCESS_USER_NAME.getName());
		query.append(" = '");
		query.append(theUser);
		query.append("'");

		if (!theZone.isEmpty()) {
			query.append(" AND ");
			query.append(RodsGenQueryEnum.COL_COLL_ACCESS_USER_ZONE.getName());
			query.append(" = '");
			query.append(theZone);
			query.append("'");
		}

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(query.toString(),
				this.getJargonProperties().getMaxFilesAndDirsQueryMax());
		IRODSQueryResultSetInterface resultSet;
		UserAO userAO = this.getIRODSAccessObjectFactory().getUserAO(
				getIRODSAccount());
		User user = null;

		try {
			resultSet = irodsGenQueryExecutor
					.executeIRODSQueryAndCloseResultInZone(irodsQuery, 0,
							objStat.getOwnerZone());
			IRODSQueryResultRow row = resultSet.getFirstResult();

			/**
			 * Due to a gen query limitation getting user type with the user
			 * permission, a separate query must be done
			 */

			user = userAO.findByIdInZone(row.getColumn(2), this
					.getIRODSAccount().getZone());
			StringBuilder userAndZone = null;
			String displayUserName = null;

			userAndZone = new StringBuilder(row.getColumn(0));
			userAndZone.append('#');
			userAndZone.append(row.getColumn(1));

			if (row.getColumn(1).equals(objStat.getOwnerZone())) {
				displayUserName = row.getColumn(0);
			} else {
				displayUserName = userAndZone.toString();
			}

			userFilePermission = new UserFilePermission(displayUserName,
					row.getColumn(2),
					FilePermissionEnum.valueOf(IRODSDataConversionUtil
							.getIntOrZeroFromIRODSValue(row.getColumn(3))),
					user.getUserType(), row.getColumn(1));
			log.debug("loaded filePermission:{}", userFilePermission);

		} catch (JargonQueryException e) {
			log.error("query exception for  query:{}", query.toString(), e);
			throw new JargonException(
					"error in query loading user file permissions for collection",
					e);
		} catch (DataNotFoundException dnf) {
			log.info("no data found for user ACL");
		}

		return userFilePermission;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.CollectionAO#listPermissionsForCollection(java
	 * .lang.String)
	 */
	@Override
	@FederationEnabled
	public List<UserFilePermission> listPermissionsForCollection(
			final String irodsCollectionAbsolutePath)
			throws FileNotFoundException, JargonException {

		if (irodsCollectionAbsolutePath == null
				|| irodsCollectionAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty collectionAbsolutePath");
		}

		log.info("listPermissionsForCollection: {}",
				irodsCollectionAbsolutePath);

		ObjStat objStat = this
				.getObjectStatForAbsolutePath(irodsCollectionAbsolutePath);
		String absPath = this.resolveAbsolutePathGivenObjStat(objStat);

		List<UserFilePermission> userFilePermissions = new ArrayList<UserFilePermission>();

		IRODSGenQueryExecutor irodsGenQueryExecutor = getIRODSAccessObjectFactory()
				.getIRODSGenQueryExecutor(getIRODSAccount());

		String query = CollectionAOHelper
				.buildACLQueryForCollectionName(absPath);
		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(query.toString(),
				this.getJargonProperties().getMaxFilesAndDirsQueryMax());
		IRODSQueryResultSetInterface resultSet;
		UserAO userAO = this.getIRODSAccessObjectFactory().getUserAO(
				getIRODSAccount());

		/*
		 * There appears to be a gen query limitation on grabbing user type by a
		 * straight query, so, unfortunately, we need to do another query per
		 * user.
		 */

		User user = null;
		try {
			resultSet = irodsGenQueryExecutor
					.executeIRODSQueryAndCloseResultInZone(irodsQuery, 0,
							objStat.getOwnerZone());

			UserFilePermission userFilePermission = null;

			for (IRODSQueryResultRow row : resultSet.getResults()) {

				user = userAO.findByIdInZone(row.getColumn(2),
						objStat.getOwnerZone());
				userFilePermission = new UserFilePermission(row.getColumn(0),
						row.getColumn(2),
						FilePermissionEnum.valueOf(IRODSDataConversionUtil
								.getIntOrZeroFromIRODSValue(row.getColumn(3))),
						user.getUserType(), row.getColumn(1));
				log.debug("loaded filePermission:{}", userFilePermission);
				userFilePermissions.add(userFilePermission);
			}

		} catch (JargonQueryException e) {
			log.error("query exception for  query:{}", query.toString(), e);
			throw new JargonException(
					"error in query loading user file permissions for collection",
					e);
		}

		return userFilePermissions;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.FileCatalogObjectAOImpl#isUserHasAccess(java
	 * .lang.String, java.lang.String)
	 */
	@Override
	public boolean isUserHasAccess(final String irodsAbsolutePath,
			final String userName) throws JargonException {
		log.info("isUserHasAccess()");
		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePath");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		log.info("irodsAbsolutePath:{}", irodsAbsolutePath);
		log.info("userName:{}", userName);

		UserFilePermission derivedPermission = this.getPermissionForUserName(
				irodsAbsolutePath, userName);
		boolean hasPermission = false;
		if (derivedPermission != null) {
			hasPermission = true;
		}

		log.info("has permision? {}", hasPermission);
		return hasPermission;
	}

}