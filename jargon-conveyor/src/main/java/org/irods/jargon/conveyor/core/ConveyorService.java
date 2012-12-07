package org.irods.jargon.conveyor.core;

/**
 * Core of conveyor framework, manages client interactions and access to conveyor services.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public interface ConveyorService {

	public abstract void setConveyorExecutorService(ConveyorExecutorService conveyorExecutorService);

	public abstract ConveyorExecutorService getConveyorExecutorService();

	public abstract void setGridAccountService(GridAccountService gridAccountService);

	public abstract GridAccountService getGridAccountService();

	public abstract void setSynchronizationManagerService(SynchronizationManagerService synchronizationManagerService);

	public abstract SynchronizationManagerService getSynchronizationManagerService();

	public abstract void setFlowManagerService(FlowManagerService flowManagerService);

	public abstract FlowManagerService getFlowManagerService();

	public abstract void setQueueMangerService(QueueManagerService queueMangerService);

	public abstract QueueManagerService getQueueMangerService();

	/**
	 * Clean up and shut down the service
	 */
	void shutdown();
	
}
