/**
 *
 */
package org.irods.jargon.core.packinstr;

/**
 * Encapsulates values to use in a SpecColInfo packing instruction tag
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class SpecColInfo {

	/**
	 * Flag that indicates whether resource heirarchy is included in sending
	 * data to iRODS, to accomodate protocol differences in 4.x
	 */
	private boolean useResourceHierarchy = false;
	private int collClass = 0;
	private int type = 0;
	private String collection = "";
	private String objPath = "";
	private String resource = "";
	private String phyPath = "";
	private String cacheDir = "";
	private int cacheDirty = 0;
	private int replNum = 0;

	public int getCollClass() {
		return collClass;
	}

	public void setCollClass(final int collClass) {
		this.collClass = collClass;
	}

	public int getType() {
		return type;
	}

	public void setType(final int type) {
		this.type = type;
	}

	public String getCollection() {
		return collection;
	}

	public void setCollection(final String collection) {
		this.collection = collection;
	}

	public String getObjPath() {
		return objPath;
	}

	public void setObjPath(final String objPath) {
		this.objPath = objPath;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(final String resource) {
		this.resource = resource;
	}

	public String getPhyPath() {
		return phyPath;
	}

	public void setPhyPath(final String phyPath) {
		this.phyPath = phyPath;
	}

	public String getCacheDir() {
		return cacheDir;
	}

	public void setCacheDir(final String cacheDir) {
		this.cacheDir = cacheDir;
	}

	public int getCacheDirty() {
		return cacheDirty;
	}

	public void setCacheDirty(final int cacheDirty) {
		this.cacheDirty = cacheDirty;
	}

	public int getReplNum() {
		return replNum;
	}

	public void setReplNum(final int replNum) {
		this.replNum = replNum;
	}

	public boolean isUseResourceHierarchy() {
		return useResourceHierarchy;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SpecColInfo [useResourceHierarchy=");
		builder.append(useResourceHierarchy);
		builder.append(", collClass=");
		builder.append(collClass);
		builder.append(", type=");
		builder.append(type);
		builder.append(", ");
		if (collection != null) {
			builder.append("collection=");
			builder.append(collection);
			builder.append(", ");
		}
		if (objPath != null) {
			builder.append("objPath=");
			builder.append(objPath);
			builder.append(", ");
		}
		if (resource != null) {
			builder.append("resource=");
			builder.append(resource);
			builder.append(", ");
		}
		if (phyPath != null) {
			builder.append("phyPath=");
			builder.append(phyPath);
			builder.append(", ");
		}
		if (cacheDir != null) {
			builder.append("cacheDir=");
			builder.append(cacheDir);
			builder.append(", ");
		}
		builder.append("cacheDirty=");
		builder.append(cacheDirty);
		builder.append(", replNum=");
		builder.append(replNum);
		builder.append("]");
		return builder.toString();
	}

	public void setUseResourceHierarchy(final boolean useResourceHierarchy) {
		this.useResourceHierarchy = useResourceHierarchy;
	}

}
