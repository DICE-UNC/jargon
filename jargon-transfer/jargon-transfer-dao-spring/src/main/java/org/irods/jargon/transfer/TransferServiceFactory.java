package org.irods.jargon.transfer;

import org.irods.jargon.transfer.engine.ConfigurationService;
import org.irods.jargon.transfer.engine.GridAccountService;
import org.irods.jargon.transfer.engine.TransferQueueService;
import org.irods.jargon.transfer.engine.synch.SynchManagerService;

/**
 * Interface to a factory that can create underlying spring-enabled services
 * used for transfer management
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface TransferServiceFactory {

	/**
	 * Initialize a <code>TransferQueueService</code> that manages the queue of
	 * transfers
	 * 
	 * @return {@link TransferQueueService}
	 */
	TransferQueueService instanceTransferQueueService();

	/**
	 * Initialize an instance of the <code>SynchManagerService</code> that
	 * manages synchronizations
	 * 
	 * @return {@link SynchManagerService}
	 */
	SynchManagerService instanceSynchManagerService();

	/**
	 * Initialize an instance of the <code>GridAccountService</code> which keeps
	 * track of identities in the transfer system
	 * 
	 * @return {@link GridAccountService}
	 */
	GridAccountService instanceGridAccountService();

	/**
	 * Initialize an instance of the service that manages the underlying
	 * configuration information, such as properties and settings
	 * 
	 * @return {@link ConfigurationService}
	 */
	ConfigurationService instanceConfigurationService();

}