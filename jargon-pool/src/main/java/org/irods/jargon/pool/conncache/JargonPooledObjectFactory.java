package org.irods.jargon.pool.conncache;

import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.irods.jargon.core.connection.AbstractIRODSMidLevelProtocol;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSProtocolManager;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.IRODSSimpleProtocolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JargonPooledObjectFactory
		extends BaseKeyedPooledObjectFactory<IRODSAccount, AbstractIRODSMidLevelProtocol> {

	public static final Logger log = LoggerFactory.getLogger(JargonPooledObjectFactory.class);

	/**
	 * Expected injected dependency {@link IRODSSimpleProtocolManager} that will
	 * be the source of the actual live connection.
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
	public AbstractIRODSMidLevelProtocol create(final IRODSAccount irodsAccount) throws Exception {
		log.info("create()");
		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}
		return irodsSimpleProtocolManager.getIRODSProtocol(irodsAccount,
				irodsSession.buildPipelineConfigurationBasedOnJargonProperties(), irodsSession);
	}

	@Override
	public PooledObject<AbstractIRODSMidLevelProtocol> wrap(final AbstractIRODSMidLevelProtocol irodsMidLevelProtocol) {
		log.info("wrap()");
		return new DefaultPooledObject<AbstractIRODSMidLevelProtocol>(irodsMidLevelProtocol);
	}

	@Override
	public void destroyObject(IRODSAccount key, PooledObject<AbstractIRODSMidLevelProtocol> p) throws Exception {
		log.info("disconnecting()");
		p.getObject().shutdown();
		super.destroyObject(key, p);
	}

	@Override
	public boolean validateObject(IRODSAccount key, PooledObject<AbstractIRODSMidLevelProtocol> p) {

		return p.getObject().isConnected();
	}

}
