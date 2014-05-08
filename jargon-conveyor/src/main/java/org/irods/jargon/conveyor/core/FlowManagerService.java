/**
 * 
 */
package org.irods.jargon.conveyor.core;

import java.util.List;

import org.irods.jargon.conveyor.flowmanager.flow.FlowSpec;
import org.irods.jargon.conveyor.flowmanager.flow.FlowSpecCacheService;
import org.irods.jargon.transfer.dao.domain.TransferAttempt;

/**
 * Manages attached rules and workflows.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface FlowManagerService {

	/**
	 * Required dependency on a service that can process source scripts into
	 * FlowSpecs
	 * 
	 * @param flowSpecCacheService
	 *            {@link FlowSpecCacheService} that will scan directories to
	 *            produce flow specifications
	 * @throws ConveyorExecutionException
	 */
	void setFlowSpecCacheService(final FlowSpecCacheService flowSpecCacheService)
			throws ConveyorExecutionException;

	/**
	 * Given a <code>TransferAttempt</code> find the candidate
	 * <code>FlowSpec</code> objects that represent flows that are matched based
	 * on selectors
	 * 
	 * @param transferAttempt
	 *            {@link TransferAttempt} to match with the given flowSpecs
	 * @return <code>List</code> of {@link FlowSpec}, which are thread safe
	 *         clones, of the available matches for the given transfer, based on
	 *         the configured selectors
	 * @throws ConveyorExecutionException
	 */
	List<FlowSpec> retrieveCandidateFlowSpecs(
			final TransferAttempt transferAttempt)
			throws ConveyorExecutionException;
}
