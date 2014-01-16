package org.irods.jargon.core.connection;

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
	public IRODSMidLevelProtocol getIRODSProtocol(
			final IRODSAccount irodsAccount,
			final PipelineConfiguration pipelineConfiguration,
			final IRODSSession irodsSession) throws JargonException {

		log.debug("creating an IRODSSimpleConnection for account:{}",
				irodsAccount);

		/*
		 * This implementation simply creates a connection to IRODS. This is a
		 * live connection that represents not only an open socket to the IRODS
		 * server, but also a 'connected' connection, meaning that the startup
		 * and handshake activities have been accomplished, leaving the
		 * IRODSProtocol in a ready state. <p/> This method also is the 'hook'
		 * that would allow alternative login methods, such as GSI, to be fully
		 * resolved, and all necessary transformations to the irodsAccount
		 * information will be accomplished when this method returns.
		 */

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
			AbstractIRODSMidLevelProtocol abstractIRODSMidLevelProtocol)
			throws JargonException {
		log.debug("abstractIRODSMidLevelProtocol returned:{}",
				abstractIRODSMidLevelProtocol);
		abstractIRODSMidLevelProtocol.shutdown();

	}

}
