package org.irods.jargon.conveyor.flowmanager.flow.dsl;

import org.irods.jargon.conveyor.flowmanager.flow.FlowSpec;
import org.irods.jargon.conveyor.flowmanager.microservice.ConditionMicroservice;
import org.irods.jargon.conveyor.flowmanager.microservice.Microservice;

/**
 * Condition portion of flow specification
 * 
 * @author Mike Conway - DICE
 * 
 */
public class ConditionSpecification extends FlowSpecDslMicroserviceElement {

	/**
	 * 
	 * @param flowSpec
	 */
	public ConditionSpecification(final FlowSpec flowSpec) {
		super(flowSpec);
	}

	/**
	 * Add a condition to the flow that will do a pre-check to see whether the
	 * chains should run
	 * 
	 * @param fullyQualifiedMicroserviceClassName
	 *            <code>String</code> with a FQCN for a subclass of
	 *            {@link ConditionMicroservice} that will be loaded.
	 * @return
	 */
	public PreOperationChainSpecification when(
			final String fullyQualifiedMicroserviceClassName) {

		Microservice microservice = createMicroserviceInstance(fullyQualifiedMicroserviceClassName);

		if (microservice instanceof ConditionMicroservice) {
			// ok
		} else {
			throw new FlowSpecificationException(
					"condition microservice must be subclass of ConditionMicroservice");
		}

		getFlowSpec().setCondition(fullyQualifiedMicroserviceClassName);
		return new PreOperationChainSpecification(getFlowSpec());

	}

	/**
	 * Continue the flow with no particular pre-conditions
	 * 
	 * @return
	 */
	public PreOperationChainSpecification onAllConditions() {
		return new PreOperationChainSpecification(getFlowSpec());
	}

}
