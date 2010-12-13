package org.irods.jargon.usertagging;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;

public abstract class AbstractIRODSTaggingService {

	protected final IRODSAccessObjectFactory irodsAccessObjectFactory;
	protected final IRODSAccount irodsAccount;

	/**
	 * Private constructor that initializes the service with access to objects
	 * that interact with iRODS.
	 * 
	 * @param irodsAccessObjectFactory
	 *            <code>IRODSAccessObjectFactory</code> that can create various
	 *            iRODS Access Objects.
	 * @param irodsAccount
	 *            <code>IRODSAccount</code> that describes the target server and
	 *            credentials.
	 * @throws JargonException
	 */
	protected AbstractIRODSTaggingService(
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) throws JargonException {

		if (irodsAccessObjectFactory == null) {
			throw new JargonException("null irodsAccessObjectFactory");
		}

		if (irodsAccount == null) {
			throw new JargonException("null irodsAccount");
		}

		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
		this.irodsAccount = irodsAccount;

	}

	protected IRODSAccessObjectFactory getIrodsAccessObjectFactory() {
		return irodsAccessObjectFactory;
	}

	protected IRODSAccount getIrodsAccount() {
		return irodsAccount;
	}

}