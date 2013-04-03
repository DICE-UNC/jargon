/**
 * 
 */
package org.irods.jargon.workflow.wso;

import org.irods.jargon.core.pub.domain.Collection;
import org.irods.jargon.core.pub.domain.IRODSDomainObject;

/**
 * Represents a WSO (Workflow Structured Object) in iRODS.
 * <p/>
 * One can view the WSO akin to an iRODS collection with a hierarchical
 * structure. At the top level of this structures, one stores all the parameter
 * files needed to run the workflow, as well as any input files and manifest
 * files that are needed for the workflow execution. Beneath this level, is
 * stored a set of run directories which actually house the results of an
 * execution. Hence, one can view the WSO as a complete structure that captures
 * all aspects of a workflow execution. In iRODS the WSO is created as a mount
 * point in the iRODS logical collection hierarchy.
 * <p/>
 * Jargon views a WSO from the perspective of the mounted collection associated
 * with the .mss file. Within the iCAT, information is stored in this collection
 * catalog data that indicates the related .mss
 * <p/>
 * This is a plain POJO value object and is mutable, so it is not thread safe
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class WorkflowStructuredObject extends IRODSDomainObject {

	/**
	 * The base information from the ICAT for the given WSO mounted collection
	 */
	private Collection collection = null;

	/**
	 * The iRODS absolute path to the .mss file that defines the workflow
	 * template
	 */
	private String mssFileAbsolutePath = "";

	/**
	 * The iRODS absolute path to the cache directory used for this .mss file
	 */
	private String mssCacheDirPath = "";

	/**
	 * A <code>String</code> representation of the contents of the .mss file
	 * associated with this collection
	 */
	private String mssAsText = "";

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("WorkflowStructuredObject");
		sb.append("\n\t collection:");
		sb.append(collection);
		sb.append("\n\t  mssFileAbsolutePath:");
		sb.append(mssFileAbsolutePath);
		sb.append("\n\t mssCacheDirPath:");
		sb.append(mssCacheDirPath);
		sb.append("\n\t mssAsText:");
		sb.append(mssAsText);
		return sb.toString();
	}

	public Collection getCollection() {
		return collection;
	}

	public void setCollection(Collection collection) {
		this.collection = collection;
	}

	public String getMssFileAbsolutePath() {
		return mssFileAbsolutePath;
	}

	public void setMssFileAbsolutePath(String mssFileAbsolutePath) {
		this.mssFileAbsolutePath = mssFileAbsolutePath;
	}

	public String getMssCacheDirPath() {
		return mssCacheDirPath;
	}

	public void setMssCacheDirPath(String mssCacheDirPath) {
		this.mssCacheDirPath = mssCacheDirPath;
	}

	public String getMssAsText() {
		return mssAsText;
	}

	public void setMssAsText(String mssAsText) {
		this.mssAsText = mssAsText;
	}

}
