package org.irods.jargon.core.transfer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Callable;

import org.irods.jargon.core.connection.ConnectionProgressStatus;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.utils.Host;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle parallel file transfer put operation, this is used within jargon.core,
 * and is not meant for public API use. See
 * {@link org.irods.jargon.core.pub.DataTransferOperations} for public API used
 * for file transfers.
 * <p/>
 * This version uses nio for the file and socket operations.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public final class ParallelPutViaNIOTransferThread extends
		AbstractNIOParallelTransferThread implements
		Callable<ParallelTransferResult> {

	private final ParallelPutFileViaNIOTransferStrategy parallelPutFileTransferStrategy;

	public static final Logger log = LoggerFactory
			.getLogger(ParallelPutViaNIOTransferThread.class);

	/**
	 * Represents a thread used in a parallel file transfer. There will be
	 * multiple threads controlled from the
	 * <code>ParalellPutFileTransferStrategy</code>. This is an immutable object
	 * , as is the <code>parallelFileTransferStrategy</code> that this object
	 * holds a reference to.
	 * 
	 * @param parallelPutFileTransferStrategy
	 *            {@link org.irods.jargon.core.transfer.ParallelPutFileTransferStrategy}
	 *            that controls the transfer threads.
	 * @return <code>ParallelPutTransferThread</code>
	 * @throws JargonException
	 */
	public static ParallelPutViaNIOTransferThread instance(
			final ParallelPutFileViaNIOTransferStrategy parallelPutFileTransferStrategy)
			throws JargonException {
		return new ParallelPutViaNIOTransferThread(
				parallelPutFileTransferStrategy);
	}

	private ParallelPutViaNIOTransferThread(
			final ParallelPutFileViaNIOTransferStrategy parallelPutFileTransferStrategy)
			throws JargonException {

		super();

		if (parallelPutFileTransferStrategy == null) {
			throw new JargonException("parallelPutFileTransferStrategy is null");
		}

		this.parallelPutFileTransferStrategy = parallelPutFileTransferStrategy;

		try {
			log.info(
					"opening socketchannel to parallel transfer (high) port at port:{}",
					parallelPutFileTransferStrategy.getPort());
			setS(SocketChannel.open());
			log.debug("socket channel open..");
			getS().connect(
					new InetSocketAddress(parallelPutFileTransferStrategy
							.getHost(), parallelPutFileTransferStrategy
							.getPort()));
			log.debug("socket connected");
		} catch (Exception e) {
			log.error("unable to create transfer thread", e);
			throw new JargonException(e);
		}
	}

	@Override
	public ParallelTransferResult call() throws JargonException {

		try {

			log.info("writing the cookie (password) for the output thread");

			// write the cookie
			byte b[] = new byte[4];
			Host.copyInt(parallelPutFileTransferStrategy.getPassword(), b);
			ByteBuffer bb = ByteBuffer.allocate(4);
			bb.put(b);
			bb.flip();

			while (bb.hasRemaining()) {
				getS().write(bb);
			}

			log.debug("cookie written for output thread...calling put() to start read/write loop");
			put();
			log.debug("put operation completed");
		} catch (Exception e) {
			log.error(
					"An exception occurred during a parallel file put operation",
					e);
			setExceptionInTransfer(e);
			throw new JargonException("error during parallel file put", e);
		} finally {
			log.info("closing sockets, this eats any exceptions");
			close();
		}

		ParallelTransferResult result = new ParallelTransferResult();
		result.transferException = getExceptionInTransfer();

		return result;

	}

	private void put() throws JargonException {
		log.info("put()..");
		int copyBuffSize = parallelPutFileTransferStrategy
				.getJargonProperties().getInputToOutputCopyBufferByteSize();
		boolean done = false;

		try {
			while (!done) {

				log.debug("in main put() loop, reading header data");

				// read the header
				int operation = readInt();

				log.info("   operation:{}", operation);

				if (operation == AbstractParallelTransferThread.PUT_OPR) {
					log.debug("put operation");
				} else if (operation == AbstractParallelTransferThread.DONE_OPR) {
					log.info("done received");
					done = true;
					break;
				} else {
					throw new JargonException("unknown operation received");
				}

				// read the flags
				int flags = readInt();
				log.info("   flags:{}", flags);
				// Where to seek into the data
				long offset = readLong();
				log.info("   offset:{}", offset);
				// How much to read/write
				long length = readLong();
				log.info("   length:{}", length);

				log.info("copy buffer length for put is: {}", copyBuffSize);

				/*
				 * Read/write loop moves data from file starting at offset down
				 * the socket until the anticipated transfer length is consumed.
				 */

				readWriteLoopForCurrentHeaderDirective(offset, length);

			}

		} catch (IOException e) {
			log.error(
					"An IO exception occurred during a parallel file put operation",
					e);
			throw new JargonException("IOException during parallel file put", e);
		}
	}

	private void readWriteLoopForCurrentHeaderDirective(final long position,
			final long length) throws IOException, JargonException {

		int copyBuffSize = parallelPutFileTransferStrategy
				.getJargonProperties().getInputToOutputCopyBufferByteSize();

		try {

			log.debug("channel copy initiate between file and socket channel");

			long currentPosition = position;
			long totalBytes = 0;

			while (totalBytes < length) {

				if (length - totalBytes < copyBuffSize) {
					copyBuffSize = (int) (length - totalBytes);
				}

				long bytesRead = parallelPutFileTransferStrategy
						.getFileChannel().transferTo(currentPosition,
								copyBuffSize, getS());

				if (bytesRead > 0) {
					currentPosition += bytesRead;
					totalBytes += bytesRead;
					/*
					 * Make an intra-file status call-back if a listener is
					 * configured
					 */
					if (parallelPutFileTransferStrategy
							.getConnectionProgressStatusListener() != null) {
						parallelPutFileTransferStrategy
								.getConnectionProgressStatusListener()
								.connectionProgressStatusCallback(
										ConnectionProgressStatus
												.instanceForSend(bytesRead));
					}

				}
			}

			log.debug("transferred:{}", totalBytes);

			log.debug("wrote data to the buffer");

		} catch (Exception e) {
			log.error("error writing to iRODS parallel transfer socket", e);
			setExceptionInTransfer(e);
			throw new JargonException(e);
		}
	}
}
