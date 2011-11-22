package org.irods.jargon.core.pub;

import java.util.List;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.domain.AuditedAction;
import org.irods.jargon.core.pub.io.IRODSFile;

/**
 *  Represents audit trail capabilities for collections 
 * @author Mike Conway - DICE (www.irods.org)
 */
public interface CollectionAuditAO {

	/**
	 * Find all audit records for a given collection path, allowing for partial starts
	 * @param irodsFile {@link IRODSFile} that represents the collection
	 * @param partialStart <code>int</code> with an offset into the results to start at
	 * @return <code>List</code> of {@link AuditedAction} that contain the audit trail information
	 * @throws JargonException
	 */
	List<AuditedAction> findAllAuditRecordsForCollection(IRODSFile irodsFile,
			int partialStart) throws JargonException;

}
