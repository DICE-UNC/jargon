package org.irods.jargon.transfer.dao;

import java.util.List;

import org.irods.jargon.transfer.dao.domain.Transfer;
import org.irods.jargon.transfer.dao.domain.TransferStateEnum;
import org.irods.jargon.transfer.dao.domain.TransferStatusEnum;

/**
 * 
 * @author jdr0887
 * 
 */
public interface TransferDAO {

	/**
	 * 
	 * @param ea
	 * @throws DAOException
	 */
	public void save(Transfer ea) throws TransferDAOException;

	/**
	 * 
	 * @param id
	 * @return
	 * @throws DAOException
	 */
	public Transfer findById(Long id) throws TransferDAOException;

	/**
	 * 
	 * @param id
	 * @return
	 * @throws TransferDAOException
	 */
	public Transfer findInitializedById(Long id) throws TransferDAOException;

	/**
	 * 
	 * @param transferState
	 * @return
	 * @throws TransferDAOException
	 */
	public List<Transfer> findByTransferState(
			TransferStateEnum... transferState) throws TransferDAOException;

	/**
	 * 
	 * @param maxResults
	 * @return
	 * @throws TransferDAOException
	 */
	public List<Transfer> findAllSortedDesc(int maxResults)
			throws TransferDAOException;

	/**
	 * 
	 * @param maxResults
	 * @return
	 * @throws TransferDAOException
	 */
	public List<Transfer> findAll() throws TransferDAOException;

	/**
	 * 
	 * @param maxResults
	 * @param transferState
	 * @return
	 * @throws TransferDAOException
	 */
	public List<Transfer> findByTransferState(int maxResults,
			TransferStateEnum... transferState) throws TransferDAOException;

	/**
	 * 
	 * @param maxResults
	 * @param transferStatus
	 * @return
	 * @throws TransferDAOException
	 */
	public List<Transfer> findByTransferStatus(int maxResults,
			TransferStatusEnum... transferStatus) throws TransferDAOException;

	/**
	 * 
	 * @param notIn
	 * @param transferState
	 * @throws TransferDAOException
	 */
	public void purgeQueue() throws TransferDAOException;

	/**
	 * 
	 * @throws TransferDAOException
	 */
	public void purgeSuccessful() throws TransferDAOException;

	/**
	 * 
	 * @param ea
	 * @throws TransferDAOException
	 */
	public void delete(Transfer ea) throws TransferDAOException;

	/**
	 * Delete the entire contents of the queue, no matter what the status is
	 * 
	 * @throws TransferDAOException
	 */
	void purgeEntireQueue() throws TransferDAOException;

	/**
	 * Initialize lazy-loaded attempts and attempt items. This is a convenience
	 * method to initialize lazily-loaded child collections, note that some of
	 * these collections can be very large!
	 * 
	 * @param transfer
	 *            {@link Transfer} that will be re-attached to a session via
	 *            merge, and then initialized via Hibernate
	 * @throws TransferDAOException
	 */
	Transfer initializeChildrenForTransfer(Transfer transfer)
			throws TransferDAOException;

	/**
	 * Do a merge of the transfer
	 * 
	 * @param transfer
	 *            {@link Transfer} that will be re-attached to a session via
	 *            merge, and then initialized via Hibernate
	 * @throws TransferDAOException
	 */
	void merge(Transfer transfer) throws TransferDAOException;

	/**
	 * Load the transfer to ensure it is attached
	 * 
	 * @param id
	 * @return
	 * @throws TransferDAOException
	 */
	Transfer load(Long id) throws TransferDAOException;

}
