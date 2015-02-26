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

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.DefaultIntraFileProgressCallbackListener;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Strategy object for parallell put file transfer. This is an immutable object.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public final class ParallelPutFileTransferStrategy extends
AbstractParallelFileTransferStrategy {

	public static final Logger log = LoggerFactory
			.getLogger(ParallelPutFileTransferStrategy.class);

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
	 * @param transferLength
	 *            <code>long</code> with the length of the total file to
	 *            transfer
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
	public static ParallelPutFileTransferStrategy instance(final String host,
			final int port, final int numberOfThreads, final int password,
			final File localFile,
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final long transferLength,
			final TransferControlBlock transferControlBlock,
			final TransferStatusCallbackListener transferStatusCallbackListener)
					throws JargonException {
		return new ParallelPutFileTransferStrategy(host, port, numberOfThreads,
				password, localFile, irodsAccessObjectFactory, transferLength,
				transferControlBlock, transferStatusCallbackListener);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ParallelPutFileTransferStrategy");
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

	private ParallelPutFileTransferStrategy(final String host, final int port,
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

		if (transferControlBlock.getTransferOptions()
				.isIntraFileStatusCallbacks()
				&& transferStatusCallbackListener != null) {
			log.info("will do intra-file status callbacks from transfer");
			setConnectionProgressStatusListener(DefaultIntraFileProgressCallbackListener
					.instance(TransferStatus.TransferType.PUT, transferLength,
							transferControlBlock,
							transferStatusCallbackListener));
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
		ExecutorService executor = getIrodsAccessObjectFactory()
				.getIrodsSession().getParallelTransferThreadPool();
		if (executor == null) {
			log.info("no pool available, transfer using single executor");
			ExecutorService executorService = null;
			try {
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
	}

	private void transferWithExecutor(final ExecutorService executor)
			throws JargonException {
		log.info("initiating transfer for: {} without executor", toString());
		final List<ParallelPutTransferThread> parallelPutTransferThreads = new ArrayList<ParallelPutTransferThread>();
		localFile.length();
		ParallelPutTransferThread parallelTransferThread;

		for (int i = 0; i < numberOfThreads; i++) {

			parallelTransferThread = ParallelPutTransferThread.instance(this);

			parallelPutTransferThreads.add(parallelTransferThread);

			log.info("created transfer thread:{}", parallelTransferThread);

		}

		try {
			log.info("invoking executor threads for put");
			List<Future<ParallelTransferResult>> transferThreadStates = executor
					.invokeAll(parallelPutTransferThreads);

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
			log.error("an error occurred in a parallel put", e);
			throw new JargonException(e);
		}
	}
}
