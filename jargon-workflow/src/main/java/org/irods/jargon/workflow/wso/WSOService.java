package org.irods.jargon.workflow.wso;

import org.irods.jargon.workflow.mso.exception.WSOException;
import org.irods.jargon.workflow.mso.exception.WSONotFoundException;

/**
 * Interface for a WSO Service.
 * <p/>
 * One can view the WSO akin to an iROS collection with a hierarchical
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
 * catalog data that indicates the related .mss.
 * <p/>
 * This service allows for specification, query, and invocation of WSOs.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface WSOService {

	/**
	 * Given an absolute path to a structured collection (mounted collection) in
	 * iRODS that was associated with a workflow (.mss file), return data about
	 * the workflow.
	 * 
	 * @param irodsAbsolutePathToWSOMountedCollection
	 *            <code>String</code> with the absolute path to the iRODS
	 *            structured file collection associated with a .mss, and mounted
	 *            using the imcoll or jargon mount method.
	 * @return {@link WorkflowStructuredObject} representing the WSO
	 * @throws WSONotFoundException
	 *             if any or all of the components of the WSO are missing or
	 *             malformed
	 * @throws WSOException
	 */
	WorkflowStructuredObject findWSOForCollectionPath(
			String irodsAbsolutePathToWSOMountedCollection)
			throws WSONotFoundException, WSOException;

}