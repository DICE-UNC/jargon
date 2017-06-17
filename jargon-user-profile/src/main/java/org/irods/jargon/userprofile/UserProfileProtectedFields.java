/**
 * 
 */
package org.irods.jargon.userprofile;

/**
 * Non public fields for user profile information that is not allowed by default
 * to the public. This is actually controlled by controlling the visibility of
 * the source of this information to the inquiring user, so that protection is
 * optional.
 * <p>
 * Note that some of the base attributes are based on the RFC for eduPerson at:
 * http://middleware.internet2.edu/eduperson/docs/internet2-mace-dir-eduperson-
 * 200806.html And going forward more attributes may be added.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class UserProfileProtectedFields {

	/**
	 * From RFC 4524: The 'mail' (rfc822mailbox) attribute type holds Internet
	 * mail addresses in Mailbox [RFC2821] form (e.g., user@example.com).
	 */
	private String mail = "";

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("UserProfileProtectedFields:");
		sb.append("\n    mail:");
		sb.append(mail);
		return sb.toString();
	}

	/**
	 * @return the mail
	 */
	public String getMail() {
		return mail;
	}

	/**
	 * @param mail
	 *            the mail to set
	 */
	public void setMail(final String mail) {
		this.mail = mail;
	}

}
