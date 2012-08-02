package org.irods.jargon.core.connection;

import java.util.concurrent.Callable;

import org.irods.jargon.core.exception.JargonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>Runnable</code> that can process iRODS reconnections. This mirrors the
 * functionality provided by the cliReconnManager in rcConnect.c in iRODS. When
 * the jargon <code>isReconnect()</code> is set in {@link JargonProperies} (and
 * therefore is enabled in {@link PipelineConfiguration}, then a thread is
 * started to periodically
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class ReconnectionManager implements Callable<Void> {

	private final IRODSCommands irodsCommands;
	private Logger log = LoggerFactory.getLogger(ReconnectionManager.class);
	private long reconnectMillis = 0;
	private static final long RECONNECT_PERIOD_MILLIS = 600000;
	private static final long SLEEP_TIME = 10000;

	public enum ConnectionStates {
		PROCESSING_STATE, /* the process is not sending nor receiving */
		RECEIVING_STATE, SENDING_STATE, CONN_WAIT_STATE
	}
	/**
	 * Creates an instance of a manager thread (a runnable) that will start up
	 * if reconnect behavior is indicated for an iRODS connection. This is done
	 * via the startup sequence, and in this case the <oode>IRODSCommands</code>
	 * object will hold the necessary data in the {@link StartupResponse}
	 * object.
	 * 
	 * @param irodsCommands
	 * @return
	 */
	public static ReconnectionManager instance(final IRODSCommands irodsCommands) {
		return new ReconnectionManager(irodsCommands);
	}

	/**
	 * Private constructor, use <code>instance()</code> method.
	 * 
	 * @param irodsCommands
	 *            {@link IRODSCommands} that wraps the underlyig connection to
	 *            iRODS
	 */
	private ReconnectionManager(final IRODSCommands irodsCommands) {
		if (irodsCommands == null) {
			throw new IllegalArgumentException("null irodsCommands");
		}

		this.irodsCommands = irodsCommands;
		reconnectMillis = System.currentTimeMillis() + RECONNECT_PERIOD_MILLIS;
	}

	/**
	 * Do a reconnect by calling the appropriate reconnect method in
	 * IRODSCommands.
	 * <p/>
	 * Note that the <code>IRODSCommands</code> reconnect method is synchronized
	 * in that class, this ensures that no other protocol operation will occur
	 * while reconnect is happening
	 */
	private void reconnect() throws JargonException {
		log.info("calling reconnect() on IRODSCommands");
		irodsCommands.reconnect();
	}

	/**
	 * @return the irodsCommands
	 */
	public IRODSCommands getIrodsCommands() {
		return irodsCommands;
	}

	@Override
	public Void call() throws JargonException {
		log.info("reconn namanger run() ");
		while (!Thread.currentThread().isInterrupted()) {
			long currentMillis = System.currentTimeMillis();
			if (currentMillis < reconnectMillis) {
				try {
					Thread.sleep(SLEEP_TIME);
				} catch (InterruptedException e) {
					// ignore
				}
			} else {
				// time to reconnect
				reconnect();
				// reconnect once, then we're outta here
				return null;
			}
		}
		return null;
	}

	/**
	 * @return the reconnectMillis
	 */
	public synchronized long getReconnectMillis() {
		return reconnectMillis;
	}

	/**
	 * @param reconnectMillis
	 *            the reconnectMillis to set
	 */
	public synchronized void setReconnectMillis(long reconnectMillis) {
		this.reconnectMillis = reconnectMillis;
	}

}
/*
 * //Build a task and an executor MyTask task = new MyTask(2, 0);
 * ExecutorService threadExecutor = Executors.newSingleThreadExecutor();
 * 
 * try { //Compute the task in a separate thread int result = (int)
 * threadExecutor.submit(task).get(); System.out.println("The result is " +
 * result); } catch (ExecutionException e) { //Handle the exception thrown by
 * the child thread if (e.getMessage().contains("cannot devide by zero"))
 * System.out.println("error in child thread caused by zero division"); } catch
 * (InterruptedException e) { //This exception is thrown if the child thread is
 * interrupted. e.printStackTrace(); }
 */
