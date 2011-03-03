/**
 *
 */
package org.irods.jargon.core.pub;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSCommands;
import org.irods.jargon.core.connection.IRODSServerProperties;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.JargonProperties;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.io.IRODSFileFactory;

/**
 * Generic interface that desribes an object that accesses an underlying IRODS
 * domain object
 * 
 * @author Mike Conway - DICE (www.irods.org)
 */
public interface IRODSAccessObject {

	IRODSCommands getIRODSProtocol() throws JargonException;

	/**
	 * Get the <code>IRODSSession</code> that provides connections to the iRODS
	 * server.
	 * 
	 * @return {@link IRODSSession}
	 */
	IRODSSession getIRODSSession();

	/**
	 * Get the <code>IRODSAccount</code> that describes the connection.
	 * 
	 * @return {@link IRODSAccount}
	 */
	IRODSAccount getIRODSAccount();

	/**
	 * Convenience method that gets the underlying
	 * <code>IRODSServerProperties</code> associated with the connection.
	 * 
	 * @return
	 */
	IRODSServerProperties getIRODSServerProperties() throws JargonException;

	/**
	 * Convenience method gets configuration information that tunes Jargon
	 * behavior
	 * 
	 * @return {@link JargonProperties}
	 */
	JargonProperties getJargonProperties();

	/**
	 * Retrieve a factory that can create other access objects
	 * @return {@link IRODSAccessObjectFactory}
	 * @throws JargonException
	 */
	IRODSAccessObjectFactory getIRODSAccessObjectFactory()
			throws JargonException;

	/**
	 * Retrieve a factory that can create iRODS file objects for this connected account
	 * @return {@link IRODSFileFactory}
	 * @throws JargonException
	 */
	IRODSFileFactory getIRODSFileFactory() throws JargonException;

}
