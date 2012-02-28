package org.irods.jargon.ticket;

import java.io.File;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.OverwriteException;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client transfer and other operations that are ticket enabled, wrapped with
 * ticket semantics
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class TicketClientOperationsImpl implements TicketClientOperations {

	public static final Logger log = LoggerFactory
			.getLogger(TicketClientOperationsImpl.class);
	@SuppressWarnings("unused")
	private final IRODSAccessObjectFactory irodsAccessObjectFactory;
	@SuppressWarnings("unused")
	private final IRODSAccount irodsAccount;
	private DataTransferOperations dataTransferOperations = null;
	private TicketClientSupport ticketClientSupport = null;

	/**
	 * Constructor initializes service for 
	 * @param irodsAccessObjectFactory
	 * @param irodsAccount
	 * @throws JargonException
	 */
	public TicketClientOperationsImpl(final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) throws JargonException {
		
		if (irodsAccessObjectFactory == null) {
			throw new IllegalArgumentException("null irodsAccessObjectFactory");
		}

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
		this.irodsAccount = irodsAccount;
		
		this.dataTransferOperations = irodsAccessObjectFactory.getDataTransferOperations(irodsAccount);
		this.ticketClientSupport = new TicketClientSupport(irodsAccessObjectFactory,irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.ticket.TicketClientOperations#putFileToIRODSUsingTicket
	 * (java.lang.String, java.io.File, org.irods.jargon.core.pub.io.IRODSFile,
	 * org.irods.jargon.core.transfer.TransferStatusCallbackListener,
	 * org.irods.jargon.core.transfer.TransferControlBlock)
	 */
	@Override
	public void putFileToIRODSUsingTicket(
			final String ticketString,
			final File sourceFile,
			final IRODSFile targetIrodsFile,
			final TransferStatusCallbackListener transferStatusCallbackListener,
			final TransferControlBlock transferControlBlock)
			throws DataNotFoundException, OverwriteException, JargonException {

		log.info("putFileToIRODSUsingTicket()");

		if (ticketString == null || ticketString.isEmpty()) {
			throw new IllegalArgumentException("null or empty ticketString");
		}

		// other param checks done in delegated methods

		log.info("initializing session with ticket:{}", ticketString);
		ticketClientSupport.initializeSessionWithTicket(ticketString);

		log.info("session initialized, doing put operation");
		dataTransferOperations.putOperation(sourceFile, targetIrodsFile,
				transferStatusCallbackListener, transferControlBlock);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.ticket.TicketClientOperations#
	 * getOperationFromIRODSUsingTicket(java.lang.String,
	 * org.irods.jargon.core.pub.io.IRODSFile, java.io.File,
	 * org.irods.jargon.core.transfer.TransferStatusCallbackListener,
	 * org.irods.jargon.core.transfer.TransferControlBlock)
	 */
	@Override
	public void getOperationFromIRODSUsingTicket(String ticketString,
			IRODSFile irodsSourceFile, File targetLocalFile,
			TransferStatusCallbackListener transferStatusCallbackListener,
			TransferControlBlock transferControlBlock)
			throws DataNotFoundException, OverwriteException, JargonException {

		log.info("getFileFromIRODSUsingTicket()");

		if (ticketString == null || ticketString.isEmpty()) {
			throw new IllegalArgumentException("null or empty ticketString");
		}

		// other param checks done in delegated methods

		log.info("initializing session with ticket:{}", ticketString);
		ticketClientSupport.initializeSessionWithTicket(ticketString);

		log.info("session initialized, doing get operation");
		dataTransferOperations.getOperation(irodsSourceFile, targetLocalFile,
				transferStatusCallbackListener, transferControlBlock);

	}
}
