package org.irods.jargon.httpstream;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener;

/**
 * Services to stream data into and out of iRODS from HTTP
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface HttpStreamingService {

	/**
	 * Accomplish a transfer by providing a URL in simple <code>String</code>
	 * form. This particular method will use an HTTP get to obtain an
	 * <code>InputStream</code>, which is then piped directly to the iRODS
	 * server.
	 * 
	 * @param sourceURL
	 *            <code>String</code> with the HTTP url to obtain a stream from
	 * @param irodsTargetFile
	 *            {@link IRODSFile} that will be the target of the transfer
	 * @param transferStatusCallbackListener
	 *            {@link TransferStatusCallbackListener} that can receive status
	 *            callbacks for transfer progress
	 * @param transferControlBlock
	 *            {@link TransferControlBlock} that can control aspects of the
	 *            running transfer
	 * @throws JargonException
	 *             for errors occurring within iRODS during the operation
	 * @throws HttpStreamingException
	 *             for errors occuring in the HTTP protocol operation
	 */
	public abstract void streamHttpUrlContentsToIRODSFile(
			final String sourceURL,
			final IRODSFile irodsTargetFile,
			final TransferStatusCallbackListener transferStatusCallbackListener,
			final TransferControlBlock transferControlBlock)
			throws JargonException, HttpStreamingException;

}