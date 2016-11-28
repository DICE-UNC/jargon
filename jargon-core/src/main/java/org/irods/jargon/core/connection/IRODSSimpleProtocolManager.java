package org.irods.jargon.core.connection;

import org.irods.jargon.core.exception.AuthenticationException;
import org.irods.jargon.core.exception.JargonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This connection manager simply returns a stand-alone connection to IRODS.
 * based on the information in the IRODSAccount.
 * <p/>
 * This particular implementation has no shared data between connections that
 * are created, and as such does not need to be thread-safe. A pooled connection
 * manager will need to process getting and returning connections from multiple
 * threads.
 * <p/>
 * NOTE: this is somewhat transitional in the way it creates the mid level
 * protocol manager and initializes itself, this will probably remain for
 * backwards compatability and another implementation may be created that allows
 * initialization of all of these factories.
 *
 * @author Mike Conway - DICE
 */
public final class IRODSSimpleProtocolManager extends IRODSProtocolManager {

	private Logger log = LoggerFactory
			.getLogger(IRODSSimpleProtocolManager.class);

	public static IRODSSimpleProtocolManager instance() {
		return new IRODSSimpleProtocolManager();
	}

	public IRODSSimpleProtocolManager() {
		log.info("creating simple protocol manager");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.irods.jargon.core.connection.IRODSProtocolManager#getIRODSProtocol
	 * (org.irods.jargon.core.connection.IRODSAccount,
	 * org.irods.jargon.core.connection.PipelineConfiguration,
	 * org.irods.jargon.core.connection.IRODSSession)
	 */
	@Override
	public AbstractIRODSMidLevelProtocol getIRODSProtocol(
			final IRODSAccount irodsAccount,
			final PipelineConfiguration pipelineConfiguration,
			final IRODSSession irodsSession) throws AuthenticationException,
			JargonException {

		log.debug("creating an IRODSSimpleConnection for account:{}",
				irodsAccount);

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		if (pipelineConfiguration == null) {
			throw new IllegalArgumentException("null pipelineConfiguration");
		}

		if (irodsSession == null) {
			throw new IllegalArgumentException("null irodsSession");
		}

		checkMidLevelProtocolFactory(irodsSession);
		return createNewProtocol(irodsAccount, pipelineConfiguration,
				irodsSession);
	}

	/**
	 * This is an interim fix to initialize the mid level protocol factory
	 *
	 * @throws JargonException
	 */
	private synchronized void checkMidLevelProtocolFactory(
			final IRODSSession irodsSession) throws JargonException {
		if (getIrodsMidLevelProtocolFactory() == null) {
			IRODSConnectionFactory irodsConnectionFactory = getIrodsConnectionFactoryProducingFactory()
					.instance(irodsSession.getJargonProperties());

			setIrodsMidLevelProtocolFactory(new IRODSMidLevelProtocolFactory(
					irodsConnectionFactory, getAuthenticationFactory()));
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
	public void returnIRODSProtocol(
			final AbstractIRODSMidLevelProtocol irodsMidLevelProtocol)
					throws JargonException {
		log.debug("irodsMidLevelProtocol returned:{}", irodsMidLevelProtocol);
		irodsMidLevelProtocol.shutdown();

	}

}
