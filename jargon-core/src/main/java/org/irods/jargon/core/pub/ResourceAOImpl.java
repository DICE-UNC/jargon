package org.irods.jargon.core.pub;

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
import org.irods.jargon.core.pub.domain.Zone;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.AbstractIRODSQueryResultSet;
import org.irods.jargon.core.query.GenQueryBuilderException;
import org.irods.jargon.core.query.GenQueryOrderByField.OrderByType;
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
import org.irods.jargon.core.utils.IRODSDataConversionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Access to CRUD and query operations on IRODS Resource.
 * <p>
 * AO objects are not shared between threads. Jargon services will confine
 * activities to one connection per thread.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public final class ResourceAOImpl extends IRODSGenericAO implements ResourceAO {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private final ZoneAO zoneAO;

	/**
	 * Used for simple caching, not thread-safe
	 */
	private Zone lastZone = null;

	public static final String ERROR_IN_RESOURCE_QUERY = "error in resource query";
	private final transient ResourceAOHelper resourceAOHelper;

	protected ResourceAOImpl(final IRODSSession irodsSession, final IRODSAccount irodsAccount) throws JargonException {
		super(irodsSession, irodsAccount);
		getIRODSAccessObjectFactory().getZoneAO(getIRODSAccount());
		resourceAOHelper = new ResourceAOHelper(getIRODSAccount(), getIRODSAccessObjectFactory());
		zoneAO = this.getIRODSAccessObjectFactory().getZoneAO(irodsAccount);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.ResourceAO#addResource(org.irods.jargon.core
	 * .pub.domain.Resource)
	 */
	@Override
	public void addResource(final Resource resource) throws DuplicateDataException, JargonException {

		log.info("addResource()");
		if (resource == null) {
			throw new IllegalArgumentException("null resource");
		}

		log.info("resource:{}", resource);

		if (!getIRODSServerProperties().isAtLeastIrods410()) {
			log.error("does not work pre iRODS 4.1");
			throw new UnsupportedOperationException("add resource only works for 4.1+");
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
		 * for host path can be blank, otherwise in / separate the location:/vault/path
		 * pair
		 * 
		 * std::string resc_ctx(_generalAdminInp->arg5 );
		 * 
		 * 
		 * examples
		 * 
		 * "iadmin mkresc rrResc random",
		 */

		GeneralAdminInpForResources adminPI = GeneralAdminInpForResources.instanceForAddResource(resource);
		log.debug("executing admin PI");
		getIRODSProtocol().irodsFunction(adminPI);
		getIRODSAccessObjectFactory().closeSession(getIRODSAccount());

		log.info("complete");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.ResourceAO#modifyResource(org.irods.jargon.core
	 * .pub.domain.Resource)
	 */
	@Override
	public void modifyResource(final Resource resource, String what) throws JargonException {
		log.info("modifyResource()");
		if (resource == null) {
			throw new IllegalArgumentException("null resource");
		}

		log.info("resource:{}", resource);

		if (!getIRODSServerProperties().isAtLeastIrods410()) {
			log.error("does not work pre iRODS 4.1");
			throw new UnsupportedOperationException("add resource only works for 4.1+");
		}

		/*
		 * arg0 modify
		 * 
		 * generalAdminInp->arg1, "resource"
		 * 
		 * std::string resc_name( _generalAdminInp->arg2 );
		 * 
		 * std::string resc_type( _generalAdminInp->arg3 );
		 * 
		 * std::string resc_host_path(_generalAdminInp->arg4 );
		 * 
		 * for host path can be blank, otherwise in / separate the location:/vault/path
		 * pair
		 * 
		 * std::string resc_ctx(_generalAdminInp->arg5 );
		 * 
		 * 
		 * examples
		 * 
		 * "iadmin mkresc rrResc random",
		 */

		GeneralAdminInpForResources adminPI = GeneralAdminInpForResources.instanceForModifyResource(resource, what);
		log.debug("executing admin PI");
		getIRODSProtocol().irodsFunction(adminPI);
		getIRODSAccessObjectFactory().closeSession(getIRODSAccount());

		log.info("complete");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.ResourceAO#deleteResource(java.lang.String)
	 */
	@Override
	public void deleteResource(final String resourceName) throws JargonException {
		log.info("deleteResource()");
		if (resourceName == null || resourceName.isEmpty()) {
			throw new IllegalArgumentException("null or empty resourceName");
		}

		GeneralAdminInpForResources adminPI = GeneralAdminInpForResources.instanceForRemoveResource(resourceName);
		log.debug("executing admin PI");
		try {
			getIRODSProtocol().irodsFunction(adminPI);
		} catch (DataNotFoundException e) {
			log.warn("data not found deleting resource, silently ignore", e);
		}
		getIRODSAccessObjectFactory().closeSession(getIRODSAccount());

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
	public void addChildToResource(final String parent, final String child, final String optionalContext)
			throws JargonException {

		log.info("addChildToResource");

		if (!getIRODSServerProperties().isAtLeastIrods410()) {
			log.error("does not work pre iRODS 4.1");
			throw new UnsupportedOperationException("only works for iRODS 4.1+");
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

		GeneralAdminInpForResources adminPI = GeneralAdminInpForResources.instanceForAddChildToResource(child, parent,
				optionalContext);
		log.debug("executing admin PI");
		try {
			getIRODSProtocol().irodsFunction(adminPI);
		} catch (ResourceHierarchyException e) {
			if (e.getUnderlyingIRODSExceptionCode() == ErrorEnum.CHILD_HAS_PARENT.getInt()) {
				log.warn("duplicate child ignored", e);
			} else {
				log.error("unknown resource exception", e);
				throw e;
			}
		}
		getIRODSAccessObjectFactory().closeSession(getIRODSAccount());

		log.info("complete");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.ResourceAO#removeChildFromResource(java.lang
	 * .String, java.lang.String)
	 */
	@Override
	public void removeChildFromResource(final String parent, final String child)
			throws InvalidResourceException, JargonException {

		log.info("removeChildFromResource");

		if (!getIRODSServerProperties().isAtLeastIrods410()) {
			log.error("does not work pre iRODS 4.1");
			throw new UnsupportedOperationException("only works for iRODS 4.1+");
		}

		if (child == null || child.isEmpty()) {
			throw new IllegalArgumentException("null or empty child");
		}

		if (parent == null || parent.isEmpty()) {
			throw new IllegalArgumentException("null or empty parent");
		}

		GeneralAdminInpForResources adminPI = GeneralAdminInpForResources.instanceForRemoveChildFromResource(child,
				parent);
		log.debug("executing admin PI");
		getIRODSProtocol().irodsFunction(adminPI);
		getIRODSAccessObjectFactory().closeSession(getIRODSAccount());

		log.info("complete");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.ResourceAO#findByName(java.lang.String)
	 */
	@Override
	public Resource findByName(final String resourceName) throws JargonException, DataNotFoundException {

		log.info("findByName()");

		if (resourceName == null || resourceName.isEmpty()) {
			throw new IllegalArgumentException("null or empty resourceName");
		}

		IRODSGenQueryBuilder builder;
		try {
			if (this.getIRODSServerProperties().isSupportsComposableResoures()) {
				builder = resourceAOHelper.buildResourceSelectsComposable();
			} else {
				builder = resourceAOHelper.buildResourceSelectsClassic();
			}

			builder.addConditionAsGenQueryField(RodsGenQueryEnum.COL_R_RESC_NAME, QueryConditionOperators.EQUAL,
					resourceName.trim());
		} catch (GenQueryBuilderException e) {
			log.error("gen query builder exception", e);
			throw new JargonException("error querying for resources", e);
		}

		IRODSQueryResultSet resultSet;
		try {
			IRODSGenQueryExecutorImpl irodsGenQueryExecutor = new IRODSGenQueryExecutorImpl(getIRODSSession(),
					getIRODSAccount());
			IRODSGenQueryFromBuilder irodsQuery = builder
					.exportIRODSQueryFromBuilder(getJargonProperties().getMaxFilesAndDirsQueryMax());
			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(irodsQuery, 0);

		} catch (JargonQueryException e) {
			log.error("query exception for query", e);
			throw new JargonException("error in query for data object", e);
		} catch (GenQueryBuilderException e) {
			log.error("query exception for query", e);
			throw new JargonException("error in query for data object", e);
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
		Resource resource;
		if (this.getIRODSServerProperties().isSupportsComposableResoures()) {
			resource = buildResourceFromResultSetRowComposable(row);
		} else {
			resource = buildResourceFromResultSetRowClassic(row);
		}

		if (!resource.getParentId().isEmpty()) {
			log.info("have a parent resc id, find the parent resc");
			Resource parent = this.findById(resource.getParentId());
			resource.setParentResource(parent);
			resource.setParentName(parent.getName());
		}

		return resource;
	}

	@Override
	public Resource findById(final String resourceId) throws JargonException, DataNotFoundException {

		log.info("findById()");

		if (resourceId == null || resourceId.isEmpty()) {
			throw new IllegalArgumentException("null or empty resourceId");
		}

		IRODSGenQueryBuilder builder;
		try {
			if (this.getIRODSServerProperties().isSupportsComposableResoures()) {
				builder = resourceAOHelper.buildResourceSelectsComposable();
			} else {
				builder = resourceAOHelper.buildResourceSelectsClassic();
			}

			builder.addConditionAsGenQueryField(RodsGenQueryEnum.COL_R_RESC_ID, QueryConditionOperators.EQUAL,
					resourceId.trim());
		} catch (GenQueryBuilderException e) {
			log.error("gen query builder exception", e);
			throw new JargonException("error querying for resources", e);
		}

		IRODSQueryResultSet resultSet;
		try {
			IRODSGenQueryExecutorImpl irodsGenQueryExecutor = new IRODSGenQueryExecutorImpl(getIRODSSession(),
					getIRODSAccount());
			IRODSGenQueryFromBuilder irodsQuery = builder
					.exportIRODSQueryFromBuilder(getJargonProperties().getMaxFilesAndDirsQueryMax());
			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(irodsQuery, 0);

		} catch (JargonQueryException e) {
			log.error("query exception for query", e);
			throw new JargonException("error in query for data object", e);
		} catch (GenQueryBuilderException e) {
			log.error("query exception for query", e);
			throw new JargonException("error in query for data object", e);
		}

		if (resultSet.getResults().size() == 0) {
			final StringBuilder messageBuilder = new StringBuilder();
			messageBuilder.append("resource not found for id:");
			messageBuilder.append(resourceId);
			final String message = messageBuilder.toString();
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
		Resource resource;
		if (this.getIRODSServerProperties().isSupportsComposableResoures()) {
			resource = buildResourceFromResultSetRowComposable(row);
		} else {
			resource = buildResourceFromResultSetRowClassic(row);

		}

		if (!resource.getParentId().isEmpty()) {
			log.info("have a parent resc id, find the parent resc");
			Resource parent = this.findById(resource.getParentId());
			resource.setParentResource(parent);
			resource.setParentName(parent.getName());

		}

		return resource;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.ResourceAO#findAll()
	 */
	@Override
	public List<Resource> findAll() throws JargonException {

		log.info("findAllComposable() - post 4.0 server");

		final IRODSGenQueryExecutor irodsGenQueryExecutor = new IRODSGenQueryExecutorImpl(getIRODSSession(),
				getIRODSAccount());

		IRODSGenQueryBuilder builder;

		IRODSQueryResultSet resultSet = null;
		try {
			if (this.getIRODSServerProperties().isSupportsComposableResoures()) {
				builder = resourceAOHelper.buildResourceSelectsComposable();
			} else {
				builder = resourceAOHelper.buildResourceSelectsClassic();
			}
			builder.addConditionAsGenQueryField(RodsGenQueryEnum.COL_R_RESC_NAME, QueryConditionOperators.NOT_EQUAL,
					"bundleResc");
			IRODSGenQueryFromBuilder irodsQuery = builder
					.exportIRODSQueryFromBuilder(getJargonProperties().getMaxFilesAndDirsQueryMax());
			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(irodsQuery, 0);

		} catch (JargonQueryException e) {
			log.error("query exception for query", e);
			throw new JargonException("error in query", e);
		} catch (GenQueryBuilderException e) {
			log.error("query exception for query", e);
			throw new JargonException("error in query", e);
		}

		if (this.getIRODSServerProperties().isSupportsComposableResoures()) {
			return buildResourceListFromResultSetComposable(resultSet);
		} else {
			return buildResourceListFromResultSetClassic(resultSet);
		}
	}

	/**
	 * Given a result of a query as generated by
	 * {@code buildResourceSelectsComposable()}, create {@code Resource} objects
	 * that represent the results
	 *
	 * @param resultSet
	 *            {@link IRODSQueryResultSet}
	 * @return {@code List} of {@link Resource}
	 * @throws JargonException
	 *             for iRODS error
	 */
	private List<Resource> buildResourceListFromResultSetComposable(final IRODSQueryResultSet resultSet)
			throws JargonException {
		List<Resource> resources = new ArrayList<Resource>();

		Resource rowResc;
		for (IRODSQueryResultRow row : resultSet.getResults()) {
			rowResc = buildResourceFromResultSetRowComposable(row);
			if (!rowResc.getParentId().isEmpty()) {
				log.info("have a parent resc id, find the parent resc");
				Resource parent = this.findById(rowResc.getParentId());
				rowResc.setParentResource(parent);
				rowResc.setParentName(parent.getName());

			}
			resources.add(rowResc);
		}

		return resources;
	}

	/**
	 * From a result set for a resource query, build the {@code Resource} domain
	 * objects.
	 *
	 * @param resultSet
	 *            {@link IRODSQueryResultSetInterface} with a gen query result
	 * @return {@code List} of {@link Resource}, which will be empty if no results
	 * @throws JargonException
	 *             for iRODS error
	 */
	private List<Resource> buildResourceListFromResultSetClassic(final IRODSQueryResultSetInterface resultSet)
			throws JargonException {

		List<Resource> resources = new ArrayList<Resource>();

		for (IRODSQueryResultRow row : resultSet.getResults()) {
			resources.add(buildResourceFromResultSetRowClassic(row));
		}

		return resources;
	}

	/**
	 * From a query result row, build a {@code Resource} domain object
	 *
	 * @param row
	 *            {@link IRODSQueryResultRow} with the result of a gen query. Each
	 *            row represents resource data
	 * @return {@link Resource} domain object
	 * @throws JargonException
	 *             for iRODS error
	 */
	private Resource buildResourceFromResultSetRowClassic(final IRODSQueryResultRow row) throws JargonException {
		Resource resource = new Resource();
		resource.setId(row.getColumn(0));
		resource.setName(row.getColumn(1));
		String zoneName = row.getColumn(2);
		if (lastZone != null && zoneName.equals(lastZone.getZoneName())) {
			// hit on last zone, use cached
		} else {
			try {
				lastZone = zoneAO.getZoneByName(zoneName);
			} catch (DataNotFoundException e) {
				String message = "no zone found for zone in resource=" + zoneName;
				log.error(message);
				throw new JargonException("zone not found for resource, data integrity error", e);
			}
		}

		resource.setZone(lastZone);
		resource.setContextString(row.getColumn(3));
		resource.setResourceClass(row.getColumn(4));
		resource.setLocation(row.getColumn(5));
		resource.setVaultPath(row.getColumn(6));

		try {
			resource.setFreeSpace(Long.parseLong(row.getColumn(7)));
		} catch (NumberFormatException nfe) {
			resource.setFreeSpace(0);
			log.warn("unable to format resourceFreeSpace for value:" + row.getColumn(7) + " setting to 0");
		}

		resource.setFreeSpaceTime(IRODSDataConversionUtil.getDateFromIRODSValue(row.getColumn(8)));
		resource.setInfo(row.getColumn(9));
		resource.setComment(row.getColumn(10));
		resource.setCreateTime(IRODSDataConversionUtil.getDateFromIRODSValue(row.getColumn(11)));
		resource.setModifyTime(IRODSDataConversionUtil.getDateFromIRODSValue(row.getColumn(12)));
		resource.setStatus(row.getColumn(13));

		if (log.isInfoEnabled()) {
			log.info("resource built \n");
			log.info(resource.toString());
		}

		return resource;
	}

	private Resource buildResourceFromResultSetRowComposable(final IRODSQueryResultRow row) throws JargonException {
		Resource resource = new Resource();
		resource.setId(row.getColumn(0));
		resource.setName(row.getColumn(1));
		String zoneName = row.getColumn(2);
		if (lastZone != null && zoneName.equals(lastZone.getZoneName())) {
			// hit on last zone, use cached
		} else {
			try {
				lastZone = zoneAO.getZoneByName(zoneName);
			} catch (DataNotFoundException e) {
				String message = "no zone found for zone in resource=" + zoneName;
				log.error(message);
				throw new JargonException("zone not found for resource, data integrity error", e);
			}
		}

		resource.setZone(lastZone);
		resource.setContextString(row.getColumn(3));
		resource.setResourceClass(row.getColumn(4));
		resource.setLocation(row.getColumn(5));
		resource.setVaultPath(row.getColumn(6));

		try {
			resource.setFreeSpace(Long.parseLong(row.getColumn(7)));
		} catch (NumberFormatException nfe) {
			resource.setFreeSpace(0);
			log.warn("unable to format resourceFreeSpace for value:" + row.getColumn(7) + " setting to 0");
		}

		resource.setFreeSpaceTime(IRODSDataConversionUtil.getDateFromIRODSValue(row.getColumn(8)));
		resource.setInfo(row.getColumn(9));
		resource.setComment(row.getColumn(10));
		resource.setCreateTime(IRODSDataConversionUtil.getDateFromIRODSValue(row.getColumn(11)));
		resource.setModifyTime(IRODSDataConversionUtil.getDateFromIRODSValue(row.getColumn(12)));
		resource.setStatus(row.getColumn(13));
		resource.setParentId(row.getColumn(14));
		// resource.setImmediateChildren(formatImmediateChildren(row.getColumn(15)));
		// children
		resource.setContextString(row.getColumn(15));

		return resource;
	}

	@Override
	public List<Resource> listResourcesForIrodsFile(final IRODSFile irodsFile)
			throws JargonException, DataNotFoundException {

		log.info("listResourcesForIrodsFile()");

		if (irodsFile == null) {
			throw new IllegalArgumentException("irods file is null");
		}

		log.info("irodsFile:{}", irodsFile);

		if (irodsFile.isDirectory()) {
			String msg = "looking for a resource for an IRODSFile, but I the file is a collection:"
					+ irodsFile.getAbsolutePath();
			log.error(msg);
			throw new JargonException(msg);
		}

		final IRODSGenQueryExecutor irodsGenQueryExecutor = new IRODSGenQueryExecutorImpl(getIRODSSession(),
				getIRODSAccount());

		IRODSGenQueryBuilder builder;
		IRODSQueryResultSet resultSet = null;

		try {
			if (this.getIRODSServerProperties().isSupportsComposableResoures()) {
				builder = resourceAOHelper.buildResourceSelectsComposable();
			} else {
				builder = resourceAOHelper.buildResourceSelectsClassic();
			}

			builder.addConditionAsGenQueryField(RodsGenQueryEnum.COL_COLL_NAME, QueryConditionOperators.EQUAL,
					irodsFile.getParent());
			builder.addConditionAsGenQueryField(RodsGenQueryEnum.COL_DATA_NAME, QueryConditionOperators.EQUAL,
					irodsFile.getName());
			IRODSGenQueryFromBuilder irodsQuery = builder
					.exportIRODSQueryFromBuilder(getJargonProperties().getMaxFilesAndDirsQueryMax());
			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(irodsQuery, 0);

		} catch (JargonQueryException e) {
			log.error("query exception for query", e);
			throw new JargonException("error in query", e);
		} catch (GenQueryBuilderException e) {
			log.error("query exception for query", e);
			throw new JargonException("error in query", e);
		}

		if (this.getIRODSServerProperties().isSupportsComposableResoures()) {
			return buildResourceListFromResultSetComposable(resultSet);
		} else {
			return buildResourceListFromResultSetClassic(resultSet);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.ResourceAO#getFirstResourceForIRODSFile(org
	 * .irods.jargon.core.pub.io.IRODSFile)
	 */
	@Override
	public Resource getFirstResourceForIRODSFile(final IRODSFile irodsFile)
			throws JargonException, DataNotFoundException {

		log.info("getFirstResourceForIRODSFile()");

		if (irodsFile == null) {
			throw new IllegalArgumentException("irods file is null");
		}

		List<Resource> resources = listResourcesForIrodsFile(irodsFile);

		if (resources.isEmpty()) {
			throw new DataNotFoundException("No resources found");
		}

		return resources.get(0);

	}

	@Override
	public List<String> listResourceAndResourceGroupNames() throws JargonException {

		log.info("listResourceAndResourceGroupNames()");
		log.info("listResourceAndResourceGroupNames()..getting resource names");
		List<String> combined = listResourceNames();

		if (getIRODSServerProperties().isAtLeastIrods410()) {
			log.info("is consortium irods, don't look for resource groups");
			return combined;
		}

		log.info("appending resource group names..");
		ResourceGroupAO resourceGroupAO = getIRODSAccessObjectFactory().getResourceGroupAO(getIRODSAccount());
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
					.addOrderByGenQueryField(RodsGenQueryEnum.COL_R_RESC_NAME, OrderByType.ASC);

			if (getIRODSServerProperties().isAtLeastIrods410()) {
				builder.addConditionAsGenQueryField(RodsGenQueryEnum.COL_R_RESC_PARENT, QueryConditionOperators.EQUAL,
						"");
			}

			IRODSGenQueryExecutor irodsGenQueryExecutor = getIRODSAccessObjectFactory()
					.getIRODSGenQueryExecutor(getIRODSAccount());

			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(builder.exportIRODSQueryFromBuilder(
					getIRODSAccessObjectFactory().getJargonProperties().getMaxFilesAndDirsQueryMax()), 0);
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
	public List<AvuData> listResourceMetadata(final String resourceName) throws JargonException {
		if (resourceName == null || resourceName.isEmpty()) {
			throw new IllegalArgumentException("null or empty resourceName");
		}
		log.info("list resource metadata for {}", resourceName);

		IRODSQueryResultSet resultSet = null;
		try {
			IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);
			builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_META_RESC_ATTR_NAME)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_META_RESC_ATTR_VALUE)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_META_RESC_ATTR_UNITS).addConditionAsGenQueryField(
							RodsGenQueryEnum.COL_R_RESC_NAME, QueryConditionOperators.EQUAL, resourceName);

			IRODSGenQueryExecutor irodsGenQueryExecutor = getIRODSAccessObjectFactory()
					.getIRODSGenQueryExecutor(getIRODSAccount());

			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(builder.exportIRODSQueryFromBuilder(
					getIRODSAccessObjectFactory().getJargonProperties().getMaxFilesAndDirsQueryMax()), 0);
		} catch (JargonQueryException e) {
			log.error("jargon query exception getting results", e);
			throw new JargonException(e);
		} catch (GenQueryBuilderException e) {
			log.error("jargon query exception getting results", e);
			throw new JargonException(e);
		}

		return AccessObjectQueryProcessingUtils.buildAvuDataListFromResultSet(resultSet);
	}

	/**
	 * 
	 * find the resources that match the given query
	 *
	 * @param avuQueryElements
	 *            {@code List} of {@link AVUQueryElement} with the query
	 * @return List of {@link Resource}
	 * @throws JargonQueryException
	 *             for query error
	 * @throws JargonException
	 *             for iRODS error
	 */
	public List<Resource> findDomainByMetadataQuery(final List<AVUQueryElement> avuQueryElements)
			throws JargonQueryException, JargonException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.ResourceAO#findMetadataValuesByMetadataQuery
	 * (java.util.List)
	 */
	@Override
	public List<MetaDataAndDomainData> findMetadataValuesByMetadataQuery(final List<AVUQueryElement> avuQuery)
			throws JargonQueryException, JargonException {

		log.info("findMetadataValuesByMetadataQuery()");
		if (avuQuery == null || avuQuery.isEmpty()) {
			throw new IllegalArgumentException("null or empty query");
		}

		log.info("building a metadata query for: {}", avuQuery);

		IRODSQueryResultSet resultSet = null;
		try {
			IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);
			builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_RESC_ID)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_RESC_NAME)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_META_RESC_ATTR_NAME)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_META_RESC_ATTR_VALUE)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_META_RESC_ATTR_UNITS)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_META_RESC_ATTR_ID);

			for (AVUQueryElement queryElement : avuQuery) {
				buildConditionPart(queryElement, builder);
			}

			IRODSGenQueryExecutor irodsGenQueryExecutor = getIRODSAccessObjectFactory()
					.getIRODSGenQueryExecutor(getIRODSAccount());

			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(builder.exportIRODSQueryFromBuilder(
					getIRODSAccessObjectFactory().getJargonProperties().getMaxFilesAndDirsQueryMax()), 0);
		} catch (JargonQueryException e) {
			log.error("jargon query exception getting results", e);
			throw new JargonException(e);
		} catch (GenQueryBuilderException e) {
			log.error("jargon query exception getting results", e);
			throw new JargonException(e);
		}

		return buildMetaDataAndDomainDatalistFromResultSet(MetadataDomain.RESOURCE, resultSet);
	}

	private List<MetaDataAndDomainData> buildMetaDataAndDomainDatalistFromResultSet(final MetadataDomain metaDataDomain,
			final IRODSQueryResultSetInterface irodsQueryResultSet) throws JargonException {
		if (metaDataDomain == null) {
			throw new JargonException("null metaDataDomain");
		}

		if (irodsQueryResultSet == null) {
			throw new JargonException("null irodsQueryResultSet");
		}

		List<MetaDataAndDomainData> metaDataResults = new ArrayList<MetaDataAndDomainData>();
		for (IRODSQueryResultRow row : irodsQueryResultSet.getResults()) {
			metaDataResults.add(buildMetaDataAndDomainDataFromResultSetRow(metaDataDomain, row,
					irodsQueryResultSet.getTotalRecords()));
		}

		return metaDataResults;
	}

	private MetaDataAndDomainData buildMetaDataAndDomainDataFromResultSetRow(
			final MetaDataAndDomainData.MetadataDomain metadataDomain, final IRODSQueryResultRow row,
			final int totalRecordCount) throws JargonException {

		String domainId = row.getColumn(0);
		String domainUniqueName = row.getColumn(1);
		String attributeName = row.getColumn(2);
		String attributeValue = row.getColumn(3);
		String attributeUnits = row.getColumn(4);
		int attributeId = row.getColumnAsIntOrZero(5);

		MetaDataAndDomainData data = MetaDataAndDomainData.instance(metadataDomain, domainId, domainUniqueName,
				attributeId, attributeName, attributeValue, attributeUnits);
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
	private void buildConditionPart(final AVUQueryElement queryElement, final IRODSGenQueryBuilder builder) {
		if (queryElement.getAvuQueryPart() == AVUQueryElement.AVUQueryPart.ATTRIBUTE) {
			builder.addConditionAsGenQueryField(RodsGenQueryEnum.COL_META_RESC_ATTR_NAME,
					QueryConditionOperators.getOperatorFromStringValue(queryElement.getOperator().getOperatorValue()),
					queryElement.getValue());
		} else if (queryElement.getAvuQueryPart() == AVUQueryElement.AVUQueryPart.VALUE) {

			builder.addConditionAsGenQueryField(RodsGenQueryEnum.COL_META_RESC_ATTR_VALUE,
					QueryConditionOperators.getOperatorFromStringValue(queryElement.getOperator().getOperatorValue()),
					queryElement.getValue());

		} else if (queryElement.getAvuQueryPart() == AVUQueryElement.AVUQueryPart.UNITS) {

			builder.addConditionAsGenQueryField(RodsGenQueryEnum.COL_META_RESC_ATTR_UNITS,
					QueryConditionOperators.getOperatorFromStringValue(queryElement.getOperator().getOperatorValue()),
					queryElement.getValue());

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.ResourceAO#addAVUMetadata(java.lang.String,
	 * org.irods.jargon.core.pub.domain.AvuData)
	 */
	@Override
	public void addAVUMetadata(final String resourceName, final AvuData avuData)
			throws InvalidResourceException, DuplicateDataException, JargonException {

		if (resourceName == null || resourceName.isEmpty()) {
			throw new IllegalArgumentException("null or empty resource name");
		}

		if (avuData == null) {
			throw new IllegalArgumentException("null AVU data");
		}

		log.info("adding avu metadata to resource: {}", resourceName);
		log.info("avu: {}", avuData);

		final ModAvuMetadataInp modifyAvuMetadataInp = ModAvuMetadataInp.instanceForAddResourceMetadata(resourceName,
				avuData);

		log.debug("sending avu request");

		try {

			getIRODSProtocol().irodsFunction(modifyAvuMetadataInp);

		} catch (JargonException je) {

			if (je.getMessage().indexOf("-817000") > -1) {
				throw new DataNotFoundException("Target resource was not found, could not add AVU");
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
	 * @see org.irods.jargon.core.pub.ResourceAO#setAVUMetadata(java.lang.String,
	 * org.irods.jargon.core.pub.domain.AvuData)
	 */
	@Override
	public void setAVUMetadata(final String resourceName, final AvuData avuData)
			throws InvalidResourceException, JargonException {

		if (resourceName == null || resourceName.isEmpty()) {
			throw new IllegalArgumentException("null or empty resource name");
		}

		if (avuData == null) {
			throw new IllegalArgumentException("null AVU data");
		}

		log.info("setting avu metadata to resource: {}", resourceName);
		log.info("avu: {}", avuData);

		final ModAvuMetadataInp modifyAvuMetadataInp = ModAvuMetadataInp.instanceForSetResourceMetadata(resourceName,
				avuData);

		log.debug("sending avu request");

		try {

			getIRODSProtocol().irodsFunction(modifyAvuMetadataInp);

		} catch (JargonException je) {

			if (je.getMessage().indexOf("-817000") > -1) {
				throw new DataNotFoundException("Target resource was not found, could not add AVU");
			}

			log.error("jargon exception setting AVU metadata", je);
			throw je;
		}

		log.debug("metadata set");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.ResourceAO#deleteAVUMetadata(java.lang.String,
	 * org.irods.jargon.core.pub.domain.AvuData)
	 */
	@Override
	public void deleteAVUMetadata(final String resourceName, final AvuData avuData)
			throws InvalidResourceException, JargonException {

		if (resourceName == null || resourceName.isEmpty()) {
			throw new IllegalArgumentException("null or empty resource name");
		}

		if (avuData == null) {
			throw new IllegalArgumentException("null AVU data");
		}

		log.info("delete avu metadata from resource: {}", resourceName);
		log.info("avu: {}", avuData);

		final ModAvuMetadataInp modifyAvuMetadataInp = ModAvuMetadataInp.instanceForDeleteResourceMetadata(resourceName,
				avuData);

		log.debug("sending avu request");

		try {
			getIRODSProtocol().irodsFunction(modifyAvuMetadataInp);
		} catch (JargonException je) {

			if (je.getMessage().indexOf("-817000") > -1) {
				throw new DataNotFoundException("Target resource was not found, could not remove AVU");
			}

			log.error("jargon exception removing AVU metadata", je);
			throw je;
		}

		log.debug("metadata removed");
	}

}
