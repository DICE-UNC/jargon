/**
 * 
 */
package org.irods.jargon.conveyor.flowmanager.microservice.builtins;

import java.io.File;
import java.io.FileNotFoundException;

import org.irods.jargon.conveyor.flowmanager.microservice.Microservice;
import org.irods.jargon.conveyor.flowmanager.microservice.MicroserviceException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.datautils.filearchive.LocalTarFileArchiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Microservice to tar up a given directory (based on the source collection of
 * the transfer).
 * 
 * @author Mike Conway - DICE
 *
 */
public class TarCollectionMicroservice extends Microservice {

	/***
	 * Name of the parameter that I will look for if there is a specific tar
	 * file name
	 */
	public static final String TAR_FILE_NAME = TarCollectionMicroservice.class
			.getName() + ":TAR_FILE_NAME";

	private static final Logger log = LoggerFactory
			.getLogger(TarCollectionMicroservice.class);

	@Override
	public ExecResult execute(TransferStatus transferStatus)
			throws MicroserviceException {

		log.info("execute");

		String tarFileName = (String) this.getInvocationContext()
				.getSharedProperties().get(TAR_FILE_NAME);

		/*
		 * TODO: where do I put this? for now use target dir and contents.tar
		 */
		if (tarFileName == null) {
			log.info("no tar file, create a temp dir for this tar file");
			StringBuilder targetDir = new StringBuilder();
			targetDir.append(transferStatus.getTargetFileAbsolutePath());
			targetDir.append("/contents.tar");
			tarFileName = targetDir.toString();
		}

		log.info("tar file name will be:{}", tarFileName);

		LocalTarFileArchiver localFileTarArchiver = new LocalTarFileArchiver(
				transferStatus.getSourceFileAbsolutePath(), tarFileName);

		try {
			File archiveFile = localFileTarArchiver.createArchive();
			this.getInvocationContext()
					.getSharedProperties()
					.put(EnqueueTransferMicroservice.SOURCE_FILE_NAME,
							archiveFile.getAbsolutePath());
			return ExecResult.CONTINUE;

		} catch (FileNotFoundException e) {
			log.error("fileNotFoundException on create of tar file", e);
			throw new MicroserviceException(
					"microservice exception creating tar file", e);
		} catch (JargonException e) {
			log.error("JargonException on create of tar file", e);
			throw new MicroserviceException(
					"JargonException creating tar file", e);
		}

	}

}
