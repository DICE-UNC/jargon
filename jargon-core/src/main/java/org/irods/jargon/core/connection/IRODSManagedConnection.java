/**
 * 
 */
package org.irods.jargon.core.connection;

import org.irods.jargon.core.exception.JargonException;

/**
 * Represents the interface for maintenance of a connection to the IRODS system,
 * and the iteraction between an {@link IRODSProtocolManager
 * IRODSConnectionManager} and an {@link IRODSConnection IRODSConnection}
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface IRODSManagedConnection {

	void disconnect() throws JargonException;

	void shutdown() throws JargonException;

	void obliterateConnectionAndDiscardErrors();

	void disconnectWithIOException() throws JargonException;

	String getConnectionUri() throws JargonException;

	boolean isConnected();

}
