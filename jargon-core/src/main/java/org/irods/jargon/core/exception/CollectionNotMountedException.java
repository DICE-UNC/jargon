/**
 * 
 */
package org.irods.jargon.core.exception;

/**
 * eqivalent to SYS_COLL_NOT_MOUNTED_ERR	-74000
 * 
 * The given collection is not mounted
 * 
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class CollectionNotMountedException extends JargonException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6576019768482482164L;

	public CollectionNotMountedException(String message,
			int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

	public CollectionNotMountedException(String message, Throwable cause,
			int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	public CollectionNotMountedException(String message, Throwable cause) {
		super(message, cause);
	}

	public CollectionNotMountedException(String message) {
		super(message);
	}

	public CollectionNotMountedException(Throwable cause,
			int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public CollectionNotMountedException(Throwable cause) {
		super(cause);
	}

	

}
