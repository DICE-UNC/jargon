package org.irods.jargon.datautils.connection;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool.Config;
import org.irods.jargon.core.connection.AbstractIRODSProtocolManager;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSCommands;
import org.irods.jargon.core.connection.IRODSManagedConnection;
import org.irods.jargon.core.connection.IRODSProtocolManager;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.PipelineConfiguration;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Special variant of the {@link IRODSProtocolMangaer} that caches a temporary
 * password and only returns that one connection. This effectively shares that
 * single connection.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class TempPasswordCachingProtocolManager extends
		AbstractIRODSProtocolManager {

	private final IRODSAccount irodsAccount;
	private final IRODSSession irodsSession;
	private final IRODSProtocolManager baseProtocolManager;
	
	private GenericObjectPool objectPool = null;

	private Logger log = LoggerFactory
			.getLogger(TempPasswordCachingProtocolManager.class);

	/**
	 * Create a protocol manager that will cache a single temporary connection in a pool for reuse.  This is because temp passwords are one-time only.  This allows
	 * client applications to (somewhat) transparently simulate the ability to get a connection on-demand.  This is used in idrop-lite, for example.
	 * @param irodsAccount {@link IRODSAccount} for the underlying cached account
	 * @param irodsSession {@link IRODSSession} that is used to obtain the account
	 * @param baseProtocolManager {@link IRODSProtocolManager} that gets the actual connected account that is subsequently cached
	 * @throws JargonException
	 */
	public TempPasswordCachingProtocolManager(final IRODSAccount irodsAccount, final IRODSSession irodsSession, final IRODSProtocolManager baseProtocolManager)
			throws JargonException {
		super();

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}
		
		if (irodsSession == null) {
			throw new IllegalArgumentException("null irodsSession");
		}
		
		if (baseProtocolManager == null) {
			throw new IllegalArgumentException("null baseProtocolManager");
		}

		this.irodsAccount = irodsAccount;
		this.irodsSession = irodsSession;
		this.baseProtocolManager = baseProtocolManager;

		initialize();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.AbstractIRODSProtocolManager#
	 * getIRODSProtocol(org.irods.jargon.core.connection.IRODSAccount,
	 * org.irods.jargon.core.connection.PipelineConfiguration)
	 */
	@Override
	public IRODSCommands getIRODSProtocol(final IRODSAccount irodsAccount,
			final PipelineConfiguration pipelineConfiguration, final IRODSSession irodsSession)
			throws JargonException {
		try {
			IRODSCommands command = (IRODSCommands) objectPool.borrowObject();
			command.setIrodsProtocolManager(this);
			return command;
		} catch (Exception e) {
			throw new JargonException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.AbstractIRODSProtocolManager#
	 * returnIRODSConnection
	 * (org.irods.jargon.core.connection.IRODSManagedConnection)
	 */
	@Override
	public void returnIRODSConnection(
			final IRODSManagedConnection irodsConnection)
			throws JargonException {
		try {
			objectPool.returnObject(irodsConnection);
		} catch (Exception e) {
			log.error("error returning connection", e);
			throw new JargonException("error returning connection to pool", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.connection.AbstractIRODSProtocolManager#destroy()
	 */
	@Override
	public void destroy() throws JargonException {
		log.info("destroy");
		try {
			objectPool.clear();
			objectPool.close();
		} catch (Exception e) {
			log.error("exception calling close, which closes objectPool", e);
			throw new JargonException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.connection.AbstractIRODSProtocolManager#initialize
	 * ()
	 */
	@Override
	public void initialize() throws JargonException {
		log.info("initialize()");

		if (irodsAccount == null) {
			throw new JargonRuntimeException(
					"null irodsAccount, initialize cannot be called");
		}

		log.info("creating factory for conns");
		ConnectionCreatingPoolableObjectFactory factory = new ConnectionCreatingPoolableObjectFactory(
				irodsAccount, this.irodsSession, this.baseProtocolManager);
		log.info("factory created, setting up config and creating pool");
		GenericObjectPool.Config config = new Config();
		config.maxActive = 1;
		config.maxIdle = 1;
		config.whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_BLOCK;

		objectPool = new GenericObjectPool(factory, config);
		log.info("pool initialized");

	}

	/**
	 * @return the irodsAccount
	 */
	public IRODSAccount getIrodsAccount() {
		return irodsAccount;
	}


}
