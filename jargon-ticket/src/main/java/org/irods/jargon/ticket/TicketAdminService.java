package org.irods.jargon.ticket;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.core.query.JargonQueryException;
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
	 * 
	 */
	String createTicket(TicketCreateModeEnum mode, IRODSFile file, String ticketId) throws JargonException;
	
	
	/**
	 * Delete a ticket for access to iRODS
	 * 
	 * @param String ticketID
	 * 			  used to specify ticket key to be deleted
	 * @throws JargonException
	 * 
	 */
	void deleteTicket(String ticketId) throws JargonException;
	
	
	/**
	 * List a ticket for access to iRODS
	 * 
	 * @param String ticketID
	 * 			  used to specify ticket key to be listed
	 * @throws JargonException
	 * @throws JargonQueryException
	 * 
	 */
	IRODSQueryResultSetInterface getTicketQueryResultForSpecifiedTicketString(String ticketId) throws JargonException, JargonQueryException;
	
	
	/**
	 * List a ticket for access to iRODS
	 * 
	 * @param String ticketID
	 * 			  used to specify ticket key to be listed
	 * @throws JargonException
	 * @throws JargonQueryException
	 * 
	 */
	//Ticket getTicketForSpecifiedTicketString(String ticketId) throws JargonException, JargonQueryException;
	
	/**
	 * List all tickets (for access to iRODS) for the current user
	 * 
	 * @param String ticketID
	 * 			  used to specify ticket key to be listed
	 * @throws JargonException
	 * @throws JargonQueryException
	 * 
	 */
	//IRODSQueryResultSetInterface getAllTickets(int continueIndex) throws JargonException, JargonQueryException;
	
	//IRODSQueryResultSetInterface getTicketAllowedUsersByTicketString(String ticketId, int continueIndex) throws JargonException, JargonQueryException;
	
	//IRODSQueryResultSetInterface getTicketsForSpecifiedDataObjectPath(String path, int continueIndex) throws JargonException, JargonQueryException;

}
