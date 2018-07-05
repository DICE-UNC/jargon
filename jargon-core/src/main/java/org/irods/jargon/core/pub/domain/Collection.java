package org.irods.jargon.core.pub.domain;

import java.util.Date;

import org.irods.jargon.core.pub.domain.ObjStat.SpecColType;

/**
 * Represents a Collection in IRODS. This object represents the ICAT domain
 * object, and is distinct from considering a collection as a type of
 * {@code java.io.File}. For these types of operations, the
 * {@link org.irods.jargon.core.pub.io.IRODSFileImpl} object is available.
 *
 * Note that the {@link org.irods.jargon.core.pub.CollectionAO} access object
 * can work with an IRODS collection as both an {@code IRODSFileImpl} for
 * typical {@code FIle} operations, as well as a {@code Collection} domain
 * object. {@code IRODSFileImpl} is useful when you want to treat IRODS as a
 * file system, while the {@code CollectionAO} is a view of the collection as an
 * IRODS entity, with IRODS specific functionality.
 *
 * Note that this class is not immutable. It is meant to be a data value object
 * suitable for use with its access object. Jargon itself will not alter or
 * retain reference to any objects of this type when used as a parameter.
 *
 * @author Mike Conway - DICE (www.irods.org)
 */
public class Collection extends IRODSDomainObject {

	private int collectionId = 0;
	/**
	 * This will be the full absolute path of the collection, in the case of a
	 * mounted collection, such as a soft link, this may be the linked name, where
	 * the objectPath will contain the canonical path or actual physial location
	 */
	private String collectionName = "";
	/**
	 * The canonical absolute path for the object if this is a soft-linked
	 * collection. If this object is retrieved by the canonical path, or it is not a
	 * special collection, this will be blank
	 */
	private String objectPath = "";
	/**
	 * This will be the full absolute path of the parent of the given collection
	 */
	private String collectionParentName = "";
	private String collectionOwnerName = "";
	private String collectionOwnerZone = "";
	private String collectionMapId = "";
	private String collectionInheritance = "";
	private String comments = "";
	private String info1 = "";
	private String info2 = "";
	private Date createdAt = new Date();
	private Date modifiedAt = new Date();
	private SpecColType specColType = SpecColType.NORMAL;
	/**
	 * Indicates that the collection is a stand-in proxy for a collection the user
	 * cannot see, this is done to handle strict acls and simulating the ability to
	 * drill down from the root of the hierarchy, using heuristics to get the
	 * available intervening paths like zone and zone/home
	 */
	private boolean isProxy = false;

	public int getCollectionId() {
		return collectionId;
	}

	public void setCollectionId(final int collectionId) {
		this.collectionId = collectionId;
	}

	public String getCollectionName() {
		return collectionName;
	}

	public void setCollectionName(final String collectionName) {
		this.collectionName = collectionName;
	}

	/**
	 * Handy method to grab the last part of the path.
	 *
	 * @return {@code String}
	 */
	public String getCollectionLastPathComponent() {
		int lastSlash = collectionName.lastIndexOf('/');
		if (lastSlash == -1) {
			return "";
		}
		return collectionName.substring(lastSlash + 1);
	}

	public String getCollectionParentName() {
		return collectionParentName;
	}

	/**
	 * Returns the absolute path for a collection, this is synonymous with the
	 * {@code getCollectionName()}.
	 *
	 * @return {@code String}
	 */
	public String getAbsolutePath() {
		return getCollectionName();
	}

	public void setCollectionParentName(final String collectionParentName) {
		this.collectionParentName = collectionParentName;
	}

	public String getCollectionOwnerName() {
		return collectionOwnerName;
	}

	public void setCollectionOwnerName(final String collectionOwnerName) {
		this.collectionOwnerName = collectionOwnerName;
	}

	public String getCollectionOwnerZone() {
		return collectionOwnerZone;
	}

	public void setCollectionOwnerZone(final String collectionOwnerZone) {
		this.collectionOwnerZone = collectionOwnerZone;
	}

	public String getCollectionMapId() {
		return collectionMapId;
	}

	public void setCollectionMapId(final String collectionMapId) {
		this.collectionMapId = collectionMapId;
	}

	public String getCollectionInheritance() {
		return collectionInheritance;
	}

	public void setCollectionInheritance(final String collectionInheritance) {
		this.collectionInheritance = collectionInheritance;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(final String comments) {
		this.comments = comments;
	}

	public String getInfo1() {
		return info1;
	}

	public void setInfo1(final String info1) {
		this.info1 = info1;
	}

	public String getInfo2() {
		return info2;
	}

	public void setInfo2(final String info2) {
		this.info2 = info2;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(final Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getModifiedAt() {
		return modifiedAt;
	}

	public void setModifiedAt(final Date modifiedAt) {
		this.modifiedAt = modifiedAt;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Collection [collectionId=").append(collectionId).append(", ");
		if (collectionName != null) {
			builder.append("collectionName=").append(collectionName).append(", ");
		}
		if (objectPath != null) {
			builder.append("objectPath=").append(objectPath).append(", ");
		}
		if (collectionParentName != null) {
			builder.append("collectionParentName=").append(collectionParentName).append(", ");
		}
		if (collectionOwnerName != null) {
			builder.append("collectionOwnerName=").append(collectionOwnerName).append(", ");
		}
		if (collectionOwnerZone != null) {
			builder.append("collectionOwnerZone=").append(collectionOwnerZone).append(", ");
		}
		if (collectionMapId != null) {
			builder.append("collectionMapId=").append(collectionMapId).append(", ");
		}
		if (collectionInheritance != null) {
			builder.append("collectionInheritance=").append(collectionInheritance).append(", ");
		}
		if (comments != null) {
			builder.append("comments=").append(comments).append(", ");
		}
		if (info1 != null) {
			builder.append("info1=").append(info1).append(", ");
		}
		if (info2 != null) {
			builder.append("info2=").append(info2).append(", ");
		}
		if (createdAt != null) {
			builder.append("createdAt=").append(createdAt).append(", ");
		}
		if (modifiedAt != null) {
			builder.append("modifiedAt=").append(modifiedAt).append(", ");
		}
		if (specColType != null) {
			builder.append("specColType=").append(specColType).append(", ");
		}
		builder.append("isProxy=").append(isProxy).append("]");
		return builder.toString();
	}

	/**
	 * @return the objectPath {@code String} that will normally be blank, but if
	 *         this is a soft link, this will reflect the canonical path to this
	 *         collection
	 */
	public String getObjectPath() {
		return objectPath;
	}

	/**
	 * @param objectPath
	 *            the objectPath to set {@code String} that will normally be blank,
	 *            but if this is a soft link, this will reflect the canonical path
	 *            to this collection
	 */
	public void setObjectPath(final String objectPath) {
		this.objectPath = objectPath;
	}

	public SpecColType getSpecColType() {
		return specColType;
	}

	public void setSpecColType(final SpecColType specColType) {
		this.specColType = specColType;
	}

	public boolean isProxy() {
		return isProxy;
	}

	public void setProxy(final boolean isProxy) {
		this.isProxy = isProxy;
	}

}
