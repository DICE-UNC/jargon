package org.irods.jargon.core.query;

import java.io.Serializable;

import org.irods.jargon.core.pub.domain.IRODSDomainObject;
import org.irods.jargon.core.query.MetaDataAndDomainData.MetadataDomain;

/**
 * Abstract superclass to a user annotation on an iRODS catalog domain item
 * (like a tag, or a starred folder).
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public abstract class UserAnnotatedCatalogItem extends IRODSDomainObject
		implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 3473243677966963376L;
	private final MetadataDomain metadataDomain;
	private final String domainUniqueName;
	private final String userName;

	/**
	 * Default constructor
	 *
	 * @param metadataDomain
	 *            {@link MetadataDomain} enum value that describes the iCAT
	 *            domain the annotation is for
	 * @param domainUniqueName
	 *            <code>String</code> with the unique identifier of the domain.
	 *            For files and collections this is the iRODS absolute path
	 * @param userName
	 *            <code>String</code> with the user name for which the item is
	 *            annotated (these annotations are per user)
	 */
	public UserAnnotatedCatalogItem(final MetadataDomain metadataDomain,
			final String domainUniqueName, final String userName) {
		super();

		if (metadataDomain == null) {
			throw new IllegalArgumentException("null metadataDomain");
		}

		if (domainUniqueName == null || domainUniqueName.isEmpty()) {
			throw new IllegalArgumentException("null or empty domainUniqueName");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		this.metadataDomain = metadataDomain;
		this.domainUniqueName = domainUniqueName;
		this.userName = userName;
	}

	public MetadataDomain getMetadataDomain() {
		return metadataDomain;
	}

	public String getDomainUniqueName() {
		return domainUniqueName;
	}

	public String getUserName() {
		return userName;
	}

}