/**
 * 
 */
package org.irods.jargon.transfer.engine;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.transfer.dao.domain.LocalIRODSTransfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>Runnable</code> meant to run in a thread, processing the given
 * transfer.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 */
public class TransferRunner implements Runnable {

	private final TransferManagerImpl transferManager;

	private final LocalIRODSTransfer localIRODSTransfer;

	private final TransferControlBlock transferControlBlock;

	private IRODSLocalTransferEngine irodsLocalTransferEngine = null;

	private final TransferQueueService transferQueueService;

	private static final Logger log = LoggerFactory
			.getLogger(TransferRunner.class);

	/**
	 * Constructs a process to transfer data to/from iRODS.
	 * 
	 * @param transferManager
	 *            {@link TransferManagerImpl} that coordinates the transfer of
	 *            data, and manages callbacks and notifications.
	 * @param localIRODSTransfer
	 *            {@link LocalIRODSTransfer} that contains data describing the
	 *            type of transfer and the status.
	 * @param transferControlBlock
	 *            {@link TransferControlBlock} maintains shared references to
	 *            data about the transfer between the client asking for the
	 *            transfer, and the transfer process.
	 * @param transferQueueService
	 *            {@link TransferQueueServiceImpl} that manages persistence and
	 *            access to the transfer data.
	 * @throws JargonException
	 */
	public TransferRunner(final TransferManagerImpl transferManager,
			final LocalIRODSTransfer localIRODSTransfer,
			final TransferControlBlock transferControlBlock,
			final TransferQueueService transferQueueService)
			throws JargonException {

		if (transferManager == null) {
			throw new JargonException("null transfer manager");
		}

		if (localIRODSTransfer == null) {
			throw new JargonException("null currentTransfer");
		}

		if (transferControlBlock == null) {
			throw new JargonException("null transferControlBlock");
		}

		if (transferQueueService == null) {
			throw new JargonException("null transferQueueService");
		}

		this.transferManager = transferManager;
		this.localIRODSTransfer = localIRODSTransfer;
		this.transferControlBlock = transferControlBlock;
		this.transferQueueService = transferQueueService;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		log.info("running transfer:{}", localIRODSTransfer);

		try {
			irodsLocalTransferEngine = IRODSLocalTransferEngine.instance(
					transferManager, transferControlBlock,
					transferManager.getTransferEngineConfigurationProperties());
		} catch (JargonException je) {
			handleErrorCreatingTransferEngineInstance(transferQueueService, je);
			irodsLocalTransferEngine.cleanUp();
			return;
		}

		try {
			// start the desired transfer by processing the first in the queue
			irodsLocalTransferEngine.processOperation(localIRODSTransfer);
			/*
			 * I have tried the transfer. If any errors had occurred, the best
			 * effort was made to log the error in the transfer database, and in
			 * case of error would have returned or thrown an exception. I can
			 * assume that the transfer went correctly and the database was
			 * updated in the irodsLocalTransferEngine. processPutOperation()
			 * method.
			 */
			processCompletionOfTransfer();
		} catch (Exception je) {
			log.error(
					"exception in run method when calling process operation, errors should have been passed back in callbacks",
					je);
			try {
				transferQueueService.markTransferAsErrorAndTerminate(
						localIRODSTransfer, je, transferManager);
			} catch (JargonException e) {
				log.error("error marking transfer as error", e);
			}
			notifyTransferManagerWhenCompletedWithError();
			return;
		} finally {
			 transferManager
				.getIrodsFileSystem().closeAndEatExceptions();
		}
	}

	/**
	 * 
	 */
	private void processCompletionOfTransfer() {
		log.info("processCompletionOfTransfer");
		try {
			transferManager.notifyComplete();
		} catch (JargonException e) {
			// ignored
		}
	}

	/**
	 * 
	 */
	private void notifyTransferManagerWhenCompletedWithError() {
		try {
			transferManager.notifyErrorCondition();
			transferManager.notifyComplete();
		} catch (Exception e) {
			// ignore
		}
	}

	/**
	 * @param transferQueueService
	 * @param je
	 * @throws JargonRuntimeException
	 */
	private void handleErrorCreatingTransferEngineInstance(
			final TransferQueueService transferQueueService,
			final JargonException je) throws JargonRuntimeException {
		log.error(
				"error attempting to get transfer engine and irods file service, which are prerequisites to processing a transfer.  Transfer not tried",
				je);
		try {
			transferQueueService.markTransferAsErrorAndTerminate(
					localIRODSTransfer, je, transferManager);
			notifyTransferManagerWhenCompletedWithError();
			return;
		} catch (Exception e) {
			log.error(
					"error marking transfer as an error, cannot update queue with this status",
					e);

			try {
				transferManager.notifyComplete();
			} catch (JargonException e1) {
				e1.printStackTrace();
			}

			throw new JargonRuntimeException(e);
		}
	}

	public final TransferManager getTransferManager() {
		return transferManager;
	}

	public final LocalIRODSTransfer getLocalIRODSTransfer() {
		return localIRODSTransfer;
	}

	public TransferControlBlock getTransferControlBlock() {
		return transferControlBlock;
	}

}
