/**
 * 
 */
package org.irods.jargon.core.transfer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;

import org.irods.jargon.core.connection.ConnectionConstants;
import org.irods.jargon.core.connection.ConnectionProgressStatus;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.utils.Host;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle parallel file transfer get operation within Jargon. See
 * {@link org.irods.jargon.core.pub.DataTransferOperations} for the public API
 * to transfer files.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public final class ParallelGetTransferThread extends
		AbstractParallelTransferThread implements
		Callable<ParallelTransferResult> {

	private final ParallelGetFileTransferStrategy parallelGetFileTransferStrategy;

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
	public ParallelTransferResult call() throws JargonException {
		try {
			setS(new Socket(parallelGetFileTransferStrategy.getHost(),
					parallelGetFileTransferStrategy.getPort()));

			if (parallelGetFileTransferStrategy
					.getParallelSocketTimeoutInSecs() > 0) {
				log.info(
						"timeout (in seconds) for parallel transfer sockets is:{}",
						parallelGetFileTransferStrategy
								.getParallelSocketTimeoutInSecs());
				getS().setSoTimeout(
						parallelGetFileTransferStrategy
								.getParallelSocketTimeoutInSecs() * 1000);
			}

			byte[] outputBuffer = new byte[4];
			Host.copyInt(parallelGetFileTransferStrategy.getPassword(),
					outputBuffer);
			setIn(new BufferedInputStream(getS().getInputStream()));
			setOut(new BufferedOutputStream(getS().getOutputStream()));
			log.debug("socket established, sending cookie to iRODS listener");
			getOut().write(outputBuffer);
			getOut().flush();
			log.debug("cookie written");
			log.info("sockets are open and password sent, now begin the get operation");

			get();
			log.info("exiting get and returning the finish object");
			ParallelTransferResult result = new ParallelTransferResult();
			result.transferException = getExceptionInTransfer();
			return result;

		} catch (UnknownHostException e) {
			log.error("Unknown host: {}",
					parallelGetFileTransferStrategy.getHost());
			setExceptionInTransfer(e);
			throw new JargonException("unknown host:"
					+ parallelGetFileTransferStrategy.getHost(), e);
		} catch (IOException e) {
			log.error(IO_EXEPTION_IN_PARALLEL_TRANSFER,
					parallelGetFileTransferStrategy.toString());
			setExceptionInTransfer(e);
			throw new JargonException(
					IO_EXCEPTION_OCCURRED_DURING_PARALLEL_FILE_TRANSFER, e);
		}

	}

	public void get() throws JargonException {
		log.info("parallel transfer get");

		if (parallelGetFileTransferStrategy
				.getConnectionProgressStatusListener() == null) {
			log.info("no connection progress status listener configured, no detailed callbacks");
		} else {
			log.info("connection listener configured, will produce callbacks");
		}

		RandomAccessFile local;
		try {
			log.info("opening local randomAccessFile");
			local = new RandomAccessFile(
					parallelGetFileTransferStrategy.getLocalFile(), "rw");
			log.info("random access file opened rw mode");
		} catch (FileNotFoundException e) {
			log.error("FileNotFoundException in parallel get operation", e);
			throw new JargonException(
					"FileNotFoundException in parallel get operation", e);
		}

		try {

			processingLoopForGetData(local);

		} catch (JargonException je) {
			log.error("a jargon exception occurred in the get loop");
			throw je;
		} catch (Exception e) {
			log.error("Exception closing local file", e);
			throw new JargonException("IOException closing local file");
		} finally {
			log.info("parallel thread closing out local random access file stream");
			try {
				log.info("closing sockets, this close eats exceptions");
				close();
				log.info("closing local file");
				local.close();
				log.info("local file closed, exiting get() method");
			} catch (IOException e) {
			}
		}
	}

	/**
	 * @param local
	 * @throws JargonException
	 */
	private void processingLoopForGetData(final RandomAccessFile local)
			throws JargonException {
		log.info("reading header info...");

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

		log.info("seeking to offset: {}", offset);
		try {

			if (length <= 0) {
				return;
			} else {
				// length has a max of 8mb?
				buffer = new byte[ConnectionConstants.OUTPUT_BUFFER_LENGTH];
			}

			seekToOffset(local, offset);

			while (length > 0) {

				log.debug("reading....");
				read = getIn().read(
						buffer,
						0,
						Math.min(ConnectionConstants.OUTPUT_BUFFER_LENGTH,
								(int) length));
				log.debug("read={}", read);

				if (read > 0) {
					length -= read;
					log.debug("length left after read={}", length);
					if (length == 0) {
						log.debug("length == 0, write the buffer, then get another header");

						local.write(buffer, 0, read);
						log.debug("buffer written to file");

						/*
						 * Make an intra-file status call-back if a listener is
						 * configured
						 */
						if (parallelGetFileTransferStrategy
								.getConnectionProgressStatusListener() != null) {
							parallelGetFileTransferStrategy
									.getConnectionProgressStatusListener()
									.connectionProgressStatusCallback(
											ConnectionProgressStatus
													.instanceForReceive(read));
						}

						log.debug("parallel transfer read next header");
						// read the next header
						operation = readInt();
						log.debug("   operation:{}", operation);
						flags = readInt();
						log.debug("   flags:{}", flags);
						offset = readLong();
						log.debug("   offset:{}", offset);
						length = readLong();
						log.debug("   length:{}", length);

						if (operation == DONE_OPR) {
							log.debug("    done...received done flag in operation");
							break;
						}

						log.debug("seeking to new offset");
						local.seek(offset);

					} else if (length < 0) {
						String msg = "length < 0 passed in header from iRODS during parallel get operation";
						log.error(msg);
						throw new JargonException(msg);
					} else {
						log.debug("length > 0, write what I have and read more...");

						local.write(buffer, 0, read);
						/*
						 * Make an intra-file status call-back if a listener is
						 * configured
						 */
						if (parallelGetFileTransferStrategy
								.getConnectionProgressStatusListener() != null) {
							parallelGetFileTransferStrategy
									.getConnectionProgressStatusListener()
									.connectionProgressStatusCallback(
											ConnectionProgressStatus
													.instanceForReceive(read));
						}
						log.debug("buffer written to file");

					}
				} else {
					log.warn("intercepted a loop condition on parallel file get, length is > 0 but I just read and got nothing...breaking...");
					// length = 0;
					throw new JargonException(
							"possible loop condition in parallel file get");
				}

				Thread.yield();
			}
		} catch (IOException e) {
			log.error(IO_EXEPTION_IN_PARALLEL_TRANSFER,
					parallelGetFileTransferStrategy.toString());
			throw new JargonException(
					IO_EXCEPTION_OCCURRED_DURING_PARALLEL_FILE_TRANSFER, e);
		} catch (Exception e) {
			log.error("exception in parallel transfer", e);
			throw new JargonException(
					"unexpected exception in parallel transfer", e);
		}
	}

	/**
	 * @param local
	 * @param offset
	 * @throws JargonRuntimeException
	 */
	private void seekToOffset(final RandomAccessFile local, final long offset)
			throws JargonException {
		if (offset < 0) {
			log.error("offset < 0 in transfer get() operation, return from get method");
			return;
		} else if (offset > 0) {
			try {
				local.seek(offset);
				log.debug("seek completed");
			} catch (IOException e) {
				log.error(IO_EXEPTION_IN_PARALLEL_TRANSFER,
						parallelGetFileTransferStrategy.toString());
				throw new JargonException(
						IO_EXCEPTION_OCCURRED_DURING_PARALLEL_FILE_TRANSFER, e);
			}
		}
	}
}
