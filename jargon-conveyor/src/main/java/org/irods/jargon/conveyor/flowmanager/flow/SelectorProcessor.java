/**
 * 
 */
package org.irods.jargon.conveyor.flowmanager.flow;

import org.irods.jargon.conveyor.basic.BasicFlowManagerService;
import org.irods.jargon.conveyor.flowmanager.flow.Selector.FlowActionEnum;
import org.irods.jargon.transfer.dao.domain.TransferAttempt;
import org.irods.jargon.transfer.dao.domain.TransferType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Processor for comparing flows to transfers to decide whether the flow is
 * selected to run. This can later be subclassed to handle regex and other
 * things
 * 
 * @author Mike Conway - DICE
 * 
 */
public class SelectorProcessor {

	private static final Logger log = LoggerFactory
			.getLogger(BasicFlowManagerService.class);

	public SelectorProcessor() {
	}

	/**
	 * We're starting out dumb and simple, it's a literal, a *, or literal+* for
	 * matching. Later we may switch to a regex or pluggable selectors
	 * 
	 * @param flowSpec
	 *            {@link FlowSpe} that is a candidate for matching
	 * @return
	 */
	public boolean evaluateSelectorForTransfer(final FlowSpec flowSpec,
			final TransferAttempt transferAttempt) {

		log.info("match on action...");
		if (flowSpec.getSelector().getFlowActionEnum() == FlowActionEnum.ANY) {
			// matches
		} else if (flowSpec.getSelector().getFlowActionEnum() == FlowActionEnum.GET
				&& transferAttempt.getTransfer().getTransferType() == TransferType.GET) {
			// matches
		} else if (flowSpec.getSelector().getFlowActionEnum() == FlowActionEnum.PUT
				&& transferAttempt.getTransfer().getTransferType() == TransferType.PUT) {
			// matches
		} else {

			return false;
		}

		log.info("passes action...check host...");

		boolean passes = compareSelectorToTransferValueAsStringWithWildcard(
				flowSpec.getSelector().getHostSelector(), transferAttempt
						.getTransfer().getGridAccount().getHost());

		if (!passes) {
			log.info("fails host match");
			return false;
		}

		log.info("passes action...check host...");

		passes = compareSelectorToTransferValueAsStringWithWildcard(flowSpec
				.getSelector().getZoneSelector(), transferAttempt.getTransfer()
				.getGridAccount().getZone());

		if (!passes) {
			log.info("fails zone match");
			return false;
		}

		log.info("matched!");
		return true;

	}

	boolean compareSelectorToTransferValueAsStringWithWildcard(
			final String selectorValue, final String transferValue) {

		/*
		 * wildcard : blah* idx = 4 transfer : blahrk
		 * 
		 * passes
		 * 
		 * 
		 * 
		 * wildcard : blahhhh* idx = 7 transfer: blahhhh
		 * 
		 * 
		 * fails
		 */

		if (selectorValue.isEmpty() || selectorValue.equals("*")) {
			// matches selector
		} else {
			int idx = selectorValue.indexOf('*');

			if (idx == -1) {
				// no wild card *, so exact match
				if (selectorValue.equals(transferValue.trim())) {
				} else {
					return false;
				}
			} else {
				// flow spec has a wild card, so match up to the wild card
				// blahh* becomes blahh, and idx is used to trim the comparison
				// value

				String wildCardVal = selectorValue.substring(0, idx);
				String transferWildCardVal = transferValue;
				if (transferWildCardVal.length() < idx) {
					// wild card longer than the host
					return false;
				} else {
					// trim transfer to match wild card and compare
					transferWildCardVal = transferWildCardVal.substring(0, idx);
					if (transferWildCardVal.equals(wildCardVal)) {
						// ok
					} else {
						return false;
					}

				}

			}

		}
		// if I get here its legit match
		return true;

	}

}
