package org.irods.jargon.conveyor.core;

import java.util.Timer;

import org.irods.jargon.conveyor.basic.BasicQueueManagerServiceImpl;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.transfer.exception.PassPhraseInvalidException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract implementation of the <code>ConveyorService</code> interface.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */

public class ConveyorServiceImpl implements ConveyorService {

	private ConveyorCallbackListener conveyorCallbackListener;

	/**
	 * required dependency
	 */
	private QueueManagerService queueManagerService;

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

	/**
	 * required dependency on the {@link ConfigurationService} that manages
	 * name/value pairs that reflect service configuration
	 */
	private ConfigurationService configurationService;

	/**
	 * required dependency on the {@link TransferAccountingManagementService}
	 * that manages the transfer actions as they are executed
	 */
	private TransferAccountingManagementService transferAccountingManagementService;

	/**
	 * required dependency on the {@link IRODSAccessObjectFactory} that will
	 * allow connections to iRODS
	 */
	private IRODSAccessObjectFactory irodsAccessObjectFactory;

	private Timer queueTimer = new Timer();

	private static final Logger log = LoggerFactory
			.getLogger(BasicQueueManagerServiceImpl.class);

	@Override
	public ConfigurationService getConfigurationService() {
		return configurationService;
	}

	@Override
	public void setConfigurationService(
			final ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}

	@Override
	public QueueManagerService getQueueManagerService() {
		log.info("returning queueManagerService: {}", queueManagerService);
		return queueManagerService;
	}

	@Override
	public void setQueueManagerService(
			final QueueManagerService queueMangerService) {
		queueManagerService = queueMangerService;
	}

	@Override
	public FlowManagerService getFlowManagerService() {
		return flowManagerService;
	}

	@Override
	public void setFlowManagerService(
			final FlowManagerService flowManagerService) {
		this.flowManagerService = flowManagerService;
	}

	@Override
	public SynchronizationManagerService getSynchronizationManagerService() {
		return synchronizationManagerService;
	}

	@Override
	public void setSynchronizationManagerService(
			final SynchronizationManagerService synchronizationManagerService) {
		this.synchronizationManagerService = synchronizationManagerService;
	}

	@Override
	public GridAccountService getGridAccountService() {
		return gridAccountService;
	}

	@Override
	public void setGridAccountService(
			final GridAccountService gridAccountService) {
		this.gridAccountService = gridAccountService;
	}

	@Override
	public ConveyorExecutorService getConveyorExecutorService() {
		return conveyorExecutorService;
	}

	@Override
	public void setConveyorExecutorService(
			final ConveyorExecutorService conveyorExecutorService) {
		this.conveyorExecutorService = conveyorExecutorService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.core.ConveyorService#shutdown()
	 */
	@Override
	public void shutdown() {
		if (conveyorExecutorService != null) {
			conveyorExecutorService.shutdown();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.conveyor.core.ConveyorService#validatePassPhrase(java
	 * .lang.String)
	 */
	@Override
	public void validatePassPhrase(final String passPhrase)
			throws PassPhraseInvalidException, ConveyorExecutionException {
		gridAccountService.validatePassPhrase(passPhrase);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.conveyor.core.ConveyorService#resetConveyorService()
	 */
	@Override
	public void resetConveyorService() throws ConveyorExecutionException {
		gridAccountService.resetPassPhraseAndAccounts();

	}

	@Override
	public boolean isPreviousPassPhraseStored()
			throws ConveyorExecutionException {
		return gridAccountService.isPassPhraseStoredAlready();
	}

	/**
	 * @return the irodsAccessObjectFactory
	 */
	@Override
	public IRODSAccessObjectFactory getIrodsAccessObjectFactory() {
		return irodsAccessObjectFactory;
	}

	/**
	 * @param irodsAccessObjectFactory
	 *            the irodsAccessObjectFactory to set
	 */
	@Override
	public void setIrodsAccessObjectFactory(
			final IRODSAccessObjectFactory irodsAccessObjectFactory) {
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
	}

	/**
	 * Get a reference to a callback listener. In order to simply code sending
	 * callbacks, this method will ensure that the callback listener is not
	 * null, and in that case will create a dummy listener
	 * 
	 * @return the transferStatusCallbackListener
	 */
	@Override
	public synchronized ConveyorCallbackListener getConveyorCallbackListener() {

		if (conveyorCallbackListener == null) {
			conveyorCallbackListener = new DevNullCallbackListener();
		}

		return conveyorCallbackListener;
	}

	/**
	 * @param transferStatusCallbackListener
	 *            the transferStatusCallbackListener to set
	 */
	@Override
	public synchronized void setConveyorCallbackListener(
			final ConveyorCallbackListener conveyorCallbackListener) {
		this.conveyorCallbackListener = conveyorCallbackListener;
	}

	@Override
	public TransferAccountingManagementService getTransferAccountingManagementService() {
		return transferAccountingManagementService;
	}

	@Override
	public void setTransferAccountingManagementService(
			final TransferAccountingManagementService transferAccountingManagementService) {
		this.transferAccountingManagementService = transferAccountingManagementService;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.conveyor.core.ConveyorService#registerCallbackListener
	 * (org.irods.jargon.core.transfer.TransferStatusCallbackListener)
	 */
	@Override
	public void registerCallbackListener(final ConveyorCallbackListener listener) {
		conveyorCallbackListener = listener;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.core.ConveyorService#getQueueStatus()
	 */
	@Override
	public synchronized QueueStatus getQueueStatus() {
		return getConveyorExecutorService().getQueueStatus();

	}

	@Override
	public synchronized void cancelQueueTimerTask() {
		log.info("cancelQueueTimerTask()");
		if (queueTimer != null) {
			queueTimer.cancel();
			queueTimer = null;
		}
	}

	@Override
	public synchronized void startQueueTimerTask() {
		/*
		 * Since I'm starting, look for any currently processing transactions
		 * and reset them to 'enqueued'
		 */

		log.info("creating timer task to trigger queue actions");

		ConveyorQueueTimerTask queueSchedulerTimerTask = new ConveyorQueueTimerTask();
		queueSchedulerTimerTask.setConveyorService(this);
		queueSchedulerTimerTask.init();

		// SynchPeriodicScheduler synchPeriodicScheduler = new
		// SynchPeriodicScheduler(idropCore.getTransferManager(),
		// idropCore.getIRODSAccessObjectFactory());
		queueTimer = new Timer();
		queueTimer.scheduleAtFixedRate(queueSchedulerTimerTask, 10000, 120000);
		log.info("timer scheduled");
	}

	@Override
	public void init() {

		log.info("init()");

		if (getConfigurationService() == null) {
			throw new ConveyorRuntimeException(
					"null configurationService, dependency was not set");
		}

		try {
			getConveyorExecutorService().setBusyForAnOperation();
			log.info("checking for any transactions that were set to processing, and reset them to enqueued...");
			getQueueManagerService().preprocessQueueAtStartup();
			log.info("preprocessing done, unlock the queue");
			getConveyorExecutorService().setOperationCompleted();
			startQueueTimerTask();
		} catch (ConveyorBusyException e) {
			log.error("cannot lock queue for initialization!", e);
			throw new ConveyorRuntimeException("cannot lock queue for init", e);
		} catch (ConveyorExecutionException e) {
			log.error("cannot process queue for initialization!", e);
			throw new ConveyorRuntimeException("cannot process queue for init",
					e);
		}

	}

}
