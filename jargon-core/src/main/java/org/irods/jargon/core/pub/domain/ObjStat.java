package org.irods.jargon.core.pub.domain;

import java.util.Date;

import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType;

/**
 * Represents an object stat value as returned from the iRODS rsObjStat. NOTE:
 * work in progress, subject to change
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class ObjStat {

	private String absolutePath = "";
	private CollectionAndDataObjectListingEntry.ObjectType objectType;
	private int dataId = 0;
	private String checksum = "";
	private String ownerName = "";
	private String ownerZone = "";
	private long objSize = 0L;
	private Date createdAt = null;
	private Date modifiedAt = null;
	private SpecColType specColType = SpecColType.NORMAL;
	private String collectionPath = "";
	private String cacheDir = "";
	private boolean cacheDirty = false;
	private int replNumber = 0;

	public enum SpecColType {
		NORMAL, STRUCT_FILE_COLL, MOUNTED_COLL, LINKED_COLL
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("objStat:");
		sb.append("\n  absolutePath:");
		sb.append(absolutePath);
		sb.append("\n   dataId:");
		sb.append(dataId);
		sb.append("\n   specColType:");
		sb.append(specColType);
		sb.append("\n   objectType:");
		sb.append(objectType);
		sb.append("\n   collectionPath:");
		sb.append(collectionPath);
		sb.append("\n   checksum:");
		sb.append(checksum);
		sb.append("\n   ownerName:");
		sb.append(ownerName);
		sb.append("\n   ownerZone:");
		sb.append(ownerZone);
		sb.append("\n  objSize:");
		sb.append(objSize);
		sb.append("\n   cacheDir:");
		sb.append(cacheDir);
		sb.append("\n   cacheDirty:");
		sb.append(cacheDirty);
		sb.append("\n   createdAt:");
		sb.append("replNumber:");
		sb.append(replNumber);
		sb.append(createdAt);
		sb.append("\n   modifiedAt:");
		sb.append(modifiedAt);
		return sb.toString();
	}

	/**
	 * @return the absolutePath
	 */
	public String getAbsolutePath() {
		return absolutePath;
	}

	/**
	 * @param absolutePath
	 *            the absolutePath to set
	 */
	public void setAbsolutePath(final String absolutePath) {
		this.absolutePath = absolutePath;
	}

	/**
	 * @return the objectType
	 */
	public CollectionAndDataObjectListingEntry.ObjectType getObjectType() {
		return objectType;
	}

	/**
	 * @param objectType
	 *            the objectType to set
	 */
	public void setObjectType(
			final CollectionAndDataObjectListingEntry.ObjectType objectType) {
		this.objectType = objectType;
	}

	/**
	 * @return the dataId
	 */
	public int getDataId() {
		return dataId;
	}

	/**
	 * @param dataId
	 *            the dataId to set
	 */
	public void setDataId(final int dataId) {
		this.dataId = dataId;
	}

	/**
	 * @return the checksum
	 */
	public String getChecksum() {
		return checksum;
	}

	/**
	 * @param checksum
	 *            the checksum to set
	 */
	public void setChecksum(final String checksum) {
		this.checksum = checksum;
	}

	/**
	 * @return the ownerName
	 */
	public String getOwnerName() {
		return ownerName;
	}

	/**
	 * @param ownerName
	 *            the ownerName to set
	 */
	public void setOwnerName(final String ownerName) {
		this.ownerName = ownerName;
	}

	/**
	 * @return the ownerZone
	 */
	public String getOwnerZone() {
		return ownerZone;
	}

	/**
	 * @param ownerZone
	 *            the ownerZone to set
	 */
	public void setOwnerZone(final String ownerZone) {
		this.ownerZone = ownerZone;
	}

	/**
	 * @return the objSize
	 */
	public long getObjSize() {
		return objSize;
	}

	/**
	 * @param objSize
	 *            the objSize to set
	 */
	public void setObjSize(final long objSize) {
		this.objSize = objSize;
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
	 * @return the modifiedAt
	 */
	public Date getModifiedAt() {
		return modifiedAt;
	}

	/**
	 * @param modifiedAt
	 *            the modifiedAt to set
	 */
	public void setModifiedAt(final Date modifiedAt) {
		this.modifiedAt = modifiedAt;
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
	public void setSpecColType(final SpecColType specColType) {
		this.specColType = specColType;
	}

	/**
	 * Convenience methods determines if this is any type of collection, versus
	 * a file or data object
	 * 
	 * @return <code>boolean</code> of <code>true</code> if this is any type of
	 *         collection or directory;
	 */
	public boolean isSomeTypeOfCollection() {
		return (objectType == ObjectType.COLLECTION || objectType == ObjectType.LOCAL_DIR);
	}

	/**
	 * @return the collectionPath <code>String</code> that indicates that actual
	 *         canonical path to the file
	 */
	public String getCollectionPath() {
		return collectionPath;
	}

	/**
	 * @param collectionPath
	 *            the collectionPath to set <code>String</code> that indicates that
	 *            actual canonical path to the file
	 */
	public void setCollectionPath(String collectionPath) {
		this.collectionPath = collectionPath;
	}

	/**
	 * @return the cacheDir
	 */
	public String getCacheDir() {
		return cacheDir;
	}

	/**
	 * @param cacheDir
	 *            the cacheDir to set
	 */
	public void setCacheDir(String cacheDir) {
		this.cacheDir = cacheDir;
	}

	/**
	 * @return the cacheDirty
	 */
	public boolean isCacheDirty() {
		return cacheDirty;
	}

	/**
	 * @param cacheDirty
	 *            the cacheDirty to set
	 */
	public void setCacheDirty(boolean cacheDirty) {
		this.cacheDirty = cacheDirty;
	}

	/**
	 * @return the replNumber
	 */
	public int getReplNumber() {
		return replNumber;
	}

	/**
	 * @param replNumber
	 *            the replNumber to set
	 */
	public void setReplNumber(int replNumber) {
		this.replNumber = replNumber;
	}

}
