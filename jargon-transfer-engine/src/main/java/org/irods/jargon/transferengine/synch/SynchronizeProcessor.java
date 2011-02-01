package org.irods.jargon.transferengine.synch;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.transferengine.TransferManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
	private static final Logger log = LoggerFactory
	.getLogger(SynchronizeProcessor.class);
	
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
	
	public void synchronizeLocalToIRODS(final String synchDeviceName, final String localRootAbsolutePath, final String irodsRootAbsolutePath, final long timestampOfLastSynch) throws JargonException {
		
		if (synchDeviceName == null || synchDeviceName.isEmpty()) {
			throw new IllegalArgumentException("null synchDeviceName");
		}
		
		if (localRootAbsolutePath == null || localRootAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null localRootAbsolutePath");
		}
		
		if (irodsRootAbsolutePath == null || irodsRootAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null irodsRootAbsolutePath");
		}
		
		if (timestampOfLastSynch < 0) {
			throw new IllegalArgumentException("negative timestampOfLastSynch, set to 0 if not specified");
		}
		
		log.info("synchronizeLocalToIRODS for device:{}", synchDeviceName);
		log.info("   localRootAbsolutePath:{}", localRootAbsolutePath);
		log.info("    irodsRootAbsolutePath:{}", irodsRootAbsolutePath);
		log.info("   timestampOfLastSynch:{}", timestampOfLastSynch);
		
		
		
		
		
	}
	
	
	
	
}
