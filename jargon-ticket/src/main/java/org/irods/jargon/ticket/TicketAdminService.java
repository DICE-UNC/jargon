package org.irods.jargon.ticket;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.ticket.packinstr.TicketCreateModeEnum;

public interface TicketAdminService {
	
	/**
	 * Create a ticket for access to iRODS
	 * 
	 * @param TicketCreateModeEnum mode
	 *            ticket create mode - read or write
	 * @param IRODSFile file
	 *            existing IRODS file or collection
	 * @param String ticketID
	 * 			  used to specify ticket key to be used for this ticket
	 * @throws JargonException
	 * \
	 */
	String createTicket(TicketCreateModeEnum mode, IRODSFile file, String ticketId) throws JargonException;

}
