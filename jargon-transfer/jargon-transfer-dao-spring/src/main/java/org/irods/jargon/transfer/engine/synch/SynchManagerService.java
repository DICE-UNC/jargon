package org.irods.jargon.transfer.engine.synch;

import java.util.List;

import org.irods.jargon.transfer.dao.domain.Synchronization;

/**
 * Interface for service to manage storage and processing of synch information
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface SynchManagerService {

    /**
     * Create a new synch configuration.  During creation, conflicts are evaluated and any necessary iRODS metadata checks are done
     * 
     * @param synchConfiguration {@link Synchronization} to be added
     * @throws SynchException if a general error occurrs
     * @throws ConflictingSynchException if the given synchronization conflicts with an existing synchronization
     */
    void createNewSynchConfiguration(Synchronization synchConfiguration) throws ConflictingSynchException, SynchException;

    /**
     * List all synch configurations
     * @return <code>List</code> of {@link Synchronization} in the transfer engine config
     * @throws SynchException
     */
	List<Synchronization> listAllSynchronizations() throws SynchException;

	/**
	 * Find the <code>Synchronization</code> with the given id, or <code>null</code> if no data found
	 * @param id <code>Long</code> with the unique id for the synchronization
	 * @return {@link Synchronization} or </code>null</code> if no data
	 * @throws SynchException
	 */
	Synchronization findById(Long id) throws SynchException;

	/**
	 * Find the <code>Synchronization</code> with the given name, or <code>null</code> if no such synchronization exists.
	 * @param name <code>String</code> with the name of the desired <code>Synchronization</code>
	 * @return {@link Synchronization} or <code>null</code>
	 * @throws SynchException
	 */
	Synchronization findByName(String name) throws SynchException;

	/**
	 * Update the given configuration, checking for duplicates and already-existing synchronizations
	 * @param synchConfiguration {@link Synchronization} to be updated
	 * @throws ConflictingSynchException thrown if a duplicate name, or path setup exists
	 * @throws SynchException
	 */
	void updateSynchConfiguration(Synchronization synchConfiguration)
			throws ConflictingSynchException, SynchException;

	/**
	 * Delete the given synchronization.  Note that this method will not allow deletion if enqueued or processing synchronizations
	 * are in the queue, and will delete all associated transfers during the deletion process.
	 * @param synchronization {@link Synchronization} that will be deleted
	 * @throws SynchException
	 */
	void deleteSynchronization(Synchronization synchronization)
			throws SynchException;

	/**
	 * Method to check instantaneous queue status to see if a transfer is running.  This method is smart enough to check the id of the synch,
	 * and if it is <code>null</code> (not persisted) it returns false.
	 * @param synchronization {@link Synchronization} to be checked
	 * @return <code>boolean</code> that will be <code>true</code> if a synchronization is enqueued or processing
	 * @throws SynchException
	 */
	boolean isSynchRunning(Synchronization synchronization)
			throws SynchException;

}