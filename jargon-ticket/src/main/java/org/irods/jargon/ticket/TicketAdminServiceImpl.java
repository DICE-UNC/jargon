package org.irods.jargon.ticket;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.Tag;
import org.irods.jargon.core.protovalues.ErrorEnum;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSGenQueryExecutor;
import org.irods.jargon.core.pub.ProtocolExtensionPoint;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.IRODSGenQuery;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSet;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.core.utils.IRODSDataConversionUtil;
import org.irods.jargon.ticket.packinstr.TicketAdminInp;
import org.irods.jargon.ticket.packinstr.TicketCreateModeEnum;
import org.irods.jargon.ticket.packinstr.TicketModifyAddOrRemoveTypeEnum;
import org.irods.jargon.ticket.utils.TicketRandomString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TicketAdminServiceImpl implements TicketAdminService {

	private static final String COMMA_SPACE = ", ";
	private static final String ERROR_IN_TICKET_QUERY = "error in ticket query";
	private static final String TICKET_NOT_FOUND = "IRODS ticket not found";
	public static final Logger log = LoggerFactory
			.getLogger(TicketAdminServiceImpl.class);
	private IRODSAccessObjectFactory irodsAccessObjectFactory;
	private IRODSAccount irodsAccount;

	/**
	 * Default constructor takes the objects necessary to communicate with iRODS
	 * via Access Objects
	 * 
	 * @param irodsAccessObjectFactory
	 *            {@link IRODSAccessObjectFactory} that can create various
	 *            access objects
	 * @param irodsAccount
	 *            {@link IRODSAccount} with login information for the target
	 *            grid
	 * @throws JargonException
	 */
	public TicketAdminServiceImpl(
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) throws JargonException {
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
		this.irodsAccount = irodsAccount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.ticket.TicketAdminService#createTicket(org.irods.jargon
	 * .ticket.packinstr.TicketCreateModeEnum,
	 * org.irods.jargon.core.pub.io.IRODSFile, java.lang.String)
	 */
	@Override
	public String createTicket(final TicketCreateModeEnum mode,
			final IRODSFile file, String ticketId) throws JargonException,
			DuplicateDataException {

		if (file == null) {
			throw new IllegalArgumentException(
					"cannot create ticket with null IRODS file/collection");
		}

		log.info("creating at ticket for :{}", file.getPath());

		if (mode == null) {
			throw new IllegalArgumentException(
					"cannot create ticket with null create mode - read or write access");
		}

		if (ticketId == null || ticketId.isEmpty()) {
			// create a new ticket string 15 chars in length
			ticketId = new TicketRandomString(15).nextString();
		}
		log.info("ticket creation mode is:{}", mode);

		TicketAdminInp ticketPI = TicketAdminInp.instanceForCreate(mode,
				file.getAbsolutePath(), ticketId);
		log.info("executing ticket PI");

		ProtocolExtensionPoint pep = irodsAccessObjectFactory
				.getProtocolExtensionPoint(irodsAccount);
		Tag ticketOperationResponse = pep.irodsFunction(ticketPI);

		log.info("received response from ticket operation:{}",
				ticketOperationResponse);

		return ticketId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.ticket.TicketAdminService#deleteTicket(java.lang.String)
	 */
	@Override
	public boolean deleteTicket(final String ticketId) throws JargonException {

		Tag ticketOperationResponse = null;
		boolean response = true;

		if ((ticketId == null) || (ticketId.isEmpty())) {
			throw new IllegalArgumentException(
					"cannot delete ticket with null or empty ticketId");
		}

		log.info("deleting ticket id/string:{}", ticketId);

		TicketAdminInp ticketPI = TicketAdminInp.instanceForDelete(ticketId);
		log.info("executing ticket PI");

		ProtocolExtensionPoint pep = irodsAccessObjectFactory
				.getProtocolExtensionPoint(irodsAccount);
		try {
			ticketOperationResponse = pep.irodsFunction(ticketPI);
		} catch (JargonException e) {
			if (e.getUnderlyingIRODSExceptionCode() == ErrorEnum.CAT_TICKET_INVALID
					.getInt()) {
				response = false;
			}
		}

		log.info("received response from ticket operation:{}",
				ticketOperationResponse);

		return response;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.ticket.TicketAdminService#listAllTicketsForDataObjects
	 * (int)
	 */
	@Override
	public List<Ticket> listAllTicketsForDataObjects(final int partialStartIndex)
			throws JargonException {

		log.info("listAllTicketsForDataObjects()");

		if (partialStartIndex < 0) {
			throw new IllegalArgumentException(
					"partial start index must be >= 0");
		}

		List<Ticket> tickets = new ArrayList<Ticket>();

		StringBuilder sb = new StringBuilder();
		sb.append(this.buildQuerySelectForLSAllTicketsForDataObjects());

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(sb.toString(),
				irodsAccessObjectFactory.getJargonProperties()
						.getMaxFilesAndDirsQueryMax());

		IRODSGenQueryExecutor irodsGenQueryExecutor = irodsAccessObjectFactory
				.getIRODSGenQueryExecutor(irodsAccount);

		IRODSQueryResultSet resultSet = null;

		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(
					irodsQuery, partialStartIndex);
			Ticket ticket = null;
			StringBuilder absPathBuilder = null;
			for (IRODSQueryResultRow row : resultSet.getResults()) {
				ticket = new Ticket();
				putResultDataIntoTicketCommonValues(ticket, row);
				absPathBuilder = new StringBuilder();
				absPathBuilder.append(row.getColumn(14));
				absPathBuilder.append('/');
				absPathBuilder.append(row.getColumn(13));
				ticket.setIrodsAbsolutePath(absPathBuilder.toString());
				// add info to track position in records for possible requery
				ticket.setLastResult(row.isLastResult());
				ticket.setCount(row.getRecordCount());
				log.info("adding ticket to results:{}", ticket);
				tickets.add(ticket);
			}

		} catch (JargonQueryException e) {
			log.error("query exception for ticket query:{}", irodsQuery, e);
			throw new JargonException(ERROR_IN_TICKET_QUERY, e);
		}

		return tickets;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.ticket.TicketAdminService#listAllTicketsForCollections
	 * (int)
	 */
	@Override
	public List<Ticket> listAllTicketsForCollections(final int partialStartIndex)
			throws JargonException {

		log.info("listAllTicketsForCollections()");

		if (partialStartIndex < 0) {
			throw new IllegalArgumentException(
					"partial start index must be >= 0");
		}

		List<Ticket> tickets = new ArrayList<Ticket>();

		StringBuilder sb = new StringBuilder();
		sb.append(this.buildQuerySelectForLSAllTicketsForCollections());

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(sb.toString(),
				irodsAccessObjectFactory.getJargonProperties()
						.getMaxFilesAndDirsQueryMax());

		IRODSGenQueryExecutor irodsGenQueryExecutor = irodsAccessObjectFactory
				.getIRODSGenQueryExecutor(irodsAccount);

		IRODSQueryResultSet resultSet = null;

		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(
					irodsQuery, partialStartIndex);
			Ticket ticket = null;
			for (IRODSQueryResultRow row : resultSet.getResults()) {
				ticket = new Ticket();
				putResultDataIntoTicketCommonValues(ticket, row);
				ticket.setIrodsAbsolutePath(row.getColumn(13));
				log.info("adding ticket to results:{}", ticket);
				tickets.add(ticket);
			}

		} catch (JargonQueryException e) {
			log.error("query exception for ticket query:{}", irodsQuery, e);
			throw new JargonException(ERROR_IN_TICKET_QUERY, e);
		}

		return tickets;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.ticket.TicketAdminService#listAllTicketsForCollections
	 * (int)
	 */
	@Override
	public List<Ticket> listAllTickets(final int partialStartIndex)
			throws JargonException {

		log.info("listAllTickets()");

		if (partialStartIndex < 0) {
			throw new IllegalArgumentException(
					"partial start index must be >= 0");
		}

		List<Ticket> tickets = new ArrayList<Ticket>();

		StringBuilder sb = new StringBuilder();
		sb.append(this.buildQuerySelectForTicketsCommon());

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(sb.toString(),
				irodsAccessObjectFactory.getJargonProperties()
						.getMaxFilesAndDirsQueryMax());

		IRODSGenQueryExecutor irodsGenQueryExecutor = irodsAccessObjectFactory
				.getIRODSGenQueryExecutor(irodsAccount);

		IRODSQueryResultSet resultSet = null;

		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(
					irodsQuery, partialStartIndex);
			Ticket ticket = null;
			for (IRODSQueryResultRow row : resultSet.getResults()) {
				ticket = new Ticket();
				putResultDataIntoTicketCommonValues(ticket, row);
				log.info("adding ticket to results:{}", ticket);
				tickets.add(ticket);
			}

		} catch (JargonQueryException e) {
			log.error("query exception for ticket query:{}", irodsQuery, e);
			throw new JargonException(ERROR_IN_TICKET_QUERY, e);
		}

		return tickets;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.ticket.TicketAdminService#getTicketForSpecifiedTicketString
	 * (int)
	 */
	@Override
	public Ticket getTicketForSpecifiedTicketString(final String ticketId)
			throws JargonException {

		Ticket ticket = null;
		IRODSQueryResultSetInterface resultSet = null;
		String queryCommon = null;

		Ticket.TicketObjectType objectType = getTicketObjectType(ticketId);
		if (objectType.equals(Ticket.TicketObjectType.DATA_OBJECT)) {
			queryCommon = buildQuerySelectForLSAllTicketsForDataObjects();
		} else {
			queryCommon = buildQuerySelectForLSAllTicketsForCollections();
		}
		// add where clause
		StringBuilder queryFull = new StringBuilder();
		queryFull.append(queryCommon);
		queryFull.append(" where ");
		queryFull.append(RodsGenQueryEnum.COL_TICKET_STRING.getName());
		queryFull.append(" = ");
		queryFull.append("'");
		queryFull.append(ticketId);
		queryFull.append("'");

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryFull.toString(),
				2);

		IRODSGenQueryExecutor irodsGenQueryExecutor = irodsAccessObjectFactory
				.getIRODSGenQueryExecutor(irodsAccount);
		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(
					irodsQuery, 0);
			if ((resultSet == null) || (resultSet.getResults().isEmpty())) {
				throw new DataNotFoundException(TICKET_NOT_FOUND);
			}
			IRODSQueryResultRow row = resultSet.getFirstResult();
			ticket = new Ticket();
			putResultDataIntoTicketCommonValues(ticket, row);
			if (objectType.equals(Ticket.TicketObjectType.DATA_OBJECT)) {
				StringBuilder absPathBuilder = new StringBuilder();
				absPathBuilder.append(row.getColumn(14));
				absPathBuilder.append('/');
				absPathBuilder.append(row.getColumn(13));
				ticket.setIrodsAbsolutePath(absPathBuilder.toString());
			} else {
				ticket.setIrodsAbsolutePath(resultSet.getFirstResult()
						.getColumn(13));

			}
		} catch (JargonQueryException e) {
			log.error("query exception for ticket query: {}", irodsQuery, e);
			throw new JargonException(ERROR_IN_TICKET_QUERY, e);
		}

		return ticket;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.ticket.TicketAdminService#
	 * listAllUserRestrictionsForSpecifiedTicket(java.lang.String)
	 */
	@Override
	public List<String> listAllUserRestrictionsForSpecifiedTicket(
			final String ticketId, final int partialStartIndex)
			throws JargonException {

		return listRestrictionsForSpecifiedTicketCommon(ticketId,
				RodsGenQueryEnum.COL_TICKET_ALLOWED_USER_NAME,
				partialStartIndex);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.ticket.TicketAdminService#
	 * listAllGroupRestrictionsForSpecifiedTicket(java.lang.String)
	 */
	@Override
	public List<String> listAllGroupRestrictionsForSpecifiedTicket(
			final String ticketId, final int partialStartIndex)
			throws JargonException {

		return listRestrictionsForSpecifiedTicketCommon(ticketId,
				RodsGenQueryEnum.COL_TICKET_ALLOWED_GROUP_NAME,
				partialStartIndex);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.ticket.TicketAdminService#
	 * listAllHostRestrictionsForSpecifiedTicket(java.lang.String)
	 */
	@Override
	public List<String> listAllHostRestrictionsForSpecifiedTicket(
			final String ticketId, final int partialStartIndex)
			throws JargonException {

		return listRestrictionsForSpecifiedTicketCommon(ticketId,
				RodsGenQueryEnum.COL_TICKET_ALLOWED_HOST, partialStartIndex);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.ticket.TicketAdminService#setTicketUsesLimit(java.lang
	 * .String)
	 */
	@Override
	public boolean setTicketUsesLimit(final String ticketId, final int usesLimit)
			throws JargonException {

		Tag ticketOperationResponse = null;
		boolean response = true;

		if ((ticketId == null) || (ticketId.isEmpty())) {
			throw new IllegalArgumentException(
					"cannot modify ticket with null or empty ticketId");
		}

		if (usesLimit < 0) {
			throw new IllegalArgumentException(
					"cannot modify a ticket with uses count less than 0");
		}

		log.info("modifying ticket id/string:{}", ticketId);

		TicketAdminInp ticketPI = TicketAdminInp.instanceForModifyNumberOfUses(
				ticketId, usesLimit);
		log.info("executing ticket PI");

		ProtocolExtensionPoint pep = irodsAccessObjectFactory
				.getProtocolExtensionPoint(irodsAccount);
		try {
			ticketOperationResponse = pep.irodsFunction(ticketPI);
		} catch (JargonException e) {
			if (e.getUnderlyingIRODSExceptionCode() == ErrorEnum.CAT_TICKET_INVALID
					.getInt()) {
				response = false;
			}
		}

		log.info("received response from ticket operation:{}",
				ticketOperationResponse);

		return response;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.ticket.TicketAdminService#setTicketFileWriteLimit(java
	 * .lang.String)
	 */
	@Override
	public boolean setTicketFileWriteLimit(final String ticketId,
			final int fileWriteLimit) throws JargonException {

		Tag ticketOperationResponse = null;
		boolean response = true;

		if ((ticketId == null) || (ticketId.isEmpty())) {
			throw new IllegalArgumentException(
					"cannot modify ticket with null or empty ticketId");
		}

		if (fileWriteLimit < 0) {
			throw new IllegalArgumentException(
					"cannot modify a ticket with file write less than 0");
		}

		log.info("modifying ticket id/string:{}", ticketId);

		TicketAdminInp ticketPI = TicketAdminInp
				.instanceForModifyFileWriteNumber(ticketId, fileWriteLimit);
		log.info("executing ticket PI");

		ProtocolExtensionPoint pep = irodsAccessObjectFactory
				.getProtocolExtensionPoint(irodsAccount);
		try {
			ticketOperationResponse = pep.irodsFunction(ticketPI);
		} catch (JargonException e) {
			if (e.getUnderlyingIRODSExceptionCode() == ErrorEnum.CAT_TICKET_INVALID
					.getInt()) {
				response = false;
			}
		}

		log.info("received response from ticket operation:{}",
				ticketOperationResponse);

		return response;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.ticket.TicketAdminService#setTicketByteWriteLimit(java
	 * .lang.String)
	 */
	@Override
	public boolean setTicketByteWriteLimit(final String ticketId,
			final int byteWriteLimit) throws JargonException {

		Tag ticketOperationResponse = null;
		boolean response = true;

		if ((ticketId == null) || (ticketId.isEmpty())) {
			throw new IllegalArgumentException(
					"cannot modify ticket with null or empty ticketId");
		}

		if (byteWriteLimit < 0) {
			throw new IllegalArgumentException(
					"cannot modify a ticket with byte write count less than 0");
		}

		log.info("modifying ticket id/string:{}", ticketId);

		TicketAdminInp ticketPI = TicketAdminInp
				.instanceForModifyByteWriteNumber(ticketId, byteWriteLimit);
		log.info("executing ticket PI");

		ProtocolExtensionPoint pep = irodsAccessObjectFactory
				.getProtocolExtensionPoint(irodsAccount);
		try {
			ticketOperationResponse = pep.irodsFunction(ticketPI);
		} catch (JargonException e) {
			if (e.getUnderlyingIRODSExceptionCode() == ErrorEnum.CAT_TICKET_INVALID
					.getInt()) {
				response = false;
			}
		}

		log.info("received response from ticket operation:{}",
				ticketOperationResponse);

		return response;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.ticket.TicketAdminService#setTicketExpiration(java.lang
	 * .String)
	 */
	@Override
	public boolean setTicketExpiration(final String ticketId,
			final Date expirationDate) throws JargonException {

		Tag ticketOperationResponse = null;
		boolean response = true;

		if ((ticketId == null) || (ticketId.isEmpty())) {
			throw new IllegalArgumentException(
					"cannot modify ticket with null or empty ticketId");
		}

		if ((expirationDate == null) || (expirationDate.getTime() <= 0)) {
			throw new IllegalArgumentException(
					"cannot modify a ticket with expiration date of less than or equal to 0");
		}

		log.info("modifying ticket id/string:{}", ticketId);

		TicketAdminInp ticketPI = TicketAdminInp.instanceForModifyExpiration(
				ticketId, expirationDate);
		log.info("executing ticket PI");

		ProtocolExtensionPoint pep = irodsAccessObjectFactory
				.getProtocolExtensionPoint(irodsAccount);
		try {
			ticketOperationResponse = pep.irodsFunction(ticketPI);
		} catch (JargonException e) {
			if (e.getUnderlyingIRODSExceptionCode() == ErrorEnum.CAT_TICKET_INVALID
					.getInt()) {
				response = false;
			}
		}

		log.info("received response from ticket operation:{}",
				ticketOperationResponse);

		return response;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.ticket.TicketAdminService#addTicketUserRestriction(java
	 * .lang.String)
	 */
	@Override
	public boolean addTicketUserRestriction(final String ticketId,
			final String userId) throws JargonException {

		Tag ticketOperationResponse = null;
		boolean response = true;

		if ((ticketId == null) || (ticketId.isEmpty())) {
			throw new IllegalArgumentException(
					"cannot modify ticket with null or empty ticketId");
		}

		if ((userId == null) || (userId.isEmpty())) {
			throw new IllegalArgumentException(
					"cannot modify ticket with null or empty userId");
		}

		log.info("modifying ticket id/string:{}", ticketId);

		TicketAdminInp ticketPI = TicketAdminInp.instanceForModifyAddAccess(
				ticketId, TicketModifyAddOrRemoveTypeEnum.TICKET_MODIFY_USER,
				userId);
		log.info("executing ticket PI");

		ProtocolExtensionPoint pep = irodsAccessObjectFactory
				.getProtocolExtensionPoint(irodsAccount);
		try {
			ticketOperationResponse = pep.irodsFunction(ticketPI);
		} catch (JargonException e) {
			if (e.getUnderlyingIRODSExceptionCode() == ErrorEnum.CAT_TICKET_INVALID
					.getInt()) {
				response = false;
			} else {
				throw e;
			}
		}

		log.info("received response from ticket operation:{}",
				ticketOperationResponse);

		return response;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.ticket.TicketAdminService#addTicketUserRestriction(java
	 * .lang.String)
	 */
	@Override
	public boolean removeTicketUserRestriction(final String ticketId,
			final String userId) throws JargonException {

		Tag ticketOperationResponse = null;
		boolean response = true;

		if ((ticketId == null) || (ticketId.isEmpty())) {
			throw new IllegalArgumentException(
					"cannot modify ticket with null or empty ticketId");
		}

		if ((userId == null) || (userId.isEmpty())) {
			throw new IllegalArgumentException(
					"cannot modify ticket with null or empty userId");
		}

		log.info("modifying ticket id/string:{}", ticketId);

		TicketAdminInp ticketPI = TicketAdminInp.instanceForModifyRemoveAccess(
				ticketId, TicketModifyAddOrRemoveTypeEnum.TICKET_MODIFY_USER,
				userId);
		log.info("executing ticket PI");

		ProtocolExtensionPoint pep = irodsAccessObjectFactory
				.getProtocolExtensionPoint(irodsAccount);
		try {
			ticketOperationResponse = pep.irodsFunction(ticketPI);
		} catch (JargonException e) {
			if (e.getUnderlyingIRODSExceptionCode() == ErrorEnum.CAT_TICKET_INVALID
					.getInt()) {
				response = false;
			} else {
				throw e;
			}
		}

		log.info("received response from ticket operation:{}",
				ticketOperationResponse);

		return response;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.ticket.TicketAdminService#addTicketGroupRestriction(
	 * java.lang.String)
	 */
	@Override
	public boolean addTicketGroupRestriction(final String ticketId,
			final String groupId) throws JargonException {

		Tag ticketOperationResponse = null;
		boolean response = true;

		if ((ticketId == null) || (ticketId.isEmpty())) {
			throw new IllegalArgumentException(
					"cannot modify ticket with null or empty ticketId");
		}

		if ((groupId == null) || (groupId.isEmpty())) {
			throw new IllegalArgumentException(
					"cannot modify ticket with null or empty groupId");
		}

		log.info("modifying ticket id/string:{}", ticketId);

		TicketAdminInp ticketPI = TicketAdminInp.instanceForModifyAddAccess(
				ticketId, TicketModifyAddOrRemoveTypeEnum.TICKET_MODIFY_GROUP,
				groupId);
		log.info("executing ticket PI");

		ProtocolExtensionPoint pep = irodsAccessObjectFactory
				.getProtocolExtensionPoint(irodsAccount);
		try {
			ticketOperationResponse = pep.irodsFunction(ticketPI);
		} catch (JargonException e) {
			if (e.getUnderlyingIRODSExceptionCode() == ErrorEnum.CAT_TICKET_INVALID
					.getInt()) {
				response = false;
			} else {
				throw e;
			}
		}

		log.info("received response from ticket operation:{}",
				ticketOperationResponse);

		return response;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.ticket.TicketAdminService#addTicketGroupRestriction(
	 * java.lang.String)
	 */
	@Override
	public boolean removeTicketGroupRestriction(final String ticketId,
			final String groupId) throws JargonException {

		Tag ticketOperationResponse = null;
		boolean response = true;

		if ((ticketId == null) || (ticketId.isEmpty())) {
			throw new IllegalArgumentException(
					"cannot modify ticket with null or empty ticketId");
		}

		if ((groupId == null) || (groupId.isEmpty())) {
			throw new IllegalArgumentException(
					"cannot modify ticket with null or empty groupId");
		}

		log.info("modifying ticket id/string:{}", ticketId);

		TicketAdminInp ticketPI = TicketAdminInp.instanceForModifyRemoveAccess(
				ticketId, TicketModifyAddOrRemoveTypeEnum.TICKET_MODIFY_GROUP,
				groupId);
		log.info("executing ticket PI");

		ProtocolExtensionPoint pep = irodsAccessObjectFactory
				.getProtocolExtensionPoint(irodsAccount);
		try {
			ticketOperationResponse = pep.irodsFunction(ticketPI);
		} catch (JargonException e) {
			if (e.getUnderlyingIRODSExceptionCode() == ErrorEnum.CAT_TICKET_INVALID
					.getInt()) {
				response = false;
			} else {
				throw e;
			}
		}

		log.info("received response from ticket operation:{}",
				ticketOperationResponse);

		return response;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.ticket.TicketAdminService#addTicketHostRestriction(java
	 * .lang.String)
	 */
	@Override
	public boolean addTicketHostRestriction(final String ticketId,
			final String host) throws JargonException {

		Tag ticketOperationResponse = null;
		boolean response = true;

		if ((ticketId == null) || (ticketId.isEmpty())) {
			throw new IllegalArgumentException(
					"cannot modify ticket with null or empty ticketId");
		}

		if ((host == null) || (host.isEmpty())) {
			throw new IllegalArgumentException(
					"cannot modify ticket with null or empty host");
		}

		log.info("modifying ticket id/string:{}", ticketId);

		TicketAdminInp ticketPI = TicketAdminInp.instanceForModifyAddAccess(
				ticketId, TicketModifyAddOrRemoveTypeEnum.TICKET_MODIFY_HOST,
				host);
		log.info("executing ticket PI");

		ProtocolExtensionPoint pep = irodsAccessObjectFactory
				.getProtocolExtensionPoint(irodsAccount);
		try {
			ticketOperationResponse = pep.irodsFunction(ticketPI);
		} catch (JargonException e) {
			if (e.getUnderlyingIRODSExceptionCode() == ErrorEnum.CAT_TICKET_INVALID
					.getInt()) {
				response = false;
			} else {
				throw e;
			}
		}

		log.info("received response from ticket operation:{}",
				ticketOperationResponse);

		return response;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.ticket.TicketAdminService#addTicketHostRestriction(java
	 * .lang.String)
	 */
	@Override
	public boolean removeTicketHostRestriction(final String ticketId,
			final String host) throws JargonException {

		Tag ticketOperationResponse = null;
		boolean response = true;

		if ((ticketId == null) || (ticketId.isEmpty())) {
			throw new IllegalArgumentException(
					"cannot modify ticket with null or empty ticketId");
		}

		if ((host == null) || (host.isEmpty())) {
			throw new IllegalArgumentException(
					"cannot modify ticket with null or empty host");
		}

		log.info("modifying ticket id/string:{}", ticketId);

		TicketAdminInp ticketPI = TicketAdminInp.instanceForModifyRemoveAccess(
				ticketId, TicketModifyAddOrRemoveTypeEnum.TICKET_MODIFY_HOST,
				host);
		log.info("executing ticket PI");

		ProtocolExtensionPoint pep = irodsAccessObjectFactory
				.getProtocolExtensionPoint(irodsAccount);
		try {
			ticketOperationResponse = pep.irodsFunction(ticketPI);
		} catch (JargonException e) {
			if (e.getUnderlyingIRODSExceptionCode() == ErrorEnum.CAT_TICKET_INVALID
					.getInt()) {
				response = false;
			} else {
				throw e;
			}
		}

		log.info("received response from ticket operation:{}",
				ticketOperationResponse);

		return response;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.ticket.TicketAdminService#deleteAllTicketsForThisUser
	 * (java.lang.String)
	 */
	@Override
	public boolean deleteAllTicketsForThisUser() throws JargonException {

		boolean returnVal = false;

		List<Ticket> tickets = listAllTickets(0);

		for (Ticket ticket : tickets) {
			// TODO: the following is an overhead that might be removable later-
			// MCC
			if (ticket.getTicketString().isEmpty()) {
				log.warn("unable to delete a ticket with an empty string:{}",
						ticket);
				continue;
			}
			returnVal = true;
			deleteTicket(ticket.getTicketString());
			tickets = listAllTickets(tickets.size());
		}

		return returnVal;
	}

	/**
	 * Given a result row from a ticket query, put the values into the provided
	 * <code>Ticket</code> common to tickets for collections and data objects
	 * 
	 * @param ticket
	 *            {@link Ticket} object that will be initialized with values
	 *            from the query result row. The provided <code>Ticket</code> in
	 *            the method parameter will be updated by this method.
	 * @param row
	 *            {@link IRODSQueryResultRow} from a query for the ticket as
	 *            specified by the methods internal to this object. This is not
	 *            a generally applicable method,, rather it assumes the columns
	 *            have been requested in a certain order.
	 * @throws JargonException
	 */
	private void putResultDataIntoTicketCommonValues(final Ticket ticket,
			final IRODSQueryResultRow row) throws JargonException {
		ticket.setTicketId(row.getColumn(0));
		ticket.setTicketString(row.getColumn(1));
		ticket.setType(TicketCreateModeEnum.findTypeByString(row.getColumn(2)));
		ticket.setObjectType(this.findObjectType(row.getColumn(3)));
		ticket.setOwnerName(row.getColumn(4));
		ticket.setOwnerZone(row.getColumn(5));
		ticket.setUsesCount(IRODSDataConversionUtil
				.getIntOrZeroFromIRODSValue(row.getColumn(6)));
		ticket.setUsesLimit(IRODSDataConversionUtil
				.getIntOrZeroFromIRODSValue(row.getColumn(7)));
		ticket.setWriteFileCount(IRODSDataConversionUtil
				.getIntOrZeroFromIRODSValue(row.getColumn(8)));
		ticket.setWriteFileLimit(IRODSDataConversionUtil
				.getIntOrZeroFromIRODSValue(row.getColumn(9)));
		ticket.setWriteByteCount(IRODSDataConversionUtil
				.getLongOrZeroFromIRODSValue(row.getColumn(10)));
		ticket.setWriteByteLimit(IRODSDataConversionUtil
				.getLongOrZeroFromIRODSValue(row.getColumn(11)));
		ticket.setExpireTime(IRODSDataConversionUtil.getDateFromIRODSValue(row
				.getColumn(12)));
	}

	/**
	 * Build the select part of a query dealing with tickets for collections
	 * 
	 * @return <code>String</code> with a gen query select statemetn for values
	 *         for collections
	 */
	private String buildQuerySelectForLSAllTicketsForCollections() {

		StringBuilder queryString = new StringBuilder();
		queryString.append(buildQuerySelectForTicketsCommon());
		queryString.append(COMMA_SPACE);
		queryString.append(RodsGenQueryEnum.COL_TICKET_COLL_NAME.getName());
		return queryString.toString();
	}

	/**
	 * Given a <code>String</code> representation of an object type for the
	 * ticket in iRODS, return an emum equivalent value
	 * 
	 * @param objectTypeFromTicketData
	 * @return
	 */
	private Ticket.TicketObjectType findObjectType(
			final String objectTypeFromTicketData) {
		if (objectTypeFromTicketData == null
				|| objectTypeFromTicketData.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty objectTypeFromTicketData");
		}

		if (objectTypeFromTicketData.equals("data")) {
			return Ticket.TicketObjectType.DATA_OBJECT;
		} else {
			return Ticket.TicketObjectType.COLLECTION;
		}
	}

	//
	// @Override
	// public IRODSQueryResultSetInterface
	// getTicketsForSpecifiedDataObjectPath(String path, int continueIndex)
	// throws JargonException, JargonQueryException {
	//
	// //// NEED to rewrite this - have to do query for both data object name
	// and data object collection in the case of the data object type.
	//
	// IRODSQueryResultSetInterface resultSet = null;
	// ObjectType objType = null;
	// String type = null;
	// String colName = null;
	//
	// CollectionAndDataObjectListAndSearchAO listAndSearch =
	// irodsAccessObjectFactory
	// .getCollectionAndDataObjectListAndSearchAO(irodsAccount);
	// ObjStat objStat = listAndSearch.retrieveObjectStatForPath(path);
	// objType = objStat.getObjectType();
	// if (objType == ObjectType.COLLECTION) {
	// type = "collection";
	// colName = RodsGenQueryEnum.COL_TICKET_COLL_NAME.getName();
	// }
	// else
	// if (objType == ObjectType.DATA_OBJECT) {
	// type = "data";
	// colName = RodsGenQueryEnum.COL_TICKET_DATA_COLL_NAME.getName();
	// }
	// else {
	// throw new JargonException("illegal data object type");
	// }
	//
	// String queryString = buildQueryStringForTicketLS(
	// colName, path, type);
	//
	// IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString, 100);
	//
	// IRODSGenQueryExecutor irodsGenQueryExecutor = irodsAccessObjectFactory
	// .getIRODSGenQueryExecutor(irodsAccount);
	//
	// resultSet =
	// irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(irodsQuery,
	// continueIndex);
	//
	// return resultSet;
	//
	// }

	/**
	 * Build the select portion of the gen query when querying for tickets
	 * associated with data objects in iRODS (Files)
	 * 
	 * @return <code>String</code> with the iRODS gen query 'SELECT' statement
	 *         without a WHERE keyword or clause. This can be augmented by the
	 *         caller to produce an iRODS gen query with conditions.
	 */
	private String buildQuerySelectForLSAllTicketsForDataObjects() {
		StringBuilder queryString = new StringBuilder();
		queryString.append(buildQuerySelectForTicketsCommon());
		queryString.append(COMMA_SPACE);
		queryString.append(RodsGenQueryEnum.COL_TICKET_DATA_NAME.getName());
		queryString.append(COMMA_SPACE);
		queryString
				.append(RodsGenQueryEnum.COL_TICKET_DATA_COLL_NAME.getName());

		return queryString.toString();

	}

	/**
	 * @param queryString
	 */
	private String buildQuerySelectForTicketsCommon() {
		StringBuilder queryString = new StringBuilder();
		queryString.append("SELECT ");
		queryString.append(RodsGenQueryEnum.COL_TICKET_ID.getName());
		queryString.append(COMMA_SPACE);
		queryString.append(RodsGenQueryEnum.COL_TICKET_STRING.getName());
		queryString.append(COMMA_SPACE);
		queryString.append(RodsGenQueryEnum.COL_TICKET_TYPE.getName());
		queryString.append(COMMA_SPACE);
		queryString.append(RodsGenQueryEnum.COL_TICKET_OBJECT_TYPE.getName());
		queryString.append(COMMA_SPACE);
		queryString.append(RodsGenQueryEnum.COL_TICKET_OWNER_NAME.getName());
		queryString.append(COMMA_SPACE);
		queryString.append(RodsGenQueryEnum.COL_TICKET_OWNER_ZONE.getName());
		queryString.append(COMMA_SPACE);
		queryString.append(RodsGenQueryEnum.COL_TICKET_USES_COUNT.getName());
		queryString.append(COMMA_SPACE);
		queryString.append(RodsGenQueryEnum.COL_TICKET_USES_LIMIT.getName());
		queryString.append(COMMA_SPACE);
		queryString.append(RodsGenQueryEnum.COL_TICKET_WRITE_FILE_COUNT
				.getName());
		queryString.append(COMMA_SPACE);
		queryString.append(RodsGenQueryEnum.COL_TICKET_WRITE_FILE_LIMIT
				.getName());
		queryString.append(COMMA_SPACE);
		queryString.append(RodsGenQueryEnum.COL_TICKET_WRITE_BYTE_COUNT
				.getName());
		queryString.append(COMMA_SPACE);
		queryString.append(RodsGenQueryEnum.COL_TICKET_WRITE_BYTE_LIMIT
				.getName());
		queryString.append(COMMA_SPACE);
		queryString.append(RodsGenQueryEnum.COL_TICKET_EXPIRY_TS.getName());
		return queryString.toString();
	}

	private List<String> listRestrictionsForSpecifiedTicketCommon(
			final String ticketId, final RodsGenQueryEnum col,
			final int partialStartIndex) throws JargonException {

		IRODSGenQuery irodsQuery = null;
		List<String> restrictions = new ArrayList<String>();

		StringBuilder queryString = new StringBuilder();
		queryString.append("SELECT ");
		queryString.append(col.getName());
		queryString.append(" WHERE ");
		queryString.append(RodsGenQueryEnum.COL_TICKET_STRING.getName());
		queryString.append(" = ");
		queryString.append("'");
		queryString.append(ticketId);
		queryString.append("'");

		try {
			irodsQuery = IRODSGenQuery.instance(queryString.toString(),
					irodsAccessObjectFactory.getJargonProperties()
							.getMaxFilesAndDirsQueryMax());
			IRODSGenQueryExecutor irodsGenQueryExecutor = irodsAccessObjectFactory
					.getIRODSGenQueryExecutor(irodsAccount);

			IRODSQueryResultSet resultSet = irodsGenQueryExecutor
					.executeIRODSQueryAndCloseResult(irodsQuery,
							partialStartIndex);
			for (IRODSQueryResultRow row : resultSet.getResults()) {
				restrictions.add(row.getColumn(0));
			}

		} catch (JargonQueryException e) {
			log.error("query exception for ticket query:{}", irodsQuery, e);
			throw new JargonException(ERROR_IN_TICKET_QUERY, e);
		}

		return restrictions;
	}

	// need this function to determine whether the ticket being queried for is
	// for a data object or collection
	private Ticket.TicketObjectType getTicketObjectType(final String ticketId)
			throws JargonException {

		IRODSQueryResultSetInterface resultSet = null;

		if ((ticketId == null) || (ticketId.isEmpty())) {
			throw new IllegalArgumentException("illegal ticket id");
		}

		StringBuilder queryString = new StringBuilder();

		queryString.append("select ");
		queryString.append(RodsGenQueryEnum.COL_TICKET_OBJECT_TYPE.getName());
		queryString.append(" where ");
		queryString.append(RodsGenQueryEnum.COL_TICKET_STRING.getName());
		queryString.append(" = ");
		queryString.append("'");
		queryString.append(ticketId);
		queryString.append("'");

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(
				queryString.toString(), 1);

		IRODSGenQueryExecutor irodsGenQueryExecutor = irodsAccessObjectFactory
				.getIRODSGenQueryExecutor(irodsAccount);

		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(
					irodsQuery, 0);
		} catch (JargonQueryException e) {
			log.error("query exception for ticket query:{}", queryString, e);
			throw new JargonException(ERROR_IN_TICKET_QUERY, e);
		}
		if ((resultSet != null) && (!resultSet.getResults().isEmpty())) {
			if (resultSet.getFirstResult().getQueryResultColumns().get(0)
					.equals("data")) {
				return Ticket.TicketObjectType.DATA_OBJECT;
			} else {
				return Ticket.TicketObjectType.COLLECTION;
			}
		} else {
			throw new DataNotFoundException(TICKET_NOT_FOUND);
		}
	}

	public IRODSAccessObjectFactory getIrodsAccessObjectFactory() {
		return irodsAccessObjectFactory;
	}

	public void setIrodsAccessObjectFactory(
			final IRODSAccessObjectFactory irodsAccessObjectFactory) {
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
	}

	public IRODSAccount getIrodsAccount() {
		return irodsAccount;
	}

	public void setIrodsAccount(final IRODSAccount irodsAccount) {
		this.irodsAccount = irodsAccount;
	}

}
