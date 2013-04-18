/**
 * 
 */
package org.irods.jargon.conveyor.basic;

import java.util.Date;
import java.util.List;

import org.irods.jargon.conveyor.core.AbstractConveyorCallable;
import org.irods.jargon.conveyor.core.AbstractConveyorComponentService;
import org.irods.jargon.conveyor.core.ConveyorBusyException;
import org.irods.jargon.conveyor.core.ConveyorExecutionException;
import org.irods.jargon.conveyor.core.ConveyorExecutionFuture;
import org.irods.jargon.conveyor.core.GridAccountService;
import org.irods.jargon.conveyor.core.QueueManagerService;
import org.irods.jargon.conveyor.core.callables.ConveyorCallableFactory;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.transfer.dao.TransferAttemptDAO;
import org.irods.jargon.transfer.dao.TransferDAO;
import org.irods.jargon.transfer.dao.domain.GridAccount;
import org.irods.jargon.transfer.dao.domain.Transfer;
import org.irods.jargon.transfer.dao.domain.TransferState;
import org.irods.jargon.transfer.dao.domain.TransferType;
import org.irods.jargon.transfer.dao.spring.TransferDAOImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * Basic implementation of a queue manager service
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
@Transactional
public class BasicQueueManagerServiceImpl extends
		AbstractConveyorComponentService implements QueueManagerService {

	/**
	 * Injected dependency
	 */
	private TransferDAO transferDAO;

	/**
	 * Injected dependency
	 */
	private TransferAttemptDAO transferAttemptDAO;

	/**
	 * Injected dependency
	 */
	private GridAccountService gridAccountService;

	private static final Logger log = LoggerFactory
			.getLogger(BasicQueueManagerServiceImpl.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.conveyor.core.QueueManagerService#enqueuePutOperation
	 * (java.lang.String, java.lang.String, java.lang.String,
	 * org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public void enqueuePutOperation(String sourceFileAbsolutePath,
			String targetFileAbsolutePath, String targetResource,
			IRODSAccount irodsAccount) throws ConveyorExecutionException {

		log.info("enqueuePutOperation()");

		if (sourceFileAbsolutePath == null || sourceFileAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty sourceFileAbsolutePath");
		}

		if (targetFileAbsolutePath == null || targetFileAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty targetFileAbsolutePath");
		}

		if (targetResource == null) {
			throw new IllegalArgumentException("null targetResource");
		}

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		log.info("sourceFileAbsolutePath:{}", sourceFileAbsolutePath);
		log.info("targetFileAbsolutePath:{}", targetFileAbsolutePath);
		log.info("targetResource:{}", targetResource);
		log.info("irodsAccount:{}", irodsAccount);

		log.info("looking up corresponding GridAccount...");
		GridAccount gridAccount = gridAccountService
				.findGridAccountByIRODSAccount(irodsAccount);
		if (gridAccount == null) {
			log.error("error finding grid account for irodsAccount:{}",
					irodsAccount);
			throw new ConveyorExecutionException(
					"unable to resolve gridAccount from given irodsAccount");
		}

		log.info("building transfer...");

		Transfer transfer = new Transfer();
		transfer.setCreatedAt(new Date());
		transfer.setGridAccount(gridAccount);
		transfer.setIrodsAbsolutePath(targetFileAbsolutePath);
		transfer.setLocalAbsolutePath(sourceFileAbsolutePath);
		transfer.setTransferState(TransferState.ENQUEUED);
		transfer.setTransferType(TransferType.PUT);
		transfer.setUpdatedAt(new Date());
		log.info("transfer added:{}", transfer);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.conveyor.core.QueueManagerService#dequeueNextOperation()
	 */
	@Override
	public void dequeueNextOperation() throws ConveyorExecutionException, JargonException, Exception {
		log.info("dequeueNextOperation()");

		synchronized (this) {
			try {
				this.getConveyorExecutorService().setBusyForAnOperation();
			} catch (ConveyorBusyException e) {
				log.info("busy, ignore..");
				return;
			}
		}

		// Transfer = transferDAO.getNextRunnableTransfer(); // or should this
		// be in queue manager? for right now just query for enqueued and sort
		// asc datetime
                TransferDAO transferDAO = new TransferDAOImpl();
                List<Transfer> transfers = transferDAO.findByTransferState(TransferState.ENQUEUED);
		//Transfer transfer = new Transfer(); // fake code for above

		if (transfers == null || transfers.isEmpty() || transfers.get(0) == null) {
			log.info("nothing to process...");
			this.getConveyorExecutorService().setOperationCompleted();
			return;
		}
                Transfer transfer = transfers.get(0);
		log.info("have transfer to run:{}", transfer);

		AbstractConveyorCallable callable = new ConveyorCallableFactory()
                        .instanceCallableForOperation(transfer, null); // FIXME: where do we get conveyorservice?
                
                ConveyorExecutionFuture call = callable.call();  // FIXME: throws Exception?

	}

	/**
	 * @return the transferDAO
	 */
	public TransferDAO getTransferDAO() {
		return transferDAO;
	}

	/**
	 * @param transferDAO
	 *            the transferDAO to set
	 */
	public void setTransferDAO(TransferDAO transferDAO) {
		this.transferDAO = transferDAO;
	}

	/**
	 * @return the transferAttemptDAO
	 */
	public TransferAttemptDAO getTransferAttemptDAO() {
		return transferAttemptDAO;
	}

	/**
	 * @param transferAttemptDAO
	 *            the transferAttemptDAO to set
	 */
	public void setTransferAttemptDAO(TransferAttemptDAO transferAttemptDAO) {
		this.transferAttemptDAO = transferAttemptDAO;
	}

	/**
	 * @return the gridAccountService
	 */
	public GridAccountService getGridAccountService() {
		return gridAccountService;
	}

	/**
	 * @param gridAccountService
	 *            the gridAccountService to set
	 */
	public void setGridAccountService(GridAccountService gridAccountService) {
		this.gridAccountService = gridAccountService;
	}

}
