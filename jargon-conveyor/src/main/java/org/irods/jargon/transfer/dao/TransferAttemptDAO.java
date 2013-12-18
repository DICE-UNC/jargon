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

	/**
	 * Do a pageable listing of items, allowing selection of the items to show
	 * by classification. <br/>
	 * If <code>showSucces</code> is true, then successes AND errors are
	 * displayed, this is a 'list all' setting. This may be further refined by
	 * setting <code>showSkipped</code>, which, when true, will show any files
	 * skipped in the attempt, because of restarting. <br/>
	 * Note that if <code>showSuccess</code> is false, then skipped files are
	 * also not shown. This will result in a listing of just error transfer
	 * items.
	 * 
	 * @param transferAttemptId
	 * @param transferAttemptId
	 *            <code>long</code> with the id of the
	 *            <code>TransferAttempt</code> that will be looked up
	 * @param start
	 *            <code>int</code> with the start index of the list of
	 *            <code>TransferItems</code> to return
	 * @param length
	 *            <code>int</code> with the max number of
	 *            <code>TransferItems</code> to return
	 * @param showSuccess
	 *            <code>boolean</code> that, when true, will show all items,
	 *            including errors. When set to false, only error items are
	 *            returned.
	 * @param showSkipped
	 *            <code>boolean</code> that, when true, will show items skipped
	 *            during a restart. When <code>showSuccess</code> is false, this
	 *            will have no effect
	 * @return {@link TransferItems} list
	 * @throws TransferDAOException
	 */
	List<TransferItem> listTransferItemsInTransferAttempt(
			Long transferAttemptId, int start, int length, boolean showSuccess,
			boolean showSkipped) throws TransferDAOException;

}
