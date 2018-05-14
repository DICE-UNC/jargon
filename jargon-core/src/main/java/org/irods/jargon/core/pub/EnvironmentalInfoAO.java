package org.irods.jargon.core.pub;

import java.util.List;

import org.irods.jargon.core.connection.IRODSServerProperties;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.domain.RemoteCommandInformation;

public interface EnvironmentalInfoAO extends IRODSAccessObject {

	/**
	 * Retrieve basic environmental information from the iRODS server
	 *
	 * @return {@link IRODSServerProperties} containing information such as boot
	 *         time, version
	 * @throws JargonException
	 *             for iRODS error
	 */
	IRODSServerProperties getIRODSServerPropertiesFromIRODSServer() throws JargonException;

	/**
	 * Get the current time on the iRODS server
	 *
	 * @return {@code long} with the time since epoch that is the current server
	 *         time
	 * @throws JargonException
	 *             for iRODS error
	 */
	long getIRODSServerCurrentTime() throws JargonException;

	/**
	 * List the available remote commands. This is an experimental method subject to
	 * API change.
	 * <p>
	 * Note that this command requires the cmd-scripts/listCommands.sh to be
	 * installed in the target iRODS server/cmd/bin directory, otherwise, a
	 * DataNotFoundException will be thrown.
	 *
	 * @return List of {@link RemoteCommandInformation}
	 * @throws DataNotFoundException
	 *             if the {@code listCommands.sh} script is not in the iRODS remote
	 *             exec bin directory
	 * @throws JargonException
	 *             for iRODS error
	 */
	List<RemoteCommandInformation> listAvailableRemoteCommands() throws DataNotFoundException, JargonException;

	/**
	 * Generate a list of the available microservices on the target server.
	 * <p>
	 * Note that the result will be in the format microservice name:module
	 *
	 * This method will operate on iRODS servers version 3.0 and up.
	 *
	 * @return {@code List<String>} with the names of the available microservices.
	 * @throws JargonException
	 *             for iRODS error
	 */
	List<String> listAvailableMicroservices() throws JargonException;

	/**
	 * Check (by version) whether this server can run specific (SQL) query
	 * <p>
	 * Note that there is some difficulty with using this method from eIRODS 3.0.
	 * See [#1663] iRODS environment shows 'rods3.0' as version
	 * <p>
	 * For EIRODS3.0, it is recommended to use the
	 * SpecificQueryAO.isSupportsSpecificQuery method
	 *
	 * @return {@code boolean} that will be {@code true} if I can run specific query
	 * @throws JargonException
	 *             for iRODS error
	 */
	boolean isAbleToRunSpecificQuery() throws JargonException;

}
