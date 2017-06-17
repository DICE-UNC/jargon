package org.irods.jargon.core.connection;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.SSLSocket;

import org.irods.jargon.core.exception.JargonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps a connection to the iRODS server described by the given IRODSAccount.
 * <p>
 * Jargon services do not directly access the <code>IRODSConnection</code>,
 * rather, they use the {@link IRODSMidLevelProtocol IRODSProtocol} interface.
 * <p>
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
	 * <p>
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

			final IRODSProtocolManager irodsProtocolManager,
			final IRODSSession irodsSession) throws JargonException {
		super(irodsAccount, pipelineConfiguration, irodsProtocolManager,
				irodsSession);
	}

	static final Logger log = LoggerFactory
			.getLogger(IRODSBasicTCPConnection.class);

	/**
	 * Default constructor that gives the account and pipeline setup
	 * information. This constructor is a special case where you already have a
	 * Socket opened to iRODS, and you want to wrap that socket with the low
	 * level iRODS semantics. An example use case is when you need to to PAM
	 * authentication and wrap an existing iRODS connection with an SSL socket.
	 * <p>
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
	 * @param IRODSession
	 *            {@link IRODSSession} associated with this connection
	 * @throws JargonException
	 */
	IRODSBasicTCPConnection(final IRODSAccount irodsAccount,
			final PipelineConfiguration pipelineConfiguration,
			final IRODSProtocolManager irodsProtocolManager,
			final Socket socket, final IRODSSession irodsSession)
					throws JargonException {

		super(irodsAccount, pipelineConfiguration, irodsProtocolManager,
				socket, irodsSession);

		setUpSocketAndStreamsAfterConnection(irodsAccount);
		if (socket instanceof SSLSocket) {
			setEncryptionType(EncryptionType.SSL_WRAPPED);
		}
		log.debug("socket opened successfully");
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
		log.debug("connect()");

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		if (connected) {
			log.warn("doing connect when already connected!, will bypass connect and proceed");
			return;
		}

		int attemptCount = 3;

		for (int i = 0; i < attemptCount; i++) {
			log.debug("connecting socket to agent");
			try {

				log.debug("normal iRODS connection");
				connection = new Socket();
				connection.setSoTimeout(getPipelineConfiguration()
						.getIrodsSocketTimeout() * 1000); // time is specified
				// in seconds

				if (getPipelineConfiguration().getPrimaryTcpSendWindowSize() > 0) {
					connection.setSendBufferSize(getPipelineConfiguration()
							.getPrimaryTcpSendWindowSize() * 1024);
				}

				if (getPipelineConfiguration().getPrimaryTcpReceiveWindowSize() > 0) {
					connection.setReceiveBufferSize(getPipelineConfiguration()
							.getPrimaryTcpReceiveWindowSize() * 1024);
				}

				connection.setPerformancePreferences(getPipelineConfiguration()
						.getPrimaryTcpPerformancePrefsConnectionTime(),
						getPipelineConfiguration()
						.getPrimaryTcpPerformancePrefsLatency(),
						getPipelineConfiguration()
						.getPrimaryTcpPerformancePrefsBandwidth());
				InetSocketAddress address = new InetSocketAddress(
						irodsAccount.getHost(), irodsAccount.getPort());
				connection.setKeepAlive(getPipelineConfiguration()
						.isPrimaryTcpKeepAlive());

				// assume reuse, nodelay
				connection.setReuseAddress(true);
				connection.setTcpNoDelay(false);
				connection.connect(address);

				// success, so break out of reconnect loop
				log.debug("connection to socket made...");
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
						Thread.sleep(1000);
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
		log.debug("socket opened successfully");
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
				log.debug("setting a connection timeout of:{} seconds",
						socketTimeout);
				connection.setSoTimeout(socketTimeout * 1000);
			}

			/*
			 * Set raw socket i/o buffering per configuration
			 */
			if (pipelineConfiguration.getInternalInputStreamBufferSize() <= -1) {
				log.debug("no buffer on input stream");
				irodsInputStream = connection.getInputStream();
			} else if (pipelineConfiguration.getInternalInputStreamBufferSize() == 0) {
				log.debug("default buffer on input stream");
				irodsInputStream = new BufferedInputStream(
						connection.getInputStream());
			} else {
				log.debug("buffer of size:{} on input stream",
						pipelineConfiguration
						.getInternalInputStreamBufferSize());
				irodsInputStream = new BufferedInputStream(
						connection.getInputStream(),
						pipelineConfiguration
						.getInternalInputStreamBufferSize());
			}

			if (pipelineConfiguration.getInternalOutputStreamBufferSize() <= -1) {
				log.debug("no buffer on output stream");
				irodsOutputStream = connection.getOutputStream();

			} else if (pipelineConfiguration
					.getInternalOutputStreamBufferSize() == 0) {
				log.debug("default buffer on input stream");
				irodsOutputStream = new BufferedOutputStream(
						connection.getOutputStream());
			} else {
				log.debug("buffer of size:{} on output stream",
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

			log.debug("is connected for : {}", toString());
			try {

				connection.close();

			} catch (Exception e) {
				// ignore
			}
			connected = false;
			log.debug("now disconnected");
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.connection.AbstractConnection#shutdown()
	 */
	@Override
	public void shutdown() throws JargonException {
		log.debug("shutting down connection: {}", connected);
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

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("IRODSBasicTCPConnection []");
		return builder.toString();
	}

}
