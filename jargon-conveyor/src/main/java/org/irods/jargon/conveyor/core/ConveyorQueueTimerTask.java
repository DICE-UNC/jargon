package org.irods.jargon.conveyor.core;

import java.util.TimerTask;

import org.slf4j.LoggerFactory;

/**
 * Task to periodically check the queue
 * 
 * @author Mike Conway - DICE (www.irods.org)
 */
public class ConveyorQueueTimerTask extends TimerTask {

	/**
	 * Required dependency
	 */
	private ConveyorService conveyorService;
	private volatile boolean initDone = false;

	/**
	 * allows the timer task to be paused (just bypasses any operations when the
	 * timer fires)
	 */
	private volatile boolean paused = false;

	private final org.slf4j.Logger log = LoggerFactory
			.getLogger(ConveyorQueueTimerTask.class);

	public ConveyorQueueTimerTask() {

	}

	/**
	 * Init method is called before starting queue timer task
	 * <p/>
	 * Right now this is just doing dependency checks but might be expanded
	 * later
	 */
	public void init() {
		log.info("init");
		if (conveyorService == null) {
			throw new ConveyorRuntimeException("conveyorService not set");
		}
		initDone = true;

	}

	/**
	 * Ping the conveyor periodically to look for any actions
	 */
	@Override
	public void run() {
		log.info("timer task running");
		if (!initDone) {
			throw new ConveyorRuntimeException("init is not done");
		}

		if (paused) {
			log.info("timer task is paused...");
			return;
		}

		try {
			log.info("timer task firing...dequeue if available");
			conveyorService.getQueueManagerService().dequeueNextOperation();
		} catch (ConveyorExecutionException e) {
			log.error("exception running timer task", e);
			throw new ConveyorRuntimeException(
					"exception encountered running timer task", e);
		}

	}

	/**
	 * @return the conveyorService
	 */
	public ConveyorService getConveyorService() {
		return conveyorService;
	}

	/**
	 * @param conveyorService
	 *            the conveyorService to set
	 */
	public void setConveyorService(final ConveyorService conveyorService) {
		this.conveyorService = conveyorService;
	}

	/**
	 * @return the paused
	 */
	public synchronized boolean isPaused() {
		return paused;
	}

	/**
	 * @param paused
	 *            the paused to set
	 */
	public synchronized void setPaused(final boolean paused) {
		log.info("set paused to:{}", paused);
		this.paused = paused;
	}

}
