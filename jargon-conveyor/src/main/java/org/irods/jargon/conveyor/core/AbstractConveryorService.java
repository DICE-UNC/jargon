package org.irods.jargon.conveyor.core;

/**
 * Abstract implementation of the <code>ConveyorService</code> interface.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class AbstractConveryorService implements ConveyorService {
	
	/**
	 * required dependency 
	 */
	private QueueManagerService queueMangerService;
	
	/**
	 * required dependency 
	 */
	private FlowManagerService flowManagerService;
	
	/**
	 * required dependency 
	 */
	private SynchronizationManagerService synchronizationManagerService;
	
	/**
	 * required dependency 
	 */
	private GridAccountService gridAccountService;
	
	/**
	 * required dependency 
	 */
	private ConveyorExecutorService conveyorExecutorService;
	
	@Override
	public QueueManagerService getQueueMangerService() {
		return queueMangerService;
	}
	
	@Override
	public void setQueueMangerService(QueueManagerService queueMangerService) {
		this.queueMangerService = queueMangerService;
	}
	
	@Override
	public FlowManagerService getFlowManagerService() {
		return flowManagerService;
	}
	
	@Override
	public void setFlowManagerService(FlowManagerService flowManagerService) {
		this.flowManagerService = flowManagerService;
	}
	
	@Override
	public SynchronizationManagerService getSynchronizationManagerService() {
		return synchronizationManagerService;
	}
	
	@Override
	public void setSynchronizationManagerService(
			SynchronizationManagerService synchronizationManagerService) {
		this.synchronizationManagerService = synchronizationManagerService;
	}
	
	@Override
	public GridAccountService getGridAccountService() {
		return gridAccountService;
	}
	
	@Override
	public void setGridAccountService(GridAccountService gridAccountService) {
		this.gridAccountService = gridAccountService;
	}
	
	@Override
	public ConveyorExecutorService getConveyorExecutorService() {
		return conveyorExecutorService;
	}
	
	@Override
	public void setConveyorExecutorService(
			ConveyorExecutorService conveyorExecutorService) {
		this.conveyorExecutorService = conveyorExecutorService;
	}
	
	/* (non-Javadoc)
	 * @see org.irods.jargon.conveyor.core.ConveyorService#shutdown()
	 */
	@Override
	public void shutdown() {
		if (conveyorExecutorService == null) {
			throw new IllegalStateException("cannot shut down, no executorService set");
		}
		conveyorExecutorService.shutdown();
	}
	

}
