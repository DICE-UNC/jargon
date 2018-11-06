/**
 * 
 */
package org.irods.jargon.pool.conncache;

import org.irods.jargon.core.connection.IRODSProtocolManager;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.IRODSSimpleProtocolManager;

/**
 * Bootstraps an {@link IRODSProtocolManager} that supports connection pooling
 * in an easy way. It is recommended to use this when employing connection
 * pooling in a Spring based application. This avoids some interdependencies
 * that can cause circular reference problems since the connection pool uses its
 * own internal {@link IRODSProtocolManager} when creating new pooled
 * connections.
 * <p>
 * Proper usage is to create an {@code IRODSSession} and pass it in as a
 * parameter. This bootstrapper will create an {@code IRODSProtocolManager} that
 * utilizes a connection pooling mechanism. This is typically done with Spring
 * but can also be done purely in code. An optional
 * {@link JargonKeyedPoolConfig} can also be passed in, otherwise some basic
 * defaults will be used.
 * <p>
 * Once the dependencies are in place, the {@code init()} method should be
 * called to set up the connection pool.
 * 
 * @author conwaymc
 *
 */
public class ConnectionPoolingProtocolManagerBootstrapper {

	private JargonKeyedPoolConfig jargonKeyedPoolConfig;
	private IRODSSession irodsSession;

	/**
	 * Default constructor
	 */
	public ConnectionPoolingProtocolManagerBootstrapper() {

	}

	/**
	 * This method must be called after the expected dependencies have been set. The
	 * method will create a cached connection pool and set this up as the protocol
	 * manager used in the {@code IRODSSession}.
	 */
	public void init() {
		if (irodsSession == null) {
			throw new IllegalStateException("IRODSSession not provided");
		}

		JargonPooledObjectFactory jargonPooledObjectFactory = new JargonPooledObjectFactory();
		jargonPooledObjectFactory.setIrodsSession(irodsSession);
		IRODSSimpleProtocolManager irodsSimpleProtocolManager = new IRODSSimpleProtocolManager();
		jargonPooledObjectFactory.setIrodsSimpleProtocolManager(irodsSimpleProtocolManager);
		JargonConnectionCache jargonConnectionCache = new JargonConnectionCache(jargonPooledObjectFactory,
				jargonKeyedPoolConfig);

		CachedIrodsProtocolManager cachedIrodsProtocolManager = new CachedIrodsProtocolManager();
		cachedIrodsProtocolManager.setJargonConnectionCache(jargonConnectionCache);
		irodsSession.setIrodsProtocolManager(cachedIrodsProtocolManager);

	}

	/**
	 * @return the jargonKeyedPoolConfig {@link JargonKeyedPoolConfig} that can
	 *         control aspects of the pool behavior.
	 */
	public JargonKeyedPoolConfig getJargonKeyedPoolConfig() {
		return jargonKeyedPoolConfig;
	}

	/**
	 * @param jargonKeyedPoolConfig
	 *            the jargonKeyedPoolConfig to set {@link JargonKeyedPoolConfig}
	 */
	public void setJargonKeyedPoolConfig(JargonKeyedPoolConfig jargonKeyedPoolConfig) {
		this.jargonKeyedPoolConfig = jargonKeyedPoolConfig;
	}

	/**
	 * @return the irodsSession {@link IRODSSession} that will have been provisioned
	 *         with a pooled connection manager.
	 */
	public IRODSSession getIrodsSession() {
		return irodsSession;
	}

	/**
	 * @param irodsSession
	 *            the irodsSession to set
	 */
	public void setIrodsSession(IRODSSession irodsSession) {
		this.irodsSession = irodsSession;
	}

}
