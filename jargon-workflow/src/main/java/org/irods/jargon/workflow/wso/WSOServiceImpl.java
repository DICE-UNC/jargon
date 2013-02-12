/**
 * 
 */
package org.irods.jargon.workflow.wso;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.Stream2StreamAO;
import org.irods.jargon.core.pub.domain.ObjStat.SpecColType;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.service.AbstractJargonService;
import org.irods.jargon.workflow.mso.exception.WSOException;
import org.irods.jargon.workflow.mso.exception.WSONotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service implementation to interact with iRODS WSO (Workflow Service Objects).
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
public class WSOServiceImpl extends AbstractJargonService implements WSOService {

	public static final Logger log = LoggerFactory
			.getLogger(WSOServiceImpl.class);

	/**
	 * @param irodsAccessObjectFactory
	 *            {@link IRODSAccessObjectFactory} from which other iRODS
	 *            service objects may be obtained
	 * @param irodsAccount
	 *            {@link IRODSAccount} with credentials used to interact with an
	 *            iRODS server
	 */
	public WSOServiceImpl(IRODSAccessObjectFactory irodsAccessObjectFactory,
			IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.workflow.wso.WSOService#findWSOForCollectionPath(java
	 * .lang.String)
	 */
	@Override
	public WorkflowStructuredObject findWSOForCollectionPath(
			final String irodsAbsolutePathToWSOMountedCollection)
			throws WSONotFoundException, WSOException {

		log.info("findWSOForCollectionPath()");
		if (irodsAbsolutePathToWSOMountedCollection == null
				|| irodsAbsolutePathToWSOMountedCollection.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePathToWSOMountedCollection");
		}

		log.info("irodsAbsolutePathToWSOMountedCollection:{}",
				irodsAbsolutePathToWSOMountedCollection);

		WorkflowStructuredObject wso = buildWSOGivenWSOMountedCollectionPath(irodsAbsolutePathToWSOMountedCollection);

		log.info("my wso:{}", wso);
		return wso;

	}

	/**
	 * Given an absolute path to the mounted collection for a WSO, return an
	 * object containing data about the WSO
	 * 
	 * @param irodsAbsolutePathToWSOMountedCollection
	 *            <code>String</code> with the absolute path to mounted
	 *            collection associated with the .mss file
	 * @return {@link WorkflowStructuredObject} based on iRODS WSO information
	 * @throws WSOException
	 * @throws WSONotFoundException
	 */
	private WorkflowStructuredObject buildWSOGivenWSOMountedCollectionPath(
			final String irodsAbsolutePathToWSOMountedCollection)
			throws WSOException, WSONotFoundException {
		CollectionAO collectionAO;
		try {
			collectionAO = irodsAccessObjectFactory
					.getCollectionAO(irodsAccount);
		} catch (JargonException e) {
			throw new WSOException(e);
		}

		WorkflowStructuredObject wso = new WorkflowStructuredObject();

		try {
			log.info("looking up collection for wso...");
			wso.setCollection(collectionAO
					.findByAbsolutePath(irodsAbsolutePathToWSOMountedCollection));

		} catch (DataNotFoundException dnf) {
			log.error("cannot find a collection at the given absolute path:{}",
					irodsAbsolutePathToWSOMountedCollection);
			throw new WSONotFoundException("no collection found");
		} catch (JargonException je) {
			throw new WSOException(je);
		}

		/*
		 * I've found the collection in iRODS, make sure it's a WSO and parse
		 * out all of the relevant info
		 */

		log.info("found collection, make sure it is a wso and parse it out...");
		if (wso.getCollection().getSpecColType() != SpecColType.STRUCT_FILE_COLL) {
			log.error(
					"not a structured file collection, has specColType of:{}",
					wso.getCollection().getSpecColType());
			throw new WSONotFoundException(
					"given collection is not a wso, not a struct file coll");

		}

		log.info("am I a workflow?, look for .mss in info1...");
		if (wso.getCollection().getInfo1().indexOf(".mss") == -1) {
			log.error(
					"not a structured file collection, has specColType of:{}",
					wso.getCollection().getSpecColType());
			throw new WSONotFoundException(
					"given collection is not a wso, not a struct file coll");

		}

		wso.setMssFileAbsolutePath(wso.getCollection().getInfo1().trim());

		log.info("parsing info2 to get the cache dir path...");

		/*
		 * The info2 holds a bunch of ; delim stuff that I need to understand
		 * more, for now get the cache dir...it looks like this:
		 * /opt/iRODS/iRODS3.2/Vault1/home/test1/jargon-scratch/
		 * CollectionAndDataObjectListAndSearchAOImplForMSSOTest
		 * /testGetFullObjectForTypeInTestWorkflow
		 * /eCWkflow.mss.cacheDir2;;;test1-resc;;;1
		 */

		if (!wso.getCollection().getInfo2().isEmpty()) {
			log.info("has a previous run, get that info from info2");
			int idx = wso.getCollection().getInfo2().indexOf(';');
			if (idx > -1) {
				wso.setMssCacheDirPath(wso.getCollection().getInfo2()
						.substring(0, idx));
			}
		}

		wso.setMssAsText(obtainMSSTextForWSO(wso.getMssFileAbsolutePath()));
		return wso;
	}

	/**
	 * Given a path to an mss file, return back the mss contents as a
	 * <code>String</code>
	 * 
	 * @param mssFileAbsolutePath
	 *            <code>String</code> with the absolute path to the .mss file
	 * @return <code>String</code> with the contents of the mss file
	 * @throws WSONotFoundException
	 * @throws WSOException
	 */
	private String obtainMSSTextForWSO(final String mssFileAbsolutePath)
			throws WSONotFoundException, WSOException {
		log.info(
				"now look up the mss file and save the data as a string, using path:{}",
				mssFileAbsolutePath);

		try {
			Stream2StreamAO stream2StreamAO = this
					.getIrodsAccessObjectFactory().getStream2StreamAO(
							irodsAccount);
			IRODSFile mssFile = this.getIrodsAccessObjectFactory()
					.getIRODSFileFactory(irodsAccount)
					.instanceIRODSFile(mssFileAbsolutePath);

			if (!mssFile.exists()) {
				log.error(
						"cannot find the mss file for this collection, should be at path:{}",
						mssFileAbsolutePath);
				throw new WSONotFoundException("mss file for wso not found");
			}

			byte[] mssAsByte = stream2StreamAO.streamFileToByte(mssFile);
			String mssText = new String(mssAsByte);
			log.info("got the mss text, we are all set...");
			return mssText;

		} catch (JargonException e) {
			throw new WSOException(e);
		}
	}
}
