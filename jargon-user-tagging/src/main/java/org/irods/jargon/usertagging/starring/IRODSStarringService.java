package org.irods.jargon.usertagging.starring;

import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.usertagging.domain.IRODSStarredFileOrCollection;

/**
 * Interface describing a service for managing 'starred' or favorite files or
 * folders
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface IRODSStarringService {

	/**
	 * Find (if it exists) the starred or favorite status and description for a
	 * file or folder. <code>null</code> will be returned if the given absolute
	 * path is not 'starred' in iRODS.
	 * 
	 * @param irodsAbsolutePath
	 *            <code>String</code> with the absolute path to the iRODS file
	 *            or collection
	 * @return {@link IRODSStarredFileOrCollection} with information for this
	 *         user (bsed on the logged in user, or <code>null</code> if no
	 *         starring is found
	 * @throws FileNotFoundException
	 *             if no file or collection is at the absolute path
	 * @throws JargonException
	 */
	IRODSStarredFileOrCollection findStarredForAbsolutePath(
			String irodsAbsolutePath) throws FileNotFoundException,
			JargonException;

	/**
	 * Annotate a file or collection as 'starred' or a favorite folder.  This is implemented as an iRODS AVU.
	 * The AVU is owned by the user who is logged in, and includes a free text description.
	 * <p/>
	 * Note that this method will either add or update, based on previous data.
	 * 
	 * @param irodsAbsolutePath  <code>String</code> with the absolute path to the iRODS file
	 *            or collection
	 * @param description <code>String</code> with an optional free text description.  Note that this should be set to blank
	 * if not used.
	 * @throws FileNotFoundException if the irods file or collection at the absolute path does not exist
	 * @throws JargonException
	 */
	void starFileOrCollection(final String irodsAbsolutePath, final String description)
			throws FileNotFoundException, JargonException;

	/**
	 * Un-star a file or collection.  Note that if the given file or collection is not starred, it treats it as successful.
	 * @param irodsAbsolutePath  <code>String</code> with the absolute path to the iRODS file
	 *            or collection
	 * @throws FileNotFoundException  if no file or collection is at the absolute path
	 * @throws JargonException
	 */
	void unstarFileOrCollection(final String irodsAbsolutePath)
			throws FileNotFoundException, JargonException;

}