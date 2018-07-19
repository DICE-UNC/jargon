package org.irods.jargon.testutils;

import org.irods.jargon.core.exception.JargonException;

public class TestingUtilsException extends JargonException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6109846366799530699L;

	public TestingUtilsException(String message) {
		super(message);
	}

	public TestingUtilsException(String message, Throwable cause) {
		super(message, cause);
	}

	public TestingUtilsException(Throwable cause) {
		super(cause);
	}

	public TestingUtilsException(String message, Throwable cause, int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	public TestingUtilsException(Throwable cause, int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public TestingUtilsException(String message, int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
