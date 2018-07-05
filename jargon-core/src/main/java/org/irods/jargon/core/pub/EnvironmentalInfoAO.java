package org.irods.jargon.core.pub;

import java.util.List;

import org.irods.jargon.core.connection.EnvironmentalInfoAccessor;
import org.irods.jargon.core.connection.IRODSServerProperties;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.domain.ClientHints;
import org.irods.jargon.core.pub.domain.RemoteCommandInformation;

public interface EnvironmentalInfoAO extends IRODSAccessObject {

	/**
	 * Retrieve basic environmental information from the iRODS server
	 *
	 * @return {@link IRODSServerProperties} containing information such as boot
	 *         time, version
	 * @throws JargonException
	 *             {@link JargonException}
	 */
	IRODSServerProperties getIRODSServerPropertiesFromIRODSServer() throws JargonException;

	/**
	 * Get the current time on the iRODS server
	 *
	 * @return {@code long} with the time since epoch that is the current server
	 *         time
	 * @throws JargonException
	 *             {@link JargonException}
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
	 *             {@link JargonException}
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
	 *             {@link JargonException}
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
	 *             {@link JargonException}
	 */
	boolean isAbleToRunSpecificQuery() throws JargonException;

	/**
	 * Make a call and retrieve the available client hints. Note this may be
	 * <code>null</code> if the version of iRODS does not support client hints API.
	 * <p>
	 * Note that the {@link EnvironmentalInfoAccessor} folds this information into
	 * the {@link IRODSServerProperties} object and does simple caching to reduce
	 * traffic to iRODS. A refresh flag will force the data to be updated and
	 * re-cached.
	 * 
	 * @param refresh
	 *            <code>boolean</code> to refresh any cached value
	 * @return {@link ClientHints} describing the iRODS server or <code>null</code>
	 *         if that data is not available
	 * @throws JargonException
	 *             {@link JargonException}
	 */
	ClientHints retrieveClientHints(final boolean refresh) throws JargonException;

}
