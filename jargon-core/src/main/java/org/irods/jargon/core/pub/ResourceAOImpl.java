package org.irods.jargon.core.pub;

import static org.irods.jargon.core.pub.aohelper.AOHelper.AND;
import static org.irods.jargon.core.pub.aohelper.AOHelper.COMMA;
import static org.irods.jargon.core.pub.aohelper.AOHelper.DEFAULT_REC_COUNT;
import static org.irods.jargon.core.pub.aohelper.AOHelper.EQUALS_AND_QUOTE;
import static org.irods.jargon.core.pub.aohelper.AOHelper.QUOTE;
import static org.irods.jargon.core.pub.aohelper.AOHelper.SPACE;
import static org.irods.jargon.core.pub.aohelper.AOHelper.WHERE;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.InvalidResourceException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.ResourceHierarchyException;
import org.irods.jargon.core.packinstr.GeneralAdminInpForResources;
import org.irods.jargon.core.packinstr.ModAvuMetadataInp;
import org.irods.jargon.core.protovalues.ErrorEnum;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.domain.Resource;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.AbstractIRODSQueryResultSet;
import org.irods.jargon.core.query.GenQueryBuilderException;
import org.irods.jargon.core.query.GenQueryOrderByField.OrderByType;
import org.irods.jargon.core.query.IRODSGenQuery;
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
import org.irods.jargon.core.utils.AccessObjectQueryProcessingUtils;
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

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	public static final String ERROR_IN_RESOURCE_QUERY = "error in resource query";
	private final transient ResourceAOHelper resourceAOHelper;

	protected ResourceAOImpl(final IRODSSession irodsSession,
			final IRODSAccount irodsAccount) throws JargonException {
		super(irodsSession, irodsAccount);
		getIRODSAccessObjectFactory().getZoneAO(getIRODSAccount());
		resourceAOHelper = new ResourceAOHelper(getIRODSAccount(),
				getIRODSAccessObjectFactory());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.ResourceAO#addResource(org.irods.jargon.core
	 * .pub.domain.Resource)
	 */
	@Override
	public void addResource(final Resource resource)
			throws DuplicateDataException, JargonException {

		log.info("addResource()");
		if (resource == null) {
			throw new IllegalArgumentException("null resource");
		}

		log.info("resource:{}", resource);

		if (!getIRODSServerProperties().isEirods()) {
			log.error("does not work pre iRODS 4.0");
			throw new UnsupportedOperationException(
					"add resource only works for 4.0+");
		}

		/*
		 * arg0 mkresc
		 * 
		 * generalAdminInp->arg1, "resource"
		 * 
		 * std::string resc_name( _generalAdminInp->arg2 );
		 * 
		 * std::string resc_type( _generalAdminInp->arg3 );
		 * 
		 * std::string resc_host_path(_generalAdminInp->arg4 );
		 * 
		 * for host path can be blank, otherwise in / separate the
		 * location:/vault/path pair
		 * 
		 * std::string resc_ctx(_generalAdminInp->arg5 );
		 * 
		 * 
		 * examples
		 * 
		 * "iadmin mkresc rrResc random",
		 */

		GeneralAdminInpForResources adminPI = GeneralAdminInpForResources
				.instanceForAddResource(resource);
		log.debug("executing admin PI");
		getIRODSProtocol().irodsFunction(adminPI);
		log.info("complete");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.ResourceAO#deleteResource(java.lang.String)
	 */
	@Override
	public void deleteResource(final String resourceName) throws Exception {
		log.info("deleteResource()");
		if (resourceName == null || resourceName.isEmpty()) {
			throw new IllegalArgumentException("null or empty resourceName");
		}

		GeneralAdminInpForResources adminPI = GeneralAdminInpForResources
				.instanceForRemoveResource(resourceName);
		log.debug("executing admin PI");
		try {
			getIRODSProtocol().irodsFunction(adminPI);
		} catch (DataNotFoundException e) {
			log.warn("data not found deleting resource, silently ignore", e);
		}
		log.info("complete");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.ResourceAO#addChildToResource(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public void addChildToResource(final String parent, final String child,
			final String optionalContext) throws JargonException {

		log.info("addChildToResource");

		if (!getIRODSServerProperties().isEirods()) {
			log.error("does not work pre iRODS 4.0");
			throw new UnsupportedOperationException("only works for iRODS 4.0+");
		}

		if (child == null || child.isEmpty()) {
			throw new IllegalArgumentException("null or empty child");
		}

		if (parent == null || parent.isEmpty()) {
			throw new IllegalArgumentException("null or empty parent");
		}

		if (optionalContext == null) {
			throw new IllegalArgumentException("null  optionalContext");
		}

		GeneralAdminInpForResources adminPI = GeneralAdminInpForResources
				.instanceForAddChildToResource(child, parent, optionalContext);
		log.debug("executing admin PI");
		try {
			getIRODSProtocol().irodsFunction(adminPI);
		} catch (ResourceHierarchyException e) {
			if (e.getUnderlyingIRODSExceptionCode() == ErrorEnum.CHILD_HAS_PARENT
					.getInt()) {
				log.warn("duplicate child ignored", e);
			} else {
				log.error("unknown resource exception", e);
				throw e;
			}
		}
		log.info("complete");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.ResourceAO#removeChildFromResource(java.lang
	 * .String, java.lang.String)
	 */
	@Override
	public void removeChildFromResource(final String parent, final String child)
			throws InvalidResourceException, JargonException {

		log.info("removeChildFromResource");

		if (!getIRODSServerProperties().isEirods()) {
			log.error("does not work pre iRODS 4.0");
			throw new UnsupportedOperationException("only works for iRODS 4.0+");
		}

		if (child == null || child.isEmpty()) {
			throw new IllegalArgumentException("null or empty child");
		}

		if (parent == null || parent.isEmpty()) {
			throw new IllegalArgumentException("null or empty parent");
		}

		GeneralAdminInpForResources adminPI = GeneralAdminInpForResources
				.instanceForRemoveChildFromResource(child, parent);
		log.debug("executing admin PI");
		getIRODSProtocol().irodsFunction(adminPI);
		log.info("complete");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.ResourceAO#findByName(java.lang.String)
	 */
	@Override
	public Resource findByName(final String resourceName)
			throws JargonException, DataNotFoundException {

		log.info("findByName()");

		if (resourceName == null || resourceName.isEmpty()) {
			throw new IllegalArgumentException("null or empty resourceName");
		}

		final IRODSGenQueryExecutorImpl irodsGenQueryExecutorImpl = new IRODSGenQueryExecutorImpl(
				getIRODSSession(), getIRODSAccount());

		final StringBuilder sb = new StringBuilder();

		sb.append(resourceAOHelper.buildResourceSelectsClassic());
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
		Resource resource = resourceAOHelper
				.buildResourceFromResultSetRowClassic(row);

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
				getIRODSSession(), getIRODSAccount());
		final StringBuilder sb = new StringBuilder();

		sb.append(resourceAOHelper.buildResourceSelectsClassic());
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
		Resource resource = resourceAOHelper
				.buildResourceFromResultSetRowClassic(row);

		return resource;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.ResourceAO#findAll()
	 */
	@Override
	public List<Resource> findAll() throws JargonException {

		log.info("findAll()");
		List<Resource> resources;
		if (getIRODSServerProperties().isEirods()) {
			resources = findAllComposable();
		} else {
			resources = findAllClassic();
		}

		log.info("resources:{}", resources);
		return resources;

	}

	private List<Resource> findAllClassic() throws JargonException {

		log.info("findAllClassic() - pre 4.0 server");
		final IRODSGenQueryExecutor irodsGenQueryExecutor = new IRODSGenQueryExecutorImpl(
				getIRODSSession(), getIRODSAccount());

		StringBuilder userQuery = new StringBuilder();
		userQuery.append(resourceAOHelper.buildResourceSelectsClassic());

		String queryString = userQuery.toString();
		if (log.isInfoEnabled()) {
			log.info("user query:{}", queryString);
		}

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString,
				DEFAULT_REC_COUNT);

		IRODSQueryResultSetInterface resultSet;
		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(
					irodsQuery, 0);
		} catch (JargonQueryException e) {
			log.error("query exception for:{}", queryString, e);
			throw new JargonException(ERROR_IN_RESOURCE_QUERY);
		}

		return resourceAOHelper
				.buildResourceListFromResultSetClassic(resultSet);
	}

	private List<Resource> findAllComposable() throws JargonException {

		log.info("findAllComposable() - post 4.0 server");

		final IRODSGenQueryExecutor irodsGenQueryExecutor = new IRODSGenQueryExecutorImpl(
				getIRODSSession(), getIRODSAccount());

		IRODSQueryResultSet resultSet = null;
		try {
			IRODSGenQueryBuilder builder = resourceAOHelper
					.buildResourceSelectsComposable();
			builder.addConditionAsGenQueryField(
					RodsGenQueryEnum.COL_R_RESC_NAME,
					QueryConditionOperators.NOT_EQUAL, "bundleResc");
			IRODSGenQueryFromBuilder irodsQuery = builder
					.exportIRODSQueryFromBuilder(getJargonProperties()
							.getMaxFilesAndDirsQueryMax());
			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(
					irodsQuery, 0);

		} catch (JargonQueryException e) {
			log.error("query exception for query", e);
			throw new JargonException("error in query", e);
		} catch (GenQueryBuilderException e) {
			log.error("query exception for query", e);
			throw new JargonException("error in query", e);
		}
		return resourceAOHelper
				.buildResourceListFromResultSetComposable(resultSet);
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
			throw new IllegalArgumentException("irods file is null");
		}

		StringBuilder query = new StringBuilder();
		query.append(resourceAOHelper.buildResourceSelectsClassic());
		query.append(" where ");

		if (irodsFile.isFile()) {
			query.append(RodsGenQueryEnum.COL_COLL_NAME.getName());
			query.append(EQUALS_AND_QUOTE);
			query.append(irodsFile.getParent());
			query.append("'");
			query.append(AND);
			query.append(RodsGenQueryEnum.COL_DATA_NAME.getName());
			query.append(EQUALS_AND_QUOTE);
			query.append(irodsFile.getName());
			query.append("'");

		} else {
			String msg = "looking for a resource for an IRODSFileImpl, but I the file is a collection:"
					+ irodsFile.getAbsolutePath();
			log.error(msg);
			throw new JargonException(msg);
		}

		IRODSGenQueryExecutorImpl irodsGenQueryExecutorImpl = new IRODSGenQueryExecutorImpl(
				getIRODSSession(), getIRODSAccount());

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
				.buildResourceListFromResultSetClassic(resultSet);

		if (resources.isEmpty()) {
			log.warn("no data found");
			throw new DataNotFoundException("no resources found for file:"
					+ irodsFile.getAbsolutePath());
		}

		return resources.get(0);

	}

	@Override
	public List<String> listResourceAndResourceGroupNames()
			throws JargonException {

		log.info("listResourceAndResourceGroupNames()");
		log.info("listResourceAndResourceGroupNames()..getting resource names");
		List<String> combined = listResourceNames();

		if (getIRODSServerProperties().isEirods()) {
			log.info("is consortium irods, don't look for resource groups");
			return combined;
		}

		log.info("appending resource group names..");
		ResourceGroupAO resourceGroupAO = getIRODSAccessObjectFactory()
				.getResourceGroupAO(getIRODSAccount());
		combined.addAll(resourceGroupAO.listResourceGroupNames());
		return combined;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.ResourceAO#listResourceNames()
	 */
	@Override
	public List<String> listResourceNames() throws JargonException {

		List<String> resourceNames = new ArrayList<String>();

		AbstractIRODSQueryResultSet resultSet = null;
		try {
			IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);
			builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_RESC_NAME)
					.addOrderByGenQueryField(RodsGenQueryEnum.COL_R_RESC_NAME,
							OrderByType.ASC);

			if (getIRODSServerProperties().isEirods()) {
				builder.addConditionAsGenQueryField(
						RodsGenQueryEnum.COL_R_RESC_PARENT,
						QueryConditionOperators.EQUAL, "");
			}

			IRODSGenQueryExecutor irodsGenQueryExecutor = getIRODSAccessObjectFactory()
					.getIRODSGenQueryExecutor(getIRODSAccount());

			resultSet = irodsGenQueryExecutor
					.executeIRODSQueryAndCloseResult(
							builder.exportIRODSQueryFromBuilder(getIRODSAccessObjectFactory()
									.getJargonProperties()
									.getMaxFilesAndDirsQueryMax()), 0);
		} catch (JargonQueryException e) {
			log.error("jargon query exception getting results", e);
			throw new JargonException(e);
		} catch (GenQueryBuilderException e) {
			log.error("jargon query exception getting results", e);
			throw new JargonException(e);
		}

		for (IRODSQueryResultRow row : resultSet.getResults()) {

			resourceNames.add(row.getColumn(0));

		}

		return resourceNames;

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
			throw new IllegalArgumentException("null or empty resourceName");
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
				getIRODSSession(), getIRODSAccount());

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
			throw new IllegalArgumentException("null or empty query");
		}

		final IRODSGenQueryExecutorImpl irodsGenQueryExecutorImpl = new IRODSGenQueryExecutorImpl(
				getIRODSSession(), getIRODSAccount());

		// TODO: ripe for factoring out as applied to other domain objects

		log.info("building a metadata query for: {}", avuQuery);

		StringBuilder query = new StringBuilder();
		query.append("SELECT ");
		query.append(RodsGenQueryEnum.COL_R_RESC_ID.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_R_RESC_NAME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_META_RESC_ATTR_VALUE.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_META_RESC_ATTR_UNITS.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_META_USER_ATTR_ID.getName());

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

		return buildMetaDataAndDomainDatalistFromResultSet(
				MetadataDomain.RESOURCE, resultSet);
	}

	private List<MetaDataAndDomainData> buildMetaDataAndDomainDatalistFromResultSet(
			final MetadataDomain metaDataDomain,
			final IRODSQueryResultSetInterface irodsQueryResultSet)
			throws JargonException {
		if (metaDataDomain == null) {
			throw new JargonException("null metaDataDomain");
		}

		if (irodsQueryResultSet == null) {
			throw new JargonException("null irodsQueryResultSet");
		}

		List<MetaDataAndDomainData> metaDataResults = new ArrayList<MetaDataAndDomainData>();
		for (IRODSQueryResultRow row : irodsQueryResultSet.getResults()) {
			metaDataResults
					.add(buildMetaDataAndDomainDataFromResultSetRow(
							metaDataDomain, row,
							irodsQueryResultSet.getTotalRecords()));
		}

		return metaDataResults;
	}

	private MetaDataAndDomainData buildMetaDataAndDomainDataFromResultSetRow(
			final MetaDataAndDomainData.MetadataDomain metadataDomain,
			final IRODSQueryResultRow row, final int totalRecordCount)
			throws JargonException {

		String domainId = row.getColumn(0);
		String domainUniqueName = row.getColumn(1);
		String attributeName = row.getColumn(2);
		String attributeValue = row.getColumn(3);
		String attributeUnits = row.getColumn(4);
		int attributeId = row.getColumnAsIntOrZero(5);

		MetaDataAndDomainData data = MetaDataAndDomainData.instance(
				metadataDomain, domainId, domainUniqueName, attributeId,
				attributeName, attributeValue, attributeUnits);
		data.setCount(row.getRecordCount());
		data.setLastResult(row.isLastResult());
		data.setTotalRecords(totalRecordCount);
		log.debug("metadataAndDomainData: {}", data);
		return data;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.ResourceAO#addAVUMetadata(java.lang.String,
	 * org.irods.jargon.core.pub.domain.AvuData)
	 */
	@Override
	public void addAVUMetadata(final String resourceName, final AvuData avuData)
			throws InvalidResourceException, DuplicateDataException,
			JargonException {

		if (resourceName == null || resourceName.isEmpty()) {
			throw new IllegalArgumentException("null or empty resource name");
		}

		if (avuData == null) {
			throw new IllegalArgumentException("null AVU data");
		}

		log.info("adding avu metadata to resource: {}", resourceName);
		log.info("avu: {}", avuData);

		final ModAvuMetadataInp modifyAvuMetadataInp = ModAvuMetadataInp
				.instanceForAddResourceMetadata(resourceName, avuData);

		log.debug("sending avu request");

		try {

			getIRODSProtocol().irodsFunction(modifyAvuMetadataInp);

		} catch (JargonException je) {

			if (je.getMessage().indexOf("-817000") > -1) {
				throw new DataNotFoundException(
						"Target resource was not found, could not add AVU");
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
	 * org.irods.jargon.core.pub.ResourceAO#deleteAVUMetadata(java.lang.String,
	 * org.irods.jargon.core.pub.domain.AvuData)
	 */
	@Override
	public void deleteAVUMetadata(final String resourceName,
			final AvuData avuData) throws InvalidResourceException,
			JargonException {

		if (resourceName == null || resourceName.isEmpty()) {
			throw new IllegalArgumentException("null or empty resource name");
		}

		if (avuData == null) {
			throw new IllegalArgumentException("null AVU data");
		}

		log.info("delete avu metadata from resource: {}", resourceName);
		log.info("avu: {}", avuData);

		final ModAvuMetadataInp modifyAvuMetadataInp = ModAvuMetadataInp
				.instanceForDeleteResourceMetadata(resourceName, avuData);

		log.debug("sending avu request");

		try {
			getIRODSProtocol().irodsFunction(modifyAvuMetadataInp);
		} catch (JargonException je) {

			if (je.getMessage().indexOf("-817000") > -1) {
				throw new DataNotFoundException(
						"Target resource was not found, could not remove AVU");
			}

			log.error("jargon exception removing AVU metadata", je);
			throw je;
		}

		log.debug("metadata removed");
	}

}
