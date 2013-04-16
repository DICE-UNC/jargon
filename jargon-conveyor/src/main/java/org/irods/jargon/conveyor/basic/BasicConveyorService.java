/**
 * 
 */
package org.irods.jargon.conveyor.basic;

import org.irods.jargon.conveyor.core.AbstractConveyorService;
import org.irods.jargon.conveyor.core.ConveyorExecutorService;

/**
 * Default conveyor service, based on the standard internal Derby database.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class BasicConveyorService extends AbstractConveyorService {

	/**
	 * Injected reference to the {@link ConveyorExecutorService} that serves to
	 * lock the queue for operations that act on data that could be accessed by
	 * an already running transfer
	 */
	private ConveyorExecutorService conveyorExecutorService;

	public BasicConveyorService() {

	}

}
