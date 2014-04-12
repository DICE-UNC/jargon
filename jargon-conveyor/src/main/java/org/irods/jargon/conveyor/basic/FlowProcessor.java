/**
 * 
 */
package org.irods.jargon.conveyor.basic;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.conveyor.core.callables.AbstractConveyorCallable;
import org.irods.jargon.conveyor.flowmanager.flow.FlowManagerException;
import org.irods.jargon.conveyor.flowmanager.flow.FlowSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Actual processor of a flow, receives callbacks and locates the FlowSpec. This
 * object is meant to be injected into a callable
 * 
 * @author mikeconway
 *
 */
public class FlowProcessor {

	private final AbstractConveyorCallable conveyorCallable;

	private static final Logger log = LoggerFactory
			.getLogger(FlowProcessor.class);

	private List<FlowSpec> candidateFlowSpecs = new ArrayList<FlowSpec>();

	/**
	 * 
	 */
	public FlowProcessor(AbstractConveyorCallable conveyorCallable) {

		if (conveyorCallable == null) {
			throw new IllegalArgumentException("null conveyorCallable");
		}

		this.conveyorCallable = conveyorCallable;
	}

	public void init() throws FlowManagerException {
		log.info("doing init() processing");
		
		for (conveyorCallable.getConveyorService().getFlowManagerService().)
		
		

	}
}
