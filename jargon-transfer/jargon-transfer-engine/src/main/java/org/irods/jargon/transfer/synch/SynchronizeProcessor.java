package org.irods.jargon.transfer.synch;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.datautils.synchproperties.SynchPropertiesService;
import org.irods.jargon.datautils.tree.FileTreeDiffUtility;
import org.irods.jargon.transfer.engine.TransferManager;

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
	 * @param timestampForLastSynchLeftHandSide
	 *            <code>long</code> with the timestamp that, if before the last
	 *            modified date of the given left hand files,
	 *            indicates that the file has changed. Leave as zero to turn off
	 *            this check.
	 *   @param timestampForLastSynchRightHandSide
	 *            <code>long</code> with the timestamp that, if before the last
	 *            modified date of the given right hand files,
	 *            indicates that the file has changed. Leave as zero to turn off
	 *            this check.
	 * @throws JargonException
	 */
	public abstract void synchronizeLocalToIRODS(final String synchDeviceName,
			final String localRootAbsolutePath,
			final String irodsRootAbsolutePath,	final long timestampforLastSynchLeftHandSide,
			final long timestampForLastSynchRightHandSide)
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

	SynchPropertiesService getSynchPropertiesService();

	void setSynchPropertiesService(SynchPropertiesService synchPropertiesService);

	/**
	 * Method to trigger an update of the synch state information for this synch collection.  The data includes the
	 * local and iRODS timestamps that mark the end of the synchronization, and are stored in special AVU information
	 * in the iRODS synch collection.
	 * @param userName <code>String</code> with the name of the synching iRODS user
	 * @param synchDeviceName <code>String</code> with the pre-configured synch device name.  Examples would be 'my laptop', or 'my work computer'
	 * @param irodsRootAbsolutePath <code>String</code> with the absolute path to an iRODS synch directory.  This directory needs to be pre-configured
	 * before it can be synchronized.
	 * @throws JargonException
	 */
	void getTimestampsAndUpdateSynchDataInIRODS(String userName,
			String synchDeviceName, String irodsRootAbsolutePath)
			throws JargonException;

}