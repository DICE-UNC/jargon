/**
 *
 */
package org.irods.jargon.datautils.indexer;

import org.irods.jargon.datautils.visitor.HierComponent;

/**
 * Interface for controlling an indexer. This can cause an indexer to halt after
 * a certain amount of time, and can control waits and pauses to indexing.
 * <p>
 * Implementations can simply sleep between accesses, or could interrogate
 * server load to decide how active the indexer will be.
 *
 * @author conwaymc
 *
 */
public interface ControlRod {

	/**
	 * Give the ability of the system to throttle or bail. Can cause a wait, can
	 * interrogate the load of the server and sleep, or can return
	 * <code>false</code> to stop the indexer
	 *
	 * @param hierComponent
	 *            {@link HierComponent} that is about to be accessed.
	 * @return {@code} boolean of <code>true</code> to continue or
	 *         <code>false</code> to end the crawl, aborting the rest of the crawl
	 *         and returning a normal status code
	 */
	public boolean checkControlRod(HierComponent hierComponent);

}
