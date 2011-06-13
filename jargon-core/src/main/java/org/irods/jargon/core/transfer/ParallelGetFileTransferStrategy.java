/**
 * 
 */
package org.irods.jargon.core.transfer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.irods.jargon.core.exception.JargonException;
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
	 * @return
	 * @throws JargonException
	 */
	public static ParallelGetFileTransferStrategy instance(final String host,
			final int port, final int numberOfThreads, final int password,
			final File localFile,
			final IRODSAccessObjectFactory irodsAccessObjectFactory)
			throws JargonException {
		return new ParallelGetFileTransferStrategy(host, port, numberOfThreads,
				password, localFile, irodsAccessObjectFactory);
	}

	private ParallelGetFileTransferStrategy(final String host, final int port,
			final int numberOfThreads, final int password,
			final File localFile,
			final IRODSAccessObjectFactory irodsAccessObjectFactory)
			throws JargonException {

		super(host, port, numberOfThreads, password, localFile,
				irodsAccessObjectFactory);

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
			log.info("no pool available, transfer using standard Threads");
			transferWithoutExecutor();
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

	/**
	 * @throws JargonException
	 */
	private void transferWithoutExecutor() throws JargonException {
		final List<Thread> transferRunningThreads = new ArrayList<Thread>();
		final List<ParallelGetTransferThread> parallelGetTransferThreads = new ArrayList<ParallelGetTransferThread>();

		for (int i = 0; i < numberOfThreads; i++) {
			final ParallelGetTransferThread parallelTransfer = ParallelGetTransferThread
					.instance(this);
			parallelGetTransferThreads.add(parallelTransfer);

			Thread parallelTransferThread = new Thread(parallelTransfer);
			log.info("created parallel transfer thread for thread: {}",
					parallelTransferThread.getName());
			transferRunningThreads.add(parallelTransferThread);
		}

		for (Thread parallelTransferThreadToStart : transferRunningThreads) {
			parallelTransferThreadToStart.start();
			log.info("started parallel transfer thread for thread: {}",
					parallelTransferThreadToStart.getName());
		}

		for (Thread parallelTransferThreadToJoin : transferRunningThreads) {
			if (parallelTransferThreadToJoin.isAlive()) {
				try {
					parallelTransferThreadToJoin.join();
				} catch (InterruptedException e) {
					log.error(
							"parallel transfer thread {} was interrupted when attempting to join",
							parallelTransferThreadToJoin.getName(), e);
					throw new JargonException(
							"parallel transfer thread interrupted when attempting to join");
				}
			}

		}

		log.info("closing threads...");

		for (ParallelGetTransferThread parallelGetTransferThread : parallelGetTransferThreads) {
			parallelGetTransferThread.close();
		}

		log.info("parallel transfer complete, checking for any errors in the threads...");

		for (ParallelGetTransferThread parallelGetTransferThread : parallelGetTransferThreads) {
			if (parallelGetTransferThread.getExceptionInTransfer() != null) {
				log.error("exeption detected in file transfer thread:{}",
						parallelGetTransferThread.getExceptionInTransfer());
				throw new JargonException(
						"exception caught in transfer thread",
						parallelGetTransferThread.getExceptionInTransfer());
			}
		}
	}

}
