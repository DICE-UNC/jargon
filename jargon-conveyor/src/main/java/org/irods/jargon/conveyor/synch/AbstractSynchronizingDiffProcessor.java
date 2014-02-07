package org.irods.jargon.conveyor.synch;

import org.irods.jargon.conveyor.core.ConveyorExecutionException;
import org.irods.jargon.datautils.tree.FileTreeModel;
import org.irods.jargon.transfer.dao.domain.Transfer;

/**
 * Abstract superclass for a mechanism to take a difference tree model and
 * synchronize between iRODS and the local file system. This can be implemented
 * for different synch strategies.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 */
public abstract class AbstractSynchronizingDiffProcessor {

	/**
	 * Given a diff embodied in a <code>FileTreeModel</code>, do necessary
	 * operations to synchronize between local and iRODS.
	 * 
	 * @param Transfer
	 *            {@link Transfer} of type <code>SYNCH</code>, with a parent
	 *            {@link Synchronization} that describes the synch relationship
	 * @param diffModel
	 *            {@link FileTreeModel} that embodies the diff between local and
	 *            iRODS
	 * @throws ConveyorExecutionException
	 */
	protected abstract void processDiff(final Transfer transfer,
			final FileTreeModel diffModel) throws ConveyorExecutionException;

}