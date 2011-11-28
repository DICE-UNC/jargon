package org.irods.jargon.datautils;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.datautils.datacache.DataCacheServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractDataUtilsService {

	public static final Logger log = LoggerFactory
				.getLogger(DataCacheServiceImpl.class);
	/**
	 * Factory to create necessary Jargon access objects, which interact with
	 * the iRODS server
	 */
	protected IRODSAccessObjectFactory irodsAccessObjectFactory;
	/**
	 * Describes iRODS server and account information
	 */
	protected IRODSAccount irodsAccount;

	/**
	 * Constructor with required dependencies
	 * @param irodsAccessObjectFactory {@link IRODSAccessObjectFactory} that can create necessary objects
	 * @param irodsAccount {@link IRODSAccount} that contains the login information
	 */
	public AbstractDataUtilsService(IRODSAccessObjectFactory irodsAccessObjectFactory, IRODSAccount irodsAccount) {
		if (irodsAccessObjectFactory == null) {
			throw new IllegalArgumentException("null irodsAccessObjectFactory");
		}
		
		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}
		
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
		this.irodsAccount = irodsAccount;
		
	}

	/**
	 * Default (no-values) constructor.
	 */
	public AbstractDataUtilsService() {
	}

	/**
	 * Check for correct dependencies
	 */
	protected void checkContracts() throws JargonRuntimeException {
		if (irodsAccessObjectFactory == null) {
			throw new JargonRuntimeException("missing irodsAccessObjectFactory");
		}
	
		if (irodsAccount == null) {
			throw new JargonRuntimeException("irodsAccount is null");
		}
	
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