package org.irods.jargon.httpstream;

/**
 * An HTTP exception or URL processing exception
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class HttpStreamingException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3177260897750311917L;

	/**
	 * 
	 */
	public HttpStreamingException() {

	}

	/**
	 * @param message
	 */
	public HttpStreamingException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public HttpStreamingException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public HttpStreamingException(String message, Throwable cause) {
		super(message, cause);
	}

}
