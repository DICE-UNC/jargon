/**
 * 
 */
package org.irods.jargon.core.transfer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.irods.jargon.core.exception.JargonException;
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
	 * @return
	 * @throws JargonException
	 */
	public static ParallelPutFileTransferStrategy instance(final String host,
			final int port, final int numberOfThreads, final int password,
			final File localFile,
			final IRODSAccessObjectFactory irodsAccessObjectFactory)
			throws JargonException {
		return new ParallelPutFileTransferStrategy(host, port, numberOfThreads,
				password, localFile, irodsAccessObjectFactory);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ParallelPutFileTransferStrategy");
		sb.append("\n   host:");
		sb.append(this.getHost());
		sb.append("\n   port:");
		sb.append(this.getPort());
		sb.append("\n   numberOfThreads:");
		sb.append(this.getNumberOfThreads());
		sb.append("\n   localFile:");
		sb.append(localFile.getAbsolutePath());
		return sb.toString();

	}

	private ParallelPutFileTransferStrategy(final String host, final int port,
			final int numberOfThreads, final int password,
			final File localFile,
			final IRODSAccessObjectFactory irodsAccessObjectFactory)
			throws JargonException {
		super(host, port, numberOfThreads, password, localFile,
				irodsAccessObjectFactory);
	}

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
	}

	private void transferWithExecutor(final ExecutorService executor)
			throws JargonException {
		log.info("initiating transfer for: {} without executor",
				this.toString());
		final List<ParallelPutTransferThread> parallelPutTransferThreads = new ArrayList<ParallelPutTransferThread>();
		final long localFileLength = localFile.length();
		final long transferLength = localFileLength / numberOfThreads;

		ParallelPutTransferThread parallelTransferThread;

		for (int i = 0; i < numberOfThreads - 1; i++) {

			parallelTransferThread = ParallelPutTransferThread.instance(this,
					transferLength, transferLength * i);

			parallelPutTransferThreads.add(parallelTransferThread);

			log.info("created transfer thread:{}", parallelTransferThread);

		}
		// last thread is a little different
		parallelTransferThread = ParallelPutTransferThread
				.instance(this,localFileLength - transferLength
						* (numberOfThreads - 1), // length
					transferLength * (numberOfThreads - 1) // offset
				);

		parallelPutTransferThreads.add(parallelTransferThread);

		try {
			log.info("invoking executor threads for put");
			List<Future<Object>> transferThreadStates = executor.invokeAll(parallelPutTransferThreads);
			
			if (log.isInfoEnabled()) {
				for (Future<Object> transferState : transferThreadStates) {
					log.info("transfer state:{}", transferState);
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

	public void transferWithoutExecutor() throws JargonException {
		log.info("initiating transfer for: {} without executor",
				this.toString());
		final List<Thread> transferRunningThreads = new ArrayList<Thread>();
		final List<ParallelPutTransferThread> parallelPutTransferThreads = new ArrayList<ParallelPutTransferThread>();
		final long localFileLength = localFile.length();
		final long transferLength = localFileLength / numberOfThreads;


		for (int i = 0; i < numberOfThreads - 1; i++) {

			ParallelPutTransferThread  parallelTransferThread = ParallelPutTransferThread.instance(this,
					transferLength, transferLength * i);

			transferRunningThreads.add(new Thread(parallelTransferThread));
			parallelPutTransferThreads.add(parallelTransferThread);

			log.info("creating transfer thread:{}", parallelTransferThread);

		}
		// last thread is a little different
		ParallelPutTransferThread  parallelTransferThread = ParallelPutTransferThread
				.instance(this, (int) (localFileLength - transferLength
						* (numberOfThreads - 1)), // length
						transferLength * (numberOfThreads - 1) // offset
				);

		transferRunningThreads.add(new Thread(parallelTransferThread));
		parallelPutTransferThreads.add(parallelTransferThread);

		log.info("creating last transfer thread{}", parallelTransferThread);

		for (Thread parallelTransferThreadToStart : transferRunningThreads) {
			parallelTransferThreadToStart.start();
			log.info("started parallel transfer thread for thread: {}",
					parallelTransferThreadToStart.getName());
		}

		for (Thread parallelTransferThreadToJoin : transferRunningThreads) {

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

		log.info("closing threads");

		for (ParallelPutTransferThread parallelPutTransferThread : parallelPutTransferThreads) {
			parallelPutTransferThread.close();
		}

		log.info("parallel transfer complete...checking for any exceptions that occurred in each thread");

		log.info("parallel transfer complete, checking for any errors in the threads...");

		for (ParallelPutTransferThread parallelPutTransferThread : parallelPutTransferThreads) {
			if (parallelPutTransferThread.getExceptionInTransfer() != null) {
				log.error("exeption detected in file transfer thread:{}",
						parallelPutTransferThread.getExceptionInTransfer());
				throw new JargonException(
						"exception caught in transfer thread",
						parallelPutTransferThread.getExceptionInTransfer());
			}
		}

	}

}
