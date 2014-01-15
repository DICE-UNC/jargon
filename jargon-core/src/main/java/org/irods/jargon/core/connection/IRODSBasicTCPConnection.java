package org.irods.jargon.core.connection;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

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
public class IRODSBasicTCPConnection extends AbstractConnection {

	static final Logger log = LoggerFactory
			.getLogger(IRODSBasicTCPConnection.class);

	/**
	 * Create an instance of a connection (underlying socket and streams) to
	 * iRODS
	 * 
	 * @param irodsAccount
	 * @param irodsConnectionManager
	 * @param pipelineConfiguration
	 * @return
	 * @throws JargonException
	 */
	static IRODSBasicTCPConnection instance(final IRODSAccount irodsAccount,
			final IRODSProtocolManager irodsConnectionManager,
			final PipelineConfiguration pipelineConfiguration)
			throws JargonException {
		IRODSBasicTCPConnection irodsSimpleConnection = new IRODSBasicTCPConnection(
				irodsAccount, irodsConnectionManager, pipelineConfiguration,
				null);
		irodsSimpleConnection.initializeConnection(irodsAccount, null);
		return irodsSimpleConnection;
	}

	/**
	 * Create an instance of a connection (underlying socket and streams) to
	 * iRODS during a reconnect operation.
	 * 
	 * @param irodsAccount
	 * @param irodsConnectionManager
	 * @param pipelineConfiguration
	 * @param irodsSession
	 * @return
	 * @throws JargonException
	 */
	static IRODSBasicTCPConnection instanceWithReconnectInfo(
			final IRODSAccount irodsAccount,
			final IRODSProtocolManager irodsConnectionManager,
			final PipelineConfiguration pipelineConfiguration,
			final StartupResponseData startupResponseData,
			final IRODSSession irodsSession) throws JargonException {

		if (irodsSession == null) {
			throw new IllegalArgumentException(
					"must have reference to the IRODSSession, it is null");
		}

		IRODSBasicTCPConnection irodsSimpleConnection = new IRODSBasicTCPConnection(
				irodsAccount, irodsConnectionManager, pipelineConfiguration,
				startupResponseData);

		irodsSimpleConnection.setIrodsSession(irodsSession);

		irodsSimpleConnection.initializeConnection(irodsAccount,
				startupResponseData);

		log.info("created instance with reconnect info, connect status is:{}",
				irodsSimpleConnection.connected);

		return irodsSimpleConnection;
	}

	/**
	 * Protected constructor
	 * 
	 * @param irodsAccount
	 * @param irodsConnectionManager
	 * @param pipelineConfiguration
	 * @param startupResponseData
	 * @throws JargonException
	 */
	protected IRODSBasicTCPConnection(final IRODSAccount irodsAccount,
			final IRODSProtocolManager irodsConnectionManager,
			final PipelineConfiguration pipelineConfiguration,
			final StartupResponseData startupResponseData)
			throws JargonException {

		super(irodsAccount, pipelineConfiguration);

		if (irodsConnectionManager == null) {
			throw new IllegalArgumentException("null irodsConnectionManager");
		}

		irodsProtocolManager = irodsConnectionManager;

		log.info("pipeline configuration:{}", pipelineConfiguration);

		if (pipelineConfiguration.getInternalCacheBufferSize() > 0) {
			log.info("using internal cache buffer of size:{}",
					pipelineConfiguration.getInternalCacheBufferSize());
			outputBuffer = new byte[pipelineConfiguration
					.getInternalCacheBufferSize()];
		}
	}

	/**
	 * Protected constructor allows specification of a <code>Socket</code> which
	 * will be utilized, and for which input and output streams will be created.
	 * <p/>
	 * In particular, this implementation is utilized to handle operations
	 * against an SSL enabled socket for secure exchange of credentials. In this
	 * case the owner of the provided socket will be responsible for enabling
	 * and disabling SSL, this class will simply take the socket as given.
	 * 
	 * @param irodsAccount
	 * @param irodsConnectionManager
	 * @param pipelineConfiguration
	 * @param startupResponseData
	 * @param providedSocket
	 * @param sslEnabled
	 *            <code>boolean</code> that indicates whether this is an SSL
	 *            enabled socket. This is currently really used to disambiguate
	 *            the constructor signatures.
	 * @throws JargonException
	 */
	protected IRODSBasicTCPConnection(final IRODSAccount irodsAccount,
			final IRODSProtocolManager irodsConnectionManager,
			final PipelineConfiguration pipelineConfiguration,
			final Socket providedSocket, final boolean sslEnabled)
			throws JargonException {

		super(irodsAccount, pipelineConfiguration);

		if (irodsConnectionManager == null) {
			throw new IllegalArgumentException("null irodsConnectionManager");
		}

		if (providedSocket == null) {
			throw new IllegalArgumentException("null providedSocket");
		}

		irodsProtocolManager = irodsConnectionManager;

		log.info("pipeline configuration:{}", pipelineConfiguration);
		connection = providedSocket;
		connected = true;
		setUpSocketAndStreamsAfterConnection(irodsAccount);

		if (pipelineConfiguration.getInternalCacheBufferSize() > 0) {
			log.info("using internal cache buffer of size:{}",
					pipelineConfiguration.getInternalCacheBufferSize());
			outputBuffer = new byte[pipelineConfiguration
					.getInternalCacheBufferSize()];
		}
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

	protected void connect(final IRODSAccount irodsAccount,
			final StartupResponseData startupResponseData)
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
				if (startupResponseData == null) {
					log.info("normal iRODS connection");
					connection = new Socket(irodsAccount.getHost(),
							irodsAccount.getPort());
				} else {
					log.info("restart iRODS connection");
					log.info("reconnect host:{}",
							startupResponseData.getReconnAddr());
					log.info("reconnection port:{}",
							startupResponseData.getReconnPort());
					connection = new Socket(
							startupResponseData.getReconnAddr(),
							startupResponseData.getReconnPort());
				}

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
