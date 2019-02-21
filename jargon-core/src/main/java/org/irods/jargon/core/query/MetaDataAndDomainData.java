/**
 *
 */
package org.irods.jargon.core.query;

import java.util.Date;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.domain.IRODSDomainObject;

/**
 * Contains immutable metadata values and values that identify the domain
 * (Resource, Collection, etc) that is associated with the metadata values
 *
 * @author Mike Conway - DICE (www.irods.org)
 */
public final class MetaDataAndDomainData extends IRODSDomainObject {

	public enum MetadataDomain {
		RESOURCE, USER, DATA, COLLECTION
	}

	private final MetadataDomain metadataDomain;
	private final String domainObjectId;
	private final String domainObjectUniqueName;
	private final int avuId;
	/**
	 * Data size (if applies to domain)
	 */
	private final long size;
	/**
	 * Created at date (if applies)
	 */
	private final Date createdAt;
	/**
	 * Modified date (if applies)
	 */
	private final Date modifiedAt;
	private final String avuAttribute;
	private final String avuValue;
	private final String avuUnit;

	/**
	 * Create an immutable instance
	 *
	 * @param metadataDomain
	 *            {@code MetadataDomain} enum value that indicates the domain
	 *            (RESOURCE, USER, etc) that this metadata applies to
	 * @param domainObjectId
	 *            {@code String} represents the unique id value for this domain
	 *            object in ICAT
	 * @param domainObjectUniqueName
	 *            {@code String} with the unique name in ICAT, such as absolute path
	 *            or resource name
	 * @param avuId
	 *            {@code int} with the AVU id
	 * @param avuAttribute
	 *            {@code String} with the AVU attribute
	 * @param avuValue
	 *            {@code String} with the AVU value
	 * @param avuUnit
	 *            {@code String} with the AVU units
	 * @return {@code MetaDataAndDomainData} representing an AVU for the given
	 *         domain
	 * @throws JargonException
	 *             for iRODS error
	 */
	public static MetaDataAndDomainData instance(final MetadataDomain metadataDomain, final String domainObjectId,
			final String domainObjectUniqueName, final int avuId, final String avuAttribute, final String avuValue,
			final String avuUnit) throws JargonException {
		return new MetaDataAndDomainData(metadataDomain, domainObjectId, domainObjectUniqueName, 0L, null, null, avuId,
				avuAttribute, avuValue, avuUnit);
	}

	/**
	 * Create an immutable instance
	 *
	 * @param metadataDomain
	 *            {@code MetadataDomain} enum value that indicates the domain
	 *            (RESOURCE, USER, etc) that this metadata applies to
	 * @param domainObjectId
	 *            {@code String} represents the unique id value for this domain
	 *            object in ICAT
	 * @param domainObjectUniqueName
	 *            {@code String} with the unique name in ICAT, such as absolute path
	 *            or resource name
	 * @param size
	 *            {@code long} with an optional data size
	 * @param createdAt
	 *            {@code Date} created at, can be {@code null}
	 * @param modifiedAt
	 *            {@code Date} modified, can be {@code null}
	 * @param avuId
	 *            {@code int} with the AVU id
	 * @param avuAttribute
	 *            {@code String} with the AVU attribute
	 * @param avuValue
	 *            {@code String} with the AVU value
	 * @param avuUnit
	 *            {@code String} with the AVU units
	 * @return {@code MetaDataAndDomainData} representing an AVU for the given
	 *         domain
	 * @throws JargonException
	 *             for iRODS error
	 */
	public static MetaDataAndDomainData instance(final MetadataDomain metadataDomain, final String domainObjectId,
			final String domainObjectUniqueName, final long size, final Date createdAt, final Date modifiedAt,
			final int avuId, final String avuAttribute, final String avuValue, final String avuUnit)
			throws JargonException {
		return new MetaDataAndDomainData(metadataDomain, domainObjectId, domainObjectUniqueName, size, createdAt,
				modifiedAt, avuId,

				avuAttribute, avuValue, avuUnit);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MetaDataAndDomainData [");
		if (metadataDomain != null) {
			builder.append("metadataDomain=");
			builder.append(metadataDomain);
			builder.append(", ");
		}
		if (domainObjectId != null) {
			builder.append("domainObjectId=");
			builder.append(domainObjectId);
			builder.append(", ");
		}
		if (domainObjectUniqueName != null) {
			builder.append("domainObjectUniqueName=");
			builder.append(domainObjectUniqueName);
			builder.append(", ");
		}
		builder.append("avuId=");
		builder.append(avuId);
		builder.append(", size=");
		builder.append(size);
		builder.append(", ");
		if (createdAt != null) {
			builder.append("createdAt=");
			builder.append(createdAt);
			builder.append(", ");
		}
		if (modifiedAt != null) {
			builder.append("modifiedAt=");
			builder.append(modifiedAt);
			builder.append(", ");
		}
		if (avuAttribute != null) {
			builder.append("avuAttribute=");
			builder.append(avuAttribute);
			builder.append(", ");
		}
		if (avuValue != null) {
			builder.append("avuValue=");
			builder.append(avuValue);
			builder.append(", ");
		}
		if (avuUnit != null) {
			builder.append("avuUnit=");
			builder.append(avuUnit);
		}
		builder.append("]");
		return builder.toString();
	}

	private MetaDataAndDomainData(final MetadataDomain metadataDomain, final String domainObjectId,
			final String domainObjectUniqueName, final long size, final Date createdAt, final Date modifiedAt,
			final int avuId, final String avuAttribute, final String avuValue, final String avuUnit)
			throws JargonException {

		if (metadataDomain == null) {
			throw new JargonException("metadataDomain is null");
		}

		if (domainObjectId == null || domainObjectId.isEmpty()) {
			throw new JargonException("domain object id is null or empty");
		}

		if (domainObjectUniqueName == null || domainObjectUniqueName.isEmpty()) {
			throw new JargonException("domain object unique name is null or empty");
		}

		if (avuAttribute == null || avuAttribute.isEmpty()) {
			throw new JargonException("avu attribute is null or empty");
		}

		if (avuValue == null) {
			throw new JargonException("avu value is null");
		}

		if (avuUnit == null) {
			throw new JargonException("avu unit is null");
		}

		this.metadataDomain = metadataDomain;
		this.domainObjectId = domainObjectId;
		this.domainObjectUniqueName = domainObjectUniqueName;
		this.avuId = avuId;
		this.avuAttribute = avuAttribute;
		this.avuValue = avuValue;
		this.avuUnit = avuUnit;
		this.size = size;
		this.modifiedAt = modifiedAt;
		this.createdAt = createdAt;
	}

	public String getDomainObjectId() {
		return domainObjectId;
	}

	public String getDomainObjectUniqueName() {
		return domainObjectUniqueName;
	}

	public String getAvuAttribute() {
		return avuAttribute;
	}

	public String getAvuValue() {
		return avuValue;
	}

	public String getAvuUnit() {
		return avuUnit;
	}

	public MetadataDomain getMetadataDomain() {
		return metadataDomain;
	}

	public int getAvuId() {
		return avuId;
	}

	public long getSize() {
		return size;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public Date getModifiedAt() {
		return modifiedAt;
	}

	@Override
	public MetaDataAndDomainData clone() throws CloneNotSupportedException {
		try {
			return new MetaDataAndDomainData(metadataDomain, domainObjectId, domainObjectUniqueName, size, createdAt,
					createdAt, avuId, avuAttribute, avuValue, avuUnit);
		} catch (JargonException e) {
			throw new JargonRuntimeException("exception during clone()", e);
		}

	}

	/**
	 * Convenience method to return this information as an {@link AvuData} object.
	 * 
	 * @return {@link AvuData} equivalent
	 */
	public AvuData asAvu() {
		return AvuData.instance(this.avuAttribute, this.avuValue, this.avuUnit);
	}
}
