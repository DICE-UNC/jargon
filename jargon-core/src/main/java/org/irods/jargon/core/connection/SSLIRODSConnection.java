/**
 * 
 */
package org.irods.jargon.core.connection;

import java.net.Socket;

import org.irods.jargon.core.exception.JargonException;

/**
 * Represents a wrapping of a socket inside an IRODSConnection with SSL for use
 * in secure credential exchange
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
final class SSLIRODSConnection extends IRODSConnection {

	protected SSLIRODSConnection(
			final IRODSConnection underlyingIRODSConnection,
			final Socket underlyingSocket) throws JargonException {
		super(underlyingIRODSConnection.getIrodsAccount(),
				underlyingIRODSConnection.getIrodsProtocolManager(),
				underlyingIRODSConnection.getPipelineConfiguration(),
				underlyingSocket, true);

	}

}
