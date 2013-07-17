/**
 * 
 */
package org.irods.jargon.core.pub;

import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.DataObjInp;
import org.irods.jargon.core.packinstr.Tag;
import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.core.pub.domain.Resource;
import org.irods.jargon.core.pub.domain.UserFilePermission;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Access object representing the file catalog in iRODS. This object is the
 * parent of access objects that deal with iRODS collections (directories) and
 * data objects (files), and contains common operations for both.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public abstract class FileCatalogObjectAOImpl extends IRODSGenericAO implements
		FileCatalogObjectAO {

	protected transient final CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO;

	public static final Logger log = LoggerFactory
			.getLogger(FileCatalogObjectAOImpl.class);
	public static final String STR_PI = "STR_PI";
	public static final String MY_STR = "myStr";
	/**
	 * value returned from 'get host for get operation' indicating that the
	 * given host address should be used
	 */

	public static final String USE_THIS_ADDRESS = "thisAddress";

	/**
	 * @param irodsSession
	 * @param irodsAccount
	 * @throws JargonException
	 */
	protected FileCatalogObjectAOImpl(final IRODSSession irodsSession,
			final IRODSAccount irodsAccount) throws JargonException {
		super(irodsSession, irodsAccount);
		this.collectionAndDataObjectListAndSearchAO = this
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.FileCatalogObjectAO#getHostForGetOperation(
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public String getHostForGetOperation(final String sourceAbsolutePath,
			final String resourceName) throws JargonException {

		if (sourceAbsolutePath == null || sourceAbsolutePath.length() == 0) {
			throw new IllegalArgumentException(
					"Null or empty sourceAbsolutePath");
		}

		if (resourceName == null) {
			throw new IllegalArgumentException("null resourceName");
		}

		log.info("getHostForGetOperation with sourceAbsolutePath: {}",
				sourceAbsolutePath);
		log.info("resourceName:{}", resourceName);

		/*
		 * If resource is specified, then the call for getHostForGet() will
		 * return the correct resource server, otherwise, I need to see if this
		 * is a data object. When a data object is being obtained, look to iRODS
		 * to find the resources that data object is located on and pick the
		 * first one
		 */

		if (resourceName.isEmpty()) {
			IRODSFile fileToGet = this.getIRODSFileFactory().instanceIRODSFile(
					sourceAbsolutePath);
			if (fileToGet.isFile()) {
				log.debug("this is a file, look for resource it is stored on to retrieve host");
				DataObjectAO dataObjectAO = this.getIRODSAccessObjectFactory()
						.getDataObjectAO(getIRODSAccount());
				List<Resource> resources = dataObjectAO
						.getResourcesForDataObject(fileToGet.getParent(),
								fileToGet.getName());
				if (resources.isEmpty()) {
					return null;
				} else {
					// if the file is on the same host, just use this
					String thisHostName = this.getIRODSAccount().getHost();
					for (Resource resource : resources) {
						if (resource.getLocation().equals(thisHostName)) {
							log.info("file replica is on current host:{}",
									thisHostName);
							return null;
						}
					}

					// not on same host, find another
					log.info("file is not on this host, so reroute to another host (first found)");
					return resources.get(0).getLocation();

				}
			}
		}

		/*
		 * Did not locate a resource based on a data object location, ask iRODS
		 */

		DataObjInp dataObjInp = DataObjInp.instanceForGetHostForGet(
				sourceAbsolutePath, resourceName);
		return evaluateGetHostResponseAndReturnReroutingHost(dataObjInp);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.FileCatalogObjectAO#getHostForPutOperation(
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public String getHostForPutOperation(final String targetAbsolutePath,
			final String resourceName) throws JargonException {

		if (targetAbsolutePath == null || targetAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"Null or empty targetAbsolutePath");
		}

		if (resourceName == null) {
			throw new IllegalArgumentException("null resourceName");
		}

		log.info("getHostForPutOperation with targetAbsolutePath: {}",
				targetAbsolutePath);
		log.info("resourceName:{}", resourceName);

		DataObjInp dataObjInp = DataObjInp.instanceForGetHostForPut(
				targetAbsolutePath, resourceName);
		return evaluateGetHostResponseAndReturnReroutingHost(dataObjInp);
	}

	/**
	 * Send a get host for get/put request to iRODS and evaluate the returned
	 * host. It will either be a host name, or null, indicating no re-routing
	 * needed.
	 * 
	 * @param dataObjInp
	 * @return
	 * @throws JargonException
	 */
	private String evaluateGetHostResponseAndReturnReroutingHost(
			final DataObjInp dataObjInp) throws JargonException {
		Tag result = this.getIRODSProtocol().irodsFunction(dataObjInp);

		// irods file doesn't exist
		if (result == null) {
			throw new JargonException(
					"null response from lookup of resource for get operation");
		}

		// Need the total dataSize
		Tag temp = result.getTag(MY_STR);
		if (temp == null) {
			throw new JargonException(
					"no host name info in response to lookup of resource for get operation");
		}

		String hostResponse = temp.getStringValue();

		log.debug("result of get host lookup:{}", hostResponse);

		if (hostResponse.equals(USE_THIS_ADDRESS)) {
			log.info("return null indicating no host rerouting");
			hostResponse = null;
		}

		return hostResponse;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.FileCatalogObjectAO#getObjectStatForAbsolutePath
	 * (java.lang.String)
	 */
	@Override
	public ObjStat getObjectStatForAbsolutePath(final String irodsAbsolutePath)
			throws FileNotFoundException, JargonException {
		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = this
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(getIRODSAccount());
		return collectionAndDataObjectListAndSearchAO
				.retrieveObjectStatForPath(irodsAbsolutePath);
	}

	/**
	 * Given an iRODS absolute path, retrieve the <code>ObjStat</code>
	 * 
	 * @param irodsAbsolutePath
	 *            <code>String</code> with the iRODS absolute path
	 * @return {@link ObjStat} with the file data from iRODS
	 * @throws FileNotFoundException
	 * @throws JargonException
	 */
	protected ObjStat retrieveObjStat(final String irodsAbsolutePath)
			throws FileNotFoundException, JargonException {

		log.info("retrieveObjStat()");

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePath");
		}

		log.info("irodsAbsolutePath:{}", irodsAbsolutePath);

		ObjStat objStat = collectionAndDataObjectListAndSearchAO
				.retrieveObjectStatForPath(irodsAbsolutePath);

		// make sure this special coll type has support
		MiscIRODSUtils.evaluateSpecCollSupport(objStat);
		return objStat;

	}

	/**
	 * Given an iRODS parent and child path, retrieve the <code>ObjStat</code>
	 * 
	 * @param parentPath
	 *            <code>String</code> with the parent path to the file
	 * @param fileName
	 *            <code>String</code> with the child file name
	 * @return{@link ObjStat} with the file data from iRODS
	 * @throws FileNotFoundException
	 * @throws JargonException
	 */
	protected ObjStat retrieveObjStat(final String parentPath,
			final String fileName) throws FileNotFoundException,
			JargonException {
		if (parentPath == null || parentPath.isEmpty()) {
			throw new IllegalArgumentException("null or empty parentPath");
		}

		if (fileName == null || fileName.isEmpty()) {
			throw new IllegalArgumentException("null or empty fileName");
		}

		IRODSFile irodsFile = this.getIRODSFileFactory().instanceIRODSFile(
				parentPath, fileName);
		return retrieveObjStat(irodsFile.getAbsolutePath());
	}

	/**
	 * Given an <code>ObjStat</code> return the absolute path to use considering
	 * things like soft links.
	 * 
	 * @param objStat
	 *            {@link ObjStat} that has been previously retrieved
	 * @return <code>String</code> with the absolute path to use to get to the
	 *         actual file
	 * @throws JargonException
	 */
	protected String resolveAbsolutePathGivenObjStat(final ObjStat objStat)
			throws JargonException {

		if (objStat == null) {
			throw new IllegalArgumentException("null objStat");
		}
		/*
		 * See if jargon supports the given object type
		 */
		MiscIRODSUtils.evaluateSpecCollSupport(objStat);
		return MiscIRODSUtils
				.determineAbsolutePathBasedOnCollTypeInObjectStat(objStat);
	}

	protected String resolveAbsolutePathViaObjStat(
			final String irodsAbsolutePath) throws JargonException {

		log.info("resoveAbsolutePathViaObjStat()");

		ObjStat objStat = retrieveObjStat(irodsAbsolutePath);
		return resolveAbsolutePathGivenObjStat(objStat);

	}

	@Override
	public abstract boolean isUserHasAccess(final String irodsAbsolutePath,
			final String userName) throws JargonException;

	/**
	 * Given two permissions (one by user, one by group) score and return the
	 * highest permission, or null if no permissions found
	 * 
	 * @param userFilePermission
	 * @param groupFilePermission
	 * @return {@link UserFilePermission} that is the highest level, or
	 *         <code>null</code> if no permissions found
	 */
	protected UserFilePermission scoreAndReturnHighestPermission(UserFilePermission userFilePermission, UserFilePermission groupFilePermission) {
		int userScore = -1;
		int groupScore = -1;
	
		if (userFilePermission != null) {
			userScore = userFilePermission.getFilePermissionEnum()
					.getPermissionNumericValue();
		}
	
		if (groupFilePermission != null) {
			groupScore = groupFilePermission.getFilePermissionEnum()
					.getPermissionNumericValue();
		}
	
		if (userScore >= groupScore && userScore > -1) {
			log.info("user file permission greater, using this:{}",
					userFilePermission);
			return userFilePermission;
		} else if (groupScore > -1) {
			log.info("returning groupFilePermission:{}", groupFilePermission);
			return groupFilePermission;
		} else {
			return null;
		}
	}

}
