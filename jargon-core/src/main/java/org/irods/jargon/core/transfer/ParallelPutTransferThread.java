package org.irods.jargon.core.transfer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;

import org.irods.jargon.core.connection.ConnectionConstants;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.sdsc.grid.io.Host;

/**
 * Handle parallel file transfer put operation, this is used within jargon.core,
 * and is not meant for public API use. See
 * {@link org.irods.jargon.core.pub.DataTransferOperations} for public API used
 * for file transfers.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public final class ParallelPutTransferThread extends
		AbstractParallelTransferThread implements Runnable {

	private long transferLength;
	private final long offset;
	private final ParallelPutFileTransferStrategy parallelPutFileTransferStrategy;

	public static final Logger log = LoggerFactory
			.getLogger(ParallelPutTransferThread.class);

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
	public static ParallelPutTransferThread instance(
			final ParallelPutFileTransferStrategy parallelPutFileTransferStrategy,
			final long transferLength, final long offset)
			throws JargonException {
		return new ParallelPutTransferThread(parallelPutFileTransferStrategy,
				transferLength, offset);
	}

	private ParallelPutTransferThread(
			final ParallelPutFileTransferStrategy parallelPutFileTransferStrategy,
			final long transferLength, final long offset)
			throws JargonException {

		super();

		if (parallelPutFileTransferStrategy == null) {
			throw new JargonException("parallelPutFileTransferStrategy is null");
		}

		if (transferLength <= 0) {
			throw new JargonException("transferLength is 0 or less than zero");
		}

		if (offset < 0) {
			throw new JargonException("transferLength is 0 or less than zero");
		}

		this.parallelPutFileTransferStrategy = parallelPutFileTransferStrategy;
		this.transferLength = transferLength;
		this.offset = offset;

	}

	@Override
	public void run() {
		try {
			log.info("getting input stream for local file");
			setIn(new BufferedInputStream(new FileInputStream(
					parallelPutFileTransferStrategy.getLocalFile())));

			long totalSkipped = 0;
			long toSkip = 0;

			// guard against occasions where skip does not skip the full amount

			if (offset > 0) {
				long skipped = getIn().skip(offset);
				totalSkipped += skipped;

				while (totalSkipped < offset) {
					log.warn("did not skip entire offset amount, call skip again");
					toSkip = offset - totalSkipped;
					skipped = getIn().skip(toSkip);
				}
			}

			log.debug("opening socket to paralllel transfer (high) port at port:{}", parallelPutFileTransferStrategy.getPort());
			setS(new Socket(parallelPutFileTransferStrategy.getHost(),
					parallelPutFileTransferStrategy.getPort()));
			getS().setSoTimeout(30000);
			setOut(new BufferedOutputStream(getS().getOutputStream()));
			
			log.info("writing the cookie (password) for the output thread");

			// write the cookie
			byte b[] = new byte[4];
			Host.copyInt(parallelPutFileTransferStrategy.getPassword(), b);
			getOut().write(b);
			getOut().flush();
			log.debug("cookie written for output thread");
			put();
			log.debug("put operation completed");
			
		} catch (Exception e) {
			log.error(
					"An exception occurred during a parallel file put operation",
					e);
			this.setExceptionInTransfer(e);
			throw new JargonRuntimeException("error during parallel file put",
					e);
		}
	}

	private void put() throws JargonException {
		// Holds all the data for transfer
		byte[] buffer = null;
		int read = 0;

		// begin transfer
		if (transferLength <= 0) {
			return;
		} else {
			// length has a max of 8mb?
			buffer = new byte[ConnectionConstants.OUTPUT_BUFFER_LENGTH];
		}

		while (transferLength > 0) {
			// need Math.min or reads into what the other threads are
			// transferring
			try {
				read = getIn().read(
						buffer,
						0,
						(int) Math.min(
								ConnectionConstants.OUTPUT_BUFFER_LENGTH,
								transferLength));
			} catch (IOException e) {
				log.error(
						"An IO exception occurred during a parallel file put operation",
						e);
				throw new JargonException(
						"IOException during parallel file put", e);
			}
			if (read > 0) {
				try {
					transferLength -= read;
					getOut().write(buffer, 0, read);
					getOut().flush();
				} catch (IOException e) {
					log.error(
							"An IO exception occurred during a parallel file put operation",
							e);
					throw new JargonException(
							"IOException during parallel file put", e);
				}
			} else if (read < 0) {
				throw new JargonException(
						"unexpected end of data in transfer operation");
			}
			Thread.yield();
		}
	}

}
