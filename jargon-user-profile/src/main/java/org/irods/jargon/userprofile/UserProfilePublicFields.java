package org.irods.jargon.userprofile;

/**
 * Represents standard conventions for user demographic and application
 * configuration data. This object represents public data that should be visible
 * to all through querying via the user profile service.
 * <p>
 * Note that some of the base attributes are based on the RFC for eduPerson at:
 * http://middleware.internet2.edu/eduperson/docs/internet2-mace-dir-eduperson-
 * 200806.html And going forward more attributes may be added.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class UserProfilePublicFields {

	/**
	 * Open-ended; whatever the person or the directory manager puts here. According
	 * to RFC 4519, "The 'description' attribute type contains human-readable
	 * descriptive phrases about the object. Each description is one value of this
	 * multi-valued attribute."
	 */
	private String description = "";

	/**
	 * Person's nickname, or the informal name by which they are accustomed to be
	 * hailed.
	 */
	private String nickName = "";

	/**
	 * The 'cn' ('commonName' in X.500) attribute type contains names of an object.
	 * Each name is one value of this multi-valued attribute. If the object
	 * corresponds to a person, it is typically the person's full name."
	 */
	private String cn = "";

	/**
	 * Part of name not surname
	 */
	private String givenName = "";

	/**
	 * Surname or family name. From RFC 4519: "The 'sn' ('surname' in X.500)
	 * attribute type contains name strings for the family names of a person. Each
	 * string is one value of this multi-valued attribute."
	 */
	private String sn = "";

	/**
	 * Campus or office address. inetOrgPerson has a homePostalAddress that
	 * complements this attribute. X.520(2000) reads: "The Postal Address attribute
	 * type specifies the address information required for the physical postal
	 * delivery to an object."
	 */
	private String postalAddress = "";

	/**
	 * Follow X.500(2001): "The postal code attribute type specifies the postal code
	 * of the named object. If this attribute
	 * <p>
	 * value is present, it will be part of the object's postal address." Zip code
	 * in USA, postal code for other countries.
	 */
	private String postalCode = "";

	/**
	 * From RFC 4519: "The 'postOfficeBox' attribute type contains postal box
	 * identifiers that a Postal Service uses when a customer arranges to receive
	 * mail at a box on the premises of the Postal Service. Each postal box
	 * identifier is a single value of this multi-valued attribute."
	 */
	private String postOfficeBox = "";

	/**
	 * According to RFC 4519, "The 'l' ('localityName' in X.500) attribute type
	 * contains names of a locality or place, such as a city, county, or other
	 * geographic region. Each name is one value of this multi-valued attribute."
	 */
	private String localityName = "";

	/**
	 * From RFC 4519: "The 'street' ('streetAddress' in X.500) attribute type
	 * contains site information from a postal address (i.e., the street name,
	 * place, avenue, and the house number). Each street is one value of this
	 * multi-valued attribute."
	 */
	private String street = "";

	/**
	 * IRODS URI or URL to a publicly available photo in String form
	 * <p>
	 * Follow inetOrgPerson definition of RFC 2798: "Used to store one or more
	 * images of a person using the JPEG File Interchange Format [JFIF]."
	 *
	 */
	private String jpegPhoto = "";

	/**
	 * Follow inetOrgPerson definition of RFC 2079: "Uniform Resource Identifier
	 * with optional label."
	 */
	private String labeledURL = "";

	/**
	 * Abbreviation for state or province name.
	 * <p>
	 * Format: The values should be coordinated on a national level. If well-known
	 * shortcuts exist, like the two-letter state abbreviations in the US, these
	 * abbreviations are preferred over longer full names.
	 * <p>
	 * From RFC 4519: "The 'st' ('stateOrProvinceName' in X.500) attribute type
	 * contains the full names of states or provinces. Each name is one value of
	 * this multi-valued attribute."
	 */
	private String st = "";

	/**
	 * From RFC 4519: "The 'title' attribute type contains the title of a person in
	 * their organizational context. Each title is one value of this multi-valued
	 * attribute."
	 */
	private String title = "";

	/**
	 * Office/campus phone number. Attribute values should comply with the ITU
	 * Recommendation E.123 [E.123]: i.e., "+44 71 123 4567."
	 */
	private String telephoneNumber = "";

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(final String description) {
		this.description = description;
	}

	/**
	 * @return the nickName
	 */
	public String getNickName() {
		return nickName;
	}

	/**
	 * @param nickName
	 *            the nickName to set
	 */
	public void setNickName(final String nickName) {
		this.nickName = nickName;
	}

	/**
	 * @return the cn
	 */
	public String getCn() {
		return cn;
	}

	/**
	 * @param cn
	 *            the cn to set
	 */
	public void setCn(final String cn) {
		this.cn = cn;
	}

	/**
	 * @return the givenName
	 */
	public String getGivenName() {
		return givenName;
	}

	/**
	 * @param givenName
	 *            the givenName to set
	 */
	public void setGivenName(final String givenName) {
		this.givenName = givenName;
	}

	/**
	 * @return the sn
	 */
	public String getSn() {
		return sn;
	}

	/**
	 * @param sn
	 *            the sn to set
	 */
	public void setSn(final String sn) {
		this.sn = sn;
	}

	/**
	 * @return the postalAddress
	 */
	public String getPostalAddress() {
		return postalAddress;
	}

	/**
	 * @param postalAddress
	 *            the postalAddress to set
	 */
	public void setPostalAddress(final String postalAddress) {
		this.postalAddress = postalAddress;
	}

	/**
	 * @return the postalCode
	 */
	public String getPostalCode() {
		return postalCode;
	}

	/**
	 * @param postalCode
	 *            the postalCode to set
	 */
	public void setPostalCode(final String postalCode) {
		this.postalCode = postalCode;
	}

	/**
	 * @return the postOfficeBox
	 */
	public String getPostOfficeBox() {
		return postOfficeBox;
	}

	/**
	 * @param postOfficeBox
	 *            the postOfficeBox to set
	 */
	public void setPostOfficeBox(final String postOfficeBox) {
		this.postOfficeBox = postOfficeBox;
	}

	/**
	 * @return the localityName
	 */
	public String getLocalityName() {
		return localityName;
	}

	/**
	 * @param localityName
	 *            the localityName to set
	 */
	public void setLocalityName(final String localityName) {
		this.localityName = localityName;
	}

	/**
	 * @return the street
	 */
	public String getStreet() {
		return street;
	}

	/**
	 * @param street
	 *            the street to set
	 */
	public void setStreet(final String street) {
		this.street = street;
	}

	/**
	 * @return the jpegPhoto
	 */
	public String getJpegPhoto() {
		return jpegPhoto;
	}

	/**
	 * @param jpegPhoto
	 *            the jpegPhoto to set
	 */
	public void setJpegPhoto(final String jpegPhoto) {
		this.jpegPhoto = jpegPhoto;
	}

	/**
	 * @return the labeledURL
	 */
	public String getLabeledURL() {
		return labeledURL;
	}

	/**
	 * @param labeledURL
	 *            the labeledURL to set
	 */
	public void setLabeledURL(final String labeledURL) {
		this.labeledURL = labeledURL;
	}

	/**
	 * @return the st
	 */
	public String getSt() {
		return st;
	}

	/**
	 * @param st
	 *            the st to set
	 */
	public void setSt(final String st) {
		this.st = st;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title
	 *            the title to set
	 */
	public void setTitle(final String title) {
		this.title = title;
	}

	/**
	 * @return the telephoneNumber
	 */
	public String getTelephoneNumber() {
		return telephoneNumber;
	}

	/**
	 * @param telephoneNumber
	 *            the telephoneNumber to set
	 */
	public void setTelephoneNumber(final String telephoneNumber) {
		this.telephoneNumber = telephoneNumber;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("UserProfilePublicFields");
		sb.append("\n    description:");
		sb.append(description);
		sb.append("\n   nickName:");
		sb.append(nickName);
		sb.append("\n   cn:");
		sb.append(cn);
		sb.append("\n   givenName:");
		sb.append(givenName);
		sb.append("\n    sn:");
		sb.append(sn);
		sb.append("\n   postalAddress:");
		sb.append(postalAddress);
		sb.append("\n   postalCode:");
		sb.append(postalCode);
		sb.append("\n   postOfficeBox:");
		sb.append(postOfficeBox);
		sb.append("\n    sn:");
		sb.append(sn);
		sb.append("\n    st:");
		sb.append(st);
		sb.append("\n    street:");
		sb.append(street);
		sb.append("\n    telephoneNumber:");
		sb.append(telephoneNumber);
		sb.append("\n    title:");
		sb.append(title);
		return sb.toString();
	}

}
