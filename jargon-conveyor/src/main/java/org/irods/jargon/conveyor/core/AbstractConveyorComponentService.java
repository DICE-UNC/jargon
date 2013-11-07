/**
 * 
 */
package org.irods.jargon.conveyor.core;


/**
 * Common abstract superclass for sub-services that are aggregated under the
 * <code>ConveyorService</code>
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class AbstractConveyorComponentService {

	/**
	 * required dependency
	 */
	private ConveyorExecutorService conveyorExecutorService;

	public ConveyorExecutorService getConveyorExecutorService() {
		return conveyorExecutorService;
	}

	public void setConveyorExecutorService(
			ConveyorExecutorService conveyorExecutorService) {
		this.conveyorExecutorService = conveyorExecutorService;
	}

}
