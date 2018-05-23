package org.irods.jargon.ticket;

import org.irods.jargon.core.exception.JargonException;

/**
 * Service interface to assist in the distribution of tickets. This is meant to
 * provide generated links in URL form for tickets, as well as other future
 * supported generation methods.
 * <p>
 * Essentially, this class can help those developing interfaces that might want
 * to create a ticket and then produce a URL for an application that wants to
 * handle tickets. This service is used internally in iDrop web.
 * <p>
 * Eventually, we can expand the ticket distribution channels to include email
 * and other services.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface TicketDistributionService {

	/**
	 * Get information about ticket distribution channels for a given valid iRODS
	 * ticket
	 * 
	 * @param ticket
	 *            {@link Ticket} for which distribution information will be
	 *            generated
	 * @return {@link TicketDistribution} with extended information on accessing the
	 *         given ticket
	 * @throws JargonException
	 *             {@link JargonException}
	 */
	TicketDistribution getTicketDistributionForTicket(final Ticket ticket) throws JargonException;

}
