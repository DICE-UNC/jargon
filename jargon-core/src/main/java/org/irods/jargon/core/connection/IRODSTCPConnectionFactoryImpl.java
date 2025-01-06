/**
 *
 */
package org.irods.jargon.core.connection;

import org.irods.jargon.core.exception.JargonException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Implementation of a connection factory for producing the default TCP/IP
 * connection layer.
 *
 * @author Mike Conway - DICE (www.irods.org) see http://code.renci.org for
 *         trackers, access info, and documentation
 *
 */
class IRODSTCPConnectionFactoryImpl extends IRODSConnectionFactory {

	private static final Logger log = LogManager.getLogger(IRODSTCPConnectionFactoryImpl.class);

	@Override
	protected AbstractConnection instance(final IRODSAccount irodsAccount, final IRODSSession irodsSession,
			final IRODSProtocolManager irodsProtocolManager) throws JargonException {

		log.debug("instance()");

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		IRODSBasicTCPConnection connection = new IRODSBasicTCPConnection(irodsAccount,
				irodsSession.buildPipelineConfigurationBasedOnJargonProperties(), irodsProtocolManager, irodsSession);

		return connection;
	}

}
