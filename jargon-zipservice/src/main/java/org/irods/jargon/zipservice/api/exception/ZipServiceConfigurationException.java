/**
 * 
 */
package org.irods.jargon.zipservice.api.exception;

/**
 * @author Mike Conway error in the configuration of iRODS or the zip service
 */
public class ZipServiceConfigurationException extends ZipServiceException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2164051501532502589L;

	/**
	 * @param message
	 */
	public ZipServiceConfigurationException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ZipServiceConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public ZipServiceConfigurationException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public ZipServiceConfigurationException(String message, Throwable cause,
			int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public ZipServiceConfigurationException(Throwable cause,
			int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param underlyingIRODSExceptionCode
	 */
	public ZipServiceConfigurationException(String message,
			int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
