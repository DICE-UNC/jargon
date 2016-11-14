/**
 *
 */
package org.irods.jargon.core.connection;

import org.irods.jargon.core.exception.JargonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a connection factory for producing the default TCP/IP
 * connection layer.
 *
 * @author Mike Conway - DICE (www.irods.org) see http://code.renci.org for
 *         trackers, access info, and documentation
 *
 */
class IRODSTCPConnectionFactoryImpl extends IRODSConnectionFactory {

	private static final Logger log = LoggerFactory
			.getLogger(IRODSTCPConnectionFactoryImpl.class);

	@Override
	protected AbstractConnection instance(final IRODSAccount irodsAccount,
			final IRODSSession irodsSession,
			final IRODSProtocolManager irodsProtocolManager)
					throws JargonException {

		log.info("instance()");

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		IRODSBasicTCPConnection connection = new IRODSBasicTCPConnection(
				irodsAccount,
				irodsSession
				.buildPipelineConfigurationBasedOnJargonProperties(),
				irodsProtocolManager, irodsSession);

		return connection;
	}

}
