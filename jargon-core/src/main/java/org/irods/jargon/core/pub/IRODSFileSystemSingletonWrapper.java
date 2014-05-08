/**
 * 
 */
package org.irods.jargon.core.pub;

import java.io.Serializable;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;

/**
 * Handy wrapper for {@link IRODSFileSystem} that makes it a singleton. The base
 * <code>IRODSFileSystem</code> doesn't impose this singleton pattern, so this
 * separate bit of code does that for you if you think it fits your situation.
 * 
 * @author Mike Conway - DICE
 * 
 */
public final class IRODSFileSystemSingletonWrapper implements Serializable {

	private static final long serialVersionUID = 7257370842026440344L;
	private static volatile IRODSFileSystem irodsFileSystem;

	protected IRODSFileSystemSingletonWrapper() {
	};

	/**
	 * Obtain a singleton instance
	 * 
	 * @return {@link IRODSFileSystem}
	 * @throws JargonException
	 */
	public static synchronized IRODSFileSystem instance() {
		if (irodsFileSystem == null) {
			try {
				irodsFileSystem = IRODSFileSystem.instance();
			} catch (JargonException e) {
				throw new JargonRuntimeException("unable to create instance");
			}
		}

		return irodsFileSystem;
	}

	protected Object readResolve() {
		return instance();

	}

}
