package org.irods.jargon.core.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.channels.ClosedChannelException;

import org.bouncycastle.util.encoders.Base64;
import org.irods.jargon.core.connection.AbstractConnection.EncryptionType;
import org.irods.jargon.core.connection.auth.AuthResponse;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.AbstractIRODSPackingInstruction;
import org.irods.jargon.core.packinstr.BinBytesBuff;
import org.irods.jargon.core.packinstr.IRodsPI;
import org.irods.jargon.core.packinstr.RErrMsg;
import org.irods.jargon.core.packinstr.SSLEndInp;
import org.irods.jargon.core.packinstr.Tag;
import org.irods.jargon.core.protovalues.ErrorEnum;
import org.irods.jargon.core.protovalues.RequestTypes;
import org.irods.jargon.core.pub.PluggableApiCallResult;
import org.irods.jargon.core.utils.IRODSConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Encapsulates sending of messages and parsing of responses above the socket
 * read/write level and below the abstract operation level.
 * <p>
 * Note that the IRODS Connection object that this protocol utilizes is not
 * synchronized. Since a connection manager may also be managing the connection.
 * This {@code IRODSProtocol} object manages any necessary synchronization on
 * the connection to the underlying {@link IRODSBasicTCPConnection
 * IRODSConnection} This connection should not be shared between threads. A rule
 * of thumb is to treat a connection to IRODS the same way you would treat a
 * JDBC database connection.
 * <p>
 * A note on iRODS connections and handling when things go bad. Typically, an
 * iRODS connection is created by opening a socket, and doing a handshake and
 * other start-up procedures. Once that is done you are connected to an iRODS
 * agent. This class is a proxy to that underlying connection, so any calls that
 * result in i/o to the agent are passed to the connection. This insulates the
 * caller from knowledge about the actual networking that goes on, helps protect
 * the integrity of the connection, and also centralizes i/o in case something
 * bad happens on the network level.
 * <p>
 * There are several cooperating objects involved in obtaining a connection.
 * There is an {@link IRODSSession} object that maintains a ThreadLocal cache of
 * connections by account. Jargon asks for connections from the
 * {@code IRODSSession} when you create access objects or files. When you call
 * {@code close()} methods, you are actually telling {@code IRODSSession} to
 * close the connection on your behalf and remove it from the cache.
 * {@code IRODSSession} will tell the {@link IRODSProtocolManager} that you are
 * through with the connection. The actual behavior of the
 * {@code IRODSProtocolManager} depends on the implementation. It may just close
 * the connection at that point, or return it to a pool or cache. When the
 * {@code IRODSProtocolManager} does decide to actually close a connection, it
 * will call the {@code disconnect} method here.
 * <p>
 * If something bad happens on the network level (IOException like a broken
 * pipe), then it is doubtful that the iRODS disconnect sequence will succeed,
 * or that the connection to the agent is still reliable. In this case, it is
 * the responsibility of the {@code IRODSConnection} that is wrapped by this
 * class, to forcefully close the socket connection (without doing the
 * disconnect sequence), and to tell the {@code IRODSSession} to remove the
 * connection from the cache. {@code IRODSSession} also has a secondary check
 * when it hands out a connection to the caller, to make sure the returned
 * {@code IRODSCommands} object is connected. It does this by interrogating the
 * {@code isConnected()} method. In the future, or in alternative
 * implementations, an actual ping could be made against the underlying
 * connection, but this is not currently done.
 * <p>
 * Bottom line, use the {@code IRODSSession} close methods. These are exposed in
 * the {@code IRODSFileSystem} and {@code IRODSAccesObjectFactory} as well. Do
 * not attempt to manipulate the connection using the methods here!
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class IRODSMidLevelProtocol {

	Logger log = LogManager.getLogger(IRODSMidLevelProtocol.class);

	private AbstractConnection irodsConnection;
	private AbstractConnection irodsConnectionNonEncryptedRef = null;
	private IRODSProtocolManager irodsProtocolManager;
	private IRODSServerProperties irodsServerProperties;
	private IRODSSession irodsSession = null;
	private StartupResponseData startupResponseData;

	/**
	 * This is an overhead for iRODS 4.0 - 4.0.3 servers per
	 * https://github.com/DICE-UNC/jargon/issues/70
	 *
	 */
	private boolean forceSslFlush = false;

	public static final int EIRODS_MIN = 301;
	public static final int EIRODS_MAX = 301;

	/**
	 * authResponse contains information about the authentication phase, including
	 * the account used to authenticate, and the actual account represented by the
	 * connection. These may be two different things, for example, in PAM, one can
	 * authenticate as a PAM user, but actually connect as standard IRODS
	 * authentication using a temporary password generated in the PAM authentication
	 * process.
	 */
	private AuthResponse authResponse = null;

	/**
	 * This account will represent the account information used for the actual
	 * connection, as specified when the user originally logged in. This is broken
	 * down into the account presented, and the account actually used in the
	 * {@code AuthResponse} object.
	 */
	private IRODSAccount irodsAccount;

	/**
	 * Create a base instance of the mid level protocol, which may be processed
	 * through multiple phases before being ready for use.
	 * <p>
	 * The life cycle of this connection is mediated by the
	 * {@code AbstractIRODSMidLevelProtocolFactory} implemenation used, and these
	 * connections should be obtained from that factory, typically through the
	 * {@code IRODSProtocolManager} implementation that has been selected.
	 *
	 * @param irodsConnection      {@link AbstractConnection} that repreents the low
	 *                             level networking connection to the iRODS agent
	 * @param irodsProtocolManager {@link IRODSProtocolManager} that was the source
	 *                             of this connection, and to which it will be
	 *                             returned when disconnecting.
	 */

	protected IRODSMidLevelProtocol(final AbstractConnection irodsConnection,
			final IRODSProtocolManager irodsProtocolManager) {
		if (irodsConnection == null) {
			throw new IllegalArgumentException("null irodsConnection");
		}

		if (irodsProtocolManager == null) {
			throw new IllegalArgumentException("null irodsProtocolManager");
		}

		this.irodsConnection = irodsConnection;
		this.irodsProtocolManager = irodsProtocolManager;
		irodsSession = irodsConnection.getIrodsSession();
	}

	synchronized boolean isForceSslFlush() {
		return forceSslFlush;
	}

	synchronized void setForceSslFlush(final boolean forceSslFlush) {
		this.forceSslFlush = forceSslFlush;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		/*
		 * Check if a still-connected agent connection is being finalized, and nag in
		 * the log, then try and disconnect
		 */
		if (getIrodsConnection().isConnected()) {
			log.error("**************************************************************************************");
			log.error("********  WARNING: POTENTIAL CONNECTION LEAK  ******************");
			log.error(
					"********  finalizer has run and found a connection left opened, please check your code to ensure that all connections are closed");
			log.error("********  IRODSCommands is:{}", this);
			log.error("********  connection is:{}, will attempt to disconnect and shut down any restart thread",
					getIrodsConnection().getConnectionInternalIdentifier());
			log.error("**************************************************************************************");
			obliterateConnectionAndDiscardErrors();
		}
		super.finalize();
	}

	synchronized void closeOutSocketAndSetAsDisconnected() throws IOException {
		getIrodsConnection().getConnection().close();
		getIrodsConnection().setConnected(false);
	}

	/**
	 * Check server version and see if I need extra flushes for SSL processing (for
	 * PAM). This is needed for PAM pre iRODS 3.3.
	 *
	 * @return {@code boolean} indicating this is a PAM flush
	 */
	boolean isPamFlush() {

		if (getPipelineConfiguration().isForcePamFlush()) { // pam flush
			return true;
		} else {

			IrodsVersion currentVersion = new IrodsVersion(getStartupResponseData().getRelVersion());
			if (currentVersion.getMajor() == 4 && currentVersion.getMinor() == 0) {
				log.warn(
						"using the pam flush behavior because of iRODS 4.0.X-ness - see https://github.com/DICE-UNC/jargon/issues/70");
				return true;
			} else {
				return false;
			}
		}

	}

	/**
	 * Call an iRODS pluggable api endpoint with the pre-serialized json and return
	 * the json as a string value to be marshaled by the wrapper function.
	 * 
	 * @return {@link PluggableApiCallResult} with the string-ified JSON response,
	 *         marshal and unmarshal are external to this method
	 */
	public synchronized PluggableApiCallResult irodsPluggableApiFunction(String inputJson, int apiNumber)
			throws JargonException {

		// BytesBuf_T
		// see
		// https://github.com/irods/irods/blob/master/unit_tests/src/test_get_file_descriptor_info.cpp#L47
		// https://github.com/irods/irods/blob/master/plugins/api/src/get_file_descriptor_info.cpp#L358-L383
		log.info("irodsPluggableApiFunction()");

		log.debug("apiNumber is:{}", apiNumber);

		if (inputJson == null || inputJson.length() == 0) {
			String err = "null or blank inputJson";
			log.error(err);
			throw new JargonException(err);
		}

		byte[] encodedInput = Base64.encode(inputJson.getBytes());

		BinBytesBuff bytesBuff = BinBytesBuff.instance(new String(encodedInput), apiNumber);
		String tagOut = bytesBuff.getParsedTags();

		// message may be null for some operations

		try {

			int messageLength = tagOut.getBytes(this.getEncoding()).length;

			sendHeader(IRODSConstants.RODS_API_REQ, messageLength, 0, 0, apiNumber);

			if (getStartupResponseData() == null) {
				log.debug("no ssl flush checking during negotiation");
			} else if (isPamFlush()) {
				log.debug("doing extra pam flush for iRODS 3.2");
				getIrodsConnection().flush();
			}

			getIrodsConnection().send(tagOut);
			getIrodsConnection().flush();

		} catch (UnsupportedEncodingException e) {
			log.error("unsupported encoding", e);
			throw new JargonException(e);
		} catch (IOException e) {
			disconnectWithForce();
			throw new JargonException(e);
		}

		return readPluggableApiMessage();

	}

	/**
	 * Read a message from iRODS in response to a pluggable api operation, which
	 * should be a JSON string
	 *
	 * @return {@link PluggableApiCallResult} with the iRODS JSON response
	 * @throws JargonException on iRODS error
	 */
	public synchronized PluggableApiCallResult readPluggableApiMessage() throws JargonException {
		log.debug("readPluggableApiMessage()");
		Tag header = readHeader();
		Tag message = null;
		String jsonOutput = null;

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

		PluggableApiCallResult pluggableApiResult = new PluggableApiCallResult();
		pluggableApiResult.setErrorInfo(errorLength);
		pluggableApiResult.setIntInfo(info);

		if (messageLength > 0) {
			log.debug("message length greater than zero");
			message = readMessageBody(messageLength, true);
			String messageBytes = message.getTag("buf").getStringValue();
			byte[] decoded = Base64.decode(messageBytes);
			pluggableApiResult.setJsonResult(new String(decoded));
		}

		if (errorLength != 0) {
			processMessageErrorNotEqualZero(errorLength);
		}

		if (bytesLength > 0) {
			throw new UnsupportedOperationException("unable to handle bytes buffer from pluggable api call");
		}

		// look for the tag with the actual encoded message (tag is buf in a
		// BinBytesBuf_PI tag

		// parse out the response

		return pluggableApiResult;

	}

	/**
	 * Read the actual JSON message body in response to a pluggable API request
	 * 
	 * @param length {@code int} with message length
	 * @return {@code String} with the JSON string response for later marshaling
	 * @throws JargonException {@link JargonException}
	 */
	String readJsonMessageBody(final int length) throws JargonException {
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
			return new String(body, this.getEncoding());
		} catch (UnsupportedEncodingException e) {
			log.error("Unsupported encoding for:{}", getEncoding());
			throw new JargonException("Unsupported encoding for:" + getEncoding());
		}
	}

	private String handlePluggableApiError(int errorLength) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Send the given iROD protocol request with any included binary data, and
	 * return the iRODS response as a {@code Tag} object. This method has detailed
	 * parameters, and there are other methods in the class with simpler signatures
	 * that should be used.
	 *
	 * @param type             {@code String} with the type of request, typically an
	 *                         iRODS protocol request
	 * @param message          {@code String} with an XML formatted messag
	 * @param errorBytes       {@code byte[]} with any error data to send to iRODS,
	 *                         can be set to {@code null}
	 * @param errorOffset      {@code int} with offset into the error data to send
	 * @param errorLength      {@code int} with the length of error data
	 * @param bytes            {@code byte[]} with binary data to send to iRODS.
	 * @param byteOffset       {@code int} with an offset into the byte array to
	 *                         send
	 * @param byteBufferLength {@code int} with the length of the bytes to send
	 * @param intInfo          {@code int} with the iRODS API number
	 * @return {@link Tag}
	 * @throws JargonException for iRODS error
	 */
	public synchronized Tag irodsFunction(final String type, final String message, final byte[] errorBytes,
			final int errorOffset, final int errorLength, final byte[] bytes, final int byteOffset,
			final int byteBufferLength, final int intInfo) throws JargonException {

		log.debug("calling irods function with byte array");

		if (intInfo != 1201) {
			log.debug("calling irods function with:{}", message);
		}

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
				messageLength = message.getBytes(getEncoding()).length;
			}

			sendHeader(type, messageLength, errorLength, byteBufferLength, intInfo);

			if (getStartupResponseData() == null) {
				log.debug("no ssl flush checking during negotiation");
			} else if (isPamFlush()) {
				log.debug("doing extra pam flush for iRODS 3.2");
				getIrodsConnection().flush();
			}

			getIrodsConnection().send(message);
			getIrodsConnection().flush();

			if (byteBufferLength > 0) {
				getIrodsConnection().send(bytes, byteOffset, byteBufferLength);
			}

			getIrodsConnection().flush();

		} catch (UnsupportedEncodingException e) {
			log.error("unsupported encoding", e);
			throw new JargonException(e);
		} catch (IOException e) {
			disconnectWithForce();
			throw new JargonException(e);
		}

		// This branch exists for situations where Jargon may receive information
		// via the RError stack. Error codes in the RError stack are purely advisory
		// and MUST NOT be treated as an error.
		//
 		// rcExecMyRule (i.e. 625) is the only known API to deliver the wanted information
		// via the RError stack.
		if (625 == intInfo) {
			return readMessageWithRErrorStack(true);
		}

		// APIs which take this code path will always ignore the RError stack information.
		return readMessage();
	}

	/**
	 * Send the given iROD protocol request with any included binary data, and
	 * return the iRODS response as a {@code Tag} object. This method has detailed
	 * parameters, and there are other methods in the class with simpler signatures
	 * that should be used.
	 *
	 * @param type             {@code String} with the type of request, typically an
	 *                         iRODS protocol request
	 * @param message          {@code String} with an XML formatted messag
	 * @param errorBytes       {@code byte[]} with any error data to send to iRODS,
	 *                         can be set to {@code null}
	 * @param errorOffset      {@code int} with offset into the error data to send
	 * @param errorLength      {@code int} with the length of error data
	 * @param bytes            {@code byte[]} with binary data to send to iRODS.
	 * @param byteOffset       {@code int} with an offset into the byte array to
	 *                         send
	 * @param byteBufferLength {@code int} with the length of the bytes to send
	 * @param intInfo          {@code int} with the iRODS API number
	 * @throws JargonException for iRODS error
	 */
	public synchronized void irodsFunctionUnidirectional(final String type, final byte[] message,
			final byte[] errorBytes, final int errorOffset, final int errorLength, final byte[] bytes,
			final int byteOffset, final int byteBufferLength, final int intInfo) throws JargonException {

		log.debug("calling irods function with byte array");
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
				messageLength = message.length;
			}

			sendHeader(type, messageLength, errorLength, byteBufferLength, intInfo);

			if (getStartupResponseData() == null) {
				log.debug("no pam flush check during negotiation phase");
			} else if (isPamFlush()) {
				log.debug("doing extra pam flush for iRODS 3.2");
				getIrodsConnection().flush();
			}

			if (messageLength > 0) {
				getIrodsConnection().send(message);
				getIrodsConnection().flush();
			}

			if (byteBufferLength > 0) {
				getIrodsConnection().send(bytes, byteOffset, byteBufferLength);
			}

			getIrodsConnection().flush();

		} catch (UnsupportedEncodingException e) {
			log.error("unsupported encoding", e);
			throw new JargonException(e);
		} catch (IOException e) {
			disconnectWithForce();
			throw new JargonException(e);
		}

	}

	/**
	 * Send an iRODS protocol header
	 * 
	 * @param type             {@code String} with type of header
	 * @param messageLength    {@code int} with length of the message
	 * @param errorLength      {@code int} with the lenght of any error content
	 * @param byteStringLength {@code long} with the byte lenght of the message
	 * @param intInfo          {@link int} with any info value to send
	 * @throws JargonException {@link JargonException}
	 * @throws IOException     {@link IOException}
	 */
	public void sendHeader(final String type, final int messageLength, final int errorLength,
			final long byteStringLength, final int intInfo) throws JargonException, IOException {

		byte[] header = createHeader(type, messageLength, errorLength, byteStringLength, intInfo);

		int len = header.length;

		getIrodsConnection().sendInNetworkOrder(len);
		getIrodsConnection().send(header);

	}

	/**
	 * Hook for any action to take before disconnecting (e.g. SSL shutdown)
	 *
	 * @throws JargonException {@link JargonException}
	 */
	void preDisconnectAction() throws JargonException {
		log.debug("preDisconnectAction()");

		if (getIrodsConnection().getEncryptionType() == EncryptionType.SSL_WRAPPED) {
			log.debug("sending SSL shutdown if ssl conn");
			SSLEndInp sslEndInp = SSLEndInp.instance();
			irodsFunction(sslEndInp);
		}
	}

	/**
	 * Create a typical iRODS function call where no binary data is streamed to
	 * iRODS
	 *
	 * @param type    {@code String} with the protocol type
	 * @param message {@code String} with the actual protocol message
	 * @param intInfo {@code int} with the iRODS api number
	 * @return {@link Tag} with the iRODS protocol response
	 * @throws JargonException on invocation of the function
	 */
	public synchronized Tag irodsFunction(final String type, final String message, final int intInfo)
			throws JargonException {
		return irodsFunction(type, message, null, 0, 0, null, 0, 0, intInfo);
	}

	/**
	 * iRODS protocol request that sends data to iRODS using the
	 * {@code OpenedDataObjInp} protocol interaction to send binary data in frames
	 * of a given size. Note the frame size is defined in the jargon.properties as
	 * jargon.put.buffer.size. The input stream should have any buffering wrapped
	 * around it before making this call, as this method will not wrap any buffering
	 * around the input stream. It is the responsibility of the caller of this
	 * method to properly close the {@code inputStream} object at the appropriate
	 * time.
	 * <p>
	 * This method is meant to handle the put operation when streaming to iRODS,
	 * this occurs when a parallel operation is overridden in server side policy,
	 * and is not used for typical put operations.
	 *
	 * @param irodsPI                          {@code IRodsPI} subclass that is the
	 *                                         definition of the packing instruction
	 * @param byteStreamLength                 {@code int} with the size of the
	 *                                         input stream data to be sent per
	 *                                         frame. The method will make repeated
	 *                                         {@code OpernedDataObjInp} protocol
	 *                                         operations, each time sending
	 *                                         jargon.put.buffer.size buffers.
	 * @param byteStream                       {@code InputStream} that has been
	 *                                         buffered if required before calling
	 *                                         this method. The method will not call
	 *                                         {@code close()} on this stream.
	 * @param connectionProgressStatusListener {@link ConnectionProgressStatusListener}
	 *                                         that can optionally processes file
	 *                                         progress. Can be set to {@code null}
	 *                                         if not required.
	 * @return {@code long} with total bytes sent. Note that this method will send
	 *         the appropriate operation complete messages
	 * @throws JargonException on function error
	 */
	public synchronized long irodsFunctionForStreamingToIRODSInFrames(final IRodsPI irodsPI, final int byteStreamLength,
			final InputStream byteStream, final ConnectionProgressStatusListener connectionProgressStatusListener)
			throws JargonException {

		if (irodsPI == null) {
			throw new IllegalArgumentException("null irodsPI");
		}

		if (byteStream == null) {
			throw new IllegalArgumentException("null byteStream");
		}

		log.debug("calling irodsFunctionForStreamingToIRODSInFrames");
		log.debug("calling irods function with:{}", irodsPI);
		log.debug("api number is:{}", irodsPI.getApiNumber());

		long dataSent = 0;

		try {
			int length = 0;
			String message = irodsPI.getParsedTags();
			if (message != null) {
				length = message.getBytes(irodsConnection.getPipelineConfiguration().getDefaultEncoding()).length;
			}
			sendHeader(IRODSConstants.RODS_API_REQ, length, 0, byteStreamLength, irodsPI.getApiNumber());
			irodsConnection.send(message);

			if (byteStreamLength > 0) {
				dataSent += irodsConnection.send(byteStream, byteStreamLength, connectionProgressStatusListener);
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

		log.debug("reading message from frame send...");
		log.debug("read commented out");
		readMessage();
		log.debug("message read");
		return dataSent;
	}

	/**
	 * the {@link StartupResponseData} from the send of the initial iRODS startup
	 * packet is provisisioned by the authentication mechanism.
	 *
	 * @return {@link StartupResponseData} as obtained when sending the startup pack
	 */
	public StartupResponseData getStartupResponseData() {
		return startupResponseData;
	}

	public void setStartupResponseData(final StartupResponseData startupResponseData) {
		this.startupResponseData = startupResponseData;
	}

	/**
	 * iRODS protocol request that sends data to iRODS. This method will stream the
	 * entire {@code inputStream} data to the given length at one time. This is used
	 * for normal put operations that do not required parallel transfers.
	 * <p>
	 * Note that the {@code inputStream} object is closed by this method when
	 * completed. Any buffering that should be done on the stream must be done
	 * before passing the stream to this method, as this method does not wrap the
	 * stream with any additional buffering.
	 *
	 * @param irodsPI                          {@code IRodsPI} subclass that is the
	 *                                         definition of the packing instruction
	 * @param byteStreamLength                 {@code int} with the size of the
	 *                                         input stream data to be sent
	 * @param byteStream                       {@code InputStream} that has been
	 *                                         buffered if required before calling
	 *                                         this method. The method will call
	 *                                         {@code close()} on this stream.
	 * @param connectionProgressStatusListener {@link ConnectionProgressStatusListener}
	 *                                         that can optionally processes file
	 *                                         progress. Can be set to {@code null}
	 *                                         if not required.
	 * @return {@code long} with total bytes sent.
	 * @throws JargonException for iRODS errors
	 */
	public synchronized Tag irodsFunctionIncludingAllDataInStream(final IRodsPI irodsPI, final long byteStreamLength,
			final InputStream byteStream, final ConnectionProgressStatusListener connectionProgressStatusListener)
			throws JargonException {

		if (irodsPI == null) {
			throw new IllegalArgumentException("null irodsPI");
		}

		if (byteStream == null) {
			throw new IllegalArgumentException("null byteStream");
		}

		log.debug("calling irods function with streams");
		log.debug("calling irods function with:{}", irodsPI);
		log.debug("api number is:{}", irodsPI.getApiNumber());

		try {
			int length = 0;
			String message = irodsPI.getParsedTags();
			if (message != null) {
				length = message.getBytes(getEncoding()).length;
			}

			log.debug("message:{}", message);

			sendHeader(IRODSConstants.RODS_API_REQ, length, 0, byteStreamLength, irodsPI.getApiNumber());
			irodsConnection.send(message);

			if (byteStreamLength > 0) {
				irodsConnection.send(byteStream, byteStreamLength, connectionProgressStatusListener);
				byteStream.close();
			} else {
				log.debug("no byte stream data, so flush output");
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

		log.debug("data sent, getting response");
		return readMessage();
	}

	/**
	 * Create an iRODS message Tag, including header. Send the bytes of the byte
	 * array, no error stream.
	 */

	/**
	 * Create an iRODS message Tag, including header. Send the bytes of the byte
	 * array, no error stream.
	 *
	 * @param irodsPI          {@link IRodsPI} with the packing instruction
	 * @param errorStream      {@code byte[]} with the error stream data
	 * @param errorOffset      {@code byte[]} with the offset that should be used to
	 *                         send from the error stream
	 * @param errorLength      {@code int} with the length of the error stream to
	 *                         send
	 * @param bytes            {@code byte[]} with the binary data to send
	 * @param byteOffset       {@code int} with the offset into the data bytes
	 * @param byteStreamLength {@code int} with the length of data from the byte
	 *                         stream to send
	 * @return {@link Tag} with the iRODS response
	 * @throws JargonException on iRODS error
	 */

	public synchronized Tag irodsFunction(final IRodsPI irodsPI, final byte[] errorStream, final int errorOffset,
			final int errorLength, final byte[] bytes, final int byteOffset, final int byteStreamLength)
			throws JargonException {

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
			sendHeader(IRODSConstants.RODS_API_REQ, out.getBytes(getEncoding()).length, errorLength, byteStreamLength,
					irodsPI.getApiNumber());
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
	 * underlying {@link org.irods.jargon.core.connection.IRODSBasicTCPConnection}
	 * and is included in this class to provide a public hook for certain
	 * operations.
	 *
	 * @param value  {@code byte[]} that will contain the data read
	 * @param offset {@code int} offset into target array
	 * @param length {@code long} length of data to read into array
	 * @return {@code int} with the number of bytes read
	 * @throws JargonException for iRODS error
	 */
	public synchronized int read(final byte[] value, final int offset, final int length) throws JargonException {

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
	 * {@code OutputStream}. This method will delegate to the underlying
	 * {@link org.irods.jargon.core.connection.IRODSBasicTCPConnection} and is
	 * included in this class to provide a public hook for certain operations.
	 *
	 * @param destination {@code OutputStream} for writing data that is read from
	 *                    the input stream.
	 * @param length      {@code long} length of data to be read and written out.
	 * @throws JargonException indicating iRODS error
	 */
	public synchronized void read(final OutputStream destination, final long length) throws JargonException {

		read(destination, length, null);
	}

	/**
	 * Read data from an input stream and write out to a destination
	 * {@code OutputStream}. This method will delegate to the underlying
	 * {@link org.irods.jargon.core.connection.IRODSBasicTCPConnection} and is
	 * included in this class to provide a public hook for certain operations.
	 *
	 * @param destination             {@code OutputStream} for writing data that is
	 *                                read from the input stream. This stream is not
	 *                                wrapped with a buffer here, so a buffer should
	 *                                be provided when calling.
	 * @param length                  {@code long} length of data to be read and
	 *                                written out.
	 * @param intraFileStatusListener {@link ConnectionProgressStatusListener} or
	 *                                {@code null} if not utilized, that can receive
	 *                                call-backs of streaming progress with a small
	 *                                peformance penalty.
	 * @throws JargonException for iRODS error
	 */
	public synchronized void read(final OutputStream destination, final long length,
			final ConnectionProgressStatusListener intraFileStatusListener) throws JargonException {

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
	 * suitable for operations that do not require error or binary streams, and will
	 * set up empty streams for the method call.
	 *
	 * @param irodsPI {@link IRodsPI} with the packing instruction to execute
	 * @return {@link Tag} with the result of the call
	 * @throws JargonException for iRODS error
	 */
	public synchronized Tag irodsFunction(final IRodsPI irodsPI) throws JargonException {

		if (irodsPI == null) {
			String err = "null irodsPI";
			log.error(err);
			throw new IllegalArgumentException(err);
		}

		return irodsFunction(IRODSConstants.RODS_API_REQ, irodsPI.getParsedTags(), irodsPI.getApiNumber());
	}

	/**
	 * Create an iRODS message Tag, including header. This convenience method is
	 * suitable for operations that do not require error or binary streams, and will
	 * set up empty streams for the method call.
	 *
	 * @param irodsPI {@link IRodsPI} with the packing instruction to execute
	 * @return {@link Tag} with the result of the call
	 * @throws JargonException for iRODS error
	 */
	public synchronized PluggableApiCallResult irodsFunctionWithPluggableResult(final IRodsPI irodsPI)
			throws JargonException {

		// BytesBuf_T
		// see
		// https://github.com/irods/irods/blob/master/unit_tests/src/test_get_file_descriptor_info.cpp#L47
		// https://github.com/irods/irods/blob/master/plugins/api/src/get_file_descriptor_info.cpp#L358-L383
		log.info("irodsFunctionWithPluggableResult()");

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
			sendHeader(IRODSConstants.RODS_API_REQ, out.getBytes(getEncoding()).length, 0, 0, irodsPI.getApiNumber());
			irodsConnection.send(out);
			irodsConnection.flush();
		} catch (IOException e) {
			log.error("");
		}
		return readPluggableApiMessage();
	}

	/**
	 * Create an iRODS message Tag, including header, for negotiation requests. This
	 * convenience method is suitable for operations that do not require error or
	 * binary streams, and will set up empty streams for the method call.
	 */

	/**
	 *
	 * @param irodsPI {@link IRodsPI} with the packing instruction to execute
	 * @return {@link Tag} with the result of the call
	 * @throws JargonException for an iRODS error
	 */
	public synchronized Tag irodsFunctionForNegotiation(final IRodsPI irodsPI) throws JargonException {

		if (irodsPI == null) {
			String err = "null irodsPI";
			log.error(err);
			throw new IllegalArgumentException(err);
		}

		return irodsFunction(IRODSConstants.RODS_NEG_REQ, irodsPI.getParsedTags(), irodsPI.getApiNumber());
	}

	/**
	 * Create an iRODS header packet
	 *
	 * @param type             {@code String} with the message type
	 * @param messageLength    {@code int} with the length of the message being sent
	 * @param errorLength      {@code int} with the length of any error data
	 * @param byteStreamLength {@code long} with the length of the byte stream
	 * @param intInfo          {@code int} with any info values
	 * @return {@code byte[]} for with the formatted header
	 * @throws JargonException for iRODS error
	 */
	public byte[] createHeader(final String type, final int messageLength, final int errorLength,
			final long byteStreamLength, final int intInfo) throws JargonException {

		return createHeaderBytesFromData(type, messageLength, errorLength, byteStreamLength, intInfo, getEncoding());
	}

	/**
	 * Create a message header to send to irods
	 *
	 * @param type             {@code String} with the header type
	 * @param messageLength    {@code long} length of the message to send
	 * @param errorLength      {@code long} length of error data
	 * @param byteStreamLength {@code long} with the length of any binary bytes to
	 *                         send
	 * @param intInfo          {@code int} with any info value
	 * @param encoding         {@link String} with the encoding
	 * @return {@code byte[]} with the header data
	 * @throws JargonException on iRODS error
	 */
	public static byte[] createHeaderBytesFromData(final String type, final int messageLength, final int errorLength,
			final long byteStreamLength, final int intInfo, final String encoding) throws JargonException {

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
		headerBuilder.append(byteStreamLength);
		headerBuilder.append("</bsLen>");
		headerBuilder.append("<intInfo>");
		headerBuilder.append(intInfo);
		headerBuilder.append("</intInfo>");
		headerBuilder.append("</MsgHeader_PI>");

		String header = headerBuilder.toString();

		byte[] temp;
		try {
			temp = header.getBytes(encoding);
			return temp;
		} catch (UnsupportedEncodingException e) {
			throw new JargonException(e);
		}
		// FIXME: issue #4
		/*
		 * byte[] full = new byte[4 + temp.length]; // load first 4 byte with header
		 * length org.irods.jargon.core.utils.Host.copyInt(temp.length, full); // copy
		 * rest of header into full System.arraycopy(temp, 0, full, 4, temp.length);
		 * return full;
		 */

	}

	/**
	 * Read a message from iRODS in response to a protocol operation. This method
	 * will decode the response using the configured encoding
	 *
	 * @return {@link Tag} with the iRODS protocol response
	 * @throws JargonException on iRODS error
	 */
	public synchronized Tag readMessage() throws JargonException {
		return readMessage(true);
	}

	public synchronized Tag readMessageWithRErrorStack(final boolean decode) throws JargonException {
		log.debug("reading message from irods");
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
				int idx = messageAsString.indexOf("GenQueryOut");
				if (idx == -1 || ConnectionConstants.DUMP_GEN_QUERY_OUT) {
					log.debug("message from IRODS read back:{}", messageAsString);
				}
			}
		}
		// previous will have returned or thrown exception

		if (errorLength != 0) {
			return processMessageRErrorStack(errorLength);
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

	/**
	 * Read a message from iRODS in response to a protocol operation. This method
	 * will decode the response using the configured encoding based on the
	 * {@code decode} parameter.
	 *
	 * @param decode {@code boolean} that will cause the protocol response to be
	 *               decoded using the given character set if {@code true}
	 * @return {@link Tag} with the iRODS protocol response
	 * @throws JargonException on iRODS error
	 */
	public synchronized Tag readMessage(final boolean decode) throws JargonException {
		log.debug("reading message from irods");
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
				int idx = messageAsString.indexOf("GenQueryOut");
				if (idx == -1 || ConnectionConstants.DUMP_GEN_QUERY_OUT) {
					log.debug("message from IRODS read back:{}", messageAsString);
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

	public synchronized boolean isConnected() {
		return irodsConnection.isConnected();
	}

	/**
	 * <b>If you are a client, you should not call this!</b> Shutdown hook used by
	 * the {@link IRODSProtocolManager} to cause the actual connection to be
	 * terminated normally by sending a disconnect method.
	 * <p>
	 * Clients should use the methods in {@link IRODSSession} to obtain and return
	 * connections, and let the IRODSSession work with the configured
	 * {@link IRODSProtocolManager} to enforce the connection life-cycle.
	 * <p>
	 * This method may be called by pooling implementations that are pruning
	 * connections in a pool rather than returning them to the pool.
	 *
	 * @throws JargonException on iRODS error
	 */
	public synchronized void shutdown() throws JargonException {
		log.debug("shutting down, need to send disconnect to irods");
		if (isConnected()) {

			preDisconnectAction();

			log.debug("sending disconnect message");
			try {
				sendHeader(RequestTypes.RODS_DISCONNECT.getRequestType(), 0, 0, 0, 0);
				irodsConnection.flush();
				log.debug("finally, shutdown is being called on the given connection");
				irodsConnection.shutdown();
				if (getIrodsConnectionNonEncryptedRef() != null) {
					getIrodsConnectionNonEncryptedRef().shutdown();
				}
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

		} else {
			log.warn(
					"disconnect called, but isConnected() is false, this is an unexpected condition that is logged and ignored");
		}

	}

	/**
	 * Method that will cause the connection to be released, returning it to the
	 * {@code IRODSProtocolManager} for actual shutdown or return to a pool.
	 * <p>
	 * This method is called for normal close of a connection from a higher level
	 * API method, and typically is not used by clients of the API. The exception
	 * would be when implementing a custom {@code IRODSProtocolManager} that needs
	 * to directly manipulate connections in a pool or cache.
	 *
	 * @throws JargonException on iRODS error
	 */
	public synchronized void disconnect() throws JargonException {
		log.debug("closing connection");
		getIrodsSession().closeSession(getIrodsAccount());

	}

	/**
	 * Method that will cause the connection to be released, returning it to the
	 * {@code IRODSProtocolManager} for shutdown when something has gone wrong with
	 * the agent or connection, and the connection should not be re-used.
	 * <p>
	 * This method is called for a forced error close of a connection from a higher
	 * level API method, and typically is not used by clients of the API. The
	 * exception would be when implementing a custom {@code IRODSProtocolManager}
	 * that needs to directly manipulate connections in a pool or cache.
	 *
	 * @throws JargonException on iRODS error
	 */
	public synchronized void disconnectWithForce() throws JargonException {
		if (getIrodsAccount() != null) {
			getIrodsSession().discardSessionForErrors(getIrodsAccount());
		}

		if (getIrodsConnection().isConnected()) {
			log.warn("partial connection, not authenticated, forcefully shut down the socket");
			getIrodsConnection().obliterateConnectionAndDiscardErrors();
		}

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
	 * Get the {@code IRODSAccount} that describes the current connection
	 *
	 * @return {@link IRODSAccount}
	 */
	public synchronized IRODSAccount getIrodsAccount() {
		return irodsAccount;
	}

	/**
	 * Required to close out certain operations, such as parallel transfer
	 * operations.
	 *
	 * @param status {@code int} with the status code to send
	 * @throws JargonException on iRODS error
	 */
	public synchronized void operationComplete(final int status) throws JargonException {
		Tag message = new Tag(AbstractIRODSPackingInstruction.INT_PI,
				new Tag[] { new Tag(AbstractIRODSPackingInstruction.MY_INT, status), });
		irodsFunction(IRODSConstants.RODS_API_REQ, message.parseTag(), IRODSConstants.OPR_COMPLETE_AN);
	}

	/**
	 * Used internally to consume status messages from various commands, this will
	 * send a given integer value in network order to iRODS.
	 *
	 * @param value {@code int} with
	 * @throws JargonException on iRODS error
	 */
	public synchronized void sendInNetworkOrder(final int value) throws JargonException {
		try {
			irodsConnection.sendInNetworkOrder(value);
		} catch (IOException e) {
			disconnectWithForce();
			throw new JargonException(e);
		}
	}

	/**
	 * Used internally to consume status messages from various commands, this will
	 * send a given integer value in network order to iRODS. Will flush after the
	 * send.
	 *
	 * @param value {@code int} with
	 * @throws JargonException on iRODS error
	 */
	public synchronized void sendInNetworkOrderWithFlush(final int value) throws JargonException {
		try {
			// irodsConnection.flush();
			irodsConnection.sendInNetworkOrder(value, true);
		} catch (IOException e) {
			disconnectWithForce();
			throw new JargonException(e);
		}
	}

	/**
	 * Get the {@code IRODSSession} that was used to obtain this connection
	 *
	 * @return {@link IRODSSession}
	 */
	public synchronized IRODSSession getIrodsSession() {
		return irodsSession;
	}

	/**
	 * Set the {@code IRODSSession} that was used to obtain this connection
	 *
	 * @param irodsSession {@link IRODSSession} to set
	 */
	public synchronized void setIrodsSession(final IRODSSession irodsSession) {
		if (irodsSession == null) {
			throw new IllegalArgumentException("null irodsSession");
		}
		irodsConnection.setIrodsSession(irodsSession);
		this.irodsSession = irodsSession;
	}

	/**
	 * @return {@link IRODSProtocolManager} set in this protocol
	 */
	public synchronized IRODSProtocolManager getIrodsProtocolManager() {
		return irodsProtocolManager;
	}

	/**
	 * @return {@link AbstractConnection} that is the iRODS connection
	 */
	public AbstractConnection getIrodsConnection() {
		return irodsConnection;
	}

	/**
	 * Respond to client status messages for an operation until exhausted.
	 *
	 * @param reply {@code Tag} containing status messages from IRODS
	 * @throws JargonException for iRODS error
	 */
	public synchronized void processClientStatusMessages(final Tag reply) throws JargonException {

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
					int fileCount = Integer.parseInt((String) fileCountTag.getValue());

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
	 * @return {@link AuthResponse} associated with the authentication of this
	 *         connection
	 */
	public synchronized AuthResponse getAuthResponse() {
		return authResponse;
	}

	/**
	 * @return {@link IRODSServerProperties} associated with this connection
	 */
	synchronized IRODSServerProperties getIrodsServerProperties() {
		return irodsServerProperties;
	}

	/**
	 * @param irodsServerProperties {@link IRODSServerProperties} to set for this
	 *                              connection
	 *
	 */
	synchronized void setIrodsServerProperties(final IRODSServerProperties irodsServerProperties) {
		this.irodsServerProperties = irodsServerProperties;
	}

	/**
	 * @param irodsAccount {@link IRODSAccount}
	 *
	 */
	synchronized void setIrodsAccount(final IRODSAccount irodsAccount) {
		this.irodsAccount = irodsAccount;
	}

	/**
	 * @param authResponse {@link AuthResponse} from the initiation of this
	 *                     connection
	 *
	 */
	protected synchronized void setAuthResponse(final AuthResponse authResponse) {
		this.authResponse = authResponse;
	}

	Tag readHeader() throws JargonException {
		byte[] header;
		int length = readHeaderLength();
		if (length < 0) {
			log.error("protocol error< header length is:" + length);
			throw new JargonException("Protocol error, read header and got header length less than zero");
		} else if (length > 10000000) {
			/*
			 * Protocol failure: One cause, if running a rule that uses msiDataObjPut or
			 * Get, then when the server requests the Put, the client instead send a query
			 * or other function, the next function sent to the server (such as the Put
			 * itself) will fail and the server will return a RError_PI message. However
			 * this message, unlike regular server communications, does not have the 4 byte
			 * message length in front of the message. Causing unexpected results when
			 * attempting to parse the message, ie. <REr are interpreted as the message
			 * length.
			 *
			 * <RError_PI> <count>1 </count> <RErrMsg_PI> <status>-808000 </status>
			 * <msg>ERROR: msiDataObjPut: rsDataObjPut failed for <MsgHeader_PI>
			 * <type>RODS_API_REPLY </type> <msgLen>0 </msgLen> <errorLen>0 </errorLen>
			 * <bsLen>0 </bsLen> <intInfo>-808000 </intInfo> </MsgHeader_PI> , status =
			 * -808000 </msg> </RErrMsg_PI> </RError_PI>
			 */

			irodsSession.discardSessionForErrors(irodsAccount);
			throw new JargonException("Server connection lost, due to error");

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
			return Tag.readNextTag(header, getEncoding());
		} catch (UnsupportedEncodingException e) {
			log.error("Unsupported encoding for:{}", getEncoding());
			throw new JargonException("Unsupported encoding for:" + getEncoding());
		}
	}

	int readHeaderLength() throws JargonException {
		byte[] headerInt = new byte[ConnectionConstants.HEADER_INT_LENGTH];
		try {
			irodsConnection.read(headerInt, 0, ConnectionConstants.HEADER_INT_LENGTH);
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

	Tag readMessageBody(final int length, final boolean decode) throws JargonException {
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
			return Tag.readNextTag(body, decode, getEncoding());
		} catch (UnsupportedEncodingException e) {
			log.error("Unsupported encoding for:{}", getEncoding());
			throw new JargonException("Unsupported encoding for:" + getEncoding());
		}
	}

	Tag processMessageRErrorStack(final int errorLength) throws JargonException {
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
		try {
			return Tag.readNextTag(errorMessage, getEncoding());
		} catch (UnsupportedEncodingException e) {
			log.error("Unsupported encoding for:{}", getEncoding());
			throw new JargonException("Unsupported encoding for:" + getEncoding());
		}
	}

	void processMessageErrorNotEqualZero(final int errorLength) throws JargonException {
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
			errorTag = Tag.readNextTag(errorMessage, getEncoding());
		} catch (UnsupportedEncodingException e) {
			log.error("Unsupported encoding for:{}", getEncoding());
			throw new JargonException("Unsupported encoding for:" + getEncoding());
		}

		Tag errorPITag = errorTag.getTag(RErrMsg.PI_TAG);
		if (errorPITag == null) {
			throw new JargonException("errorPITag missing when processing an error in response from iRODS");
		}

		Tag status = errorPITag.getTag("status");
		if (status == null) {
			throw new JargonException("no status tag in error PI tag when processing error in response from iRODS");
		}

		int statusVal = status.getIntValue();
		if (statusVal == 0) {
			log.debug("error status of 0 indicates normal operation, ignored");
			String errorText = errorTag.getTag(RErrMsg.PI_TAG).getTag(IRodsPI.MESSAGE_TAG).getStringValue();
			log.debug("error tag contents:{}", errorText);
		}

		String errorText = errorTag.getTag(RErrMsg.PI_TAG).getTag(IRodsPI.MESSAGE_TAG).getStringValue();

		log.error("IRODS error encountered:{}", errorText);
		log.error("status from error is:{}", statusVal);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.connection.IRODSManagedConnection#
	 * obliterateConnectionAndDiscardErrors()
	 */
	public synchronized void obliterateConnectionAndDiscardErrors() {
		log.warn("obliterateConnectionAndDiscardErrors() will forcefully close the connection");
		irodsConnection.obliterateConnectionAndDiscardErrors();
	}

	void processMessageInfoLessThanZero(final int messageLength, final int errorLength, final int info)
			throws JargonException {
		log.debug("info is < 0");
		byte[] messageByte = new byte[messageLength];
		// if nothing else, read the returned bytes and throw them away
		if (messageLength > 0) {
			log.debug("throwing away bytes");
			try {
				irodsConnection.read(new byte[messageLength], 0, messageLength);
				Tag.readNextTag(messageByte, getEncoding());

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
			log.debug("success but no info returned from irods");
		} else {
			IRODSErrorScanner.inspectAndThrowIfNeeded(info, addlMessage);
		}

	}

	private String readAndLogErrorMessage(final int errorLength, final int info) throws JargonException {
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
				errorTag = Tag.readNextTag(errorMessage, getEncoding());

				if (errorTag != null) {
					log.error("IRODS error occured " + errorTag.getTag(RErrMsg.PI_TAG).getTag(IRodsPI.MESSAGE_TAG)
							+ " : " + info);

					additionalMessage = errorTag.getTag(RErrMsg.PI_TAG).getTag(IRodsPI.MESSAGE_TAG).getStringValue();
				}

			} catch (UnsupportedEncodingException e) {
				log.error("Unsupported encoding for: {}", getEncoding());
				throw new JargonException("Unsupported encoding for: " + getEncoding());
			}

		}
		return additionalMessage;

	}

	/**
	 * Handy method to get the pipeline configuration, which is derived from the
	 * jargon properties and describes the various networing and buffering options
	 *
	 * @return {@link PipelineConfiguration}
	 */
	public PipelineConfiguration getPipelineConfiguration() {
		return irodsConnection.getPipelineConfiguration();
	}

	/**
	 * Handy method to get the encoding scheme used
	 *
	 * @return {@code String} with the encoding scheme
	 */
	public String getEncoding() {
		return irodsConnection.getPipelineConfiguration().getDefaultEncoding();
	}

	/**
	 * @param irodsProtocolManager {@link IRODSProtocolManager} to set
	 *
	 */
	public void setIrodsProtocolManager(final IRODSProtocolManager irodsProtocolManager) {
		this.irodsProtocolManager = irodsProtocolManager;
	}

	/**
	 * Get the system time the connection was made, in milliseconds
	 *
	 * @return {@code long} with the system time in milliseconds the connection was
	 *         made
	 */
	public long getConnectTimeInMillis() {
		return getIrodsConnection().getConnectTimeInMillis();
	}

	/**
	 * @param irodsConnection the irodsConnection to set
	 */
	protected void setIrodsConnection(final AbstractConnection irodsConnection) {
		this.irodsConnection = irodsConnection;
	}

	/**
	 * @return {@link AbstractConnection} that is not wrapped in encryption
	 *
	 */
	public AbstractConnection getIrodsConnectionNonEncryptedRef() {
		return irodsConnectionNonEncryptedRef;
	}

	/**
	 * @param irodsConnectionNonEncryptedRef {@link AbstractConnection} that is not
	 *                                       encrypted, and may have been wrapped in
	 *                                       SSL
	 */
	public void setIrodsConnectionNonEncryptedRef(final AbstractConnection irodsConnectionNonEncryptedRef) {
		this.irodsConnectionNonEncryptedRef = irodsConnectionNonEncryptedRef;
	}

}
