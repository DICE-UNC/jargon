/**
 * 
 */
package org.irods.jargon.conveyor.flowmanager.flow.dsl;

import org.irods.jargon.conveyor.flowmanager.flow.FlowSpec;

/**
 * Handles post file chains in the DSL
 * 
 * @author Mike Conway - DICE
 * 
 */
public class PostFileChainSpecification extends FlowSpecDslMicroserviceElement {

	/**
	 * @param flowSpec
	 */
	public PostFileChainSpecification(final FlowSpec flowSpec) {
		super(flowSpec);
	}

	/**
	 * Add the given microservice as the next link in the pre-operation chain
	 * 
	 * @param microserviceFullyQualifiedClassName
	 * @return
	 */
	public PostFileChainSpecification addPostFileMicroservice(
			final String microserviceFullyQualifiedClassName) {

		createMicroserviceInstance(microserviceFullyQualifiedClassName);
		getFlowSpec().getPostFileChain().add(
				microserviceFullyQualifiedClassName);
		return this;

	}

	/**
	 * End the post file operation chain
	 * 
	 * @return
	 */
	public PostOperationChainSpecification endPostFileChain() {
		return new PostOperationChainSpecification(getFlowSpec());
	}

}
