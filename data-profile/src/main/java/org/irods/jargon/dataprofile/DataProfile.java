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
	private List<IRODSTagValue> irodsTagValues = new ArrayList<IRODSTagValue>();

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

}
