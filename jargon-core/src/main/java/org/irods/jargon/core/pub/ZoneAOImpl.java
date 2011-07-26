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
import org.irods.jargon.core.query.IRODSGenQuery;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.RodsGenQueryEnum;
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

		IRODSGenQueryExecutorImpl irodsGenQueryExecutorImpl = new IRODSGenQueryExecutorImpl(
				this.getIRODSSession(), this.getIRODSAccount());
		ResourceAO resourceAO = new ResourceAOImpl(this.getIRODSSession(),
				this.getIRODSAccount());
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

		List<Zone> zones = new ArrayList<Zone>();
		Zone zone;

		for (IRODSQueryResultRow row : resultSet.getResults()) {
			zone = new Zone();
			zone.setZoneId(row.getColumn(0));
			zone.setZoneName(row.getColumn(1));
			zone.setZoneType(row.getColumn(2));
			zone.setZoneConnection(row.getColumn(3));
			zone.setZoneComment(row.getColumn(4));
			zone.setResources(resourceAO.listResourcesInZone(zone.getZoneName()));
			// TODO: set up the dates
			zones.add(zone);
			if (log.isInfoEnabled()) {
				log.info("got zone \n");
				log.info(zone.toString());
			}
		}

		return zones;

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
				this.getIRODSSession(), this.getIRODSAccount());
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

		Zone zone = new Zone();
		zone.setZoneId(row.getColumn(0));
		zone.setZoneName(row.getColumn(1));
		zone.setZoneType(row.getColumn(2));
		zone.setZoneConnection(row.getColumn(3));
		zone.setZoneComment(row.getColumn(4));
		// TODO: set up the dates

		if (log.isInfoEnabled()) {
			log.info("got zone \n");
			log.info(zone.toString());
		}
		return zone;
	}
}
