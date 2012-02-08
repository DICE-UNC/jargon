package org.irods.jargon.ticket;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.ticket.packinstr.TicketAdminInp;
import org.irods.jargon.ticket.packinstr.TicketCreateModeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TicketAdminServiceImpl implements TicketAdminService {
	
	Logger log = LoggerFactory.getLogger(this.getClass());
	private IRODSAccessObjectFactory irodsAccessObjectFactory;
	private IRODSAccount irodsAccount;

	public TicketAdminServiceImpl(
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount)
			throws JargonException {
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
