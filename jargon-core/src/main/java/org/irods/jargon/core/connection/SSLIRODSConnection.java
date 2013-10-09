/**
 * 
 */
package org.irods.jargon.core.connection;

import java.net.Socket;

import org.irods.jargon.core.exception.JargonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a wrapping of a socket inside an IRODSConnection with SSL for use
 * in secure credential exchange
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
final class SSLIRODSConnection extends IRODSConnection {

	private Logger log = LoggerFactory.getLogger(SSLIRODSConnection.class);

	protected SSLIRODSConnection(
			final IRODSConnection underlyingIRODSConnection,
			final Socket underlyingSocket) throws JargonException {
		super(underlyingIRODSConnection.getIrodsAccount(),
				underlyingIRODSConnection.getIrodsProtocolManager(),
				underlyingIRODSConnection.getPipelineConfiguration(),
				underlyingSocket, true);

		// FIXME: set session and connection identifier
	}

	protected void endSSLConnection() throws JargonException {
		log.info("endSSLConnecton()");

	}

}
