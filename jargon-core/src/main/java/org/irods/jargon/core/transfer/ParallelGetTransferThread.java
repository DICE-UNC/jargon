/**
 * 
 */
package org.irods.jargon.core.transfer;

import static edu.sdsc.grid.io.irods.IRODSConstants.DONE_OPR;
import static edu.sdsc.grid.io.irods.IRODSConstants.GET_OPR;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.net.UnknownHostException;

import org.irods.jargon.core.connection.ConnectionConstants;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.sdsc.grid.io.Host;

/**
 * Handle parallel file transfer get operation within Jargon. See
 * {@link org.irods.jargon.core.pub.DataTransferOperations} for the public API
 * to transfer files.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public final class ParallelGetTransferThread extends
		AbstractParallelTransferThread implements Runnable {

	final ParallelGetFileTransferStrategy parallelGetFileTransferStrategy;

	public static final Logger log = LoggerFactory
			.getLogger(ParallelGetTransferThread.class);

	/**
	 * Represents a thread used in a parallel file transfer. There will be
	 * multiple threads controlled from the
	 * <code>ParalellFileTransferStrategy</code>. This is an immutable object ,
	 * as is the <code>parallelFileTransferStrategy</code> that this object
	 * holds a reference to.
	 * 
	 * @param parallelFileTransferStrategy
	 *            {@link org.irods.jargon.core.transfer.ParallelGetFileTransferStrategy}
	 *            that controls the transfer threads.
	 * @return <code>ParallelGetTransferThread</code>
	 * @throws JargonException
	 */
	public static ParallelGetTransferThread instance(
			final ParallelGetFileTransferStrategy parallelGetFileTransferStrategy)
			throws JargonException {
		return new ParallelGetTransferThread(parallelGetFileTransferStrategy);
	}

	private ParallelGetTransferThread(
			final ParallelGetFileTransferStrategy parallelGetFileTransferStrategy)
			throws JargonException {

		if (parallelGetFileTransferStrategy == null) {
			throw new JargonException("parallelGetFileTransferStrategy is null");
		}

		this.parallelGetFileTransferStrategy = parallelGetFileTransferStrategy;
	}

	@Override
	public void run() {
		try {
			setS(new Socket(parallelGetFileTransferStrategy.getHost(),
					parallelGetFileTransferStrategy.getPort()));
			// getS().setSoTimeout(30000);
			byte[] outputBuffer = new byte[4];
			Host.copyInt(parallelGetFileTransferStrategy.getPassword(),
					outputBuffer);
			setIn(getS().getInputStream());
			this.setOut(getS().getOutputStream());
			getOut().write(outputBuffer);
		} catch (UnknownHostException e) {
			log.error("Unknown host: {}",
					parallelGetFileTransferStrategy.getHost());
			this.setExceptionInTransfer(e);
			throw new JargonRuntimeException("unknown host:"
					+ parallelGetFileTransferStrategy.getHost(), e);
		} catch (IOException e) {
			log.error(IO_EXEPTION_IN_PARALLEL_TRANSFER,
					parallelGetFileTransferStrategy.toString());
			this.setExceptionInTransfer(e);
			throw new JargonRuntimeException(
					IO_EXCEPTION_OCCURRED_DURING_PARALLEL_FILE_TRANSFER, e);
		}

		log.info("sockets are open and password sent, now begin the get operation");
		try {
			get();
		} catch (JargonException e) {
			this.setExceptionInTransfer(e);
			log.error("JargonException rethrown as runtime exception for method contract");
			throw new JargonRuntimeException(
					"JargonException rethrown as runtime exception for method contract",
					e);
		}
	}

	public void get() throws JargonException {
		log.info("parallel transfer get");

		RandomAccessFile local;
		try {
			local = new RandomAccessFile(
					parallelGetFileTransferStrategy.getLocalFile(), "rw");
		} catch (FileNotFoundException e) {
			log.error("FileNotFoundException in parallel get operation", e);
			throw new JargonException(
					"FileNotFoundException in parallel get operation", e);
		}

		// read the header
		int operation = readInt();

		log.info("   operation:{}", operation);

		// read the flags
		int flags = readInt();

		log.info("   flags:{}", flags);

		// Where to seek into the data
		long offset = readLong();

		log.info("   offset:{}", offset);

		// How much to read/write
		long length = readLong();

		log.info("   length:{}", length);

		// Holds all the data for transfer
		byte[] buffer = null;
		int read = 0;

		if (operation != GET_OPR) {

			log.error("Parallel transfer expected GET,  server requested {}",
					operation);
			throw new JargonException(
					"parallel get transfer, unexpected transfer type from iRODS:"
							+ operation);
		}

		seekToOffset(local, offset);

		if (length <= 0) {
			return;
		} else {
			// length has a max of 8mb?
			buffer = new byte[ConnectionConstants.OUTPUT_BUFFER_LENGTH];
		}

		while (length > 0) {

			try {
				read = getIn().read(
						buffer,
						0,
						Math.min(ConnectionConstants.OUTPUT_BUFFER_LENGTH,
								(int) length));
			} catch (IOException e) {
				log.error(IO_EXEPTION_IN_PARALLEL_TRANSFER,
						parallelGetFileTransferStrategy.toString());
				throw new JargonRuntimeException(
						IO_EXCEPTION_OCCURRED_DURING_PARALLEL_FILE_TRANSFER, e);
			}
			if (read > 0) {
				length -= read;
				if (length == 0) {
					try {
						local.write(buffer, 0, read);
					} catch (IOException e) {
						log.error(IO_EXEPTION_IN_PARALLEL_TRANSFER,
								parallelGetFileTransferStrategy.toString());
						throw new JargonRuntimeException(
								IO_EXCEPTION_OCCURRED_DURING_PARALLEL_FILE_TRANSFER,
								e);
					}

					log.debug("parallel tranfsser read next header");
					// read the next header
					operation = readInt();
					flags = readInt();
					offset = readLong();
					length = readLong();
					if (operation == DONE_OPR) {
						log.debug("    done");
						return;
					}

					// probably unnecessary
					try {
						local.seek(offset);
					} catch (IOException e) {
						log.error(IO_EXEPTION_IN_PARALLEL_TRANSFER,
								parallelGetFileTransferStrategy.toString());
						throw new JargonRuntimeException(
								IO_EXCEPTION_OCCURRED_DURING_PARALLEL_FILE_TRANSFER,
								e);
					}

				} else if (length < 0) {
					String msg = "length < 0 when reading from iRODS during parallel get operation";
					log.error(msg);
					throw new JargonException(msg);
				} else {
					try {
						local.write(buffer, 0, read);
					} catch (IOException e) {
						log.error(IO_EXEPTION_IN_PARALLEL_TRANSFER,
								parallelGetFileTransferStrategy.toString());
						throw new JargonRuntimeException(
								IO_EXCEPTION_OCCURRED_DURING_PARALLEL_FILE_TRANSFER,
								e);
					}
				}
			} else {
				log.warn("intercepted a loop condition on parallel file get, length is > 0 but I just read and got nothing...breaking...");
				// length = 0;
				throw new JargonException(
						"possible loop condition in parallel file get");
			}
		}
	}

	/**
	 * @param local
	 * @param offset
	 * @throws JargonRuntimeException
	 */
	private void seekToOffset(final RandomAccessFile local, final long offset)
			throws JargonRuntimeException {
		if (offset < 0) {
			log.error("offset < 0 in transfer get() operation, return from get method");
			return;
		} else if (offset > 0) {
			try {
				local.seek(offset);
			} catch (IOException e) {
				log.error(IO_EXEPTION_IN_PARALLEL_TRANSFER,
						parallelGetFileTransferStrategy.toString());
				throw new JargonRuntimeException(
						IO_EXCEPTION_OCCURRED_DURING_PARALLEL_FILE_TRANSFER, e);
			}
		}
	}

}
