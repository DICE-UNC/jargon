/**
 * 
 */
package org.irods.jargon.conveyor.basic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.irods.jargon.conveyor.core.ConveyorExecutionException;
import org.irods.jargon.conveyor.core.ConveyorService;
import org.irods.jargon.conveyor.core.FlowManagerService;
import org.irods.jargon.conveyor.flowmanager.flow.FlowSpec;
import org.irods.jargon.conveyor.flowmanager.flow.FlowSpecCacheService;
import org.irods.jargon.conveyor.flowmanager.flow.SelectorProcessor;
import org.irods.jargon.transfer.dao.domain.TransferAttempt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Flow Manager service handles client-side actions and workflows
 * 
 * @author Mike Conway - DICE
 * 
 */
public class BasicFlowManagerService implements FlowManagerService {

	private static final Logger log = LoggerFactory
			.getLogger(BasicFlowManagerService.class);

	/**
	 * Required dependency on conveyor service
	 */
	private ConveyorService conveyorService;

	/**
	 * Required dependency on a service to extract a cache of {@link FlowSpec}
	 * objects
	 */
	private FlowSpecCacheService flowSpecCacheService;

	/*
	 * eventually this could be injectable, right now it's just a simple deal
	 */
	private final SelectorProcessor selectorProcessor = new SelectorProcessor();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.conveyor.core.FlowManagerService#setFlowSpecCacheService
	 * (org.irods.jargon.conveyor.flowmanager.flow.FlowSpecCacheService)
	 */
	@Override
	public void setFlowSpecCacheService(
			final FlowSpecCacheService flowSpecCacheService)
			throws ConveyorExecutionException {
		if (flowSpecCacheService == null) {
			throw new IllegalArgumentException("null flowSpecCacheService");
		}

		this.flowSpecCacheService = flowSpecCacheService;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.conveyor.core.FlowManagerService#retrieveCandidateFlowSpecs
	 * (org.irods.jargon.transfer.dao.domain.TransferAttempt)
	 */
	@Override
	public List<FlowSpec> retrieveCandidateFlowSpecs(
			TransferAttempt transferAttempt) throws ConveyorExecutionException {

		log.info("retrieveCandidateFlowSpecs()");
		if (transferAttempt == null) {
			throw new IllegalArgumentException("null transfer attempt");
		}

		List<FlowSpec> candidateFlowSpecs = new ArrayList<FlowSpec>();

		List<FlowSpec> flowSpecs = flowSpecCacheService.getFlowSpecs();
		for (FlowSpec flowSpec : flowSpecs) {
			if (selectorProcessor.evaluateSelectorForTransfer(flowSpec,
					transferAttempt)) {
				log.info("added candidate flow spec:{}", flowSpec);
				candidateFlowSpecs.add(flowSpec);
			}
		}

		return Collections.unmodifiableList(candidateFlowSpecs);

	}

	/**
	 * @return the conveyorService
	 */
	public ConveyorService getConveyorService() {
		return conveyorService;
	}

	/**
	 * @param conveyorService
	 *            the conveyorService to set
	 */
	public void setConveyorService(ConveyorService conveyorService) {
		this.conveyorService = conveyorService;
	}
}
