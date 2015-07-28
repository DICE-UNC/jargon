/**
 * 
 */
package org.irods.jargon.core.connection;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.packinstr.SSLStartInp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities to generate SSL connections for PAM and for general TLS support
 * 
 * @author Mike Conway - DICE
 *
 */
class SslConnectionUtilities {

	private final IRODSSession irodsSession;

	private Logger log = LoggerFactory.getLogger(SslConnectionUtilities.class);

	SslConnectionUtilities(final IRODSSession irodsSession) {
		super();
		this.irodsSession = irodsSession;
	}

	/**
	 * @param irodsAccount
	 * @param irodsCommands
	 * @return
	 * @throws JargonException
	 * @throws AssertionError
	 */
	SSLSocket createSslSocketForProtocol(final IRODSAccount irodsAccount,
			final AbstractIRODSMidLevelProtocol irodsCommands)
			throws JargonException, AssertionError {
		// start ssl
		log.info("startSSL for PAM auth");
		SSLStartInp sslStartInp = SSLStartInp.instance();
		irodsCommands.irodsFunction(sslStartInp);

		SSLContext ctx;
		try {
			ctx = SSLContext.getInstance("TLSv1.2", "SunJSSE");
		} catch (NoSuchAlgorithmException e) {
			try {
				ctx = SSLContext.getInstance("TLSv1", "SunJSSE");
			} catch (NoSuchAlgorithmException e1) {
				// The TLS 1.0 provider should always be available.
				throw new AssertionError(e1);
			} catch (NoSuchProviderException e1) {
				throw new AssertionError(e1);
			}
		} catch (NoSuchProviderException e) {
			// The SunJSSE provider should always be available.
			throw new AssertionError(e);
		}
		try {
			ctx.init(null, null, null);
		} catch (KeyManagementException e1) {
			log.error("error initializing ssl context:{}", e1);
			throw new JargonRuntimeException("ssl context init exception", e1);
		}

		// if all went well (no exceptions) then the server is ready for the
		// credential exchange, first grab an SSL enabled connection
		log.debug("getting ssl socket factory");
		SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory
				.getDefault();
		log.debug("supported cyphers:{}",
				sslSocketFactory.getSupportedCipherSuites());

		SSLSocket sslSocket = null;
		try {

			sslSocket = (SSLSocket) sslSocketFactory.createSocket(irodsCommands
					.getIrodsConnection().getConnection(), irodsAccount
					.getHost(), irodsAccount.getPort(), false);
			log.debug("ssl socket created for credential exchage..now connect");
			// Prepare TLS parameters. These have to applied to every TLS
			// socket before the handshake is triggered.
			SSLParameters params = ctx.getDefaultSSLParameters();
			// Do not send an SSL-2.0-compatible Client Hello.
			ArrayList<String> protocols = new ArrayList<String>(
					Arrays.asList(params.getProtocols()));
			protocols.remove("SSLv2Hello");
			params.setProtocols(protocols.toArray(new String[protocols.size()]));
			// Adjust the supported ciphers.
			ArrayList<String> ciphers = new ArrayList<String>(
					Arrays.asList(params.getCipherSuites()));
			ciphers.retainAll(Arrays.asList("TLS_RSA_WITH_AES_128_CBC_SHA256",
					"TLS_RSA_WITH_AES_256_CBC_SHA256",
					"TLS_RSA_WITH_AES_256_CBC_SHA",
					"TLS_RSA_WITH_AES_128_CBC_SHA",
					"SSL_RSA_WITH_3DES_EDE_CBC_SHA",
					"SSL_RSA_WITH_RC4_128_SHA1", "SSL_RSA_WITH_RC4_128_MD5",
					"TLS_EMPTY_RENEGOTIATION_INFO_SCSV"));
			params.setCipherSuites(ciphers.toArray(new String[ciphers.size()]));
			log.debug("supported protocols:{}",
					sslSocket.getSupportedProtocols());

		} catch (IOException e) {
			log.error("ioException creating socket", e);
			throw new JargonException(
					"unable to create the underlying ssl socket", e);
		}

		/*
		 * register a callback for handshaking completion event
		 */
		if (log.isDebugEnabled()) {
			sslSocket
					.addHandshakeCompletedListener(new HandshakeCompletedListener() {
						@Override
						public void handshakeCompleted(
								final HandshakeCompletedEvent event) {
							log.debug("Handshake finished!");
							log.debug("\t CipherSuite:{}",
									event.getCipherSuite());
							log.debug("\t SessionId {}", event.getSession());
							log.debug("\t PeerHost {}", event.getSession()
									.getPeerHost());
						}
					});
		}

		log.debug("starting SSL handshake");
		try {
			sslSocket.setUseClientMode(true);
			sslSocket.startHandshake();
		} catch (IOException e) {
			log.error("ssl exception in handshake", e);
			throw new JargonException("unable to start SSL socket", e);
		}
		log.debug("ssl handshake successful");
		return sslSocket;
	}

}
