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

	/**
	 * Called to signal that this connection is done, and may be closed or
	 * returned by the client in a normal fashion.
	 * 
	 * @throws JargonException
	 */
	void disconnect() throws JargonException;

	/**
	 * Internal method to actually close the underlying connection by sending a
	 * disconnect to IRODS, and then physically closing down the socket. This
	 * method will be called at the appropriate time by the (@link
	 * IRODSConnectionManager IRODSConnectionManager} at the appropriate time.
	 * 
	 * @throws JargonException
	 */
	void shutdown() throws JargonException;

	/**
	 * Internal method to close the underlying connection by closing the socket
	 * without further communication. This method can be used by the
	 * <code>IRODSSession</code> when the connection is known to have
	 * malfunction (e.g. an agent has terminated abnormally)
	 */
	void obliterateConnectionAndDiscardErrors();

	/**
	 * Called by a client to signal that  the connection should be cleaned up. This should be
	 * called in methods accessing the underlying iRODS socket where a socket or
	 * i/o exception has occurred, making it unlikely that the normal iRODS
	 * disconnect sequence will work. This prevents 'hung' connections.
	 * <p/>
	 * This is also used in some authentication scenarios where the connection needs to be reset because of multiple connection
	 * phases, such as pam login.
	 * 
	 * @throws JargonException
	 */
	void disconnectWithForce() throws JargonException;

	/**
	 * Get a simple <code>URI</code> format that describes the connection
	 * 
	 * @return
	 * @throws JargonException
	 */
	String getConnectionUri() throws JargonException;

	/**
	 * Is this connection currently connected to an iRODS agent. This is based
	 * on the last communication with iRODS, and does not 'ping'. That might be
	 * a good addition.
	 * 
	 * @return
	 */
	boolean isConnected();

	/**
	 * Get the {@link IRODSSession} that holds this connection. This is provided
	 * when the <code>IRODSSession</code> obtained the connection on behalf of
	 * the client.
	 * 
	 * @return
	 */
	IRODSSession getIrodsSession();

	/**
	 * Called by {@IRODSSession} when the connection is obtained,
	 * this gives the connection a reference back to the session cache in case
	 * errors occur that require the session to terminate a connection.
	 * 
	 * @param irodsSession
	 */
	void setIrodsSession(IRODSSession irodsSession);

	/**
	 * Get the iRODS account associated with this connection
	 * 
	 * @return
	 */
	IRODSAccount getIrodsAccount();

}
