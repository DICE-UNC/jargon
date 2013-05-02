package org.irods.jargon.conveyor.core;

import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener;
import org.irods.jargon.transfer.exception.PassPhraseInvalidException;

/**
 * Core of conveyor framework, manages client interactions and access to
 * conveyor services.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface ConveyorService {

	/**
	 * Register a listener who will receive callback messages from running
	 * transfers
	 * 
	 * @param listener
	 *            {@link TransferStatusCallbackListener}
	 */
	void registerCallbackListener(TransferStatusCallbackListener listener);

	/**
	 * Get the registered callback listener (may be null)
	 * 
	 * @return {@link TransferStatusCallbackListener}
	 */
	TransferStatusCallbackListener getTransferStatusCallbackListener();

	/**
	 * Required initialization method that must be called before the
	 * <code>ConveyorService</code> can be used. This method validates (or
	 * initially sets) a pass phrase that unlocks the underlying cache of
	 * accounts.
	 * 
	 * @param passPhrase
	 *            <code>String</code> with the pass phrase used to initialize
	 *            the underlying data store
	 * @throws PassPhraseInvalidException
	 *             thrown if the pass phrase is not valid
	 * @throws ConveyorExecutionException
	 */
	void validatePassPhrase(String passPhrase)
			throws PassPhraseInvalidException, ConveyorExecutionException;

	/**
	 * Check to see if this is the first run of the conveyor service by looking
	 * for the presence of the pass phrase
	 * 
	 * @return <code>boolean</code> of
	 *         <code>true<code> if the pass phrase is already remembered
	 * @throws ConveyorExecutionException
	 */
	boolean isPreviousPassPhraseStored() throws ConveyorExecutionException;

	/**
	 * Method to blow away the conveyor store, can be used if the pass phrase is
	 * forgotten. This clears all information from the memory, equivalent to
	 * clearing all underlying data tables.
	 * 
	 * @throws ConveyorExecutionException
	 */
	void resetConveyorService() throws ConveyorExecutionException;

	void setConveyorExecutorService(
			ConveyorExecutorService conveyorExecutorService);

	ConveyorExecutorService getConveyorExecutorService();

	void setGridAccountService(GridAccountService gridAccountService);

	GridAccountService getGridAccountService();

	void setSynchronizationManagerService(
			SynchronizationManagerService synchronizationManagerService);

	SynchronizationManagerService getSynchronizationManagerService();

	void setFlowManagerService(FlowManagerService flowManagerService);

	FlowManagerService getFlowManagerService();

	void setQueueManagerService(QueueManagerService queueManagerService);

	QueueManagerService getQueueManagerService();

	/**
	 * Get the service that maintains records of status of transfers and
	 * transfer items
	 * 
	 * @return
	 */
	TransferAccountingManagementService getTransferAccountingManagementService();

	/**
	 * Set the service that maintains records of status of transfers and
	 * transfer items
	 * 
	 * @param transferAccountingManagementService
	 */
	void setTransferAccountingManagementService(
			TransferAccountingManagementService transferAccountingManagementService);

	/**
	 * Clean up and shut down the service
	 */
	void shutdown();

	/**
	 * Get the {@link ConfigurationService} that is a required dependency, and
	 * manages the storage of arbitrary properties for configuration.
	 * 
	 * @return {@link ConfigurationService}
	 */
	ConfigurationService getConfigurationService();

	/**
	 * Setter for required dependency to manage configuration properties
	 * 
	 * @param configurationService
	 */
	void setConfigurationService(ConfigurationService configurationService);

	IRODSAccessObjectFactory getIrodsAccessObjectFactory();

	void setIrodsAccessObjectFactory(
			IRODSAccessObjectFactory irodsAccessObjectFactory);

	void setTransferStatusCallbackListener(
			TransferStatusCallbackListener transferStatusCallbackListener);

}
