package org.irods.jargon.datautils.uploads;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.io.IRODSFile;

/**
 * Service to help manage an 'uploads' directory, used by convention in multiple
 * interfaces as a generic location to upload data.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface UploadsService {

	public static final String UPLOADS_DIR_DEFAULT_NAME = "uploads";

	/**
	 * Retrieve an 'uploads' directory under the user home directory, creating
	 * it if it does not exist yet
	 * 
	 * @return {@link IRODSFile} which represents the uploads top level
	 *         direcrory under the user home directory
	 * @throws JargonException
	 */
	IRODSFile getUploadsDirectory() throws JargonException;

	public abstract void deleteUploadsDirectory() throws JargonException;

}