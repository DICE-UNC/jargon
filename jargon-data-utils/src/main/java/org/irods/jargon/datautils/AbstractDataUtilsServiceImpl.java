package org.irods.jargon.datautils;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractDataUtilsServiceImpl implements DataUtilsService {

	public static final Logger log = LoggerFactory
			.getLogger(AbstractDataUtilsServiceImpl.class);
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
	 *
	 * @param irodsAccessObjectFactory
	 *            {@link IRODSAccessObjectFactory} that can create necessary
	 *            objects
	 * @param irodsAccount
	 *            {@link IRODSAccount} that contains the login information
	 */
	public AbstractDataUtilsServiceImpl(
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

	/**
	 * Default (no-values) constructor.
	 */
	public AbstractDataUtilsServiceImpl() {
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

	@Override
	public IRODSAccessObjectFactory getIrodsAccessObjectFactory() {
		return irodsAccessObjectFactory;
	}

	@Override
	public void setIrodsAccessObjectFactory(
			final IRODSAccessObjectFactory irodsAccessObjectFactory) {
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
	}

	@Override
	public IRODSAccount getIrodsAccount() {
		return irodsAccount;
	}

	@Override
	public void setIrodsAccount(final IRODSAccount irodsAccount) {
		this.irodsAccount = irodsAccount;
	}

}