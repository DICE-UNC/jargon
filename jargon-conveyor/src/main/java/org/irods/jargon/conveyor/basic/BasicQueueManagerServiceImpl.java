/**
 * 
 */
package org.irods.jargon.conveyor.basic;

import java.util.Date;

import org.irods.jargon.conveyor.core.AbstractConveyorComponentService;
import org.irods.jargon.conveyor.core.ConveyorExecutionException;
import org.irods.jargon.conveyor.core.GridAccountService;
import org.irods.jargon.conveyor.core.QueueManagerService;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.transfer.dao.TransferAttemptDAO;
import org.irods.jargon.transfer.dao.TransferDAO;
import org.irods.jargon.transfer.dao.domain.GridAccount;
import org.irods.jargon.transfer.dao.domain.Transfer;
import org.irods.jargon.transfer.dao.domain.TransferState;
import org.irods.jargon.transfer.dao.domain.TransferType;
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
	public void dequeueNextOperation() throws ConveyorExecutionException {
		// FIXME: implement!
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
