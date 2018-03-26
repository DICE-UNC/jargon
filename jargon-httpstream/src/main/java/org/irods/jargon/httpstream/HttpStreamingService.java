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
@Deprecated

public interface HttpStreamingService {

	/**
	 * Accomplish a transfer by providing a URL in simple {@code String} form. This
	 * particular method will use an HTTP get to obtain an {@code InputStream},
	 * which is then piped directly to the iRODS server.
	 * 
	 * @param sourceURL
	 *            {@code String} with the HTTP url to obtain a stream from
	 * @param irodsTargetFile
	 *            {@link IRODSFile} that will be the target of the transfer
	 * @param transferStatusCallbackListener
	 *            {@link TransferStatusCallbackListener} that can receive status
	 *            callbacks for transfer progress
	 * @param transferControlBlock
	 *            {@link TransferControlBlock} that can control aspects of the
	 *            running transfer
	 * @return {@code String} with the iRODS absolute path to the file that holds
	 *         the result of the operation
	 * @throws JargonException
	 *             for errors occurring within iRODS during the operation
	 * @throws HttpStreamingException
	 *             for errors occuring in the HTTP protocol operation
	 */
	public abstract String streamHttpUrlContentsToIRODSFile(final String sourceURL, final IRODSFile irodsTargetFile,
			final TransferStatusCallbackListener transferStatusCallbackListener,
			final TransferControlBlock transferControlBlock) throws JargonException, HttpStreamingException;

}