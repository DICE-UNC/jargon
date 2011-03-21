package org.irods.jargon.datautils.synchproperties;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.AVUQueryElement.AVUQueryPart;
import org.irods.jargon.core.query.AVUQueryOperatorEnum;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service to maintain synchronization properties for client->iRODS data
 * synchronization
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class SynchPropertiesServiceImpl implements SynchPropetiesService {

	public static final Logger log = LoggerFactory
			.getLogger(SynchPropertiesServiceImpl.class);

	/**
	 * Factory to create necessary Jargon access objects, which interact with
	 * the iRODS server
	 */
	private IRODSAccessObjectFactory irodsAccessObjectFactory;

	/**
	 * Describes iRODS server and account information
	 */
	private IRODSAccount irodsAccount;

	public IRODSAccessObjectFactory getIrodsAccessObjectFactory() {
		return irodsAccessObjectFactory;
	}

	public void setIrodsAccessObjectFactory(
			final IRODSAccessObjectFactory irodsAccessObjectFactory) {
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
	}

	public IRODSAccount getIrodsAccount() {
		return irodsAccount;
	}

	public void setIrodsAccount(final IRODSAccount irodsAccount) {
		this.irodsAccount = irodsAccount;
	}

	/*
	 * attrib | value | unit
	 * 
	 * in home dir
	 * 
	 * [device name] | [user name] | iRODSSynch:userDevice
	 * 
	 * in synch root dir
	 * 
	 * [device name]:[user name] | [lastLocalSynch | lastIrodsSynch |
	 * localAbsPath] | iRODSSynch:userSynchDir
	 */

	/* (non-Javadoc)
	 * @see org.irods.jargon.datautils.synchproperties.SynchPropetiesService#getUserSynchTargetForUserAndAbsolutePath(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public UserSynchTarget getUserSynchTargetForUserAndAbsolutePath(
			final String userName, final String deviceName,
			final String irodsAbsolutePath) throws DataNotFoundException,
			JargonException {

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		if (deviceName == null || deviceName.isEmpty()) {
			throw new IllegalArgumentException("null or empty deviceName");
		}

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePath");
		}

		checkDependencies();

		log.info("getUserSynchTargetForUserAndAbsolutePath for user:{}",
				userName);
		log.info("    for absPath:{}", irodsAbsolutePath);

		CollectionAO collectionAO = irodsAccessObjectFactory
				.getCollectionAO(irodsAccount);

		StringBuilder userDevAttrib = new StringBuilder();
		userDevAttrib.append(userName);
		userDevAttrib.append(":");
		userDevAttrib.append(deviceName);

		List<AVUQueryElement> avuQuery = new ArrayList<AVUQueryElement>();
		List<MetaDataAndDomainData> queryResults;

		log.debug("building avu query");

		try {
			AVUQueryElement avuQueryElement = AVUQueryElement
					.instanceForValueQuery(AVUQueryPart.UNITS,
							AVUQueryOperatorEnum.EQUAL, USER_SYNCH_DIR_TAG);
			avuQuery.add(avuQueryElement);
			avuQueryElement = AVUQueryElement.instanceForValueQuery(
					AVUQueryPart.ATTRIBUTE, AVUQueryOperatorEnum.EQUAL,
					userDevAttrib.toString());
			avuQuery.add(avuQueryElement);
			queryResults = collectionAO
					.findMetadataValuesByMetadataQueryForCollection(avuQuery,
							irodsAbsolutePath);
		} catch (JargonQueryException e) {
			log.error(
					"error creating query for lookup of user synch target data",
					e);
			throw new JargonException(e);
		}

		log.debug("result of query for synch target data:{}", queryResults);

		// there should be only one synch device. If there's more than one,
		// treat as an error for now
		if (queryResults.isEmpty()) {
			throw new DataNotFoundException(
					"no synch data for given user and device");
		} else if (queryResults.size() > 1) {
			throw new JargonException(
					"more than one synch entry found for given user and device");
		}

		MetaDataAndDomainData deviceInfo = queryResults.get(0);
		// split the data out of the avu data value

		String[] valueComponents = deviceInfo.getAvuValue().split("[|]");
		if (valueComponents.length != 3) {
			log.error("did not find 3 expected values in the avu value: {}",
					deviceInfo);
			throw new JargonException(
					"unexpected data in AVU value for UserSynchTarget");
		}

		long lastIrodsSynch;
		long lastLocalSynch;
		String localSynchAbsolutePath;

		try {
			lastIrodsSynch = Long.valueOf(valueComponents[0]);
			lastLocalSynch = Long.valueOf(valueComponents[1]);
		} catch (NumberFormatException nfe) {
			log.error(
					"unable to parse out timestamps from synch data in AVU value:{}",
					deviceInfo);
			throw new JargonException("UserSynchTarget parse error");
		}

		localSynchAbsolutePath = valueComponents[2];

		if (localSynchAbsolutePath == null || localSynchAbsolutePath.isEmpty()) {
			log.error("no local synch absolute path found in:{}", deviceInfo);
			throw new JargonException("no local synch absolute path");
		}

		UserSynchTarget userSynchTarget = new UserSynchTarget();
		userSynchTarget.setDeviceName(deviceName);
		userSynchTarget.setIrodsSynchRootAbsolutePath(irodsAbsolutePath);
		userSynchTarget.setLocalSynchRootAbsolutePath(localSynchAbsolutePath);
		userSynchTarget.setLastIRODSSynchTimestamp(lastIrodsSynch);
		userSynchTarget.setLastLocalSynchTimestamp(lastLocalSynch);
		userSynchTarget.setUserName(userName);

		log.info("build UserSynchTarget:{}", userSynchTarget);

		return userSynchTarget;

	}

	/**
	 * Ensure whether the required dependencies have been set
	 */
	private void checkDependencies() throws JargonException {

		if (irodsAccessObjectFactory == null) {
			throw new JargonException(
					"irodsAccessObjectFactory was not initialized");
		}

		if (irodsAccount == null) {
			throw new JargonException("irodsAccount was not initialized");
		}

	}

}
