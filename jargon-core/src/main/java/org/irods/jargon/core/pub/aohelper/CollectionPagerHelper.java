/**
 * 
 */
package org.irods.jargon.core.pub.aohelper;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.query.PagingAwareCollectionListingDescriptor;
import org.irods.jargon.core.query.PagingChunk;

/**
 * Helper functions for handling collection paging listings
 * 
 * @author Mike Conway - DICE
 *
 */
public class CollectionPagerHelper {

	/**
	 * Compute the paging chunks listing that can be put into the listing
	 * descriptor. These chunks represent pages of data for navigation.
	 * 
	 * @param pagingAwareCollectionListingDescriptor
	 *            {@link PagingAwareCollectionListingDescriptor} that will be
	 *            used to compute the chunk list. This method will not touch
	 *            that object's data
	 * @param maxChunks
	 *            <code>int</code> with the maximum number of chunks to pass
	 *            back. The current 'chunk' will be at the center, and some
	 *            number of pages before and after will be returned. A zero
	 *            means return all.
	 * @return <code>List</code> of {@link PagingChunk} with the computed paging
	 *         chunks that are suitable for display.
	 */
	public static List<PagingChunk> computePagingChunks(
			final PagingAwareCollectionListingDescriptor pagingAwareCollectionListingDescriptor, final int maxChunks) {
		List<PagingChunk> pagingChunks = new ArrayList<>();

		// are there any records?

		if (!pagingAwareCollectionListingDescriptor.hasAnyDataToShow()) {
			return pagingChunks;
		}

		int totalSize = pagingAwareCollectionListingDescriptor.computeAbsoluteTotalSize();

		return pagingChunks;

	}

}
