package org.irods.jargon.conveyor.core;

import org.irods.jargon.conveyor.synch.SynchComponentFactory;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.AuthenticationException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
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
	 * Retrieve the current error and running status of the conveyor service
	 * 
	 * @return {@link QueueStatus} reflecting the current state of the conveyor
	 *         service
	 */
	QueueStatus getQueueStatus();

	/**
	 * Register a listener who will receive callback messages from running
	 * transfers
	 * 
	 * @param listener
	 *            {@link ConveyorCallbackListener}
	 */
	void registerCallbackListener(ConveyorCallbackListener listener);

	/**
	 * Get the registered callback listener (may be null)
	 * 
	 * @return {@link ConveyorCallbackListener}
	 */
	ConveyorCallbackListener getConveyorCallbackListener();

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
	 * Initialize the conveyor service in shared mode. This means that all grid
	 * accounts are cleared and initialized at start up, all previous transfers
	 * are cleared, and the app starts in a 'fresh' state, with the account
	 * information provided, and the
	 * 
	 * @param irodsAccount
	 * @throws AuthenticationException
	 * @throws JargonException
	 * @throws ConveyorExecutionException
	 */
	void validatePassPhraseInTearOffMode(final IRODSAccount irodsAccount)
			throws AuthenticationException, JargonException,
			ConveyorExecutionException, JargonException;

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

	void setConveyorCallbackListener(
			ConveyorCallbackListener conveyorCallbackListener);

	/**
	 * Initialize the timer task that asynchronously triggers the queue, and any
	 * other setup tasks
	 * <p/>
	 * This method must be called when bootstrapping the conveyor service,
	 * through spring configuration or otherwise
	 */
	void init();

	/**
	 * Cancel the timer task that periodically checks the queue. It can be
	 * started again using the <code>startQueueTimerTask</code> method.
	 */
	void cancelQueueTimerTask();

	/**
	 * Start the timer task that periodically checks the queue. It can be
	 * cancelled through the <code>cancelQueueTimerTask</code> method.
	 * <p/>
	 * Calling this method also begins execution of the periodic timer task that
	 * will run any pending processes and do any periodic activity within the
	 * conveyor service. This method must be called by the client when ready to
	 * start processing.
	 * 
	 * @throws ConveyorExecutionException
	 */
	void beginFirstProcessAndRunPeriodicServiceInvocation()
			throws ConveyorExecutionException;

	/**
	 * Set the factory used to build components used in the synch process
	 * 
	 * @param synchComponentFactory
	 *            {@link SynchComponentFactory}
	 */
	void setSynchComponentFactory(
			final SynchComponentFactory synchComponentFactory);

	/**
	 * Get the factory used to build synch components
	 * 
	 * @return {@link SynchComponentFactory}
	 */
	SynchComponentFactory getSynchComponentFactory();

}
