/**
 * 
 */
package org.irods.jargon.conveyor.flowmanager.flow.dsl;

import org.irods.jargon.conveyor.flowmanager.flow.FlowSpec;

/**
 * @author Mike Conway
 *
 */
public class FlowCondition {

	private FlowSpec flowSpec;
	
	/**
	 * 
	 */
	public FlowCondition(FlowSpec flowSpec) {
		if (flowSpec == null) {
			throw new IllegalArgumentException("null flowSpec");
		}
		this.flowSpec = flowSpec;
	}
	
	

	
	

}
