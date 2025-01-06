package org.irods.jargon.core.pub.io;

import java.io.IOException;
import java.io.InputStream;

import org.irods.jargon.core.connection.IRODSMidLevelProtocol;
import org.irods.jargon.core.connection.IrodsVersion;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.ExecCmdStreamClose;
import org.irods.jargon.core.packinstr.ExecCmdStreamClose419;
import org.irods.jargon.core.packinstr.FileReadInp;
import org.irods.jargon.core.packinstr.Tag;
import org.irods.jargon.core.utils.IRODSConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Special subclass of {@code InputStream} meant to encapsulate binary data
 * streamed from iRODS as the result of a remote execution. Normally, when a
 * remote script is executed on iRODS, the data is returned in the response
 * packing instruction as BASE64-encoded binary data. It is possible to request
 * of iRODS (after version 2.4.1) that larger data sizes may be streamed back
 * from the remotely executed script. In this case, the first segement of data
 * is sent and processed as before, while the remainder streams back as binary
 * data sent after the packing instruction message.
 * <p>
 * This stream represents the supplementary binary data coming back from iRODS.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class RemoteExecutionBinaryResultInputStream extends InputStream {

	public static final Logger log = LogManager.getLogger(RemoteExecutionBinaryResultInputStream.class);

	private final IRODSMidLevelProtocol irodsCommands;
	private final int fileDescriptor;

	public int getFileDescriptor() {
		return fileDescriptor;
	}

	/**
	 * iRODS streams are simulated, so there are not bytes waiting to be read from
	 * the socket until read is requested. This is set to return an {@code int}
	 * value of 1 so it does not block if used. This may need some further thought!
	 *
	 * @return {@code int} with the available bytes
	 */
	@Override
	public int available() throws IOException {
		return 1; // TODO: consider the effect of returning this available value
		// here...
	}

	/**
	 * Close the input stream. This method will send a command to iRODS to close the
	 * file descriptor set up when the additional stream data was sent.
	 */
	@Override
	public void close() throws IOException {
		log.info("closing input stream");
		try {
			IrodsVersion irodsVersion = irodsCommands.getIRODSServerProperties().getIrodsVersion();
			if (irodsVersion.hasVersionOfAtLeast("rods4.1.9")) {
				log.debug("using 4.1.9 and later close");
				ExecCmdStreamClose419 execCmdStreamClose = ExecCmdStreamClose419.instance(fileDescriptor, "");
				irodsCommands.irodsFunction(execCmdStreamClose);

			} else {
				log.debug("using pre4.1.9 close");
				ExecCmdStreamClose execCmdStreamClose = ExecCmdStreamClose.instance(fileDescriptor);
				irodsCommands.irodsFunction(execCmdStreamClose);

			}
		} catch (JargonException e) {
			log.error("Jargon exception will be rethrown as an IOException for the method contracts", e);
			throw new IOException(e);
		}
	}

	/**
	 * mark() is not supported, so calling this method will result in an
	 * {@code UnsupportedOperationException}
	 */
	@Override
	public synchronized void mark(final int readlimit) {
		throw new UnsupportedOperationException("mark is not supported");
	}

	/**
	 * mark() is not supported, so this will always return false;
	 */
	@Override
	public boolean markSupported() {
		return false;
	}

	@Override
	public int read(final byte[] b, final int off, final int len) throws IOException {
		try {

			log.info("stream read for fd: {}", fileDescriptor);

			if (fileDescriptor <= 0) {
				throw new IllegalArgumentException("invalid file descriptor");
			}

			if (len < 0) {
				throw new IllegalArgumentException("invalid len");
			}

			FileReadInp fileReadInp = FileReadInp.instanceForReadStream(fileDescriptor, len);

			Tag message = irodsCommands.irodsFunction(fileReadInp);

			if (message == null) {
				log.warn("null response from iRODS on send of command, treat as eof");
				return -1;
			}

			int buffLength = message.getTag(IRODSConstants.MsgHeader_PI).getTag(IRODSConstants.bsLen).getIntValue();

			// read the message byte stream for the length that the header
			// indicates

			int read = irodsCommands.read(b, off, buffLength);

			if (read != message.getTag(IRODSConstants.MsgHeader_PI).getTag(IRODSConstants.intInfo).getIntValue()) {

				log.error("did not read length equal to response length, expected" + buffLength
						+ " bytes actually read:" + read);
				throw new IOException("Bytes read mismatch");
			}

			if (read < 0) {
				return -1;
			}

			return read;

		} catch (JargonException e) {
			log.error("JargonException in read is converted to IOException for method contract", e);
			throw new IOException(e);
		}
	}

	@Override
	public int read(final byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	/**
	 * reset() is not supported and will result in an
	 * {@code UnsupportedOperationException}
	 */
	@Override
	public void reset() throws IOException {
		throw new UnsupportedOperationException("reset is not supported");
	}

	/**
	 * Skip the desired amount of bytes from the stream. This method will repeatedly
	 * attempt to read past the skipped value, discard the bytes, and continue
	 * reading until the full amount has been skipped, or the end of the stream is
	 * encountered.
	 *
	 * @param n
	 *            {@code long} with the amount to skip
	 * @return {@code long}
	 */
	@Override
	public long skip(final long n) throws IOException {

		if (n <= 0) {
			throw new IllegalArgumentException("attempt to skip a neg or zero amount");
		}

		int skippedSoFar = 0;
		int read = 0;

		while (read > -1 && skippedSoFar < n) {
			read = read(new byte[(int) n]);
			if (read > -1) {
				skippedSoFar += read;
			}
		}

		return skippedSoFar;
	}

	/**
	 * Create a special type of binary input stream for data being streamed as a
	 * result of the remote execution of an iRODS command.
	 *
	 * @param irodsCommands
	 *            {@link IRODSMidLevelProtocol} for iRODS connections
	 * @param fileDescriptor
	 *            {@code int} with the file handle
	 */
	public RemoteExecutionBinaryResultInputStream(final IRODSMidLevelProtocol irodsCommands, final int fileDescriptor) {
		super();

		if (irodsCommands == null) {
			throw new IllegalArgumentException("null irodsCommands");
		}

		if (fileDescriptor <= 0) {
			throw new IllegalArgumentException("negative or zero file descriptor for stream");
		}

		this.irodsCommands = irodsCommands;
		this.fileDescriptor = fileDescriptor;

	}

	/**
	 * Note: Use of this method is inadvisable due to the long delays that can occur
	 * with network communications. Reading even a few bytes in this manner could
	 * cause noticeable slow-downs.
	 *
	 * Reads the next byte of data from the input stream. The value byte is returned
	 * as an {@code int} in the range {@code 0} to {@code 255}. If no byte is
	 * available because the end of the stream has been reached, the value
	 * {@code -1} is returned. This method blocks until input data is available, the
	 * end of the stream is detected, or an exception is thrown.
	 *
	 * @return the next byte of data, or {@code -1} if the end of the stream is
	 *         reached.
	 * @exception IOException
	 *                if an I/O error occurs.
	 */
	@Override
	public int read() throws IOException {
		byte buffer[] = new byte[1];
		int read = read(buffer, 0, 1);
		if (read > -1) {
			return (buffer[0] & 0xFF);
		} else {
			return -1;
		}
	}

}
