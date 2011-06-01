package org.irods.jargon.transfer.engine.synch;

import org.irods.jargon.transfer.dao.domain.SynchConfiguration;

/**
 * Interface for service to manage storage and processing of synch information
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface SynchManagerService {

	/**
	 * Create a new synch configuration.
	 * 
	 * @param synchConfiguration
	 * @throws SynchException
	 */
	void createNewSynchConfiguration(SynchConfiguration synchConfiguration)
			throws SynchException;

}