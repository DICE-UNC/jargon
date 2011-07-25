package org.irods.jargon.transfer.synch;

import org.irods.jargon.datautils.tree.FileTreeModel;
import org.irods.jargon.transfer.TransferEngineException;
import org.irods.jargon.transfer.dao.domain.LocalIRODSTransfer;

/**
 * Interface for a mechanism to take a difference tree model and synchronize
 * between iRODS and the local file system.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface SynchronizingDiffProcessor {

	/**
	 * Given a diff embodied in a <code>FileTreeModel</code>, do necessary
	 * operations to synchronize between local and iRODS.
	 * 
	 * @param localIRODSTransfer
	 *            {@link LocalIRODSTransfer} of type <code>SYNCH</code>, with a
	 *            parent {@link Synchronization} that describes the synch
	 *            relationship
	 * @param diffModel
	 *            {@link FileTreeModel} that embodies the diff between local and
	 *            iRODS
	 * @throws TransferEngineException
	 */
	void processDiff(final LocalIRODSTransfer localIRODSTransfer,
			final FileTreeModel diffModel) throws TransferEngineException;

}