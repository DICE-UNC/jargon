package org.irods.jargon.core.transfer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.utils.Host;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract superclasss for a parallel file transfer operation via NIO
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class AbstractNIOParallelTransferThread {

	protected static final String IO_EXCEPTION_OCCURRED_DURING_PARALLEL_FILE_TRANSFER = "IOException occurred during parallel file transfer";
	protected static final String IO_EXEPTION_IN_PARALLEL_TRANSFER = "IOExeption in parallel transfer";
	private SocketChannel s;

	private Exception exceptionInTransfer = null;
	public static final int DONE_OPR = 9999;
	public static final int PUT_OPR = 1;
	public static final int GET_OPR = 2;

	public static final Logger log = LoggerFactory
			.getLogger(AbstractNIOParallelTransferThread.class);

	protected AbstractNIOParallelTransferThread() {
		super();
	}

	private ByteBuffer readLenFromSocket(final int length)
			throws JargonException {
		final ByteBuffer b = ByteBuffer.allocate(length);

		int read;
		int tot = 0;
		try {

			read = getS().read(b);
			while (read != -1) {
				if (Thread.interrupted()) {
					throw new IOException(

							"interrupted, consider connection corrupted and return IOException to clear");
				}
				tot += read;
				if (tot == length) {
					break;
				}
				read = getS().read(b);

			}
		} catch (Exception e) {
			log.error(IO_EXEPTION_IN_PARALLEL_TRANSFER, e);
			throw new JargonException(
					IO_EXCEPTION_OCCURRED_DURING_PARALLEL_FILE_TRANSFER, e);
		}
		return b;
	}

	protected int readInt() throws JargonException {
		int len = 4;
		final byte[] bAsByte = new byte[len];
		ByteBuffer b = readLenFromSocket(len);
		b.flip();
		b.get(bAsByte);
		return Host.castToInt(bAsByte);
	}

	protected long readLong() throws JargonException {
		int len = 8;
		final byte[] bAsByte = new byte[len];
		ByteBuffer b = readLenFromSocket(len);
		b.flip();
		b.get(bAsByte);
		return Host.castToLong(bAsByte);
	}

	public void close() throws JargonException {
		// garbage collector can be too slow
		if (s != null) {
			try {
				s.close();
			} catch (IOException e) {
				log.warn("IOException on close - LOG and ignore");
			}
			s = null;
		}
	}

	protected final SocketChannel getS() {
		return s;
	}

	protected final void setS(final SocketChannel s) {
		this.s = s;
	}

	/**
	 * Any exception that occurs in this transfer thread is saved so that the
	 * parallel transfer process can access it and handle any errors.
	 *
	 * @return {@code Exception} that occured in this thread, or
	 *         {@code null} if no error occurred.
	 */
	public Exception getExceptionInTransfer() {
		return exceptionInTransfer;
	}

	protected void setExceptionInTransfer(final Exception exceptionInTransfer) {
		this.exceptionInTransfer = exceptionInTransfer;
	}
}