package org.irods.jargon.core.connection;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.SSLSocket;

import org.irods.jargon.core.exception.JargonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps a connection to the iRODS server described by the given IRODSAccount.
 * <p/>
 * Jargon services do not directly access the <code>IRODSConnection</code>,
 * rather, they use the {@link IRODSMidLevelProtocol IRODSProtocol} interface.
 * <p/>
 * The connection is confined to one thread, and as such the various methods do
 * not need to be synchronized. All operations pass through the
 * <code>IRODScommands</code> object wrapping this connection, and
 * <code>IRODSCommands</code> does maintain synchronized access to operations
 * that read and write to this connection.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
class IRODSBasicTCPConnection extends AbstractConnection {

	/**
	 * Default constructor that gives the account and pipeline setup
	 * information.
	 * <p/>
	 * This may be updated a bit later when we implement SSL negotiation for
	 * iRODS 4+.
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} that defines the connection
	 * @param pipelineConfiguration
	 *            {@link PipelineConfiguration} that defines the low level
	 *            connection and networking configuration
	 * @param irodsProtocolManager
	 *            {@link irodsProtocolManager} that requested this connection
	 * @throws JargonException
	 */
	IRODSBasicTCPConnection(final IRODSAccount irodsAccount,
			final PipelineConfiguration pipelineConfiguration,
			final IRODSProtocolManager irodsProtocolManager)
			throws JargonException {
		super(irodsAccount, pipelineConfiguration, irodsProtocolManager);
	}

	static final Logger log = LoggerFactory
			.getLogger(IRODSBasicTCPConnection.class);

	/**
	 * Default constructor that gives the account and pipeline setup
	 * information. This constructor is a special case where you already have a
	 * Socket opened to iRODS, and you want to wrap that socket with the low
	 * level iRODS semantics. An example use case is when you need to to PAM
	 * authentication and wrap an existing iRODS connection with an SSL socket.
	 * <p/>
	 * This may be updated a bit later when we implement SSL negotiation for
	 * iRODS 4+.
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} that defines the connection
	 * @param pipelineConfiguration
	 *            {@link PipelineConfiguration} that defines the low level
	 *            connection and networking configuration
	 * @param irodsProtocolManager
	 *            {@link irodsProtocolManager} that requested this connection
	 * @param socket
	 *            {@link Socket} being wrapped in this connection, this allows
	 *            an arbitrary connected socket to be wrapped in low level
	 *            jargon communication semantics.
	 * @throws JargonException
	 */
	IRODSBasicTCPConnection(final IRODSAccount irodsAccount,
			final PipelineConfiguration pipelineConfiguration,
			final IRODSProtocolManager irodsProtocolManager, final Socket socket)
			throws JargonException {

		super(irodsAccount, pipelineConfiguration, irodsProtocolManager);

		if (socket == null) {
			throw new IllegalArgumentException("null socket");
		}

		this.connection = socket;
		setUpSocketAndStreamsAfterConnection(irodsAccount);
		connected = true;

		if (socket instanceof SSLSocket) {
			this.setEncryptionType(EncryptionType.SSL_WRAPPED);
		}

		log.info("socket opened successfully");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.connection.AbstractConnection#connect(org.irods
	 * .jargon.core.connection.IRODSAccount)
	 */
	@Override
	protected void connect(final IRODSAccount irodsAccount)
			throws JargonException {
		log.info("connect()");

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		if (connected) {
			log.warn("doing connect when already connected!, will bypass connect and proceed");
			return;
		}

		int attemptCount = 3;

		for (int i = 0; i < attemptCount; i++) {
			log.info("connecting socket to agent");
			try {

				log.info("normal iRODS connection");
				connection = new Socket(irodsAccount.getHost(),
						irodsAccount.getPort());

				// success, so break out of reconnect loop
				log.info("connection to socket made...");
				break;

			} catch (UnknownHostException e) {
				log.error(
						"exception opening socket to:" + irodsAccount.getHost()
								+ " port:" + irodsAccount.getPort(), e);
				throw new JargonException(e);
			} catch (IOException ioe) {

				if (i < attemptCount - 1) {
					log.error("IOExeption, sleep and attempt a reconnect", ioe);
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						// ignore
					}

				} else {

					log.error(
							"io exception opening socket to:"
									+ irodsAccount.getHost() + " port:"
									+ irodsAccount.getPort(), ioe);
					throw new JargonException(ioe);
				}
			}

		}

		setUpSocketAndStreamsAfterConnection(irodsAccount);
		connected = true;
		log.info("socket opened successfully");
	}

	/**
	 * @param irodsAccount
	 * @throws JargonException
	 */
	void setUpSocketAndStreamsAfterConnection(final IRODSAccount irodsAccount)
			throws JargonException {
		try {

			int socketTimeout = pipelineConfiguration.getIrodsSocketTimeout();
			if (socketTimeout > 0) {
				log.info("setting a connection timeout of:{} seconds",
						socketTimeout);
				connection.setSoTimeout(socketTimeout * 1000);
			}

			/*
			 * Set raw socket i/o buffering per configuration
			 */
			if (pipelineConfiguration.getInternalInputStreamBufferSize() <= -1) {
				log.info("no buffer on input stream");
				irodsInputStream = connection.getInputStream();
			} else if (pipelineConfiguration.getInternalInputStreamBufferSize() == 0) {
				log.info("default buffer on input stream");
				irodsInputStream = new BufferedInputStream(
						connection.getInputStream());
			} else {
				log.info("buffer of size:{} on input stream",
						pipelineConfiguration
								.getInternalInputStreamBufferSize());
				irodsInputStream = new BufferedInputStream(
						connection.getInputStream(),
						pipelineConfiguration
								.getInternalInputStreamBufferSize());
			}

			if (pipelineConfiguration.getInternalOutputStreamBufferSize() <= -1) {
				log.info("no buffer on output stream");
				irodsOutputStream = connection.getOutputStream();

			} else if (pipelineConfiguration
					.getInternalOutputStreamBufferSize() == 0) {
				log.info("default buffer on input stream");
				irodsOutputStream = new BufferedOutputStream(
						connection.getOutputStream());
			} else {
				log.info("buffer of size:{} on output stream",
						pipelineConfiguration
								.getInternalOutputStreamBufferSize());
				irodsOutputStream = new BufferedOutputStream(
						connection.getOutputStream(),
						pipelineConfiguration
								.getInternalOutputStreamBufferSize());
			}

		} catch (UnknownHostException e) {
			log.error("exception opening socket to:" + irodsAccount.getHost()
					+ " port:" + irodsAccount.getPort(), e);
			throw new JargonException(e);
		} catch (IOException ioe) {
			log.error(
					"io exception opening socket to:" + irodsAccount.getHost()
							+ " port:" + irodsAccount.getPort(), ioe);
			throw new JargonException(ioe);
		}
	}

	/**
	 * 
	 */
	void closeDownSocketAndEatAnyExceptions() {
		if (isConnected()) {

			log.info("is connected for : {}", toString());
			try {
				connection.close();

			} catch (Exception e) {
				// ignore
			}
			connected = false;
			log.info("now disconnected");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.AbstractConnection#shutdown()
	 */
	@Override
	public void shutdown() throws JargonException {
		log.info("shutting down connection: {}", connected);
		closeDownSocketAndEatAnyExceptions();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.AbstractConnection#
	 * obliterateConnectionAndDiscardErrors()
	 */
	@Override
	public void obliterateConnectionAndDiscardErrors() {
		closeDownSocketAndEatAnyExceptions();
	}

}
