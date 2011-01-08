package org.irods.jargon.core.remoteexecute;

import java.io.InputStream;

import org.irods.jargon.core.exception.JargonException;

/**
 * Interface that describes a mid-level service that handles remote execution of a script on iRODS, and the streaming of the results as a binary
 * stream to the client.  These remote scripts are defined in a special directory on the iRODS server, and can do arbitrary tasks.
 * @author mikeconway
 *
 */
public interface RemoteExecutionService {

	/**
	 * Execute the remote script, and return the results as an input stream.  Note that this method
	 * indicates to iRODS that the client does not want to receive vary large results in stream form.  There
	 * are other methods in this class that indicate to iRODS that the behavior of streaming very large results from 
	 * remote execution of scripts is desired.
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