/**
 * 
 */
package org.irods.jargon.core.query;

import org.irods.jargon.core.exception.JargonException;
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
	private final String avuAttribute;
	private final String avuValue;
	private final String avuUnit;

	/**
	 * Create an immutable instance
	 * 
	 * @param metadataDomain
	 *            <code>MetadataDomain</code> enum value that indicates the
	 *            domain (RESOURCE, USER, etc) that this metadata applies to
	 * @param domainObjectId
	 *            <code>String</code> represents the unique id value for this
	 *            domain object in ICAT
	 * @param domainObjectUniqueName
	 *            <code>String</code> with the unique name in ICAT, such as
	 *            absolute path or resource name
	 * @param avuAttribute
	 *            <code>String</code> with the AVU attribute
	 * @param avuValue
	 *            <code>String</code> with the AVU value
	 * @param avuUnit
	 *            <code>String</code> with the AVU units
	 * @return <code>MetaDataAndDomainData</code> representing an AVU for the
	 *         given domain
	 * @throws JargonException
	 */
	public static MetaDataAndDomainData instance(
			final MetadataDomain metadataDomain, final String domainObjectId,
			final String domainObjectUniqueName, final String avuAttribute,
			final String avuValue, final String avuUnit) throws JargonException {
		return new MetaDataAndDomainData(metadataDomain, domainObjectId,
				domainObjectUniqueName, avuAttribute, avuValue, avuUnit);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("MetaDataAndDomainData:");
		sb.append("\n   domain:");
		sb.append(metadataDomain);
		sb.append("\n   id:");
		sb.append(domainObjectId);
		sb.append("\n   domainObjectUniqueName:");
		sb.append(domainObjectUniqueName);
		sb.append("\n   avuAttribute:");
		sb.append(avuAttribute);
		sb.append("\n   avuValue:");
		sb.append(avuValue);
		sb.append("\n   avuUnit:");
		sb.append(avuUnit);
		return sb.toString();
	}

	private MetaDataAndDomainData(final MetadataDomain metadataDomain,
			final String domainObjectId, final String domainObjectUniqueName,
			final String avuAttribute, final String avuValue,
			final String avuUnit) throws JargonException {

		if (metadataDomain == null) {
			throw new JargonException("metadataDomain is null");
		}

		if (domainObjectId == null || domainObjectId.isEmpty()) {
			throw new JargonException("domain object id is null or empty");
		}

		if (domainObjectUniqueName == null || domainObjectUniqueName.isEmpty()) {
			throw new JargonException(
					"domain object unique name is null or empty");
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
		this.avuAttribute = avuAttribute;
		this.avuValue = avuValue;
		this.avuUnit = avuUnit;
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

}
