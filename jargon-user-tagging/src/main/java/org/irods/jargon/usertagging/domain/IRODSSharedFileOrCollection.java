/**
 * 
 */
package org.irods.jargon.usertagging.domain;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.query.MetaDataAndDomainData.MetadataDomain;
import org.irods.jargon.core.query.UserAnnotatedCatalogItem;

/**
 * Represents a first class shared file or folder. If this is a collection, only
 * the top level is marked as a share, and child data will be part of this named
 * share.
 * <p>
 * All fields are final and immutable.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class IRODSSharedFileOrCollection extends UserAnnotatedCatalogItem
		implements Serializable {

	private static final long serialVersionUID = 3413211086947009069L;

	/**
	 * Generic name for the share
	 */
	private final String shareName;

	/**
	 * Owner of the share, no zone in name
	 */
	private final String shareOwner;

	/**
	 * Zone of the share owner
	 */
	private final String shareOwnerZone;

	/**
	 * Represents the list of users to share with
	 */
	private final List<ShareUser> shareUsers;

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("IRODSSharedFileOrCollection");
		sb.append("\n\tshareName:");
		sb.append(shareName);
		sb.append("\n\tmetadataDomain:");
		sb.append(getMetadataDomain());
		sb.append("\n\tpath:");
		sb.append(getDomainUniqueName());
		sb.append("\n\tshareOwner:");
		sb.append(shareOwner);
		sb.append("\n\tshareZone:");
		sb.append(shareOwnerZone);
		sb.append("\n");
		sb.append(shareUsers);
		return sb.toString();
	}

	/**
	 * A shared folder or collection. Describes the user and object that is
	 * being starred, and providing a description.
	 * 
	 * @param metadataDomain
	 *            <code>MetaDataAndDomainData.MetadataDomain</code> enum value
	 *            that identifies the domain object type that is tagged (e.g.
	 *            DataObject, Collection)
	 * @param domainUniqueName
	 *            <code>String</code> with the unque name for the tagged data
	 *            object (e.g. iRODS absolute path for a data object or
	 *            collection).
	 * @param shareName
	 *            <code>String</code> with an alias, or name for the share
	 * @param shareOwner
	 *            <code>String</code> with the user name who owns the shared
	 *            file or collection
	 * @param shareOwnerZone
	 *            <code>String</code> with zone for the user name who owns the
	 *            shared file or collection. May be set blank to denote current
	 *            zone
	 * @param shareUser
	 *            {@link ShareUser} with the user names and permissions
	 * @param shareOwner
	 *            <code>String</code> with the owner of the share, in name#zone
	 *            format
	 * 
	 * @throws JargonException
	 */
	public IRODSSharedFileOrCollection(final MetadataDomain metadataDomain,
			final String domainUniqueName, final String shareName,
			final String shareOwner, final String shareOwnerZone,
			final List<ShareUser> shareUsers) throws JargonException {

		super(metadataDomain, domainUniqueName, shareOwner);

		if (shareName == null || shareName.isEmpty()) {
			throw new IllegalArgumentException("null or empty shareName");
		}

		if (shareOwner == null || shareOwner.isEmpty()) {
			throw new IllegalArgumentException("null or empty shareOwner");
		}

		if (shareOwnerZone == null) {
			throw new IllegalArgumentException("null or empty shareOwnerZone");
		}

		if (shareUsers == null) {
			throw new IllegalArgumentException("null shareUsers");
		}

		this.shareName = shareName;
		this.shareOwner = shareOwner;
		this.shareOwnerZone = shareOwnerZone;
		this.shareUsers = Collections.unmodifiableList(shareUsers);

	}

	public String getShareName() {
		return shareName;
	}

	public List<ShareUser> getShareUsers() {
		return shareUsers;
	}

	public String getShareOwner() {
		return shareOwner;
	}

	public String getShareOwnerZone() {
		return shareOwnerZone;
	}

}
