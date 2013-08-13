package org.irods.jargon.workflow.wso;

import java.io.InputStream;

import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.workflow.mso.exception.WSOException;
import org.irods.jargon.workflow.mso.exception.WSONotFoundException;

/**
 * Interface for a WSO Service.
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
 * catalog data that indicates the related .mss.
 * <p/>
 * This service allows for specification, query, and invocation of WSOs.
 * <p/>
 * see: https://www.irods.org/index.php/Workflow_Objects_(WSO)
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

	/**
	 * Given a .mss workflow file, put it into iRODS and mount a workflow
	 * collection for processing.
	 * <p/>
	 * This method takes a local file path to the mso object that will be 'put'
	 * to iRODS as an mso file. Then the given collection is 'mounted' as a WSSO
	 * given the provided path to the desired collection, and the .wss file that
	 * was just.
	 * 
	 * @param absolutePathToTheMSSOToBeMounted
	 *            <code>String</code> with the absolute path to msso structured
	 *            object to mount
	 * @param absolutePathToMountedCollection
	 *            <code>String</code> with the absolute path to the iRODS
	 *            collection that will be created based on the MSSO service
	 * @throws FileNotFoundException
	 * @throws JargonException
	 */
	void createNewWorkflow(String absoluteLocalPathToWssFile,
			String absoluteIRODSTargetPathToTheWssToBeMounted,
			String absolutePathToMountedCollection)
			throws FileNotFoundException, JargonException;

	/**
	 * Convenience method will remove both the workflow mounted collection, as
	 * well as the .mss file used to mount the workflow collection. Note that
	 * this delete method will return a <code>WSONotFoundException</code> if it
	 * cannot find the mounted workflow file.
	 * 
	 * @param absolutePathToMountedWorkflowCollection
	 *            <code>String</code> with the absolute path to msso structured
	 *            object to mount for the workflow. This is the special
	 *            collection that holds the workflow data
	 * @throws WSONotFoundException
	 *             if the workflow information is not found
	 * @throws WSOException
	 */
	void removeWorkflowFileAndMountedCollection(
			final String absolutePathToMountedWorkflowCollection)
			throws WSONotFoundException, WSOException;

	/**
	 * Ingest the given workflow parameter file, causing the workflow to run
	 * 
	 * @param workflowParameterLocalFileAbsolutePath
	 *            <code>String</code> with the absolute path to a workflow
	 *            parameter file on the local file system.
	 * @param absolutePathToMountedWorkflowCollection
	 *            <code>String</code> with the iRODS mounted collection path
	 *            associated with a workflow.
	 * @throws WSONotFoundException
	 * @throws WSOException
	 */
	void ingestLocalParameterFileIntoWorkflow(
			String workflowParameterLocalFileAbsolutePath,
			String absolutePathToMountedWorkflowCollection)
			throws WSONotFoundException, WSOException;

	/**
	 * Ingest the given workflow parameter file as a stream. This convenience
	 * method will stream the content to a file given a file name (not an
	 * absolute path), the parameter file will be placed in the mounted workflow
	 * collection
	 * 
	 * @param targetParameterFileName
	 *            <code>String</code> with the unique (must not exist) parameter
	 *            file name
	 * @param workflowParameterFileInputStream
	 *            <code>InputStream</code> that will be streamed to the mounted
	 *            workflow collection. Note that this method will wrap the
	 *            stream in a buffer for you
	 * @param absolutePathToMountedWorkflowCollection
	 *            <code>String</code> with the iRODS mounted collection path
	 *            associated with a workflow.
	 * @throws WSONotFoundException
	 * @throws WSOException
	 */
	void ingestLocalParameterFileIntoWorkflow(String targetParameterFileName,
			InputStream workflowParameterFileInputStream,
			String absolutePathToMountedWorkflowCollection)
			throws WSONotFoundException, WSOException;

}