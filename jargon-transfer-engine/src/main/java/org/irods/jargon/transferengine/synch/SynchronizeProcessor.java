package org.irods.jargon.transferengine.synch;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.transferengine.TransferManager;

/**
 * Compare a local watched folder to a remote iRODS folder and enqueue necessary transfers to synchronize between the two.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class SynchronizeProcessor {

	private final IRODSAccount irodsAccount;
	private final IRODSAccessObjectFactory irodsAccessObjectFactory;
	private final TransferManager transferManager;
	
	/**
	 * Static instance method creates a <code>SynchronizeProcessor</code> ready to interact with a specified iRODS server.
	 * @param irodsAccount {@link IRODSAccount} that contains information on the user and iRODS instance to synchronize with
	 * @param irodsAccessObjectFactory {@link IRODSAccessObjectFactory} that can be used to obtain objects that can interact with iRODS
	 * @param transferManager {@link TransferManager} that oversees the actual transfer queue between the client and iRODS
	 * @return
	 */
	public static SynchronizeProcessor instance(final IRODSAccount irodsAccount, final IRODSAccessObjectFactory irodsAccessObjectFactory, final TransferManager transferManager) {
		return new SynchronizeProcessor(irodsAccount, irodsAccessObjectFactory, transferManager);
	}
	
	/**
	 * Private constructor
	 * @param irodsAccount
	 * @param irodsAccessObjectFactory
	 * @param transferManager
	 */
	private SynchronizeProcessor(final IRODSAccount irodsAccount, final IRODSAccessObjectFactory irodsAccessObjectFactory, final TransferManager transferManager) {
		
		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}
		
		if (irodsAccessObjectFactory == null) {
			throw new IllegalArgumentException("null irodsAccessObjectFactory");
		}
		
		if (transferManager == null) {
			throw new IllegalArgumentException("null transferManager");
		}
		
		this.irodsAccount = irodsAccount;
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
		this.transferManager = transferManager;
		
	}
	
	
	
	
}
