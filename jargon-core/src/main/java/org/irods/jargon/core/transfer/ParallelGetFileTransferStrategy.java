/**
 * 
 */
package org.irods.jargon.core.transfer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.DefaultIntraFileProgressCallbackListener;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle parallel file transfer get operation within Jargon. See
 * {@link org.irods.jargon.core.pub.DataTransferOperations} for the public API
 * to transfer files.
 * 
 * This is an immutable object that encapsulates the parallel file transfer
 * algorithm, and is used internally.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public final class ParallelGetFileTransferStrategy extends
		AbstractParallelFileTransferStrategy {

	public static final Logger log = LoggerFactory
			.getLogger(ParallelGetFileTransferStrategy.class);

	/**
	 * Create an instance of a strategy to accomplish a parallel file transfer.
	 * 
	 * @param host
	 *            <code>String</code> with the host name to transfer to.
	 * @param port
	 *            <code>int</code> with the port number for the host.
	 * @param numberOfThreads
	 *            <code>int</code> with the number of threads over which the
	 *            transfer will occur.
	 * @param password
	 *            <code>String</code> with the password sent by iRODS for this
	 *            transfer.
	 * @param localFile
	 *            <code>File</code> representing the local file
	 * @param irodsAccessObjectFactory
	 *            {@link IRODSAccessObjectFactory} for the session.
	 * 
	 * @param transferLength
	 *            <code>long</code> with the total length of the transfer
	 * @param transferControlBlock
	 *            {@link TransferControlBlock} that controls and keeps track of
	 *            the transfer operation, required.
	 * @param transferStatusCallbackListener
	 *            {@link TransferStatusCallbackListener} or <code>null</code> if
	 *            not desired. This can receive call-backs on the status of the
	 *            parallel transfer operation.
	 * @return
	 * @throws JargonException
	 */
	public static ParallelGetFileTransferStrategy instance(final String host,
			final int port, final int numberOfThreads, final int password,
			final File localFile,
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final long transferLength,
			final TransferControlBlock transferControlBlock,
			final TransferStatusCallbackListener transferStatusCallbackListener)
			throws JargonException {
		return new ParallelGetFileTransferStrategy(host, port, numberOfThreads,
				password, localFile, irodsAccessObjectFactory, transferLength,
				transferControlBlock, transferStatusCallbackListener);
	}

	private ParallelGetFileTransferStrategy(final String host, final int port,
			final int numberOfThreads, final int password,
			final File localFile,
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final long transferLength,
			final TransferControlBlock transferControlBlock,
			final TransferStatusCallbackListener transferStatusCallbackListener)
			throws JargonException {

		super(host, port, numberOfThreads, password, localFile,
				irodsAccessObjectFactory, transferLength, transferControlBlock,
				transferStatusCallbackListener);

		log.info("transfer options in transfer control block:{}",
				transferControlBlock.getTransferOptions());

		if (transferStatusCallbackListener == null) {
			log.info("null transferStatusCallbackListener");
		}

		if (transferControlBlock.getTransferOptions()
				.isIntraFileStatusCallbacks()
				&& transferStatusCallbackListener != null) {
			log.info("will do intra-file status callbacks from transfer");
			this.setConnectionProgressStatusListener(DefaultIntraFileProgressCallbackListener
					.instance(TransferStatus.TransferType.GET,
							getTransferLength(), transferControlBlock,
							transferStatusCallbackListener));
		} else {
			log.info("transfer status callbacks will not be processed");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.transfer.AbstractParallelFileTransferStrategy#transfer
	 * ()
	 */
	@Override
	public void transfer() throws JargonException {
		log.info("initiating transfer for: {}", this.toString());
		ExecutorService executor = getIrodsAccessObjectFactory()
				.getIrodsSession().getParallelTransferThreadPool();
		if (executor == null) {
			ExecutorService executorService = null;
			try {
				log.info("no pool available, transfer using single executor");
				executorService = Executors.newFixedThreadPool(numberOfThreads);
				transferWithExecutor(executorService);
			} finally {
				if (executorService != null)
					executorService.shutdown();
			}

		} else {
			log.info("transfer via executor");
			transferWithExecutor(executor);
		}
		log.info("transfer process has returned");
	}

	private void transferWithExecutor(final ExecutorService executor)
			throws JargonException {
		final List<ParallelGetTransferThread> parallelGetTransferThreads = new ArrayList<ParallelGetTransferThread>();

		for (int i = 0; i < numberOfThreads; i++) {
			final ParallelGetTransferThread parallelTransfer = ParallelGetTransferThread
					.instance(this);
			parallelGetTransferThreads.add(parallelTransfer);
		}

		try {
			log.info("invoking executor threads for get");
			executor.invokeAll(parallelGetTransferThreads);
			log.info("executor completed");
		} catch (InterruptedException e) {
			log.error("interrupted exception in thread", e);
			throw new JargonException(e);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ParallelGetFileTransferStrategy");
		sb.append("\n   host:");
		sb.append(this.getHost());
		sb.append("\n   port:");
		sb.append(this.getPort());
		sb.append("\n   numberOfThreads:");
		sb.append(this.getNumberOfThreads());
		sb.append("\n   localFile:");
		sb.append(localFile.getAbsolutePath());
		sb.append("\n   transferLength:");
		sb.append(transferLength);
		return sb.toString();

	}

}
