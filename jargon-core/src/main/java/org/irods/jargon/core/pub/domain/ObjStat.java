package org.irods.jargon.core.pub.domain;

import java.util.Date;

import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;

/**
 * Represents an object stat value as returned from the iRODS rsObjStat.
 * NOTE: work in progress, subject to change
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
	public enum SpecColType { NORMAL }
	
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
		sb.append("\n   checksum:");
		sb.append(checksum);
		sb.append("\n   ownerName:");
		sb.append(ownerName);
		sb.append("\n   ownerZone:");
		sb.append(ownerZone);
		sb.append("\n  objSize:");
		sb.append(objSize);
		sb.append("\n   createdAt:");
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
	 * @param absolutePath the absolutePath to set
	 */
	public void setAbsolutePath(String absolutePath) {
		this.absolutePath = absolutePath;
	}
	/**
	 * @return the objectType
	 */
	public CollectionAndDataObjectListingEntry.ObjectType getObjectType() {
		return objectType;
	}
	/**
	 * @param objectType the objectType to set
	 */
	public void setObjectType(
			CollectionAndDataObjectListingEntry.ObjectType objectType) {
		this.objectType = objectType;
	}
	/**
	 * @return the dataId
	 */
	public int getDataId() {
		return dataId;
	}
	/**
	 * @param dataId the dataId to set
	 */
	public void setDataId(int dataId) {
		this.dataId = dataId;
	}
	/**
	 * @return the checksum
	 */
	public String getChecksum() {
		return checksum;
	}
	/**
	 * @param checksum the checksum to set
	 */
	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}
	/**
	 * @return the ownerName
	 */
	public String getOwnerName() {
		return ownerName;
	}
	/**
	 * @param ownerName the ownerName to set
	 */
	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}
	/**
	 * @return the ownerZone
	 */
	public String getOwnerZone() {
		return ownerZone;
	}
	/**
	 * @param ownerZone the ownerZone to set
	 */
	public void setOwnerZone(String ownerZone) {
		this.ownerZone = ownerZone;
	}
	/**
	 * @return the objSize
	 */
	public long getObjSize() {
		return objSize;
	}
	/**
	 * @param objSize the objSize to set
	 */
	public void setObjSize(long objSize) {
		this.objSize = objSize;
	}
	/**
	 * @return the createdAt
	 */
	public Date getCreatedAt() {
		return createdAt;
	}
	/**
	 * @param createdAt the createdAt to set
	 */
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
	/**
	 * @return the modifiedAt
	 */
	public Date getModifiedAt() {
		return modifiedAt;
	}
	/**
	 * @param modifiedAt the modifiedAt to set
	 */
	public void setModifiedAt(Date modifiedAt) {
		this.modifiedAt = modifiedAt;
	}

	/**
	 * @return the specColType
	 */
	public SpecColType getSpecColType() {
		return specColType;
	}

	/**
	 * @param specColType the specColType to set
	 */
	public void setSpecColType(SpecColType specColType) {
		this.specColType = specColType;
	}
	
	

}
