package org.irods.jargon.core.pub;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.CatalogSQLException;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.OperationNotSupportedByThisServerException;
import org.irods.jargon.core.packinstr.ModAccessControlInp;
import org.irods.jargon.core.packinstr.ModAvuMetadataInp;
import org.irods.jargon.core.protovalues.FilePermissionEnum;
import org.irods.jargon.core.protovalues.UserTypeEnum;
import org.irods.jargon.core.pub.BulkAVUOperationResponse.ResultStatus;
import org.irods.jargon.core.pub.RuleProcessingAO.RuleProcessingType;
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
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType;
import org.irods.jargon.core.query.GenQueryBuilderException;
import org.irods.jargon.core.query.GenQueryField.SelectFieldTypes;
import org.irods.jargon.core.query.IRODSGenQueryBuilder;
import org.irods.jargon.core.query.IRODSGenQueryFromBuilder;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSet;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.core.query.MetaDataAndDomainData.MetadataDomain;
import org.irods.jargon.core.query.QueryConditionOperators;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.core.query.SpecificQuery;
import org.irods.jargon.core.query.SpecificQueryResultSet;
import org.irods.jargon.core.rule.IRODSRuleExecResult;
import org.irods.jargon.core.rule.IRODSRuleParameter;
import org.irods.jargon.core.rule.RuleInvocationConfiguration;
import org.irods.jargon.core.utils.AccessObjectQueryProcessingUtils;
import org.irods.jargon.core.utils.CollectionAndPath;
import org.irods.jargon.core.utils.FederationEnabled;
import org.irods.jargon.core.utils.IRODSDataConversionUtil;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.irods.jargon.core.utils.RuleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Access object handles various operations for an IRODS Collection.
 * <p>
 * Note that traditional file io per the java.io.* interfaces is handled through
 * the objects in the {@code org.irods.jargon.core.pub.io} package. This class
 * represents operations that are outside of the contracts one would expect from
 * an {@code java.io.File} object or the various streams.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public final class CollectionAOImpl extends FileCatalogObjectAOImpl implements CollectionAO {

	public static final String SHOW_COLL_ACLS = "ShowCollAcls";
	public static final String ERROR_IN_COLECTION_QUERY = "An error occurred in the query for the collection";
	private final IRODSFileFactory irodsFileFactory = new IRODSFileFactoryImpl(getIRODSSession(), getIRODSAccount());
	private final IRODSGenQueryExecutor irodsGenQueryExecutor = new IRODSGenQueryExecutorImpl(getIRODSSession(),
			getIRODSAccount());
	public static final Logger log = LoggerFactory.getLogger(CollectionAOImpl.class);

	/**
	 * Default constructor
	 *
	 * @param irodsSession {@link IRODSSession}
	 * @param irodsAccount {@link IRODSAccount}
	 * @throws JargonException for iRODS error
	 */
	protected CollectionAOImpl(final IRODSSession irodsSession, final IRODSAccount irodsAccount)
			throws JargonException {
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
	public IRODSFile instanceIRODSFileForCollectionPath(final String collectionPath) throws JargonException {
		log.info("returning a collection for path: {}", collectionPath);
		final IRODSFile collection = irodsFileFactory.instanceIRODSFile(collectionPath);
		return collection;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.CollectionAO#findDomainByMetadataQuery(java
	 * .util.List)
	 */
	@Override
	public List<Collection> findDomainByMetadataQuery(final List<AVUQueryElement> avuQueryElements)
			throws JargonQueryException, JargonException {

		return findDomainByMetadataQuery(avuQueryElements, 0);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.CollectionAO#findDomainByMetadataQuery(java
	 * .util.List, int)
	 */
	@Override
	public List<Collection> findDomainByMetadataQuery(final List<AVUQueryElement> avuQueryElements,
			final int partialStartIndex) throws JargonQueryException, JargonException {

		return findDomainByMetadataQuery(avuQueryElements, partialStartIndex, false);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.CollectionAO#findDomainByMetadataQuery(java
	 * .util.List, int, boolean)
	 */
	@Override
	public List<Collection> findDomainByMetadataQuery(final List<AVUQueryElement> avuQueryElements,
			final int partialStartIndex, final boolean caseInsensitive) throws JargonQueryException, JargonException {

		log.info("findDomainByMetadataQuery()");

		if (avuQueryElements == null || avuQueryElements.isEmpty()) {
			throw new IllegalArgumentException("null or empty avuQueryElements");
		}

		if (caseInsensitive) {

			/*
			 * It's a long story, but I need to check if this is eirods, otherwise I may not
			 * properly comprehend if case-insensitive queries are supported
			 */

			if (getIRODSServerProperties().isSupportsCaseInsensitiveQueries()) {
				log.info("this is eirods, case insensitive is supported");
			} else {
				throw new JargonException("case insensitive queries not supported on this iRODS version");
			}
		}

		log.info("avuQueryElements:{}", avuQueryElements);
		log.info("partialStartIndex:}{}", partialStartIndex);
		log.info("caseInsensitive:{}", caseInsensitive);

		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, caseInsensitive, null);
		IRODSQueryResultSetInterface resultSet;

		try {
			CollectionAOHelper.buildSelectsByAppendingToBuilder(builder);
			builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_META_COLL_ATTR_NAME)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_META_COLL_ATTR_VALUE)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_META_COLL_ATTR_UNITS);

			for (AVUQueryElement queryElement : avuQueryElements) {
				CollectionAOHelper.appendConditionPartToBuilderQuery(queryElement, builder);
			}

			IRODSGenQueryFromBuilder irodsQuery = builder
					.exportIRODSQueryFromBuilder(getJargonProperties().getMaxFilesAndDirsQueryMax());

			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(irodsQuery, partialStartIndex);
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
	 * @see org.irods.jargon.core.pub.CollectionAO#findMetadataValuesByMetadataQuery
	 * (java.util.List, int)
	 */
	@Override
	public List<MetaDataAndDomainData> findMetadataValuesByMetadataQuery(final List<AVUQueryElement> avuQuery,
			final int offset) throws JargonQueryException, JargonException {
		return findMetadataValuesByMetadataQueryForCollection(avuQuery, "", offset, false);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.CollectionAO#findMetadataValuesByMetadataQuery
	 * (java.util.List, int, boolean)
	 */
	@Override
	public List<MetaDataAndDomainData> findMetadataValuesByMetadataQuery(final List<AVUQueryElement> avuQuery,
			final int offset, final boolean caseInsensitive) throws JargonQueryException, JargonException {
		return findMetadataValuesByMetadataQueryForCollection(avuQuery, "", offset, caseInsensitive);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.CollectionAO#findMetadataValuesByMetadataQuery
	 * (java.util.List)
	 */
	@Override
	public List<MetaDataAndDomainData> findMetadataValuesByMetadataQuery(final List<AVUQueryElement> avuQuery)
			throws JargonQueryException, JargonException {
		return findMetadataValuesByMetadataQueryForCollection(avuQuery, "");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.CollectionAO#findMetadataValuesByMetadataQuery
	 * (java.util.List, boolean)
	 */
	@Override
	public List<MetaDataAndDomainData> findMetadataValuesByMetadataQuery(final List<AVUQueryElement> avuQuery,
			final boolean caseInsensitive) throws JargonQueryException, JargonException {
		return findMetadataValuesByMetadataQueryForCollection(avuQuery, "", 0, caseInsensitive);
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
			final List<AVUQueryElement> avuQuery, final String collectionAbsolutePath)
			throws JargonQueryException, JargonException {

		return findMetadataValuesByMetadataQueryForCollection(avuQuery, collectionAbsolutePath, 0);
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
			final List<AVUQueryElement> avuQuery, final String collectionAbsolutePath, final int partialStartIndex)
			throws JargonQueryException, JargonException {

		return findMetadataValuesByMetadataQueryForCollection(avuQuery, collectionAbsolutePath, partialStartIndex,
				false);
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
			final List<AVUQueryElement> avuQuery, final String collectionAbsolutePath, final int partialStartIndex,
			final boolean caseInsensitive) throws FileNotFoundException, JargonQueryException, JargonException {

		if (avuQuery == null || avuQuery.isEmpty()) {
			throw new IllegalArgumentException("null or empty query");
		}

		if (collectionAbsolutePath == null) {
			throw new IllegalArgumentException("Null absolutePath for collection");
		}

		if (caseInsensitive) {
			if (!getIRODSServerProperties().isSupportsCaseInsensitiveQueries()) {
				throw new JargonException("case insensitive queries not supported on this iRODS version");
			}
		}

		String myPath = MiscIRODSUtils.normalizeIrodsPath(collectionAbsolutePath);

		log.info("absPath for querying iCAT:{}", myPath);

		log.info("building a metadata query for: {}", avuQuery);

		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, caseInsensitive, null);
		IRODSQueryResultSetInterface resultSet;

		try {
			addSelectsForMetadataAndDomainDataToBuilder(builder);

			if (!myPath.isEmpty()) {
				builder.addConditionAsGenQueryField(RodsGenQueryEnum.COL_COLL_NAME, QueryConditionOperators.EQUAL,
						myPath);
			}

			for (AVUQueryElement queryElement : avuQuery) {
				CollectionAOHelper.appendConditionPartToBuilderQuery(queryElement, builder);
			}

			IRODSGenQueryFromBuilder irodsQuery = builder
					.exportIRODSQueryFromBuilder(getJargonProperties().getMaxFilesAndDirsQueryMax());

			if (myPath.isEmpty()) {

				resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(irodsQuery, partialStartIndex);
			} else {

				resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResultInZone(irodsQuery, partialStartIndex,
						MiscIRODSUtils.getZoneInPath(myPath));
			}
		} catch (GenQueryBuilderException e) {
			log.error("error building query", e);
			throw new JargonException("error building query", e);
		} catch (JargonQueryException jqe) {
			log.error("error executing query", jqe);
			throw new JargonException("error executing query", jqe);
		}

		return AccessObjectQueryProcessingUtils.buildMetaDataAndDomainDatalistFromResultSet(MetadataDomain.COLLECTION,
				resultSet);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.CollectionAO#addBulkAVUMetadataToCollection
	 * (java.lang.String, java.util.List)
	 */
	@Override
	public List<BulkAVUOperationResponse> addBulkAVUMetadataToCollection(final String absolutePath,
			final List<AvuData> avuData) throws JargonException {

		log.info("addBulkAVUMetadataToCollection()");

		if (absolutePath == null || absolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolute path");
		}

		if (avuData == null || avuData.isEmpty()) {
			throw new IllegalArgumentException("null or empty avuData");
		}

		List<BulkAVUOperationResponse> responses = new ArrayList<BulkAVUOperationResponse>();

		for (AvuData value : avuData) {
			try {
				addAVUMetadata(absolutePath, value);
			} catch (FileNotFoundException dnf) {
				log.error("FileNotFoundException when adding an AVU, catch and add to response data", dnf);
				responses.add(BulkAVUOperationResponse.instance(ResultStatus.MISSING_METADATA_TARGET, value,
						dnf.getMessage()));
				continue;
			} catch (DuplicateDataException dde) {
				log.error("DuplicateDataException when adding an AVU, catch and add to response data", dde);
				responses.add(BulkAVUOperationResponse.instance(ResultStatus.DUPLICATE_AVU, value, dde.getMessage()));
				continue;

			}

			log.info("treat as success...", value);
			responses.add(BulkAVUOperationResponse.instance(ResultStatus.OK, value, ""));
		}

		log.info("...complete");
		return responses;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.irods.jargon.core.pub.CollectionAO#deleteBulkAVUMetadataFromCollection
	 * (java.lang.String, java.util.List)
	 */
	@Override
	public List<BulkAVUOperationResponse> deleteBulkAVUMetadataFromCollection(final String absolutePath,
			final List<AvuData> avuData) throws JargonException {

		log.info("deleteBulkAVUMetadataToCollection()");

		if (avuData == null || avuData.isEmpty()) {
			throw new IllegalArgumentException("null or empty avuData");
		}

		List<BulkAVUOperationResponse> responses = new ArrayList<BulkAVUOperationResponse>();

		for (AvuData value : avuData) {
			try {
				deleteAVUMetadata(absolutePath, value);
			} catch (FileNotFoundException dnf) {
				log.error("FileNotFoundException when deleti an AVU, catch and add to response data", dnf);
				responses.add(BulkAVUOperationResponse.instance(ResultStatus.MISSING_METADATA_TARGET, value,
						dnf.getMessage()));
				continue;

			}

			log.info("treat as success...", value);
			responses.add(BulkAVUOperationResponse.instance(ResultStatus.OK, value, ""));
		}

		log.info("...complete");
		return responses;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.CollectionAO#addAVUMetadata(java.lang.String,
	 * org.irods.jargon.core.pub.domain.AvuData)
	 */
	@Override
	public void addAVUMetadata(final String absolutePath, final AvuData avuData)
			throws FileNotFoundException, DuplicateDataException, JargonException {

		if (absolutePath == null || absolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolutePath");
		}

		if (avuData == null) {
			throw new IllegalArgumentException("null AVU data");
		}

		log.info("adding avu metadata to collection: {}", avuData);
		log.info("absolute path: {}", absolutePath);

		String myPath = MiscIRODSUtils.normalizeIrodsPath(absolutePath);

		final ModAvuMetadataInp modifyAvuMetadataInp = ModAvuMetadataInp.instanceForAddCollectionMetadata(myPath,
				avuData);

		log.debug("sending avu request");

		try {

			getIRODSProtocol().irodsFunction(modifyAvuMetadataInp);

		} catch (JargonException je) {

			if (je.getMessage().indexOf("-814000") > -1) {
				throw new DataNotFoundException("Target collection was not found, could not add AVU");
			} else if (je.getMessage().indexOf("-809000") > -1) {
				throw new DuplicateDataException("Duplicate AVU exists, cannot add");
			}

			log.error("jargon exception adding AVU metadata", je);
			throw je;
		}

		log.debug("metadata added");

	}

	@Override
	public void setAVUMetadata(final String absolutePath, final AvuData avuData)
			throws FileNotFoundException, JargonException {

		if (absolutePath == null || absolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolutePath");
		}

		if (avuData == null) {
			throw new IllegalArgumentException("null AVU data");
		}

		log.info("setting avu metadata to collection: {}", avuData);
		log.info("absolute path: {}", absolutePath);

		if (!getIRODSServerProperties().isSupportsMetadataSet()) {
			throw new OperationNotSupportedByThisServerException("metadata set not supported in this iRODS version");
		}

		String myPath = MiscIRODSUtils.normalizeIrodsPath(absolutePath);

		final ModAvuMetadataInp modifyAvuMetadataInp = ModAvuMetadataInp.instanceForSetCollectionMetadata(myPath,
				avuData);

		log.debug("sending avu request");

		try {

			getIRODSProtocol().irodsFunction(modifyAvuMetadataInp);

		} catch (JargonException je) {

			if (je.getMessage().indexOf("-814000") > -1) {
				throw new DataNotFoundException("Target collection was not found, could not add AVU");
			} else if (je.getMessage().indexOf("-809000") > -1) {
				throw new DuplicateDataException("Duplicate AVU exists, cannot add");
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
	 * org.irods.jargon.core.pub.CollectionAO#deleteAVUMetadata(java.lang.String ,
	 * org.irods.jargon.core.pub.domain.AvuData)
	 */
	@Override
	public void deleteAVUMetadata(final String absolutePath, final AvuData avuData)
			throws FileNotFoundException, JargonException {
		if (absolutePath == null || absolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolutePath");
		}

		if (avuData == null) {
			throw new IllegalArgumentException("null AVU data");
		}

		String myPath = MiscIRODSUtils.normalizeIrodsPath(absolutePath);

		log.info("deleting avu metadata from collection: {}", avuData);
		log.info("absolute path: {}", myPath);

		final ModAvuMetadataInp modifyAvuMetadataInp = ModAvuMetadataInp.instanceForDeleteCollectionMetadata(myPath,
				avuData);

		log.debug("sending avu request");

		try {
			getIRODSProtocol().irodsFunction(modifyAvuMetadataInp);
		} catch (JargonException je) {

			if (je.getMessage().indexOf("-814000") > -1) {
				throw new FileNotFoundException("Target collection was not found, could not remove AVU");
			}

			log.error("jargon exception removing AVU metadata", je);
			throw je;
		}

		log.debug("metadata removed");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.CollectionAO#deleteAllAVUMetadata(java.lang
	 * .String)
	 */
	@Override
	public void deleteAllAVUMetadata(final String absolutePath) throws FileNotFoundException, JargonException {

		log.info("deleteAllAVUMetadata");

		if (absolutePath == null || absolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolutePath");
		}

		log.info("absolute path: {}", absolutePath);

		ObjStat objStat = this.retrieveObjStat(absolutePath);

		if (objStat.getSpecColType() == SpecColType.MOUNTED_COLL) {
			log.info("objStat indicates collection type that does not support this operation:{}", objStat);
			return;
		}

		List<MetaDataAndDomainData> metadatas;
		try {
			metadatas = this.findMetadataValuesForCollection(objStat, 0);
		} catch (JargonQueryException e) {
			throw new JargonException(e);
		}

		List<AvuData> avusToDelete = new ArrayList<AvuData>();

		for (MetaDataAndDomainData metadata : metadatas) {
			avusToDelete
					.add(AvuData.instance(metadata.getAvuAttribute(), metadata.getAvuValue(), metadata.getAvuUnit()));
		}

		if (avusToDelete.isEmpty()) {
			log.debug("no metadata to delete");
		} else {
			deleteBulkAVUMetadataFromCollection(objStat.getAbsolutePath(), avusToDelete);
			log.debug("metadata removed");
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.CollectionAO#
	 * modifyAvuValueBasedOnGivenAttributeAndUnit(java.lang.String,
	 * org.irods.jargon.core.pub.domain.AvuData)
	 */
	@Override
	public void modifyAvuValueBasedOnGivenAttributeAndUnit(final String absolutePath, final AvuData avuData)
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

		String myPath = MiscIRODSUtils.normalizeIrodsPath(absolutePath);

		// avu is distinct based on attrib and value, so do an attrib/unit
		// query, can only be one result
		List<AVUQueryElement> queryElements = new ArrayList<AVUQueryElement>();
		List<MetaDataAndDomainData> result;

		try {
			queryElements.add(AVUQueryElement.instanceForValueQuery(AVUQueryElement.AVUQueryPart.ATTRIBUTE,
					QueryConditionOperators.EQUAL, avuData.getAttribute()));
			queryElements.add(AVUQueryElement.instanceForValueQuery(AVUQueryElement.AVUQueryPart.UNITS,
					QueryConditionOperators.EQUAL, avuData.getUnit()));
			result = this.findMetadataValuesByMetadataQueryForCollection(queryElements, myPath);

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

		AvuData currentAvuData = new AvuData(result.get(0).getAvuAttribute(), result.get(0).getAvuValue(),
				result.get(0).getAvuUnit());

		AvuData modAvuData = new AvuData(result.get(0).getAvuAttribute(), avuData.getValue(),
				result.get(0).getAvuUnit());
		modifyAVUMetadata(myPath, currentAvuData, modAvuData);
		log.info("metadata modified to:{}", modAvuData);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.irods.jargon.core.pub.CollectionAO#modifyAVUMetadata(java.lang.String ,
	 * org.irods.jargon.core.pub.domain.AvuData,
	 * org.irods.jargon.core.pub.domain.AvuData)
	 */
	@Override
	public void modifyAVUMetadata(final String absolutePath, final AvuData currentAvuData, final AvuData newAvuData)
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

		String myPath = MiscIRODSUtils.normalizeIrodsPath(absolutePath);

		final ModAvuMetadataInp modifyAvuMetadataInp = ModAvuMetadataInp.instanceForModifyCollectionMetadata(myPath,
				currentAvuData, newAvuData);

		log.debug("sending avu request");

		try {

			getIRODSProtocol().irodsFunction(modifyAvuMetadataInp);

		} catch (JargonException je) {

			if (je.getMessage().indexOf("-814000") > -1) {
				throw new DataNotFoundException("Target collection was not found, could not modify AVU");
			}

			log.error("jargon exception modifying AVU metadata", je);
			throw je;
		}

		log.debug("metadata rewritten");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.CollectionAO#findMetadataValuesForCollection
	 * (java.lang.String, int)
	 */
	@Override
	public List<MetaDataAndDomainData> findMetadataValuesForCollection(final String collectionAbsolutePath,
			final int partialStartIndex) throws FileNotFoundException, JargonException, JargonQueryException {

		if (collectionAbsolutePath == null || collectionAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty collectionAbsolutePath");
		}

		if (partialStartIndex < 0) {
			throw new IllegalArgumentException("partialStartIndex must be 0 or greater, set to 0 if no offset desired");
		}

		log.info("find metadata values for collection:{}", collectionAbsolutePath);
		log.info("with partial start of:{}", partialStartIndex);

		ObjStat objStat = getObjectStatForAbsolutePath(collectionAbsolutePath);
		return findMetadataValuesForCollection(objStat, partialStartIndex);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.CollectionAO#
	 * findMetadataValueForCollectionByMetadataId(java.lang.String, int)
	 */
	@Override
	public MetaDataAndDomainData findMetadataValueForCollectionByMetadataId(final String collectionAbsolutePath,
			final int id) throws FileNotFoundException, DataNotFoundException, JargonException {

		log.info("findMetadataValueForCollectionByMetadataId()");

		if (collectionAbsolutePath == null || collectionAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty collectionAbsolutePath");
		}

		if (id < 0) {
			throw new IllegalArgumentException("id must be 0 or greater");
		}

		log.info("find metadata values for collection:{}", collectionAbsolutePath);
		log.info("with id:{}", id);

		ObjStat objStat = getObjectStatForAbsolutePath(collectionAbsolutePath);
		return findMetadataValueForCollectionById(objStat, id);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.irods.jargon.core.pub.CollectionAO#findMetadataValueForCollectionById
	 * (org.irods.jargon.core.pub.domain.ObjStat, int)
	 */
	@Override
	public MetaDataAndDomainData findMetadataValueForCollectionById(final ObjStat objStat, final int id)
			throws DataNotFoundException, JargonException {

		if (objStat == null) {
			throw new IllegalArgumentException("null or empty objStat");
		}

		if (id < 0) {
			throw new IllegalArgumentException("id must be 0 or greater");
		}

		log.info("find metadata values for collection:{}", objStat);
		log.info("with id of:{}", id);

		String absPath;

		absPath = MiscIRODSUtils.determineAbsolutePathBasedOnCollTypeInObjectStat(objStat);

		log.info("absPath for querying iCAT:{}", absPath);

		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);
		IRODSQueryResultSetInterface resultSet;

		try {
			addSelectsForMetadataAndDomainDataToBuilder(builder)
					.addConditionAsGenQueryField(RodsGenQueryEnum.COL_COLL_NAME, QueryConditionOperators.EQUAL, absPath)
					.addConditionAsGenQueryField(RodsGenQueryEnum.COL_META_COLL_ATTR_ID, QueryConditionOperators.EQUAL,
							id);

			IRODSGenQueryFromBuilder irodsQuery = builder
					.exportIRODSQueryFromBuilder(getJargonProperties().getMaxFilesAndDirsQueryMax());

			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResultInZone(irodsQuery, 0,
					MiscIRODSUtils.getZoneInPath(absPath));

			return AccessObjectQueryProcessingUtils.buildMetaDataAndDomainDataFromResultSetRow(
					MetaDataAndDomainData.MetadataDomain.COLLECTION, resultSet.getFirstResult(), 1);

		} catch (GenQueryBuilderException e) {
			log.error("error building query", e);
			throw new JargonException("error building query", e);
		} catch (JargonQueryException jqe) {
			log.error("error executing query", jqe);
			throw new JargonException("error executing query", jqe);
		}

	}

	private IRODSGenQueryBuilder addSelectsForMetadataAndDomainDataToBuilder(final IRODSGenQueryBuilder builder)
			throws GenQueryBuilderException {
		return builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_ID)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_NAME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_CREATE_TIME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_MODIFY_TIME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_META_COLL_ATTR_NAME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_META_COLL_ATTR_VALUE)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_META_COLL_ATTR_UNITS)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_META_COLL_ATTR_ID);
	}

	private List<MetaDataAndDomainData> findMetadataValuesForCollection(final ObjStat objStat,
			final int partialStartIndex) throws FileNotFoundException, JargonException, JargonQueryException {

		if (objStat == null) {
			throw new IllegalArgumentException("null or empty objStat");
		}

		if (partialStartIndex < 0) {
			throw new IllegalArgumentException("partialStartIndex must be 0 or greater, set to 0 if no offset desired");
		}

		log.info("find metadata values for collection:{}", objStat);
		log.info("with partial start of:{}", partialStartIndex);

		String absPath;

		absPath = MiscIRODSUtils.determineAbsolutePathBasedOnCollTypeInObjectStat(objStat);

		log.info("absPath for querying iCAT:{}", absPath);

		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);
		IRODSQueryResultSetInterface resultSet;

		try {
			addSelectsForMetadataAndDomainDataToBuilder(builder).addConditionAsGenQueryField(
					RodsGenQueryEnum.COL_COLL_NAME, QueryConditionOperators.EQUAL, absPath);

			IRODSGenQueryFromBuilder irodsQuery = builder
					.exportIRODSQueryFromBuilder(getJargonProperties().getMaxFilesAndDirsQueryMax());

			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResultInZone(irodsQuery, partialStartIndex,
					MiscIRODSUtils.getZoneInPath(absPath));

		} catch (GenQueryBuilderException e) {
			log.error("error building query", e);
			throw new JargonException("error building query", e);
		} catch (JargonQueryException jqe) {
			log.error("error executing query", jqe);
			throw new JargonException("error executing query", jqe);
		}

		return AccessObjectQueryProcessingUtils.buildMetaDataAndDomainDatalistFromResultSet(MetadataDomain.COLLECTION,
				resultSet);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.CollectionAO#findMetadataValuesForCollection
	 * (java.lang.String)
	 */
	@Override
	public List<MetaDataAndDomainData> findMetadataValuesForCollection(final String collectionAbsolutePath)
			throws JargonException, JargonQueryException {

		return findMetadataValuesForCollection(collectionAbsolutePath, 0);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.CollectionAO#findGivenObjStat(org.irods.jargon
	 * .core.pub.domain.ObjStat)
	 */
	@Override
	public Collection findGivenObjStat(final ObjStat objStat) throws DataNotFoundException, JargonException {

		log.info("findGivenObjStat()");

		if (objStat == null) {
			throw new IllegalArgumentException("null objStat");
		}

		log.info("objStat:{}", objStat);

		if (!objStat.isSomeTypeOfCollection()) {
			log.error("objStat is not for a collection, wrong method called:{}", objStat);
			throw new JargonException("object is not a collection");
		}

		// make sure this special coll type has support
		MiscIRODSUtils.evaluateSpecCollSupport(objStat);

		// get absolute path to use for querying iCAT (could be a soft link)
		String absPath = MiscIRODSUtils.determineAbsolutePathBasedOnCollTypeInObjectStat(objStat);

		log.info("absPath for querying iCAT:{}", absPath);

		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);
		IRODSQueryResultSetInterface resultSet;

		try {
			CollectionAOHelper.buildSelectsByAppendingToBuilder(builder);
			builder.addConditionAsGenQueryField(RodsGenQueryEnum.COL_COLL_NAME, QueryConditionOperators.EQUAL, absPath);
			IRODSGenQueryFromBuilder irodsQuery = builder.exportIRODSQueryFromBuilder(1);

			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResultInZone(irodsQuery, 0,
					MiscIRODSUtils.getZoneInPath(absPath));
		} catch (GenQueryBuilderException e) {
			log.error("builder exception in query", e);
			throw new JargonException("error in query", e);
		} catch (JargonQueryException e) {
			log.error(" exception in query", e);
			throw new JargonException("error in query", e);
		}

		List<Collection> collectionList = CollectionAOHelper.buildListFromResultSet(resultSet);

		Collection collection;
		if (collectionList.size() == 0) {
			log.info("No collection found for path, see if heuristic path guessing was done:{}", absPath);
			if (objStat.getObjectType() == ObjectType.COLLECTION_HEURISTIC_STANDIN) {
				log.info(
						"collection is a stand in proxy for a hierarchy layer the user cannot see, generate a proxy collection for:{}",
						objStat);
			}
			collection = new Collection();
			collection.setCollectionId(0);
			collection.setCollectionName(objStat.getAbsolutePath());
			CollectionAndPath collAndPath = MiscIRODSUtils
					.separateCollectionAndPathFromGivenAbsolutePath(objStat.getAbsolutePath());
			collection.setCollectionParentName(collAndPath.getCollectionParent());
			collection.setObjectPath(objStat.getObjectPath());
			collection.setSpecColType(objStat.getSpecColType());
			collection.setProxy(true);
		} else {
			collection = collectionList.get(0);
			if (objStat.getSpecColType() == SpecColType.LINKED_COLL) {
				log.info("this is a special collection,so update the paths and add an object path");
			}

			collection.setObjectPath(objStat.getObjectPath());
			CollectionAndPath collectionAndPath = MiscIRODSUtils
					.separateCollectionAndPathFromGivenAbsolutePath(objStat.getAbsolutePath());

			StringBuilder sb = new StringBuilder();
			sb.append(collectionAndPath.getCollectionParent());
			sb.append("/");
			collection.setCollectionParentName(sb.toString());
			collection.setCollectionName(objStat.getAbsolutePath());
			collection.setSpecColType(objStat.getSpecColType());
		}

		return collection;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.irods.jargon.core.pub.CollectionAO#findByAbsolutePath(java.lang.String )
	 */
	@Override
	public Collection findByAbsolutePath(final String irodsCollectionAbsolutePath)
			throws DataNotFoundException, JargonException {

		log.info("findByAbsolutePath()");

		if (irodsCollectionAbsolutePath == null || irodsCollectionAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty irodsCollectionAbsolutePath");
		}

		log.info("irodsCollectionAbsolutePath:{}", irodsCollectionAbsolutePath);

		ObjStat objStat = this.retrieveObjStat(irodsCollectionAbsolutePath);
		return findGivenObjStat(objStat);

	}

	@Override
	public Collection findById(final int id) throws DataNotFoundException, JargonException {

		log.info("findById() with id:{}", id);

		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);
		IRODSQueryResultSetInterface resultSet;

		try {
			CollectionAOHelper.buildSelectsByAppendingToBuilder(builder);
			builder.addConditionAsGenQueryField(RodsGenQueryEnum.COL_COLL_ID, QueryConditionOperators.EQUAL,
					String.valueOf(id));
			IRODSGenQueryFromBuilder irodsQuery = builder.exportIRODSQueryFromBuilder(1);

			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(irodsQuery, 0);
		} catch (GenQueryBuilderException e) {
			log.error("builder exception in query", e);
			throw new JargonException("error in query", e);
		} catch (JargonQueryException e) {
			log.error(" exception in query", e);
			throw new JargonException("error in query", e);
		}

		return CollectionAOHelper.buildCollectionFromResultSetRow(resultSet.getFirstResult());

	}

	/*
	 * (non-Javadoc)
	 *
	 * @seeorg.irods.jargon.core.pub.CollectionAO#
	 * countAllFilesUnderneathTheGivenCollection(java.lang.String)
	 */
	@Override
	public int countAllFilesUnderneathTheGivenCollection(final String irodsCollectionAbsolutePath)
			throws FileNotFoundException, JargonException {

		if (irodsCollectionAbsolutePath == null) {
			throw new IllegalArgumentException("irodsCollectionAbsolutePath is null");
		}

		ObjStat objStat = collectionAndDataObjectListAndSearchAO.retrieveObjectStatForPath(irodsCollectionAbsolutePath);

		/*
		 * See if jargon supports the given object type
		 */
		MiscIRODSUtils.evaluateSpecCollSupport(objStat);

		String effectiveAbsolutePath = MiscIRODSUtils.determineAbsolutePathBasedOnCollTypeInObjectStat(objStat);
		log.info("determined effectiveAbsolutePathToBe:{}", effectiveAbsolutePath);

		// I cannot get children if this is not a directory (a file has no
		// children)
		if (!objStat.isSomeTypeOfCollection()) {
			log.error("this is a file, not a directory, and therefore I cannot get a count of the children: {}",
					irodsCollectionAbsolutePath);
			throw new JargonException(
					"attempting to count children under a file at path:" + irodsCollectionAbsolutePath);
		}

		IRODSGenQueryExecutor irodsGenQueryExecutor = new IRODSGenQueryExecutorImpl(getIRODSSession(),
				getIRODSAccount());

		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);
		IRODSQueryResultSetInterface resultSet;

		try {
			builder.addSelectAsAgregateGenQueryValue(RodsGenQueryEnum.COL_COLL_NAME, SelectFieldTypes.COUNT)
					.addSelectAsAgregateGenQueryValue(RodsGenQueryEnum.COL_DATA_NAME, SelectFieldTypes.COUNT)
					.addConditionAsGenQueryField(RodsGenQueryEnum.COL_DATA_REPL_NUM, QueryConditionOperators.EQUAL, 0)
					.addConditionAsGenQueryField(RodsGenQueryEnum.COL_COLL_NAME, QueryConditionOperators.LIKE,
							effectiveAbsolutePath + "%");
			IRODSGenQueryFromBuilder irodsQuery = builder.exportIRODSQueryFromBuilder(1);

			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResultInZone(irodsQuery, 0,
					MiscIRODSUtils.getZoneInPath(effectiveAbsolutePath));
		} catch (JargonQueryException e) {
			log.error("error in query", e);
			throw new JargonException("error in exists query", e);
		} catch (GenQueryBuilderException e) {
			log.error("error in query", e);
			throw new JargonException("error in exists query", e);
		}

		int fileCtr = 0;

		if (resultSet.getResults().size() > 0) {
			fileCtr = IRODSDataConversionUtil.getIntOrZeroFromIRODSValue(resultSet.getFirstResult().getColumn(0));
		}

		return fileCtr;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.CollectionAO#setAccessPermissionInherit(java
	 * .lang.String, java.lang.String, boolean)
	 */
	@Override
	public void setAccessPermissionInherit(final String zone, final String absolutePath, final boolean recursive)
			throws JargonException {

		if (zone == null) {
			throw new IllegalArgumentException("null zone");
		}

		if (absolutePath == null || absolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolutePath");
		}

		// pi tests parameters
		log.info("setAccessPermissionInherit on absPath:{}", absolutePath);

		String effectiveAbsPath = resolveAbsolutePathViaObjStat(absolutePath);
		boolean collNeedsRecursive = adjustRecursiveOption(effectiveAbsPath, recursive);

		ModAccessControlInp modAccessControlInp = ModAccessControlInp
				.instanceForSetInheritOnACollection(collNeedsRecursive, zone, effectiveAbsPath);
		getIRODSProtocol().irodsFunction(modAccessControlInp);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.CollectionAO#setAccessPermissionInheritAsAdmin
	 * (java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public void setAccessPermissionInheritAsAdmin(final String zone, final String absolutePath, final boolean recursive)
			throws JargonException {

		if (zone == null) {
			throw new IllegalArgumentException("null zone");
		}

		if (absolutePath == null || absolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolutePath");
		}

		// pi tests parameters
		log.info("setAccessPermissionInherit on absPath:{}", absolutePath);

		String effectiveAbsPath = resolveAbsolutePathViaObjStat(absolutePath);
		boolean collNeedsRecursive = adjustRecursiveOption(effectiveAbsPath, recursive);

		ModAccessControlInp modAccessControlInp = ModAccessControlInp
				.instanceForSetInheritOnACollectionInAdminMode(collNeedsRecursive, zone, effectiveAbsPath);
		getIRODSProtocol().irodsFunction(modAccessControlInp);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.CollectionAO#setAccessPermissionToNotInherit
	 * (java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public void setAccessPermissionToNotInherit(final String zone, final String absolutePath, final boolean recursive)
			throws JargonException {

		if (zone == null) {
			throw new IllegalArgumentException("null zone");
		}

		if (absolutePath == null || absolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolutePath");
		}

		// pi tests parameters
		log.info("setAccessPermissionToNotInherit on absPath:{}", absolutePath);

		String effectiveAbsPath = resolveAbsolutePathViaObjStat(absolutePath);

		boolean collNeedsRecursive = adjustRecursiveOption(effectiveAbsPath, recursive);

		ModAccessControlInp modAccessControlInp = ModAccessControlInp
				.instanceForSetNoInheritOnACollection(collNeedsRecursive, zone, effectiveAbsPath);
		getIRODSProtocol().irodsFunction(modAccessControlInp);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.CollectionAO#
	 * setAccessPermissionToNotInheritInAdminMode(java.lang.String,
	 * java.lang.String, boolean)
	 */
	@Override
	public void setAccessPermissionToNotInheritInAdminMode(final String zone, final String absolutePath,
			final boolean recursive) throws JargonException {

		if (zone == null) {
			throw new IllegalArgumentException("null zone");
		}

		if (absolutePath == null || absolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolutePath");
		}

		// pi tests parameters
		log.info("setAccessPermissionToNotInherit on absPath:{}", absolutePath);

		String effectiveAbsPath = resolveAbsolutePathViaObjStat(absolutePath);

		boolean collNeedsRecursive = adjustRecursiveOption(effectiveAbsPath, recursive);

		ModAccessControlInp modAccessControlInp = ModAccessControlInp
				.instanceForSetNoInheritOnACollectionInAdminMode(collNeedsRecursive, zone, effectiveAbsPath);
		getIRODSProtocol().irodsFunction(modAccessControlInp);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.CollectionAO#setAccessPermissionRead(java.lang
	 * .String, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	@FederationEnabled
	public void setAccessPermissionRead(final String zone, final String absolutePath, final String userName,
			final boolean recursive) throws JargonException {

		if (zone == null) {
			throw new IllegalArgumentException("null zone");
		}

		if (absolutePath == null || absolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolutePath");
		}

		// pi tests parameters
		log.info("setAccessPermissionRead on absPath:{}", absolutePath);

		String effectiveAbsPath = resolveAbsolutePathViaObjStat(absolutePath);

		boolean collNeedsRecursive = adjustRecursiveOption(effectiveAbsPath, recursive);

		ModAccessControlInp modAccessControlInp = ModAccessControlInp.instanceForSetPermission(collNeedsRecursive, zone,
				effectiveAbsPath, userName, ModAccessControlInp.READ_PERMISSION);
		getIRODSProtocol().irodsFunction(modAccessControlInp);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.CollectionAO#setAccessPermissionReadAsAdmin
	 * (java.lang.String, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public void setAccessPermissionReadAsAdmin(final String zone, final String absolutePath, final String userName,
			final boolean recursive) throws JargonException {

		if (zone == null) {
			throw new IllegalArgumentException("null zone");
		}

		if (absolutePath == null || absolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolutePath");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		// pi tests parameters
		log.info("setAccessPermissionReadAsAdmin on absPath:{}", absolutePath);

		String effectiveAbsPath = resolveAbsolutePathViaObjStat(absolutePath);

		boolean collNeedsRecursive = adjustRecursiveOption(effectiveAbsPath, recursive);

		ModAccessControlInp modAccessControlInp = ModAccessControlInp.instanceForSetPermissionInAdminMode(
				collNeedsRecursive, zone, effectiveAbsPath, userName, ModAccessControlInp.READ_PERMISSION);
		getIRODSProtocol().irodsFunction(modAccessControlInp);

	}

	@Override
	public void setAccessPermission(final String zone, final String absolutePath, final String userName,
			final boolean recursive, final FilePermissionEnum filePermission) throws JargonException {

		if (zone == null) {
			throw new IllegalArgumentException("null zone");
		}

		if (absolutePath == null || absolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolutePath");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		if (filePermission == null) {
			throw new IllegalArgumentException("null filePermission");
		}

		// right now, own, read, write are only permission I can set

		if (filePermission == FilePermissionEnum.OWN) {
			setAccessPermissionOwn(zone, absolutePath, userName, recursive);
		} else if (filePermission == FilePermissionEnum.READ) {
			setAccessPermissionRead(zone, absolutePath, userName, recursive);
		} else if (filePermission == FilePermissionEnum.WRITE) {
			setAccessPermissionWrite(zone, absolutePath, userName, recursive);
		} else if (filePermission == FilePermissionEnum.NONE) {
			removeAccessPermissionForUser(zone, absolutePath, userName, recursive);
		} else {
			throw new JargonException(
					"Cannot update permission, currently only READ, WRITE, and OWN, and NONE are supported");
		}

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.CollectionAO#setAccessPermissionWrite(java.
	 * lang.String, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public void setAccessPermissionWrite(final String zone, final String absolutePath, final String userName,
			final boolean recursive) throws JargonException {

		if (zone == null) {
			throw new IllegalArgumentException("null zone");
		}

		if (absolutePath == null || absolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolutePath");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		// pi tests parameters
		log.info("setAccessPermissionWrite on absPath:{}", absolutePath);
		// overhead iRODS behavior, if you set perm with recursive when no
		// children, then won't take

		String effectiveAbsPath = resolveAbsolutePathViaObjStat(absolutePath);

		boolean collNeedsRecursive = adjustRecursiveOption(effectiveAbsPath, recursive);

		ModAccessControlInp modAccessControlInp = ModAccessControlInp.instanceForSetPermission(collNeedsRecursive, zone,
				effectiveAbsPath, userName, ModAccessControlInp.WRITE_PERMISSION);
		getIRODSProtocol().irodsFunction(modAccessControlInp);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.CollectionAO#setAccessPermissionWriteAsAdmin
	 * (java.lang.String, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public void setAccessPermissionWriteAsAdmin(final String zone, final String absolutePath, final String userName,
			final boolean recursive) throws JargonException {

		if (zone == null) {
			throw new IllegalArgumentException("null zone");
		}

		if (absolutePath == null || absolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolutePath");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		// pi tests parameters
		log.info("setAccessPermissionWriteAsAdmin on absPath:{}", absolutePath);
		String effectiveAbsPath = resolveAbsolutePathViaObjStat(absolutePath);

		// overhead iRODS behavior, if you set perm with recursive when no
		// children, then won't take
		boolean collNeedsRecursive = adjustRecursiveOption(effectiveAbsPath, recursive);

		ModAccessControlInp modAccessControlInp = ModAccessControlInp.instanceForSetPermissionInAdminMode(
				collNeedsRecursive, zone, effectiveAbsPath, userName, ModAccessControlInp.WRITE_PERMISSION);
		getIRODSProtocol().irodsFunction(modAccessControlInp);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.CollectionAO#setAccessPermissionOwn(java.lang
	 * .String, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public void setAccessPermissionOwn(final String zone, final String absolutePath, final String userName,
			final boolean recursive) throws JargonException {

		if (zone == null) {
			throw new IllegalArgumentException("null zone");
		}

		if (absolutePath == null || absolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolutePath");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		// pi tests parameters
		log.info("setAccessPermissionOwn on absPath:{}", absolutePath);

		String effectiveAbsPath = resolveAbsolutePathViaObjStat(absolutePath);

		// overhead iRODS behavior, if you set perm with recursive when no
		// children, then won't take
		boolean collNeedsRecursive = adjustRecursiveOption(effectiveAbsPath, recursive);

		ModAccessControlInp modAccessControlInp = ModAccessControlInp.instanceForSetPermission(collNeedsRecursive, zone,
				effectiveAbsPath, userName, ModAccessControlInp.OWN_PERMISSION);
		getIRODSProtocol().irodsFunction(modAccessControlInp);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.CollectionAO#setAccessPermissionOwnAsAdmin(
	 * java.lang.String, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public void setAccessPermissionOwnAsAdmin(final String zone, final String absolutePath, final String userName,
			final boolean recursive) throws JargonException {

		if (zone == null) {
			throw new IllegalArgumentException("null zone");
		}

		if (absolutePath == null || absolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolutePath");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		// pi tests parameters
		log.info("setAccessPermissionOwnAsAdmin on absPath:{}", absolutePath);
		// overhead iRODS behavior, if you set perm with recursive when no
		// children, then won't take
		String effectiveAbsPath = resolveAbsolutePathViaObjStat(absolutePath);

		boolean collNeedsRecursive = adjustRecursiveOption(effectiveAbsPath, recursive);

		ModAccessControlInp modAccessControlInp = ModAccessControlInp.instanceForSetPermissionInAdminMode(
				collNeedsRecursive, zone, effectiveAbsPath, userName, ModAccessControlInp.OWN_PERMISSION);
		getIRODSProtocol().irodsFunction(modAccessControlInp);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.CollectionAO#removeAccessPermissionForUser(
	 * java.lang.String, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public void removeAccessPermissionForUser(final String zone, final String absolutePath, final String userName,
			final boolean recursive) throws JargonException {

		if (zone == null) {
			throw new IllegalArgumentException("null zone");
		}

		if (absolutePath == null || absolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolutePath");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		// pi tests parameters
		log.info("removeAccessPermission on absPath:{}", absolutePath);
		log.info("for user:{}", userName);
		// overhead iRODS behavior, if you set perm with recursive when no
		// children, then won't take
		String effectiveAbsPath = resolveAbsolutePathViaObjStat(absolutePath);

		boolean collNeedsRecursive = adjustRecursiveOption(effectiveAbsPath, recursive);

		ModAccessControlInp modAccessControlInp = ModAccessControlInp.instanceForSetPermission(collNeedsRecursive, zone,
				effectiveAbsPath, userName, ModAccessControlInp.NULL_PERMISSION);
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
	public void removeAccessPermissionForUserAsAdmin(final String zone, final String absolutePath,
			final String userName, final boolean recursive) throws JargonException {

		if (zone == null) {
			throw new IllegalArgumentException("null zone");
		}

		if (absolutePath == null || absolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolutePath");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		// pi tests parameters
		log.info("removeAccessPermissionAsAdmin on absPath:{}", absolutePath);
		log.info("for user:{}", userName);
		// overhead iRODS behavior, if you set perm with recursive when no
		// children, then won't take

		String effectiveAbsPath = resolveAbsolutePathViaObjStat(absolutePath);
		boolean collNeedsRecursive = adjustRecursiveOption(effectiveAbsPath, recursive);

		ModAccessControlInp modAccessControlInp = ModAccessControlInp.instanceForSetPermissionInAdminMode(
				collNeedsRecursive, zone, effectiveAbsPath, userName, ModAccessControlInp.NULL_PERMISSION);
		getIRODSProtocol().irodsFunction(modAccessControlInp);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.CollectionAO#
	 * isCollectionSetForPermissionInheritance(java.lang.String)
	 */
	@Override
	public boolean isCollectionSetForPermissionInheritance(final String absolutePath)
			throws FileNotFoundException, JargonException {

		if (absolutePath == null || absolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolutePathToCollection");
		}

		ObjStat objStat = getObjectStatForAbsolutePath(absolutePath);
		String absPath = resolveAbsolutePathGivenObjStat(objStat);

		IRODSGenQueryExecutor irodsGenQueryExecutor = getIRODSAccessObjectFactory()
				.getIRODSGenQueryExecutor(getIRODSAccount());

		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);
		CollectionAOHelper.buildInheritanceQueryForCollectionAbsolutePath(absPath, builder);

		IRODSQueryResultSet resultSet;

		try {

			IRODSGenQueryFromBuilder irodsQuery = builder.exportIRODSQueryFromBuilder(1);
			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResultInZone(irodsQuery, 0,
					MiscIRODSUtils.getZoneInPath(absPath));
		} catch (JargonQueryException | GenQueryBuilderException e) {
			log.error("error querying for inheritance flag", e);
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
	 * @see org.irods.jargon.core.pub.CollectionAO#getPermissionForCollection(java
	 * .lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public FilePermissionEnum getPermissionForCollection(final String irodsAbsolutePath, final String userName,
			final String zone) throws FileNotFoundException, JargonException {

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty irodsAbsolutePath");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		if (zone == null) {
			throw new IllegalArgumentException("null zone");
		}

		String myPath = MiscIRODSUtils.normalizeIrodsPath(irodsAbsolutePath);

		log.info("getPermissionForCollection for absPath:{}", myPath);
		log.info("userName:{}", userName);

		UserFilePermission permission = getPermissionForUserName(myPath, userName);
		if (permission == null) {
			log.info("no permission found, return 'none'");
			return FilePermissionEnum.NONE;
		} else {
			log.info("returning permission:{}", permission);
			return permission.getFilePermissionEnum();
		}

	}

	/**
	 * Method overheads an iRODS protocol issue where recursive flag when collection
	 * has no children causes no permissions to be set.
	 *
	 * @param absolutePath
	 * @param recursive
	 * @return
	 * @throws FileNotFoundException if the underlying file is not found by the
	 *                               absolute path
	 * @throws JargonException
	 */
	private boolean adjustRecursiveOption(final String absolutePath, final boolean recursive)
			throws FileNotFoundException, JargonException {

		if (absolutePath == null || absolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolutePath");
		}

		String myPath = MiscIRODSUtils.normalizeIrodsPath(absolutePath);

		IRODSFile collFile = getIRODSFileFactory().instanceIRODSFile(myPath);

		if (!collFile.exists()) {
			throw new JargonException("irodsFile does not exist for given path, cannot set permissions on it");
		}

		// soft links accounted for in collectionAndDataObjectListAndSearchAO
		boolean collNeedsRecursive = recursive;
		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(getIRODSAccount());
		int countFilesUnderParent = collectionAndDataObjectListAndSearchAO
				.countDataObjectsAndCollectionsUnderPath(myPath);

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
	 * @see org.irods.jargon.core.pub.CollectionAO#getPermissionForUserName(java.
	 * lang.String, java.lang.String)
	 */
	@Override
	@FederationEnabled
	public UserFilePermission getPermissionForUserName(final String irodsCollectionAbsolutePath, final String userName)
			throws FileNotFoundException, JargonException {

		if (irodsCollectionAbsolutePath == null || irodsCollectionAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty irodsCollectionAbsolutePath");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		log.info("getPermissionForUserName with irodsCollectionAbsolutePath: {}", irodsCollectionAbsolutePath);
		log.info("   userName:{}", userName);

		ObjStat objStat = getObjectStatForAbsolutePath(irodsCollectionAbsolutePath);
		String absPath = resolveAbsolutePathGivenObjStat(objStat);

		/*
		 * User may have permission via a direct user permission, or may have a group
		 * level permission, check both and get the highest value
		 */

		UserFilePermission userFilePermission = getPermissionViaGenQuery(userName, absPath);

		UserFilePermission groupFilePermission = getPermissionViaSpecQueryAsGroupMember(userName, objStat, absPath);

		return scoreAndReturnHighestPermission(userFilePermission, groupFilePermission);

	}

	private UserFilePermission getPermissionViaSpecQueryAsGroupMember(final String userName, final ObjStat objStat,
			final String absPath) throws JargonException {
		log.info("see if there is a permission based on group membership...");
		UserFilePermission permissionViaGroup = null;

		if (getJargonProperties().isUsingSpecQueryForDataObjPermissionsForUserInGroup()) {
			log.info(
					"is set to use specific query for group permissions via isUsingSpecQueryForDataObjPermissionsForUserInGroup()");
			permissionViaGroup = findPermissionForUserGrantedThroughUserGroup(userName,
					MiscIRODSUtils.getZoneInPath(absPath), objStat.determineAbsolutePathBasedOnCollTypeInObjectStat());
			return permissionViaGroup;
		} else {
			log.info("no group membership data found, not using specific query");
			return null;
		}
	}

	private UserFilePermission findPermissionForUserGrantedThroughUserGroup(final String userName, final String zone,
			final String absPath) throws JargonException {
		log.info("findPermissionForUserGrantedThroughUserGroup()");

		IRODSFile collFile = getIRODSFileFactory().instanceIRODSFile(absPath);

		SpecificQueryAO specificQueryAO = getIRODSAccessObjectFactory().getSpecificQueryAO(getIRODSAccount());

		if (!specificQueryAO.isSupportsSpecificQuery()) {
			log.info("no specific query support, so just return null");
			return null;
		}

		// I support spec query, give it a try

		List<String> arguments = new ArrayList<String>(2);
		arguments.add(collFile.getAbsolutePath());
		arguments.add(userName);

		SpecificQuery specificQuery = SpecificQuery.instanceArguments("listUserACLForCollectionViaGroup", arguments, 0,
				zone);

		SpecificQueryResultSet specificQueryResultSet;
		UserFilePermission userFilePermission = null;
		try {

			IRODSQueryResultRow row = null;

			try {
				specificQueryResultSet = specificQueryAO.executeSpecificQueryUsingAlias(specificQuery,
						getJargonProperties().getMaxFilesAndDirsQueryMax(), 0);
				row = specificQueryResultSet.getFirstResult();
				userFilePermission = buildUserFilePermissionFromResultRow(row);

			} catch (DataNotFoundException dnf) {
				log.info("no result, return null");
				return null;
			} catch (CatalogSQLException cse) {
				log.warn("no result due to specific query error, return null");
				return null;
			}

		} catch (JargonQueryException e) {
			log.error("jargon query exception looking up permission via specific query", e);
			throw new JargonException(e);
		}

		return userFilePermission;
	}

	/**
	 * @param row
	 * @return
	 * @throws JargonException
	 */
	private UserFilePermission buildUserFilePermissionFromResultRow(final IRODSQueryResultRow row)
			throws JargonException {

		UserFilePermission userFilePermission;
		userFilePermission = new UserFilePermission(row.getColumn(0), row.getColumn(1),
				FilePermissionEnum.valueOf(IRODSDataConversionUtil.getIntOrZeroFromIRODSValue(row.getColumn(2))),
				UserTypeEnum.findTypeByString(row.getColumn(3)), row.getColumn(4));
		return userFilePermission;
	}

	private UserFilePermission getPermissionViaGenQuery(final String userName, final String absPath)
			throws JargonException {
		UserFilePermission userFilePermission;
		String theUser = MiscIRODSUtils.getUserInUserName(userName);
		String theZone = MiscIRODSUtils.getZoneInUserName(userName);
		IRODSGenQueryExecutor irodsGenQueryExecutor = getIRODSAccessObjectFactory()
				.getIRODSGenQueryExecutor(getIRODSAccount());

		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);
		CollectionAOHelper.buildACLQueryForCollectionName(absPath, builder);
		builder.addConditionAsGenQueryField(RodsGenQueryEnum.COL_COLL_ACCESS_USER_NAME, QueryConditionOperators.EQUAL,
				theUser.trim());

		if (!theZone.isEmpty()) {
			builder.addConditionAsGenQueryField(RodsGenQueryEnum.COL_COLL_ACCESS_USER_ZONE,
					QueryConditionOperators.EQUAL, theZone.trim());
		}

		IRODSQueryResultSet resultSet;
		UserAO userAO = getIRODSAccessObjectFactory().getUserAO(getIRODSAccount());
		User user = null;

		try {
			IRODSGenQueryFromBuilder irodsQuery = builder
					.exportIRODSQueryFromBuilder(getJargonProperties().getMaxFilesAndDirsQueryMax());
			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResultInZone(irodsQuery, 0,
					MiscIRODSUtils.getZoneInPath(absPath));
			IRODSQueryResultRow row = resultSet.getFirstResult();

			/**
			 * Due to a gen query limitation getting user type with the user permission, a
			 * separate query must be done
			 */

			user = userAO.findByIdInZone(row.getColumn(2), getIRODSAccount().getZone());
			StringBuilder userAndZone = null;
			String displayUserName = null;

			userAndZone = new StringBuilder(row.getColumn(0));
			userAndZone.append('#');
			userAndZone.append(row.getColumn(1));

			if (row.getColumn(1).equals(MiscIRODSUtils.getZoneInPath(absPath))) {
				displayUserName = row.getColumn(0);
			} else {
				displayUserName = userAndZone.toString();
			}

			userFilePermission = new UserFilePermission(displayUserName, row.getColumn(2),
					FilePermissionEnum.valueOf(IRODSDataConversionUtil.getIntOrZeroFromIRODSValue(row.getColumn(3))),
					user.getUserType(), row.getColumn(1));
			log.info("loaded filePermission:{}", userFilePermission);
			return userFilePermission;

		} catch (JargonQueryException e) {
			log.error("query exception for  query", e);
			throw new JargonException("error in query loading user file permissions for collection", e);
		} catch (DataNotFoundException dnf) {
			log.info("no data found for user ACL");
			return null;
		} catch (GenQueryBuilderException e) {
			log.error("query exception for  query", e);
			throw new JargonException("error in query loading user file permissions for collection", e);
		}

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.CollectionAO#listPermissionsForCollection(java
	 * .lang.String)
	 */
	@Override
	@FederationEnabled
	public List<UserFilePermission> listPermissionsForCollection(final String irodsCollectionAbsolutePath)
			throws FileNotFoundException, JargonException {

		if (irodsCollectionAbsolutePath == null || irodsCollectionAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty collectionAbsolutePath");
		}

		log.info("listPermissionsForCollection: {}", irodsCollectionAbsolutePath);

		ObjStat objStat = getObjectStatForAbsolutePath(irodsCollectionAbsolutePath);
		String absPath = resolveAbsolutePathGivenObjStat(objStat);
		String zoneName = MiscIRODSUtils.getZoneInPath(absPath);

		List<UserFilePermission> userFilePermissions = new ArrayList<UserFilePermission>();

		/*
		 * See if the ShowCollAcls specific query is available and use this
		 */

		SpecificQueryAO specificQueryAO = this.getIRODSAccessObjectFactory().getSpecificQueryAO(getIRODSAccount());
		boolean queryViaSpecific = false;

		if (specificQueryAO.isSupportsSpecificQuery()) {
			log.info("specific query supported looking for ShowCollAcls");
			try {
				specificQueryAO.findSpecificQueryByAlias(SHOW_COLL_ACLS, zoneName);
				queryViaSpecific = true;
			} catch (DataNotFoundException e) {
				log.info("specific query not found, will use genquery approach");
			}

		}

		if (queryViaSpecific) {
			log.info("querying via specific query");

			SpecificQuery specificQuery = SpecificQuery.instanceWithOneArgument(SHOW_COLL_ACLS, absPath, 0, zoneName);

			try {
				SpecificQueryResultSet specificQueryResultSet = specificQueryAO.executeSpecificQueryUsingAlias(
						specificQuery, getJargonProperties().getMaxFilesAndDirsQueryMax());

				UserFilePermission userFilePermission = null;

				for (IRODSQueryResultRow row : specificQueryResultSet.getResults()) {

					userFilePermission = new UserFilePermission(row.getColumn(0), "",
							FilePermissionEnum.enumValueFromSpecificQueryTextPermission(row.getColumn(2)),
							UserTypeEnum.findTypeByString(row.getColumn(3)), row.getColumn(1));

					/*
					 * LL Patch code userFilePermission = new UserFilePermission(row.getColumn(0),
					 * "",
					 * FilePermissionEnum.enumValueFromSpecificQueryTextPermission(row.getColumn(2))
					 * , UserTypeEnum.RODS_USER, row.getColumn(1));
					 */

					log.debug("loaded filePermission:{}", userFilePermission);
					userFilePermissions.add(userFilePermission);
				}

			} catch (JargonQueryException e) {
				log.error("query exception for  query", e);
				throw new JargonException("error in query loading user file permissions for collection", e);
			}

		} else {
			log.info("querying via genQuery");
			IRODSGenQueryExecutor irodsGenQueryExecutor = getIRODSAccessObjectFactory()
					.getIRODSGenQueryExecutor(getIRODSAccount());
			IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);

			CollectionAOHelper.buildACLQueryForCollectionName(absPath, builder);

			IRODSQueryResultSet resultSet;
			UserAO userAO = getIRODSAccessObjectFactory().getUserAO(getIRODSAccount());

			/*
			 * There appears to be a gen query limitation on grabbing user type by a
			 * straight query, so, unfortunately, we need to do another query per user.
			 */

			User user = null;
			try {
				IRODSGenQueryFromBuilder irodsQuery = builder
						.exportIRODSQueryFromBuilder(getJargonProperties().getMaxFilesAndDirsQueryMax());

				resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResultInZone(irodsQuery, 0, zoneName);

				UserFilePermission userFilePermission = null;

				for (IRODSQueryResultRow row : resultSet.getResults()) {

					user = userAO.findByIdInZone(row.getColumn(2), zoneName);
					userFilePermission = new UserFilePermission(row.getColumn(0), row.getColumn(2),
							FilePermissionEnum
									.valueOf(IRODSDataConversionUtil.getIntOrZeroFromIRODSValue(row.getColumn(3))),
							user.getUserType(), row.getColumn(1));
					log.debug("loaded filePermission:{}", userFilePermission);
					userFilePermissions.add(userFilePermission);
				}

			} catch (JargonQueryException e) {
				log.error("query exception for  query", e);
				throw new JargonException("error in query loading user file permissions for collection", e);
			} catch (GenQueryBuilderException e) {
				log.error("query exception for  query", e);
				throw new JargonException("error in query loading user file permissions for collection", e);
			}

		}

		return userFilePermissions;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.FileCatalogObjectAOImpl#isUserHasAccess(java
	 * .lang.String, java.lang.String)
	 */
	@Override
	public boolean isUserHasAccess(final String irodsAbsolutePath, final String userName) throws JargonException {
		log.info("isUserHasAccess()");
		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty irodsAbsolutePath");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		String myPath = MiscIRODSUtils.normalizeIrodsPath(irodsAbsolutePath);

		log.info("irodsAbsolutePath:{}", myPath);
		log.info("userName:{}", userName);

		UserFilePermission derivedPermission = getPermissionForUserName(myPath, userName);
		boolean hasPermission = false;
		if (derivedPermission != null) {
			hasPermission = true;
		}

		log.info("has permision? {}", hasPermission);
		return hasPermission;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.CollectionAO#replicateCollectionAsynchronously
	 * (java.lang.String, java.lang.String, int)
	 */
	@Override
	public void replicateCollectionAsynchronously(final String irodsCollectionAbsolutePath, final String resourceName,
			final int delayInMinutes) throws JargonException {

		log.info("replicateCollectionAsynchronously()");

		if (irodsCollectionAbsolutePath == null || irodsCollectionAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty irodsCollectionAbsolutePath");
		}
		if (resourceName == null || resourceName.isEmpty()) {
			throw new IllegalArgumentException("null or empty resourceName");
		}

		if (delayInMinutes <= 0) {
			throw new IllegalArgumentException("delay in minutes must be > 0");
		}

		log.info("irodsCollectionAbsolutePath:{}", irodsCollectionAbsolutePath);
		log.info("resourceName:{}", resourceName);
		log.info("delayInMinutes:{}", delayInMinutes);

		if (!getIRODSServerProperties().isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods3.0")) {
			throw new JargonException("service not available on servers prior to rods3.0");
		}

		RuleProcessingAO ruleProcessingAO = getIRODSAccessObjectFactory().getRuleProcessingAO(getIRODSAccount());

		List<IRODSRuleParameter> irodsRuleParameters = new ArrayList<IRODSRuleParameter>();

		irodsRuleParameters.add(
				new IRODSRuleParameter("*SourceFile", MiscIRODSUtils.wrapStringInQuotes(irodsCollectionAbsolutePath)));

		irodsRuleParameters.add(new IRODSRuleParameter("*Resource", MiscIRODSUtils.wrapStringInQuotes(resourceName)));

		irodsRuleParameters
				.add(new IRODSRuleParameter("*DelayInfo", RuleUtils.buildDelayParamForMinutes(delayInMinutes)));

		RuleInvocationConfiguration ruleInvocationConfiguration = RuleInvocationConfiguration
				.instanceWithDefaultAutoSettings(getJargonProperties());
		ruleInvocationConfiguration.setRuleProcessingType(RuleProcessingType.EXTERNAL);

		IRODSRuleExecResult result = ruleProcessingAO.executeRuleFromResource("/rules/rulemsiCollReplAsync.r",
				irodsRuleParameters, ruleInvocationConfiguration);
		log.info("result of action:{}", result.getRuleExecOut().trim());

	}

}
