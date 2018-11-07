package org.irods.jargon.core.pub;

import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.NoMoreDataException;
import org.irods.jargon.core.query.PagingAwareCollectionListing;
import org.irods.jargon.core.query.PagingAwareCollectionListingDescriptor;

/**
 * Improved tools for managing paging through children collections and data
 * objects underneath an iRODS path. iRODS keeps collections and data objects in
 * separate places, so some work needs to be done to manage paging back and
 * forth across this boundary. Mostly this service relies on a POJO descriptor
 * of the current state of a listing which is kept on the client and passed back
 * to this method on subsequent paging requests. In this way, a client can just
 * say 'page forward' and delegate all of the head-scratching about how to
 * accomplish that to this service, which you will love, because that sucks to
 * manage mostly.
 *
 * @author Mike Conway - DICE
 *
 */
public interface CollectionPagerAO {

	/**
	 * Start a listing under a path, getting the first page of results.
	 * <p>
	 * Inside the {@link PagingAwareCollectionListing} is a POJO containing
	 * information about the status of the listing (more collections or data objects
	 * to page, counts, etc). This object may be retained by the client caller and
	 * submitted back to this object in other methods to page around given
	 * information on the current position. This
	 * {@link PagingAwareCollectionListingDescriptor} can be extracted from the
	 * listing (to save memory storing the whole listing in some state keeping
	 * mechanism) to help manage paging.
	 *
	 *
	 *
	 * @param irodsAbsolutePath
	 *            <code>String</code> with the iRODS absolute path of the parent
	 *            collection
	 * @return {@link PagingAwareCollectionListing} with the list of children and a
	 *         block of data about position and paging
	 * @throws FileNotFoundException
	 *             {@code FileNotFoundException}
	 * @throws NoMoreDataException
	 *             {@code NoMoreDataException}
	 * @throws JargonException
	 *             {@code JargonException}
	 */
	public abstract PagingAwareCollectionListing retrieveFirstPageUnderParent(String irodsAbsolutePath)
			throws FileNotFoundException, NoMoreDataException, JargonException;

	/**
	 * Given my current location, as depicted in the
	 * <code>PagingAwareCollectionListingDescriptor</code>, page forwards. This will
	 * give the next page of whatever (collections, data objects) are available, and
	 * if need be page across the Collections/Data Objects boundary.
	 *
	 * @param lastListingDescriptor
	 *            {@link PagingAwareCollectionListingDescriptor} from the previous
	 *            page, as retained by the client. This keeps state of the position
	 *            in the collection
	 * @return {@link PagingAwareCollectionListing} with the next page of data
	 * @throws FileNotFoundException
	 *             {@code FileNotFoundException}
	 * @throws NoMoreDataException
	 *             {@code NoMoreDataException}
	 * @throws JargonException
	 *             {@code JargonException}
	 */
	public abstract PagingAwareCollectionListing retrieveNextPage(
			final PagingAwareCollectionListingDescriptor lastListingDescriptor)
			throws FileNotFoundException, NoMoreDataException, JargonException;

}