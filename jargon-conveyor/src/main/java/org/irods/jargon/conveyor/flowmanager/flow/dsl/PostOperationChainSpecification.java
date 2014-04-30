/**
 * 
 */
package org.irods.jargon.conveyor.flowmanager.flow.dsl;

import org.irods.jargon.conveyor.flowmanager.flow.FlowSpec;

/**
 * @author Mike Conway - DICE
 * 
 */
public class PostOperationChainSpecification extends
		FlowSpecDslMicroserviceElement {

	public PostOperationChainSpecification(final FlowSpec flowSpec) {
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

		createMicroserviceInstance(microserviceFullyQualifiedClassName);
		getFlowSpec().getPostOperationChain().add(
				microserviceFullyQualifiedClassName);
		return this;

	}

	/**
	 * End the post file operation chain
	 * 
	 * @return
	 */
	public ErrorHandlerSpecification endPostOperationChain() {
		return new ErrorHandlerSpecification(getFlowSpec());
	}

}
