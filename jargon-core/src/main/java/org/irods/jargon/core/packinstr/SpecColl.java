/**
 * 
 */
package org.irods.jargon.core.packinstr;

import org.irods.jargon.core.exception.JargonException;

/**
 * Analogue of SpecColl_PI
 * 
 * @author Mike Conway - DFC
 *
 */
public class SpecColl extends AbstractIRODSPackingInstruction {

	private int collClass = 0;
	private int type = 0;
	private String collection = "";
	private String objPath = "";
	private String resource = "";
	private String rescHier = "";
	private String phyPath = "";
	private String cacheDir = "";
	private int cacheDirty = 0;
	private int replNum = 0;

	/*
	 * #define SpecColl_PI
	 * "int collClass; int type; str collection[MAX_NAME_LEN]; str objPath[MAX_NAME_LEN]; str resource[NAME_LEN]; str rescHier[MAX_NAME_LEN]; str phyPath[MAX_NAME_LEN]; str cacheDir[MAX_NAME_LEN]; int cacheDirty; int replNum;"
	 */

	/**
	 * 
	 */
	public SpecColl() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.packinstr.AbstractIRODSPackingInstruction#getTagValue
	 * ()
	 */
	@Override
	public Tag getTagValue() throws JargonException {
		return null;
	}

	public int getCollClass() {
		return collClass;
	}

	public void setCollClass(int collClass) {
		this.collClass = collClass;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getCollection() {
		return collection;
	}

	public void setCollection(String collection) {
		this.collection = collection;
	}

	public String getObjPath() {
		return objPath;
	}

	public void setObjPath(String objPath) {
		this.objPath = objPath;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public String getRescHier() {
		return rescHier;
	}

	public void setRescHier(String rescHier) {
		this.rescHier = rescHier;
	}

	public String getPhyPath() {
		return phyPath;
	}

	public void setPhyPath(String phyPath) {
		this.phyPath = phyPath;
	}

	public String getCacheDir() {
		return cacheDir;
	}

	public void setCacheDir(String cacheDir) {
		this.cacheDir = cacheDir;
	}

	public int getCacheDirty() {
		return cacheDirty;
	}

	public void setCacheDirty(int cacheDirty) {
		this.cacheDirty = cacheDirty;
	}

	public int getReplNum() {
		return replNum;
	}

	public void setReplNum(int replNum) {
		this.replNum = replNum;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SpecColl [collClass=").append(collClass)
				.append(", type=").append(type).append(", ");
		if (collection != null) {
			builder.append("collection=").append(collection).append(", ");
		}
		if (objPath != null) {
			builder.append("objPath=").append(objPath).append(", ");
		}
		if (resource != null) {
			builder.append("resource=").append(resource).append(", ");
		}
		if (rescHier != null) {
			builder.append("rescHier=").append(rescHier).append(", ");
		}
		if (phyPath != null) {
			builder.append("phyPath=").append(phyPath).append(", ");
		}
		if (cacheDir != null) {
			builder.append("cacheDir=").append(cacheDir).append(", ");
		}
		builder.append("cacheDirty=").append(cacheDirty).append(", replNum=")
				.append(replNum).append("]");
		return builder.toString();
	}

}
