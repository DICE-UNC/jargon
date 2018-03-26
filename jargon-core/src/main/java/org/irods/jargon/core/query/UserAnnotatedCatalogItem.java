package org.irods.jargon.core.query;

import java.io.Serializable;
import java.util.Date;

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
	 * optional (can be zero) data size
	 */
	private final long dataSize;
	/**
	 * optional (can be null) created date
	 */
	private final Date createdAt;
	/**
	 * optional (can be null) modified date
	 */
	private final Date modifiedAt;

	/**
	 * constructor that ignores created, modified dates and data size
	 * 
	 * @param metadataDomain
	 *            {@link MetadataDomain} enum value that describes the iCAT
	 *            domain the annotation is for
	 * @param domainUniqueName
	 *            {@code String} with the unique identifier of the domain.
	 *            For files and collections this is the iRODS absolute path
	 * @param userName
	 *            {@code String} with the user name for which the item is
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
		this.dataSize = 0L;
		this.createdAt = null;
		this.modifiedAt = null;
	}

	/**
	 * All fields constructor
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
	 * @param dataSize
	 *            <code>long</code> with a data size (if applies)
	 * @param createdAt
	 *            {@link Date} created, can be <code>null</code>
	 * @param modifiedAt
	 *            {@link Date} created, can be <code>null</code>
	 */
	public UserAnnotatedCatalogItem(MetadataDomain metadataDomain,
			String domainUniqueName, String userName, long dataSize,
			Date createdAt, Date modifiedAt) {
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
		this.dataSize = dataSize;
		this.createdAt = createdAt;
		this.modifiedAt = modifiedAt;
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

	/**
	 * @return the serialversionuid
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	/**
	 * @return the dataSize
	 */
	public long getDataSize() {
		return dataSize;
	}

	/**
	 * @return the createdAt
	 */
	public Date getCreatedAt() {
		return createdAt;
	}

	/**
	 * @return the modifiedAt
	 */
	public Date getModifiedAt() {
		return modifiedAt;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("UserAnnotatedCatalogItem [");
		if (metadataDomain != null) {
			builder.append("metadataDomain=");
			builder.append(metadataDomain);
			builder.append(", ");
		}
		if (domainUniqueName != null) {
			builder.append("domainUniqueName=");
			builder.append(domainUniqueName);
			builder.append(", ");
		}
		if (userName != null) {
			builder.append("userName=");
			builder.append(userName);
			builder.append(", ");
		}
		builder.append("dataSize=");
		builder.append(dataSize);
		builder.append(", ");
		if (createdAt != null) {
			builder.append("createdAt=");
			builder.append(createdAt);
			builder.append(", ");
		}
		if (modifiedAt != null) {
			builder.append("modifiedAt=");
			builder.append(modifiedAt);
		}
		builder.append("]");
		return builder.toString();
	}

}