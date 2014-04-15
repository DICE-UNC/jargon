/**
 * 
 */
package org.irods.jargon.conveyor.basic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.irods.jargon.conveyor.core.AbstractConveyorComponentService;
import org.irods.jargon.conveyor.core.FlowManagerService;
import org.irods.jargon.conveyor.flowmanager.flow.FlowSpec;
import org.irods.jargon.conveyor.flowmanager.flow.FlowSpecCacheService;
import org.irods.jargon.conveyor.flowmanager.flow.Selector.FlowActionEnum;
import org.irods.jargon.transfer.dao.domain.TransferAttempt;
import org.irods.jargon.transfer.dao.domain.TransferType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Flow Manager service handles client-side actions and workflows
 * 
 * @author Mike Conway - DICE
 * 
 */
public class BasicFlowManagerService extends AbstractConveyorComponentService
		implements FlowManagerService {

	private static final Logger log = LoggerFactory
			.getLogger(BasicFlowManagerService.class);

	/**
	 * Required dependency on a service to extract a cache of {@link FlowSpec}
	 * objects
	 */
	private FlowSpecCacheService flowSpecCacheService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.conveyor.core.FlowManagerService#setFlowSpecCacheService
	 * (org.irods.jargon.conveyor.flowmanager.flow.FlowSpecCacheService)
	 */
	@Override
	public void setFlowSpecCacheService(
			final FlowSpecCacheService flowSpecCacheService) {
		if (flowSpecCacheService == null) {
			throw new IllegalArgumentException("null flowSpecCacheService");
		}

		this.flowSpecCacheService = flowSpecCacheService;
	}

	public List<FlowSpec> findFlowSpecsForTransferAttempt(
			final TransferAttempt transferAttempt) {

		log.info("findFlowSpecsForTransferAttempt()");
		if (transferAttempt == null) {
			throw new IllegalArgumentException("null transferAttempt");
		}

		log.info("transfer attempt to evaluate:{}", transferAttempt);

		/*
		 * bounch thru a clone of the flow specs looking for matches on the
		 * selectors
		 */

		List<FlowSpec> candidateFlowSpecs = new ArrayList<FlowSpec>();

		for (FlowSpec flowSpec : flowSpecCacheService.getFlowSpecs()) {

			if (evaluateSelectorForTransfer(flowSpec, transferAttempt)) {
				log.info("adding candidate:{}", flowSpec);
				candidateFlowSpecs.add(flowSpec);
			}

		}

		return Collections.unmodifiableList(candidateFlowSpecs);

	}

	/**
	 * We're starting out dumb and simple, it's a literal, a *, or literal+* for
	 * matching. Later we may switch to a regex or pluggable selectors
	 * 
	 * @param flowSpec
	 *            {@link FlowSpe} that is a candidate for matching
	 * @return
	 */
	private boolean evaluateSelectorForTransfer(FlowSpec flowSpec,
			TransferAttempt transferAttempt) {

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

		if (flowSpec.getSelector().getHostSelector().isEmpty()
				|| flowSpec.getSelector().getHostSelector().equals("*")) {
			// matches host selector
		} else {
			int idx = flowSpec.getSelector().getHostSelector().indexOf('*');

			if (idx == -1) {
				// no wild card *, so exact match
				if (flowSpec
						.getSelector()
						.getHostSelector()
						.trim()
						.equals(transferAttempt.getTransfer().getGridAccount()
								.getHost().trim())) {
					log.info("exact match on host");
				} else {
					log.info("no host match, discard");
					return false;
				}
			} else {
				// flow spec has a wild card, so match up to the wild card
				String wildCardHost = flowSpec.getSelector().getHostSelector()
						.substring(0, idx);
				String transferHost = transferAttempt.getTransfer()
						.getGridAccount().getHost();
				if (transferHost.length() <= idx) {
					return false;
				}

			}

		}

		return true;

	}

	/**
	 * 
	 */
	public BasicFlowManagerService() {

	}

	public void init() {

		log.info("init()");
		log.info("collect flowspecs from the directories");

	}

}
