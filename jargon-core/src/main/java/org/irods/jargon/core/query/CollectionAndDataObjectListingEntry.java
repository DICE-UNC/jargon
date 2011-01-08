package org.irods.jargon.core.query;

import java.util.Date;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.domain.IRODSDomainObject;

/**
 * Value object that holds information on data objects and collections. This
 * object includes info to distinguish between data object and collection, to
 * identify it in iRODS by path, and also information that can be used for
 * paging.
 * <p/>
 * This object is meant to be used for use cases such as iRODS file tree browsing, and as such it
 * is meant to be returned in collections. The behavior of these objects in the
 * collection is such that objects for collections and objects for data objects
 * separately hold counts and indicators that this particular collection or
 * object is the last result. In this way, a caller gets a result that can say
 * that all the collections have been returned, but the data objects may have
 * more results to page.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class CollectionAndDataObjectListingEntry extends IRODSDomainObject {

	public enum ObjectType {
		COLLECTION, DATA_OBJECT
	}

	private String parentPath = "";
	private String pathOrName = "";
	private ObjectType objectType = null;
	private Date createdAt = null;
	private Date modifiedAt = null;
	private long dataSize = 0L;
	private int id;

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
	 * Utility method to get the last part of the collection path component.
	 * Returns the last subdir that is the collection.
	 * 
	 * @return <code>String</code> with the last part of the collection path.
	 * @throws JargonException
	 *             returned if this method is called on a data object.
	 */
	public String getLastPathComponentForCollectionName()
			throws JargonException {
		if (objectType != ObjectType.COLLECTION) {
			throw new JargonException(
					"this is not a collection, cannot get last component for collection name");
		}

		String[] paths = pathOrName.split("/");
		if (paths.length == 0) {
			return "";
		}

		return paths[paths.length - 1];

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
	 * Handy method that will compute the appropriate absolute path, whether a data object or a collection.
	 * @return
	 */
	public String getFormattedAbsolutePath() {
		StringBuilder sb = new StringBuilder();
		if (objectType == ObjectType.COLLECTION) {
			sb.append(pathOrName);
		} else {
			sb.append(parentPath);
			sb.append('/');
			sb.append(pathOrName);
		}
		
		return sb.toString();
	}
	
	/**
	 * Tree nodes typically want a short name for the subdirectory or data name to display.  Obtain a descriptive name for the 
	 * collection (the last path component with no /'s), or the data object
	 * name (with no /'s).  This method will eat any errors and make a best effort to return something meaningful.
	 * @return <code>String</code> with a value suitable for a node name in a tree.
	 */
	public String getNodeLabelDisplayValue() {
		String nodeVal;
		if (objectType == ObjectType.COLLECTION) {
			try {
				nodeVal = getLastPathComponentForCollectionName();
			} catch (JargonException e) {
				nodeVal = toString();
			}
		} else {
			nodeVal = pathOrName;
		}
		return nodeVal;
		
	}
	
	
	public boolean isCollection() {
		return(objectType == ObjectType.COLLECTION);
	}
	
	public boolean isDataObject() {
		return(objectType == ObjectType.DATA_OBJECT);
	}

}
