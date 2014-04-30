/**
 * 
 */
package org.irods.jargon.conveyor.flowmanager.microservice.builtins;

import java.io.FileNotFoundException;

import org.irods.jargon.conveyor.flowmanager.microservice.ConditionMicroservice;
import org.irods.jargon.conveyor.flowmanager.microservice.MicroserviceException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.core.transfer.TransferStatus.TransferType;
import org.irods.jargon.core.utils.LocalFileUtils;
import org.irods.jargon.datautils.tree.TreeSummarizingService;
import org.irods.jargon.datautils.tree.TreeSummarizingServiceImpl;
import org.irods.jargon.datautils.tree.TreeSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Inspect a source directory and decide whether it is useful to bundle up the
 * contents instead of a file-by-file transfer
 * 
 * @author Mike Conway - DICE
 * 
 */
public class InspectForBundleOperationMicroservice extends
		ConditionMicroservice {

	private static final Logger log = LoggerFactory
			.getLogger(InspectForBundleOperationMicroservice.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.conveyor.flowmanager.microservice.ConditionMicroservice
	 * #execute(org.irods.jargon.core.transfer.TransferStatus)
	 */
	@Override
	public ExecResult execute(final TransferStatus transferStatus)
			throws MicroserviceException {

		log.info("execute()...do a summary of the source dir");

		if (transferStatus.getTransferType() != TransferType.PUT) {
			throw new MicroserviceException(
					"this microservice only makes sense in a PUT operation");
		}

		TreeSummarizingService service = new TreeSummarizingServiceImpl(
				getContainerEnvironment().getConveyorService()
						.getIrodsAccessObjectFactory(), getInvocationContext()
						.getIrodsAccount());

		try {
			log.info("generating tree summary");
			TreeSummary summary = service
					.generateTreeSummaryForLocalFileTree(LocalFileUtils
							.normalizePath(transferStatus
									.getSourceFileAbsolutePath()));

			double averageSize = summary.calculateAverageLength();

			/*
			 * Some dumb heuristics...
			 */

			int score = 0;

			if (averageSize <= 20 * 1024) {
				score += 10;
			} else if (averageSize <= 200 * 1024) {
				score += 5;
			} else if (averageSize <= 1 * 1024 * 1024) {
				score += 2;
			}

			if (summary.getMaxLength() <= 20 * 1024) {
				score += 10;
			} else if (summary.getMaxLength() <= 200 * 1024) {
				score += 5;
			} else if (summary.getMaxLength() <= 20 * 1024 * 1024) {
				score += 2;
			}

			if (summary.getTotalFiles() >= 3000) {
				score += 10;
			} else if (summary.getTotalFiles() >= 2000) {
				score += 5;
			} else if (summary.getTotalFiles() >= 1000) {
				score += 2;
			}

			log.info("total score:{}", score);
			if (score > 20) {
				return ExecResult.CONTINUE;
			} else {
				return ExecResult.TERMINATE_FLOW_FAIL_PRECONDITION;
			}

		} catch (FileNotFoundException e) {
			log.error("source file not found:{}",
					transferStatus.getSourceFileAbsolutePath());
			throw new MicroserviceException("cannot find source file");
		} catch (JargonException e) {
			log.error("jargon exception", e);
			throw new MicroserviceException(
					"jargon exception running microservice", e);
		}

	}

}
