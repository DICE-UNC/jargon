package org.irods.jargon.core.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.channels.ClosedChannelException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.irods.jargon.core.connection.auth.AuthResponse;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.packinstr.AbstractIRODSPackingInstruction;
import org.irods.jargon.core.packinstr.IRodsPI;
import org.irods.jargon.core.packinstr.RErrMsg;
import org.irods.jargon.core.packinstr.ReconnMsg;
import org.irods.jargon.core.packinstr.Tag;
import org.irods.jargon.core.protovalues.ErrorEnum;
import org.irods.jargon.core.protovalues.RequestTypes;
import org.irods.jargon.core.utils.IRODSConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractIRODSMidLevelProtocol {

	protected IRODSBasicTCPConnection irodsConnection;
	protected IRODSServerProperties irodsServerProperties;
	protected IRODSProtocolManager irodsProtocolManager;
	protected final PipelineConfiguration pipelineConfiguration;
	protected final AuthMechanism authMechanism;
	protected ExecutorService reconnectService = null;
	protected ReconnectionManager reconnectionManager = null;
	protected Future<Void> reconnectionFuture = null;
	private boolean inRestartMode = false;
	protected boolean closeConnectionOnFinalizer = true;
	protected IRODSSession irodsSession = null;
	public static final int EIRODS_MIN = 301;
	public static final int EIRODS_MAX = 301;

	Logger log = LoggerFactory.getLogger(AbstractIRODSMidLevelProtocol.class);

	/**
	 * authResponse contains
	 */
	protected AuthResponse authResponse = null;
	/**
	 * note that this field is not final. This is due to the fact that it may be
	 * altered during initialization to resolve GSI info.
	 */
	protected IRODSAccount irodsAccount;

	/**
	 * Protected constructor called by subclasses
	 * 
	 * @param authMechanism
	 * @param pipelineConfiguration
	 */
	protected AbstractIRODSMidLevelProtocol(final AuthMechanism authMechanism,
			final PipelineConfiguration pipelineConfiguration) {

		if (authMechanism == null) {
			throw new IllegalArgumentException("null authMechanism");
		}

		if (pipelineConfiguration == null) {
			throw new IllegalArgumentException("null pipelineConfiguration");
		}

		this.authMechanism = authMechanism;
		this.pipelineConfiguration = pipelineConfiguration;

	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("IRODSCommands");
		sb.append("\n   underlying account:");
		sb.append(irodsAccount);
		sb.append("\n   underlying connection:");
		sb.append(irodsConnection);
		return sb.toString();
	}

	/**
	 * Create a typical iRODS function call where no binary data is streamed to
	 * iRODS
	 * 
	 * @param type
	 *            <code>String</code> with the protocol type
	 * @param message
	 *            <code>String</code> with the actual protocol message
	 * @param intInfo
	 *            <code>int</code> with the iRODS api number
	 * @return {@link Tag} with the iRODS protocol response
	 * @throws JargonException
	 */
	public synchronized Tag irodsFunction(final String type,
			final String message, final int intInfo) throws JargonException {
		return irodsFunction(type, message, null, 0, 0, null, 0, 0, intInfo);
	}

	/**
	 * Send the given iROD protocol request with any included binary data, and
	 * return the iRODS response as a <code>Tag</code> object. This method has
	 * detailed parameters, and there are other methods in the class with
	 * simpler signatures that should be used.
	 * 
	 * @param type
	 *            <code>String</code> with the type of request, typically an
	 *            iRODS protocol request
	 * @param message
	 *            <code>String</code> with an XML formatted messag
	 * @param errorBytes
	 *            <code>byte[]</code> with any error data to send to iRODS, can
	 *            be set to <code>null</code>
	 * @param errorOffset
	 *            <code>int</code> with offset into the error data to send
	 * @param errorLength
	 *            <code>int</code> with the length of error data
	 * @param bytes
	 *            <code>byte[]</code> with binary data to send to iRODS.
	 * @param byteOffset
	 *            <code>int</code> with an offset into the byte array to send
	 * @param byteStringLength
	 *            <code>int</code> with the length of the bytes to send
	 * @param intInfo
	 *            <code>int</code> with the iRODS API number
	 * @return
	 * @throws JargonException
	 */
	public abstract Tag irodsFunction(final String type, final String message,
			final byte[] errorBytes, final int errorOffset,
			final int errorLength, final byte[] bytes, final int byteOffset,
			final int byteStringLength, final int intInfo)
			throws JargonException;

	/**
	 * iRODS protocol request that sends data to iRODS using the
	 * <code>OpenedDataObjInp</code> protocol interaction to send binary data in
	 * frames of a given size. Note the frame size is defined in the
	 * jargon.properties as jargon.put.buffer.size. The input stream should have
	 * any buffering wrapped around it before making this call, as this method
	 * will not wrap any buffering around the input stream. It is the
	 * responsibility of the caller of this method to properly close the
	 * <code>inputStream</code> object at the appropriate time.
	 * <p/>
	 * This method is meant to handle the put operation when streaming to iRODS,
	 * this occurs when a parallel operation is overridden in server side
	 * policy, and is not used for typical put operations.
	 * 
	 * 
	 * @param irodsPI
	 *            <code>IRodsPI</code> subclass that is the definition of the
	 *            packing instruction
	 * @param byteStreamLength
	 *            <code>int</code> with the size of the input stream data to be
	 *            sent per frame. The method will make repeated
	 *            <code>OpernedDataObjInp</code> protocol operations, each time
	 *            sending jargon.put.buffer.size buffers.
	 * @param byteStream
	 *            <code>InputStream</code> that has been buffered if required
	 *            before calling this method. The method will not call
	 *            <code>close()</code> on this stream.
	 * @param connectionProgressStatusListener
	 *            {@link ConnectionProgressStatusListener} that can optionally
	 *            processes file progress. Can be set to <code>null</code> if
	 *            not required.
	 * @return <code>long</code> with total bytes sent. Note that this method
	 *         will send the appropriate operation complete messages
	 * @throws JargonException
	 */
	public synchronized long irodsFunctionForStreamingToIRODSInFrames(
			final IRodsPI irodsPI,
			final int byteStreamLength,
			final InputStream byteStream,
			final ConnectionProgressStatusListener connectionProgressStatusListener)
			throws JargonException {

		if (irodsPI == null) {
			throw new IllegalArgumentException("null irodsPI");
		}

		if (byteStream == null) {
			throw new IllegalArgumentException("null byteStream");
		}

		log.info("calling irodsFunctionForStreamingToIRODSInFrames");
		log.debug("calling irods function with:{}", irodsPI);
		log.debug("api number is:{}", irodsPI.getApiNumber());

		long dataSent = 0;

		try {
			int length = 0;
			String message = irodsPI.getParsedTags();
			if (message != null) {
				length = message.getBytes(pipelineConfiguration
						.getDefaultEncoding()).length;
			}
			irodsConnection.send(createHeader(IRODSConstants.RODS_API_REQ,
					length, 0, byteStreamLength, irodsPI.getApiNumber()));
			irodsConnection.send(message);

			if (byteStreamLength > 0) {
				dataSent += irodsConnection.send(byteStream, byteStreamLength,
						connectionProgressStatusListener);
				// do not close stream, it may be sent again in a subsequent
				// call, and will maintain its internal pointer
			}

		} catch (UnsupportedEncodingException e) {
			log.error("unsupported encoding", e);
			throw new JargonException(e);
		} catch (IOException e) {
			log.error("ioexception", e);
			disconnectWithForce();

			throw new JargonException(e);
		}

		log.info("reading message from frame send...");
		log.info("read commented out");
		readMessage();
		log.info("message read");
		return dataSent;
	}

	/**
	 * iRODS protocol request that sends data to iRODS. This method will stream
	 * the entire <code>inputStream</code> data to the given length at one time.
	 * This is used for normal put operations that do not required parallel
	 * transfers.
	 * <p/>
	 * Note that the <code>inputStream</code> object is closed by this method
	 * when completed. Any buffering that should be done on the stream must be
	 * done before passing the stream to this method, as this method does not
	 * wrap the stream with any additional buffering.
	 * 
	 * @param irodsPI
	 *            <code>IRodsPI</code> subclass that is the definition of the
	 *            packing instruction
	 * @param byteStreamLength
	 *            <code>int</code> with the size of the input stream data to be
	 *            sent
	 * @param byteStream
	 *            <code>InputStream</code> that has been buffered if required
	 *            before calling this method. The method will call
	 *            <code>close()</code> on this stream.
	 * @param connectionProgressStatusListener
	 *            {@link ConnectionProgressStatusListener} that can optionally
	 *            processes file progress. Can be set to <code>null</code> if
	 *            not required.
	 * @return <code>long</code> with total bytes sent.
	 * @throws JargonException
	 */
	public synchronized Tag irodsFunctionIncludingAllDataInStream(
			final IRodsPI irodsPI,
			final long byteStreamLength,
			final InputStream byteStream,
			final ConnectionProgressStatusListener connectionProgressStatusListener)
			throws JargonException {

		if (irodsPI == null) {
			throw new IllegalArgumentException("null irodsPI");
		}

		if (byteStream == null) {
			throw new IllegalArgumentException("null byteStream");
		}

		log.info("calling irods function with streams");
		log.debug("calling irods function with:{}", irodsPI);
		log.debug("api number is:{}", irodsPI.getApiNumber());

		try {
			int length = 0;
			String message = irodsPI.getParsedTags();
			if (message != null) {
				length = message.getBytes(pipelineConfiguration
						.getDefaultEncoding()).length;
			}

			log.debug("message:{}", message);

			irodsConnection.send(createHeader(IRODSConstants.RODS_API_REQ,
					length, 0, byteStreamLength, irodsPI.getApiNumber()));
			irodsConnection.send(message);

			if (byteStreamLength > 0) {
				irodsConnection.send(byteStream, byteStreamLength,
						connectionProgressStatusListener);
				byteStream.close();
			} else {
				log.info("no byte stream data, so flush output");
				irodsConnection.flush();
			}

		} catch (UnsupportedEncodingException e) {
			log.error("unsupported encoding", e);
			throw new JargonException(e);
		} catch (IOException e) {
			log.error("ioexception", e);
			disconnectWithForce();

			throw new JargonException(e);
		}

		log.info("data sent, getting response");
		return readMessage();
	}

	/**
	 * Create an iRODS message Tag, including header. Send the bytes of the byte
	 * array, no error stream.
	 */
	public synchronized Tag irodsFunction(final IRodsPI irodsPI,
			final byte[] errorStream, final int errorOffset,
			final int errorLength, final byte[] bytes, final int byteOffset,
			final int byteStreamLength) throws JargonException {

		if (irodsPI == null) {
			String err = "null irodsPI";
			log.error(err);
			throw new IllegalArgumentException(err);
		}

		String out = irodsPI.getParsedTags();

		if (out == null || out.length() == 0) {
			String err = "null or missing message returned from parse";
			log.error(err);
			throw new IllegalArgumentException(err);
		}

		if (log.isDebugEnabled()) {
			log.debug(out);
		}

		try {
			irodsConnection
					.send(createHeader(IRODSConstants.RODS_API_REQ, out
							.getBytes(pipelineConfiguration
									.getDefaultEncoding()).length, errorLength,
							byteStreamLength, irodsPI.getApiNumber()));
			irodsConnection.send(out);

			if (byteStreamLength > 0) {
				irodsConnection.send(bytes, byteOffset, byteStreamLength);
			}

			irodsConnection.flush();
			return readMessage();

		} catch (UnsupportedEncodingException e) {
			log.error("unsupported encoding", e);
			throw new JargonException(e);
		} catch (IOException e) {
			log.error("io exception sending irods command", e);
			disconnectWithForce();

			throw new JargonException(e);
		}

	}

	/**
	 * Read from a stream into a byte array. This method will delegate to the
	 * underlying
	 * {@link org.irods.jargon.core.connection.IRODSBasicTCPConnection} and is
	 * included in this class to provide a public hook for certain operations.
	 * 
	 * @param value
	 *            <code>byte[]</code> that will contain the data read
	 * @param offset
	 *            <code>int</code> offset into target array
	 * @param length
	 *            <code>long</code> length of data to read into array
	 * @return
	 * @throws JargonException
	 */
	public synchronized int read(final byte[] value, final int offset,
			final int length) throws JargonException {

		if (value == null || value.length == 0) {
			throw new JargonException("null or empty value");
		}

		if (offset < 0 || offset > value.length) {
			throw new JargonException("offset out of range");
		}

		if (length <= 0 || length > value.length) {
			throw new JargonException("length out of range");
		}

		try {
			return irodsConnection.read(value, offset, length);
		} catch (UnsupportedEncodingException e) {
			log.error("unsupported encoding", e);
			throw new JargonException(e);
		} catch (IOException e) {
			log.error("io exception sending irods command", e);
			disconnectWithForce();

			throw new JargonException(e);
		}
	}

	/**
	 * Read data from an input stream and write out to a destination
	 * <code>OutputStream</code>. This method will delegate to the underlying
	 * {@link org.irods.jargon.core.connection.IRODSBasicTCPConnection} and is
	 * included in this class to provide a public hook for certain operations.
	 * 
	 * @param destination
	 *            <code>OutputStream</code> for writing data that is read from
	 *            the input stream.
	 * @param length
	 *            <code>long</code> length of data to be read and written out.
	 * @throws JargonException
	 */
	public synchronized void read(final OutputStream destination,
			final long length) throws JargonException {

		read(destination, length, null);
	}

	/**
	 * Read data from an input stream and write out to a destination
	 * <code>OutputStream</code>. This method will delegate to the underlying
	 * {@link org.irods.jargon.core.connection.IRODSBasicTCPConnection} and is
	 * included in this class to provide a public hook for certain operations.
	 * 
	 * @param destination
	 *            <code>OutputStream</code> for writing data that is read from
	 *            the input stream. This stream is not wrapped with a buffer
	 *            here, so a buffer should be provided when calling.
	 * @param length
	 *            <code>long</code> length of data to be read and written out.
	 * @param intraFileStatusListener
	 *            {@link ConnectionProgressStatusListener} or <code>null</code>
	 *            if not utilized, that can receive call-backs of streaming
	 *            progress with a small peformance penalty.
	 * @throws JargonException
	 */
	public synchronized void read(final OutputStream destination,
			final long length,
			final ConnectionProgressStatusListener intraFileStatusListener)
			throws JargonException {

		if (length <= 0) {
			throw new JargonException("length out of range");
		}

		if (destination == null) {
			throw new JargonException("destination is null");
		}

		try {
			irodsConnection.read(destination, length, intraFileStatusListener);
		} catch (UnsupportedEncodingException e) {
			log.error("unsupported encoding", e);
			throw new JargonException(e);
		} catch (IOException e) {
			log.error("io exception sending irods command", e);
			disconnectWithForce();

			throw new JargonException(e);
		}
	}

	/**
	 * Create an iRODS message Tag, including header. This convenience method is
	 * suitable for operations that do not require error or binary streams, and
	 * will set up empty streams for the method call.
	 */
	public synchronized Tag irodsFunction(final IRodsPI irodsPI)
			throws JargonException {

		if (irodsPI == null) {
			String err = "null irodsPI";
			log.error(err);
			throw new IllegalArgumentException(err);
		}

		return irodsFunction(IRODSConstants.RODS_API_REQ,
				irodsPI.getParsedTags(), irodsPI.getApiNumber());
	}

	/**
	 * Create the iRODS header packet
	 */
	public byte[] createHeader(final String type, final int messageLength,
			final int errorLength, final long byteStringLength,
			final int intInfo) throws JargonException {

		log.debug("functionID: {}", intInfo);

		StringBuilder headerBuilder = new StringBuilder();
		headerBuilder.append("<MsgHeader_PI>");
		headerBuilder.append("<type>");
		headerBuilder.append(type);
		headerBuilder.append("</type>");
		headerBuilder.append("<msgLen>");
		headerBuilder.append(messageLength);
		headerBuilder.append("</msgLen>");
		headerBuilder.append("<errorLen>");
		headerBuilder.append(errorLength);
		headerBuilder.append("</errorLen>");
		headerBuilder.append("<bsLen>");
		headerBuilder.append(byteStringLength);
		headerBuilder.append("</bsLen>");
		headerBuilder.append("<intInfo>");
		headerBuilder.append(intInfo);
		headerBuilder.append("</intInfo>");
		headerBuilder.append("</MsgHeader_PI>");

		String header = headerBuilder.toString();

		log.debug("header:{}", header);

		byte[] temp;
		try {
			temp = header.getBytes(pipelineConfiguration.getDefaultEncoding());
		} catch (UnsupportedEncodingException e) {
			throw new JargonException(e);
		}
		byte[] full = new byte[4 + temp.length];
		// load first 4 byte with header length
		org.irods.jargon.core.utils.Host.copyInt(temp.length, full);
		// copy rest of header into full
		System.arraycopy(temp, 0, full, 4, temp.length);
		return full;
	}

	/**
	 * Read a message from iRODS in response to a protocol operation. This
	 * method will decode the response using the configured encoding
	 * 
	 * @return {@link Tag} with the iRODS protocol response
	 * @throws JargonException
	 */
	public synchronized Tag readMessage() throws JargonException {
		return readMessage(true);
	}

	/**
	 * Read a message from iRODS in response to a protocol operation. This
	 * method will decode the response using the configured encoding based on
	 * the <code>decode</code> parameter.
	 * 
	 * @param decode
	 *            <code>boolean</code> that will cause the protocol response to
	 *            be decoded using the given character set if <code>true</code>
	 * @return {@link Tag} with the iRODS protocol response
	 * @throws JargonException
	 */
	public synchronized Tag readMessage(final boolean decode)
			throws JargonException {
		log.info("reading message from irods");
		Tag header = readHeader();
		Tag message = null;

		int messageLength = header.getTags()[1].getIntValue();
		int errorLength = header.getTags()[2].getIntValue();
		int bytesLength = header.getTags()[3].getIntValue();
		int info = header.getTags()[4].getIntValue();

		if (log.isDebugEnabled()) {
			log.debug("message length:{}", messageLength);
			log.debug("error length:{}", errorLength);
			log.debug("bytesLength:{}", bytesLength);
			log.debug("info value:{}", info);
		}

		// Reports iRODS errors, throw exception if appropriate
		if (info < 0) {
			processMessageInfoLessThanZero(messageLength, errorLength, info);
			log.debug("returning null, no results");
			// query with no results
			return null;
		}

		if (messageLength > 0) {
			log.debug("message length greater than zero");
			message = readMessageBody(messageLength, decode);

			// squelch genqueryout data for nicer logs
			if (log.isDebugEnabled()) {
				String messageAsString = message.parseTag();
				if (message.parseTag().indexOf("GenQueryOut") == -1
						|| ConnectionConstants.DUMP_GEN_QUERY_OUT) {
					log.debug("message from IRODS read back:{}",
							messageAsString);
				}
			}
		}

		// previous will have returned or thrown exception

		if (errorLength != 0) {
			processMessageErrorNotEqualZero(errorLength);
		}

		if (bytesLength != 0 || info > 0) {
			log.debug("bytes length is not zero");
			if (message == null) {
				message = new Tag(IRodsPI.MSG_HEADER_PI_TAG);
			}

			message.addTag(header);
		}

		return message;
	}

	public synchronized String getConnectionUri() throws JargonException {
		return irodsConnection.getConnectionUri();
	}

	public synchronized boolean isConnected() {
		return irodsConnection.isConnected();
	}

	public synchronized void shutdown() throws JargonException {

		log.info("shutdown()...check if executor service is running for reconnect");

		checkAndShutdownReconnectService();

		log.info("shutting down, need to send disconnect to irods");
		if (isConnected()) {

			log.info("sending disconnect message");
			try {
				irodsConnection.send(createHeader(
						RequestTypes.RODS_DISCONNECT.getRequestType(), 0, 0, 0,
						0));
				irodsConnection.flush();
			} catch (ClosedChannelException e) {
				log.error("closed channel", e);
				disconnectWithForce();

				throw new JargonException(e);
			} catch (InterruptedIOException e) {
				log.error("interrupted io", e);
				disconnectWithForce();

				throw new JargonException(e);
			} catch (IOException e) {
				log.error("io exception", e);
				disconnectWithForce();

				throw new JargonException(e);
			} finally {
				log.debug("finally, shutdown is being called on the given connection");
				irodsConnection.shutdown();
			}

		} else {
			log.warn("disconnect called, but isConnected() is false, this is an unexpected condition that is logged and ignored");
		}

	}

	/**
	 * Method that will cause the connection to be released, returning it to the
	 * <code>IRODSProtocolManager</code> for actual shutdown or return to a
	 * pool.
	 * <p/>
	 * This method is called for normal close of a connection from a higher
	 * level API method
	 * 
	 * @throws JargonException
	 */
	public synchronized void disconnect() throws JargonException {
		log.info("closing connection");
		irodsProtocolManager.returnIRODSProtocol(this);

	}

	/**
	 * Method that will cause the connection to be released, returning it to the
	 * <code>IRODSProtocolManager</code> for shutdown when something has gone
	 * wrong with the agent or connection, and the connection should not be
	 * re-used.
	 * <p/>
	 * This method is called for abnormal close of a connection from a higher
	 * level API method
	 */
	public synchronized void disconnectWithForce() throws JargonException {
		irodsProtocolManager.returnConnectionWithForce(irodsConnection);

	}

	/**
	 * Get various properties that describe the version and type of server
	 * 
	 * @return {@link IRODSServerProperties}
	 */
	public synchronized IRODSServerProperties getIRODSServerProperties() {
		return irodsServerProperties;
	}

	/**
	 * Get the <code>IRODSAccount</code> that describes the current connection
	 * 
	 * @return
	 */
	public synchronized IRODSAccount getIrodsAccount() {
		return irodsAccount;
	}

	/**
	 * Required to close out certain operations, such as parallel transfer
	 * operations.
	 * 
	 * @param status
	 * @throws IOException
	 */
	public synchronized void operationComplete(final int status)
			throws JargonException {
		Tag message = new Tag(AbstractIRODSPackingInstruction.INT_PI,
				new Tag[] { new Tag(AbstractIRODSPackingInstruction.MY_INT,
						status), });
		irodsFunction(IRODSConstants.RODS_API_REQ, message.parseTag(),
				IRODSConstants.OPR_COMPLETE_AN);
	}

	/**
	 * Used internally to consume status messages from various commands, this
	 * will send a given integer value in network order to iRODS.
	 * 
	 * @param value
	 *            <code>int</code> with
	 * @throws JargonException
	 */
	public synchronized void sendInNetworkOrder(final int value)
			throws JargonException {
		try {
			irodsConnection.sendInNetworkOrder(value);
		} catch (IOException e) {
			disconnectWithForce();
			throw new JargonException(e);
		}
	}

	/**
	 * Get the <code>IRODSSession</code> that was used to obtain this connection
	 * 
	 * @return {@link IRODSSession}
	 */
	public synchronized IRODSSession getIrodsSession() {
		return irodsSession;
	}

	/**
	 * Set the <code>IRODSSession</code> that was used to obtain this connection
	 * 
	 * @return {@link IRODSSession}
	 */
	public synchronized void setIrodsSession(final IRODSSession irodsSession) {
		if (irodsSession == null) {
			throw new IllegalArgumentException("null irodsSession");
		}
		irodsConnection.setIrodsSession(irodsSession);
	}

	/**
	 * @return the irodsProtocolManager
	 */
	public synchronized IRODSProtocolManager getIrodsProtocolManager() {
		return irodsProtocolManager;
	}

	/**
	 * @param irodsProtocolManager
	 *            the irodsProtocolManager to set
	 */
	public synchronized void setIrodsProtocolManager(
			final IRODSProtocolManager irodsProtocolManager) {
		this.irodsProtocolManager = irodsProtocolManager;
	}

	/**
	 * @return the irodsConnection
	 */
	public AbstractConnection getIrodsConnection() {
		return irodsConnection;
	}

	/**
	 * Respond to client status messages for an operation until exhausted.
	 * 
	 * @param reply
	 *            <code>Tag</code> containing status messages from IRODS
	 * @throws IOException
	 */
	public synchronized void processClientStatusMessages(final Tag reply)
			throws JargonException {

		boolean done = false;
		Tag ackResult = reply;

		while (!done) {
			if (ackResult.getLength() > 0) {
				if (ackResult.getName().equals(IRODSConstants.CollOprStat_PI)) {
					// formulate an answer status reply

					// if the total file count is 0, then I will continue and
					// send
					// the coll stat reply, otherwise, just ignore and
					// don't send the reply.

					Tag fileCountTag = ackResult.getTag("filesCnt");
					int fileCount = Integer.parseInt((String) fileCountTag
							.getValue());

					if (fileCount < IRODSConstants.SYS_CLI_TO_SVR_COLL_STAT_SIZE) {
						done = true;
					} else {
						sendInNetworkOrder(IRODSConstants.SYS_CLI_TO_SVR_COLL_STAT_REPLY);
						ackResult = readMessage();
					}
				}
			}
		}

	}

	/**
	 * @return the pipelineConfiguration
	 */
	public synchronized PipelineConfiguration getPipelineConfiguration() {
		return pipelineConfiguration;
	}

	/**
	 * Allows restart attempts to be made.
	 * <p/>
	 * Jargon can emulate the -T connection restart mode of certain iCommands.
	 * Since Jargon holds a persistent connection the semantics may be slightly
	 * different then the one-connection-per-operation icommands. For this
	 * reason, <code>IRODSCommands</code> keeps an <code>inRestartMode</code>
	 * flag. This method allows restarting to be 'turned on' or 'turned off', so
	 * that the default behavior may be to not do connection restarts. Then, the
	 * restart behavior can be turned on by bracketing the operation with the
	 * restart mode.
	 * 
	 * @return the inRestartMode that is <code>true</code> if connection
	 *         restarting is done
	 */
	public synchronized boolean isInRestartMode() {
		return inRestartMode;
	}

	/**
	 * @param inRestartMode
	 *            the inRestartMode to set <code>true</code> if connection
	 *            restarting is done
	 */
	public synchronized void setInRestartMode(final boolean inRestartMode) {
		this.inRestartMode = inRestartMode;
	}

	/**
	 * Checks used by <code>ReconnectionManager</code> together in one protected
	 * block
	 * 
	 * @return <code>boolean</code> of <code>true</code> if the
	 *         <code>ReconnectionManager</code> should call
	 *         <code>reconnect()</code> on this connection. Further checks will
	 *         be made in the actual <code>reconnect()</code> method.
	 */
	protected synchronized boolean isReconnectShouldBeCalled() {
		return isInRestartMode() && isConnected();
	}

	/**
	 * @return the authResponse
	 */
	public AuthResponse getAuthResponse() {
		return authResponse;
	}

	/**
	 * @return the authMechanism
	 */
	protected synchronized AuthMechanism getAuthMechanism() {
		return authMechanism;
	}

	/**
	 * @return the irodsServerProperties
	 */
	synchronized IRODSServerProperties getIrodsServerProperties() {
		return irodsServerProperties;
	}

	/**
	 * @param irodsServerProperties
	 *            the irodsServerProperties to set
	 */
	synchronized void setIrodsServerProperties(
			final IRODSServerProperties irodsServerProperties) {
		this.irodsServerProperties = irodsServerProperties;
	}

	/**
	 * @param irodsAccount
	 *            the irodsAccount to set
	 */
	synchronized void setIrodsAccount(final IRODSAccount irodsAccount) {
		this.irodsAccount = irodsAccount;
	}

	/**
	 * @param authResponse
	 *            the authResponse to set
	 */
	protected synchronized void setAuthResponse(final AuthResponse authResponse) {
		this.authResponse = authResponse;
	}

	/**
	 * Going to read the header somewhat differently
	 */
	Tag readHeader() throws JargonException {
		byte[] header;
		int length = readHeaderLength();
		if (length < 0) {
			log.error("protocol error< header length is:" + length);
			throw new JargonException(
					"Protocol error, read header and got header length less than zero");
		} else if (length > 10000000) {
			/*
			 * Protocol failure: One cause, if running a rule that uses
			 * msiDataObjPut or Get, then when the server requests the Put, the
			 * client instead send a query or other function, the next function
			 * sent to the server (such as the Put itself) will fail and the
			 * server will return a RError_PI message. However this message,
			 * unlike regular server communications, does not have the 4 byte
			 * message length in front of the message. Causing unexpected
			 * results when attempting to parse the message, ie. <REr are
			 * interpreted as the message length.
			 * 
			 * <RError_PI> <count>1 </count> <RErrMsg_PI> <status>-808000
			 * </status> <msg>ERROR: msiDataObjPut: rsDataObjPut failed for
			 * <MsgHeader_PI> <type>RODS_API_REPLY </type> <msgLen>0 </msgLen>
			 * <errorLen>0 </errorLen> <bsLen>0 </bsLen> <intInfo>-808000
			 * </intInfo> </MsgHeader_PI> , status = -808000 </msg>
			 * </RErrMsg_PI> </RError_PI>
			 */

			log.warn("protocol error - length was:{}", length);

			// to recover from some protocol errors, (slowly and if lucky)
			// read until a new message header is found.
			boolean cont = true;
			int protoChar;
			byte[] temp = new byte[13];
			String newHeader = "MsgHeader_PI>";
			// hopefully won't be too many bytes...
			do {
				protoChar = irodsConnection.read();
				if (protoChar == '<') {
					protoChar = irodsConnection.read(temp);
					String headerString;

					try {
						headerString = new String(temp,
								pipelineConfiguration.getDefaultEncoding());
					} catch (UnsupportedEncodingException e) {
						log.error("unsupported encoding");
						throw new JargonException(e);
					}

					if (headerString.equals(newHeader)) {
						temp = new byte[1000];
						// find the end of the header and proceed from there
						for (int i = 0; i < temp.length; i++) {
							temp[i] = irodsConnection.read();
							if (temp[i] == '>' && temp[i - 1] == 'I'
									&& temp[i - 2] == 'P' && temp[i - 3] == '_'
									&& temp[i - 4] == 'r' && temp[i - 5] == 'e'
									&& temp[i - 6] == 'd' && temp[i - 7] == 'a'
									&& temp[i - 8] == 'e' && temp[i - 9] == 'H'
									&& temp[i - 10] == 'g'
									&& temp[i - 11] == 's'
									&& temp[i - 12] == 'M'
									&& temp[i - 13] == '/'
									&& temp[i - 14] == '<') {
								// almost forgot the '\n'
								irodsConnection.read();

								// <MsgHeader_PI> + the above header
								header = new byte[i + 1 + 14];
								System.arraycopy(("<" + newHeader).getBytes(),
										0, header, 0, 14);
								System.arraycopy(temp, 0, header, 14, i + 1);
								try {
									return Tag.readNextTag(header,
											pipelineConfiguration
													.getDefaultEncoding());
								} catch (UnsupportedEncodingException e) {
									log.error("Unsupported encoding for:{}",
											pipelineConfiguration
													.getDefaultEncoding());
									throw new JargonException(
											"Unsupported encoding for:"
													+ pipelineConfiguration
															.getDefaultEncoding());
								}
							}
						}
					}

				} else if (protoChar == -1) {
					irodsConnection.disconnectWithForce();
					throw new JargonException(
							"Server connection lost, due to error");
				}
			} while (cont);

		}

		header = new byte[length];
		try {
			irodsConnection.read(header, 0, length);
		} catch (IOException e) {
			log.error("io exception", e);
			disconnectWithForce();
			throw new JargonException(e);
		}

		try {
			return Tag.readNextTag(header,
					pipelineConfiguration.getDefaultEncoding());
		} catch (UnsupportedEncodingException e) {
			log.error("Unsupported encoding for:{}",
					pipelineConfiguration.getDefaultEncoding());
			throw new JargonException("Unsupported encoding for:"
					+ pipelineConfiguration.getDefaultEncoding());
		}
	}

	int readHeaderLength() throws JargonException {
		byte[] headerInt = new byte[ConnectionConstants.HEADER_INT_LENGTH];
		try {
			irodsConnection.read(headerInt, 0,
					ConnectionConstants.HEADER_INT_LENGTH);
		} catch (ClosedChannelException e) {
			log.error("closed channel", e);
			disconnectWithForce();
			throw new JargonException(e);
		} catch (InterruptedIOException e) {
			log.error("interrupted io", e);
			throw new JargonException(e);
		} catch (IOException e) {
			log.error("io exception", e);
			disconnectWithForce();
			throw new JargonException(e);
		}
		return org.irods.jargon.core.utils.Host.castToInt(headerInt);
	}

	Tag readMessageBody(final int length, final boolean decode)
			throws JargonException {
		byte[] body = new byte[length];
		try {
			irodsConnection.read(body, 0, length);
		} catch (ClosedChannelException e) {
			log.error("closed channel", e);
			disconnectWithForce();
			throw new JargonException(e);
		} catch (InterruptedIOException e) {
			log.error("interrupted io", e);
			disconnectWithForce();
			throw new JargonException(e);
		} catch (IOException e) {
			log.error("io exception", e);
			disconnectWithForce();
			throw new JargonException(e);
		}
		try {
			return Tag.readNextTag(body, decode,
					pipelineConfiguration.getDefaultEncoding());
		} catch (UnsupportedEncodingException e) {
			log.error("Unsupported encoding for:{}",
					pipelineConfiguration.getDefaultEncoding());
			throw new JargonException("Unsupported encoding for:"
					+ pipelineConfiguration.getDefaultEncoding());
		}
	}

	void processMessageErrorNotEqualZero(final int errorLength)
			throws JargonException {
		log.debug("error length is not zero, process error");
		byte[] errorMessage = new byte[errorLength];
		try {
			irodsConnection.read(errorMessage, 0, errorLength);
		} catch (ClosedChannelException e) {
			log.error("closed channel", e);
			e.printStackTrace();
			throw new JargonException(e);
		} catch (InterruptedIOException e) {
			log.error("interrupted io", e);
			e.printStackTrace();
			throw new JargonException(e);
		} catch (IOException e) {
			log.error("io exception", e);
			disconnectWithForce();
			throw new JargonException(e);
		}
		Tag errorTag;
		try {
			errorTag = Tag.readNextTag(errorMessage,
					pipelineConfiguration.getDefaultEncoding());
		} catch (UnsupportedEncodingException e) {
			log.error("Unsupported encoding for:{}",
					pipelineConfiguration.getDefaultEncoding());
			throw new JargonException("Unsupported encoding for:"
					+ pipelineConfiguration.getDefaultEncoding());
		}

		Tag errorPITag = errorTag.getTag(RErrMsg.PI_TAG);
		if (errorPITag == null) {
			throw new JargonException(
					"errorPITag missing when processing an error in response from iRODS");
		}

		Tag status = errorPITag.getTag("status");
		if (status == null) {
			throw new JargonException(
					"no status tag in error PI tag when processing error in response from iRODS");
		}

		int statusVal = status.getIntValue();
		if (statusVal == 0) {
			log.debug("error status of 0 indicates normal operation, ignored");
			return;
		}

		String errorText = errorTag.getTag(RErrMsg.PI_TAG)
				.getTag(IRodsPI.MESSAGE_TAG).getStringValue();

		log.error("IRODS error encountered:{}", errorText);
		log.error("status from error is:{}", statusVal);

		throw new JargonException("error returned from iRODS, status = "
				+ statusVal + " message:" + errorText);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.IRODSManagedConnection#
	 * obliterateConnectionAndDiscardErrors()
	 */
	public synchronized void obliterateConnectionAndDiscardErrors() {
		log.info("shutdown()...check if executor service is running for reconnect");
		checkAndShutdownReconnectService();
		irodsConnection.obliterateConnectionAndDiscardErrors();
	}

	/**
	 * Try and gracefully shut down the reconnect service
	 */
	void checkAndShutdownReconnectService() {
		if (reconnectionFuture != null) {
			log.info("shutting down executor for reconnect...");

			// just a sanity check, having a reconnect service but no reference
			// to the 'future' should never happen.
			if (reconnectionFuture != null) {
				reconnectionFuture.cancel(true);
			} else {
				log.error("I have a reconnect service but no reference to the Future?");
				throw new JargonRuntimeException(
						"invalid state of reconnect service, future task is missing but I have the reonnectService");
			}

			// reconnectService.shutdownNow();

			try {

				if (!reconnectService.awaitTermination(5, TimeUnit.SECONDS)) {
					reconnectService.shutdownNow();
					if (!reconnectService.awaitTermination(1, TimeUnit.SECONDS)) {
						log.error("Pool did not terminate");
					}
				}
			} catch (InterruptedException ie) {
				reconnectService.shutdownNow();
				Thread.currentThread().interrupt();
			}
		}

	}

	/**
	 * Reconnect as would be called by the {@link ReconnectionManager}. This is
	 * only valid if reconnect was set as an option when the startup packet was
	 * sent to iRODS (by specifying restart in the jargon.properties).
	 * <p/>
	 * Note that the methods in <code>IRODSCommands</code> are synchronized,
	 * such that this reconnect call will occur atomically, and only when other
	 * operations are not underway.
	 * <p/>
	 * Note that reconnects are managed by the {@link ReconnectionManager}, and
	 * the <code>boolean</code> value returned here will be <code>true</code> if
	 * the reconnect was made. A <code>false</code> return indicates that the
	 * reconnect did not happen for 'normal' reasons that would indicate the
	 * need to sleep and retry.
	 * <p/>
	 * See lib/core/src/sockComm.c ~ line 390 for the iRODS client analog to
	 * this procedure
	 * 
	 * @return <code>boolean</code> that will be <code>true</code> if reconnect
	 *         works, or <code>false</code> if the reconnect management thread
	 *         should sleep and retry.
	 * 
	 * @throws JargonException
	 */
	protected synchronized boolean reconnect() throws JargonException {
		log.info("reconnect() was called");

		if (authResponse.getStartupResponse().getReconnAddr().isEmpty()) {
			log.error("reconnect was called, but was not specified in the startup pack.  This is some sort of logic error within Jargon!");
			throw new JargonRuntimeException(
					"cannot call reconnect if the connection was not started with reconnect option");
		}

		IRODSBasicTCPConnection reconnectedIRODSConnection;
		try {
			reconnectedIRODSConnection = IRODSBasicTCPConnection
					.instanceWithReconnectInfo(irodsAccount,
							getIrodsProtocolManager(), pipelineConfiguration,
							authResponse.getStartupResponse(),
							getIrodsSession());
		} catch (Exception e) {
			log.debug(
					"exception on open of reconnect socket, will sleep and try again:{}",
					e.getMessage());
			return false; // false indicates no success and should sleep and
							// retry
		}

		// now reconnect with the provided reconnect information gathered at the
		// startup pack send
		ReconnMsg reconnMsg = new ReconnMsg(irodsAccount,
				authResponse.getStartupResponse());
		String reconnData = reconnMsg.getParsedTags();
		try {
			sendReconnMessage(reconnectedIRODSConnection, reconnData);
		} catch (Exception e) {
			log.debug(
					"exception on open of reconnect socket, will sleep and try again:{}",
					e.getMessage());
			reconnectedIRODSConnection.disconnectWithForce();
			return false;
		}

		IRODSMidLevelProtocol restartedCommands = new IRODSMidLevelProtocol(
				irodsAccount, irodsProtocolManager, pipelineConfiguration,
				authResponse, authMechanism, reconnectedIRODSConnection, false);

		try {

			log.info("reading response from startup message..");

			Tag responseMessage = restartedCommands.readMessage();

			if (responseMessage == null) {
				log.debug("null response from restart procedure, will sleep and try again");
				reconnectedIRODSConnection.disconnectWithForce();
				return false;
			}

			log.debug("response:{}", responseMessage);

			int agentStatus = responseMessage.getTag("procState").getIntValue();
			log.debug("proc state for agent:{}", agentStatus);

			if (agentStatus == ReconnectionManager.ProcessingState.RECEIVING_STATE
					.ordinal()) {
				/*
				 * if you get a receiving state from the agent, resend the
				 * message again, per lib/core/src/procApiRequest.c ~ line 155
				 */
				log.debug("agent in receiving state, resend the reconn message");
				sendReconnMessage(reconnectedIRODSConnection, reconnData);
			} else if (agentStatus != ReconnectionManager.ProcessingState.PROCESSING_STATE
					.ordinal()) {
				log.debug("agent is not in receiving state, so sleep and try again");
				reconnectedIRODSConnection.disconnectWithForce();
				return false;
			}

		} catch (Exception e) {
			log.debug(
					"exception on read of reconnect message, will sleep and try again:{}",
					e.getMessage());
			reconnectedIRODSConnection.disconnectWithForce();
			return false;
		}

		log.info("disconnect current connection and switch with reconnected socket");
		irodsConnection.shutdown();
		irodsConnection = reconnectedIRODSConnection;

		log.info("reconnect operation complete... new connection is:{}",
				reconnectedIRODSConnection);
		return true;
	}

	/**
	 * @param reconnectedIRODSConnection
	 * @param reconnData
	 * @throws IOException
	 * @throws JargonException
	 */
	private void sendReconnMessage(
			final AbstractConnection reconnectedIRODSConnection,
			final String reconnData) throws IOException, JargonException {
		log.info("sending reconnect message");
		reconnectedIRODSConnection.send(createHeader(
				RequestTypes.RODS_RECONNECT.getRequestType(),
				reconnData.length(), 0, 0, 0));
		reconnectedIRODSConnection.send(reconnData);
		reconnectedIRODSConnection.flush();
	}

	void processMessageInfoLessThanZero(final int messageLength,
			final int errorLength, final int info) throws JargonException {
		log.debug("info is < 0");
		byte[] messageByte = new byte[messageLength];
		// if nothing else, read the returned bytes and throw them away
		if (messageLength > 0) {
			log.debug("throwing away bytes");
			try {
				irodsConnection.read(new byte[messageLength], 0, messageLength);
				Tag.readNextTag(messageByte,
						pipelineConfiguration.getDefaultEncoding()); // just
																		// thrown
																		// away
																		// for
																		// now
			} catch (ClosedChannelException e) {
				log.error("closed channel", e);
				throw new JargonException(e);
			} catch (InterruptedIOException e) {
				log.error("interrupted io", e);
				throw new JargonException(e);
			} catch (IOException e) {
				log.error("io exception", e);
				disconnectWithForce();

				throw new JargonException(e);
			}
		}

		String addlMessage = readAndLogErrorMessage(errorLength, info);

		if (info == ErrorEnum.CAT_SUCCESS_BUT_WITH_NO_INFO.getInt()) {
			// handleSuccessButNoRowsFound(errorLength, info);
			log.info("success but no info returned from irods");
		} else {
			IRODSErrorScanner.inspectAndThrowIfNeeded(info, addlMessage);
		}

	}

	/**
	 * Look at error message and log it, returning any additional message info
	 * found in the error tag
	 * 
	 * @param errorLength
	 * @param info
	 * @throws JargonException
	 */
	private String readAndLogErrorMessage(final int errorLength, final int info)
			throws JargonException {
		String additionalMessage = "";
		if (errorLength != 0) {
			byte[] errorMessage = new byte[errorLength];
			try {
				irodsConnection.read(errorMessage, 0, errorLength);
			} catch (ClosedChannelException e) {
				log.error("closed channel", e);
				throw new JargonException(e);
			} catch (InterruptedIOException e) {
				log.error("interrupted io", e);
				throw new JargonException(e);
			} catch (IOException e) {
				log.error("io exception", e);
				disconnectWithForce();

				throw new JargonException(e);
			}

			Tag errorTag;

			try {
				errorTag = Tag.readNextTag(errorMessage,
						pipelineConfiguration.getDefaultEncoding());

				if (errorTag != null) {
					log.error("IRODS error occured "
							+ errorTag.getTag(RErrMsg.PI_TAG).getTag(
									IRodsPI.MESSAGE_TAG) + " : " + info);

					additionalMessage = errorTag.getTag(RErrMsg.PI_TAG)
							.getTag(IRodsPI.MESSAGE_TAG).getStringValue();
				}

			} catch (UnsupportedEncodingException e) {
				log.error("Unsupported encoding for: {}",
						pipelineConfiguration.getDefaultEncoding());
				throw new JargonException("Unsupported encoding for: "
						+ pipelineConfiguration.getDefaultEncoding());
			}

		}
		return additionalMessage;

	}

}