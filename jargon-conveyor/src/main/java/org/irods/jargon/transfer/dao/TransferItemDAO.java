package org.irods.jargon.transfer.dao;

import java.util.List;

import org.irods.jargon.transfer.dao.domain.TransferItem;

/**
 * 
 * @author jdr0887
 * 
 */
public interface TransferItemDAO {

	/**
	 * 
	 * @param ea
	 * @throws DAOException
	 */
	public void save(TransferItem ea) throws TransferDAOException;

	/**
	 * 
	 * @param id
	 * @return
	 * @throws DAOException
	 */
	public TransferItem findById(Long id) throws TransferDAOException;

	/**
	 * 
	 * @param id
	 * @return
	 * @throws TransferDAOException
	 */
	public List<TransferItem> findErrorItemsByTransferAttemptId(Long id)
			throws TransferDAOException;

	/**
	 * 
	 * @param ea
	 * @throws TransferDAOException
	 */
	public void delete(TransferItem ea) throws TransferDAOException;

	/**
	 * 
	 * @param transferId
	 * @return
	 */
	public List<TransferItem> findAllItemsForTransferByTransferAttemptId(
			Long transferId) throws TransferDAOException;
        
}
