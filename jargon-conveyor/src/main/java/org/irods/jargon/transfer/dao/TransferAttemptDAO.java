package org.irods.jargon.transfer.dao;

import java.util.List;

import org.irods.jargon.transfer.dao.domain.TransferAttempt;
import org.irods.jargon.transfer.dao.domain.TransferStatusEnum;

/**
 * 
 * @author lisa
 */
public interface TransferAttemptDAO {

	/**
	 * 
	 * @param ea
	 * @throws DAOException
	 */
	public void save(TransferAttempt ea) throws TransferDAOException;

	/**
	 * 
	 * @param id
	 * @return
	 * @throws DAOException
	 */
	public TransferAttempt findById(Long id) throws TransferDAOException;

	/**
	 * 
	 * @param ea
	 * @throws TransferDAOException
	 */
	public void delete(TransferAttempt ea) throws TransferDAOException;

	/**
	 * 
	 * @param maxResults
	 * @param transferStatus
	 * @return
	 * @throws TransferDAOException
	 */
	public List<TransferAttempt> findByTransferAttemptStatus(int maxResults,
			TransferStatusEnum... transferStatus) throws TransferDAOException;

	/**
	 * Find the last <code>TransferAttempt</code> (most recent) if it exists for
	 * the given transfer. If the transfer or transfer attempt do not exist
	 * <code>null</code> will be returned
	 * 
	 * @param transferId
	 *            <code>long</code> with the id of the <code>Transfer</code>
	 *            that will be looked up
	 * @return {@link TransferAttempt} that is the last attempt associated with
	 *         the <code>Transfer<code>, or <code>null</code>
	 * @throws TransferDAOException
	 */
	public TransferAttempt findLastTransferAttemptForTransferByTransferId(
			final long transferId) throws TransferDAOException;

}
