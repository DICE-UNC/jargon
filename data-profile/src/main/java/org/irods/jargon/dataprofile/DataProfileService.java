package org.irods.jargon.dataprofile;

import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;

public interface DataProfileService {

	/**
	 * Retrieve a summary profile of a data object or collection
	 *
	 * @param irodsAbsolutePath
	 *            <code>String</code> with the absolute path to an iRODS file or
	 *            collection
	 * @return {@link DataProfile} with summary data
	 * @throws FileNotFoundException
	 * @throws JargonException
	 */
	public abstract DataProfile retrieveDataProfile(String irodsAbsolutePath)
			throws FileNotFoundException, JargonException;

	public abstract DataTypeResolutionService getDataTypeResolutionService();

}