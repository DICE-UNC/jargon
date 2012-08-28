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
	private static final long SLEEP_TIME = 1000;

	public enum ProcessingState {
		PROCESSING_STATE, RECEIVING_STATE, SENDING_STATE, CONN_WAIT_STATE
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
		reconnectMillis = System.currentTimeMillis()
				+ irodsCommands.getPipelineConfiguration()
						.getReconnectTimeInMillis();
		log.info("current time:{}", System.currentTimeMillis());
		log.info("reconnect millis:{}", reconnectMillis);
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
		log.info("evaluating whether to call reconnect() on IRODSCommands: {}",
				irodsCommands);

		log.info("restart mode:{}", irodsCommands.isInRestartMode());
		try {
			if (irodsCommands.isInRestartMode() && irodsCommands.isConnected()) {
				log.info("in restart mode...reconnect");
				irodsCommands.reconnect();
			} else {
				log.info("not in reconnect mode...ignore");
			}
		} finally {
			reconnectMillis = System.currentTimeMillis()
					+ irodsCommands.getPipelineConfiguration()
							.getReconnectTimeInMillis();
			log.info("...reset reconnect time to:{}", reconnectMillis);
		}

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

		/*
		 * Note that the check here on the while typically will not be
		 * encountered, instead, the catch of the InterruptedException will
		 * break out of the loop. Nevertheless it does not hurt anything and
		 * looks better then while(true).
		 */
		while (!Thread.currentThread().isInterrupted()) {
			long currentMillis = System.currentTimeMillis();
			// log.debug("current millis now:{}", currentMillis);
			// log.debug("reconnect time:{}", reconnectMillis);
			if (currentMillis < reconnectMillis) {
				try {
					// log.debug("check and sleep reconn manager");
					Thread.sleep(SLEEP_TIME);
				} catch (InterruptedException e) {
					log.info("interrupted...will shut down");
					break;
				}
			} else {
				// time to reconnect
				log.info("calling reconnect internally on irods commands: {}",
						this.getIrodsCommands());
				reconnect();
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
	public synchronized void setReconnectMillis(final long reconnectMillis) {
		this.reconnectMillis = reconnectMillis;
	}

}
