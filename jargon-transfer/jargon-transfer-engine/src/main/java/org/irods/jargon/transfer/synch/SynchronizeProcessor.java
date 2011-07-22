package org.irods.jargon.transfer.synch;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.datautils.synchproperties.SynchPropertiesService;
import org.irods.jargon.datautils.tree.FileTreeDiffUtility;
import org.irods.jargon.transfer.dao.domain.LocalIRODSTransfer;
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

	
	void setTransferManager(final TransferManager transferManager);

	TransferManager getTransferManager();

	void setIrodsAccessObjectFactory(
			final IRODSAccessObjectFactory irodsAccessObjectFactory);

	IRODSAccessObjectFactory getIrodsAccessObjectFactory();

	void setIrodsAccount(final IRODSAccount irodsAccount);

	IRODSAccount getIrodsAccount();

	void setFileTreeDiffUtility(final FileTreeDiffUtility fileTreeDiffUtility);

	FileTreeDiffUtility getFileTreeDiffUtility();

	SynchPropertiesService getSynchPropertiesService();

	void setSynchPropertiesService(SynchPropertiesService synchPropertiesService);

	/**
	 * Method to trigger an update of the synch state information for this synch
	 * collection. The data includes the local and iRODS timestamps that mark
	 * the end of the synchronization, and are stored in special AVU information
	 * in the iRODS synch collection.
	 * 
	 * @param userName
	 *            <code>String</code> with the name of the synching iRODS user
	 * @param synchDeviceName
	 *            <code>String</code> with the pre-configured synch device name.
	 *            Examples would be 'my laptop', or 'my work computer'
	 * @param irodsRootAbsolutePath
	 *            <code>String</code> with the absolute path to an iRODS synch
	 *            directory. This directory needs to be pre-configured before it
	 *            can be synchronized.
	 * @throws JargonException
	 */
	void getTimestampsAndUpdateSynchDataInIRODS(String userName,
			String synchDeviceName, String irodsRootAbsolutePath)
			throws JargonException;

	/**
	 * Process a synchronization attempt
	 * @param localIRODSTransfer {@link LocalIRODSTransfer} that describes the synch attempt
	 * @throws JargonException
	 */
	void synchronizeLocalToIRODS(LocalIRODSTransfer localIRODSTransfer)
			throws JargonException;

	public abstract void setSynchronizingDiffProcessor(final SynchronizingDiffProcessor synchronizingDiffProcessor);

	

	
}