/**
 * 
 */
package org.irods.jargon.core.pub.io;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSGenericAO;

/**
 * Variant of the access object that supports iRODS file io operations.  This subclass supports the streaming of large data streams from the remote execution of 
 * scripts by iRODS.  
 * <p/>
 * When executing a remote script, the standard output is captured in a buf and returned in the output packing instruction message.  If that result is larger than 
 * an iRODS-defined threshold, the remainder of the stream is sent separately.  iRODS will bind this additional output to a file descriptor.  This access object uses that 
 * file descriptor to read from the file in the same manner as IRODSFileInputStream does, using the same <code>DataObjInp</code> packing instruction set.
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class RemoteStreamFileIOOperationsAOImpl extends IRODSGenericAO {

	/**
	 * @param irodsSession
	 * @param irodsAccount
	 * @throws JargonException
	 */
	public RemoteStreamFileIOOperationsAOImpl(IRODSSession irodsSession,
			IRODSAccount irodsAccount) throws JargonException {
		super(irodsSession, irodsAccount);
	}

}
