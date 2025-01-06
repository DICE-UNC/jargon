package org.irods.jargon.core.connection;

import org.irods.jargon.core.exception.AuthenticationException;
import org.irods.jargon.core.exception.JargonException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This connection manager simply returns a stand-alone connection to IRODS.
 * based on the information in the IRODSAccount.
 * <p>
 * This particular implementation has no shared data between connections that
 * are created, and as such does not need to be thread-safe. A pooled connection
 * manager will need to process getting and returning connections from multiple
 * threads.
 * <p>
 * NOTE: this is somewhat transitional in the way it creates the mid level
 * protocol manager and initializes itself, this will probably remain for
 * backwards compatability and another implementation may be created that allows
 * initialization of all of these factories.
 *
 * @author Mike Conway - DICE
 */
public final class IRODSSimpleProtocolManager extends IRODSProtocolManager {

	private Logger log = LogManager.getLogger(IRODSSimpleProtocolManager.class);

	public static IRODSSimpleProtocolManager instance() {
		return new IRODSSimpleProtocolManager();
	}

	public IRODSSimpleProtocolManager() {
		log.info("creating simple protocol manager");
	}

	@Override
	public IRODSMidLevelProtocol getIRODSProtocol(final IRODSAccount irodsAccount,
			final PipelineConfiguration pipelineConfiguration, final IRODSSession irodsSession)
			throws AuthenticationException, JargonException {

		log.debug("creating an IRODSSimpleConnection for account:{}", irodsAccount);

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
		return createNewProtocol(irodsAccount, pipelineConfiguration, irodsSession);
	}

	/**
	 * This is an interim fix to initialize the mid level protocol factory
	 *
	 * @throws JargonException
	 */
	private synchronized void checkMidLevelProtocolFactory(final IRODSSession irodsSession) throws JargonException {
		if (getIrodsMidLevelProtocolFactory() == null) {
			IRODSConnectionFactory irodsConnectionFactory = getIrodsConnectionFactoryProducingFactory()
					.instance(irodsSession.getJargonProperties());

			setIrodsMidLevelProtocolFactory(
					new IRODSMidLevelProtocolFactory(irodsConnectionFactory, getAuthenticationFactory()));
		}
	}

	@Override
	public void returnIRODSProtocol(final IRODSMidLevelProtocol irodsMidLevelProtocol) throws JargonException {
		log.debug("irodsMidLevelProtocol returned:{}", irodsMidLevelProtocol);
		irodsMidLevelProtocol.shutdown();

	}

}
