package org.irods.jargon.datautils;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;

public interface DataUtilsService {

	public abstract IRODSAccessObjectFactory getIrodsAccessObjectFactory();

	public abstract void setIrodsAccessObjectFactory(
			final IRODSAccessObjectFactory irodsAccessObjectFactory);

	public abstract IRODSAccount getIrodsAccount();

	public abstract void setIrodsAccount(final IRODSAccount irodsAccount);

}