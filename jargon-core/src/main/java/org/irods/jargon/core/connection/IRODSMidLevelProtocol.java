package org.irods.jargon.core.connection;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.irods.jargon.core.connection.AbstractConnection.EncryptionType;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.Tag;
import org.irods.jargon.core.utils.IRODSConstants;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulates sending of messages and parsing of responses above the socket
 * read/write level and below the abstract operation level.
 * <p/>
 * Note that the IRODS Connection object that this protocol utilizes is not
 * synchronized. Since a connection manager may also be managing the connection.
 * This <code>IRODSProtocol</code> object manages any necessary synchronization
 * on the connection to the underlying {@link IRODSBasicTCPConnection
 * IRODSConnection} This connection should not be shared between threads. A rule
 * of thumb is to treat a connection to IRODS the same way you would treat a
 * JDBC database connection.
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
public class IRODSMidLevelProtocol extends AbstractIRODSMidLevelProtocol {

	protected IRODSMidLevelProtocol(final AbstractConnection irodsConnection,
			final IRODSProtocolManager irodsProtocolManager) {
		super(irodsConnection, irodsProtocolManager);
	}

	Logger log = LoggerFactory.getLogger(IRODSMidLevelProtocol.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		/*
		 * Check if a still-connected agent connection is being finalized, and
		 * nag in the log, then try and disconnect
		 */
		if (getIrodsConnection().isConnected()) {
			log.error("**************************************************************************************");
			log.error("********  WARNING: POTENTIAL CONNECTION LEAK  ******************");
			log.error("********  finalizer has run and found a connection left opened, please check your code to ensure that all connections are closed");
			log.error("********  IRODSCommands is:{}", this);
			log.error(
					"********  connection is:{}, will attempt to disconnect and shut down any restart thread",
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
	 * Check server version and see if I need extra flushes for SSL processing
	 * (for PAM). This is needed for PAM pre iRODS 3.3.
	 *
	 * @return
	 */
	boolean isPamFlush() { // FIXME: pam flush issue for 3.3.1?

		boolean postThreeDotThree = MiscIRODSUtils
				.isTheIrodsServerAtLeastAtTheGivenReleaseVersion(
						getStartupResponseData().getRelVersion(), "rods3.3");

		boolean beforeFourPointOne = !MiscIRODSUtils
				.isTheIrodsServerAtLeastAtTheGivenReleaseVersion(
						getStartupResponseData().getRelVersion(), "rods4.1.0");

		if (getIrodsConnection().getEncryptionType() == EncryptionType.SSL_WRAPPED
				&& !postThreeDotThree) {
			return true;
		} else if (getPipelineConfiguration().isForcePamFlush()) { // pam flush
			// can be
			// set by a
			// jargon.properties
			// setting
			return true;
			/*
			 * Is the server 4.0.X and not yet 4.1? Then I need to worry about
			 * pam flushes per https://github.com/DICE-UNC/jargon/issues/70 This
			 * overhead will force the pam flush based on the forceSslFlush
			 * flag, which will only be turned on to bracket the necessary calls
			 * to the protocol, preventing a performance drop from unneeded
			 * flushes later
			 */
		} else if (postThreeDotThree && beforeFourPointOne && isForceSslFlush()) {
			log.warn("using the pam flush behavior because of iRODS 4.0.X-ness - see https://github.com/DICE-UNC/jargon/issues/70");
			return true;
		} else {
			return false;
		}
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
	@Override
	public synchronized Tag irodsFunction(final String type,
			final String message, final byte[] errorBytes,
			final int errorOffset, final int errorLength, final byte[] bytes,
			final int byteOffset, final int byteStringLength, final int intInfo)
			throws JargonException {

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
				messageLength = message.getBytes(getEncoding()).length;
			}

			getIrodsConnection().send(
					createHeader(IRODSConstants.RODS_API_REQ, messageLength,
							errorLength, byteStringLength, intInfo));

			if (isPamFlush()) {
				log.debug("doing extra pam flush for iRODS 3.2");
				getIrodsConnection().flush();
			}

			getIrodsConnection().send(message);
			getIrodsConnection().flush();

			if (byteStringLength > 0) {
				getIrodsConnection().send(bytes, byteOffset, byteStringLength);
			}

			getIrodsConnection().flush();

		} catch (UnsupportedEncodingException e) {
			log.error("unsupported encoding", e);
			throw new JargonException(e);
		} catch (IOException e) {
			disconnectWithForce();
			throw new JargonException(e);
		}

		return readMessage();
	}

}
