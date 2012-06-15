package org.irods.jargon.core.pub.domain;

import java.util.Date;

import org.irods.jargon.core.pub.domain.ObjStat.SpecColType;

/**
 * Represents a Collection in IRODS. This object represents the ICAT domain
 * object, and is distinct from considering a collection as a type of
 * <code>java.io.File</code>. For these types of operations, the
 * {@link org.irods.jargon.core.pub.io.IRODSFileImpl} object is available.
 * 
 * Note that the {@link org.irods.jargon.core.pub.CollectionAO} access object
 * can work with an IRODS collection as both an <code>IRODSFileImpl</code> for
 * typical <code>FIle</code> operations, as well as a <code>Collection</code>
 * domain object. <code>IRODSFileImpl</code> is useful when you want to treat
 * IRODS as a file system, while the <code>CollectionAO</code> is a view of the
 * collection as an IRODS entity, with IRODS specific functionality.
 * 
 * Note that this class is not immutable. It is meant to be a data value object
 * suitable for use with its access object. Jargon itself will not alter or
 * retain reference to any objects of this type when used as a parameter.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 */
public class Collection extends IRODSDomainObject {

	private int collectionId = 0;
	private String collectionName = "";
	/**
	 * The canonical absolute path for the object if this is a soft-linked
	 * collection. If this object is retrieved by the canonical path, or it is
	 * not a special collection, this will be blank
	 */
	private String objectPath = "";
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
	 * Get the full absolute path to the collection, this appends the parent
	 * collection name to the sub collection name
	 * 
	 * @return
	 */
	public String getAbsolutePath() {
		StringBuilder sb = new StringBuilder();
		if (collectionParentName.length() > 1) {
			sb.append(collectionParentName);
		}

		if (collectionName.length() > 1) {
			sb.append(collectionName);
		}
		return sb.toString();
	}

	/**
	 * Handy method to grab the last part of the path.
	 * 
	 * @return <code>String</code>
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
		StringBuilder sb = new StringBuilder();
		sb.append("Collection:");
		sb.append("\n   id:");
		sb.append(collectionId);
		sb.append("\n   name:");
		sb.append(collectionName);
		sb.append("\n   parentName:");
		sb.append(collectionParentName);
		sb.append("\n   ownerName:");
		sb.append(collectionOwnerName);
		sb.append("\n   collectionOwnerZone:");
		sb.append(collectionOwnerZone);
		sb.append("\n   collectionMapId:");
		sb.append(collectionMapId);
		sb.append("\n   collectionInheritance:");
		sb.append(collectionInheritance);
		sb.append("\n   comments:");
		sb.append(comments);
		sb.append("\n   specialCollectionType:");
		sb.append(specColType);
		sb.append("\n   objectPath:");
		sb.append(objectPath);
		sb.append("\n   info1:");
		sb.append(info1);
		sb.append("\n   info2:");
		sb.append(info2);
		sb.append("\n   createdAt:");
		sb.append(createdAt);
		sb.append("\n   modifiedAt:");
		sb.append(modifiedAt);
		return sb.toString();
	}

	/**
	 * @return the objectPath <code>String</code> that will normally be blank,
	 *         but if this is a soft link, this will reflect the canonical path
	 *         to this collection
	 */
	public String getObjectPath() {
		return objectPath;
	}

	/**
	 * @param objectPath
	 *            the objectPath to set <code>String</code> that will normally
	 *            be blank, but if this is a soft link, this will reflect the
	 *            canonical path to this collection
	 */
	public void setObjectPath(String objectPath) {
		this.objectPath = objectPath;
	}

	/**
	 * @return the specColType
	 */
	public SpecColType getSpecColType() {
		return specColType;
	}

	/**
	 * @param specColType
	 *            the specColType to set
	 */
	public void setSpecColType(SpecColType specColType) {
		this.specColType = specColType;
	}

}
