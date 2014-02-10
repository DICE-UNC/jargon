/**
 * 
 */
package org.irods.jargon.conveyor.synch;

import org.irods.jargon.conveyor.core.ConveyorService;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Process a one-way local to iRODS diff
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class LocalToIRODSDiffProcessor extends
		AbstractSynchronizingDiffProcessor {

	private static final Logger log = LoggerFactory
			.getLogger(LocalToIRODSDiffProcessor.class);

	public LocalToIRODSDiffProcessor(ConveyorService conveyorService,
			TransferControlBlock transferControlBlock) {
		super(conveyorService, transferControlBlock);
	}

}
