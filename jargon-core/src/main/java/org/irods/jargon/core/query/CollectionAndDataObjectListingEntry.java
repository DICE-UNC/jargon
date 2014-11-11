package org.irods.jargon.core.query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.irods.jargon.core.pub.domain.IRODSDomainObject;
import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.core.pub.domain.ObjStat.SpecColType;
import org.irods.jargon.core.pub.domain.UserFilePermission;
import org.irods.jargon.core.utils.MiscIRODSUtils;

/**
 * Value object that holds information on data objects and collections. This
 * object includes info to distinguish between data object and collection, to
 * identify it by path, and also information that can be used for paging.
 * <p/>
 * This object is meant to be used for use cases such as iRODS file tree
 * browsing, and as such it is meant to be returned in collections. The behavior
 * of these objects in the collection is such that objects for collections and
 * objects for data objects separately hold counts and indicators that this
 * particular collection or object is the last result. In this way, a caller
 * gets a result that can say that all the collections have been returned, but
 * the data objects may have more results to page.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class CollectionAndDataObjectListingEntry extends IRODSDomainObject
		implements Comparable<CollectionAndDataObjectListingEntry> {

	/**
	 * Analogous to objType_t defined in rodsType.h
	 */
	public enum ObjectType {
		UNKNOWN, DATA_OBJECT, COLLECTION, UNKNOWN_FILE, LOCAL_FILE, LOCAL_DIR, NO_INPUT
	}

	private String parentPath = "";
	private String pathOrName = "";
	/**
	 * Optional arbitrary string value that can contain a description or special
	 * note. This can be useful for special types of collection listings
	 */
	private String description = "";

	public void setDescription(String description) {
		this.description = description;
	}

	private String specialObjectPath = "";
	private ObjectType objectType = null;
	private Date createdAt = null;
	private Date modifiedAt = null;
	private long dataSize = 0L;
	private String ownerName = "";
	private String ownerZone = "";
	private List<UserFilePermission> userFilePermission = new ArrayList<UserFilePermission>();
	private int id;
	private ObjStat.SpecColType specColType = SpecColType.NORMAL;

	/**
	 * Return the absolute path the the parent of the file or collection.
	 * 
	 * @return <code>String</code> with the absolute path to the parent of the
	 *         file or collection.
	 */
	public String getParentPath() {
		return parentPath;
	}

	public void setParentPath(final String parentPath) {
		this.parentPath = parentPath;
	}

	/**
	 * Return the absolute path of the file or collection under the parent
	 * 
	 * @return <code>String</code> with the absolute path to the file or
	 *         collection under the parent.
	 */
	public String getPathOrName() {
		return pathOrName;
	}

	public void setPathOrName(final String pathOrName) {
		this.pathOrName = pathOrName;
	}

	/**
	 * Return an enum that differentiates between collection and data object
	 * 
	 * @return <code>ObjectType</code> enum value
	 */
	public ObjectType getObjectType() {
		return objectType;
	}

	public void setObjectType(final ObjectType objectType) {
		this.objectType = objectType;
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

	public long getDataSize() {
		return dataSize;
	}

	public void setDataSize(final long dataSize) {
		this.dataSize = dataSize;
	}

	/**
	 * Handy method gets a displayable <code>String</code> with a unit (e.g. MB,
	 * GB) appropriate to the file length
	 * 
	 * @return <code>String</code> with displayable file size
	 */
	public String getDisplayDataSize() {
		// return FileUtils.byteCountToDisplaySize(dataSize);
		return MiscIRODSUtils.humanReadableByteCount(dataSize);
	}

	public int getId() {
		return id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof CollectionAndDataObjectListingEntry)) {
			return false;
		}
		CollectionAndDataObjectListingEntry otherEntry = (CollectionAndDataObjectListingEntry) obj;
		return (otherEntry.parentPath.equals(parentPath) && otherEntry.pathOrName
				.equals(pathOrName));
	}

	@Override
	public int hashCode() {
		return parentPath.hashCode() + pathOrName.hashCode();
	}

	@Override
	public String toString() {
		String thisPath = pathOrName.substring(pathOrName.lastIndexOf('/') + 1);

		if (thisPath.isEmpty()) {
			thisPath = "/";
		}

		return thisPath;
	}

	/**
	 * Handy method that will compute the appropriate absolute path, whether a
	 * data object or a collection. This will be the display path. In the case
	 * of a soft-linked directory, this is the soft link target, rather then the
	 * canonical source directory. This is appropriate as listings under a soft
	 * linked directory should be in terms of the parent directory, but may not
	 * give the intended result if used to query the iCAT for information about
	 * the file or collection.
	 * 
	 * @return
	 */
	public String getFormattedAbsolutePath() {
		StringBuilder sb = new StringBuilder();
		if (objectType == ObjectType.COLLECTION) {
			sb.append(pathOrName);
		} else {
			sb.append(parentPath);
			sb.append("/");
			sb.append(pathOrName);
		}

		return sb.toString();
	}

	/**
	 * Tree nodes typically want a short name for the subdirectory or data name
	 * to display. Obtain a descriptive name for the collection (the last path
	 * component with no /'s), or the data object name (with no /'s). This
	 * method will eat any errors and make a best effort to return something
	 * meaningful.
	 * 
	 * @return <code>String</code> with a value suitable for a node name in a
	 *         tree.
	 */
	public String getNodeLabelDisplayValue() {
		String nodeVal;
		if (objectType == ObjectType.COLLECTION) {
			nodeVal = MiscIRODSUtils
					.getLastPathComponentForGiveAbsolutePath(getPathOrName());
		} else {
			nodeVal = pathOrName;
		}
		return nodeVal;

	}

	public boolean isCollection() {
		return (objectType == ObjectType.COLLECTION);
	}

	public boolean isDataObject() {
		return (objectType == ObjectType.DATA_OBJECT);
	}

	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(final String ownerName) {
		this.ownerName = ownerName;
	}

	/**
	 * Gets the permissions associated with the collection or data object. Note
	 * that this information is not retrieved in some of the query methods
	 * within Jargon, so make sure that a method that adds user permissions is
	 * called. In other cases, this collection will be empty.
	 * 
	 * @return <code>List</code> of {@link UserFilePermission} with the per-user
	 *         ACL information, included if explicity requested from Jargon,
	 *         otherwise, empty
	 */
	public List<UserFilePermission> getUserFilePermission() {
		return userFilePermission;
	}

	public void setUserFilePermission(
			final List<UserFilePermission> userFilePermission) {
		this.userFilePermission = userFilePermission;
	}

	@Override
	public int compareTo(final CollectionAndDataObjectListingEntry obj) {
		return getFormattedAbsolutePath().compareTo(
				(obj).getFormattedAbsolutePath());
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
	 * @return the specColType {@link ObjStat.SpecColType} enum value that
	 *         indicates if this is some type of special collection
	 */
	public ObjStat.SpecColType getSpecColType() {
		return specColType;
	}

	/**
	 * @param specColType
	 *            the specColType to set {@link ObjStat.SpecColType} enum value
	 *            that indicates if this is some type of special collection
	 */
	public void setSpecColType(final ObjStat.SpecColType specColType) {
		this.specColType = specColType;
	}

	/**
	 * @return the specialObjectPath <code>String</code> with the underlyng
	 *         special object path. If this is a soft link, this reflects the
	 *         canonical iRODS path.
	 */
	public String getSpecialObjectPath() {
		return specialObjectPath;
	}

	/**
	 * @param specialObjectPath
	 *            the specialObjectPath to set <code>String</code> with the
	 *            underlyng special object path.
	 */
	public void setSpecialObjectPath(final String specialObjectPath) {
		this.specialObjectPath = specialObjectPath;
	}

	public String getDescription() {
		return description;
	}
}
