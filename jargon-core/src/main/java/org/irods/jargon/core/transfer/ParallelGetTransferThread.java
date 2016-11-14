/**
 *
 */
package org.irods.jargon.core.transfer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;

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
	 * @param threadNumber
	 *            <code>int</code> with the thread number
	 * @return <code>ParallelGetTransferThread</code>
	 * @throws JargonException
	 */
	public static ParallelGetTransferThread instance(
			final ParallelGetFileTransferStrategy parallelGetFileTransferStrategy,
			final int threadNumber) throws JargonException {
		return new ParallelGetTransferThread(parallelGetFileTransferStrategy,
				threadNumber);
	}

	private ParallelGetTransferThread(
			final ParallelGetFileTransferStrategy parallelGetFileTransferStrategy,
			final int threadNumber) throws JargonException {

		super(threadNumber);
		if (parallelGetFileTransferStrategy == null) {
			throw new JargonException("parallelGetFileTransferStrategy is null");
		}

		this.parallelGetFileTransferStrategy = parallelGetFileTransferStrategy;
	}

	@Override
	public ParallelTransferResult call() throws JargonException {
		try {
			Socket s = new Socket();
			if (parallelGetFileTransferStrategy.getPipelineConfiguration()
					.getParallelTcpSendWindowSize() > 0) {
				s.setSendBufferSize(parallelGetFileTransferStrategy
						.getPipelineConfiguration()
						.getParallelTcpSendWindowSize() * 1024);
			}

			if (parallelGetFileTransferStrategy.getPipelineConfiguration()
					.getParallelTcpReceiveWindowSize() > 0) {
				s.setReceiveBufferSize(parallelGetFileTransferStrategy
						.getPipelineConfiguration()
						.getParallelTcpReceiveWindowSize() * 1024);
			}

			s.setPerformancePreferences(parallelGetFileTransferStrategy
					.getPipelineConfiguration()
					.getParallelTcpPerformancePrefsConnectionTime(),
					parallelGetFileTransferStrategy.getPipelineConfiguration()
							.getParallelTcpPerformancePrefsLatency(),
					parallelGetFileTransferStrategy.getPipelineConfiguration()
							.getParallelTcpPerformancePrefsBandwidth());

			InetSocketAddress address = new InetSocketAddress(
					parallelGetFileTransferStrategy.getHost(),
					parallelGetFileTransferStrategy.getPort());

			s.setSoTimeout(parallelGetFileTransferStrategy
					.getParallelSocketTimeoutInSecs() * 1000);

			s.setKeepAlive(parallelGetFileTransferStrategy
					.getPipelineConfiguration().isParallelTcpKeepAlive());

			// assume reuse, nodelay
			s.setReuseAddress(true);
			s.setTcpNoDelay(false);
			s.connect(address);
			setS(s);
			byte[] outputBuffer = new byte[4];
			Host.copyInt(parallelGetFileTransferStrategy.getPassword(),
					outputBuffer);

			int inputBuffSize = parallelGetFileTransferStrategy
					.getJargonProperties().getInternalInputStreamBufferSize();
			int outputBuffSize = parallelGetFileTransferStrategy
					.getJargonProperties().getInternalOutputStreamBufferSize();

			if (inputBuffSize < 0) {
				setIn(getS().getInputStream());
			} else if (inputBuffSize == 0) {
				setIn(new BufferedInputStream(getS().getInputStream()));
			} else {
				setIn(new BufferedInputStream(getS().getInputStream(),
						inputBuffSize));
			}

			if (outputBuffSize < 0) {
				setOut(getS().getOutputStream());
			} else if (outputBuffSize == 0) {
				setOut(new BufferedOutputStream(getS().getOutputStream()));
			} else {
				setOut(new BufferedOutputStream(getS().getOutputStream(),
						outputBuffSize));
			}

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
					parallelGetFileTransferStrategy.getHost(), e);
			setExceptionInTransfer(e);
			throw new JargonException("unknown host:"
					+ parallelGetFileTransferStrategy.getHost(), e);
		} catch (Throwable e) {
			log.error("unchecked exception in transfer", e);
			throw new JargonException(e);
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

		RandomAccessFile local = null;

		try {
			log.info("opening local randomAccessFile");
			local = new RandomAccessFile(
					parallelGetFileTransferStrategy.getLocalFile(), "rw");
			log.info("random access file opened rw mode");
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
				if (local != null) {
					local.close();
				}
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
		// log.info("reading header info...");

		// read the header
		int operation = readInt();
		// log.info("   operation:{}", operation);

		readInt();

		// Where to seek into the data
		long offset = readLong();

		// How much to read/write
		long length = readLong();
		log.info(">>>new offset:{}", offset);
		log.info(">>>new length:{}", length);

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
				// c code - size_t buf_size = ( 2 * TRANS_BUF_SZ ) * sizeof(
				// unsigned char );
				buffer = new byte[parallelGetFileTransferStrategy
						.getJargonProperties().getParallelCopyBufferSize()];
			}

			seekToOffset(local, offset);

			long totalWrittenSinceLastRestartUpdate = 0;

			while (length > 0) {

				if (Thread.interrupted()) {
					throw new IOException(

					"interrupted, consider connection corrupted and return IOException to clear");
				}

				log.debug("reading....");

				read = myRead(getIn(), buffer, Math.min(
						parallelGetFileTransferStrategy.getJargonProperties()
								.getParallelCopyBufferSize(), (int) length));

				totalWrittenSinceLastRestartUpdate += read;

				if (read > 0) {
					length -= read;
					if (length == 0) {

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

						if (parallelGetFileTransferStrategy
								.getFileRestartInfo() != null) {

							parallelGetFileTransferStrategy.getRestartManager()
									.updateLengthForSegment(
											parallelGetFileTransferStrategy
													.getFileRestartInfo()
													.identifierFromThisInfo(),
											getThreadNumber(),
											totalWrittenSinceLastRestartUpdate);
							totalWrittenSinceLastRestartUpdate = 0;
							log.debug("signal storage of new info");

						}

						// read the next header
						operation = readInt();
						readInt();
						offset = readLong();
						length = readLong();

						log.info(">>>new offset:{}", offset);
						log.info(">>>new length:{}", length);

						if (operation == DONE_OPR) {
							break;
						}

						/*
						 * If restarting, maintain a reference to the offset
						 */

						seekToOffset(local, offset);

					} else if (length < 0) {
						String msg = "length < 0 passed in header from iRODS during parallel get operation";
						log.error(msg);
						throw new JargonException(msg);
					} else {

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

					}
				} else {
					log.warn("intercepted a loop condition on parallel file get, length is > 0 but I just read and got nothing...breaking...");
					length = 0;
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
		} catch (Throwable e) {
			log.error("exception in parallel transfer", e);
			throw new JargonException(
					"unexpected exception in parallel transfer", e);
		}
	}

	private int myRead(final InputStream in, final byte[] buffer,
			final int length) throws IOException, JargonException {
		int myLength = length;
		int ptr = 0;
		int read = 0;
		int totalRead = 0;

		if (buffer.length < length) {
			throw new JargonException("attempting to read more than buffer");
		}

		while (myLength > 0) {
			log.info(">>>>>>>>>>>>> top of while, my length:{} <<<<<<<<<<<",
					myLength);
			if (ptr > buffer.length) {
				log.error("ptr out of synch");
				log.error("buffer size:{}", buffer.length);
				log.error("ptr:{}", ptr);
				log.error("myLength:{}", myLength);
				log.error("totalRead:{}", totalRead);
				throw new JargonException(
						"pointer is pointing out of range of the buffer");
			}

			log.info("===========================");
			log.info("ptr:{}", ptr);
			log.info("myLength:{}", myLength);

			read = in.read(buffer, ptr, myLength);

			log.info(">>> read:{}", read);

			if (read < 0) {
				log.error("read < 0");
				break;
			}

			myLength -= read;
			totalRead += read;
			ptr += read;

			log.info("total read now:{}", totalRead);
			log.info("out of original length:{}", length);
			log.info("makes my length:{}", myLength);

		}

		if (totalRead != length) {
			log.error("did not read expected length in myRead()");
			throw new JargonException("did not read expected length");
		}
		return totalRead;
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

			if (parallelGetFileTransferStrategy.getFileRestartInfo() != null) {
				parallelGetFileTransferStrategy.getRestartManager()
						.updateOffsetForSegment(
								parallelGetFileTransferStrategy
										.getFileRestartInfo()
										.identifierFromThisInfo(),
								getThreadNumber(), offset);
			}

			try {
				if (offset == local.getFilePointer()) {
					return; // at current location
				}
				local.seek(offset);

				// log.debug("seek completed");
			} catch (Exception e) {
				log.error(IO_EXEPTION_IN_PARALLEL_TRANSFER,
						parallelGetFileTransferStrategy.toString());
				throw new JargonException(
						IO_EXCEPTION_OCCURRED_DURING_PARALLEL_FILE_TRANSFER, e);
			}
		}
	}
}
