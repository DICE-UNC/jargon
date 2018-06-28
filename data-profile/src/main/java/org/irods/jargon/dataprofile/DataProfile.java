/**
 * 
 */
package org.irods.jargon.dataprofile;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.pub.domain.IRODSDomainObject;
import org.irods.jargon.core.pub.domain.UserFilePermission;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.usertagging.domain.IRODSTagValue;

/**
 * Represents a consolidated summary of a data object or collection as a simple
 * POJO
 * 
 * @author Mike Conway - DICE
 *
 */
public class DataProfile<T extends IRODSDomainObject> {

	private boolean file = false;
	private T domainObject;
	private List<MetaDataAndDomainData> metadata = new ArrayList<MetaDataAndDomainData>();
	private List<UserFilePermission> acls = new ArrayList<UserFilePermission>();
	private boolean isStarred = false;
	private boolean isShared = false;
	private String mimeType = "";
	private String infoType = "";
	private List<IRODSTagValue> irodsTagValues = new ArrayList<IRODSTagValue>();

	/**
	 * parent of the current data object
	 */
	private String parentPath = "";
	/**
	 * child name (last path component)
	 */
	private String childName = "";
	/**
	 * List of path components suitable for generating breadcrumbs and the like
	 */
	private List<String> pathComponents = null;

	public void setDomainObject(T domainObject) {
		this.domainObject = domainObject;
	}

	public T getDomainObject() {
		return domainObject;
	}

	/**
	 * 
	 */
	public DataProfile() {
	}

	public List<MetaDataAndDomainData> getMetadata() {
		return metadata;
	}

	public List<UserFilePermission> getAcls() {
		return acls;
	}

	public boolean isStarred() {
		return isStarred;
	}

	public boolean isShared() {
		return isShared;
	}

	public void setMetadata(List<MetaDataAndDomainData> metadata) {
		this.metadata = metadata;
	}

	public void setAcls(List<UserFilePermission> acls) {
		this.acls = acls;
	}

	public void setStarred(boolean isStarred) {
		this.isStarred = isStarred;
	}

	public void setShared(boolean isShared) {
		this.isShared = isShared;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public boolean isFile() {
		return file;
	}

	public void setFile(boolean file) {
		this.file = file;
	}

	public List<IRODSTagValue> getIrodsTagValues() {
		return irodsTagValues;
	}

	public void setIrodsTagValues(List<IRODSTagValue> irodsTagValues) {
		this.irodsTagValues = irodsTagValues;
	}

	/**
	 * @return the parentPath
	 */
	public String getParentPath() {
		return parentPath;
	}

	/**
	 * @param parentPath
	 *            the parentPath to set
	 */
	public void setParentPath(String parentPath) {
		this.parentPath = parentPath;
	}

	/**
	 * @return the childName
	 */
	public String getChildName() {
		return childName;
	}

	/**
	 * @param childName
	 *            the childName to set
	 */
	public void setChildName(String childName) {
		this.childName = childName;
	}

	/**
	 * @return the pathComponents
	 */
	public List<String> getPathComponents() {
		return pathComponents;
	}

	/**
	 * @param pathComponents
	 *            the pathComponents to set
	 */
	public void setPathComponents(List<String> pathComponents) {
		this.pathComponents = pathComponents;
	}

	@Override
	public String toString() {
		final int maxLen = 10;
		StringBuilder builder = new StringBuilder();
		builder.append("DataProfile [file=").append(file).append(", domainObject=").append(domainObject)
				.append(", metadata=")
				.append(metadata != null ? metadata.subList(0, Math.min(metadata.size(), maxLen)) : null)
				.append(", acls=").append(acls != null ? acls.subList(0, Math.min(acls.size(), maxLen)) : null)
				.append(", isStarred=").append(isStarred).append(", isShared=").append(isShared).append(", mimeType=")
				.append(mimeType).append(", infoType=").append(infoType).append(", irodsTagValues=")
				.append(irodsTagValues != null ? irodsTagValues.subList(0, Math.min(irodsTagValues.size(), maxLen))
						: null)
				.append(", parentPath=").append(parentPath).append(", childName=").append(childName)
				.append(", pathComponents=")
				.append(pathComponents != null ? pathComponents.subList(0, Math.min(pathComponents.size(), maxLen))
						: null)
				.append("]");
		return builder.toString();
	}

	/**
	 * @return the infoType
	 */
	public String getInfoType() {
		return infoType;
	}

	/**
	 * @param infoType
	 *            the infoType to set
	 */
	public void setInfoType(String infoType) {
		this.infoType = infoType;
	}

}
