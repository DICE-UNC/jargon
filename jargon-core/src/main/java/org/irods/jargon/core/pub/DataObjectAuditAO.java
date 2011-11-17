package org.irods.jargon.core.pub;

import java.util.List;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.domain.AuditedAction;
import org.irods.jargon.core.pub.io.IRODSFile;

/**
 * Access object to query and view audit trail information for data objects.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public interface DataObjectAuditAO {

	/**
	 * List all audit records for a given data object.  This has a partial start index for paging through very large data sets.  The <code>AuditedAction</code>
	 * objects contain information about 'more results' as well as sequence numbers to aid in paging.
	 * @param irodsFile {@link IRODSFile} that will be the target of the query
	 * @param partialStart <code>int</code> that is 0 or an offset into the result set, for paging
	 * @return <code>List</code> of {@link AuditedAction} with information about the audit history of the data object
	 * @throws JargonException
	 */
	List<AuditedAction> findAllAuditRecordsForDataObject(final IRODSFile irodsFile,
			final int partialStart) throws JargonException;

}