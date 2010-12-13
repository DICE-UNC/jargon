package org.irods.jargon.transferengine;

import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.transferengine.domain.LocalIRODSTransfer;
import org.irods.jargon.transferengine.domain.LocalIRODSTransferItem;
import org.irods.jargon.transferengine.util.HibernateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage the transfer queue and display of status of transfers. This
 * thread-safe object is meant to be a singleton and manages processing of a
 * transfer queue on behalf of the <code>TransferManager</code>
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
final class TransferQueueService {

	private static final Logger log = LoggerFactory
			.getLogger(TransferQueueService.class);
	
	private final HibernateUtil hibernateUtil;

	/**
	 * Static initializer for a transfer queue service using default configuration.
	 * @return <code>TransferQueueService</code> instance.
	 */
	public static final TransferQueueService instance() throws JargonException {
		return new TransferQueueService();
	}

	/**
	 * Static initializer for an instance that will use a transfer database at a given location.  This is useful when the database should be
	 * kept in a user directory.
	 * @param pathToTransferDatabase <code>String</code> giving the path to the transfer database such that it can be used in the
	 * JDBC database URL.
	 * @throws JargonException
	 */
	public static final TransferQueueService instanceGivingPathToTransferDatabase(final String pathToTransferDatabase) throws JargonException {
		return new TransferQueueService(pathToTransferDatabase);
	}
	
	/**
	 * Create the transferQueueService using database defaults in the Hibernate config.
	 * @throws JargonException
	 */
	private TransferQueueService() throws JargonException {
		hibernateUtil = HibernateUtil.instanceUsingDefaultConfig();
	}
	
	/**
	 * Create an instance that will use a transfer database at a given location.  This is useful when the database should be
	 * kept in a user directory.
	 * @param pathToTransferDatabase <code>String</code> giving the path to the transfer database such that it can be used in the
	 * JDBC database URL.
	 * @throws JargonException
	 */
	private TransferQueueService(final String pathToTransferDatabase) throws JargonException {
		// param checks done in hibernateUtil.
		hibernateUtil = HibernateUtil.instanceGivingPathToDatabase(pathToTransferDatabase);
	}

	@SuppressWarnings("unchecked")
	protected LocalIRODSTransfer dequeueTransfer() throws JargonException {

		String queryStringMaster = "from LocalIRODSTransfer transfer  where transfer.transferState = 'ENQUEUED' or transfer.transferState = 'PROCESSING' or transfer.transferState = 'PAUSED' order by transfer.transferStart desc";

		log.info("getting hibernate session factory and opening session");
		Session session = hibernateUtil.getSession();

		Transaction tx = null;
		LocalIRODSTransfer transfer;
		try {
			log.info("beginning tx");
			tx = session.beginTransaction();
			Query q = session.createQuery(queryStringMaster);

			List<LocalIRODSTransfer> irodsTransferQueue = q.list();
			if (irodsTransferQueue.size() == 0) {
				log.info("no transfers in queue, return null");
				tx.commit();
				return null;
			}

			transfer = irodsTransferQueue.get(0);
			log.info("dequeue transfer:{}", transfer);

			// FIXME: decide whether a restart and add to control block

			transfer.setTransferStart(new Date());
			transfer
					.setTransferState(LocalIRODSTransfer.TRANSFER_STATE_PROCESSING);
			transfer
					.setTransferErrorStatus(LocalIRODSTransfer.TRANSFER_STATUS_OK);
			session.update(transfer);
			//session.flush();
			tx.commit();

		} catch (RuntimeException e) {

			if (tx != null)
				tx.rollback();

			log.error("error in transaction", e);
			throw new JargonException(e);
		} finally {
			//session.close();
			log.info("session closed");
		}

		log.info("dequeued");
		return transfer;

	}

	/**
	 * Add a put transfer to the queue. This causes the entry to be made with a
	 * status of enqueued. The entry into the database does not, however,
	 * automatically cause the transfer to begin, that is done by the
	 * {@link org.irods.jargon.transferengine.TransferManager}.
	 * 
	 * @param localSourceAbsolutePath
	 * @param targetIRODSAbsolutePath
	 * @param targetResource
	 * @param irodsAccount
	 * @return
	 * @throws JargonException
	 */
	protected LocalIRODSTransfer enqueuePutTransfer(
			final String localSourceAbsolutePath,
			final String targetIRODSAbsolutePath, final String targetResource,
			final IRODSAccount irodsAccount) throws JargonException {

		if (localSourceAbsolutePath == null
				|| localSourceAbsolutePath.isEmpty()) {
			throw new JargonException(
					"localSourceAbsolutePath is null or empty");
		}

		if (targetIRODSAbsolutePath == null
				|| targetIRODSAbsolutePath.isEmpty()) {
			throw new JargonException(
					"targetIRODSAbsolutePath is null or empty");
		}

		if (targetResource == null) {
			throw new JargonException(
					"targetResource is null, set as blank if not used");
		}

		if (irodsAccount == null) {
			throw new JargonException("null irodsAccount");
		}

		log.info("enqueue put transfer from local source: {}",
				localSourceAbsolutePath);
		log.info("   target iRODS path: {}", targetIRODSAbsolutePath);
		log.info("   target resource:{}", targetResource);

		log.info("getting hibernate session factory and opening session");
		final Session session = hibernateUtil.getSession();

		Transaction tx = null;
		final LocalIRODSTransfer enqueuedTransfer;
		try {
			log.info("beginning tx");
			tx = session.beginTransaction();
			enqueuedTransfer = new LocalIRODSTransfer();
			enqueuedTransfer.setCreatedAt(new Date());
			enqueuedTransfer.setIrodsAbsolutePath(targetIRODSAbsolutePath);
			enqueuedTransfer.setLocalAbsolutePath(localSourceAbsolutePath);
			enqueuedTransfer.setTransferHost(irodsAccount.getHost());
			enqueuedTransfer.setTransferPort(irodsAccount.getPort());
			enqueuedTransfer.setTransferResource(targetResource);
			enqueuedTransfer.setTransferZone(irodsAccount.getZone());
			enqueuedTransfer.setTransferStart(new Date());
			enqueuedTransfer.setTransferType("PUT");
			enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
			enqueuedTransfer.setTransferPassword(HibernateUtil
					.obfuscate(irodsAccount.getPassword()));
			enqueuedTransfer
					.setTransferState(LocalIRODSTransfer.TRANSFER_STATE_ENQUEUED);
			enqueuedTransfer
					.setTransferErrorStatus(LocalIRODSTransfer.TRANSFER_STATUS_OK);
			log.info("saving...{}", enqueuedTransfer);

			session.saveOrUpdate(enqueuedTransfer);
			//session.flush();
			log.info("commit");
			tx.commit();
		} catch (RuntimeException e) {

			if (tx != null)
				tx.rollback();

			log.error("error in transaction", e);
			throw new JargonException(e);
		} finally {
			//session.close();
			log.info("session closed");
		}

		log.info("enqueued...");
		return enqueuedTransfer;

	}

	/**
	 * Add a get transfer to the queue. This causes the entry to be made with a
	 * status of enqueued. The entry into the database does not, however,
	 * automatically cause the transfer to begin, that is done by the
	 * {@link org.irods.jargon.transferengine.TransferManager}.
	 * 
	 * @param irodsSourceAbsolutePath
	 * @param targetLocalAbsolutePath
	 * @param sourceResource
	 * @param irodsAccount
	 * @return
	 * @throws JargonException
	 */
	protected LocalIRODSTransfer enqueueGetTransfer(
			final String irodsSourceAbsolutePath,
			final String targetLocalAbsolutePath, final String sourceResource,
			final IRODSAccount irodsAccount) throws JargonException {

		if (irodsSourceAbsolutePath == null
				|| irodsSourceAbsolutePath.isEmpty()) {
			throw new JargonException(
					"irodsSourceAbsolutePath is null or empty");
		}

		if (targetLocalAbsolutePath == null
				|| targetLocalAbsolutePath.isEmpty()) {
			throw new JargonException(
					"targetLocalAbsolutePath is null or empty");
		}

		if (sourceResource == null) {
			throw new JargonException(
					"sourceResource is null, set as blank if not used");
		}

		if (irodsAccount == null) {
			throw new JargonException("null irodsAccount");
		}

		log.info("enqueue get transfer from irods source: {}",
				irodsSourceAbsolutePath);
		log.info("   target local path: {}", targetLocalAbsolutePath);
		log.info("   target resource:{}", sourceResource);

		log.info("getting hibernate session factory and opening session");
		final Session session = hibernateUtil.getSession();

		Transaction tx = null;
		final LocalIRODSTransfer enqueuedTransfer;
		try {
			log.info("beginning tx");
			tx = session.beginTransaction();
			enqueuedTransfer = new LocalIRODSTransfer();
			enqueuedTransfer.setCreatedAt(new Date());
			enqueuedTransfer.setIrodsAbsolutePath(irodsSourceAbsolutePath);
			enqueuedTransfer.setLocalAbsolutePath(targetLocalAbsolutePath);
			enqueuedTransfer.setTransferHost(irodsAccount.getHost());
			enqueuedTransfer.setTransferPort(irodsAccount.getPort());
			enqueuedTransfer.setTransferResource(sourceResource);
			enqueuedTransfer.setTransferZone(irodsAccount.getZone());
			enqueuedTransfer.setTransferStart(new Date());
			enqueuedTransfer.setTransferType("GET");
			enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
			enqueuedTransfer.setTransferPassword(HibernateUtil
					.obfuscate(irodsAccount.getPassword()));
			enqueuedTransfer
					.setTransferState(LocalIRODSTransfer.TRANSFER_STATE_ENQUEUED);
			enqueuedTransfer
					.setTransferErrorStatus(LocalIRODSTransfer.TRANSFER_STATUS_OK);
			log.info("saving...{}", enqueuedTransfer);

			session.saveOrUpdate(enqueuedTransfer);
			//session.flush();
			log.info("commit");
			tx.commit();
		} catch (RuntimeException e) {

			if (tx != null)
				tx.rollback();

			log.error("error in transaction", e);
			throw new JargonException(e);
		} finally {
			//session.close();
			log.info("session closed");
		}

		log.info("enqueued...");
		return enqueuedTransfer;

	}

	/**
	 * Mark the given transfer as an error in the transfer database.
	 * 
	 * @param localIRODSTransfer
	 *            {@link org.irods.jargon.transferengine.domain.LocalIRODSTransfer}
	 *            that will be updated to reflect the error
	 * @param transferManager
	 *            <code>TransferManager</code> that controls the transfers
	 * @throws JargonException
	 */
	protected void markTransferAsErrorAndTerminate(
			final LocalIRODSTransfer localIRODSTransfer,
			final TransferManager transferManager) throws JargonException {
		markTransferAsErrorAndTerminate(localIRODSTransfer, null,
				transferManager);
	}

	/**
	 * Mark the given transfer as an error in the transfer database, incluing
	 * information from any exception passed to this method. This method will
	 * accept a null <code>errorException</code> parameter and will just ignore
	 * that data.
	 * 
	 * @param localIRODSTransfer
	 *            {@link org.irods.jargon.transferengine.domain.LocalIRODSTransfer}
	 *            that will be updated to reflect the error
	 * @param errorException
	 *            <code>Exception</code> that indicates a global error for the
	 *            transfer. This is opposed to an exception that was thrown on
	 *            an individual file transfer, and indicates a more general
	 *            issue with the entire transfer.
	 * @param transferManager
	 *            <code>TransferManager</code> that controls the transfers
	 * 
	 * @throws JargonException
	 */
	protected void markTransferAsErrorAndTerminate(
			final LocalIRODSTransfer localIRODSTransfer,
			final Exception errorException,
			final TransferManager transferManager) throws JargonException {
		log.info("getting hibernate session factory and opening session");
		Session session = hibernateUtil.getSession();

		Transaction tx = null;

		try {
			log.info("beginning tx");
			tx = session.beginTransaction();
			final LocalIRODSTransfer mergedTransfer = (LocalIRODSTransfer) session
					.load(LocalIRODSTransfer.class, localIRODSTransfer.getId());
			mergedTransfer.setTransferEnd(new Date());
			mergedTransfer
					.setTransferState(LocalIRODSTransfer.TRANSFER_STATE_COMPLETE);
			mergedTransfer
					.setTransferErrorStatus(LocalIRODSTransfer.TRANSFER_STATUS_ERROR);

			if (errorException != null) {
				log.warn("setting global exception to:{}", errorException);
				mergedTransfer.setGlobalException(errorException.getMessage());
			}

			log.info("saving as error{}", mergedTransfer);

			session.update(mergedTransfer);
			log.info("commit");
			//session.flush();
			tx.commit();
		} catch (RuntimeException e) {

			if (tx != null)
				tx.rollback();

			log.error("error in transaction", e);
			throw new JargonException(e);
		} finally {
			notifyManagerOfError(transferManager);
			//session.close();
			log.info("session closed");
		}

	}

	/**
	 * @param transferManager
	 */
	private void notifyManagerOfError(final TransferManager transferManager) {
		try {
			transferManager.notifyErrorCondition();
		} catch (JargonException e) {
			// ignore
		}
	}

	/**
	 * Get a list of the recent transfer queue (all states), using the row count
	 * desired.
	 * 
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.transferengine.domain.LocalIRODSTransfer}
	 *         contining queue items of any status
	 * @throws JargonException
	 */
	protected List<LocalIRODSTransfer> getLastNInQueue(
			final int countOfEntriesToShow) throws JargonException {
		if (countOfEntriesToShow <= 0) {
			throw new JargonException("must show at least 1 entry");
		}

		final String queryString = "from LocalIRODSTransfer transfer order by transfer.transferStart desc";
		return processQueryOfQueue(queryString, countOfEntriesToShow);
	}

	/**
	 * Get a list of the current transfer queue (enqueued or processing
	 * transfers). This will default to a max of 80 rows.
	 * 
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.transferengine.domain.LocalIRODSTransfer}
	 *         contining queue items
	 * @throws JargonException
	 */
	protected List<LocalIRODSTransfer> getCurrentQueue() throws JargonException {

		final String queryString = "from LocalIRODSTransfer transfer where transfer.transferState = 'ENQUEUED' or transfer.transferState = 'PROCESSING' or transfer.transferState = 'PAUSED' order by transfer.transferStart desc";
		return processQueryOfQueue(queryString, 80);

	}

	/**
	 * Get a list of the current transfer queue that show errors. This will
	 * default to a max of 80 rows.
	 * 
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.transferengine.domain.LocalIRODSTransfer}
	 *         contining queue items
	 * @throws JargonException
	 */
	protected List<LocalIRODSTransfer> getErrorQueue() throws JargonException {

		final String queryString = "from LocalIRODSTransfer transfer where transfer.transferErrorStatus = 'ERROR' order by transfer.transferStart desc";
		return processQueryOfQueue(queryString, 80);

	}

	/**
	 * Get a list of the current transfer queue that show warnings. This will
	 * default to a max of 80 rows.
	 * 
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.transferengine.domain.LocalIRODSTransfer}
	 *         contining queue items
	 * @throws JargonException
	 */
	protected List<LocalIRODSTransfer> getWarningQueue() throws JargonException {

		final String queryString = "from LocalIRODSTransfer transfer where transfer.transferErrorStatus = 'WARNING' order by transfer.transferStart desc";
		return processQueryOfQueue(queryString, 80);

	}

	/**
	 * Get a list of the most recent transfers. This will default to a max of 80
	 * rows.
	 * 
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.transferengine.domain.LocalIRODSTransfer}
	 *         contining queue items
	 * @throws JargonException
	 */
	protected List<LocalIRODSTransfer> getRecentQueue() throws JargonException {

		return getLastNInQueue(80);

	}

	/**
	 * Internal common method to query and get <code>LocalIRODSTransfer</code>
	 * objects that match the query.
	 * 
	 * @param queryString
	 *            <code>String<code> with the HQL query that must return <code>LocalIRODSTransfer</code>
	 *            objects.
	 * @param rowCount
	 *            <code>int</code> with the desired row count, or 0 if no row
	 *            limit desired.
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.transferengine.domain.LocalIRODSTransfer}
	 * @throws JargonException
	 */
	@SuppressWarnings("unchecked")
	private List<LocalIRODSTransfer> processQueryOfQueue(
			final String queryString, final int rowCount)
			throws JargonException {
		
		if (queryString == null || queryString.isEmpty()) {
			throw new JargonException("queryString is null or empty");
		}

		if (rowCount < 0) {
			throw new JargonException(
					"row count should be set to 0 if no count limit desired");
		}

		log.info("getting transfer queue using query:{}", queryString);
		final Session session = hibernateUtil.getSession();

		Transaction tx = null;

		try {
			log.info("beginning tx to store status of this transfer status");
			tx = session.beginTransaction();

			final Query q = session.createQuery(queryString);

			if (rowCount > 0) {
				log.info("setting row count for this query to:{}", rowCount);
				q.setMaxResults(rowCount);
			}

			final List<LocalIRODSTransfer> irodsTransferQueue = q.list();
			//session.flush();
			tx.commit();

			return irodsTransferQueue;

		} catch (RuntimeException e) {

			if (tx != null)
				tx.rollback();

			log.error("error in transaction", e);
			throw new JargonException(e);
		} finally {
			//session.close();
			log.info("session closed");
		}

	}

	/**
	 * Purge all non-processing transfers from the queue
	 * 
	 * @throws JargonException
	 */
	protected void purgeQueue() throws JargonException {

		log.info("purging the queue of all items (except a processing item");
		final String queryString = "delete from LocalIRODSTransfer as transfer where transfer.transferState != 'PROCESSING'";

		final Session session = hibernateUtil.getSession();
		Transaction tx = null;

		try {
			log.info("beginning tx to delete transfer queue");
			tx = session.beginTransaction();
			Query query = session.createQuery(queryString);
			query.executeUpdate();
			//session.flush();
			tx.commit();

		} catch (RuntimeException e) {

			if (tx != null)
				tx.rollback();

			log.error("error in transaction", e);
			throw new JargonException(e);
		} finally {
			//session.close();
			log.info("session closed");
		}

	}

	protected void purgeSuccessful() throws JargonException {
		log.info("purging the queue of all complete items");
		final String queryString = "delete from LocalIRODSTransfer as transfer where transfer.transferState = 'COMPLETE' or transfer.transferState = 'CANCELLED' and transfer.transferErrorStatus = 'OK'";

		final Session session = hibernateUtil.getSession();
		Transaction tx = null;

		try {
			log.info("beginning tx to delete transfer queue");
			tx = session.beginTransaction();
			Query query = session.createQuery(queryString);
			query.executeUpdate();
			//session.flush();
			tx.commit();

		} catch (RuntimeException e) {

			if (tx != null)
				tx.rollback();

			log.error("error in transaction", e);
			throw new JargonException(e);
		} finally {
			//session.close();
			log.info("session closed");
		}
	}

	protected List<LocalIRODSTransfer> showErrorTransfers()
			throws JargonException {
		final String queryString = "from LocalIRODSTransfer transfer where transfer.transferErrorStatus = 'ERROR' order by transfer.transferStart desc";
		return processQueryOfQueue(queryString, 80);
	}

	protected List<LocalIRODSTransfer> showWarningTransfers()
			throws JargonException {
		final String queryString = "from LocalIRODSTransfer transfer where transfer.transferErrorStatus = 'WARNING' order by transfer.transferStart desc";
		return processQueryOfQueue(queryString, 80);
	}

	/**
	 * Get a list of all of the transfer items for the given transfer. This
	 * particular method will return all transfer items, and other methods exist
	 * to filter for errors and other attributes.
	 * 
	 * @param localIRODSTransferId
	 *            <code>Long</code> with the key of the given transfer
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.transferengine.LocalIRODSTransferItem}
	 *         with query results.
	 * @throws JargonException
	 */
	@SuppressWarnings("unchecked")
	protected List<LocalIRODSTransferItem> getAllTransferItemsForTransfer(
			final Long localIRODSTransferId) throws JargonException {
		final String queryString = "from LocalIRODSTransferItem transfer where transfer.localIRODSTransfer.id = :transferId order by transfer.transferredAt asc";
		log.info("getting transfer queue using query:{}", queryString);
		final Session session = hibernateUtil.getSession();

		Transaction tx = null;

		try {
			log.info("beginning tx to store status of this transfer status");
			tx = session.beginTransaction();

			final Query q = session.createQuery(queryString).setLong(
					"transferId", localIRODSTransferId);
			final List<LocalIRODSTransferItem> irodsTransferQueue = q.list();

			//session.flush();
			tx.commit();

			return irodsTransferQueue;

		} catch (RuntimeException e) {

			if (tx != null)
				tx.rollback();

			log.error("error in transaction", e);
			throw new JargonException(e);
		} finally {
			//session.close();
			log.info("session closed");
		}
	}

	/**
	 * Get a list of all of the transfer items for the given transfer that are
	 * an error.
	 * 
	 * @param localIRODSTransferId
	 *            <code>Long</code> with the key of the given transfer
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.transferengine.LocalIRODSTransferItem}
	 *         with query results.
	 * @throws JargonException
	 */
	@SuppressWarnings("unchecked")
	protected List<LocalIRODSTransferItem> getErrorTransferItemsForTransfer(
			final Long localIRODSTransferId) throws JargonException {
		final String queryString = "from LocalIRODSTransferItem transfer where transfer.localIRODSTransfer.id = :transferId and transfer.error = true order by transfer.transferredAt asc";
		log.info("getting transfer queue using query:{}", queryString);
		final Session session = hibernateUtil.getSession();

		Transaction tx = null;

		try {
			log.info("beginning tx to store status of this transfer status");
			tx = session.beginTransaction();

			final Query q = session.createQuery(queryString).setLong(
					"transferId", localIRODSTransferId);
			final List<LocalIRODSTransferItem> irodsTransferQueue = q.list();
			//session.flush();

			tx.commit();

			return irodsTransferQueue;

		} catch (RuntimeException e) {

			if (tx != null)
				tx.rollback();

			log.error("error in transaction", e);
			throw new JargonException(e);
		} finally {
			//session.close();
			log.info("session closed");
		}
	}

	/**
	 * For the given transfer, set it to enqueued so that it can be restarted.
	 * 
	 * @param localIRODSTransfer
	 *            <code>LocalIRODSTransfer</code> with the restarted transfer
	 *            information. Note that this method will reload the transfer as
	 *            it is on the database before updating.
	 * @throws JargonException
	 */
	protected void restartTransfer(final LocalIRODSTransfer localIRODSTransfer)
			throws JargonException {

		if (localIRODSTransfer == null) {
			throw new JargonException("localIRODSTransfer");
		}

		log.info("restarting a transfer:{}", localIRODSTransfer);

		final Session session = hibernateUtil.getSession();

		Transaction tx = null;

		try {
			log.info("beginning tx to store status of this transfer ");
			tx = session.beginTransaction();

			LocalIRODSTransfer txfrToUpdate = (LocalIRODSTransfer) session
					.load(LocalIRODSTransfer.class, localIRODSTransfer.getId());
			log.info(">>>>restart last successful path:{}", txfrToUpdate
					.getLastSuccessfulPath());
			txfrToUpdate
					.setTransferErrorStatus(LocalIRODSTransfer.TRANSFER_STATUS_OK);
			txfrToUpdate
					.setTransferState(LocalIRODSTransfer.TRANSFER_STATE_ENQUEUED);
			session.update(txfrToUpdate);
			log.info("status reset and enqueued for restart");
			//session.flush();
			tx.commit();

		} catch (RuntimeException e) {

			if (tx != null)
				tx.rollback();

			log.error("error in transaction", e);
			throw new JargonException(e);
		} finally {
			//session.close();
			log.info("session closed");
		}

	}

	/**
	 * For the given transfer, set it to enqueued so that it can be restarted
	 * from the beginning. This will clear out the 'last successful' field so
	 * that all files are transferred again.
	 * 
	 * @param localIRODSTransfer
	 *            <code>LocalIRODSTransfer</code> with the restarted transfer
	 *            information. Note that this method will reload the transfer as
	 *            it is on the database before updating.
	 * @throws JargonException
	 */
	protected void resubmitTransfer(final LocalIRODSTransfer localIRODSTransfer)
			throws JargonException {

		if (localIRODSTransfer == null) {
			throw new JargonException("localIRODSTransfer");
		}

		log.info("restarting a transfer:{}", localIRODSTransfer);

		final Session session = hibernateUtil.getSession();

		Transaction tx = null;
		final String queryString = "delete from LocalIRODSTransferItem as transferItem where transferItem.localIRODSTransfer = ( from LocalIRODSTransfer as transfer where id = :id )";

		try {
			log.info("beginning tx to store status of this transfer ");
			tx = session.beginTransaction();

			LocalIRODSTransfer txfrToUpdate = (LocalIRODSTransfer) session
					.load(LocalIRODSTransfer.class, localIRODSTransfer.getId());
			log.info(">>>>restart last successful path:{}", txfrToUpdate
					.getLastSuccessfulPath());
			txfrToUpdate
					.setTransferErrorStatus(LocalIRODSTransfer.TRANSFER_STATUS_OK);
			txfrToUpdate
					.setTransferState(LocalIRODSTransfer.TRANSFER_STATE_ENQUEUED);
			txfrToUpdate.setLastSuccessfulPath("");
			session.update(txfrToUpdate);
			// delete all items recorded for this transfer previously
			Query query = session.createQuery(queryString);
			query.setLong("id", txfrToUpdate.getId());
			query.executeUpdate();

			log
					.info("status reset and enqueued for resubmit, old transfer items removed");
			//session.flush();
			tx.commit();

		} catch (RuntimeException e) {

			if (tx != null)
				tx.rollback();

			log.error("error in transaction", e);
			throw new JargonException(e);
		} finally {
			//session.close();
			log.info("session closed");
		}

	}

	/**
	 * Place a replicate transfer into the queue. This replication will be
	 * recursive for a collection
	 * 
	 * @param irodsAbsolutePath
	 *            <code>String</code> with the absolute path of the
	 *            irodsCollection or file to replicate.
	 * @param targetResource
	 *            <code>String</code> to which the file/collection will be
	 *            replicated.
	 * @param irodsAccount
	 *            <code>IRODSAccount</code> to which the files will be
	 *            replicated.
	 */
	protected LocalIRODSTransfer enqueueReplicateTransfer(
			String irodsAbsolutePath, String targetResource,
			IRODSAccount irodsAccount) throws JargonException {

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new JargonException("irodsAbsolutePath is null or empty");
		}

		if (targetResource == null) {
			throw new JargonException(
					"targetResource is null, set as blank if not used");
		}

		if (irodsAccount == null) {
			throw new JargonException("null irodsAccount");
		}

		log
				.info("enqueue replicate transfer from iRODS: {}",
						irodsAbsolutePath);
		log.info("   target resource:{}", targetResource);

		log.info("getting hibernate session factory and opening session");
		final Session session = hibernateUtil.getSession();

		Transaction tx = null;
		final LocalIRODSTransfer enqueuedTransfer;
		try {
			log.info("beginning tx");
			tx = session.beginTransaction();
			enqueuedTransfer = new LocalIRODSTransfer();
			enqueuedTransfer.setCreatedAt(new Date());
			enqueuedTransfer.setIrodsAbsolutePath(irodsAbsolutePath);
			enqueuedTransfer.setLocalAbsolutePath("");
			enqueuedTransfer.setTransferHost(irodsAccount.getHost());
			enqueuedTransfer.setTransferPort(irodsAccount.getPort());
			enqueuedTransfer.setTransferResource(targetResource);
			enqueuedTransfer.setTransferZone(irodsAccount.getZone());
			enqueuedTransfer.setTransferStart(new Date());
			enqueuedTransfer
					.setTransferType(LocalIRODSTransfer.TRANSFER_TYPE_REPLICATE);
			enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
			enqueuedTransfer.setTransferPassword(HibernateUtil
					.obfuscate(irodsAccount.getPassword()));
			enqueuedTransfer
					.setTransferState(LocalIRODSTransfer.TRANSFER_STATE_ENQUEUED);
			enqueuedTransfer
					.setTransferErrorStatus(LocalIRODSTransfer.TRANSFER_STATUS_OK);
			log.info("saving...{}", enqueuedTransfer);

			session.saveOrUpdate(enqueuedTransfer);
			//session.flush();
			log.info("commit");
			tx.commit();
		} catch (RuntimeException e) {

			if (tx != null)
				tx.rollback();

			log.error("error in transaction", e);
			throw new JargonException(e);
		} finally {
			//session.close();
			log.info("session closed");
		}

		log.info("enqueued...");
		return enqueuedTransfer;

	}

	/**
	 * Mark the given transfer as cancelled. Note that transfers that are marked
	 * as 'complete' are left alone.
	 * 
	 * @param localIRODSTransfer
	 *            <code>LocalIRODSTransfer</code> that represents the transfer
	 *            to mark as cancelled.
	 * @throws JargonException
	 */
	protected void setTransferAsCancelled(
			final LocalIRODSTransfer localIRODSTransfer) throws JargonException {

		if (localIRODSTransfer == null) {
			throw new JargonException("localIRODSTransfer is null");
		}

		log.info("cancelling a transfer:{}", localIRODSTransfer);

		final Session session = hibernateUtil.getSession();

		Transaction tx = null;

		try {
			log.info("beginning tx to store status of this transfer ");
			tx = session.beginTransaction();

			LocalIRODSTransfer txfrToCancel = (LocalIRODSTransfer) session
					.load(LocalIRODSTransfer.class, localIRODSTransfer.getId());

			// if already complete, do not mark cancelled
			if (!txfrToCancel.getTransferState().equals(
					LocalIRODSTransfer.TRANSFER_STATE_COMPLETE)) {
				txfrToCancel
						.setTransferErrorStatus(LocalIRODSTransfer.TRANSFER_STATUS_OK);
				txfrToCancel
						.setTransferState(LocalIRODSTransfer.TRANSFER_STATE_CANCELLED);
				session.update(txfrToCancel);
				log.info("status set to cancelled");
			}

			//session.flush();
			tx.commit();

		} catch (RuntimeException e) {

			if (tx != null)
				tx.rollback();

			log.error("error in transaction", e);
			throw new JargonException(e);
		} finally {
			//session.close();
			log.info("session closed");
		}
	}

	protected void purgeQueueBasedOnDate(final int retentionDays)
			throws JargonException {

		if (retentionDays < 0) {
			throw new JargonException("retentionDays must be 0 or greater");
		}

		log
				.info(
						"purging the queue of all completed or cancelled items more than {} days old",
						retentionDays);

		final String queryString = "delete from LocalIRODSTransfer as transfer where transfer.transferState == 'COMPLETE' or transferState == 'CANCELLED'";

		final Session session = hibernateUtil.getSession();
		Transaction tx = null;

		try {
			log.info("beginning tx to delete transfer queue");
			tx = session.beginTransaction();
			Query query = session.createQuery(queryString);
			query.executeUpdate();
			//session.flush();
			tx.commit();

		} catch (RuntimeException e) {

			if (tx != null)
				tx.rollback();

			log.error("error in transaction", e);
			throw new JargonException(e);
		} finally {
			//session.close();
			log.info("session closed");
		}

	}

	/**
	 * This method is for startup processing of the queue.  There may have been previous errors,
	 * or transfers left in an indeterminate state.  This method is called at startup to set up the 
	 * queue before the transfer engine starts.
	 * @throws JargonException
	 */
	protected void processQueueAtStartup() throws JargonException {
		log.info("in startup...");
		List<LocalIRODSTransfer> currentQueue = getCurrentQueue();

		if (currentQueue.isEmpty()) {
			log.info("queue is empty");
			return;
		}

		for (LocalIRODSTransfer localIrodsTransfer : currentQueue) {
			if (localIrodsTransfer.getTransferState().equals(
					LocalIRODSTransfer.TRANSFER_STATE_PROCESSING)) {
				log.info("resetting a processing transfer to enqueued:{}",
						localIrodsTransfer);
				resetTransferToEnqueued(localIrodsTransfer);
			}
		}

	}

	/**
	 * Reset a transfer to enqueued. Used on startup so transfers marked as
	 * processed are not treated as such during dequeue.
	 * 
	 * @param transferToReset
	 *            {@link org.irods.jargon.transferengine.domain.LocalIRODSTransfer}
	 *            to be reset
	 * @throws JargonException
	 */
	private void resetTransferToEnqueued(
			final LocalIRODSTransfer transferToReset) throws JargonException {
		final Session session = hibernateUtil.getSession();

		Transaction tx = null;

		try {
			log.info("beginning tx to reset status of this transfer:{} ", transferToReset);
			tx = session.beginTransaction();

			transferToReset
					.setTransferErrorStatus(LocalIRODSTransfer.TRANSFER_STATUS_OK);
			transferToReset
					.setTransferState(LocalIRODSTransfer.TRANSFER_STATE_ENQUEUED);
			session.update(transferToReset);
			log.info("status set to enqueued");

			//session.flush();
			tx.commit();

		} catch (RuntimeException e) {

			if (tx != null)
				tx.rollback();

			log.error("error in transaction", e);
			throw new JargonException(e);
		} finally {
			//session.close();
			log.info("session closed");
		}
	}

	/**
	 * @return the hibernateUtil
	 */
	public HibernateUtil getHibernateUtil() {
		return hibernateUtil;
	}

}
