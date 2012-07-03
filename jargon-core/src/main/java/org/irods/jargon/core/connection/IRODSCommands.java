package org.irods.jargon.core.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.channels.ClosedChannelException;

import org.irods.jargon.core.connection.auth.AuthResponse;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.AbstractIRODSPackingInstruction;
import org.irods.jargon.core.packinstr.IRodsPI;
import org.irods.jargon.core.packinstr.RErrMsg;
import org.irods.jargon.core.packinstr.Tag;
import org.irods.jargon.core.protovalues.ErrorEnum;
import org.irods.jargon.core.protovalues.RequestTypes;
import org.irods.jargon.core.utils.IRODSConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulates sending of messages and parsing of responses above the socket
 * read/write level and below the abstract operation level.
 * <p/>
 * Note that the IRODS Connection object that this protocol utilizes is not
 * synchronized. Since a connection manager may also be managing the connection.
 * This <code>IRODSProtocol</code> object manages any necessary synchronization
 * on the connection to the underlying {@link IRODSConnection IRODSConnection}
 * This connection should not be shared between threads. A rule of thumb is to
 * treat a connection to IRODS the same way you would treat a JDBC database
 * connection.
 * <p/>
 * A note on iRODS connections and handling when things go bad. Typically, an
 * iRODS connection is created by opening a socket, and doing a handshake and
 * other start-up procedures. Once that is done you are connected to an iRODS
 * agent. This class is a proxy to that underlying connection, so any calls that
 * result in i/o to the agent are passed to the connection. This insulates the
 * caller from knowledge about the actual networking that goes on, helps protect
 * the integrity of the connection, and also centralizes i/o in case something
 * bad happens on the network level.
 * <p/>
 * There are several cooperating objects involved in obtaining a connection.
 * There is an {@link IRODSSession} object that maintains a ThreadLocal cache of
 * connections by account. Jargon asks for connections from the
 * <code>IRODSSession</code> when you create access objects or files. When you
 * call <code>close()</code> methods, you are actually telling
 * <code>IRODSSession</code> to close the connection on your behalf and remove
 * it from the cache. <code>IRODSSession</code> will tell the
 * {@link IRODSProtocolManager} that you are through with the connection. The
 * actual behavior of the <code>IRODSProtocolManager</code> depends on the
 * implementation. It may just close the connection at that point, or return it
 * to a pool or cache. When the <code>IRODSProtocolManager</code> does decide to
 * actually close a connection, it will call the <code>disconnect</code> method
 * here.
 * <p/>
 * If something bad happens on the network level (IOException like a broken
 * pipe), then it is doubtful that the iRODS disconnect sequence will succeed,
 * or that the connection to the agent is still reliable. In this case, it is
 * the responsibility of the <code>IRODSConnection</code> that is wrapped by
 * this class, to forcefully close the socket connection (without doing the
 * disconnect sequence), and to tell the <code>IRODSSession</code> to remove the
 * connection from the cache. <code>IRODSSession</code> also has a secondary
 * check when it hands out a connection to the caller, to make sure the returned
 * <code>IRODSCommands</code> object is connected. It does this by interrogating
 * the <code>isConnected()</code> method. In the future, or in alternative
 * implementations, an actual ping could be made against the underlying
 * connection, but this is not currently done.
 * <p/>
 * Bottom line, use the <code>IRODSSession</code> close methods. These are
 * exposed in the <code>IRODSFileSystem</code> and
 * <code>IRODSAccesObjectFactory</code> as well. Do not attempt to manipulate
 * the connection using the methods here!
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class IRODSCommands implements IRODSManagedConnection {

	private Logger log = LoggerFactory.getLogger(IRODSCommands.class);
	private final IRODSConnection irodsConnection;
	private IRODSServerProperties irodsServerProperties;
	private IRODSProtocolManager irodsProtocolManager;
	private final PipelineConfiguration pipelineConfiguration;
	private final AuthMechanism authMechanism;

	private String cachedChallengeValue = "";

	/**
	 * note that this field is not final. This is due to the fact that it may be
	 * altered during initialization to resolve GSI info. The field's setter
	 * method is not published, and the <code>IRODSAccount</code> is immutable,
	 * but care should be taken.
	 */
	private IRODSAccount irodsAccount;

	/**
	 * Constructor creates an IRODSCommands object
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} defining the iRODS connection
	 * @param irodsConnectionManager
	 *            {IRODSProtocolManager} that creates new connections
	 * @param pipelineConfiguration
	 *            {@link PipelineConfiguration} that specifies detailed settings
	 *            for the connection (e.g. buffer sizes)
	 * @param authMechanism
	 *            {@link AuthMechanism} that will authenticate with iRODS
	 * @param startUp
	 *            <code>boolean</code> that indicates that authentication should
	 *            be done to iRODS with the provided <code>authMechanism</code>
	 * @return instance of <code>IRODSCommands</code> connected and
	 *         authenticated to an iRODS agent
	 * @throws JargonException
	 */
	private IRODSCommands(final IRODSAccount irodsAccount,
			final IRODSProtocolManager irodsConnectionManager,
			final PipelineConfiguration pipelineConfiguration,
			final AuthMechanism authMechanism, final boolean startUp)
			throws JargonException {
		/*
		 * create the IRODSConnection object. The connection object encapsulates
		 * an open socket to the host/port described by the irodsAccount.
		 */

		if (irodsConnectionManager == null) {
			throw new IllegalArgumentException("irodsConnectionManager is null");
		}

		if (pipelineConfiguration == null) {
			throw new IllegalArgumentException("null pipelineConfiguration");
		}

		this.irodsConnection = IRODSConnection.instance(irodsAccount,
				irodsConnectionManager, pipelineConfiguration);
		this.irodsProtocolManager = irodsConnectionManager;
		this.pipelineConfiguration = pipelineConfiguration;
		this.authMechanism = authMechanism;
		if (startUp) {
			startupConnection(irodsAccount);
		}

	}

	private void startupConnection(final IRODSAccount irodsAccount) throws
	  JargonException {

		AuthResponse authResponse = authMechanism.authenticate(this,
				irodsAccount);
		this.irodsAccount = authResponse.getAuthenticatedIRODSAccount();
		this.cachedChallengeValue = authResponse.getChallengeValue();
	  
	  // set the server properties
	  
	  EnvironmentalInfoAccessor environmentalInfoAccessor = new
	  EnvironmentalInfoAccessor( this); irodsServerProperties =
	  environmentalInfoAccessor .getIRODSServerProperties();
	  log.info(irodsServerProperties.toString()); }

	/**
	 * Instance method used to create an IRODSCommands object
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} defining the iRODS connection
	 * @param irodsConnectionManager
	 *            {IRODSProtocolManager} that creates new connections
	 * @param pipelineConfiguration
	 *            {@link PipelineConfiguration} that specifies detailed settings
	 *            for the connection (e.g. buffer sizes)
	 * @param authMechanism
	 *            {@link AuthMechanism} that will authenticate with iRODS
	 * @return instance of <code>IRODSCommands</code> connected and
	 *         authenticated to an iRODS agent
	 * @throws JargonException
	 */
	static IRODSCommands instance(final IRODSAccount irodsAccount,
			final IRODSProtocolManager irodsConnectionManager,
			final PipelineConfiguration pipelineConfiguration,
			final AuthMechanism authMechanism) throws JargonException {

		return new IRODSCommands(irodsAccount, irodsConnectionManager,
				pipelineConfiguration, authMechanism, true);
	}

	/**
	 * Instance method used to create an IRODSCommands object, note that this
	 * method will not start up (send authentication request) to iRODS, and this
	 * is only useful in specific situations where some sort of
	 * pre-authentication interaction is required.
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} defining the iRODS connection
	 * @param irodsConnectionManager
	 *            {IRODSProtocolManager} that creates new connections
	 * @param pipelineConfiguration
	 *            {@link PipelineConfiguration} that specifies detailed settings
	 *            for the connection (e.g. buffer sizes)
	 * @param authMechanism
	 *            {@link AuthMechanism} that will authenticate with iRODS
	 * @return instance of <code>IRODSCommands</code> connected and
	 *         authenticated to an iRODS agent
	 * @throws JargonException
	 */
	static IRODSCommands instanceWithoutStartup(
			final IRODSAccount irodsAccount,
			final IRODSProtocolManager irodsConnectionManager,
			final PipelineConfiguration pipelineConfiguration,
			final AuthMechanism authMechanism) throws JargonException {

		return new IRODSCommands(irodsAccount, irodsConnectionManager,
				pipelineConfiguration, authMechanism, false);
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
	public synchronized Tag irodsFunction(final String type,
			final String message, final byte[] errorBytes,
			final int errorOffset, final int errorLength, final byte[] bytes,
			final int byteOffset, final int byteStringLength, final int intInfo)
			throws JargonException {

		log.info("calling irods function with byte array");
		log.debug("calling irods function with:{}", message);
		log.debug("api number is:{}", intInfo);

		if (type == null || type.length() == 0) {
			String err = "null or blank type";
			log.error(err);
			throw new JargonException(err);
		}

		// message may be null for some operations

		try {
			int messageLength = 0;

			if (message != null) {
				messageLength = message.getBytes(pipelineConfiguration
						.getDefaultEncoding()).length;
			}

			irodsConnection.send(createHeader(IRODSConstants.RODS_API_REQ,
					messageLength, errorLength, byteStringLength, intInfo));

			irodsConnection.send(message);

			if (byteStringLength > 0) {
				irodsConnection.send(bytes, byteOffset, byteStringLength);
			}

			irodsConnection.flush();

		} catch (UnsupportedEncodingException e) {
			log.error("unsupported encoding", e);
			throw new JargonException(e);
		} catch (IOException e) {
			log.error("ioexception", e);
			throw new JargonException(e);
		}

		return readMessage();
	}

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
			throw new JargonException(e);
		}

	}

	/**
	 * Read from a stream into a byte array. This method will delegate to the
	 * underlying {@link org.irods.jargon.core.connection.IRODSConnection} and
	 * is included in this class to provide a public hook for certain
	 * operations.
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
			throw new JargonException(e);
		}
	}

	/**
	 * Read data from an input stream and write out to a destination
	 * <code>OutputStream</code>. This method will delegate to the underlying
	 * {@link org.irods.jargon.core.connection.IRODSConnection} and is included
	 * in this class to provide a public hook for certain operations.
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
	 * {@link org.irods.jargon.core.connection.IRODSConnection} and is included
	 * in this class to provide a public hook for certain operations.
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
	public void read(final OutputStream destination, final long length,
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
				if (message.parseTag().indexOf("GenQueryOut") == -1) {
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

	private void processMessageInfoLessThanZero(final int messageLength,
			final int errorLength, final int info) throws JargonException {
		log.debug("info is < 0");
		// if nothing else, read the returned bytes and throw them away
		if (messageLength > 0) {
			log.debug("throwing away bytes");
			try {
				irodsConnection.read(new byte[messageLength], 0, messageLength);
			} catch (ClosedChannelException e) {
				log.error("closed channel", e);
				throw new JargonException(e);
			} catch (InterruptedIOException e) {
				log.error("interrupted io", e);
				throw new JargonException(e);
			} catch (IOException e) {
				log.error("io exception", e);
				throw new JargonException(e);
			}
		}

		readAndLogErrorMessage(errorLength, info);

		if (info == ErrorEnum.CAT_SUCCESS_BUT_WITH_NO_INFO.getInt()) {
			// handleSuccessButNoRowsFound(errorLength, info);
			log.info("success but no info returned from irods");
		} else {
			IRODSErrorScanner.inspectAndThrowIfNeeded(info);
		}

	}

	/**
	 * @param errorLength
	 * @param info
	 * @throws JargonException
	 */
	private void readAndLogErrorMessage(final int errorLength, final int info)
			throws JargonException {
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
				throw new JargonException(e);
			}
			if (log.isDebugEnabled()) {
				Tag errorTag;
				try {
					errorTag = Tag.readNextTag(errorMessage,
							pipelineConfiguration.getDefaultEncoding());
				} catch (UnsupportedEncodingException e) {
					log.error("Unsupported encoding for: {}",
							pipelineConfiguration.getDefaultEncoding());
					throw new JargonException("Unsupported encoding for: "
							+ pipelineConfiguration.getDefaultEncoding());
				}
				log.error("IRODS error occured "
						+ errorTag.getTag(RErrMsg.PI_TAG).getTag(
								IRodsPI.MESSAGE_TAG) + " : " + info);
			}
		}
	}

	private void processMessageErrorNotEqualZero(final int errorLength)
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
			e.printStackTrace();
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

	/**
	 * Going to read the header somewhat differently
	 */
	private Tag readHeader() throws JargonException {
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
					irodsConnection.disconnectWithIOException();
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
			e.printStackTrace();
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
			e.printStackTrace();
			throw new JargonException(e);
		} catch (InterruptedIOException e) {
			log.error("interrupted io", e);
			e.printStackTrace();
			throw new JargonException(e);
		} catch (IOException e) {
			log.error("io exception", e);
			e.printStackTrace();
			throw new JargonException(e);
		}
		return org.irods.jargon.core.utils.Host.castToInt(headerInt);
	}

	private Tag readMessageBody(final int length, final boolean decode)
			throws JargonException {
		byte[] body = new byte[length];
		try {
			irodsConnection.read(body, 0, length);
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
			e.printStackTrace();
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.connection.IRODSManagedConnection#getConnectionUri
	 * ()
	 */
	@Override
	public String getConnectionUri() throws JargonException {
		return irodsConnection.getConnectionUri();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.connection.IRODSManagedConnection#isConnected()
	 */
	@Override
	public synchronized boolean isConnected() {
		return irodsConnection.isConnected();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.IRODSManagedConnection#
	 * obliterateConnectionAndDiscardErrors()
	 */
	@Override
	public synchronized void obliterateConnectionAndDiscardErrors() {
		irodsConnection.obliterateConnectionAndDiscardErrors();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.IRODSManagedConnection#shutdown()
	 */
	@Override
	public synchronized void shutdown() throws JargonException {
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
				throw new JargonException(e);
			} catch (InterruptedIOException e) {
				log.error("interrupted io", e);
				throw new JargonException(e);
			} catch (IOException e) {
				log.error("io exception", e);
				throw new JargonException(e);
			} finally {
				irodsConnection.shutdown();
			}

		} else {
			log.warn("disconnect called, but isConnected() is false, this is an unexpected condition that is logged and ignored");
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.IRODSManagedConnection#disconnect()
	 */
	@Override
	public synchronized void disconnect() throws JargonException {
		log.info("closing connection");
		irodsProtocolManager.returnIRODSConnection(this);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.IRODSManagedConnection#
	 * disconnectWithIOException()
	 */
	@Override
	public synchronized void disconnectWithIOException() throws JargonException {
		irodsProtocolManager.returnConnectionWithIoException(irodsConnection);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.connection.IRODSConnection#getIRODSServerProperties
	 * ()
	 */
	public IRODSServerProperties getIRODSServerProperties() {
		return irodsServerProperties;
	}

	@Override
	public IRODSAccount getIrodsAccount() {
		return irodsAccount;
	}

	/**
	 * get the zone associated with this connection. Convenience method
	 * retrieves this information from the returned server properties.
	 * 
	 * @return <code>String</code> with zone name.
	 */
	public String getZone() {
		return irodsServerProperties.getRodsZone();
	}

	/**
	 * Required to close out certain operations, such as parallel transfer
	 * operations.
	 * 
	 * @param status
	 * @throws IOException
	 */
	public void operationComplete(final int status) throws JargonException {
		Tag message = new Tag(AbstractIRODSPackingInstruction.INT_PI,
				new Tag[] { new Tag(AbstractIRODSPackingInstruction.MY_INT,
						status), });
		irodsFunction(IRODSConstants.RODS_API_REQ, message.parseTag(),
				IRODSConstants.OPR_COMPLETE_AN);
	}

	/**
	 * Get the challenge value sent by iRODS at connection startup. This is used
	 * for various obfuscation routines, such as an administrative password
	 * change
	 * 
	 * @return <code>String</code> with the cached challange string sent by
	 *         iRODS at connection startup
	 */
	public String getCachedChallengeValue() {
		return cachedChallengeValue;
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
			throw new JargonException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.connection.IRODSManagedConnection#getIrodsSession()
	 */
	@Override
	public IRODSSession getIrodsSession() {
		return irodsConnection.getIrodsSession();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.connection.IRODSManagedConnection#setIrodsSession
	 * (org.irods.jargon.core.connection.IRODSSession)
	 */
	@Override
	public void setIrodsSession(final IRODSSession irodsSession) {
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
	public IRODSConnection getIrodsConnection() {
		return irodsConnection;
	}

	/**
	 * @return the pipelineConfiguration
	 */
	public PipelineConfiguration getPipelineConfiguration() {
		return pipelineConfiguration;
	}
		
	/**
	 * Respond to client status messages for an operation until exhausted.
	 * 
	 * @param reply
	 *            <code>Tag</code> containing status messages from IRODS
	 * @throws IOException
	 */
	public void processClientStatusMessages(final Tag reply)
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
						sendInNetworkOrder(
								IRODSConstants.SYS_CLI_TO_SVR_COLL_STAT_REPLY);
						ackResult = readMessage();
					}
				}
			}
		}

	}

}
