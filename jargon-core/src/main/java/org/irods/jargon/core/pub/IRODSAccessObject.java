/**
 *
 */
package org.irods.jargon.core.pub;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSCommands;
import org.irods.jargon.core.connection.IRODSServerProperties;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.JargonException;

/**
 * Generic interface that desribes an object that accesses an underlying IRODS
 * domain object
 * 
 * @author Mike Conway - DICE (www.irods.org)
 */
public interface IRODSAccessObject {

	IRODSCommands getIRODSProtocol() throws JargonException;

	IRODSSession getIRODSSession();

	/**
	 * Convenience method that gets the underlying <code>IRODSAccount</code>
	 * associated with the connection.
	 * 
	 * @return
	 */
	IRODSAccount getIRODSAccount();

	/**
	 * Convenience method that gets the underlying
	 * <code>IRODSServerProperties</code> associated with the connection.
	 * 
	 * @return
	 */
	IRODSServerProperties getIRODSServerProperties() throws JargonException;

}
