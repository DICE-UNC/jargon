package org.irods.jargon.core.pub;

import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.domain.AuditedAction;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Represents audit trail capabilities for data objects.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class DataObjectAuditAOImpl extends AbstractAuditAOImpl implements DataObjectAuditAO {

	public static final Logger log = LogManager.getLogger(DataObjectAuditAOImpl.class);
	public static final char COMMA = ',';

	/**
	 * Default constructor as invoked by {@link IRODSAccessObjectFactory}
	 *
	 * @param irodsSession
	 *            {@link IRODSSession}
	 * @param irodsAccount
	 *            {@link IRODSAccount}
	 * @throws JargonException
	 *             for iRODS error
	 */
	protected DataObjectAuditAOImpl(final IRODSSession irodsSession, final IRODSAccount irodsAccount)
			throws JargonException {
		super(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.irods.jargon.core.pub.DataObjectAuditAO#getAuditedActionForDataObject
	 * (org.irods.jargon.core.pub.io.IRODSFile, java.lang.String, java.lang.String)
	 */
	@Override
	public AuditedAction getAuditedActionForDataObject(final IRODSFile irodsFile, final String auditActionCode,
			final String timeStampInIRODSFormat) throws DataNotFoundException, JargonException {

		log.info("getAuditedActionForDataObject()");

		return super.getAuditedActionForFile(irodsFile, auditActionCode, timeStampInIRODSFormat);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.irods.jargon.core.pub.DataObjectAuditAO#findAllAuditRecords(org.irods
	 * .jargon.core.pub.io.IRODSFile, int)
	 */
	@Override
	public List<AuditedAction> findAllAuditRecordsForDataObject(final IRODSFile irodsFile, final int partialStart,
			final int numberOfResultsDesired) throws FileNotFoundException, JargonException {

		log.info("findAllAuditRecordsForDataObject()");
		return super.findAllAuditRecordsForFile(irodsFile, partialStart, numberOfResultsDesired);

	}

}
