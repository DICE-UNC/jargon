package org.irods.jargon.core.pub;

import java.util.List;

import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.domain.Zone;

public interface ZoneAO extends IRODSAccessObject {

	/**
	 * List all zones
	 *
	 * @return List of {@link Zone}
	 * @throws JargonException
	 *             for iRODS error
	 */
	List<Zone> listZones() throws JargonException;

	/**
	 * List all zone names
	 *
	 * @return {@code List<String>}
	 * @throws JargonException
	 *             for iRODS error
	 */
	List<String> listZoneNames() throws JargonException;

	/**
	 * Given a zone name, get the detailed information
	 *
	 * @param zoneName
	 *            {@code String} with the unique name of the iRODS zone
	 * @return {@link Zone} domain object. Note that a {@code DataNotFoundException}
	 *         will result if the zone is not in iRODS.
	 * @throws JargonException
	 *             for iRODS error
	 * @throws DataNotFoundException
	 *             for missing zone
	 */
	Zone getZoneByName(String zoneName) throws JargonException, DataNotFoundException;

}