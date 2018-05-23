package org.irods.jargon.ticket;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;

/**
 * Factory for various services that support iRODS tickets. This is helpful for
 * creating these services in various applications in a way that is easy to mock
 * and test.
 * <p>
 * Note that tickets are not supported in versions before iRODS 3.1
 * 
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface TicketServiceFactory {

	/**
	 * Create a new instance of the {@code TicketAdminService} that can create and
	 * modify tickets
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} that describes the server and connection info
	 * @return {@link TicketAdminService} object to interact with iRODS tickets
	 * @throws JargonException
	 *             {@link JargonException}
	 */
	TicketAdminService instanceTicketAdminService(IRODSAccount irodsAccount) throws JargonException;

	/**
	 * Create a new instance of the {@code TicketClientOperations} that can redeem
	 * tickets
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} that describes the server and connection info
	 * @return {@link TicketAdminService} object to interact with iRODS tickets
	 * @throws JargonException
	 *             {@link JargonException}
	 */
	TicketClientOperations instanceTicketClientOperations(IRODSAccount irodsAccount) throws JargonException;

	/**
	 * Create a new instance of the {@code TicketDistributionService} that can
	 * distribute tickets via various channels
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} that describes the server and connection info
	 * @param ticketDistributionContext
	 *            {@link TicketDistributionContext} with information on the
	 *            particular available channels
	 * @return {@link TicketDistributionService} object to interact with iRODS
	 *         tickets
	 * @throws JargonException
	 *             {@link JargonException}
	 */
	TicketDistributionService instanceTicketDistributionService(IRODSAccount irodsAccount,
			TicketDistributionContext ticketDistributionContext) throws JargonException;

}
