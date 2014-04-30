/**
 * 
 */
package org.irods.jargon.conveyor.flowmanager.microservice.builtins;

import org.irods.jargon.conveyor.flowmanager.microservice.Microservice;

/**
 * Microservice to enqueue a transfer. This will use the current transfer status
 * and then check the shared whiteboard for any override parameters.
 * 
 * @author Mike Conway - DICE
 *
 */
public class EnqueueTransferMicroservice extends Microservice {

	/**
	 * Parameters that will override those in the transfer status for the new
	 * transfer
	 */
	public static final String SOURCE_FILE_NAME = EnqueueTransferMicroservice.class
			.getName() + ":SOURCE_PATH";

	public static final String TARGET_FILE_NAME = EnqueueTransferMicroservice.class
			.getName() + ":TARGET_PATH";

}
