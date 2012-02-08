package org.irods.jargon.ticket;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.GeneralAdminInp;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSGenericAO;
import org.irods.jargon.core.pub.ProtocolExtensionPoint;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.ticket.packinstr.TicketAdminInp;
import org.irods.jargon.ticket.packinstr.TicketCreateModeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TicketAOImpl extends IRODSGenericAO implements TicketAO {
	
	Logger log = LoggerFactory.getLogger(this.getClass());

	public TicketAOImpl(IRODSSession irodsSession, IRODSAccount irodsAccount)
			throws JargonException {
		super(irodsSession, irodsAccount);
		// TODO Auto-generated constructor stub
	}
	
	public String createTicket(TicketCreateModeEnum mode, IRODSFile file, String ticketId) throws JargonException {

		if (file == null) {
			throw new IllegalArgumentException("cannot create ticket with null IRODS file/collection");
		}
		
		if (log.isDebugEnabled()) {
			log.debug("creating at ticket for :{}", file.getPath());
		}
		
		if (mode == null) {
			throw new IllegalArgumentException("cannot create ticket with null create mode - read or write access");
		}
		log.debug("ticket creation mode is:{}", mode);

		TicketAdminInp ticketPI = TicketAdminInp.instanceForCreate(mode, file.getAbsolutePath(), ticketId);
		log.debug("executing ticket PI");
		

		return null;
	}

}
