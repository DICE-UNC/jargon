package org.irods.jargon.vircoll;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;

/**
 * Context information for a virtual collection, including info needed to connect with and interact with iRODS
 * 
 * @author mikeconway
 *
 */
public interface VirtualCollectionContext {

	public abstract IRODSAccount getIrodsAccount();

	public abstract IRODSAccessObjectFactory getIrodsAccessObjectFactory();

}