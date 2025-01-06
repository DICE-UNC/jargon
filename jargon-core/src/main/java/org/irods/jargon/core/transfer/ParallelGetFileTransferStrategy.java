/**
 *
 */
package org.irods.jargon.core.transfer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.irods.jargon.core.connection.NegotiatedClientServerConfiguration;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.DefaultIntraFileProgressCallbackListener;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
public final class ParallelGetFileTransferStrategy extends AbstractParallelFileTransferStrategy {

	public static final Logger log = LogManager.getLogger(ParallelGetFileTransferStrategy.class);

	/**
	 * Create an instance of a strategy to accomplish a parallel file transfer.
	 *
	 * @param host
	 *            {@code String} with the host name to transfer to.
	 * @param port
	 *            {@code int} with the port number for the host.
	 * @param numberOfThreads
	 *            {@code int} with the number of threads over which the transfer
	 *            will occur.
	 * @param password
	 *            {@code String} with the password sent by iRODS for this transfer.
	 * @param localFile
	 *            {@code File} representing the local file
	 * @param irodsAccessObjectFactory
	 *            {@link IRODSAccessObjectFactory} for the session.
	 *
	 * @param transferLength
	 *            {@code long} with the total length of the transfer
	 * @param transferControlBlock
	 *            {@link TransferControlBlock} that controls and keeps track of the
	 *            transfer operation, required.
	 * @param transferStatusCallbackListener
	 *            {@link TransferStatusCallbackListener} or {@code null} if not
	 *            desired. This can receive call-backs on the status of the parallel
	 *            transfer operation.
	 * @param fileRestartInfo
	 *            {@link FileRestartInfo}
	 * @param negotiatedClientServerConfiguration
	 *            {@link NegotiatedClientServerConfiguration} including encryption
	 *            requirements
	 * @return {@link ParallelGetFileTransferStrategy}
	 * @throws JargonException
	 *             for iRODS error
	 */
	public static ParallelGetFileTransferStrategy instance(final String host, final int port, final int numberOfThreads,
			final int password, final File localFile, final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final long transferLength, final TransferControlBlock transferControlBlock,
			final TransferStatusCallbackListener transferStatusCallbackListener, final FileRestartInfo fileRestartInfo,
			final NegotiatedClientServerConfiguration negotiatedClientServerConfiguration) throws JargonException {
		return new ParallelGetFileTransferStrategy(host, port, numberOfThreads, password, localFile,
				irodsAccessObjectFactory, transferLength, transferControlBlock, transferStatusCallbackListener,
				fileRestartInfo, negotiatedClientServerConfiguration);
	}

	private ParallelGetFileTransferStrategy(final String host, final int port, final int numberOfThreads,
			final int password, final File localFile, final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final long transferLength, final TransferControlBlock transferControlBlock,
			final TransferStatusCallbackListener transferStatusCallbackListener, final FileRestartInfo fileRestartInfo,
			final NegotiatedClientServerConfiguration negotiatedClientServerConfiguration) throws JargonException {

		super(host, port, numberOfThreads, password, localFile, irodsAccessObjectFactory, transferLength,
				transferControlBlock, transferStatusCallbackListener, fileRestartInfo,
				negotiatedClientServerConfiguration);

		log.info("transfer options in transfer control block:{}", transferControlBlock.getTransferOptions());

		if (transferStatusCallbackListener == null) {
			log.info("null transferStatusCallbackListener");
		}

		if (transferControlBlock.getTransferOptions().isIntraFileStatusCallbacks()
				&& transferStatusCallbackListener != null) {
			log.info("will do intra-file status callbacks from transfer");
			setConnectionProgressStatusListener(
					DefaultIntraFileProgressCallbackListener.instance(TransferStatus.TransferType.GET,
							getTransferLength(), transferControlBlock, transferStatusCallbackListener));
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
		log.info("initiating transfer for: {}", toString());
		ExecutorService executor = getIrodsAccessObjectFactory().getIrodsSession().getParallelTransferThreadPool();
		if (executor == null) {
			ExecutorService executorService = null;
			try {
				log.info("no pool available, transfer using single executor");
				executorService = Executors.newFixedThreadPool(numberOfThreads);
				transferWithExecutor(executorService);
			} finally {
				if (executorService != null) {
					executorService.shutdown();
				}
			}

		} else {
			log.info("transfer via executor");
			transferWithExecutor(executor);
		}
		log.info("transfer process has returned");
	}

	private void transferWithExecutor(final ExecutorService executor) throws JargonException {
		final List<ParallelGetTransferThread> parallelGetTransferThreads = new ArrayList<ParallelGetTransferThread>();

		try {

			for (int i = 0; i < numberOfThreads; i++) {
				final ParallelGetTransferThread parallelTransfer = ParallelGetTransferThread.instance(this, i);
				parallelGetTransferThreads.add(parallelTransfer);
			}
			log.info("invoking executor threads for get");
			log.info("invoking executor threads for put");
			List<Future<ParallelTransferResult>> transferThreadStates = executor.invokeAll(parallelGetTransferThreads);

			for (Future<ParallelTransferResult> transferState : transferThreadStates) {
				try {
					transferState.get();
				} catch (ExecutionException e) {
					throw new JargonException(e.getCause());
				}

			}

			log.info("executor completed");
		} catch (InterruptedException e) {
			log.error("interrupted exception in thread", e);
			throw new JargonException(e);
		} catch (Exception e) {
			log.error("an error occurred in a parallel get", e);
			throw new JargonException(e);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ParallelGetFileTransferStrategy");
		sb.append("\n   host:");
		sb.append(getHost());
		sb.append("\n   port:");
		sb.append(getPort());
		sb.append("\n   numberOfThreads:");
		sb.append(getNumberOfThreads());
		sb.append("\n   localFile:");
		sb.append(localFile.getAbsolutePath());
		sb.append("\n   transferLength:");
		sb.append(transferLength);
		return sb.toString();

	}

}
