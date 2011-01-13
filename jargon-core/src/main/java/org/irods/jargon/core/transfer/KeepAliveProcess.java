/**
 * 
 */
package org.irods.jargon.core.transfer;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.pub.EnvironmentalInfoAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *TODO: this is experimental...
 *
 * Describes a thread that runs concurrently with a parallel or other transfer strategy, and does an occasional ping on the message socket 
 * to make sure that socket connection is alive.  This prevents time-outs on the main connection to an iRODS agent while waiting for a process to complete.
 * 
 * @author Mike Conway - DICE (www.renci.org)
 * 
 */
public class KeepAliveProcess implements Runnable {

	public static final Logger log = LoggerFactory
			.getLogger(KeepAliveProcess.class);

	private final EnvironmentalInfoAO environmentalInfoAO;
	private boolean terminate = false;

	public synchronized boolean isTerminate() {
		return terminate;
	}

	public synchronized void setTerminate(final boolean terminate) {
		this.terminate = terminate;
	}

	public KeepAliveProcess(final EnvironmentalInfoAO environmentalInfoAO) {
		
		if (environmentalInfoAO == null) {
			throw new IllegalArgumentException("environmentalInfoAO is null");
		}
		
		this.environmentalInfoAO = environmentalInfoAO;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		int renewalCounter = 0;

		while (!isTerminate()) {
			try {
				renewalCounter++;
				if (renewalCounter > 60) {
					log.debug("keep alive is pinging");
					environmentalInfoAO
							.getIRODSServerPropertiesFromIRODSServer();
					renewalCounter = 0;
				}
				Thread.sleep(1000);
			} catch (JargonException e) {
				throw new JargonRuntimeException(e);
			} catch (InterruptedException e) {
				throw new JargonRuntimeException(e);
			}
		}

	}

}
