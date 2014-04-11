/**
 * 
 */
package org.irods.jargon.conveyor.flowmanager.flow.dsl;

import org.irods.jargon.conveyor.flowmanager.flow.FlowSpec;
import org.irods.jargon.conveyor.flowmanager.microservice.Microservice;

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
	public PostFileChainSpecification(FlowSpec flowSpec) {
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

		Microservice microservice = this
				.createMicroserviceInstance(microserviceFullyQualifiedClassName);
		this.getFlowSpec().getPostFileChain().add(microservice);
		return this;

	}

	/**
	 * End the post file operation chain
	 * 
	 * @return
	 */
	public PostOperationChainSpecification endPostFileChain() {
		return new PostOperationChainSpecification(this.getFlowSpec());
	}

}
