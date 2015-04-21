package org.irods.jargon.core.service;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;

/**
 * Handy base class that can be used to build 'services' on top of Jargon. These
 * are higher level components that use a mix of 'access objects' and i/o
 * objects created by the <code>IRODSAccessObjectFactory</code>.
 * <p/>
 * Testing note:
 * <p/>
 * Typically, jargon 'services' are created by a factory themselves, and this
 * allows easier mocking of the objects for testing. One may inject a mock
 * <code>IRODSAccessObjectFactory</code> and then test services without
 * requiring an actual iRODS server connection.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public abstract class AbstractJargonService {

	// let subclasses access directly
	protected IRODSAccessObjectFactory irodsAccessObjectFactory;
	protected IRODSAccount irodsAccount;

	/**
	 * Constructor with required dependencies
	 * 
	 * @param irodsAccessObjectFactory
	 *            {@link IRODSAccessObjectFactory}
	 * @param irodsAccount
	 *            {@link IRODSAccount}
	 */
	public AbstractJargonService(
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) {

		if (irodsAccessObjectFactory == null) {
			throw new IllegalArgumentException("null irodsAccessObjectFactory");
		}

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
		this.irodsAccount = irodsAccount;
	}

	public AbstractJargonService() {
		super();
	}

	/**
	 * get the <code>IRODSAccessObjectFactory</code> that is the key object for
	 * creating Jargon services.
	 * 
	 * @return {@link IRODSAccessObjectFactory}
	 */
	public IRODSAccessObjectFactory getIrodsAccessObjectFactory() {
		return irodsAccessObjectFactory;
	}

	/**
	 * Set the <code>IRODSAccessObjectFactory</code> that is the key object for
	 * creating Jargon services.
	 * 
	 * @param irodsAccessObjectFactory
	 *            {@link IRODSAccessObjectFactory}
	 */
	public void setIrodsAccessObjectFactory(
			final IRODSAccessObjectFactory irodsAccessObjectFactory) {
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
	}

	/**
	 * Get the <code>IRODSAccount</code> that will be used to create objects
	 * from the <code>IRODSAccessObjectFactory</code>
	 * 
	 * @return {@link IRODSAccount}
	 */
	public IRODSAccount getIrodsAccount() {
		return irodsAccount;
	}

	/**
	 * Set the <code>IRODSAccount</code> that will be used to create objects
	 * from the <code>IRODSAccessObjectFactory</code>
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount}
	 */
	public void setIrodsAccount(final IRODSAccount irodsAccount) {
		this.irodsAccount = irodsAccount;
	}
}
