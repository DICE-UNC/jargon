package org.irods.jargon.transfer.dao;

import java.util.List;

import org.irods.jargon.transfer.dao.domain.TransferAttempt;
import org.irods.jargon.transfer.dao.domain.TransferItem;
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

	/**
	 * Get list of files associated with the <code>TransferAttempt</code> this
	 * list is paged with start record, and number of records to be retrieved
	 * 
	 * @param transferAttemptId
	 *            <code>long</code> with the id of the
	 *            <code>TransferAttempt</code> that will be looked up
	 * @param start
	 *            <code>int</code> with the start index of the list of
	 *            <code>TransferItems</code> to return
	 * @param length
	 *            <code>int</code> with the max number of
	 *            <code>TransferItems</code> to return
	 * @return {@link TransferItems} list
	 * @throws TransferDAOException
	 */
	public List<TransferItem> listTransferItemsInTransferAttempt(
			final Long transferAttemptId, final int start, final int length)
			throws TransferDAOException;

	/**
	 * Do a load of the <code>TransferAttempt</code> to ensure that the object
	 * is associated with a session
	 * 
	 * @param id
	 * @return
	 * @throws TransferDAOException
	 */
	TransferAttempt load(Long id) throws TransferDAOException;

}
