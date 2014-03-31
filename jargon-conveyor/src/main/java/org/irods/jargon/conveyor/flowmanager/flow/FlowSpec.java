/**
 * 
 */
package org.irods.jargon.conveyor.flowmanager.flow;

import java.util.List;

/**
 * Represents a specification for a single flow. This is a workflow chain
 * associated with a selector
 * 
 * @author Mike Conway - DICE
 * 
 */
public class FlowSpec {

	private Selector selector;
	private MicroserviceDef condition;
	private List<MicroserviceDef> microserviceDefChain;

	/**
	 * 
	 */
	public FlowSpec() {
	}

}
