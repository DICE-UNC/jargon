/**
 * 
 */
package org.irods.jargon.usertagging.domain;

import java.util.Collections;
import java.util.List;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;

/**
 * Represents a search on a set of tags, and includes both the originating
 * query, and the result values.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class TagQuerySearchResult {

	private final String searchTags;
	private final List<CollectionAndDataObjectListingEntry> queryResultEntries;

	/**
	 * Static initializer.
	 * 
	 * @param searchTags
	 *            <code>String</code> with the free tags used to search.
	 * @param queryResultEntries
	 *            <code>List</code> of
	 *            <code>CollectionAndDataObjectListingEntry</code> that has the
	 *            query results.
	 * @return instance of <code>TagQuerySearchResult</code>
	 * @throws JargonException
	 */
	public static TagQuerySearchResult instance(final String searchTags,
			final List<CollectionAndDataObjectListingEntry> queryResultEntries)
			throws JargonException {
		return new TagQuerySearchResult(searchTags, queryResultEntries);
	}

	/**
	 * Private constructor.
	 * 
	 * @param searchTags
	 *            <code>String</code> with the free tags used to search.
	 * @param queryResultEntries
	 *            <code>List</code> of
	 *            <code>CollectionAndDataObjectListingEntry</code> that has the
	 *            query results.
	 * @throws JargonException
	 */
	private TagQuerySearchResult(final String searchTags,
			final List<CollectionAndDataObjectListingEntry> queryResultEntries)
			throws JargonException {

		if (searchTags == null || searchTags.isEmpty()) {
			throw new JargonException(
					"null or empty searchTags, at least one search tag must be entered");
		}

		if (queryResultEntries == null) {
			throw new JargonException("null queryResultEntries");
		}

		this.searchTags = searchTags;
		this.queryResultEntries = Collections
				.unmodifiableList(queryResultEntries);

	}

	/**
	 * Get the tags used to generate the search.
	 * 
	 * @return <code>String</code> with the free-form set of tags used to search
	 */
	public String getSearchTags() {
		return searchTags;
	}

	/**
	 * Get the objects that are in response to the query.
	 * 
	 * @return <code>List</code> of
	 *         <code>CollectionAndDataObjectListingEntry</code>
	 */
	public List<CollectionAndDataObjectListingEntry> getQueryResultEntries() {
		return queryResultEntries;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("TagQuerySearchResult");
		sb.append("\n   searchTags:");
		sb.append(searchTags);
		sb.append("\n   queryResultEntries:");
		sb.append(queryResultEntries);
		return sb.toString();
	}

	@Override
	public boolean equals(final Object obj) {

		if (!(obj instanceof TagQuerySearchResult)) {
			return false;
		}

		TagQuerySearchResult otherSearchResult = (TagQuerySearchResult) obj;

		return (searchTags.equals(otherSearchResult.getSearchTags()) && queryResultEntries == otherSearchResult
				.getQueryResultEntries());
	}

	@Override
	public int hashCode() {
		return searchTags.hashCode() + queryResultEntries.hashCode();
	}

}
