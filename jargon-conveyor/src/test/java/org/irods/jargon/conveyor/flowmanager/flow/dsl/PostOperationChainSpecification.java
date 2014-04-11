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
public class PostOperationChainSpecification extends
		FlowSpecDslMicroserviceElement {

	public PostOperationChainSpecification(FlowSpec flowSpec) {
		super(flowSpec);
	}

	/**
	 * Add the given microservice as the next link in the post-operation chain
	 * 
	 * @param microserviceFullyQualifiedClassName
	 * @return
	 */
	public PostOperationChainSpecification addPostOperationMicroservice(
			final String microserviceFullyQualifiedClassName) {

		Microservice microservice = this
				.createMicroserviceInstance(microserviceFullyQualifiedClassName);
		this.getFlowSpec().getPostOperationChain().add(microservice);
		return this;

	}

	/**
	 * End the post file operation chain
	 * 
	 * @return
	 */
	public ErrorHandlerSpecification endPostOperationChain() {
		return new ErrorHandlerSpecification(this.getFlowSpec());
	}

}
