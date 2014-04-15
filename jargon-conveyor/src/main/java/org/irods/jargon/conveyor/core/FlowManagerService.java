/**
 * 
 */
package org.irods.jargon.conveyor.core;

import org.irods.jargon.conveyor.flowmanager.flow.FlowSpecCacheService;

/**
 * Manages attached rules and workflows.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface FlowManagerService {

	/**
	 * Required dependency on a service that can process source scripts into
	 * FlowSpecs
	 * 
	 * @param flowSpecCacheService
	 *            {@link FlowSpecCacheService} that will scan directories to
	 *            produce flow specifications
	 */
	void setFlowSpecCacheService(final FlowSpecCacheService flowSpecCacheService);

}
