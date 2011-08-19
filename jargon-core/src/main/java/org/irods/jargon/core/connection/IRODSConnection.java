package org.irods.jargon.core.connection;

import static org.irods.jargon.core.connection.ConnectionConstants.INT_LENGTH;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.channels.ClosedChannelException;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.utils.Host;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps a connection to the iRODS server described by the given IRODSAccount.
 * <p/>
 * Jargon services do not directly access the <code>IRODSConnection</code>,
 * rather, they use the {@link IRODSCommands IRODSProtocol} interface.
 * <p/>
 * The connection is confined to one thread, and as such the various methods do
 * not need to be synchronized. They do remain so for any possible edge cases
 * and as an extra layer of protection.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
final class IRODSConnection implements IRODSManagedConnection {

	private Logger log = LoggerFactory.getLogger(IRODSConnection.class);
	private final IRODSProtocolManager irodsProtocolManager;
	private String connectionInternalIdentifier;
	private boolean connected = false;
	private Socket connection;
	private InputStream irodsInputStream;
	private OutputStream irodsOutputStream;
	private IRODSSession irodsSession = null;
	private final IRODSAccount irodsAccount;

	static final int DEFAULT_BUFFER_SIZE = 65535;

	/**
	 * 4 bytes at the front of the header, outside XML
	 */
	public static final int HEADER_INT_LENGTH = 4;

	/**
	 * Buffer output to the socket.
	 */
	private byte outputBuffer[] = new byte[DEFAULT_BUFFER_SIZE];

	/**
	 * Holds the offset into the outputBuffer array for adding new data.
	 */
	private int outputOffset = 0;

	static IRODSConnection instance(final IRODSAccount irodsAccount,
			final IRODSProtocolManager irodsConnectionManager)
			throws JargonException {
		IRODSConnection irodsSimpleConnection = new IRODSConnection(
				irodsAccount, irodsConnectionManager);
		irodsSimpleConnection.initializeConnection(irodsAccount);
		return irodsSimpleConnection;
	}

	private void initializeConnection(final IRODSAccount irodsAccount)
			throws JargonException {
		// connect to irods, do handshake
		// save the irods startup information to the IRODSServerProperties
		// object in the irodsConnection

		log.debug("initializing connection with account:{}", irodsAccount);

		if (irodsAccount == null) {
			log.error("no irods account");
			throw new JargonException(
					"no irods account specified, cannot connect");
		}

		if (irodsProtocolManager == null) {
			log.error("null irods connection manager");
			throw new JargonException("null irods connection manager");
		}

		log.info("opening irods socket");

		connect(irodsAccount);

		// build an identifier for this connection, at least for now
		StringBuilder connectionInternalIdentifierBuilder = new StringBuilder();
		connectionInternalIdentifierBuilder.append(getConnectionUri());
		connectionInternalIdentifierBuilder.append('/');
		connectionInternalIdentifierBuilder.append(Thread.currentThread()
				.getName());
		connectionInternalIdentifierBuilder.append('/');
		connectionInternalIdentifierBuilder.append(System.currentTimeMillis());
		this.connectionInternalIdentifier = connectionInternalIdentifierBuilder
				.toString();
	}

	private IRODSConnection() {
		this.irodsProtocolManager = null;
		this.irodsAccount = null;
	}

	private IRODSConnection(final IRODSAccount irodsAccount,
			final IRODSProtocolManager irodsConnectionManager)
			throws JargonException {
		this.irodsProtocolManager = irodsConnectionManager;
		this.irodsAccount = irodsAccount;

	}

	private void connect(final IRODSAccount irodsAccount)
			throws JargonException {
		log.info("connecting socket...");

		if (connected) {
			log.warn("doing connect when already connected!, will bypass connect and proceed");
			return;
		}

		try {
			connection = new Socket(irodsAccount.getHost(),
					irodsAccount.getPort());

			if (getIrodsSession() != null) {
				int socketTimeout = getIrodsSession().getJargonProperties()
						.getIRODSSocketTimeout();
				if (socketTimeout > 0) {
					log.warn("setting a connection timeout of:{} seconds",
							socketTimeout);
					connection.setSoTimeout(socketTimeout * 1000);
				}
			}

			irodsInputStream = new BufferedInputStream(
					connection.getInputStream());
			irodsOutputStream = connection.getOutputStream();

		} catch (UnknownHostException e) {
			log.error("exception opening socket to:" + irodsAccount.getHost()
					+ " port:" + irodsAccount.getPort(), e);
			//disconnectWithIOException();
			throw new JargonException(e);
		} catch (IOException ioe) {
			log.error(
					"io exception opening socket to:" + irodsAccount.getHost()
							+ " port:" + irodsAccount.getPort(), ioe);
			//disconnectWithIOException();
			throw new JargonException(ioe);
		}

		connected = true;

		log.info("socket opened successfully");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.IRODSConnection#disconnect()
	 */
	@Override
	public void disconnect() throws JargonException {
		if (!connection.isConnected()) {
			log.debug("not connected, just bypass");
		}
		log.info("disconnecting...");
		// disconnect from irods and close
		this.irodsProtocolManager.returnIRODSConnection(this);

		log.info("disconnected");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.IRODSManagedConnection#
	 * disconnectWithIOException()
	 */
	@Override
	public void disconnectWithIOException() {

		log.info("disconnecting...");
		// disconnect from irods and close
		this.irodsProtocolManager.returnConnectionWithIoException(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.IRODSManagedConnection#shutdown()
	 */
	@Override
	public void shutdown() throws JargonException {
		log.info("shutting down connection: {}", connected);
		closeDownSocketAndEatAnyExceptions();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.IRODSManagedConnection#
	 * obliterateConnectionAndDiscardErrors()
	 */
	@Override
	public void obliterateConnectionAndDiscardErrors() {
		closeDownSocketAndEatAnyExceptions();
	}

	/**
	 * 
	 */
	private void closeDownSocketAndEatAnyExceptions() {
		if (this.isConnected()) {

			log.info("closing underlying iRODS socket connections, errors will be discarded");
			try {
				connection.shutdownInput();
			} catch (Exception e) {
				// ignore
			}

			try {
				connection.shutdownOutput();
			} catch (Exception e) {
				// ignore
			}

			try {
				connection.close();

			} catch (Exception e) {
				// ignore
			}

			connected = false;
		}
	}

	/**
	 * Get a URI that describes the connection FIXME: implement
	 */
	@Override
	public String getConnectionUri() throws JargonException {
		// eventually build uri from irodsAccount info
		return "irodsSimpleConnection";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.IRODSConnection#isConnected()
	 */
	@Override
	public boolean isConnected() {
		return connected;
	}

	public IRODSProtocolManager getIRODSProtocolManager() {
		return irodsProtocolManager;
	}

	@Override
	public String toString() {
		return this.connectionInternalIdentifier;
	}

	/**
	 * Writes value.length bytes to this output stream.
	 * 
	 * @param value
	 *            value to be sent
	 * @throws NullPointerException
	 *             Send buffer is empty
	 * @throws IOException
	 *             If an IOException occurs
	 */
	void send(final byte[] value) throws IOException {

		try {
			// packing instructions may be null, in which case nothing is sent
			if (value == null) {
				return;
			}

			if (value.length == 0) {
				// nothing to send, warn and ignore
				return;
			}

			if ((value.length + outputOffset) >= DEFAULT_BUFFER_SIZE) {
				// in cases where OUTPUT_BUFFER_LENGTH isn't big enough
				irodsOutputStream.write(outputBuffer, 0, outputOffset);
				irodsOutputStream.write(value);
				outputOffset = 0;
			} else {

				// the message sent isn't longer than OUTPUT_BUFFER_LENGTH
				System.arraycopy(value, 0, outputBuffer, outputOffset,
						value.length);
				outputOffset += value.length;

			}
		} catch (IOException ioe) {
			disconnectWithIOException();
			throw ioe;
		}
	}

	/**
	 * Writes a certain length of bytes at some offset in the value array to the
	 * output stream, by converting the value to a byte array and calling send(
	 * byte[] value ).
	 * 
	 * @param value
	 *            value to be sent
	 * @param offset
	 *            offset into array
	 * @param length
	 *            number of bytes to read
	 * @throws IOException
	 *             If an IOException occurs
	 */
	void send(final byte[] value, final int offset, final int length)
			throws IOException {

		if (value == null) {
			log.error("value cannot be null");
			throw new IllegalArgumentException("value cannot be null");
		}

		if (value.length == 0) {
			// nothing to send, warn and ignore
			log.warn("nothing to send, ignoring...");
			return;
		}

		if (offset > value.length) {
			String err = "trying to send a byte buffer from an offset that is out of range";
			log.error(err);
			throw new IllegalArgumentException(err);
		}

		if (length <= 0) {
			// nothing to send, warn and ignore
			String err = "send length is zero";
			log.error(err);
			throw new IllegalArgumentException(err);
		}

		byte temp[] = new byte[length];

		System.arraycopy(value, offset, temp, 0, length);

		try {
			send(temp);
		} catch (IOException ioe) {
			disconnectWithIOException();
			throw ioe;
		}
	}

	/**
	 * Writes value.length bytes to this output stream.
	 * 
	 * @param value
	 *            value to be sent
	 * @throws IOException
	 *             If an IOException occurs
	 */
	void send(final String value) throws IOException {
		if (value == null) {
			log.debug("null input packing instruction, do not send");
			return;
		}
		try {
			send(value.getBytes(ConnectionConstants.JARGON_CONNECTION_ENCODING)); // FIXME: make encoding a jargon.properties value
		} catch (IOException ioe) {
			disconnectWithIOException();
			throw ioe;
		}

	}

	/**
	 * Writes an int to the output stream as four bytes, network order (high
	 * byte first).
	 * 
	 * @param value
	 *            value to be sent
	 * @throws IOException
	 *             If an IOException occurs
	 */
	void sendInNetworkOrder(final int value) throws IOException {
		byte bytes[] = new byte[INT_LENGTH];

		try {
			Host.copyInt(value, bytes);
			send(bytes);
			flush();
		} catch (IOException ioe) {
			disconnectWithIOException();
			throw ioe;
		}

	}

	/**
	 * Writes an long to the output stream as eight bytes, low byte first.
	 * 
	 * @param value
	 *            value to be sent
	 * @param length
	 *            <code>long</code> with the length of data to send
	 * @param connectionProgressStatusListener
	 *            {link ConnectionProgressStatusListener} or <code>null</code>
	 *            if no listener desired. This listener can then receive
	 *            call-backs of instantaneous byte counts.
	 * @throws IOException
	 *             If an IOException occurs
	 */
	void send(
			final InputStream source,
			long length,
			final ConnectionProgressStatusListener connectionProgressStatusListener)
			throws IOException {
		if (source == null) {
			String err = "value is null";
			log.error(err);
			throw new IllegalArgumentException(err);
		}

		try {
			int lenThisRead = 0;
			byte[] temp = new byte[Math.min(DEFAULT_BUFFER_SIZE, (int) length)];
			while (length > 0) {
				if (temp.length > length) {
					temp = new byte[(int) length];
				}
				lenThisRead = source.read(temp, 0, temp.length);
				length -= lenThisRead;
				send(temp);

				/*
				 * If a listener is specified, send call-backs with progress
				 */
				if (connectionProgressStatusListener != null) {
					connectionProgressStatusListener
							.connectionProgressStatusCallback(ConnectionProgressStatus
									.instanceForSend(lenThisRead));
				}

			}
		} catch (IOException ioe) {
			disconnectWithIOException();
			throw ioe;
		}
	}

	/**
	 * Flushes all data in the output stream and sends it to the server.
	 * 
	 * @throws NullPointerException
	 *             Send buffer empty
	 * @throws IOException
	 *             If an IOException occurs
	 */
	void flush() throws IOException {
		if (connection.isClosed()) {
			// hopefully this isn't too slow to check.
			throw new ClosedChannelException();
		}

		try {
			irodsOutputStream.write(outputBuffer, 0, outputOffset);
		} catch (IOException ioe) {
			disconnectWithIOException();
			throw ioe;
		}
		byte zerByte = (byte) 0;
		java.util.Arrays.fill(outputBuffer, zerByte);

		outputOffset = 0;
	}

	/**
	 * Reads a byte from the server.
	 * 
	 * @throws IOException
	 *             If an IOException occurs
	 */
	byte read() throws JargonException {
		try {
			return (byte) irodsInputStream.read();
		} catch (IOException ioe) {
			log.error("io exception reading", ioe);
			disconnectWithIOException();
			throw new JargonException(ioe);
		}
	}

	int read(final byte[] value) throws JargonException {
		try {
			return read(value, 0, value.length);
		} catch (IOException ioe) {
			log.error("io exception reading", ioe);
			disconnectWithIOException();
			throw new JargonException(ioe);
		}
	}

	/**
	 * read length bytes from the server socket connection and write them to
	 * destination
	 */
	void read(final OutputStream destination, long length) throws IOException {
		read(destination, length, null);
	}

	/**
	 * Read from the iRODS connection for a given length, and write what is read
	 * from iRODS to the give <code>OutputStream</code>.
	 * 
	 * @param destination
	 *            <code>OutputStream</code> to which data will be streamed from
	 *            iRODS.
	 * @param length
	 *            <code>long</code> with the length of data to be read from
	 *            iRODS and pushed to the stream.
	 * @param intraFileStatusListener
	 *            {@link ConnectionProgressStatusListener} that will receive
	 *            progress on the streaming, or <code>null</code> for no such
	 *            call-backs.
	 */
	public void read(OutputStream destination, long length,
			ConnectionProgressStatusListener intraFileStatusListener)
			throws IOException {

		if (destination == null) {
			String err = "destination is null";
			log.error(err);
			throw new IllegalArgumentException(err);
		}

		if (length == 0) {
			String err = "read length is set to zero";
			log.error(err);
			throw new IllegalArgumentException(err);
		}

		try {
			byte[] temp = new byte[Math.min(DEFAULT_BUFFER_SIZE, (int) length)];
			int n = 0;
			while (length > 0) {
				n = read(temp, 0, Math.min(DEFAULT_BUFFER_SIZE, (int) length));
				if (n > 0) {
					length -= n;
					destination.write(temp, 0, n);
					/*
					 * If a listener is specified, send call-backs with progress
					 */
					if (intraFileStatusListener != null) {
						intraFileStatusListener
								.connectionProgressStatusCallback(ConnectionProgressStatus
										.instanceForSend(n));
					}
				} else {
					length = n;
				}
			}
		} catch (IOException ioe) {
			log.error("io exception reading", ioe);
			disconnectWithIOException();
			throw ioe;
		}
	}

	/**
	 * Reads a byte array from the server. Blocks until <code>length</code>
	 * number of bytes are read.
	 * 
	 * @param length
	 *            length of byte array to be read
	 * @return byte[] bytes read from the server
	 * @throws OutOfMemoryError
	 *             Read buffer overflow
	 * @throws ClosedChannelException
	 *             if the connection is closed
	 * @throws NullPointerException
	 *             Read buffer empty
	 * @throws IOException
	 *             If an IOException occurs
	 */
	int read(final byte[] value, final int offset, final int length)
			throws ClosedChannelException, InterruptedIOException, IOException {

		if (value == null) {
			String err = "no data sent";
			log.error(err);
			throw new IllegalArgumentException(err);
		}

		if (log.isDebugEnabled()) {
			log.debug("IRODSConnection.read, byte array size =  {}",
					value.length);
			log.debug("offset = {}", offset);
			log.debug("length = {}", length);
		}

		if (length == 0) {
			String err = "read length is set to zero";
			log.error(err);
			throw new IllegalArgumentException(err);
		}

		int result = 0;
		if (length + offset > value.length) {
			log.error("index out of bounds exception, length + offset larger then byte array");
			throw new IllegalArgumentException(
					"length + offset larger than byte array");
		}

		try {
			int bytesRead = 0;
			while (bytesRead < length) {
				int read = irodsInputStream.read(value, offset + bytesRead,
						length - bytesRead);
				if (read == -1) {
					break;
				}
				bytesRead += read;
			}
			result = bytesRead;

			return result;
		} catch (ClosedChannelException e) {
			log.error("exception reading from socket", e);
			disconnectWithIOException();
			throw e;

		} catch (InterruptedIOException e) {
			log.error("exception reading from socket", e);
			disconnectWithIOException();
			throw e;

		} catch (IOException e) {
			log.error("exception reading from socket", e);
			disconnectWithIOException();
			throw e;
		}
	}

	/**
	 * @return the irodsSession that created this connection
	 */
	@Override
	public IRODSSession getIrodsSession() {
		return irodsSession;
	}

	/**
	 * @param irodsSession
	 *            the irodsSession that created this connection
	 */
	@Override
	public void setIrodsSession(IRODSSession irodsSession) {
		this.irodsSession = irodsSession;
	}

	/**
	 * @return the irodsAccount associated with this connection
	 */
	@Override
	public IRODSAccount getIrodsAccount() {
		return irodsAccount;
	}

}
