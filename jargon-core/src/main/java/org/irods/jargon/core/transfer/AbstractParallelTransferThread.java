package org.irods.jargon.core.transfer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.utils.Host;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract superclasss for a parallel file transfer operation
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class AbstractParallelTransferThread {

	protected static final String IO_EXCEPTION_OCCURRED_DURING_PARALLEL_FILE_TRANSFER = "IOException occurred during parallel file transfer";
	protected static final String IO_EXEPTION_IN_PARALLEL_TRANSFER = "IOExeption in parallel transfer";
	private Socket s;
	private InputStream in;
	private OutputStream out;
	private Exception exceptionInTransfer = null;
	public static final int DONE_OPR = 9999;
	public static final int PUT_OPR = 1;
	public static final int GET_OPR = 2;
	/**
	 * Index of the given thread. 0 based index
	 */
	private final int threadNumber;

	public static final Logger log = LoggerFactory.getLogger(AbstractParallelTransferThread.class);

	protected AbstractParallelTransferThread(final int threadNumber) {
		super();
		this.threadNumber = threadNumber;
	}

	protected int readInt() throws JargonException {
		final byte[] b = new byte[4];
		int read;
		try {
			read = in.read(b);
		} catch (Exception e) {
			log.error(IO_EXEPTION_IN_PARALLEL_TRANSFER, e);
			throw new JargonException(IO_EXCEPTION_OCCURRED_DURING_PARALLEL_FILE_TRANSFER, e);
		}
		if (read != 4) {
			log.error("unexpected length read when reading int");
			throw new JargonException("unexpected length read when reading int");
		}
		return Host.castToInt(b);
	}

	protected long readLong() throws JargonException {
		// length comes down the wire as an signed long long in network
		// order
		final byte[] b = new byte[8];

		int read;
		try {
			read = in.read(b);
		} catch (Exception e) {
			log.error(IO_EXEPTION_IN_PARALLEL_TRANSFER);
			throw new JargonException(IO_EXCEPTION_OCCURRED_DURING_PARALLEL_FILE_TRANSFER, e);
		}
		if (read != 8) {
			log.error("did not read 8 bytes for long");
			throw new JargonException("unable to read all the bytes for an expected long value");
		}

		return Host.castToLong(b);
	}

	public void close() throws JargonException {
		// garbage collector can be too slow
		if (out != null) {
			try {

				out.close();
			} catch (IOException e) {
				log.warn("IOException on close - log and ignore");
			}
			out = null;
		}
		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {
				log.warn("IOException on close - log and ignore");
			}
			in = null;
		}
		if (s != null) {
			try {
				s.close();
			} catch (IOException e) {
				log.warn("IOException on close - log and ignore");
			}
			s = null;
		}
	}

	protected final Socket getS() {
		return s;
	}

	protected final void setS(final Socket s) {
		this.s = s;
	}

	protected final InputStream getIn() {
		return in;
	}

	protected final void setIn(final InputStream in) {
		this.in = in;
	}

	protected final OutputStream getOut() {
		return out;
	}

	protected final void setOut(final OutputStream out) {
		this.out = out;
	}

	/**
	 * Any exception that occurs in this transfer thread is saved so that the
	 * parallel transfer process can access it and handle any errors.
	 *
	 * @return {@code Exception} that occured in this thread, or {@code null} if no
	 *         error occurred.
	 */
	public Exception getExceptionInTransfer() {
		return exceptionInTransfer;
	}

	protected void setExceptionInTransfer(final Exception exceptionInTransfer) {
		this.exceptionInTransfer = exceptionInTransfer;
	}

	/**
	 * @return the threadNumber
	 */
	protected int getThreadNumber() {
		return threadNumber;
	}

}