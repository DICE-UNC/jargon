/**
 *
 */
package org.irods.jargon.core.pub;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.domain.Zone;
import org.irods.jargon.core.query.GenQueryBuilderException;
import org.irods.jargon.core.query.GenQueryOrderByField.OrderByType;
import org.irods.jargon.core.query.IRODSGenQuery;
import org.irods.jargon.core.query.IRODSGenQueryBuilder;
import org.irods.jargon.core.query.IRODSGenQueryFromBuilder;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSet;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.core.utils.IRODSDataConversionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Access to CRUD and query operations on IRODS Zone.
 *
 * AO objects are not shared between threads. Jargon services will confine
 * activities to one connection per thread.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public final class ZoneAOImpl extends IRODSGenericAO implements ZoneAO {

	private static final Logger log = LoggerFactory.getLogger(ZoneAOImpl.class);

	protected ZoneAOImpl(final IRODSSession irodsSession,
			final IRODSAccount irodsAccount) throws JargonException {
		super(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.accessobject.ZoneAO#listZones()
	 */
	@Override
	public List<Zone> listZones() throws JargonException {

		IRODSGenQueryExecutor irodsGenQueryExecutor = new IRODSGenQueryExecutorImpl(
				getIRODSSession(), getIRODSAccount());

		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);
		IRODSQueryResultSet resultSet;

		try {
			builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_ZONE_ID)
			.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_ZONE_NAME)
			.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_ZONE_TYPE)
			.addSelectAsGenQueryValue(
					RodsGenQueryEnum.COL_ZONE_CONNECTION)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_ZONE_COMMENT)
					.addSelectAsGenQueryValue(
							RodsGenQueryEnum.COL_ZONE_CREATE_TIME)
							.addSelectAsGenQueryValue(
									RodsGenQueryEnum.COL_ZONE_MODIFY_TIME)
									.addOrderByGenQueryField(RodsGenQueryEnum.COL_ZONE_NAME,
											OrderByType.ASC);

			IRODSGenQueryFromBuilder irodsQuery = builder
					.exportIRODSQueryFromBuilder(50);

			resultSet = irodsGenQueryExecutor
					.executeIRODSQueryAndCloseResultInZone(irodsQuery, 0, "");
		} catch (JargonQueryException e) {
			log.error(CollectionListingUtils.QUERY_EXCEPTION_FOR_QUERY, e);
			throw new JargonException("error in query", e);
		} catch (GenQueryBuilderException e) {
			log.error(CollectionListingUtils.QUERY_EXCEPTION_FOR_QUERY, e);
			throw new JargonException("error in query", e);
		}

		List<Zone> zones = new ArrayList<Zone>();
		Zone zone;

		for (IRODSQueryResultRow row : resultSet.getResults()) {
			zone = buildZoneForRow(row);

			zones.add(zone);
			log.info("got zone:{}", zone.toString());

		}

		return zones;

	}

	private Zone buildZoneForRow(final IRODSQueryResultRow row)
			throws JargonException {
		Zone zone;
		zone = new Zone();
		zone.setZoneId(row.getColumn(0));
		zone.setZoneName(row.getColumn(1));
		zone.setZoneType(row.getColumn(2));
		zone.setZoneConnection(row.getColumn(3));
		zone.setZoneComment(row.getColumn(4));
		zone.setZoneCreateTime(IRODSDataConversionUtil
				.getDateFromIRODSValue(row.getColumn(5)));
		zone.setZoneModifyTime(IRODSDataConversionUtil
				.getDateFromIRODSValue(row.getColumn(6)));

		String[] components = zone.getZoneConnection().split(":");
		if (components.length == 0) {
			// nothing
		} else if (components.length == 1) {
			zone.setHost(components[0]);
		} else if (components.length == 2) {
			zone.setHost(components[0]);
			try {
				zone.setPort(Integer.parseInt(components[1]));
			} catch (NumberFormatException e) {
				log.error("unable to parse connection string:{}",
						zone.getZoneConnection(), e);
				throw new JargonException(
						"error parsing zone connection string", e);
			}
		} else {
			throw new JargonException(
					"unable to parse connection for host and port");
		}
		return zone;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.ZoneAO#getZoneByName(java.lang.String)
	 */
	@Override
	public Zone getZoneByName(final String zoneName) throws JargonException,
	DataNotFoundException {

		if (zoneName.equals("tempZone")) {
			Zone tempZone = new Zone();
			tempZone.setZoneName("tempZone");
			return tempZone;
		}

		IRODSGenQueryExecutorImpl irodsGenQueryExecutorImpl = new IRODSGenQueryExecutorImpl(
				getIRODSSession(), getIRODSAccount());
		StringBuilder zoneQuery = new StringBuilder();
		char comma = ',';

		zoneQuery.append("select ");
		zoneQuery.append(RodsGenQueryEnum.COL_ZONE_ID.getName());
		zoneQuery.append(comma);
		zoneQuery.append(RodsGenQueryEnum.COL_ZONE_NAME.getName());
		zoneQuery.append(comma);
		zoneQuery.append(RodsGenQueryEnum.COL_ZONE_TYPE.getName());
		zoneQuery.append(comma);
		zoneQuery.append(RodsGenQueryEnum.COL_ZONE_CONNECTION.getName());
		zoneQuery.append(comma);
		zoneQuery.append(RodsGenQueryEnum.COL_ZONE_COMMENT.getName());
		zoneQuery.append(comma);
		zoneQuery.append(RodsGenQueryEnum.COL_ZONE_CREATE_TIME.getName());
		zoneQuery.append(comma);
		zoneQuery.append(RodsGenQueryEnum.COL_ZONE_MODIFY_TIME.getName());
		zoneQuery.append(" where ");
		zoneQuery.append(RodsGenQueryEnum.COL_ZONE_NAME.getName());
		zoneQuery.append(" = '");
		zoneQuery.append(zoneName);
		zoneQuery.append("'");

		String queryString = zoneQuery.toString();
		if (log.isInfoEnabled()) {
			log.info("zone query:" + toString());
		}

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString, 500);

		IRODSQueryResultSetInterface resultSet;
		try {
			resultSet = irodsGenQueryExecutorImpl
					.executeIRODSQueryAndCloseResult(irodsQuery, 0);
		} catch (JargonQueryException e) {
			log.error("query exception for:" + queryString, e);
			throw new JargonException("error in query");
		}

		if (resultSet.getResults().size() == 0) {
			StringBuilder message = new StringBuilder();
			message.append("zone not found for name:");
			message.append(zoneName);
			log.warn(message.toString());
			throw new DataNotFoundException(message.toString());
		}

		IRODSQueryResultRow row = resultSet.getResults().get(0);
		return buildZoneForRow(row);
	}
}
