package org.irods.jargon.ticket;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;

/**
 * Factory for different ticket service classes.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class TicketServiceFactoryImpl implements TicketServiceFactory {

	private final IRODSAccessObjectFactory irodsAccessObjectFactory;

	public TicketServiceFactoryImpl(
			final IRODSAccessObjectFactory irodsAccessObjectFactory) {
		if (irodsAccessObjectFactory == null) {
			throw new IllegalArgumentException(
					"irodsAccessObjectFactory is null");
		}

		this.irodsAccessObjectFactory = irodsAccessObjectFactory;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.ticket.TicketServiceFactory#instanceTicketAdminService
	 * (org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public TicketAdminService instanceTicketAdminService(
			final IRODSAccount irodsAccount) throws JargonException {

		checkDependencies();

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		return new TicketAdminServiceImpl(irodsAccessObjectFactory,
				irodsAccount);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.ticket.TicketServiceFactory#instanceTicketClientOperations
	 * (org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public TicketClientOperations instanceTicketClientOperations(
			final IRODSAccount irodsAccount) throws JargonException {

		checkDependencies();

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		return new TicketClientOperationsImpl(irodsAccessObjectFactory,
				irodsAccount);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.ticket.TicketServiceFactory#
	 * instanceTicketDistributionService
	 * (org.irods.jargon.core.connection.IRODSAccount,
	 * org.irods.jargon.ticket.TicketDistributionContext)
	 */
	@Override
	public TicketDistributionService instanceTicketDistributionService(
			final IRODSAccount irodsAccount,
			final TicketDistributionContext ticketDistributionContext)
			throws JargonException {

		checkDependencies();

		return new TicketDistributionServiceImpl(irodsAccessObjectFactory,
				irodsAccount, this, ticketDistributionContext);

	}

	void checkDependencies() {
		if (irodsAccessObjectFactory == null) {
			throw new IllegalArgumentException(
					"the irodsAccessObjectFactory was not set for this instance");
		}
	}

}
