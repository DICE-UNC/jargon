package org.irods.jargon.datautils.connection;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSMidLevelProtocol;
import org.irods.jargon.core.connection.IRODSProtocolManager;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.PipelineConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Factory for a pool-able object that is an iRODS connection. In the current
 * implementation, this is a pool of 1 iRODS connection (and
 * {@code IRODSCommands} instance) that will block waiting to get a handle. This
 * is intended for clients that are sharing a single connection to iRODS, and
 * specifically for clients sharing a temporary password connection.
 * <p>
 * In the future, more generalized pooling implementations may be developed, but
 * for now, this is narrowly focused.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class ConnectionCreatingPoolableObjectFactory implements PooledObjectFactory<IRODSMidLevelProtocol> {

	private final IRODSAccount cachedIRODSAccount;
	private final IRODSProtocolManager irodsProtocolManager;
	private final IRODSSession irodsSession;
	private final PipelineConfiguration pipelineConfiguration;

	private Logger log = LogManager.getLogger(ConnectionCreatingPoolableObjectFactory.class);

	/**
	 * Constructor will build a connection source based on the given
	 * {@code cachedIRODSAccount} and return an open connection on demand.
	 *
	 * @param cachedIRODSAccount
	 *            {@link IRODSAccount} that will describe the source of the
	 *            connection to iRODS
	 * @param irodsSession
	 *            {@link IRODSSession}
	 * @param irodsProtocolManager
	 *            {@link IRODSProtocolManager}
	 */
	public ConnectionCreatingPoolableObjectFactory(final IRODSAccount cachedIRODSAccount,
			final IRODSSession irodsSession, final IRODSProtocolManager irodsProtocolManager) {
		if (cachedIRODSAccount == null) {
			throw new IllegalArgumentException("null cachedIRODSAccount");
		}
		if (irodsSession == null) {
			throw new IllegalArgumentException("null irodsSession");
		}
		if (irodsProtocolManager == null) {
			throw new IllegalArgumentException("null irodsProtocolManager");
		}
		this.cachedIRODSAccount = cachedIRODSAccount;
		log.info("caching iRODS account:{}", cachedIRODSAccount);
		this.irodsProtocolManager = irodsProtocolManager;
		this.irodsSession = irodsSession;

		pipelineConfiguration = irodsSession.buildPipelineConfigurationBasedOnJargonProperties();

	}

	@Override
	public void activateObject(PooledObject<IRODSMidLevelProtocol> objectToActivate) throws Exception {
		log.info("activateObject:{}", objectToActivate);
	}

	@Override
	public void destroyObject(PooledObject<IRODSMidLevelProtocol> objectToDestroy) throws Exception {
		log.info("destroyObject:{}", objectToDestroy.getObject());
		IRODSMidLevelProtocol irodsMidLevelProtocol = objectToDestroy.getObject();
		irodsMidLevelProtocol.setIrodsProtocolManager(irodsProtocolManager);
		log.info("disconnecting");
		irodsMidLevelProtocol.shutdown();

	}

	@Override
	public PooledObject<IRODSMidLevelProtocol> makeObject() throws Exception {
		log.info("makeObject returns a new iRODS connection");
		return new DefaultPooledObject<IRODSMidLevelProtocol>(
				irodsProtocolManager.getIRODSProtocol(cachedIRODSAccount, pipelineConfiguration, irodsSession));
	}

	@Override
	public void passivateObject(PooledObject<IRODSMidLevelProtocol> objectToPassivate) throws Exception {
		log.info("passivateObject()");
		// nothing done here
	}

	@Override
	public boolean validateObject(PooledObject<IRODSMidLevelProtocol> objectToValidate) {
		return true;
	}

}
