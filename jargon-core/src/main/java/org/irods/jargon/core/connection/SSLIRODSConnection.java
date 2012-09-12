/**
 * 
 */
package org.irods.jargon.core.connection;

import java.io.IOException;
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

	private final IRODSConnection underlyingIRODSConnection;

	private Logger log = LoggerFactory.getLogger(SSLIRODSConnection.class);

	protected SSLIRODSConnection(IRODSConnection underlyingIRODSConnection,
			Socket underlyingSocket)
			throws JargonException {
		super(underlyingIRODSConnection.getIrodsAccount(),
				underlyingIRODSConnection.getIrodsProtocolManager(),
				underlyingIRODSConnection.getPipelineConfiguration(),
				underlyingSocket, true);
		this.underlyingIRODSConnection = underlyingIRODSConnection;
	}

	protected void endSSLConnection() throws JargonException {
		log.info("endSSLConnecton()");
		try {
			// TODO: do I close, what about the streams? test and see...
			underlyingIRODSConnection.getConnection().close();
		} catch (IOException e) {
			log.error("error closing sslConnection", e);
			this.obliterateConnectionAndDiscardErrors();
			throw new JargonException("exception shutting down SSL processing, connection is abandoned");
		};
	}

}
