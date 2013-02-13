package org.irods.jargon.usertagging.domain;

import java.io.Serializable;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.query.MetaDataAndDomainData.MetadataDomain;

/**
 * Represents a user 'star' of a file or collection. This is like a favorite
 * folder, and includes a free text description of the folder
 * 
 * This object is a bridge between the typical client presentation of a free tag
 * area, and the underlying one-AVU-per-tag structure of the data.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public final class IRODSStarredFileOrCollection extends
		UserAnnotatedCatalogItem implements Serializable {

	private static final long serialVersionUID = -6429439523354271218L;
	private final String description;

	public String getDescription() {
		return description;
	}

	/**
	 * Constructor describes the user and
	 * object that is being starred, and providing a description.
	 * 
	 * @param metadataDomain
	 *            <code>MetaDataAndDomainData.MetadataDomain</code> enum value
	 *            that identifies the domain object type that is tagged (e.g.
	 *            DataObject, Collection)
	 * @param domainUniqueName
	 *            <code>String</code> with the unque name for the tagged data
	 *            object (e.g. iRODS absolute path for a data object or
	 *            collection).
	 * @param description
	 *            <code>String</code> with a description of the favorite.
	 * @param userName
	 *            <code>String</code> with the user name who is associated with
	 *            the tags.
	 * @throws JargonException
	 */
	public IRODSStarredFileOrCollection(final MetadataDomain metadataDomain,
			final String domainUniqueName, final String description,
			final String userName) throws JargonException {

		super(metadataDomain, domainUniqueName, userName);

		if (description == null) {
			throw new JargonException("null spaceDelimitedTagsForDomain");
		}

		this.description = description.trim();

	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("IRODSStarredFileOrCollection:");
		sb.append("\n   metadataDomain:");
		sb.append(getMetadataDomain());
		sb.append("\n   domainUniqueName:");
		sb.append(getDomainUniqueName());
		sb.append("\n   description:");
		sb.append(description);
		sb.append("\n   userName:");
		sb.append(getUserName());
		return sb.toString();
	}

}
