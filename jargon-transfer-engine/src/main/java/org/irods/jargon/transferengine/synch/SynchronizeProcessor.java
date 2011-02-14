package org.irods.jargon.transferengine.synch;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.datautils.tree.FileTreeDiffUtility;
import org.irods.jargon.transferengine.TransferManager;

/**
 * Utility that compares a local directory to a specified iRODS collection. This
 * will create a diff, and schedule the necessary operations to synchronize the
 * two folders.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface SynchronizeProcessor {

	/**
	 * Inspect a local directory, and compare it to a directory on iRODS. For
	 * the resulting diff, enqueue the appropriate transfers.
	 * 
	 * @param synchDeviceName
	 *            <code>String</code> with the name of the local device as known
	 *            to iRODS
	 * @param localRootAbsolutePath
	 *            <code>String</code> with the absolute path to the root
	 *            directory that will be synchronized
	 * @param irodsRootAbsolutePath
	 *            <code>String</code> with the absolute path to the root
	 *            directory in iRODS that will be synchronized
	 * @param timestampOfLastSynch
	 *            <code>long</code> with the time stamp of the last
	 *            synchronization. This is used to decide when a file has
	 *            changed on iRODS
	 * @throws JargonException
	 */
	public abstract void synchronizeLocalToIRODS(final String synchDeviceName,
			final String localRootAbsolutePath,
			final String irodsRootAbsolutePath, final long timestampOfLastSynch)
			throws JargonException;

	public abstract void setTransferManager(
			final TransferManager transferManager);

	public abstract TransferManager getTransferManager();

	public abstract void setIrodsAccessObjectFactory(
			final IRODSAccessObjectFactory irodsAccessObjectFactory);

	public abstract IRODSAccessObjectFactory getIrodsAccessObjectFactory();

	public abstract void setIrodsAccount(final IRODSAccount irodsAccount);

	public abstract IRODSAccount getIrodsAccount();

	public abstract void setFileTreeDiffUtility(
			final FileTreeDiffUtility fileTreeDiffUtility);

	public abstract FileTreeDiffUtility getFileTreeDiffUtility();

}