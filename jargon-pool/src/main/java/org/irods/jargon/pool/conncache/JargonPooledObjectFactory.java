package org.irods.jargon.pool.conncache;

import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSMidLevelProtocol;
import org.irods.jargon.core.connection.IRODSProtocolManager;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.IRODSSimpleProtocolManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Factory for pooled iRODS connections. Note that the key of the pool is the
 * {@link IRODSAccount} that was originally used to create the object. This may
 * have resulted in a secondary iRODS account, for example, as a proxy account,
 * a temp account, or through some other transformation.
 * 
 * @author conwaymc
 *
 */
public class JargonPooledObjectFactory extends BaseKeyedPooledObjectFactory<IRODSAccount, IRODSMidLevelProtocol> {

	public static final Logger log = LogManager.getLogger(JargonPooledObjectFactory.class);

	/**
	 * Expected injected dependency {@link IRODSSimpleProtocolManager} that will be
	 * the source of the actual live connection.
	 */
	private IRODSProtocolManager irodsSimpleProtocolManager;

	/**
	 * Expected injected dependency {@link IRODSSession}
	 */
	private IRODSSession irodsSession;

	public IRODSSession getIrodsSession() {
		return irodsSession;
	}

	public void setIrodsSession(final IRODSSession irodsSession) {
		this.irodsSession = irodsSession;
	}

	/**
	 * @return the irodsSimpleProtocolManager
	 */
	public IRODSProtocolManager getIrodsSimpleProtocolManager() {
		return irodsSimpleProtocolManager;
	}

	/**
	 * @param irodsSimpleProtocolManager
	 *            the irodsSimpleProtocolManager to set
	 */
	public void setIrodsSimpleProtocolManager(final IRODSProtocolManager irodsSimpleProtocolManager) {
		this.irodsSimpleProtocolManager = irodsSimpleProtocolManager;
	}

	@Override
	public IRODSMidLevelProtocol create(final IRODSAccount irodsAccount) throws Exception {
		log.info("create()");
		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}
		return irodsSimpleProtocolManager.getIRODSProtocol(irodsAccount,
				irodsSession.buildPipelineConfigurationBasedOnJargonProperties(), irodsSession);
	}

	@Override
	public PooledObject<IRODSMidLevelProtocol> wrap(final IRODSMidLevelProtocol irodsMidLevelProtocol) {
		log.info("wrap()");
		return new DefaultPooledObject<IRODSMidLevelProtocol>(irodsMidLevelProtocol);
	}

	@Override
	public void destroyObject(final IRODSAccount key, final PooledObject<IRODSMidLevelProtocol> p) throws Exception {
		log.info("disconnecting()");
		p.getObject().shutdown();
		super.destroyObject(key, p);
	}

	@Override
	public boolean validateObject(final IRODSAccount key, final PooledObject<IRODSMidLevelProtocol> p) {
		return p.getObject().isConnected();
	}

}
