package org.irods.jargon.core.pub;

import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.domain.AuditedAction;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents audit trail capabilities for collections
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class CollectionAuditAOImpl extends AbstractAuditAOImpl implements
CollectionAuditAO {

	public static final Logger log = LoggerFactory
			.getLogger(CollectionAuditAOImpl.class);
	public static final char COMMA = ',';

	/**
	 * Default constructor as invoked by {@link IRODSAccessObjectFactory}
	 *
	 * @param irodsSession
	 * @param irodsAccount
	 * @throws JargonException
	 */
	protected CollectionAuditAOImpl(final IRODSSession irodsSession,
			final IRODSAccount irodsAccount) throws JargonException {
		super(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.irods.jargon.core.pub.CollectionAuditAO#findAllAuditRecordsForCollection
	 * (org.irods.jargon.core.pub.io.IRODSFile, int, int)
	 */
	@Override
	public List<AuditedAction> findAllAuditRecordsForCollection(
			final IRODSFile irodsFile, final int partialStart,
			final int numberOfResultsDesired) throws FileNotFoundException,
			JargonException {

		log.info("findAllAuditRecordsForCollection()");
		return super.findAllAuditRecordsForFile(irodsFile, partialStart,
				numberOfResultsDesired);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.irods.jargon.core.pub.CollectionAuditAO#getAuditedActionForCollection
	 * (org.irods.jargon.core.pub.io.IRODSFile, java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public AuditedAction getAuditedActionForCollection(
			final IRODSFile irodsFile, final String auditActionCode,
			final String timeStampInIRODSFormat) throws DataNotFoundException,
			JargonException {

		log.info("getAuditedActionForDataObject()");

		return super.getAuditedActionForFile(irodsFile, auditActionCode,
				timeStampInIRODSFormat);

	}

}
