package org.irods.jargon.transfer.dao.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Represents a store of kvp configuration properties
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
@Entity
@Table(name = "configuration_property")
public class ConfigurationProperty implements Serializable {

	private static final long serialVersionUID = -8108807996395281600L;

	@Id()
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long id = null;

	/**
	 * iDrop configuration property, stored as a key
	 */
	@Column(name = "propertyKey", unique = true, nullable = false)
	private String propertyKey = "";

	@Column(name = "propertyValue", nullable = false)
	private String propertyValue = "";

	@Column(name = "created_at", nullable = false)
	private Date createdAt;

	@Column(name = "updated_at")
	private Date updatedAt;

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ConfigurationProperty:");
		sb.append("\n   id:");
		sb.append(id);
		sb.append("\n   propertyKey:");
		sb.append(propertyKey);
		sb.append("\n   propertyValue:");
		sb.append(propertyValue);
		sb.append("\n   createdAt:");
		sb.append(createdAt);
		sb.append("\n   updatedAt:");
		sb.append(updatedAt);
		return sb.toString();
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(final Long id) {
		this.id = id;
	}

	/**
	 * @return the propertyKey
	 */
	public String getPropertyKey() {
		return propertyKey;
	}

	/**
	 * @param propertyKey
	 *            the propertyKey to set
	 */
	public void setPropertyKey(final String propertyKey) {
		this.propertyKey = propertyKey;
	}

	/**
	 * @return the propertyValue
	 */
	public String getPropertyValue() {
		return propertyValue;
	}

	/**
	 * access the property value as a boolean
	 * 
	 * @return <code>boolean</code> that represents the cached property
	 */
	public boolean propertyValueAsBoolean() {
		if (propertyValue == null) {
			return false;
		}

		return Boolean.parseBoolean(propertyValue);
	}

	/**
	 * access the property value as an int
	 * 
	 * @return <code>int</code> that represents the cached property. No property
	 *         resolves to a zero
	 */
	public int propertyValueAsInt() {
		if (propertyValue == null) {
			return 0;
		}

		return Integer.parseInt(propertyValue);
	}

	/**
	 * @param propertyValue
	 *            the propertyValue to set
	 */
	public void setPropertyValue(final String propertyValue) {
		this.propertyValue = propertyValue;
	}

	/**
	 * @return the createdAt
	 */
	public Date getCreatedAt() {
		return createdAt;
	}

	/**
	 * @param createdAt
	 *            the createdAt to set
	 */
	public void setCreatedAt(final Date createdAt) {
		this.createdAt = createdAt;
	}

	/**
	 * @return the updatedAt
	 */
	public Date getUpdatedAt() {
		return updatedAt;
	}

	/**
	 * @param updatedAt
	 *            the updatedAt to set
	 */
	public void setUpdatedAt(final Date updatedAt) {
		this.updatedAt = updatedAt;
	}

}
