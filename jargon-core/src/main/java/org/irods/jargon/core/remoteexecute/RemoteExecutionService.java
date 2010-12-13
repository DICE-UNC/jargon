package org.irods.jargon.core.remoteexecute;

import java.io.InputStream;

import org.irods.jargon.core.exception.JargonException;

public interface RemoteExecutionService {

	/**
	 * Execute the remote script, and return the results as an input stream
	 * 
	 * @return <code>InputStream</code> containing the results of the remote
	 *         script invocation
	 * @throws JargonException
	 */
	public InputStream execute() throws JargonException;

	/**
	 * For iRODS 2.4.1+, it is possible to stream very large results from remote
	 * script invocation. Calling this method signals to iRODS that such
	 * streaming behavior is desired. This method is separate since it requires
	 * a different protocol, and will fail if invoked against a server that does
	 * not support this method of streaming.
	 * 
	 * @return <code>InputStream</code> containing the results of the remote
	 *         script invocation
	 * @throws JargonException
	 *             for an exception, including invocation against a server that
	 *             does not support the streaming flag.
	 */
	public InputStream executeAndStream() throws JargonException;

}