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
	 * @param irodsAbsolutePath <code>String</code> with the iRODS absolute path of
	 *                          the parent collection
	 * @return {@link PagingAwareCollectionListing} with the list of children and a
	 *         block of data about position and paging
	 * @throws FileNotFoundException {@code FileNotFoundException}
	 * @throws NoMoreDataException   {@code NoMoreDataException}
	 * @throws JargonException       {@code JargonException}
	 */
	public abstract PagingAwareCollectionListing retrieveFirstPageUnderParent(String irodsAbsolutePath)
			throws FileNotFoundException, NoMoreDataException, JargonException;

	/**
	 * Retrieve the next page of data given minimal coordinates describing the
	 * paging state
	 * 
	 * @param irodsAbsolutePath <code>String</code> with the iRODS absolute path of
	 *                          the parent collection
	 * @param inCollections     {@code boolean} that will be {@code true} if the
	 *                          current page is displaying collections. A value of
	 *                          {@false} indicates that data objects are currently
	 *                          displayed. If the current view is not 'split' into
	 *                          files and collections, the parameter is ignored.
	 * @param offset            {@code int} with the next offset. This will be an
	 *                          offset within the current type (file or collection)
	 *                          in a split view, or the offset into the entire
	 *                          result set otherwise. Note that this offset can be
	 *                          used by referring to the current count in the
	 *                          {@link PagingAwareCollectionListing} from a previous
	 *                          call
	 * @param pageSize          {@code int} with the desired page size. Note this is
	 *                          currently not supported and will be ignored, but in
	 *                          later iterations will have an effect}
	 * @return {@link PagingAwareCollectionListing}
	 * @throws FileNotFoundException {@link FileNotFoundException}
	 * @throws NoMoreDataException   {@link NoMoreDataException}
	 * @throws JargonException       {@link JargonException}
	 */
	PagingAwareCollectionListing retrieveNextPage(String irodsAbsolutePath, boolean inCollections, int offset,
			long pageSize) throws FileNotFoundException, NoMoreDataException, JargonException;

}