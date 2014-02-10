/**
 * 
 */
package org.irods.jargon.conveyor.synch;

import org.irods.jargon.conveyor.core.ConveyorExecutionException;
import org.irods.jargon.datautils.tree.FileTreeModel;
import org.irods.jargon.transfer.dao.domain.Transfer;

/**
 * Process a one-way local to iRODS diff
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class LocalToIRODSDiffProcessor extends
		AbstractSynchronizingDiffProcessor {

	/**
	 * 
	 */
	public LocalToIRODSDiffProcessor() {
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.synch.AbstractSynchronizingDiffProcessor#
	 * processDiff(org.irods.jargon.transfer.dao.domain.Transfer,
	 * org.irods.jargon.datautils.tree.FileTreeModel)
	 */
	@Override
	protected void processDiff(Transfer transfer, FileTreeModel diffModel)
			throws ConveyorExecutionException {

		// TODO: implement this

	}

}
