package org.irods.jargon.core.pub;

import java.util.List;

import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.domain.Zone;

public interface ZoneAO extends IRODSAccessObject {

	/**
	 * List all zones
	 * 
	 * @return
	 * @throws JargonException
	 */
	List<Zone> listZones() throws JargonException;

	/**
	 * Given a zone name, get the detailed information
	 * 
	 * @param zoneName
	 *            <code>String</code> with the unique name of the iRODS zone
	 * @return {@link Zone} domain object. Note that a
	 *         <code>DataNotFoundException</code> will result if the zone is not
	 *         in iRODS.
	 * @throws JargonException
	 * @throws DataNotFoundException
	 */
	Zone getZoneByName(String zoneName) throws JargonException,
			DataNotFoundException;

}