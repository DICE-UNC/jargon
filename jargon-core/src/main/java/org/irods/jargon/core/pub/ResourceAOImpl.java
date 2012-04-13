/**
 * 
 */
package org.irods.jargon.core.pub;

import static org.irods.jargon.core.pub.aohelper.AOHelper.AND;
import static org.irods.jargon.core.pub.aohelper.AOHelper.COMMA;
import static org.irods.jargon.core.pub.aohelper.AOHelper.DEFAULT_REC_COUNT;
import static org.irods.jargon.core.pub.aohelper.AOHelper.EQUALS_AND_QUOTE;
import static org.irods.jargon.core.pub.aohelper.AOHelper.QUOTE;
import static org.irods.jargon.core.pub.aohelper.AOHelper.SPACE;
import static org.irods.jargon.core.pub.aohelper.AOHelper.WHERE;

import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.domain.Resource;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.IRODSGenQuery;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.core.query.MetaDataAndDomainData.MetadataDomain;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.core.utils.AccessObjectQueryProcessingUtils;
import org.irods.jargon.core.utils.IRODSDataConversionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Access to CRUD and query operations on IRODS Resource.
 * <p/>
 * AO objects are not shared between threads. Jargon services will confine
 * activities to one connection per thread.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public final class ResourceAOImpl extends IRODSGenericAO implements ResourceAO {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	public static final String ERROR_IN_RESOURCE_QUERY = "error in resource query";
	private final transient ResourceAOHelper resourceAOHelper;

	public ResourceAOImpl(final IRODSSession irodsSession,
			final IRODSAccount irodsAccount) throws JargonException {
		super(irodsSession, irodsAccount);
		this.getIRODSAccessObjectFactory().getZoneAO(getIRODSAccount());
		resourceAOHelper = new ResourceAOHelper(getIRODSAccount(),
				getIRODSAccessObjectFactory());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.ResourceAO#findByName(java.lang.String)
	 */
	@Override
	public Resource findByName(final String resourceName)
			throws JargonException, DataNotFoundException {
		final IRODSGenQueryExecutorImpl irodsGenQueryExecutorImpl = new IRODSGenQueryExecutorImpl(
				this.getIRODSSession(), this.getIRODSAccount());

		final StringBuilder sb = new StringBuilder();

		sb.append(resourceAOHelper.buildResourceSelects());
		sb.append(WHERE);
		sb.append(RodsGenQueryEnum.COL_R_RESC_NAME.getName());
		sb.append(EQUALS_AND_QUOTE);
		sb.append(resourceName.trim());
		sb.append("'");

		final String queryString = sb.toString();
		if (log.isInfoEnabled()) {
			log.info("query:" + queryString);
		}

		final IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString,
				DEFAULT_REC_COUNT);

		IRODSQueryResultSetInterface resultSet;
		try {
			resultSet = irodsGenQueryExecutorImpl
					.executeIRODSQueryAndCloseResult(irodsQuery, 0);
		} catch (JargonQueryException e) {
			log.error("query exception for user query:{}", queryString, e);
			throw new JargonException(ERROR_IN_RESOURCE_QUERY);
		}

		if (resultSet.getResults().size() == 0) {
			final StringBuilder messageBuilder = new StringBuilder();
			messageBuilder.append("resource not found for name:");
			messageBuilder.append(resourceName);
			final String message = messageBuilder.toString();
			log.warn(message);
			throw new DataNotFoundException(message);
		}

		if (resultSet.getResults().size() > 1) {
			StringBuilder messageBuilder = new StringBuilder();
			messageBuilder.append("more than one resource found for name:");
			messageBuilder.append(resourceName);
			String message = messageBuilder.toString();
			log.error(message);
			throw new JargonException(message);
		}

		// I know I have just one user

		IRODSQueryResultRow row = resultSet.getFirstResult();
		Resource resource = resourceAOHelper.buildResourceFromResultSetRow(row);

		return resource;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.ResourceAO#findById(java.lang.String)
	 */
	@Override
	public Resource findById(final String resourceId) throws JargonException,
			DataNotFoundException {
		final IRODSGenQueryExecutorImpl irodsGenQueryExecutorImpl = new IRODSGenQueryExecutorImpl(
				this.getIRODSSession(), this.getIRODSAccount());
		final StringBuilder sb = new StringBuilder();

		sb.append(resourceAOHelper.buildResourceSelects());
		sb.append(" where ");
		sb.append(RodsGenQueryEnum.COL_R_RESC_ID.getName());
		sb.append(EQUALS_AND_QUOTE);
		sb.append(resourceId.trim());
		sb.append("'");

		String queryString = sb.toString();
		if (log.isInfoEnabled()) {
			log.info("query:{}", queryString);
		}

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString,
				DEFAULT_REC_COUNT);

		IRODSQueryResultSetInterface resultSet;
		try {
			resultSet = irodsGenQueryExecutorImpl
					.executeIRODSQueryAndCloseResult(irodsQuery, 0);
		} catch (JargonQueryException e) {
			log.error("query exception for user query:" + queryString, e);
			throw new JargonException(ERROR_IN_RESOURCE_QUERY);
		}

		if (resultSet.getResults().size() == 0) {
			StringBuilder messageBuilder = new StringBuilder();
			messageBuilder.append("resource not found for id:");
			messageBuilder.append(resourceId);
			String message = messageBuilder.toString();
			log.warn(message);
			throw new DataNotFoundException(message);
		}

		if (resultSet.getResults().size() > 1) {
			StringBuilder messageBuilder = new StringBuilder();
			messageBuilder.append("more than one resource found for id:");
			messageBuilder.append(resourceId);
			String message = messageBuilder.toString();
			log.error(message);
			throw new JargonException(message);
		}

		// I know I have just one resource
		IRODSQueryResultRow row = resultSet.getFirstResult();
		Resource resource = resourceAOHelper.buildResourceFromResultSetRow(row);

		return resource;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.ResourceAO#findAll()
	 */
	@Override
	public List<Resource> findAll() throws JargonException {
		final IRODSGenQueryExecutorImpl irodsGenQueryExecutorImpl = new IRODSGenQueryExecutorImpl(
				this.getIRODSSession(), this.getIRODSAccount());
		StringBuilder userQuery = new StringBuilder();
		userQuery.append(resourceAOHelper.buildResourceSelects());

		String queryString = userQuery.toString();
		if (log.isInfoEnabled()) {
			log.info("user query:{}", queryString);
		}

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString,
				DEFAULT_REC_COUNT);

		IRODSQueryResultSetInterface resultSet;
		try {
			resultSet = irodsGenQueryExecutorImpl
					.executeIRODSQueryAndCloseResult(irodsQuery, 0);
		} catch (JargonQueryException e) {
			log.error("query exception for:{}", queryString, e);
			throw new JargonException(ERROR_IN_RESOURCE_QUERY);
		}

		return resourceAOHelper.buildResourceListFromResultSet(resultSet);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.ResourceAO#getFirstResourceForIRODSFile(org
	 * .irods.jargon.core.pub.io.IRODSFile)
	 */
	@Override
	public Resource getFirstResourceForIRODSFile(final IRODSFile irodsFile)
			throws JargonException, DataNotFoundException {
		if (irodsFile == null) {
			throw new JargonException("irods file is null");
		}

		StringBuilder query = new StringBuilder();
		query.append(resourceAOHelper.buildResourceSelects());
		query.append(" where ");

		if (irodsFile.isFile()) {
			query.append(RodsGenQueryEnum.COL_COLL_NAME.getName());
			query.append(EQUALS_AND_QUOTE);
			query.append(IRODSDataConversionUtil.escapeSingleQuotes(irodsFile
					.getParent()));
			query.append("'");
			query.append(AND);
			query.append(RodsGenQueryEnum.COL_DATA_NAME.getName());
			query.append(EQUALS_AND_QUOTE);
			query.append(IRODSDataConversionUtil.escapeSingleQuotes(irodsFile
					.getName()));
			query.append("'");

		} else {
			String msg = "looking for a resource for an IRODSFileImpl, but I the file is a collection:"
					+ irodsFile.getAbsolutePath();
			log.error(msg);
			throw new JargonException(msg);
		}

		IRODSGenQueryExecutorImpl irodsGenQueryExecutorImpl = new IRODSGenQueryExecutorImpl(
				this.getIRODSSession(), this.getIRODSAccount());

		String queryString = query.toString();
		if (log.isInfoEnabled()) {
			log.info("resource query:{}", toString());
		}

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString, 500);

		IRODSQueryResultSetInterface resultSet;
		try {
			resultSet = irodsGenQueryExecutorImpl
					.executeIRODSQueryAndCloseResult(irodsQuery, 0);
		} catch (JargonQueryException e) {
			log.error("query exception for:{}", queryString, e);
			throw new JargonException("error in query");
		}

		List<Resource> resources = resourceAOHelper
				.buildResourceListFromResultSet(resultSet);

		if (resources.isEmpty()) {
			log.warn("no data found");
			throw new DataNotFoundException("no resources found for file:"
					+ irodsFile.getAbsolutePath());
		}

		return resources.get(0);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.ResourceAO#listResourcesInZone(java.lang.String
	 * )
	 */
	@Override
	public List<Resource> listResourcesInZone(final String zoneName)
			throws JargonException {

		if (zoneName == null || zoneName.length() == 0) {
			throw new JargonException("zone name is null or blank");
		}

		IRODSGenQueryExecutorImpl irodsGenQueryExecutorImpl = new IRODSGenQueryExecutorImpl(
				this.getIRODSSession(), this.getIRODSAccount());
		StringBuilder query = new StringBuilder();

		query.append(resourceAOHelper.buildResourceSelects());
		query.append(" where ");
		query.append(RodsGenQueryEnum.COL_R_ZONE_NAME.getName());
		query.append(EQUALS_AND_QUOTE);
		query.append(zoneName);
		query.append("'");

		String queryString = query.toString();
		if (log.isInfoEnabled()) {
			log.info("resource query:" + toString());
		}

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString, 500);

		IRODSQueryResultSetInterface resultSet;
		try {
			resultSet = irodsGenQueryExecutorImpl
					.executeIRODSQueryAndCloseResult(irodsQuery, 0);
		} catch (JargonQueryException e) {
			log.error("query exception for:{}", queryString, e);
			throw new JargonException("error in query");
		}

		return resourceAOHelper.buildResourceListFromResultSet(resultSet);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.UserAO#listUserMetadata(java.lang.String)
	 */
	@Override
	public List<AvuData> listResourceMetadata(final String resourceName)
			throws JargonException {
		if (resourceName == null || resourceName.isEmpty()) {
			throw new JargonException("null or empty resourceName");
		}
		log.info("list resource metadata for {}", resourceName);

		final StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		sb.append(RodsGenQueryEnum.COL_META_RESC_ATTR_NAME.getName());
		sb.append(COMMA);
		sb.append(RodsGenQueryEnum.COL_META_RESC_ATTR_VALUE.getName());
		sb.append(COMMA);
		sb.append(RodsGenQueryEnum.COL_META_RESC_ATTR_UNITS.getName());
		sb.append(WHERE);
		sb.append(RodsGenQueryEnum.COL_R_RESC_NAME.getName());
		sb.append(EQUALS_AND_QUOTE);
		sb.append(resourceName);
		sb.append("'");
		log.debug("resource avu list query: {}", sb.toString());
		final IRODSGenQuery irodsQuery = IRODSGenQuery.instance(sb.toString(),
				DEFAULT_REC_COUNT);
		final IRODSGenQueryExecutorImpl irodsGenQueryExecutorImpl = new IRODSGenQueryExecutorImpl(
				this.getIRODSSession(), this.getIRODSAccount());

		IRODSQueryResultSetInterface resultSet;

		try {
			resultSet = irodsGenQueryExecutorImpl
					.executeIRODSQueryAndCloseResult(irodsQuery, 0);
		} catch (JargonQueryException e) {
			log.error("query exception for user query: " + sb.toString(), e);
			throw new JargonException(ERROR_IN_RESOURCE_QUERY);
		}

		return AccessObjectQueryProcessingUtils
				.buildAvuDataListFromResultSet(resultSet);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.ResourceAO#findWhere(java.lang.String)
	 */
	@Override
	public List<Resource> findWhere(final String whereStatement)
			throws JargonException {

		// FIXME: collapse other query methods onto this one method....

		if (whereStatement == null) {
			throw new JargonException("null where statement");
		}

		final IRODSGenQueryExecutorImpl irodsGenQueryExecutorImpl = new IRODSGenQueryExecutorImpl(
				this.getIRODSSession(), this.getIRODSAccount());

		final StringBuilder sb = new StringBuilder();
		sb.append(resourceAOHelper.buildResourceSelects());

		if (whereStatement.isEmpty()) {
			log.debug("no where statement given, so will do plain select");
		} else {
			sb.append(WHERE);
			sb.append(whereStatement);
		}

		String queryString = sb.toString();
		if (log.isInfoEnabled()) {
			log.info("query: " + queryString);
		}

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString,
				DEFAULT_REC_COUNT);

		IRODSQueryResultSetInterface resultSet;
		try {
			resultSet = irodsGenQueryExecutorImpl
					.executeIRODSQueryAndCloseResult(irodsQuery, 0);
		} catch (JargonQueryException e) {
			log.error("query exception for query:" + queryString, e);
			throw new JargonException(ERROR_IN_RESOURCE_QUERY);
		}
		return resourceAOHelper.buildResourceListFromResultSet(resultSet);
	}

	/**
	 * FIXME: implement, add to interface Given a set of metadata query
	 * parameters, return a list of Resources that match the metadata query
	 * 
	 * @param avuQueryElements
	 * @return
	 * @throws JargonQueryException
	 * @throws JargonException
	 */
	public List<Resource> findDomainByMetadataQuery(
			final List<AVUQueryElement> avuQueryElements)
			throws JargonQueryException, JargonException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.ResourceAO#findMetadataValuesByMetadataQuery
	 * (java.util.List)
	 */
	@Override
	public List<MetaDataAndDomainData> findMetadataValuesByMetadataQuery(
			final List<AVUQueryElement> avuQuery) throws JargonQueryException,
			JargonException {
		if (avuQuery == null || avuQuery.isEmpty()) {
			throw new JargonException("null or empty query");
		}

		final IRODSGenQueryExecutorImpl irodsGenQueryExecutorImpl = new IRODSGenQueryExecutorImpl(
				this.getIRODSSession(), this.getIRODSAccount());

		// TODO: ripe for factoring out as applied to other domain objects

		log.info("building a metadata query for: {}", avuQuery);

		StringBuilder query = new StringBuilder();
		query.append("SELECT ");
		query.append(RodsGenQueryEnum.COL_R_RESC_ID.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_R_RESC_NAME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_META_RESC_ATTR_NAME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_META_RESC_ATTR_VALUE.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_META_RESC_ATTR_UNITS.getName());

		query.append(WHERE);
		boolean previousElement = false;
		StringBuilder queryCondition = null;

		for (AVUQueryElement queryElement : avuQuery) {

			queryCondition = new StringBuilder();
			if (previousElement) {
				queryCondition.append(AND);
			}
			previousElement = true;
			query.append(buildConditionPart(queryElement));
		}

		String queryString = query.toString();
		log.debug("query string for AVU query: {}", queryString);

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString,
				DEFAULT_REC_COUNT);

		IRODSQueryResultSetInterface resultSet;
		try {
			resultSet = irodsGenQueryExecutorImpl
					.executeIRODSQueryAndCloseResult(irodsQuery, 0);

		} catch (JargonQueryException e) {
			log.error("query exception for query:" + queryString, e);
			throw new JargonException(ERROR_IN_RESOURCE_QUERY);
		}

		return AccessObjectQueryProcessingUtils
				.buildMetaDataAndDomainDatalistFromResultSet(
						MetadataDomain.RESOURCE, resultSet);
	}

	/**
	 * @param queryCondition
	 * @param queryElement
	 */
	private StringBuilder buildConditionPart(final AVUQueryElement queryElement) {
		StringBuilder queryCondition = new StringBuilder();
		if (queryElement.getAvuQueryPart() == AVUQueryElement.AVUQueryPart.ATTRIBUTE) {
			queryCondition.append(RodsGenQueryEnum.COL_META_RESC_ATTR_NAME
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
			queryCondition.append(RodsGenQueryEnum.COL_META_RESC_ATTR_VALUE
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
			queryCondition.append(RodsGenQueryEnum.COL_META_RESC_ATTR_UNITS
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

}
