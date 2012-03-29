package org.irods.jargon.datautils.sharing;

import java.util.List;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;

/**
 * Interface for a service to view and query iRODS collections based on how they
 * are shared, for example, listings can be created of files owned by one user
 * shared with another defined user.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface SharedFilesAndCollectionsSearchAndListService {

	/**
	 * This method searches under a given parent path, and shows files owned by
	 * one user and shared with another user. The behavior is variable in the
	 * sense that owner name, shared with user name, or both may be selected.
	 * <p/>
	 * Optional fields may be set to blank or <code>null</code> as indicated.
	 * <ul>
	 * <li>If <code>ownerName</code> is specified, then files owned by the given
	 * owner will be retrieved.</li>
	 * <li>if <code>sharedWithName</code> is specified, then files where the
	 * given name have an ACL permission are shown.</li>
	 * <li>if both are entered, then files owned by one user and shared with
	 * another user are returned.</li>
	 * <li>if neither are entered, an <code>IllegalArgumentException</code> will
	 * be thrown.</li>
	 * </ul>
	 * Note that the <code>searchStartAbsolutePath</code> represents a parent
	 * path under which the files will be listed. If not specified, root will be
	 * the default.
	 * 
	 * @param searchStartAbsolutePath
	 *            <code>String</code> with the absolute path to a parent
	 *            collection under which files may be listed. Optional.
	 * @param ownerName
	 *            <code>String</code> with the name of the desired owner of the
	 *            file. Optional.
	 * @param sharedWithName
	 *            <code>String</code> with the name of the user to whom the file
	 *            is shared.
	 * @param partialStartIndex
	 *            <code>int</code> with the offset into the results for which
	 *            reults will be returned. Set to 0 if no offset is required.
	 * @return <code>List</code> of {@link CollectionAndDataObjectListingEntry}
	 *         containing the query results.
	 * @throws JargonException
	 */
	List<CollectionAndDataObjectListingEntry> listDataObjectsSharedWithUserByOwner(
			String searchStartAbsolutePath, final String ownerName,
			final String sharedWithName, int partialStartIndex)
			throws JargonException;

}