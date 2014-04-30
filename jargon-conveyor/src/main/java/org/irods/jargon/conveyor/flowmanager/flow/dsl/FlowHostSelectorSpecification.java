/**
 * 
 */
package org.irods.jargon.conveyor.flowmanager.flow.dsl;

import org.irods.jargon.conveyor.flowmanager.flow.FlowSpec;
import org.irods.jargon.conveyor.flowmanager.flow.Selector;

/**
 * Allows specification of a host selector
 * 
 * @author Mike Conway - DICE
 * 
 */
public class FlowHostSelectorSpecification extends FlowSpecDslElement {

	public FlowHostSelectorSpecification(final FlowSpec flowSpec) {
		super(flowSpec);
	}

	/**
	 * Add a selector (a string with wildcard or regex) for the host to
	 * consider. Blank or * will select any hose.
	 * 
	 * @param hostSelector
	 *            <code>String</code> with the host selector to which this will
	 *            apply
	 * @return
	 */
	public FlowZoneSelectorSpecification forHost(final String hostSelector) {
		if (hostSelector == null) {
			throw new IllegalArgumentException("null hostSpecification");
		}

		String mySelector;
		if (hostSelector.isEmpty()) {
			mySelector = Selector.ANY;
		} else {
			mySelector = hostSelector;
		}
		getFlowSpec().getSelector().setHostSelector(mySelector);

		return new FlowZoneSelectorSpecification(getFlowSpec());

	}

	/**
	 * Select any host for this flow
	 * 
	 * @return
	 */
	public FlowZoneSelectorSpecification forAnyHost() {
		getFlowSpec().getSelector().setHostSelector(Selector.ANY);
		return new FlowZoneSelectorSpecification(getFlowSpec());

	}

}
