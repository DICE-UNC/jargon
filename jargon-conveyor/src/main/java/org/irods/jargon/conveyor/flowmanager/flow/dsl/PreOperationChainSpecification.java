/**
 * 
 */
package org.irods.jargon.conveyor.flowmanager.flow.dsl;

import org.irods.jargon.conveyor.flowmanager.flow.FlowSpec;

/**
 * @author Mike Conway - DICE
 * 
 */
public class PreOperationChainSpecification extends
		FlowSpecDslMicroserviceElement {

	/**
	 * @param flowSpec
	 */
	public PreOperationChainSpecification(final FlowSpec flowSpec) {
		super(flowSpec);
	}

	/**
	 * Add the given microervice as the next link in the pre-operation chain
	 * 
	 * @param microserviceFullyQualifiedClassName
	 * @return
	 */
	public PreOperationChainSpecification addPreOperationMicroservice(
			final String microserviceFullyQualifiedClassName) {

		createMicroserviceInstance(microserviceFullyQualifiedClassName);
		getFlowSpec().getPreOperationChain().add(
				microserviceFullyQualifiedClassName);
		return this;

	}

	/**
	 * End the pre operation chain
	 * 
	 * @return
	 */
	public PreFileChainSpecification endPreOperationChain() {
		return new PreFileChainSpecification(getFlowSpec());
	}
}
