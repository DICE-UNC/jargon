package org.irods.jargon.conveyor.flowmanager.flow.dsl;

import org.irods.jargon.conveyor.flowmanager.flow.FlowSpec;

/**
 * Parent class for an element of the FlowSpec dsl flow chain builder
 * 
 * @author mikeconway
 * 
 */
public class FlowSpecDslElement {

	private final FlowSpec flowSpec;

	public FlowSpecDslElement(final FlowSpec flowSpec) {
		super();
		if (flowSpec == null) {
			throw new IllegalArgumentException("null flowSpec");
		}
		this.flowSpec = flowSpec;
	}

	protected FlowSpec getFlowSpec() {
		return flowSpec;
	}

}