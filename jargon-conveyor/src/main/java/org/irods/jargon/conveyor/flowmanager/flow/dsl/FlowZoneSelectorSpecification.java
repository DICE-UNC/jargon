/**
 * 
 */
package org.irods.jargon.conveyor.flowmanager.flow.dsl;

import org.irods.jargon.conveyor.flowmanager.flow.FlowSpec;
import org.irods.jargon.conveyor.flowmanager.flow.Selector;

/**
 * @author Mike Conway - DICE
 *
 */
public class FlowZoneSelectorSpecification extends FlowSpecDslElement {

	public FlowZoneSelectorSpecification(FlowSpec flowSpec) {
		super(flowSpec);
	}

	/**
	 * Add a selector (a string with wildcard or regex) for the zone to
	 * consider. Blank or * will select any zone.
	 * 
	 * @param hostSelector
	 *            <code>String</code> with the zone selector to which this will
	 *            apply
	 * @return
	 */
	public ConditionSpecification forZone(final String zoneSelector) {
		if (zoneSelector == null) {
			throw new IllegalArgumentException("null zoneSelector");
		}

		String mySelector;
		if (zoneSelector.isEmpty()) {
			mySelector = Selector.ANY;
		} else {
			mySelector = zoneSelector;
		}
		this.getFlowSpec().getSelector().setZoneSelector(mySelector);

		return new ConditionSpecification(this.getFlowSpec());

	}

	/**
	 * Select any zone for this flow
	 * 
	 * @return
	 */
	public ConditionSpecification forAnyZone() {
		this.getFlowSpec().getSelector().setZoneSelector(Selector.ANY);
		return new ConditionSpecification(this.getFlowSpec());

	}
}
