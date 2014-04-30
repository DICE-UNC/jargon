/**
 * 
 */
package org.irods.jargon.conveyor.flowmanager.flow.dsl;

import org.irods.jargon.conveyor.flowmanager.flow.FlowSpec;

/**
 * Base to initiate the start of a FlowSpec in the DSL
 * 
 * @author Mike Conway - DICE
 * 
 */
public class Flow {

	/**
	 * 
	 */
	private Flow() {
	}

	/**
	 * Begin the definition of a flow chain here
	 * 
	 * @return
	 */
	public static FlowActionSelectorSpecification define() {
		FlowSpec flowSpec = new FlowSpec();
		FlowActionSelectorSpecification flowSelectorSpecification = new FlowActionSelectorSpecification(
				flowSpec);
		return flowSelectorSpecification;
	}

}
