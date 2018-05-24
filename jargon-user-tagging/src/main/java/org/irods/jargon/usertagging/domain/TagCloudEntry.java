/**
 *
 */
package org.irods.jargon.usertagging.domain;

import java.io.Serializable;

import org.irods.jargon.core.exception.JargonException;

/**
 * An entry that decribes a tag in a user tag cloud. This object identifies the
 * tag by name and can hold counts of occurrences for data objects and for
 * collections.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public final class TagCloudEntry implements Serializable {

	private static final long serialVersionUID = 3201265141915523181L;
	private final IRODSTagValue irodsTagValue;
	private int countOfFiles = 0;
	private int countOfCollections = 0;

	/**
	 * Private constructor.
	 *
	 * @param irodsTagValue
	 *            {@link IRODSTagValue} with information on the tag
	 * @param countOfFiles
	 *            {@code int} with a count of the number of occurrences of the tag
	 *            for the given domain in files
	 * @param countOfCollections
	 *            {@code int} with a count of the number of occurrences of the tag
	 *            for the given domain in collections
	 * @throws JargonException
	 *             {@link JargonException}
	 */
	public TagCloudEntry(final IRODSTagValue irodsTagValue, final int countOfFiles, final int countOfCollections)
			throws JargonException {

		if (irodsTagValue == null) {
			throw new JargonException("null irodsTagValue");
		}

		if (countOfFiles < 0) {
			throw new JargonException("count of files cannot be less than zero");
		}

		if (countOfCollections < 0) {
			throw new JargonException("count of files cannot be less than zero");
		}

		this.irodsTagValue = irodsTagValue;
		this.countOfFiles = countOfFiles;
		this.countOfCollections = countOfCollections;

	}

	/**
	 * Get the value of the iRODS tag for this entry
	 *
	 * @return {@link IRODSTagValue} for the given cloud entry.
	 */
	public IRODSTagValue getIrodsTagValue() {
		return irodsTagValue;
	}

	/**
	 * Get the count of tag occurrences for files (data objects)
	 *
	 * @return {@code int}
	 */
	public int getCountOfFiles() {
		return countOfFiles;
	}

	/**
	 * Get the count of tag occurrences for collections (directories)
	 *
	 * @return {@code int}
	 */
	public int getCountOfCollections() {
		return countOfCollections;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("TagCloudEntry");
		sb.append("\n   irodsTagValue:");
		sb.append(irodsTagValue);
		sb.append("\n   countOfFiles:");
		sb.append(countOfFiles);
		sb.append("\n   countOfCollections:");
		sb.append(countOfCollections);
		return sb.toString();
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof TagCloudEntry)) {
			return false;
		}

		TagCloudEntry other = (TagCloudEntry) obj;
		return (other.getIrodsTagValue() == getIrodsTagValue());

	}

	@Override
	public int hashCode() {
		return getIrodsTagValue().hashCode();
	}

	public void setCountOfFiles(final int countOfFiles) {
		this.countOfFiles = countOfFiles;
	}

	public void setCountOfCollections(final int countOfCollections) {
		this.countOfCollections = countOfCollections;
	}

}
