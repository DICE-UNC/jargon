/**
 * 
 */
package org.irods.jargon.conveyor.flowmanager.flow.dsl;

import org.irods.jargon.conveyor.flowmanager.flow.FlowSpec;
import org.irods.jargon.conveyor.flowmanager.microservice.Microservice;

/**
 * @author Mike Conway - DICE
 *
 */
public class PreOperationChainSpecification extends
		FlowSpecDslMicroserviceElement {

	/**
	 * @param flowSpec
	 */
	public PreOperationChainSpecification(FlowSpec flowSpec) {
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

		Microservice microservice = this
				.createMicroserviceInstance(microserviceFullyQualifiedClassName);
		this.getFlowSpec().getPreOperationChain().add(microservice);
		return this;

	}

	/**
	 * End the pre operation chain
	 * 
	 * @return
	 */
	public PreFileChainSpecification endPreOperationChain() {
		return new PreFileChainSpecification(this.getFlowSpec());
	}
}
