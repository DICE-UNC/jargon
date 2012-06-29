package org.irods.jargon.core.pub;

import java.util.List;

import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.domain.AuditedAction;
import org.irods.jargon.core.pub.io.IRODSFile;

/**
 * Access object to query and view audit trail information for data objects.
 * <p/>
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
	 * <code>AuditedAction</code> objects contain information about 'more
	 * results' as well as sequence numbers to aid in paging.
	 * 
	 * @param irodsFile
	 *            {@link IRODSFile} that will be the target of the query
	 * @param partialStart
	 *            <code>int</code> that is 0 or an offset into the result set,
	 *            for paging
	 * @param numberOfResultsDesired
	 *            <code>int</code> that indicates the number of results to
	 *            return in one query
	 * 
	 * @return <code>List</code> of {@link AuditedAction} with information about
	 *         the audit history of the data object
	 * @throws JargonException
	 */
	List<AuditedAction> findAllAuditRecordsForDataObject(IRODSFile irodsFile,
			int partialStart, int numberOfResultsDesired)
			throws JargonException;

	/**
	 * Retrieve an individual audit record based on the associated data object
	 * and the unique id of the audit record desired
	 * 
	 * @param irodsFile
	 *            {@link IRODSFile} that will be the target of the query
	 * @param id
	 *            <code>int</code> with the unique id (from iCAT) for this audit
	 *            entry
	 * @return {@link AuditedAction} with information about the audit history of
	 *         the data object
	 * @throws DataNotFoundException
	 *             if the requested audit data does not exist
	 * @throws JargonException
	 */
	AuditedAction getAuditedActionForDataObject(IRODSFile irodsFile, int id)
			throws DataNotFoundException, JargonException;

}