/**
 * 
 */
package org.irods.jargon.conveyor.basic;

import org.irods.jargon.conveyor.core.AbstractConveyorComponentService;
import org.irods.jargon.conveyor.core.GridAccountService;
import org.irods.jargon.conveyor.core.TransferAccountingManagementService;
import org.irods.jargon.transfer.dao.TransferAttemptDAO;
import org.irods.jargon.transfer.dao.TransferDAO;
import org.irods.jargon.transfer.dao.TransferItemDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of a service to manage transfers and update them as they
 * execute, based on callbacks and conveyor actions.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
@Transactional
public class TransferAccountingManagementServiceImpl extends
		AbstractConveyorComponentService implements
		TransferAccountingManagementService {

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
	private TransferItemDAO transferItemDAO;

	/**
	 * Injected dependency
	 */
	private GridAccountService gridAccountService;

	private static final Logger log = LoggerFactory
			.getLogger(TransferAccountingManagementServiceImpl.class);

	/**
	 * 
	 */
	public TransferAccountingManagementServiceImpl() {
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
	 * @return the transferItemDAO
	 */
	public TransferItemDAO getTransferItemDAO() {
		return transferItemDAO;
	}

	/**
	 * @param transferItemDAO
	 *            the transferItemDAO to set
	 */
	public void setTransferItemDAO(TransferItemDAO transferItemDAO) {
		this.transferItemDAO = transferItemDAO;
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
