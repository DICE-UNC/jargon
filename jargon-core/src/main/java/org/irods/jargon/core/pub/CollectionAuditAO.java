package org.irods.jargon.core.pub;

import java.util.List;

import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.domain.AuditedAction;
import org.irods.jargon.core.pub.io.IRODSFile;

/**
 * Represents audit trail capabilities for collections
 *
 * @author Mike Conway - DICE (www.irods.org)
 */
public interface CollectionAuditAO {

	/**
	 * Find all audit records for a given collection path, allowing for partial
	 * starts
	 *
	 * @param irodsFile
	 *            {@link IRODSFile} that represents the collection
	 * @param partialStart
	 *            <code>int</code> with an offset into the results to start at
	 * @param numberOfResultsDesired
	 *            <code>int</code> with the number of desired results for the
	 *            page of query results
	 * @return <code>List</code> of {@link AuditedAction} that contain the audit
	 *         trail information
	 * @throws FileNotFoundException
	 *             if the collection does not exist
	 * @throws JargonException
	 */
	List<AuditedAction> findAllAuditRecordsForCollection(IRODSFile irodsFile,
			int partialStart, int numberOfResultsDesired)
					throws FileNotFoundException, JargonException;

	/**
	 * Get an individual audit action for a collection, given that you know
	 * enough fields to find the unique entry. This is sort of difficult (can
	 * can be expensive) as there is not a unique index or generated id to an
	 * audit event, so use sparingly.
	 *
	 * @param irodsFile
	 *            {@link IRODSFile} that will be the target of the query
	 * @param auditActionCode
	 *            <code>String</code> with the audited action code (the event
	 *            type)
	 * @param timeStampInIRODSFormat
	 *            <code>String</code> with the time stamp (in irods format) that
	 *            is associated with this event. Conveniently, the
	 *            <code>AuditedAction</code> object returned from a query has
	 *            this data in the correct format.
	 * @return {@link AuditedAction} with available details about the audit
	 *         event
	 * @throws DataNotFoundException
	 *             if the data object cannot be found
	 * @throws JargonException
	 */
	AuditedAction getAuditedActionForCollection(IRODSFile irodsFile,
			String auditActionCode, String timeStampInIRODSFormat)
					throws DataNotFoundException, JargonException;

}
