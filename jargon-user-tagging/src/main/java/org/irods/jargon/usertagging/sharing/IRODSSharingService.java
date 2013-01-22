package org.irods.jargon.usertagging.sharing;

import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.usertagging.domain.IRODSSharedFileOrCollection;

/**
 * Service interface to create and manage shares.
 * Like the star and tagging facility, a share is a special metadata tag on a
 * Collection or Data Object, naming that item as shared. In the process of
 * declaring the share, the proper ACL settings are done.
 * <p/>
 * Sharing using a special tag at the 'root' of the share avoids representing
 * every file or collection in a deeply nested shared collection as 'shared', as
 * it would be based purely on the ACL settings. As a first class object, a
 * share can have an alias name, and is considered one unit.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface IRODSSharingService {

	/**
	 * Create a new share.  This will tag the top level as a shared collection and data object, and set requisite AVUs for the provided
	 * set of users.  Note that collections will have recursive set of permissions, as well as inheritance.
	 * <p/>
	 * Note that the share is only settable as originating from the file or collection owner
	 * 
	 * @param irodsSharedFileOrCollection {@link IRODSSharedFileOrCollection} representing the share
	 * @throws FileNotFoundException
	 * @throws JargonException
	 */
	void createShare(IRODSSharedFileOrCollection irodsSharedFileOrCollection)
			throws FileNotFoundException, JargonException;

	/**
	 * Given an absolute path to an iRODS file or collection, return the <code>IRODSSahredFileOrCollection</code> that may exist. Note that <code>null</code> is
	 * returned if no such share exists, and a <code>FileNotFoundException</code> is returned if the absolute path does not exist.
	 * @param irodsAbsolutePath <code>String</code> with a valid iRODS absolute path to a file or collection
	 * @return {@link IRODSSharedFileOrCollection} or <code>null</code>
	 * @throws FileNotFoundException if the target absolute path does not exist in iRODS
	 * @throws JargonException
	 */
	IRODSSharedFileOrCollection findShareByAbsolutePath(String irodsAbsolutePath)
			throws FileNotFoundException, JargonException;

}
