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

	// FIXME: clean up, comment, toString, think about enums
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

}
