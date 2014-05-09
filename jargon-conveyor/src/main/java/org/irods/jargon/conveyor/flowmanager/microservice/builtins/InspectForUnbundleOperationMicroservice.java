/**
 * 
 */
package org.irods.jargon.conveyor.flowmanager.microservice.builtins;

import java.io.File;
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
 * Inspect a source and see if a tar file is being transferred that should be unbundled
 * 
 * @author Mike Conway - DICE
 * 
 */
public class InspectForUnbundleOperationMicroservice extends
		ConditionMicroservice {

	private static final Logger log = LoggerFactory
			.getLogger(InspectForUnbundleOperationMicroservice.class);

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

		File localFile = new File(transferStatus.getSourceFileAbsolutePath());

		if (!localFile.exists()) {
			log.info("source file does not exist, consider a failed condition");
			return ExecResult.TERMINATE_FLOW_FAIL_PRECONDITION;
		}

		if (localFile.isDirectory()) {
			log.info("source file is  a dir, consider a failed condition");
			return ExecResult.TERMINATE_FLOW_FAIL_PRECONDITION;
		}

		String ext = LocalFileUtils.getFileExtension(localFile.getName());
		log.info("extension is:{}", ext);
		
		if (ext.equals(".tar")) {
			log.info("is a tar...");
			return ExecResult.CONTINUE;
		} else {
			log.info("not a tar");
			return ExecResult.TERMINATE_FLOW_FAIL_PRECONDITION;
		}
		

	}

}
