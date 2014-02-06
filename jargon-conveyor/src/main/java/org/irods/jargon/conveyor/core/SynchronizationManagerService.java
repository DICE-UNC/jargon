/**
 * 
 */
package org.irods.jargon.conveyor.core;

import java.util.List;

import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.transfer.dao.domain.Synchronization;

/**
 * Manages synchronizations scheduling.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface SynchronizationManagerService {

	/**
	 * Get a list of all configured synchronizations
	 * 
	 * @return <code>List</code> of {@link Synchronization}
	 * @throws ConveyorExecutionException
	 */
	List<Synchronization> listAllSynchronizations()
			throws ConveyorExecutionException;

	/**
	 * Find and return a synch based on its id key. Return <code>null</code> if
	 * not found
	 * 
	 * @param id
	 *            <code>long</code> with the id
	 * @return {@link Synchronization} with that id, or <code>null</code> if not
	 *         found
	 * @throws ConveyorExecutionException
	 */
	Synchronization findById(final long id) throws ConveyorExecutionException;

	/**
	 * Cause the next pending (if any) synchronization process to be executed If
	 * no synchs are pending, then nothing is done.
	 * 
	 * @return
	 * 
	 * @throws ConveyorExecutionException
	 */
	void triggerExecutionOfAnyNextPendingSynchronization()
			throws ConveyorExecutionException;

	/**
	 * Cause an add or update of a synchronization
	 * 
	 * @param synchronization
	 *            {@link Synchronization} to add or update.
	 * @throws ConveyorExecutionException
	 */
	void addOrUpdateSynchronization(final Synchronization synchronization)
			throws ConveyorExecutionException;

	/**
	 * Delete a synchronization
	 * 
	 * @param synchronization
	 *            {@link Synchronization} to delete
	 * @throws ConveyorExecutionException
	 */
	void deleteSynchronization(final Synchronization synchronization)
			throws ConveyorExecutionException;

	/**
	 * Clear the history of transfer and attemps for the given synchronization
	 * 
	 * @param synchronization
	 *            {@link Synchronization} to purge
	 * @throws DataNotFoundException
	 *             if the synch does not exist
	 * @throws ConveyorExecutionException
	 */
	void purgeSynchronizationHistory(final Synchronization synchronization)
			throws DataNotFoundException, ConveyorExecutionException;

	/**
	 * Trigger immediate processing of a synchronization
	 * 
	 * @param synchronization
	 *            {@link Synchronization} to trigger, it must exist
	 * @throws RejectedTransferException
	 *             if the transfer cannot be scheduled
	 * @throws ConveyorExecutionException
	 */
	void triggerSynchronizationNow(final Synchronization synchronization)
			throws RejectedTransferException, ConveyorExecutionException;

}
