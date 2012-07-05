/**
 * 
 */
package org.irods.jargon.core.connection;

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
public class ReconnectionManager implements Runnable {

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {


	}

}
