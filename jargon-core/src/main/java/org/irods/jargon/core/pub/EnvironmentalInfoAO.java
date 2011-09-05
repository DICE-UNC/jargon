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

}