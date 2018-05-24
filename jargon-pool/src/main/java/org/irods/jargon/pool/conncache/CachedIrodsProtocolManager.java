/**
 *
 */
package org.irods.jargon.pool.conncache;

import org.irods.jargon.core.connection.AbstractIRODSMidLevelProtocol;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSProtocolManager;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.PipelineConfiguration;
import org.irods.jargon.core.exception.AuthenticationException;
import org.irods.jargon.core.exception.JargonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mconway
 *
 */
public class CachedIrodsProtocolManager extends IRODSProtocolManager {

	/**
	 * Expected injected dependency of the {@link JargonConnectionCache} pool
	 */
	private JargonConnectionCache jargonConnectionCache;

	private Logger log = LoggerFactory.getLogger(CachedIrodsProtocolManager.class);

	/**
	 *
	 */
	public CachedIrodsProtocolManager() {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.connection.IRODSProtocolManager#getIRODSProtocol(
	 * org.irods.jargon.core.connection.IRODSAccount,
	 * org.irods.jargon.core.connection.PipelineConfiguration,
	 * org.irods.jargon.core.connection.IRODSSession)
	 */
	@Override
	public AbstractIRODSMidLevelProtocol getIRODSProtocol(final IRODSAccount irodsAccount,
			final PipelineConfiguration pipelineConfiguration, final IRODSSession irodsSession)
			throws AuthenticationException, JargonException {
		log.info("getIRODSProtocol()");

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		if (pipelineConfiguration == null) {
			throw new IllegalArgumentException("null pipelineConfiguration");
		}

		if (irodsSession == null) {
			throw new IllegalArgumentException("null irodsSession");
		}

		log.info("for irodsAccount:{}", irodsAccount);

		try {
			return jargonConnectionCache.borrowObject(irodsAccount);
		} catch (Exception e) {
			log.error("error creating connection", e);
			if (e instanceof AuthenticationException) {
				log.error("authentication exception");
				throw (AuthenticationException) e;
			} else {
				log.error("jargon exception");
				throw new JargonException(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.irods.jargon.core.connection.IRODSProtocolManager#returnIRODSProtocol
	 * (org.irods.jargon.core.connection.AbstractIRODSMidLevelProtocol)
	 */
	@Override
	protected void returnIRODSProtocol(final AbstractIRODSMidLevelProtocol abstractIrodsMidLevelProtocol)
			throws JargonException {
		log.info("returnIRODSProtocol()");
		if (abstractIrodsMidLevelProtocol == null) {
			throw new IllegalArgumentException("null abstractIRODSMidLevelProtocol");
		}

		IRODSAccount irodsAccount = abstractIrodsMidLevelProtocol.getIrodsAccount();
		log.info("irodsAccount being returned:{}", irodsAccount);

		jargonConnectionCache.returnObject(abstractIrodsMidLevelProtocol.getIrodsAccount(),
				abstractIrodsMidLevelProtocol);

	}

	/**
	 * Optional method for any cleanup when shutting down the operation of the
	 * protocol manager. This is useful if the protocol manager is acting as a pool
	 * or cache that must be cleared.
	 *
	 * @throws JargonException
	 */
	@Override
	protected synchronized void destroy() throws JargonException {
		log.info("destroy called, this will terminate the session and clear it");
		getJargonConnectionCache().close();

	}

	/**
	 * Optional method to do any startup when beginning operations of the protocol
	 * manager. This can be useful if the protocol manager is a pool or cache that
	 * must do startup tasks before being used.
	 *
	 * @throws JargonException
	 */
	@Override
	public synchronized void initialize() throws JargonException {
		log.debug("initialize called, does nothing by default");
	}

	/**
	 * @return the jargonConnectionCache
	 */
	public JargonConnectionCache getJargonConnectionCache() {
		return jargonConnectionCache;
	}

	/**
	 * @param jargonConnectionCache
	 *            the jargonConnectionCache to set
	 */
	public void setJargonConnectionCache(final JargonConnectionCache jargonConnectionCache) {
		this.jargonConnectionCache = jargonConnectionCache;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.irods.jargon.core.connection.IRODSProtocolManager#returnWithForce(org
	 * .irods.jargon.core.connection.AbstractIRODSMidLevelProtocol)
	 */
	@Override
	protected void returnWithForce(final AbstractIRODSMidLevelProtocol irodsMidLevelProtocol) {
		log.warn("returning with force, mark as disconnected");
		try {
			getJargonConnectionCache().invalidateObject(irodsMidLevelProtocol.getIrodsAccount(), irodsMidLevelProtocol);
		} catch (Exception e) {
			log.error("exception returning with force, will be eaten", e);
		}

	}

}
