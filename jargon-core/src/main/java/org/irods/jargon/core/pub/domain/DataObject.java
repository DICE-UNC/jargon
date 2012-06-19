/**
 * 
 */
package org.irods.jargon.core.pub.domain;

import java.util.Date;

import org.irods.jargon.core.pub.domain.ObjStat.SpecColType;

/**
 * Represents a DataObject (file) in iRODS
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class DataObject extends IRODSDomainObject {

	private int id = 0;
	private int collectionId = 0;
	private String dataName = "";
	private String collectionName = "";
	private int dataReplicationNumber = 0;
	private int dataVersion = 0;
	private String dataTypeName = "";
	private long dataSize = 0L;
	private String resourceGroupName = "";
	private String resourceName = "";
	private String dataPath = "";
	private String dataOwnerName = "";
	private String dataOwnerZone = "";
	private String replicationStatus = "";
	private String dataStatus = "";
	private String checksum = "";
	private String expiry = "";
	private int dataMapId = 0;
	private String comments = "";
	private Date createdAt = new Date();
	private Date updatedAt = new Date();
	private SpecColType specColType = SpecColType.NORMAL;
	private String objectPath = "";

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Data Object:");
		sb.append("\n   id:");
		sb.append(id);
		sb.append("\n   collection id:");
		sb.append(collectionId);
		sb.append("\n   dataName:");
		sb.append(dataName);
		sb.append("\n   collectionName:");
		sb.append(collectionName);
		sb.append("\n   resourceName:");
		sb.append(resourceName);
		sb.append("\n   dataPath");
		sb.append(dataPath);
		sb.append("\n   checkSum:");
		sb.append(checksum);
		sb.append("\n   dataSize:");
		sb.append(dataSize);
		sb.append("\n   specColType:");
		sb.append(specColType);
		sb.append("\n   objectPath:");
		sb.append(objectPath);
		return sb.toString();
	}

	/**
	 * Handy method to concatenate collection and data object name and return
	 * the computed absolute path
	 * 
	 * @return <code>String</code> with the absolute path.
	 */
	public String getAbsolutePath() {
		StringBuilder sb = new StringBuilder();
		sb.append(collectionName);
		sb.append('/');
		sb.append(dataName);
		return sb.toString();
	}

	/**
	 * Get the database unique identifier for the data object in iRODS
	 * 
	 * @return <code>int</code> with database unique key
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets the unique database identifier for the data object in iRODS. This is
	 * typically set in the construction of the data object from the
	 * <code>DataObjectAO</code> class, and setting it outside of internal
	 * routines that build the data object from iCAT values has no meaning.
	 * 
	 * @param id
	 *            <code>int</code> with the unique id from the iRODS ICAT.
	 */
	public void setId(final int id) {
		this.id = id;
	}

	/**
	 * Gets the unique database identifier for the data object's parent
	 * collection. This is set by the <code>DataObjectAO</code> when retrieving
	 * from ICAT, and has no effect if set by a user.
	 * 
	 * @return <code>int</code> with the unique id for this data object's parent
	 *         collection in the ICAT.
	 */
	public int getCollectionId() {
		return collectionId;
	}

	/**
	 * Sets the unique database identifier for the data object's parent
	 * collection in iRODS. This is typically set in the construction of the
	 * data object from the <code>DataObjectAO</code> class, and setting it
	 * outside of internal routines that build the data object from iCAT values
	 * has no meaning.
	 * 
	 * @param id
	 *            <code>int</code> with the unique id of the parent collection
	 *            from the iRODS ICAT.
	 */
	public void setCollectionId(final int collectionId) {
		this.collectionId = collectionId;
	}

	/**
	 * Gets the name of the iRODS file. Note that the parent collection name is
	 * not reflected here.
	 * 
	 * @return <code>String</code> with the name of the iRODS data object.
	 */
	public String getDataName() {
		return dataName;
	}

	/**
	 * Sets the name of the data object in iRODS.
	 * 
	 * @param dataName
	 *            <code>String</code> set by the <code>DataObjectAO</code> when
	 *            retrieving from ICAT, and has no effect if set by a user.
	 *            Methods are available in Jargon to rename files.
	 */
	public void setDataName(final String dataName) {
		this.dataName = dataName;
	}

	/**
	 * Gets the replica number
	 * 
	 * @return <code>int</code> with the replica number
	 */
	public int getDataReplicationNumber() {
		return dataReplicationNumber;
	}

	/**
	 * Sets the replica number
	 * 
	 * @param dataReplicationNumber
	 *            <code>int</code> with the replica number
	 */
	public void setDataReplicationNumber(final int dataReplicationNumber) {
		this.dataReplicationNumber = dataReplicationNumber;
	}

	public int getDataVersion() {
		return dataVersion;
	}

	public void setDataVersion(final int dataVersion) {
		this.dataVersion = dataVersion;
	}

	public String getDataTypeName() {
		return dataTypeName;
	}

	public void setDataTypeName(final String dataTypeName) {
		this.dataTypeName = dataTypeName;
	}

	/**
	 * Get the length of the iRODS file
	 * 
	 * @return <code>long</code> with the length of the file in iRODS, in bytes.
	 */
	public long getDataSize() {
		return dataSize;
	}

	/**
	 * Sets the length of the iRODS file
	 * 
	 * @param dataSize
	 *            <code>long</code> with the length of the file in iRODS, in
	 *            bytes.
	 */
	public void setDataSize(final long dataSize) {
		this.dataSize = dataSize;
	}

	/**
	 * Gets the resource group (if any) that this file belongs to
	 * 
	 * @return <code>String</code> with the name of the resource group the file
	 *         belongs to, or blank if none (no null).
	 */
	public String getResourceGroupName() {
		return resourceGroupName;
	}

	/**
	 * Sets the resource group that this file belongs to.
	 * 
	 * @param resourceGroupName
	 *            <code>String</code> with the name of the resource group, or
	 *            blank if none (no null).
	 */
	public void setResourceGroupName(final String resourceGroupName) {
		this.resourceGroupName = resourceGroupName;
	}

	/**
	 * Gets the name of the resource that this file is stored on.
	 * 
	 * @return <code>String</code> with name of resoruce.
	 */
	public String getResourceName() {
		return resourceName;
	}

	/**
	 * Sets the name of the resource that this file is stored on
	 * 
	 * @param resourceName
	 *            <code>String</code> with the name of the resource, or blank if
	 *            not used (no null).
	 */
	public void setResourceName(final String resourceName) {
		this.resourceName = resourceName;
	}

	/**
	 * Gets the physical path on the resource that is the location of the file
	 * in the local file system.
	 * 
	 * @return <code>String</code> with the absolute path to the physical file
	 *         on the resource.
	 */
	public String getDataPath() {
		return dataPath;
	}

	/**
	 * Sets the physical path on the resource that is the location of the file
	 * in the local file system.
	 * 
	 * @param dataPath
	 *            <code>String</code> with the absolute path to the physical
	 *            file on the resource.
	 */
	public void setDataPath(final String dataPath) {
		this.dataPath = dataPath;
	}

	/**
	 * Gets the name of the owner of this file.
	 * 
	 * @return <code>String</code> with the name of the owner of the file (iRODS
	 *         User Name)
	 */
	public String getDataOwnerName() {
		return dataOwnerName;
	}

	/**
	 * Sets the name fo the owner of this file
	 * 
	 * @param dataOwnerName
	 *            <code>String</code> with iRODS user name.
	 */
	public void setDataOwnerName(final String dataOwnerName) {
		this.dataOwnerName = dataOwnerName;
	}

	/**
	 * Gets the zone on which the file is stored.
	 * 
	 * @return <code>String</code> with the name of the zone that has the file
	 *         in the catalog.
	 */
	public String getDataOwnerZone() {
		return dataOwnerZone;
	}

	/**
	 * Sets the zone on which the file is stored
	 * 
	 * @param dataOwnerZone
	 *            <code>String</code> with the name of the zone that hosts the
	 *            file.
	 */
	public void setDataOwnerZone(final String dataOwnerZone) {
		this.dataOwnerZone = dataOwnerZone;
	}

	public String getReplicationStatus() {
		return replicationStatus;
	}

	public void setReplicationStatus(final String replicationStatus) {
		this.replicationStatus = replicationStatus;
	}

	public String getDataStatus() {
		return dataStatus;
	}

	public void setDataStatus(final String dataStatus) {
		this.dataStatus = dataStatus;
	}

	/**
	 * Gets the iRODS computed checksum (if calculated), or blank (no nulls).
	 * 
	 * @return <code>String</code> with the computed checksum.
	 */
	public String getChecksum() {
		return checksum;
	}

	/**
	 * Sets the iRODS computed checksum, or blank if none (no nulls).
	 * 
	 * @param checksum
	 *            <code>String</code> with the computed checksum, or blank.
	 */
	public void setChecksum(final String checksum) {
		this.checksum = checksum;
	}

	public String getExpiry() {
		return expiry;

	}

	public void setExpiry(final String expiry) {
		this.expiry = expiry;
	}

	public int getDataMapId() {
		return dataMapId;
	}

	public void setDataMapId(final int dataMapId) {
		this.dataMapId = dataMapId;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(final String comments) {
		this.comments = comments;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(final Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(final Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	/**
	 * Sets the name of the collection that is the parent of this data object.
	 * 
	 * @param collectionName
	 *            <code>String</code> with the absolute path to the parent of
	 *            this collection.
	 */
	public void setCollectionName(final String collectionName) {
		this.collectionName = collectionName;
	}

	/**
	 * Get the name of the parent collection of the iRODS data object.
	 * 
	 * @return <code>String</code> with the name of the parent collection.
	 */
	public String getCollectionName() {
		return collectionName;
	}

	/**
	 * @return the specColType {@link SpecColType} enum value that indicates the
	 *         type of collection. If this is a special collection, such as a
	 *         mounted collection or a soft link, it will be reflected here
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

	/**
	 * @return the objectPath <code>String</code> with the canonical path of the
	 *         object. if this is a soft link the object path is the canonical
	 *         path to the data object, and will carry the full file name.
	 */
	public String getObjectPath() {
		return objectPath;
	}

	/**
	 * @param objectPath
	 *            the objectPath to set
	 */
	public void setObjectPath(String objectPath) {
		this.objectPath = objectPath;
	}
}