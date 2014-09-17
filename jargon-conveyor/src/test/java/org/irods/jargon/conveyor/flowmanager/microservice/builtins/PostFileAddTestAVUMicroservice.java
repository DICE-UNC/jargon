/**
 * 
 */
package org.irods.jargon.conveyor.flowmanager.microservice.builtins;

import java.io.File;

import org.irods.jargon.conveyor.flowmanager.microservice.Microservice;
import org.irods.jargon.conveyor.flowmanager.microservice.MicroserviceException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.core.utils.LocalFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A basic microservice that will send a log message, and depending on log
 * level, dump interesting things.
 * <p/>
 * This microservice will ask to skip any file containing the specific string
 * SKIPME in the source or target of the transfer status
 * 
 * @author Mike Conway - DICE
 * 
 */
public class PostFileAddTestAVUMicroservice extends Microservice {

	private static final Logger log = LoggerFactory
			.getLogger(PostFileAddTestAVUMicroservice.class);

	public static final String SKIPME = "SKIPME";

	@Override
	public ExecResult execute(final TransferStatus transferStatus)
			throws MicroserviceException {
		log.info("execute()");

		try {
			AvuData avuData = AvuData.instance("SOURCEFILE", LocalFileUtils
					.normalizePath(transferStatus.getSourceFileAbsolutePath()),
					"");
			File sourceAsFile = new File(
					transferStatus.getSourceFileAbsolutePath());
			IRODSFile targetAsFile = getContainerEnvironment()
					.getConveyorService()
					.getIrodsAccessObjectFactory()
					.getIRODSFileFactory(
							getInvocationContext().getIrodsAccount())
					.instanceIRODSFile(
							transferStatus.getTargetFileAbsolutePath(),
							sourceAsFile.getName());
			DataObjectAO dataObjectAO = getContainerEnvironment()
					.getConveyorService().getIrodsAccessObjectFactory()
					.getDataObjectAO(getInvocationContext().getIrodsAccount());
			log.info("adding an avu to:{}", targetAsFile.getAbsolutePath());
			dataObjectAO
					.addAVUMetadata(targetAsFile.getAbsolutePath(), avuData);
			log.info("avu added");
			return ExecResult.CONTINUE;
		} catch (JargonException e) {
			log.error("error adding avu post transfer", e);
			throw new MicroserviceException("error adding avu", e);
		}

	}

}
