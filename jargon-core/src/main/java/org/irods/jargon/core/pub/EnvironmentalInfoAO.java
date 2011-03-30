package org.irods.jargon.core.pub;

import org.irods.jargon.core.connection.IRODSServerProperties;
import org.irods.jargon.core.exception.JargonException;

public interface EnvironmentalInfoAO extends IRODSAccessObject {

	/**
	 * Retrieve basic environmental information from the iRODS server
	 * 
	 * @return {@link IRODSServerProperties} containing information such as boot
	 *         time, version
	 * @throws JargonException
	 */
	IRODSServerProperties getIRODSServerPropertiesFromIRODSServer()
			throws JargonException;

	/**
	 * Get the current time on the iRODS server
	 * @return <code>long</code> with the time since epoch that is the current server time
	 * @throws JargonException
	 */
	long getIRODSServerCurrentTime() throws JargonException;

}