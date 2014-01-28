/**
 * 
 */
package org.irods.jargon.vircoll;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.service.AbstractJargonService;

/**
 * Context information for a virtual collection, including info needed to
 * connect with and interact with iRODS
 * 
 * @author mikeconway
 * 
 */
public class VirtualCollectionContextImpl extends AbstractJargonService
		implements VirtualCollectionContext {

	/**
	 * Default constructor
	 * 
	 * @param irodsAccessObjectFactory
	 * @param irodsAccount
	 */
	public VirtualCollectionContextImpl(
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
	}

	@Override
	public IRODSAccessObjectFactory getIrodsAccessObjectFactory() {
		return super.getIrodsAccessObjectFactory();
	}

	@Override
	public IRODSAccount getIrodsAccount() {
		return super.getIrodsAccount();
	}

}
