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

}
