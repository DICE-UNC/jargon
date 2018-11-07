/**
 *
 */
package org.irods.jargon.zipservice.api.exception;

/**
 * @author Mike Conway error in the configuration of iRODS or the zip service
 */
public class ZipServiceConfigurationException extends ZipServiceException {

	private static final long serialVersionUID = -2164051501532502589L;

	public ZipServiceConfigurationException(final String message) {
		super(message);
	}

	public ZipServiceConfigurationException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public ZipServiceConfigurationException(final Throwable cause) {
		super(cause);
	}

	public ZipServiceConfigurationException(final String message, final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	public ZipServiceConfigurationException(final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public ZipServiceConfigurationException(final String message, final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
