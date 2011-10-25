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
	 */
	IRODSServerProperties getIRODSServerPropertiesFromIRODSServer()
			throws JargonException;

	/**
	 * Get the current time on the iRODS server
	 * 
	 * @return <code>long</code> with the time since epoch that is the current
	 *         server time
	 * @throws JargonException
	 */
	long getIRODSServerCurrentTime() throws JargonException;

	/**
	 * Show the rules loaded into the current iRODS instance as a <code>String</code> containing a 'pretty printed'
	 * contents of the core irb rules
	 * @return <code>String</code> containing the iRODS core contents, will be blank if none could be obtained.
	 * @throws JargonException
	 */
	String showLoadedRules() throws JargonException;

	/**
	 * See if strict ACLs are turned on for the server.
	 * @return <code>boolean</code> that will be <code>true</code> if stict ACL's are enforced.
	 * @throws JargonException
	 */
	boolean isStrictACLs() throws JargonException;

	/**
	 * List the available remote commands.  This is an experimental method subject to API change.
	 * <p/>
	 * Note that this command requires the cmd-scripts/listCommands.sh to be installed in the target iRODS server/cmd/bin 
	 * directory, otherwise, a DataNotFoundException will be thrown.
	 * @return
	 * @throws DataNotFoundException if the <code>listCommands.sh</code> script is not in the iRODS remote exec bin directory
	 * @throws JargonException
	 */
	List<RemoteCommandInformation> listAvailableRemoteCommands()
			throws DataNotFoundException, JargonException;

	/**
	 * Generate a list of the available microservices on the target server.
	 * <p/>
	 * Note that the result will be in the format microservice name:module
	 * 
	 * This method will operate on iRODS servers version 3.0 and up.
	 * @return <code>List<String></code> with the names of the available microservices.
	 * @throws JargonException
	 */
	List<String> listAvailableMicroservices() throws JargonException;

}