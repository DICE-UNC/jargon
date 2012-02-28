package org.irods.jargon.ticket;

import java.util.ArrayList;
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

	// @Override
	// @Deprecated
	/**
	 * @deprecated - don't expose query stuff to public callers, shift to
	 *             methods returning plain java objects (e.g. tickets)
	 */
	// public IRODSQueryResultSetInterface
	// getTicketQueryResultForSpecifiedTicketString(
	// final String ticketId, final Ticket.TicketObjectType objectType)
	// throws JargonException {
	//
	// IRODSQueryResultSetInterface resultSet = null;
	//
	// getTicketObjectType(ticketId);
	//
	// String queryString = buildQueryStringForTicket(
	// RodsGenQueryEnum.COL_TICKET_STRING.getName(), ticketId,
	// objectType);
	//
	// IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString, 2);
	//
	// IRODSGenQueryExecutor irodsGenQueryExecutor = irodsAccessObjectFactory
	// .getIRODSGenQueryExecutor(irodsAccount);
	// try {
	// resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(
	// irodsQuery, 0);
	// } catch (JargonQueryException e) {
	// log.error("query exception for ticket query: {}", irodsQuery, e);
	// throw new JargonException(ERROR_IN_TICKET_QUERY, e);
	// }
	//
	// if ((resultSet == null) || (resultSet.getResults().isEmpty())) {
	// throw new DataNotFoundException(TICKET_NOT_FOUND);
	// }
	//
	// return resultSet;
	// }

	@Deprecated
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
	// getTicketAllowedUsersByTicketString(String ticketId, int continueIndex)
	// throws JargonException, JargonQueryException {
	// IRODSQueryResultSetInterface resultSet = null;
	//
	// StringBuilder queryString = new StringBuilder();
	//
	// queryString.append("select ");
	// queryString.append(RodsGenQueryEnum.COL_TICKET_ALLOWED_USER_NAME.getName());
	// queryString.append(" where ");
	// queryString.append(RodsGenQueryEnum.COL_TICKET_STRING.getName());
	// queryString.append(" = ");
	// queryString.append("'");
	// queryString.append(ticketId);
	// queryString.append("'");
	//
	// IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString.toString(),
	// 100);
	//
	// IRODSGenQueryExecutor irodsGenQueryExecutor = irodsAccessObjectFactory
	// .getIRODSGenQueryExecutor(irodsAccount);
	//
	// resultSet =
	// irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(irodsQuery,
	// continueIndex);
	//
	// return resultSet;
	// }
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

	// private String buildQueryStringForTicket(final String selectName,
	// final String selectVal, final Ticket.TicketObjectType objType) {
	//
	// // first find out if this is a data object or collection
	//
	// StringBuilder queryString = new StringBuilder();
	//
	// queryString.append("select ");
	// queryString.append(RodsGenQueryEnum.COL_TICKET_ID.getName());
	// queryString.append(COMMA_SPACE);
	// queryString.append(RodsGenQueryEnum.COL_TICKET_STRING.getName());
	// queryString.append(COMMA_SPACE);
	// queryString.append(RodsGenQueryEnum.COL_TICKET_TYPE.getName());
	// queryString.append(COMMA_SPACE);
	// queryString.append(RodsGenQueryEnum.COL_TICKET_OBJECT_TYPE.getName());
	// queryString.append(COMMA_SPACE);
	// queryString.append(RodsGenQueryEnum.COL_TICKET_OWNER_NAME.getName());
	// queryString.append(COMMA_SPACE);
	// queryString.append(RodsGenQueryEnum.COL_TICKET_OWNER_ZONE.getName());
	// queryString.append(COMMA_SPACE);
	// queryString.append(RodsGenQueryEnum.COL_TICKET_USES_COUNT.getName());
	// queryString.append(COMMA_SPACE);
	// queryString.append(RodsGenQueryEnum.COL_TICKET_USES_LIMIT.getName());
	// queryString.append(COMMA_SPACE);
	// queryString.append(RodsGenQueryEnum.COL_TICKET_WRITE_FILE_COUNT
	// .getName());
	// queryString.append(COMMA_SPACE);
	// queryString.append(RodsGenQueryEnum.COL_TICKET_WRITE_FILE_LIMIT
	// .getName());
	// queryString.append(COMMA_SPACE);
	// queryString.append(RodsGenQueryEnum.COL_TICKET_WRITE_BYTE_COUNT
	// .getName());
	// queryString.append(COMMA_SPACE);
	// queryString.append(RodsGenQueryEnum.COL_TICKET_WRITE_BYTE_LIMIT
	// .getName());
	// queryString.append(COMMA_SPACE);
	// queryString.append(RodsGenQueryEnum.COL_TICKET_EXPIRY_TS.getName());
	//
	// if (objType == Ticket.TicketObjectType.DATA_OBJECT) {
	// queryString.append(COMMA_SPACE);
	// queryString.append(RodsGenQueryEnum.COL_TICKET_DATA_NAME.getName());
	// queryString.append(COMMA_SPACE);
	// queryString.append(RodsGenQueryEnum.COL_TICKET_DATA_COLL_NAME
	// .getName());
	// } else {
	// queryString.append(COMMA_SPACE);
	// queryString.append(RodsGenQueryEnum.COL_TICKET_COLL_NAME.getName());
	// }

	// TODO: not sure to ask for these
	// queryString.append(", ");
	// queryString.append(RodsGenQueryEnum.COL_TICKET_ALLOWED_USER_NAME.getName());
	// queryString.append(", ");
	// queryString.append(RodsGenQueryEnum.COL_TICKET_ALLOWED_GROUP_NAME.getName());
	// queryString.append(", ");
	// queryString.append(RodsGenQueryEnum.COL_TICKET_ALLOWED_HOST.getName());

	// if ((selectName != null) && (!selectName.isEmpty())
	// && (selectVal != null) && (!selectVal.isEmpty())) {
	// queryString.append(" where ");
	// queryString.append(selectName);
	// queryString.append(" = ");
	// queryString.append("'");
	// queryString.append(selectVal);
	// queryString.append("'");
	// }
	//
	// return queryString.toString();
	//
	// }

	/*
	 * private String buildQueryStringForTicketLS_ALL(final String selectName,
	 * final String selectVal) {
	 * 
	 * StringBuilder queryString = new StringBuilder();
	 * 
	 * queryString.append("select ");
	 * queryString.append(RodsGenQueryEnum.COL_TICKET_ID.getName());
	 * queryString.append(COMMA_SPACE);
	 * queryString.append(RodsGenQueryEnum.COL_TICKET_STRING.getName());
	 * queryString.append(COMMA_SPACE);
	 * queryString.append(RodsGenQueryEnum.COL_TICKET_TYPE.getName());
	 * queryString.append(COMMA_SPACE);
	 * queryString.append(RodsGenQueryEnum.COL_TICKET_OBJECT_TYPE.getName());
	 * queryString.append(COMMA_SPACE);
	 * queryString.append(RodsGenQueryEnum.COL_TICKET_OWNER_NAME.getName());
	 * queryString.append(COMMA_SPACE);
	 * queryString.append(RodsGenQueryEnum.COL_TICKET_OWNER_ZONE.getName());
	 * queryString.append(COMMA_SPACE);
	 * queryString.append(RodsGenQueryEnum.COL_TICKET_USES_COUNT.getName());
	 * queryString.append(COMMA_SPACE);
	 * queryString.append(RodsGenQueryEnum.COL_TICKET_USES_LIMIT.getName());
	 * queryString.append(COMMA_SPACE);
	 * queryString.append(RodsGenQueryEnum.COL_TICKET_WRITE_FILE_COUNT
	 * .getName()); queryString.append(COMMA_SPACE);
	 * queryString.append(RodsGenQueryEnum.COL_TICKET_WRITE_FILE_LIMIT
	 * .getName()); queryString.append(COMMA_SPACE);
	 * queryString.append(RodsGenQueryEnum.COL_TICKET_WRITE_BYTE_COUNT
	 * .getName()); queryString.append(COMMA_SPACE);
	 * queryString.append(RodsGenQueryEnum.COL_TICKET_WRITE_BYTE_LIMIT
	 * .getName()); queryString.append(COMMA_SPACE);
	 * queryString.append(RodsGenQueryEnum.COL_TICKET_EXPIRY_TS.getName()); //
	 * TODO: not sure to ask for these // queryString.append(", "); //
	 * queryString
	 * .append(RodsGenQueryEnum.COL_TICKET_ALLOWED_USER_NAME.getName()); //
	 * queryString.append(", "); //
	 * queryString.append(RodsGenQueryEnum.COL_TICKET_ALLOWED_GROUP_NAME
	 * .getName()); // queryString.append(", "); //
	 * queryString.append(RodsGenQueryEnum.COL_TICKET_ALLOWED_HOST.getName());
	 * 
	 * if ((selectName != null) && (!selectName.isEmpty()) && (selectVal !=
	 * null) && (!selectVal.isEmpty())) { queryString.append(" where ");
	 * queryString.append(selectName); queryString.append(" = ");
	 * queryString.append("'"); queryString.append(selectVal);
	 * queryString.append("'"); }
	 * 
	 * return queryString.toString();
	 * 
	 * }
	 */

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
