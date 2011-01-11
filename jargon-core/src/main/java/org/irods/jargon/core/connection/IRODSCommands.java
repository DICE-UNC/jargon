/**
 * 
 */
package org.irods.jargon.core.connection;

import static edu.sdsc.grid.io.irods.IRODSConstants.OPR_COMPLETE_AN;
import static edu.sdsc.grid.io.irods.IRODSConstants.RODS_API_REQ;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.channels.ClosedChannelException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;

import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonFileOrCollAlreadyExistsException;
import org.irods.jargon.core.packinstr.AbstractIRODSPackingInstruction;
import org.irods.jargon.core.packinstr.AuthResponseInp;
import org.irods.jargon.core.packinstr.IRodsPI;
import org.irods.jargon.core.packinstr.RErrMsg;
import org.irods.jargon.core.packinstr.StartupPack;
import org.irods.jargon.core.protovalues.ErrorEnum;
import org.irods.jargon.core.protovalues.RequestTypes;
import org.irods.jargon.core.protovalues.XmlProtApis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.sdsc.grid.io.Base64;
import edu.sdsc.grid.io.FileFactory;
import edu.sdsc.grid.io.Host;
import edu.sdsc.grid.io.Lucid;
import edu.sdsc.grid.io.irods.Tag;

/**
 * Encapsulates sending of messages and parsing of responses above the socket
 * read/write level and below the abstract operation level.
 * 
 * Note that the IRODS Connection object that this protocol utilizes is not
 * synchronized. Since a connection manager may also be managing the connection.
 * This <code>IRODSProtocol</code> object manages any necessary synchronization
 * on the connection to the underlying {@link IRODSConnection IRODSConnection}
 * This connection should not be shared between threads. A rule of thumb is to
 * treat a connection to IRODS the same way you would treat a JDBC database
 * connection.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class IRODSCommands implements IRODSManagedConnection {

	private Logger log = LoggerFactory.getLogger(IRODSCommands.class);
	private final IRODSConnection irodsConnection;
	private IRODSServerProperties irodsServerProperties;
	private final IRODSProtocolManager irodsProtocolManager;

	private String cachedChallengeValue = "";
	/**
	 * note that this field is not final. This is due to the fact that it may be
	 * altered during initialization to resolve GSI info. The field's setter
	 * method is not published, and the <code>IRODSAccount</code> is immutable,
	 * but care should be taken.
	 */
	private IRODSAccount irodsAccount;

	private IRODSCommands(final IRODSAccount irodsAccount,
			final IRODSProtocolManager irodsConnectionManager)
			throws JargonException {
		/*
		 * create the IRODSConnection object. The connection object encapsulates
		 * an open socket to the host/port described by the irodsAccount.
		 */

		if (irodsConnectionManager == null) {
			throw new JargonException("irodsConnectionManager is null");
		}

		this.irodsConnection = IRODSConnection.instance(irodsAccount,
				irodsConnectionManager);
		this.irodsProtocolManager = irodsConnectionManager;
		startupConnection(irodsAccount);

	}

	private void startupConnection(final IRODSAccount irodsAccount)
			throws JargonException {
		// send startup packet here
		this.sendStartupPacket(irodsAccount);
		// LOG in and augment/store IRODS Account
		if (irodsAccount.getAuthenticationScheme().equals(
				IRODSAccount.GSI_PASSWORD)) {
			sendGSIPassword(irodsAccount);
			this.irodsAccount = lookupAdditionalIRODSAccountInfoWhenGSI(irodsAccount);
		} else {
			sendStandardPassword(irodsAccount);
			this.irodsAccount = irodsAccount;
		}

		// set the server properties

		EnvironmentalInfoAccessor environmentalInfoAccessor = new EnvironmentalInfoAccessor(
				this);
		irodsServerProperties = environmentalInfoAccessor
				.getIRODSServerProperties();
		log.info(irodsServerProperties.toString());
	}

	private IRODSAccount lookupAdditionalIRODSAccountInfoWhenGSI(
			final IRODSAccount irodsAccount2) {
		// FIXME: implement with GSI
		return null;
	}

	static IRODSCommands instance(final IRODSAccount irodsAccount,
			final IRODSProtocolManager irodsConnectionManager)
			throws JargonException {

		return new IRODSCommands(irodsAccount, irodsConnectionManager);
	}

	/**
	 * Create a typical iRODS api call Tag
	 */
	public synchronized Tag irodsFunction(final String type,
			final String message, final int intInfo) throws JargonException {
		return irodsFunction(type, message, 0, null, 0, null, intInfo);
	}

	/**
	 * Create an iRODS message Tag, including header. Send the bytes of the byte
	 * array, no error stream.
	 */
	public synchronized Tag irodsFunction(final String type,
			final String message, final byte[] errorStream,
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

		if (message == null || message.length() == 0) {
			String err = "null or missing message returned from parse";
			log.error(err);
			throw new JargonException(err);
		}

		try {
			irodsConnection
					.send(createHeader(
							RODS_API_REQ,
							message.getBytes(ConnectionConstants.JARGON_CONNECTION_ENCODING).length,
							errorLength, byteStringLength, intInfo));

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
	 * Create an iRODS message Tag, including header.
	 */
	public synchronized Tag irodsFunction(final String type,
			final String message, final int errorLength,
			final InputStream errorStream, final long byteStringLength,
			final InputStream byteStream, final int intInfo)
			throws JargonException {

		log.info("calling irods function with streams");
		if (log.isDebugEnabled()) {
			log.debug("calling irods function with:" + message);
			log.debug("api number is:" + intInfo);
		}

		if (type == null || type.length() == 0) {
			String err = "null or blank type";
			log.error(err);
			throw new IllegalArgumentException(err); // FIXME: jargon excep
		}

		if (message == null) {
			String err = "null message";
			log.error(err);
			throw new IllegalArgumentException(err);
		}

		if (log.isDebugEnabled()) {
			log.debug(message);
		}
		try {
			irodsConnection
					.send(createHeader(
							RODS_API_REQ,
							message.getBytes(ConnectionConstants.JARGON_CONNECTION_ENCODING).length,
							errorLength, byteStringLength, intInfo));
			irodsConnection.send(message);
			if (errorLength > 0) {
				irodsConnection.send(errorStream, errorLength);
			}
			if (byteStringLength > 0) {
				irodsConnection.send(byteStream, byteStringLength);
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
	 * Create an iRODS message Tag, including header. Send the bytes of the byte
	 * array, no error stream.
	 */
	public synchronized Tag irodsFunction(final IRodsPI irodsPI,
			final byte[] errorStream, final int errorOffset,
			final int errorLength, final byte[] bytes, final int byteOffset,
			final int byteStringLength) throws JargonException {

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
					.send(createHeader(
							RODS_API_REQ,
							out.getBytes(ConnectionConstants.JARGON_CONNECTION_ENCODING).length,
							errorLength, byteStringLength, irodsPI
									.getApiNumber()));
			irodsConnection.send(out);

			if (byteStringLength > 0) {
				irodsConnection.send(bytes, byteOffset, byteStringLength);
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

		if (length <= 0) {
			throw new JargonException("length out of range");
		}

		if (destination == null) {
			throw new JargonException("destination is null");
		}

		try {
			irodsConnection.read(destination, length);
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

		return irodsFunction(RODS_API_REQ, irodsPI.getParsedTags(),
				irodsPI.getApiNumber());
	}

	/**
	 * Create the iRODS header packet
	 */
	private byte[] createHeader(final String type, final int messageLength,
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
			temp = header
					.getBytes(ConnectionConstants.JARGON_CONNECTION_ENCODING);
		} catch (UnsupportedEncodingException e) {
			throw new JargonException(e);
		}
		byte[] full = new byte[4 + temp.length];
		// load first 4 byte with header length
		Host.copyInt(temp.length, full);
		// copy rest of header into full
		System.arraycopy(temp, 0, full, 4, temp.length);
		return full;
	}

	private Tag readMessage() throws JargonException {
		return readMessage(true);
	}

	private Tag readMessage(final boolean decode) throws JargonException {
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

		// FIXME: insert iRODS error manager

		if (info < 0) {
			processMessageInfoLessThanZero(messageLength, errorLength, info);
			log.debug("returning null, no results");
			// query with no results
			return null;
		} else if (info == ErrorEnum.OVERWITE_WITHOUT_FORCE_FLAG.getInt()) {
			log.error("Attempt to overwrite file without force flag. Info val:"
					+ info);
			throw new JargonFileOrCollAlreadyExistsException(
					"Attempt to overwrite file without force flag. Info val:"
							+ info);
		} else if (info == ErrorEnum.CAT_INVALID_AUTHENTICATION.getInt()) {
			throw new JargonException("invalid user or password");
		}

		// previous will have returned or thrown exception

		if (errorLength != 0) {
			processMessageErrorNotEqualZero(errorLength);
		}

		if (messageLength > 0) {
			log.debug("message length greater than zero");
			message = readMessageBody(messageLength, decode);

			log.debug("message from IRODS read back:{}", message.parseTag());
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

		// TODO: create an iRODS error scanner class to throw correct exception.
		// Good place to refactor.
		if (info == ErrorEnum.CAT_NO_ROWS_FOUND.getInt()) {
			throw new DataNotFoundException("no data found");
		} else if (info == ErrorEnum.CAT_SUCCESS_BUT_WITH_NO_INFO.getInt()) {
			handleSuccessButNoRowsFound(errorLength, info);
		} else if (info == ErrorEnum.CAT_NAME_EXISTS_AS_COLLECTION.getInt()
				|| info == ErrorEnum.CAT_NAME_EXISTS_AS_DATAOBJ.getInt()) {
			handleOverwriteOfCollectionOrDataObject(errorLength, info);
		} else {
			String msg = "error occurred in irods with a return value of "
					+ info;
			log.error(msg);
			throw new JargonException("IRODS Exception:" + info, info);
		}
	}

	private void handleOverwriteOfCollectionOrDataObject(final int errorLength,
			final int info) throws JargonException {
		log.debug("was no rows found or success with no info");
		readAndLogErrorMessage(errorLength, info);
		throw new JargonFileOrCollAlreadyExistsException(
				"Attempt to overwrite file without force flag", info);
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
							ConnectionConstants.JARGON_CONNECTION_ENCODING);
				} catch (UnsupportedEncodingException e) {
					log.error("Unsupported encoding for:"
							+ ConnectionConstants.JARGON_CONNECTION_ENCODING);
					throw new JargonException("Unsupported encoding for:"
							+ ConnectionConstants.JARGON_CONNECTION_ENCODING);
				}
				log.error("IRODS error occured "
						+ errorTag.getTag(RErrMsg.PI_TAG).getTag(
								AbstractIRODSPackingInstruction.MESSAGE_TAG)
						+ " : " + info);
			}
		}
	}

	/**
	 * @param errorLength
	 * @param info
	 * @throws JargonException
	 */
	private void handleSuccessButNoRowsFound(final int errorLength,
			final int info) throws JargonException {
		log.debug("was no rows found or success with no info");
		if (errorLength != 0) {
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
			if (log.isDebugEnabled()) {
				Tag errorTag;
				try {
					errorTag = Tag.readNextTag(errorMessage,
							ConnectionConstants.JARGON_CONNECTION_ENCODING);
				} catch (UnsupportedEncodingException e) {
					log.error("Unsupported encoding for:"
							+ ConnectionConstants.JARGON_CONNECTION_ENCODING);
					throw new JargonException("Unsupported encoding for:"
							+ ConnectionConstants.JARGON_CONNECTION_ENCODING);
				}
				log.error("IRODS error occured "
						+ errorTag.getTag(RErrMsg.PI_TAG).getTag(
								AbstractIRODSPackingInstruction.MESSAGE_TAG)
						+ " : " + info);
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
					ConnectionConstants.JARGON_CONNECTION_ENCODING);
		} catch (UnsupportedEncodingException e) {
			log.error("Unsupported encoding for:"
					+ ConnectionConstants.JARGON_CONNECTION_ENCODING);
			throw new JargonException("Unsupported encoding for:"
					+ ConnectionConstants.JARGON_CONNECTION_ENCODING);
		}
		log.error("IRODS error occured "
				+ errorTag.getTag(RErrMsg.PI_TAG).getTag(
						AbstractIRODSPackingInstruction.MESSAGE_TAG));

		throw new JargonException("IRODS error occured "
				+ errorTag.getTag(RErrMsg.PI_TAG).getTag(
						AbstractIRODSPackingInstruction.MESSAGE_TAG));
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

			log.warn("protocol error " + length);

			// to recover from some protocol errors, (slowly and if lucky)
			// read until a new message header is found.
			boolean cont = true;
			int protoChar;
			byte[] temp = new byte[13];
			String newHeader = "MsgHeader_PI>";
			// hopefully won't be too many bytes...
			do {
				protoChar = irodsConnection.read();
				if (protoChar == (int) '<') {
					protoChar = irodsConnection.read(temp);
					String headerString;

					try {
						headerString = new String(temp,
								ConnectionConstants.JARGON_CONNECTION_ENCODING);
					} catch (UnsupportedEncodingException e) {
						log.error("unsupported encoding");
						throw new JargonException(e);
					}

					if (headerString.equals(newHeader)) {
						temp = new byte[1000];
						// find the end of the header and proceed from there
						for (int i = 0; i < temp.length; i++) {
							temp[i] = (byte) irodsConnection.read();
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
									return Tag
											.readNextTag(
													header,
													ConnectionConstants.JARGON_CONNECTION_ENCODING);
								} catch (UnsupportedEncodingException e) {
									log.error("Unsupported encoding for:"
											+ ConnectionConstants.JARGON_CONNECTION_ENCODING);
									throw new JargonException(
											"Unsupported encoding for:"
													+ ConnectionConstants.JARGON_CONNECTION_ENCODING);
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
					ConnectionConstants.JARGON_CONNECTION_ENCODING);
		} catch (UnsupportedEncodingException e) {
			log.error("Unsupported encoding for:"
					+ ConnectionConstants.JARGON_CONNECTION_ENCODING);
			throw new JargonException("Unsupported encoding for:"
					+ ConnectionConstants.JARGON_CONNECTION_ENCODING);
		}
	}

	private int readHeaderLength() throws JargonException {
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
		return Host.castToInt(headerInt);
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
					ConnectionConstants.JARGON_CONNECTION_ENCODING);
		} catch (UnsupportedEncodingException e) {
			log.error("Unsupported encoding for:"
					+ ConnectionConstants.JARGON_CONNECTION_ENCODING);
			throw new JargonException("Unsupported encoding for:"
					+ ConnectionConstants.JARGON_CONNECTION_ENCODING);
		}
	}

	@Override
	public String getConnectionUri() throws JargonException {
		return irodsConnection.getConnectionUri();
	}

	public IRODSAccount getIRODSAccount() {
		return irodsAccount;
	}

	@Override
	public synchronized boolean isConnected() {
		return irodsConnection.isConnected();
	}

	/**
	 * Handles sending the userinfo connection protocol. First, sends initial
	 * handshake with IRODS.
	 * <P>
	 * 
	 * @throws IOException
	 *             if the host cannot be opened or created.
	 */
	Tag sendStartupPacket(final IRODSAccount irodsAccount)
			throws JargonException {

		StartupPack startupPack = new StartupPack(irodsAccount);
		String startupPackData = startupPack.getParsedTags();
		try {
			irodsConnection.send(createHeader(
					RequestTypes.RODS_CONNECT.getRequestType(),
					startupPackData.length(), 0, 0, 0));
			irodsConnection.send(startupPackData);
			irodsConnection.flush();
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
		Tag responseMessage = readMessage();
		return responseMessage;
	}

	void sendStandardPassword(final IRODSAccount irodsAccount)
			throws JargonException {
		if (irodsAccount == null) {
			throw new JargonException("irods account is null");
		}
		log.info("sending standard irods password");
		try {
			irodsConnection.send(createHeader(
					RequestTypes.RODS_API_REQ.getRequestType(), 0, 0, 0,
					XmlProtApis.AUTH_REQUEST_AN.getApiNumber()));
			irodsConnection.flush();
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
		Tag message = readMessage(false);

		// Create and send the response
		cachedChallengeValue = message.getTag(StartupPack.CHALLENGE)
				.getStringValue();
		log.debug("cached challenge response:{}", cachedChallengeValue);

		String response = challengeResponse(
				message.getTag(StartupPack.CHALLENGE).getStringValue(),
				irodsAccount.getPassword());
		AuthResponseInp authResponse_PI = new AuthResponseInp(
				irodsAccount.getUserName(), response);

		// should be a header with no body if successful
		irodsFunction(RequestTypes.RODS_API_REQ.getRequestType(),
				authResponse_PI.getParsedTags(),
				XmlProtApis.AUTH_RESPONSE_AN.getApiNumber());
	}

	void sendGSIPassword(final IRODSAccount irodsAccount)
			throws JargonException {
		if (irodsAccount == null) {
			throw new JargonException("irods account is null");
		}
		try {
			irodsConnection.send(createHeader(
					RequestTypes.RODS_API_REQ.getRequestType(), 0, 0, 0,
					XmlProtApis.GSI_AUTH_REQUEST_AN.getApiNumber()));
			irodsConnection.flush();
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

		// FIXME: not yet implemented
		/*
		 * Create and send the response note that this is the one use of the get
		 * methods for the socket and streams of the connection in Jargon. This
		 * is not optimal, and will be refactored at a later time
		 */

		/*
		 * String serverDn =
		 * readMessage(false).getTag(AuthResponseInp_PI.SERVER_DN)
		 * .getStringValue(); new GSIAuth(account,
		 * irodsConnection.getConnection(), irodsConnection
		 * .getIrodsOutputStream(), irodsConnection.getIrodsInputStream());
		 */
	}

	/**
	 * Add the password to the end of the challenge string, pad to the correct
	 * length, and take the md5 of that.
	 */
	private String challengeResponse(final String challenge, String password)
			throws JargonException {
		// Convert base64 string to a byte array
		byte[] chal = null;
		byte[] temp = Base64.fromString(challenge);

		getIRODSAccount();
		if (IRODSAccount.isDefaultObfuscate()) {
			// FIXME: get rid of this utf stuff and just put the code in here,
			// remove refs to other old Jargon cruft
			try {
				/*
        \u002a\u002f\u0070\u0061\u0073\u0073\u0077\u006f\u0072\u0064 \u003d \u006e\u0065\u0077 \u004c\u0075\u0063\u0069\u0064\u0028
						\u0046\u0069\u006c\u0065\u0046\u0061\u0063\u0074\u006f\u0072\u0079
								\u002e\u006e\u0065\u0077\u0046\u0069\u006c\u0065\u0028\u006e\u0065\u0077 \u0055\u0052\u0049\u0028
										\u0070\u0061\u0073\u0073\u0077\u006f\u0072\u0064\u0029\u0029\u0029
						\u002e\u006c\u0031\u0036\u0028\u0029\u003b\u002f\u002a
        */
			} catch (Throwable e) {
				log.error("error during account obfuscation", e);
			}
		}

		if (password.length() < ConnectionConstants.MAX_PASSWORD_LENGTH) {
			// pad the end with zeros to MAX_PASSWORD_LENGTH
			chal = new byte[ConnectionConstants.CHALLENGE_LENGTH
					+ ConnectionConstants.MAX_PASSWORD_LENGTH];
		} else {
			log.error("password is too long");
			throw new IllegalArgumentException("Password is too long");
		}

		// add the password to the end
		System.arraycopy(temp, 0, chal, 0, temp.length);
		try {
			temp = password
					.getBytes(ConnectionConstants.JARGON_CONNECTION_ENCODING);
		} catch (UnsupportedEncodingException e1) {
			log.error("unsupported encoding of:"
					+ ConnectionConstants.JARGON_CONNECTION_ENCODING, e1);
			throw new JargonException("unsupported encoding:"
					+ ConnectionConstants.JARGON_CONNECTION_ENCODING);
		}
		System.arraycopy(temp, 0, chal, ConnectionConstants.CHALLENGE_LENGTH,
				temp.length);

		// get the md5 of the challenge+password
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			chal = digest.digest(chal);
		} catch (GeneralSecurityException e) {
			SecurityException se = new SecurityException();
			se.initCause(e);
			log.error(
					"general security exception, initCause is:"
							+ e.getMessage(), e);
			throw se;
		}

		// after md5 turn any 0 into 1
		for (int i = 0; i < chal.length; i++) {
			if (chal[i] == 0) {
				chal[i] = 1;
			}
		}

		// return to Base64
		return Base64.toString(chal);
	}

	@Override
	public synchronized void obliterateConnectionAndDiscardErrors() {
		irodsConnection.obliterateConnectionAndDiscardErrors();

	}

	@Override
	public synchronized void shutdown() throws JargonException {
		log.info("shutting down, need to send disconnect to irods");

		irodsConnection.shutdown();
	}

	/**
	 * Send a shutdown message to irods, then tell the underlying
	 * {@link IRODSConnection} to shut down;
	 */
	@Override
	public synchronized void disconnect() throws JargonException {
		log.info("closing connection");
		if (isConnected()) {
			log.info("sending disconnect message");
			try {
				irodsConnection.send(createHeader(
						RequestTypes.RODS_DISCONNECT.getRequestType(), 0, 0, 0,
						0));
				irodsConnection.flush();
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

			irodsProtocolManager.returnIRODSConnection(irodsConnection);
		} else {
			log.warn("disconnect called, but isConnected() is false, this is an unexpected condition that is logged and ignored");
		}
	}

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
		irodsFunction(RODS_API_REQ, message.parseTag(), OPR_COMPLETE_AN);
	}

	protected String getCachedChallengeValue() {
		return cachedChallengeValue;
	}

}
