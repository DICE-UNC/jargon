package org.irods.jargon.ticket;

import java.util.List;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.ticket.Ticket.TicketObjectType;
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
	 * Temporarily here for testing...get rid of this after specific methods are
	 * created - MCC
	 * 
	 * @param ticketId
	 * @param objectType
	 * @return
	 * @throws JargonException
	 */
	IRODSQueryResultSetInterface getTicketQueryResultForSpecifiedTicketString(
			String ticketId, TicketObjectType objectType)
			throws JargonException;

	/**
	 * Generate a list of all tickets for data objects (files). Note that, for a
	 * regular user, this will be tickets for that user. For a rodsadmin, this
	 * will be all tickets.
	 * 
	 * @param partialStartIndex
	 *            <code>int</code> value >= 0 which provides an offset into
	 *            results for paging
	 * @return <code>List</code> of {@link Ticket} objects for data objects
	 * @throws JargonException
	 */
	List<Ticket> listAllTicketsForDataObjects(int partialStartIndex)
			throws JargonException;

	/**
	 * Generate a list of all tickets for collections (directories). Note that,
	 * for a regular user, this will be tickets for that user. For a rodsadmin,
	 * this will be all tickets.
	 * 
	 * @param partialStartIndex
	 *            <code>int</code> value >= 0 which provides an offset into
	 *            results for paging
	 * @return <code>List</code> of {@link Ticket} objects for collections
	 * @throws JargonException
	 */
	List<Ticket> listAllTicketsForCollections(int partialStartIndex)
			throws JargonException;
	
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
