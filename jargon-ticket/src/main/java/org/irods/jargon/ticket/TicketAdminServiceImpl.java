package org.irods.jargon.ticket;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.Tag;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSGenQueryExecutor;
import org.irods.jargon.core.pub.ProtocolExtensionPoint;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.IRODSGenQuery;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.ticket.packinstr.TicketAdminInp;
import org.irods.jargon.ticket.packinstr.TicketCreateModeEnum;
import org.irods.jargon.ticket.utils.TicketRandomString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TicketAdminServiceImpl implements TicketAdminService {
	
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
	public String createTicket(TicketCreateModeEnum mode, IRODSFile file, String ticketId) throws JargonException {

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

		if ((ticketId == null) || (ticketId.isEmpty())) {
			throw new IllegalArgumentException("cannot delete ticket with null or empty ticketId");
		}
		
		log.info("deleting ticket id/string:{}", ticketId);
		
		TicketAdminInp ticketPI = TicketAdminInp.instanceForDelete(ticketId);
		log.info("executing ticket PI");
		
		ProtocolExtensionPoint pep = irodsAccessObjectFactory
				.getProtocolExtensionPoint(irodsAccount);
		Tag ticketOperationResponse = pep.irodsFunction(ticketPI);

		log.info("received response from ticket operation:{}",
				ticketOperationResponse);
		
	}
	
	@Override
	public IRODSQueryResultSetInterface listTicketByTicketString(String ticketId) throws JargonException, JargonQueryException {
		
		IRODSQueryResultSetInterface resultSet = null;
		
		String queryString = "select "
			+ RodsGenQueryEnum.COL_TICKET_ID.getName()
			+ ", "
			+ RodsGenQueryEnum.COL_TICKET_STRING.getName()
			+ ", "
			+ RodsGenQueryEnum.COL_TICKET_TYPE.getName()
			+ ", "
			+ RodsGenQueryEnum.COL_TICKET_OBJECT_TYPE.getName()
			+ ", "
			+ RodsGenQueryEnum.COL_TICKET_OWNER_NAME.getName()
			+ ", "
			+ RodsGenQueryEnum.COL_TICKET_OWNER_ZONE.getName()
			+ ", "
			+ RodsGenQueryEnum.COL_TICKET_USES_COUNT.getName()
			+ ", "
			+ RodsGenQueryEnum.COL_TICKET_USES_LIMIT.getName()
			+ ", "
			+ RodsGenQueryEnum.COL_TICKET_WRITE_FILE_COUNT.getName()
			+ ", "
			+ RodsGenQueryEnum.COL_TICKET_WRITE_FILE_LIMIT.getName()
			+ ", "
			+ RodsGenQueryEnum.COL_TICKET_WRITE_BYTE_COUNT.getName()
			+ ", "
			+ RodsGenQueryEnum.COL_TICKET_WRITE_BYTE_LIMIT.getName()
			+ ", "
			+ RodsGenQueryEnum.COL_TICKET_EXPIRY_TS.getName()
			+ ", "
			+ RodsGenQueryEnum.COL_TICKET_DATA_NAME.getName()
			+ ", "
			+ RodsGenQueryEnum.COL_TICKET_DATA_COLL_NAME.getName()
// TODO: not sure to ask for these
//			+ ", "
//			+ RodsGenQueryEnum.COL_TICKET_ALLOWED_USER_NAME.getName()
//			+ ", "
//			+ RodsGenQueryEnum.COL_TICKET_ALLOWED_GROUP_NAME.getName()
//			+ ", "
//			+ RodsGenQueryEnum.COL_TICKET_ALLOWED_HOST.getName()
			+ " where "
			+ RodsGenQueryEnum.COL_TICKET_STRING.getName()
			+ " = "
			+ "'"
			+ ticketId
			+ "'";

		// TODO: how many results should ask for - 100?
		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString, 100);
	
		IRODSGenQueryExecutor irodsGenQueryExecutor = irodsAccessObjectFactory
				.getIRODSGenQueryExecutor(irodsAccount);
	
		resultSet = irodsGenQueryExecutor.executeIRODSQuery(irodsQuery, 0);
		// TODO: should do this instead? resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(irodsQuery, 0);
		
		return resultSet;
	}

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

}
