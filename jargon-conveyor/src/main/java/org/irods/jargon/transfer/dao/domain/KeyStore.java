/**
 * 
 */
package org.irods.jargon.transfer.dao.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Represents the stored key in the database. This is the 'pass phrase' by which
 * passwords are encrypted. This is stored as a hash of the actual pass phrase,
 * and can be used for logging in to iDrop, and for validating the pass phrase
 * at login.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 */
@Entity
@Table(name = "key_store")
public class KeyStore implements Serializable {

	private static final long serialVersionUID = -5176136872075721634L;
	public static final String KEY_STORE_PASS_PHRASE = "PASS_PHRASE";

	@Id()
	@Column(name = "id")
	private String id = "";

	@Column(name = "value", nullable = false)
	private String value = "";

	public KeyStore() {
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(final String value) {
		this.value = value;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(final String id) {
		this.id = id;
	}

}
