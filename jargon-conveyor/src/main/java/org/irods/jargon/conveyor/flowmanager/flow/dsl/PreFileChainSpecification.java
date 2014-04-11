/**
 * 
 */
package org.irods.jargon.conveyor.flowmanager.flow.dsl;

import org.irods.jargon.conveyor.flowmanager.flow.FlowSpec;
import org.irods.jargon.conveyor.flowmanager.microservice.Microservice;

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

		Microservice microservice = this
				.createMicroserviceInstance(microserviceFullyQualifiedClassName);
		this.getFlowSpec().getPreFileChain().add(microservice);
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
