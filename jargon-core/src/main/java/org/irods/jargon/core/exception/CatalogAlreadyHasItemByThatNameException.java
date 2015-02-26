package org.irods.jargon.core.exception;

import org.irods.jargon.core.protovalues.ErrorEnum;

/**
 * The given item is already in the catalog
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class CatalogAlreadyHasItemByThatNameException extends JargonException {
	/**
	 *
	 */
	private static final long serialVersionUID = 8297408727971695894L;
	private static final int ERROR_CODE = ErrorEnum.CATALOG_ALREADY_HAS_ITEM_BY_THAT_NAME
			.getInt();

	public CatalogAlreadyHasItemByThatNameException(final String message,
			final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

	public CatalogAlreadyHasItemByThatNameException(final String message,
			final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	public CatalogAlreadyHasItemByThatNameException(final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public CatalogAlreadyHasItemByThatNameException(final String message) {
		super(message, ERROR_CODE);
	}

	public CatalogAlreadyHasItemByThatNameException(final String message,
			final Throwable cause) {
		super(message, cause, ERROR_CODE);
	}

	public CatalogAlreadyHasItemByThatNameException(final Throwable cause) {
		super(cause, ERROR_CODE);
	}
}
