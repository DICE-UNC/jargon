package org.irods.jargon.ticket;

import java.util.List;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.ticket.packinstr.TicketCreateModeEnum;

public interface TicketAdminService {

	/**
	 * Create a ticket for access to iRODS
	 * 
	 * @param TicketCreateModeEnum
	 *            mode ticket create mode - read or write
	 * @param IRODSFile
	 *            file existing IRODS file or collection
	 * @param String
	 *            ticketID used to specify ticket key to be used for this ticket
	 * @throws JargonException
	 * 
	 */
	String createTicket(TicketCreateModeEnum mode, IRODSFile file,
			String ticketId) throws JargonException;

	/**
	 * Delete a ticket for access to iRODS
	 * 
	 * @param String
	 *            ticketID used to specify ticket key to be deleted
	 * @return <code>boolean</code> that will be <code>true</code> if the ticket
	 *         was found to delete. <code>false</code> means that the delete was
	 *         not successful, due to the ticket not being found. This can be
	 *         ignored.
	 * @throws JargonException
	 * 
	 */
	boolean deleteTicket(String ticketId) throws JargonException;

	/**
	 * Generate a list of all tickets for data objects (files). Note that, for a
	 * regular user, this will be tickets for that user. For a rodsadmin, this
	 * will be all tickets.
	 * 
	 * @param ticketId
	 *            - string used to identify the ticket
	 * @return {@link Ticket} object for specified ticket string identifier
	 * @throws JargonException
	 */
	Ticket getTicketForSpecifiedTicketString(String ticketId)
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
	 * Generate a list of all tickets. Note that,
	 * for a regular user, this will be tickets for that user. For a rodsadmin,
	 * this will be all tickets. Also this will not return associated collections
	 * or data aobjects
	 * 
	 * @param partialStartIndex
	 *            <code>int</code> value >= 0 which provides an offset into
	 *            results for paging
	 * @return <code>List</code> of {@link Ticket} objects for collections
	 * @throws JargonException
	 */
	List<Ticket> listAllTickets(int partialStartIndex)
			throws JargonException;

	/**
	 * Modify the uses limit of a ticket for access to iRODS
	 * 
	 * @param ticketId
	 *            <code>String</code> used to specify ticket key to be modified
	 * @param usesLimit
	 *            <code>int</code> value >= 0 which specifies the uses limit for
	 *            the specified ticket
	 * @return <code>boolean</code> that will be <code>true</code> if the ticket
	 *         was found to modify. <code>false</code> means that the modify was
	 *         not successful, due to the ticket not being found. This can be
	 *         ignored.
	 * @throws JargonException
	 * 
	 */
	boolean setTicketUsesLimit(String ticketId, int usesLimit) throws JargonException;
	
	/**
	 * Modify the file write limit of a ticket for access to iRODS
	 * 
	 * @param ticketId
	 *            <code>String</code> used to specify ticket key to be modified
	 * @param fileWriteLimit
	 *            <code>int</code> value >= 0 which specifies the file write limit for
	 *            the specified ticket
	 * @return <code>boolean</code> that will be <code>true</code> if the ticket
	 *         was found to modify. <code>false</code> means that the modify was
	 *         not successful, due to the ticket not being found. This can be
	 *         ignored.
	 * @throws JargonException
	 * 
	 */
	boolean setTicketFileWriteLimit(String ticketId, int fileWriteLimit) throws JargonException;
	
	/**
	 * Modify the byte write of a ticket for access to iRODS
	 * 
	 * @param ticketId
	 *            <code>String</code> used to specify ticket key to be modified
	 * @param byteWriteLimit
	 *            <code>int</code> value >= 0 which specifies the byte write limit for
	 *            the specified ticket
	 * @return <code>boolean</code> that will be <code>true</code> if the ticket
	 *         was found to modify. <code>false</code> means that the modify was
	 *         not successful, due to the ticket not being found. This can be
	 *         ignored.
	 * @throws JargonException
	 * 
	 */
	boolean setTicketByteWriteLimit(String ticketId, int byteWriteLimit) throws JargonException;
	
	/**
	 * delete all IRODS access tickets for this IRODSAccount user
	 * 
	 * @return <code>boolean</code> that will be <code>true</code> if the delete
	 *         ticket operation was successful. <code>false</code> means that the delete was
	 *         not successful, due to no tickets found. This can be ignored.
	 * @throws JargonException
	 * 
	 */
	boolean deleteAllTicketsForThisUser() throws JargonException;


	// IRODSQueryResultSetInterface getAllTickets(int continueIndex) throws
	// JargonException, JargonQueryException;

	// IRODSQueryResultSetInterface getTicketAllowedUsersByTicketString(String
	// ticketId, int continueIndex) throws JargonException,
	// JargonQueryException;

	// IRODSQueryResultSetInterface getTicketsForSpecifiedDataObjectPath(String
	// path, int continueIndex) throws JargonException, JargonQueryException;

}
