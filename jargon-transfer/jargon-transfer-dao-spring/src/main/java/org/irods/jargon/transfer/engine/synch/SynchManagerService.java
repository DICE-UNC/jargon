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

}