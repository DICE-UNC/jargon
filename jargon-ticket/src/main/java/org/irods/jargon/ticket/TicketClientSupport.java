package org.irods.jargon.ticket;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.Tag;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.ticket.packinstr.TicketInp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TicketClientSupport {

	public static final Logger log = LoggerFactory
			.getLogger(TicketClientSupport.class);

	private final IRODSAccessObjectFactory irodsAccessObjectFactory;
	private final IRODSAccount irodsAccount;

	/**
	 * Create an instance of the Ticket Client Service.
	 * 
	 * @param irodsAccessObjectFactory
	 *            {@link IRODSAccessObjectFactory} for managing connections to
	 *            iRODS
	 * @param irodsAccount
	 *            {@link IRODSAccount} for encapsulating connection information
	 */
	public TicketClientSupport(
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
	 * Do a call to initialize the current session with a ticket. This is done
	 * prior to get/put operations that present a ticket
	 * 
	 * @param ticketString
	 *            {@code String} with the unique ticket id for the
	 *            interaction
	 * @throws JargonException
	 */
	public void initializeSessionWithTicket(final String ticketString)
			throws JargonException {

		log.info("initializeSessionWithTicket()");

		if (ticketString == null || ticketString.isEmpty()) {
			throw new IllegalArgumentException("null or empty ticketString");
		}

		log.info("ticketString:{}", ticketString);

		TicketInp ticketInp = TicketInp
				.instanceForSetSessionWithTicket(ticketString);
		Tag ticketSessionResponse = irodsAccessObjectFactory.getIrodsSession()
				.currentConnection(irodsAccount).irodsFunction(ticketInp);

		log.debug("ticket init session response:{}", ticketSessionResponse);

	}
}
