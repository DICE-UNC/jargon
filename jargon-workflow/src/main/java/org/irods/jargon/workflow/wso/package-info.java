/**
 * Supoort for WSO (workflow structured objects) within iRODS.  
 * <p/>
 * One can view the WSO akin to an iROS collection with a hierarchical structure. At the top level of this structures, 
 * one stores all the parameter files needed to run the workflow, as well as any input files and manifest files that are 
 * needed for the workflow execution. Beneath this level, is stored a set of run directories which actually house the 
 * results of an execution. Hence, one can view the WSO as a complete structure that captures all aspects of a 
 * workflow execution. 
 * <p/>
 * In iRODS the WSO is created as a mount point in the iRODS logical collection hierarchy. This is similar to a mounted 
 * collection but of type "msso". See Mounted iRODS Collection for more details. One uses imcoll command to 
 * create this mount point. We use WSO and MSSO (micro-service structured object" synonymously for historic reasons 
 * since the need and idea for WSO/MSSO came from the usage experience for Micro-Service Objects (MSO).
 * Apart from the workflow file there is one other important file called the parameter file (with dot-extension '.mpf') 
 * which contains information needed for executing the workflow. We separated the parameter file from the workflow file such that 
 * one can associate multiple parameter files with a workflow and use them for executing with different values. 
 * The parameter files contains values for *-variables that need to be used in the workflow execution. 
 * It also contains information about files that need to staged in before the execution and staged out for archive after 
 * the the execution. It also contains directives for the workflow execution engine. 
 * <p/>
 * The parameter files as well as any input files can be ingested into the WSO using normal icommands such as iput.
 * When a parameter file is ingested into a WSO, a run file is automatically created which can be used to run the 
 * parameter file with the associated workflow. When a workflow execution occurs a run directory is created for 
 * storing the results of this run. Depending upon the directives in the parameter file, older results are versioned 
 * out or discarded after a successful workflow execution. These version directories can be listed and accessed using the 
 * normal icommands such as ils and iget.
 * 
 * 
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
package org.irods.jargon.workflow.wso;