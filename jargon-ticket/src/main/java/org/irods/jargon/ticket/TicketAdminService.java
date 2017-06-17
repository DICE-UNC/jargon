package org.irods.jargon.ticket;

import java.util.Date;
import java.util.List;

import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.ticket.packinstr.TicketCreateModeEnum;

public interface TicketAdminService {

	/**
	 * Create a ticket for access to iRODS
	 * <p>
	 * This operation may be done on files or collections. Note that, for
	 * collections, the inheritance bit will be set, so that the ticket creator
	 * may have permissions on any files that grantees create in the collection.
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
	 * @throws DataNotFoundException
	 *             if ticket cannot be found
	 * @throws JargonException
	 */
	Ticket getTicketForSpecifiedTicketString(String ticketId)
			throws DataNotFoundException, JargonException;

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
	 * Generate a list of all tickets. Note that, for a regular user, this will
	 * be tickets for that user. For a rodsadmin, this will be all tickets. Also
	 * this will not return associated collections or data objects
	 * 
	 * @param partialStartIndex
	 *            <code>int</code> value >= 0 which provides an offset into
	 *            results for paging
	 * @return <code>List</code> of {@link Ticket} objects for collections
	 * @throws JargonException
	 */
	List<Ticket> listAllTickets(int partialStartIndex) throws JargonException;

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
	boolean setTicketUsesLimit(String ticketId, int usesLimit)
			throws JargonException;

	/**
	 * Modify the file write limit of a ticket for access to iRODS
	 * 
	 * @param ticketId
	 *            <code>String</code> used to specify ticket key to be modified
	 * @param fileWriteLimit
	 *            <code>int</code> value >= 0 which specifies the file write
	 *            limit for the specified ticket
	 * @return <code>boolean</code> that will be <code>true</code> if the ticket
	 *         was found to modify. <code>false</code> means that the modify was
	 *         not successful, due to the ticket not being found. This can be
	 *         ignored.
	 * @throws JargonException
	 * 
	 */
	boolean setTicketFileWriteLimit(String ticketId, int fileWriteLimit)
			throws JargonException;

	/**
	 * Modify the byte write of a ticket for access to iRODS
	 * 
	 * @param ticketId
	 *            <code>String</code> used to specify ticket key to be modified
	 * @param byteWriteLimit
	 *            <code>int</code> value >= 0 which specifies the byte write
	 *            limit for the specified ticket
	 * @return <code>boolean</code> that will be <code>true</code> if the ticket
	 *         was found to modify. <code>false</code> means that the modify was
	 *         not successful, due to the ticket not being found. This can be
	 *         ignored.
	 * @throws JargonException
	 * 
	 */
	boolean setTicketByteWriteLimit(String ticketId, long byteWriteLimit)
			throws JargonException;

	/**
	 * Modify the expire time of a ticket for access to iRODS
	 * 
	 * @param ticketId
	 *            <code>String</code> used to specify ticket key to be modified
	 * @param expirationTime
	 *            <code>Date</code> java.util.Date set to a valid date/time used
	 *            to specify expiration time of ticket
	 * @return <code>boolean</code> that will be <code>true</code> if the ticket
	 *         was found to modify. <code>false</code> means that the modify was
	 *         not successful, due to the ticket not being found. This can be
	 *         ignored.
	 * @throws JargonException
	 * 
	 */
	boolean setTicketExpiration(String ticketId, Date expirationTime)
			throws JargonException;

	/**
	 * Generate a list of all iRODS users that can use this ticket. Passing a
	 * <code>null<code> expiration will cause
	 * the expiration to be removed altogether.
	 * 
	 * @param ticketId
	 *            <code>String</code> used to specify ticket key to use for
	 *            retrieval of user restrictions
	 * @param partialStartIndex
	 *            <code>int</code> value >= 0 which provides an offset into
	 *            results for paging
	 * @return <code>List</code> of iRODS user ids
	 * @throws JargonException
	 */
	List<String> listAllUserRestrictionsForSpecifiedTicket(String ticketId,
			int partialStartIndex) throws JargonException;

	/**
	 * Generate a list of all iRODS groups that can use this ticket
	 * 
	 * @param ticketId
	 *            <code>String</code> used to specify ticket key to use for
	 *            retrieval of group restrictions
	 * @param partialStartIndex
	 *            <code>int</code> value >= 0 which provides an offset into
	 *            results for paging
	 * @return <code>List</code> of iRODS group ids
	 * @throws JargonException
	 */
	List<String> listAllGroupRestrictionsForSpecifiedTicket(String ticketId,
			int partialStartIndex) throws JargonException;

	/**
	 * Generate a list of all hosts that can use this ticket
	 * 
	 * @param ticketId
	 *            <code>String</code> used to specify ticket key to use for
	 *            retrieval of host restrictions
	 * @param partialStartIndex
	 *            <code>int</code> value >= 0 which provides an offset into
	 *            results for paging
	 * @return <code>List</code> of hosts
	 * @throws JargonException
	 */
	List<String> listAllHostRestrictionsForSpecifiedTicket(String ticketId,
			int partialStartIndex) throws JargonException;

	/**
	 * Modify the user access for an IRODS ticket
	 * 
	 * @param ticketId
	 *            <code>String</code> used to specify ticket key to be modified
	 * @param userId
	 *            <code>String</code> that identifies a valid iRODS user
	 * @return <code>boolean</code> that will be <code>true</code> if the ticket
	 *         was found to modify. <code>false</code> means that the modify was
	 *         not successful, due to the ticket not being found. This can be
	 *         ignored.
	 * @throws JargonException
	 *             (InvalidUserException)
	 * 
	 */
	boolean addTicketUserRestriction(String ticketId, String userId)
			throws JargonException;

	/**
	 * Modify the user access for an IRODS ticket
	 * 
	 * @param ticketId
	 *            <code>String</code> used to specify ticket key to be modified
	 * @param userId
	 *            <code>String</code> that identifies a valid iRODS user
	 * @return <code>boolean</code> that will be <code>true</code> if the ticket
	 *         was found to modify. <code>false</code> means that the modify was
	 *         not successful, due to the ticket not being found. This can be
	 *         ignored.
	 * @throws JargonException
	 *             (InvalidUserException)
	 * 
	 */
	boolean removeTicketUserRestriction(String ticketId, String userId)
			throws JargonException;

	/**
	 * Modify the group access for an IRODS ticket
	 * 
	 * @param ticketId
	 *            <code>String</code> used to specify ticket key to be modified
	 * @param groupId
	 *            <code>String</code> that identifies a valid iRODS group
	 * @return <code>boolean</code> that will be <code>true</code> if the ticket
	 *         was found to modify. <code>false</code> means that the modify was
	 *         not successful, due to the ticket not being found. This can be
	 *         ignored.
	 * @throws JargonException
	 *             (InvalidGroupException)
	 * 
	 */
	boolean addTicketGroupRestriction(String ticketId, String userId)
			throws JargonException;

	/**
	 * Modify the group access for an IRODS ticket
	 * 
	 * @param ticketId
	 *            <code>String</code> used to specify ticket key to be modified
	 * @param groupId
	 *            <code>String</code> that identifies a valid iRODS group
	 * @return <code>boolean</code> that will be <code>true</code> if the ticket
	 *         was found to modify. <code>false</code> means that the modify was
	 *         not successful, due to the ticket not being found. This can be
	 *         ignored.
	 * @throws JargonException
	 *             (InvalidGroupException)
	 * 
	 */
	boolean removeTicketGroupRestriction(String ticketId, String userId)
			throws JargonException;

	/**
	 * Modify the host access for an IRODS ticket
	 * 
	 * @param ticketId
	 *            <code>String</code> used to specify ticket key to be modified
	 * @param host
	 *            <code>String</code> that identifies a valid host
	 * @return <code>boolean</code> that will be <code>true</code> if the ticket
	 *         was found to modify. <code>false</code> means that the modify was
	 *         not successful, due to the ticket not being found. This can be
	 *         ignored.
	 * @throws JargonException
	 *             (InvalidHostException)
	 * 
	 */
	boolean addTicketHostRestriction(String ticketId, String host)
			throws JargonException;

	/**
	 * Modify the host access for an IRODS ticket
	 * 
	 * @param ticketId
	 *            <code>String</code> used to specify ticket key to be modified
	 * @param host
	 *            <code>String</code> that identifies a valid host
	 * @return <code>boolean</code> that will be <code>true</code> if the ticket
	 *         was found to modify. <code>false</code> means that the modify was
	 *         not successful, due to the ticket not being found. This can be
	 *         ignored.
	 * @throws JargonException
	 *             (InvalidHostException)
	 * 
	 */
	boolean removeTicketHostRestriction(String ticketId, String host)
			throws JargonException;

	/**
	 * delete all IRODS access tickets for this user
	 * 
	 * @return <code>boolean</code> that will be <code>true</code> if the delete
	 *         ticket operation was successful. <code>false</code> means that
	 *         the delete was not successful, due to no tickets found. This can
	 *         be ignored. NOTE: Use EXTREME caution with this method - for a
	 *         regular user (rodsuser), this will delete tickets just for that
	 *         user. However for a rodsadmin user this will delete ALL tickets.
	 * @throws JargonException
	 *             (InvalidHostException)
	 * 
	 */
	boolean deleteAllTicketsForThisUser() throws JargonException;

	/**
	 * Is the given ticket string already in use?
	 * 
	 * @param ticketString
	 *            <code>String</code> which is the generated key for the ticket
	 * @return <code>boolean</code> of <code>true</code> if the ticket is in use
	 * @throws JargonException
	 */
	boolean isTicketInUse(final String ticketString) throws JargonException;

	/**
	 * Create a listing of ticket objects in effect for a given collection at an
	 * iRODS absolute path
	 * 
	 * @param irodsAbsolutePath
	 *            <code>String</code> with an iRODS absolute path for a
	 *            collection
	 * @param partialStartIndex
	 *            <code>int</code> that can be set to 0 if starting at the
	 *            beginning. This specifies
	 * @return <code>List</code> of {@link Ticket} objects for the collection at
	 *         the given absolute path
	 * @throws JargonException
	 */
	List<Ticket> listAllTicketsForGivenCollection(String irodsAbsolutePath,
			int partialStartIndex) throws JargonException;

	/**
	 * Create a listing of ticket objects in effect for a given data object at
	 * an iRODS absolute path
	 * 
	 * @param irodsAbsolutePath
	 *            <code>String</code> with an iRODS absolute path for a
	 *            collection
	 * @param partialStartIndex
	 *            <code>int</code> that can be set to 0 if starting at the
	 *            beginning. This specifies
	 * @return <code>List</code> of {@link Ticket} objects for the collection at
	 *         the given absolute path
	 * @throws JargonException
	 */
	List<Ticket> listAllTicketsForGivenDataObject(String irodsAbsolutePath,
			int partialStartIndex) throws FileNotFoundException,
			JargonException;

	/**
	 * This is a 'meta' method that can manage the creation of iRODS tickets,
	 * and the simultaneous setting of the various limits. This convenience
	 * method removes the need to put this somewhat complicated sequence of code
	 * into applications.
	 * <p>
	 * It must be noted that iRODS protocol clients cannot frame this sort of
	 * operation in a transaction, so there is a very small chance that the
	 * operaton will not happen a atomically. This is not something that the
	 * client can do anything about. However, this still isolates the operations
	 * into a well tested unit.
	 * 
	 * @param ticket
	 *            {@link Ticket} to be added. Note that the only data that
	 *            really needs to be in the <code>Ticket</code> is the
	 *            (optional) ticket string, the ticket type (READ or WRITE), and
	 *            the absolute path. The rest of the data can either be left
	 *            alone, or it can be specified, and this will be handled by the
	 *            update process. For example, if you set a write byte limit in
	 *            the provided ticket, a call will be made to establish that
	 *            value.
	 *            <p>
	 *            Note that the <code>ticketString</code> in the
	 *            <code>Ticket</code> may either be specified, or it may be left
	 *            blank. If left blank, a ticket id will be randomly generated.
	 *            The <code>Ticket</code> object returned from this method will
	 *            carry all of the values properly initialized.
	 * @throws DuplicateDataException
	 *             if the <code>Ticket.ticketString</code> is already in use
	 * @throws DataNotFoundException
	 *             if the iRODS file does not exist
	 * @throws JargonException
	 */
	Ticket createTicketFromTicketObject(Ticket ticket)
			throws DuplicateDataException, DataNotFoundException,
			JargonException;

	/**
	 * Service method takes a course-grained ticket (must already exist) and
	 * compares this ticket, reflecting the desired values, and the current
	 * data. The delta between the two, for fields that may be updated, is used
	 * to call appropriate update methods.
	 * <p>
	 * Note that this is not transactional, so there is some small chance that
	 * not all changes will occur, this method will return a <code>Ticket</code>
	 * object that reflects the final state of the data in iRODS.
	 * <p>
	 * The current update-able fields are the various access and write limits,
	 * as well as the expiration data, other fields will be ignored.
	 * 
	 * @param ticketWithDesiredData
	 *            {@link Ticket} containing a valid ticket string, and
	 *            reflecting the update-able fields in the state they should be
	 *            in.
	 * @return {@link Ticket} as it now exists in the iCAT
	 * @throws DataNotFoundException
	 *             if the ticket with the given ticket string is not found
	 * @throws JargonException
	 */
	Ticket compareGivenTicketToActualAndUpdateAsNeeded(
			Ticket ticketWithDesiredData) throws DataNotFoundException,
			JargonException;

	/**
	 * This is a 'meta' method that can manage the creation of iRODS tickets,
	 * and the simultaneous setting of the various limits. This convenience
	 * method removes the need to put this somewhat complicated sequence of code
	 * into applications.
	 * <p>
	 * This variant uses a delegation technique so that a rodsAdmin can create a
	 * ticket that another user will own. This is important in scenarios where a
	 * proxy user may be interacting with iRODS on behalf of a user.
	 * <p>
	 * It must be noted that iRODS protocol clients cannot frame this sort of
	 * operation in a transaction, so there is a very small chance that the
	 * operaton will not happen a atomically. This is not something that the
	 * client can do anything about. However, this still isolates the operations
	 * into a well tested unit.
	 * 
	 * @param ticket
	 *            {@link Ticket} to be added. Note that the only data that
	 *            really needs to be in the <code>Ticket</code> is the
	 *            (optional) ticket string, the ticket type (READ or WRITE), and
	 *            the absolute path. The rest of the data can either be left
	 *            alone, or it can be specified, and this will be handled by the
	 *            update process. For example, if you set a write byte limit in
	 *            the provided ticket, a call will be made to establish that
	 *            value.
	 *            <p>
	 *            Note that the <code>ticketString</code> in the
	 *            <code>Ticket</code> may either be specified, or it may be left
	 *            blank. If left blank, a ticket id will be randomly generated.
	 *            The <code>Ticket</code> object returned from this method will
	 *            carry all of the values properly initialized.
	 * @param userName
	 *            <code>String</code>
	 * @throws DuplicateDataException
	 *             if the <code>Ticket.ticketString</code> is already in use
	 * @throws DataNotFoundException
	 *             if the iRODS file does not exist
	 * @throws JargonException
	 */
	Ticket createTicketFromTicketObjectAsAdminForGivenUser(Ticket ticket,
			String userName) throws DuplicateDataException,
			DataNotFoundException, JargonException;
}
