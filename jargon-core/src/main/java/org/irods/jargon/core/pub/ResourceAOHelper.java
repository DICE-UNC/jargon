/**
 *
 */
package org.irods.jargon.core.pub;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.aohelper.AOHelper;
import org.irods.jargon.core.pub.domain.Resource;
import org.irods.jargon.core.pub.domain.Zone;
import org.irods.jargon.core.query.GenQueryBuilderException;
import org.irods.jargon.core.query.IRODSGenQueryBuilder;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSet;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.core.utils.IRODSDataConversionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper functions for ResourceAO access object.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
class ResourceAOHelper extends AOHelper {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private Zone lastZone = null;
	private final ZoneAO zoneAO;

	/**
	 * Default constructor
	 *
	 * @throws JargonException
	 */
	protected ResourceAOHelper(final IRODSAccount irodsAccount,
			final IRODSAccessObjectFactory irodsAccessObjectFactory)
			throws JargonException {
		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		if (irodsAccessObjectFactory == null) {
			throw new IllegalArgumentException("null irodsAccessObjectFactory");
		}

		zoneAO = irodsAccessObjectFactory.getZoneAO(irodsAccount);
	}

	/**
	 * Build a string with selects for resource
	 *
	 * @return <code>String</code> with a select statement to get resource data
	 *         via GenQuery. "SELECT" is included.
	 */
	protected String buildResourceSelectsClassic() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT ");
		query.append(RodsGenQueryEnum.COL_R_RESC_ID.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_R_RESC_NAME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_R_ZONE_NAME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_R_TYPE_NAME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_R_CLASS_NAME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_R_LOC.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_R_VAULT_PATH.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_R_FREE_SPACE.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_R_FREE_SPACE_TIME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_R_RESC_INFO.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_R_RESC_COMMENT.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_R_CREATE_TIME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_R_MODIFY_TIME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_R_RESC_STATUS.getName());
		return query.toString();
	}

	/**
	 * Build selects for resource
	 *
	 * @param builder
	 *            {@link IRODSGenQueryBuilder}
	 */
	protected void buildResourceSelects(final IRODSGenQueryBuilder builder)
			throws JargonException {

		if (builder == null) {
			throw new IllegalArgumentException("null builder");
		}

		try {
			builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_RESC_ID)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_RESC_NAME)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_ZONE_NAME)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_TYPE_NAME)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_CLASS_NAME)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_LOC)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_VAULT_PATH)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_FREE_SPACE)
					.addSelectAsGenQueryValue(
							RodsGenQueryEnum.COL_R_FREE_SPACE_TIME)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_RESC_INFO)
					.addSelectAsGenQueryValue(
							RodsGenQueryEnum.COL_R_RESC_COMMENT)
					.addSelectAsGenQueryValue(
							RodsGenQueryEnum.COL_R_CREATE_TIME)
					.addSelectAsGenQueryValue(
							RodsGenQueryEnum.COL_R_MODIFY_TIME)
					.addSelectAsGenQueryValue(
							RodsGenQueryEnum.COL_R_RESC_STATUS);
		} catch (GenQueryBuilderException e) {
			throw new JargonException(e);
		}

	}

	/**
	 * From a query result row, build a <code>Resource</code> domain object
	 *
	 * @param row
	 *            {@link IRODSQueryResultRow} with the result of a gen query.
	 *            Each row represents resource data
	 * @return {@link Resource} domain object
	 * @throws JargonException
	 */
	protected Resource buildResourceFromResultSetRowClassic(
			final IRODSQueryResultRow row) throws JargonException {
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
				String message = "no zone found for zone in resource="
						+ zoneName;
				log.error(message);
				throw new JargonException(
						"zone not found for resource, data integrity error", e);
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
			log.warn("unable to format resourceFreeSpace for value:"
					+ row.getColumn(7) + " setting to 0");
		}

		resource.setFreeSpaceTime(IRODSDataConversionUtil
				.getDateFromIRODSValue(row.getColumn(8)));
		resource.setInfo(row.getColumn(9));
		resource.setComment(row.getColumn(10));
		resource.setCreateTime(IRODSDataConversionUtil
				.getDateFromIRODSValue(row.getColumn(11)));
		resource.setModifyTime(IRODSDataConversionUtil
				.getDateFromIRODSValue(row.getColumn(12)));
		resource.setStatus(row.getColumn(13));

		if (log.isInfoEnabled()) {
			log.info("resource built \n");
			log.info(resource.toString());
		}

		return resource;
	}

	/**
	 * From a result set for a resource query, build the <code>Resource</code>
	 * domain objects.
	 *
	 * @param resultSet
	 *            {@link IRODSQueryResultSetInterface} with a gen query result
	 * @return <code>List</code> of {@link Resource}, which will be empty if no
	 *         results
	 * @throws JargonException
	 */
	protected List<Resource> buildResourceListFromResultSetClassic(
			final IRODSQueryResultSetInterface resultSet)
			throws JargonException {

		List<Resource> resources = new ArrayList<Resource>();

		for (IRODSQueryResultRow row : resultSet.getResults()) {
			resources.add(buildResourceFromResultSetRowClassic(row));
		}

		return resources;
	}

	/**
	 * Build genquery selects for a resource query against the new composable
	 * resource tree
	 *
	 * @return
	 * @throws GenQueryBuilderException
	 */
	IRODSGenQueryBuilder buildResourceSelectsComposable()
			throws GenQueryBuilderException {
		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);

		builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_RESC_ID)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_RESC_NAME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_ZONE_NAME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_TYPE_NAME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_CLASS_NAME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_LOC)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_VAULT_PATH)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_FREE_SPACE)
				.addSelectAsGenQueryValue(
						RodsGenQueryEnum.COL_R_FREE_SPACE_TIME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_RESC_INFO)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_RESC_COMMENT)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_CREATE_TIME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_MODIFY_TIME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_RESC_STATUS)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_RESC_PARENT)
				// .addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_RESC_CHILDREN)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_RESC_CONTEXT);

		return builder;

	}

	/**
	 * Given a result of a query as generated by
	 * <code>buildResourceSelectsComposable()</code>, create
	 * <code>Resource</code> objects that represent the results
	 *
	 * @param resultSet
	 * @return
	 * @throws JargonException
	 */
	public List<Resource> buildResourceListFromResultSetComposable(
			final IRODSQueryResultSet resultSet) throws JargonException {
		List<Resource> resources = new ArrayList<Resource>();

		for (IRODSQueryResultRow row : resultSet.getResults()) {
			resources.add(buildResourceFromResultSetRowComposable(row));
		}

		return resources;
	}

	private Resource buildResourceFromResultSetRowComposable(
			final IRODSQueryResultRow row) throws JargonException {
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
				String message = "no zone found for zone in resource="
						+ zoneName;
				log.error(message);
				throw new JargonException(
						"zone not found for resource, data integrity error", e);
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
			log.warn("unable to format resourceFreeSpace for value:"
					+ row.getColumn(7) + " setting to 0");
		}

		resource.setFreeSpaceTime(IRODSDataConversionUtil
				.getDateFromIRODSValue(row.getColumn(8)));
		resource.setInfo(row.getColumn(9));
		resource.setComment(row.getColumn(10));
		resource.setCreateTime(IRODSDataConversionUtil
				.getDateFromIRODSValue(row.getColumn(11)));
		resource.setModifyTime(IRODSDataConversionUtil
				.getDateFromIRODSValue(row.getColumn(12)));
		resource.setStatus(row.getColumn(13));
		resource.setParentName(row.getColumn(14));
		// resource.setImmediateChildren(formatImmediateChildren(row.getColumn(15)));
		// children
		resource.setContextString(row.getColumn(15));

		return resource;
	}

	List<String> formatImmediateChildren(final String childrenString) {

		if (childrenString == null) {
			throw new IllegalArgumentException("null childrenString");
		}

		List<String> immediateChildren = new ArrayList<String>();

		// empty list if no children
		if (childrenString.isEmpty()) {
			return immediateChildren;
		}

		String[] items = childrenString.split(";");

		for (String item : items) {
			if (item.isEmpty()) {
				continue;
			}

			int idxBracket = item.indexOf("{");

			if (idxBracket > -1) {
				immediateChildren.add(item.substring(0, idxBracket));
			} else {
				immediateChildren.add(item);
			}

		}

		return immediateChildren;

	}

}
