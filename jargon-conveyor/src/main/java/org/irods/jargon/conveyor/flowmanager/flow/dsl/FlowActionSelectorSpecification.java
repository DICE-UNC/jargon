/**
 * 
 */
package org.irods.jargon.conveyor.flowmanager.flow.dsl;

import org.irods.jargon.conveyor.flowmanager.flow.FlowSpec;
import org.irods.jargon.conveyor.flowmanager.flow.Selector.FlowActionEnum;

/**
 * Sets up the ability to chose a required 'for' action
 * 
 * @author Mike Conway - DICE
 * 
 */
public class FlowActionSelectorSpecification extends FlowSpecDslElement {

	/**
	 * 
	 */
	public FlowActionSelectorSpecification(final FlowSpec flowSpec) {
		super(flowSpec);
	}

	/**
	 * Add an action to which this flow pertains. This defaults to 'any' action
	 * 
	 * @param flowActionEnum
	 *            {@link FlowActionEnum}
	 * @return
	 */
	public FlowHostSelectorSpecification forAction(
			final FlowActionEnum flowActionEnum) {
		if (flowActionEnum == null) {
			throw new IllegalArgumentException("null flowActionEnum");
		}

		getFlowSpec().getSelector().setFlowActionEnum(flowActionEnum);

		return new FlowHostSelectorSpecification(getFlowSpec());

	}

	/**
	 * Apply this specification to any action at all
	 * 
	 * @return
	 */
	public FlowHostSelectorSpecification forAnyAction() {

		getFlowSpec().getSelector().setFlowActionEnum(FlowActionEnum.ANY);
		return new FlowHostSelectorSpecification(getFlowSpec());

	}

}
