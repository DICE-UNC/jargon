package org.irods.jargon.core.pub;

import java.util.List;

import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.domain.AuditedAction;
import org.irods.jargon.core.pub.io.IRODSFile;

/**
 * Access object to query and view audit trail information for data objects.
 * <p>
 * For insight into the audit information, you can refer to the iRODS wiki at
 * https://irods.sdsc.edu/index.php/Audit_Records
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public interface DataObjectAuditAO {

	/**
	 * List all audit records for a given data object. This has a partial start
	 * index for paging through very large data sets. The
	 * {@code AuditedAction} objects contain information about 'more
	 * results' as well as sequence numbers to aid in paging.
	 *
	 * @param irodsFile
	 *            {@link IRODSFile} that will be the target of the query
	 * @param partialStart
	 *            {@code int} that is 0 or an offset into the result set,
	 *            for paging
	 * @param numberOfResultsDesired
	 *            {@code int} that indicates the number of results to
	 *            return in one query
	 *
	 * @return {@code List} of {@link AuditedAction} with information about
	 *         the audit history of the data object
	 * @throws FileNotFoundException
	 *             if data object is missing
	 * @throws JargonException
	 */
	List<AuditedAction> findAllAuditRecordsForDataObject(IRODSFile irodsFile,
			int partialStart, int numberOfResultsDesired)
					throws FileNotFoundException, JargonException;

	/**
	 * Get an individual audit action for a data object, given that you know
	 * enough fields to find the unique entry. This is sort of difficult (can
	 * can be expensive) as there is not a unique index or generated id to an
	 * audit event, so use sparingly.
	 *
	 * @param irodsFile
	 *            {@link IRODSFile} that will be the target of the query
	 * @param auditActionCode
	 *            {@code String} with the audited action code (the event
	 *            type)
	 * @param timeStampInIRODSFormat
	 *            {@code String} with the time stamp (in irods format) that
	 *            is associated with this event. Conveniently, the
	 *            {@code AuditedAction} object returned from a query has
	 *            this data in the correct format.
	 * @return {@link AuditedAction} with available details about the audit
	 *         event
	 * @throws DataNotFoundException
	 *             if the data object cannot be found
	 * @throws JargonException
	 */
	AuditedAction getAuditedActionForDataObject(IRODSFile irodsFile,
			String auditActionCode, String timeStampInIRODSFormat)
					throws DataNotFoundException, JargonException;

}
