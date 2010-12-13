/**
 *
 */
package org.irods.jargon.core.connection;

import java.util.HashMap;
import java.util.Map;

import org.irods.jargon.core.exception.JargonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility to open and maintain connections to iRODS across services. This is
 * used internally to keep connections to iRODS on a per-thread basis. A
 * <code>Map</code> is kept in a ThreadLocal cache with the
 * <code>IRODSAccount</code> as the key. 
 * <p/>
 * Connections are returned to the
 * particular <code>IRODSProtocolManager</code> for disposal or return to cache
 * or pool.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public final class IRODSSession {

	public static final ThreadLocal<Map<String, IRODSCommands>> sessionMap = new ThreadLocal<Map<String, IRODSCommands>>();
	private transient IRODSProtocolManager irodsProtocolManager;
	private Logger log = LoggerFactory.getLogger(IRODSSession.class);

	/**
	 * Close all sessions to iRODS that exist for this Thread.
	 * 
	 * @throws JargonException
	 */
	public void closeSession() throws JargonException {
		log.info("closing all irods sessions");
		final Map<String, IRODSCommands> irodsProtocols = sessionMap.get();
		if (irodsProtocols == null) {
			log.warn("closing session that is already closed, silently ignore");
			return;
		}

		for (IRODSCommands irodsCommands : irodsProtocols.values()) {
			log.debug("found and am closing connection to : {}", irodsCommands
					.getIRODSAccount().toString());
			irodsCommands.disconnect();

			// I don't remove from the map because the map is just going to be
			// set to null in the ThreadLocal below
		}

		log.debug("all sessions closed for this Thread");
		sessionMap.set(null);
	}

	private IRODSSession() {
		log.info("IRODS Session creation");
	}

	private IRODSSession(final IRODSProtocolManager irodsConnectionManager)
			throws JargonException {

		if (irodsConnectionManager == null) {
			throw new JargonException("irods connection manager cannot be null");
		}

		this.irodsProtocolManager = irodsConnectionManager;
	}

	public static IRODSSession instance(
			final IRODSProtocolManager irodsConnectionManager)
			throws JargonException {
		return new IRODSSession(irodsConnectionManager);
	}

	/**
	 * For a given <code>IRODSAccount</code>, create and return, or return a
	 * connection from the cache. This connection is per-Thread, so if another
	 * thread has a cached connection, it is not visible from here, and must be
	 * properly closed on that Thread.
	 * 
	 * @param irodsAccount
	 *            <code>IRODSAccount</code> that describes this connection to
	 *            iRODS.
	 * @return {@link org.irods.jargon.core.connection.IRODSCommands} that
	 *         represents low level (but above the socket level) communications
	 *         to iRODS.
	 * @throws JargonException
	 */
	public IRODSCommands currentConnection(final IRODSAccount irodsAccount)
			throws JargonException {

		if (irodsProtocolManager == null) {
			log.error("no irods connection manager provided");
			throw new JargonException(
					"IRODSSession improperly initialized, requires the IRODSConnectionManager to be initialized");
		}

		if (irodsAccount == null) {
			log.error("irodsAccount is null in connection");
			throw new JargonException("irodsAccount is null");
		}

		log.debug("call to current connection for account: {}",
				irodsAccount.toString());

		IRODSCommands irodsProtocol = null;

		Map<String, IRODSCommands> irodsProtocols = sessionMap.get();

		if (irodsProtocols == null) {
			log.debug("no connections are cached, so create a new cache map");
			irodsProtocols = new HashMap<String, IRODSCommands>();
			irodsProtocol = irodsProtocolManager.getIRODSProtocol(irodsAccount);
			irodsProtocols.put(irodsAccount.toString(), irodsProtocol);
			log.debug("put a reference to a new connection for account: {}",
					irodsAccount.toString());
			sessionMap.set(irodsProtocols);
			return irodsProtocol;
		}

		// there is a protocol map, look up the connection for this account

		log.debug("looking into the session map for a connection that might already be established");

		irodsProtocol = irodsProtocols.get(irodsAccount.toString());

		if (irodsProtocol == null) {
			log.debug("null connection in thread local, using IRODSConnectionManager to create a new connection");
			irodsProtocol = irodsProtocolManager.getIRODSProtocol(irodsAccount);
			if (irodsProtocol == null) {
				log.error("no connection returned from connection manager");
				throw new JargonException(
						"null connection returned from connection manager");
			}
			irodsProtocols.put(irodsAccount.toString(), irodsProtocol);
			log.debug("put a reference to a new connection for account: {}",
					irodsAccount.toString());
			sessionMap.set(irodsProtocols);
		} else {
			log.debug("session using previously established connection:"
					+ irodsProtocol);
		}

		return irodsProtocol;
	}

	/**
	 * @return the irodsConnectionManager
	 */
	public IRODSProtocolManager getIrodsConnectionManager() {
		return irodsProtocolManager;
	}

	/**
	 * @param irodsConnectionManager
	 *            the irodsConnectionManager to set
	 */
	public void setIrodsConnectionManager(
			final IRODSProtocolManager irodsConnectionManager) {
		this.irodsProtocolManager = irodsConnectionManager;
	}

	/**
	 * Close an iRODS session for the given account
	 * 
	 * @param irodsAccount
	 *            <code>IRODSAccount</code> that describes the connection that
	 *            should be closed.
	 * @throws JargonException
	 *             if an error occurs on the close. If the connection does not
	 *             exist it is logged and ignored.
	 */
	public void closeSession(final IRODSAccount irodsAccount)
			throws JargonException {

		log.debug("closing irods session for: {}", irodsAccount.toString());
		final Map<String, IRODSCommands> irodsProtocols = sessionMap.get();
		if (irodsProtocols == null) {
			log.warn("closing session that is already closed, silently ignore");
			return;
		}

		final IRODSCommands irodsProtocol = irodsProtocols.get(irodsAccount
				.toString());

		if (irodsProtocol == null) {
			log.warn("closing a connection that is not held, silently ignore");
			return;

		}
		log.debug("found and am closing connection to : {}",
				irodsAccount.toString());

		irodsProtocol.disconnect();

		irodsProtocols.remove(irodsAccount.toString());
		if (irodsProtocols.isEmpty()) {
			log.debug("no more connections, so clear cache from ThreadLocal");
			sessionMap.set(null);
		}

	}

	/**
	 * This method is not particularly useful, but does provide a route to get a
	 * direct handle on the connections for this Thread in cases where such
	 * status information needs to be kept. Returns null if no map is available.
	 * 
	 * @return
	 */
	public Map<String, IRODSCommands> getIRODSCommandsMap() {
		return sessionMap.get();
	}

}
