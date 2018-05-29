/**
 *
 */
package org.irods.jargon.core.connection;

/**
 * Factory that will create authenticated and connected
 * {@code IRODSMidLevelProtocol} implementations.
 *
 * @author Mike Conway - DICE (www.irods.org) see http://code.renci.org for
 *         trackers, access info, and documentation
 *
 */
public class IRODSMidLevelProtocolFactory extends AbstractIRODSMidLevelProtocolFactory {

	/**
	 * Create the factory that will produced connected 'mid level protocol
	 * handlers'. This represents the layer that jargon uses internally to send
	 * different protocol operations and receive protocol responses from iRODS. This
	 * is above the low level networking level, which is produced by the provided
	 * {@code IRODSConnectionFactory}.
	 * <p>
	 * The goal of this factory is to return a live, connected, and authentication
	 * connection to an iRODS agent with the correct version of the mid level
	 * protocol handler, therefore an authentication factory is also required,
	 * allowing a level of plug-ability to the authentication layer.
	 *
	 * @param irodsConnectionFactory
	 *            {@link IRODSConnectionFactory} implementation that can provide a
	 *            low-level networking layer
	 * @param authenticationFactory
	 *            {@link AuthenticationFactory} that can provide authentication
	 *            implementations
	 */
	public IRODSMidLevelProtocolFactory(final IRODSConnectionFactory irodsConnectionFactory,
			final AuthenticationFactory authenticationFactory) {
		super(irodsConnectionFactory, authenticationFactory);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.connection.AbstractIRODSMidLevelProtocolFactory
	 * #createInitialProtocol (org.irods.jargon.core.connection.AbstractConnection,
	 * org.irods.jargon.core.connection.IRODSProtocolManager)
	 */
	@Override
	protected AbstractIRODSMidLevelProtocol createInitialProtocol(final AbstractConnection connection,
			final IRODSProtocolManager irodsProtocolManager) {
		return new IRODSMidLevelProtocol(connection, irodsProtocolManager);
	}

}
