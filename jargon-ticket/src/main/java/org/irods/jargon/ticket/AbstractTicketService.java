package org.irods.jargon.ticket;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;

/**
 * Abstract service to handle ticket processing
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class AbstractTicketService {

	/**
	 * Constructor with required dependencies
	 * 
	 * @param irodsAccessObjectFactory
	 * @param irodsAccount
	 */
	AbstractTicketService(IRODSAccessObjectFactory irodsAccessObjectFactory,
			IRODSAccount irodsAccount) {

		if (irodsAccessObjectFactory == null) {
			throw new IllegalArgumentException("null irodsAccessObjectFactory");
		}

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
		this.irodsAccount = irodsAccount;
	}

	protected IRODSAccessObjectFactory irodsAccessObjectFactory;
	protected IRODSAccount irodsAccount;


	public AbstractTicketService() {
		super();
	}

	public IRODSAccessObjectFactory getIrodsAccessObjectFactory() {
		return irodsAccessObjectFactory;
	}

	public void setIrodsAccessObjectFactory(final IRODSAccessObjectFactory irodsAccessObjectFactory) {
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
	}

	public IRODSAccount getIrodsAccount() {
		return irodsAccount;
	}

	public void setIrodsAccount(final IRODSAccount irodsAccount) {
		this.irodsAccount = irodsAccount;
	}

}