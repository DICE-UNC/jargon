/**
 * 
 */
package org.irods.jargon.conveyor.flowmanager.flow.dsl;

import org.irods.jargon.conveyor.flowmanager.flow.FlowSpec;
import org.irods.jargon.conveyor.flowmanager.microservice.ErrorHandlerMicroservice;
import org.irods.jargon.conveyor.flowmanager.microservice.Microservice;

/**
 * Final link in a flow, this microservice can do any error handling if the flow
 * is aborted abnormally
 * 
 * @author Mike Conway - DICE
 * 
 */
public class ErrorHandlerSpecification extends FlowSpecDslMicroserviceElement {

	/**
	 * @param flowSpec
	 */
	public ErrorHandlerSpecification(final FlowSpec flowSpec) {
		super(flowSpec);
	}

	/**
	 * End the flow with no specified recovery error handler
	 * 
	 * @return
	 */
	public FlowSpec endFlowWithoutErrorHandler() {
		return getFlowSpec();
	}

	/**
	 * Set an error/recovery handler microservice and then end the flow chain.
	 * This returns a completed, executable flow.
	 * 
	 * @param fullyQualifiedMicroserviceClassName
	 *            <code>String</code> with a FQCN for a subclass
	 * @return
	 */
	public FlowSpec endFlowWithErrorHandler(
			final String fullyQualifiedMicroserviceClassName) {

		Microservice microservice = createMicroserviceInstance(fullyQualifiedMicroserviceClassName);

		if (microservice instanceof ErrorHandlerMicroservice) {
			// ok
		} else {
			throw new FlowSpecificationException(
					"error microservice must be subclass of ErrorHandlerMicroservice");
		}

		getFlowSpec().setErrorHandler(fullyQualifiedMicroserviceClassName);
		return getFlowSpec();

	}
}
