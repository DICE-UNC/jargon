package org.irods.jargon.ticket;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSProtocolManager;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.IRODSSimpleProtocolManager;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.Tag;
import org.irods.jargon.core.protovalues.ErrorEnum;
import org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO;
import org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAOImpl;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSGenQueryExecutor;
import org.irods.jargon.core.pub.ProtocolExtensionPoint;
import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType;
import org.irods.jargon.core.query.IRODSGenQuery;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.ticket.packinstr.TicketAdminInp;
import org.irods.jargon.ticket.packinstr.TicketCreateModeEnum;
import org.irods.jargon.ticket.utils.TicketRandomString;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TicketAdminServiceImpl implements TicketAdminService {
	
	private static final String ERROR_IN_TICKET_QUERY = "error in ticket query";
	private static final String TICKET_NOT_FOUND = "IRODS ticket not found";
	public static final Logger log = LoggerFactory.getLogger(TicketAdminServiceImpl.class);
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
			final IRODSAccount irodsAccount)
			throws JargonException {
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
	public String createTicket(TicketCreateModeEnum mode, IRODSFile file, String ticketId)
			throws JargonException, DuplicateDataException {
		
		if (file == null) {
			throw new IllegalArgumentException("cannot create ticket with null IRODS file/collection");
		}
		
		log.info("creating at ticket for :{}", file.getPath());
		
		if (mode == null) {
			throw new IllegalArgumentException("cannot create ticket with null create mode - read or write access");
		}
		
		if (ticketId == null || ticketId.isEmpty()) {
			// create a new ticket string 15 chars in length
			ticketId = new TicketRandomString(15).nextString();
		}
		log.info("ticket creation mode is:{}", mode);

		TicketAdminInp ticketPI = TicketAdminInp.instanceForCreate(mode, file.getAbsolutePath(), ticketId);
		log.info("executing ticket PI");
		
		ProtocolExtensionPoint pep = irodsAccessObjectFactory
				.getProtocolExtensionPoint(irodsAccount);	
		Tag ticketOperationResponse = pep.irodsFunction(ticketPI);

		log.info("received response from ticket operation:{}",
				ticketOperationResponse);

		return ticketId;
	}
	
	@Override
	public void deleteTicket(String ticketId) throws JargonException {

		Tag ticketOperationResponse = null;
		
		if ((ticketId == null) || (ticketId.isEmpty())) {
			throw new IllegalArgumentException("cannot delete ticket with null or empty ticketId");
		}
		
		log.info("deleting ticket id/string:{}", ticketId);
		
		TicketAdminInp ticketPI = TicketAdminInp.instanceForDelete(ticketId);
		log.info("executing ticket PI");
		
		ProtocolExtensionPoint pep = irodsAccessObjectFactory
				.getProtocolExtensionPoint(irodsAccount);
		try {
			ticketOperationResponse = pep.irodsFunction(ticketPI);
		} catch (JargonException e) {
			if (e.getUnderlyingIRODSExceptionCode() == ErrorEnum.CAT_TICKET_INVALID.getInt()) {
				throw new DataNotFoundException(TICKET_NOT_FOUND);
			}
		}

		log.info("received response from ticket operation:{}",
				ticketOperationResponse);
		
	}
	
	@Override
	public IRODSQueryResultSetInterface getTicketQueryResultForSpecifiedTicketString(String ticketId)
			throws JargonException {
		
		IRODSQueryResultSetInterface resultSet = null;
		
		String objType = getTicketObjectType(ticketId);
		
		String queryString = buildQueryStringForTicketLS(
				RodsGenQueryEnum.COL_TICKET_STRING.getName(), ticketId, objType);
		
		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString, 2);
	
		IRODSGenQueryExecutor irodsGenQueryExecutor = irodsAccessObjectFactory
				.getIRODSGenQueryExecutor(irodsAccount);
		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(irodsQuery, 0);
		} catch (JargonQueryException e) {
			log.error("query exception for ticket query: {}", irodsQuery, e);
			throw new JargonException(ERROR_IN_TICKET_QUERY, e);
		}
		
		if ((resultSet == null) || (resultSet.getResults().isEmpty())) {
			throw new DataNotFoundException(TICKET_NOT_FOUND);
		}
		
		return resultSet;
	}
	
//	@Override
//	public Ticket getTicketForSpecifiedTicketString(String ticketId) throws JargonException, JargonQueryException {
//		
//		Ticket ticket = null;
//		IRODSQueryResultSetInterface resultSet = null;
//		
//		resultSet = getTicketQueryResultForSpecifiedTicketString(ticketId);
//		ticket = new Ticket(resultSet.getFirstResult());
//		
//		return ticket;
//	}
//	
//	@Override
//	public IRODSQueryResultSetInterface getAllTickets(int continueIndex) throws JargonException, JargonQueryException {
//		
//		IRODSQueryResultSetInterface resultSet = null;
//		
//		String queryString = buildQueryStringForTicketLS_ALL(null, null);
//		
//		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString, 100);
//	
//		IRODSGenQueryExecutor irodsGenQueryExecutor = irodsAccessObjectFactory
//				.getIRODSGenQueryExecutor(irodsAccount);
//	
//		resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(irodsQuery, continueIndex);
//		
//		return resultSet;
//	}
//	
//	@Override
//	public IRODSQueryResultSetInterface getTicketAllowedUsersByTicketString(String ticketId, int continueIndex) throws JargonException, JargonQueryException {
//		IRODSQueryResultSetInterface resultSet = null;
//		
//		StringBuilder queryString = new StringBuilder();
//		
//		queryString.append("select ");
//		queryString.append(RodsGenQueryEnum.COL_TICKET_ALLOWED_USER_NAME.getName());
//		queryString.append(" where ");
//		queryString.append(RodsGenQueryEnum.COL_TICKET_STRING.getName());
//		queryString.append(" = ");
//		queryString.append("'");
//		queryString.append(ticketId);
//		queryString.append("'");
//		
//		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString.toString(), 100);
//		
//		IRODSGenQueryExecutor irodsGenQueryExecutor = irodsAccessObjectFactory
//				.getIRODSGenQueryExecutor(irodsAccount);
//	
//		resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(irodsQuery, continueIndex);
//		
//		return resultSet;
//	}
//	
//	@Override
//	public IRODSQueryResultSetInterface getTicketsForSpecifiedDataObjectPath(String path, int continueIndex) 
//			throws JargonException, JargonQueryException {
//		
////// NEED to rewrite this - have to do query for both data object name and data object collection in the case of the data object type.
//	
//		IRODSQueryResultSetInterface resultSet = null;
//		ObjectType objType = null;
//		String type = null;
//		String colName = null;
//		
//		CollectionAndDataObjectListAndSearchAO listAndSearch = irodsAccessObjectFactory
//			.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
//		ObjStat objStat = listAndSearch.retrieveObjectStatForPath(path);
//		objType = objStat.getObjectType();
//		if (objType == ObjectType.COLLECTION) {
//			type = "collection";
//			colName = RodsGenQueryEnum.COL_TICKET_COLL_NAME.getName();
//		}
//		else
//		if (objType == ObjectType.DATA_OBJECT) {
//			type = "data";
//			colName = RodsGenQueryEnum.COL_TICKET_DATA_COLL_NAME.getName();
//		}
//		else {
//			throw new JargonException("illegal data object type");
//		}
//		
//		String queryString = buildQueryStringForTicketLS(
//				colName, path, type);
//		
//		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString, 100);
//		
//		IRODSGenQueryExecutor irodsGenQueryExecutor = irodsAccessObjectFactory
//				.getIRODSGenQueryExecutor(irodsAccount);
//	
//		resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(irodsQuery, continueIndex);
//		
//		return resultSet;
//		
//	}

	public IRODSAccessObjectFactory getIrodsAccessObjectFactory() {
		return irodsAccessObjectFactory;
	}

	public void setIrodsAccessObjectFactory(
			IRODSAccessObjectFactory irodsAccessObjectFactory) {
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
	}

	public IRODSAccount getIrodsAccount() {
		return irodsAccount;
	}

	public void setIrodsAccount(IRODSAccount irodsAccount) {
		this.irodsAccount = irodsAccount;
	}
	
	private String buildQueryStringForTicketLS(String selectName, String selectVal, String objType) {
		
		// first find out if this is a data object or collection
	
		StringBuilder queryString = new StringBuilder();
		
		queryString.append("select ");
		queryString.append(RodsGenQueryEnum.COL_TICKET_ID.getName());
		queryString.append(", ");
		queryString.append(RodsGenQueryEnum.COL_TICKET_STRING.getName());
		queryString.append(", ");
		queryString.append(RodsGenQueryEnum.COL_TICKET_TYPE.getName());
		queryString.append(", ");
		queryString.append(RodsGenQueryEnum.COL_TICKET_OBJECT_TYPE.getName());
		queryString.append(", ");
		queryString.append(RodsGenQueryEnum.COL_TICKET_OWNER_NAME.getName());
		queryString.append(", ");
		queryString.append(RodsGenQueryEnum.COL_TICKET_OWNER_ZONE.getName());
		queryString.append(", ");
		queryString.append(RodsGenQueryEnum.COL_TICKET_USES_COUNT.getName());
		queryString.append(", ");
		queryString.append(RodsGenQueryEnum.COL_TICKET_USES_LIMIT.getName());
		queryString.append(", ");
		queryString.append(RodsGenQueryEnum.COL_TICKET_WRITE_FILE_COUNT.getName());
		queryString.append(", ");
		queryString.append(RodsGenQueryEnum.COL_TICKET_WRITE_FILE_LIMIT.getName());
		queryString.append(", ");
		queryString.append(RodsGenQueryEnum.COL_TICKET_WRITE_BYTE_COUNT.getName());
		queryString.append(", ");
		queryString.append(RodsGenQueryEnum.COL_TICKET_WRITE_BYTE_LIMIT.getName());
		queryString.append(", ");
		queryString.append(RodsGenQueryEnum.COL_TICKET_EXPIRY_TS.getName());
		
		if (objType.equals("data")) {
			queryString.append(", ");
			queryString.append(RodsGenQueryEnum.COL_TICKET_DATA_NAME.getName());
			queryString.append(", ");
			queryString.append(RodsGenQueryEnum.COL_TICKET_DATA_COLL_NAME.getName());
		}
		else {
			queryString.append(", ");
			queryString.append(RodsGenQueryEnum.COL_TICKET_COLL_NAME.getName());
		}

// TODO: not sure to ask for these
//			queryString.append(", ");
//			queryString.append(RodsGenQueryEnum.COL_TICKET_ALLOWED_USER_NAME.getName());
//			queryString.append(", ");
//			queryString.append(RodsGenQueryEnum.COL_TICKET_ALLOWED_GROUP_NAME.getName());
//			queryString.append(", ");
//			queryString.append(RodsGenQueryEnum.COL_TICKET_ALLOWED_HOST.getName());
		
		if ((selectName != null) && (!selectName.isEmpty()) && (selectVal != null) && (!selectVal.isEmpty())) {
			queryString.append(" where ");
			queryString.append(selectName);
			queryString.append(" = ");
			queryString.append("'");
			queryString.append(selectVal);
			queryString.append("'");
		}
		
		return queryString.toString();
		
	}
	
	private String buildQueryStringForTicketLS_ALL(String selectName, String selectVal) {
		
		StringBuilder queryString = new StringBuilder();
		
		queryString.append("select ");
		queryString.append(RodsGenQueryEnum.COL_TICKET_ID.getName());
		queryString.append(", ");
		queryString.append(RodsGenQueryEnum.COL_TICKET_STRING.getName());
		queryString.append(", ");
		queryString.append(RodsGenQueryEnum.COL_TICKET_TYPE.getName());
		queryString.append(", ");
		queryString.append(RodsGenQueryEnum.COL_TICKET_OBJECT_TYPE.getName());
		queryString.append(", ");
		queryString.append(RodsGenQueryEnum.COL_TICKET_OWNER_NAME.getName());
		queryString.append(", ");
		queryString.append(RodsGenQueryEnum.COL_TICKET_OWNER_ZONE.getName());
		queryString.append(", ");
		queryString.append(RodsGenQueryEnum.COL_TICKET_USES_COUNT.getName());
		queryString.append(", ");
		queryString.append(RodsGenQueryEnum.COL_TICKET_USES_LIMIT.getName());
		queryString.append(", ");
		queryString.append(RodsGenQueryEnum.COL_TICKET_WRITE_FILE_COUNT.getName());
		queryString.append(", ");
		queryString.append(RodsGenQueryEnum.COL_TICKET_WRITE_FILE_LIMIT.getName());
		queryString.append(", ");
		queryString.append(RodsGenQueryEnum.COL_TICKET_WRITE_BYTE_COUNT.getName());
		queryString.append(", ");
		queryString.append(RodsGenQueryEnum.COL_TICKET_WRITE_BYTE_LIMIT.getName());
		queryString.append(", ");
		queryString.append(RodsGenQueryEnum.COL_TICKET_EXPIRY_TS.getName());
// TODO: not sure to ask for these
//			queryString.append(", ");
//			queryString.append(RodsGenQueryEnum.COL_TICKET_ALLOWED_USER_NAME.getName());
//			queryString.append(", ");
//			queryString.append(RodsGenQueryEnum.COL_TICKET_ALLOWED_GROUP_NAME.getName());
//			queryString.append(", ");
//			queryString.append(RodsGenQueryEnum.COL_TICKET_ALLOWED_HOST.getName());
		
		if ((selectName != null) && (!selectName.isEmpty()) && (selectVal != null) && (!selectVal.isEmpty())) {
			queryString.append(" where ");
			queryString.append(selectName);
			queryString.append(" = ");
			queryString.append("'");
			queryString.append(selectVal);
			queryString.append("'");
		}
		
		return queryString.toString();
		
	}
	
	// need this function to determine whether the ticket being queried for is for a data object or collection
	private String getTicketObjectType(String ticketId) throws JargonException {
		
		String objType = null;
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
		
		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString.toString(), 1);
		
		IRODSGenQueryExecutor irodsGenQueryExecutor = irodsAccessObjectFactory
				.getIRODSGenQueryExecutor(irodsAccount);
	
		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(irodsQuery, 0);
		} catch (JargonQueryException e) {
			log.error("query exception for ticket query:{}", queryString, e);
			throw new JargonException(ERROR_IN_TICKET_QUERY, e);
		}
		if ((resultSet != null) && (!resultSet.getResults().isEmpty())) {
			objType = resultSet.getFirstResult().getQueryResultColumns().get(0);
		}
		else {
			throw new DataNotFoundException(TICKET_NOT_FOUND);
		}
		
		return objType;
	}

}
