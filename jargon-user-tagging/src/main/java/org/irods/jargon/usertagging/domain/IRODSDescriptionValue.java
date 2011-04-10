/**
 * 
 */
package org.irods.jargon.usertagging.domain;

import java.io.Serializable;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.usertagging.UserTaggingConstants;

/**
 * Represents a user comment, or description for iRODS data.   This is an immutable object, and is validated when constructed
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public final class IRODSDescriptionValue implements Serializable {
	
	private static final long serialVersionUID = -2793016247394049440L;
	private  final String description;
	private final  String tagUser;
	
	/**
	 * Construct a comment tag with the given value
	 * @param description <code>String</code> with the comment  value for the data object.
	 * @param user <code>String</code> with the user name that owns the comment.
	 * @throws JargonException
	 */
	public IRODSDescriptionValue(final String description, final String tagUser) throws JargonException {
		
		if (description == null || description.isEmpty()) {
			throw new JargonException("null or empty description");
		}
		
		if (tagUser == null || tagUser.isEmpty()) {
			throw new JargonException("null or empty tagUser");
		}
		
		this.description = description;
		this.tagUser = tagUser;
		
	}
	
	/**
	 * Special constructor that can interpret AVU data as a description.  Note that the description is in the AVU attribute, and the user is in the AVU Value
	 * as <code>user[username]
	 * @param metadataAndDomainData <code>MetaDataAndDomainData</code> object from Jargon that contains information from a raw AVU triple.
	 * @throws JargonException
	 */
	public IRODSDescriptionValue(final MetaDataAndDomainData metadataAndDomainData) throws JargonException {
		
		if (metadataAndDomainData == null) {
			throw new JargonException("null metadataAndDomainData");
		}
		
		if (!(metadataAndDomainData.getAvuUnit().equals(UserTaggingConstants.DESCRIPTION_AVU_UNIT))) {
			throw new JargonException("the given metadata value is not a user tag, based on the value in unit");
		}
		
		if (metadataAndDomainData.getAvuValue() == null || metadataAndDomainData.getAvuValue().isEmpty()) {
			throw new JargonException("no user supplied");
		}
		
		this.description = metadataAndDomainData.getAvuAttribute();
		this.tagUser = metadataAndDomainData.getAvuValue();
		
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("IRODSTagValue");
		sb.append("\n   description:");
		sb.append(description);
		sb.append("\n   tagUser:");
		sb.append(tagUser);
		return sb.toString();
	}

	public String getDescription() {
		return description;
	}

	public String getTagUser() {
		return tagUser;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof IRODSDescriptionValue)) {
			return false;
		}
		
		IRODSDescriptionValue other = (IRODSDescriptionValue) obj;
		return (other.getDescription().equals(getDescription()) && other.getTagUser().equals(getTagUser()));
	
	}

	@Override
	public int hashCode() {
		return getDescription().hashCode() + getTagUser().hashCode();
	}


}
