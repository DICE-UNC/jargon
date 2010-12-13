package org.irods.jargon.core.pub;

import org.irods.jargon.core.connection.IRODSServerProperties;
import org.irods.jargon.core.exception.JargonException;

public interface EnvironmentalInfoAO {

	/**
	 * Retrieve basic environmental information from the iRODS server
	 * 
	 * @return {@link IRODSServerProperties} containing information such as boot
	 *         time, version
	 * @throws JargonException
	 */
	IRODSServerProperties getIRODSServerPropertiesFromIRODSServer()
			throws JargonException;

}