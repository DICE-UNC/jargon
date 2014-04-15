/**
 * 
 */
package org.irods.jargon.conveyor.flowmanager.flow.dsl;

import org.irods.jargon.conveyor.flowmanager.flow.FlowSpec;

/**
 * @author mikeconway
 *
 */
public class PreFileChainSpecification extends FlowSpecDslMicroserviceElement {

	/**
	 * @param flowSpec
	 */
	public PreFileChainSpecification(FlowSpec flowSpec) {
		super(flowSpec);
	}

	/**
	 * Add the given microservice as the next link in the pre-file chain
	 * 
	 * @param microserviceFullyQualifiedClassName
	 * @return
	 */
	public PreFileChainSpecification addPreFileMicroservice(
			final String microserviceFullyQualifiedClassName) {

		this.getFlowSpec().getPreFileChain()
				.add(microserviceFullyQualifiedClassName);
		return this;

	}

	/**
	 * End the pre file chain
	 * 
	 * @return
	 */
	public PostFileChainSpecification endPreFileChain() {
		return new PostFileChainSpecification(this.getFlowSpec());
	}

}
