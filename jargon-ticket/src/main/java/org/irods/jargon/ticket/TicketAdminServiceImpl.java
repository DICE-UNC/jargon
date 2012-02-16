package org.irods.jargon.ticket;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.Tag;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.ProtocolExtensionPoint;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.ticket.packinstr.TicketAdminInp;
import org.irods.jargon.ticket.packinstr.TicketCreateModeEnum;
import org.irods.jargon.ticket.utils.TicketRandomString;
import org.junit.Ignore;
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
	
	@Ignore
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
