/**
 * 
 */
package org.irods.jargon.core.checksum;

import org.irods.jargon.core.connection.DiscoveredServerPropertiesCache;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSServerProperties;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.protovalues.ChecksumEncodingEnum;
import org.irods.jargon.core.pub.EnvironmentalInfoAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service to determine, based on local and remote server properties, the
 * correct checksum hash type.
 * 
 * @author Mike Conway - DICE
 * 
 */
public class ChecksumManagerImpl implements ChecksumManager {

	private final IRODSAccount irodsAccount;
	private final IRODSAccessObjectFactory irodsAccessObjectFactory;

	public static final Logger log = LoggerFactory
			.getLogger(ChecksumManagerImpl.class);

	/**
	 * @param irodsAccount
	 *            {@link IRODSAccount} that represents the connected server
	 * @param irodsAccessObjectFactory
	 *            {@link IRODSAccessObjectFactory} that allows access to iRODS
	 *            services
	 */
	public ChecksumManagerImpl(final IRODSAccount irodsAccount,
			final IRODSAccessObjectFactory irodsAccessObjectFactory) {
		super();
		this.irodsAccount = irodsAccount;
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.checksum.ChecksumManager#
	 * determineChecksumEncodingForTargetServer()
	 */
	@Override
	public ChecksumEncodingEnum determineChecksumEncodingForTargetServer()
			throws JargonException {

		log.info("determineChecksumEncodingForTargetServer()");

		log.info("checking discovered cache to see if I have stored a checksum type...");

		String checksumTypeRetrievedFromCache = irodsAccessObjectFactory
				.getDiscoveredServerPropertiesCache().retrieveValue(
						irodsAccount.getHost(), irodsAccount.getZone(),
						DiscoveredServerPropertiesCache.CHECKSUM_TYPE);

		if (checksumTypeRetrievedFromCache != null) {
			return ChecksumEncodingEnum
					.findTypeByString(checksumTypeRetrievedFromCache);
		}

		/*
		 * No cache hit...If I've specified in the jargon.properties exactly,
		 * use that encoding
		 */

		ChecksumEncodingEnum encodingFromProperties = irodsAccessObjectFactory
				.getJargonProperties().getChecksumEncoding();

		if (encodingFromProperties == null) {
			throw new JargonRuntimeException(
					"jargon properties has null checksum encoding");
		}

		if (encodingFromProperties == ChecksumEncodingEnum.MD5) {
			log.info("jargon properties specifies MD5");
			cacheEncoding(ChecksumEncodingEnum.MD5);
			return ChecksumEncodingEnum.MD5;
		} else if (encodingFromProperties == ChecksumEncodingEnum.SHA256) {
			log.info("jargon properties specifies SHA256");
			cacheEncoding(ChecksumEncodingEnum.SHA256);
			return ChecksumEncodingEnum.SHA256;
		}

		/*
		 * The jargon properties settings need some interpretation based on the
		 * version and other discoverable qualities of the target server.
		 */

		EnvironmentalInfoAO environmentalInfoAO = irodsAccessObjectFactory
				.getEnvironmentalInfoAO(irodsAccount);

		IRODSServerProperties serverProperties = environmentalInfoAO
				.getIRODSServerProperties();

		boolean isConsortium = serverProperties.isEirods();

		log.info("is this consortium? (post 3.3.1):{}", isConsortium);

		/*
		 * Negotiation:
		 * 
		 * DEFAULT - use MD5 pre consortium and SHA256 post
		 * 
		 * STRONG - use MD5 pre 3.3.1 and SHA256 after
		 */

		if (isConsortium) {
			log.info("is consortium iRODS");
			if (encodingFromProperties == ChecksumEncodingEnum.DEFAULT) {
				log.info("checksumEncoding set to SHA256");
				cacheEncoding(ChecksumEncodingEnum.SHA256);
				return ChecksumEncodingEnum.SHA256;
			} else if (encodingFromProperties == ChecksumEncodingEnum.STRONG) {
				log.info("checksumEncoding set to SHA256");
				cacheEncoding(ChecksumEncodingEnum.SHA256);
				return ChecksumEncodingEnum.SHA256;
			} else {
				log.error("unhandled checksum type:{}", encodingFromProperties);
				throw new JargonException("unknown checksum type");
			}
		} else if (serverProperties
				.isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods3.3.1")) {
			log.info("is at least iRODS3.3.1");
			if (encodingFromProperties == ChecksumEncodingEnum.DEFAULT) {
				log.info("checksumEncoding set to MD5");
				cacheEncoding(ChecksumEncodingEnum.MD5);
				return ChecksumEncodingEnum.MD5;
			} else if (encodingFromProperties == ChecksumEncodingEnum.STRONG) {
				log.info("checksumEncoding set to SHA256");
				cacheEncoding(ChecksumEncodingEnum.SHA256);
				return ChecksumEncodingEnum.SHA256;
			} else {
				log.error("unhandled checksum type:{}", encodingFromProperties);
				throw new JargonException("unknown checksum type");
			}
		} else {
			log.info("checksumEncoding set to MD5");
			cacheEncoding(ChecksumEncodingEnum.MD5);
			return ChecksumEncodingEnum.MD5;
		}

	}

	private void cacheEncoding(final ChecksumEncodingEnum checksumEncoding) {
		irodsAccessObjectFactory.getDiscoveredServerPropertiesCache()
				.cacheAProperty(irodsAccount.getHost(), irodsAccount.getZone(),
						DiscoveredServerPropertiesCache.CHECKSUM_TYPE,
						checksumEncoding.toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.checksum.ChecksumManager#
	 * determineChecksumEncodingFromIrodsData(java.lang.String)
	 */
	@Override
	public ChecksumValue determineChecksumEncodingFromIrodsData(
			final String irodsChecksumValue)
			throws ChecksumMethodUnavailableException {

		log.info("determineChecksumEncodingFromIrodsData()");

		if (irodsChecksumValue == null || irodsChecksumValue.isEmpty()) {
			throw new IllegalArgumentException("null or empty checksum value");
		}

		log.info("irodsChecksumValue:{}", irodsChecksumValue);

		ChecksumEncodingEnum checksumEncodingEnum = null;
		int idxColon = irodsChecksumValue.indexOf(":");
		if (idxColon == -1) {
			checksumEncodingEnum = ChecksumEncodingEnum.MD5;
		} else {

			String beforeColon = irodsChecksumValue.substring(0, idxColon);
			if (beforeColon.equals("md5")) {
				checksumEncodingEnum = ChecksumEncodingEnum.MD5;
			} else if (beforeColon.equals("sha2")) {
				checksumEncodingEnum = ChecksumEncodingEnum.SHA256;
			} else {
				log.error("unknown checksum type:{}", beforeColon);
				throw new ChecksumMethodUnavailableException(
						"unknown checksum type:" + beforeColon);
			}
		}

		log.info("have encoding of :{}", checksumEncodingEnum);
		String checksumData;
		if (idxColon == -1) {
			checksumData = irodsChecksumValue;
		} else {
			checksumData = irodsChecksumValue.substring(idxColon + 1);
		}

		ChecksumValue checksumValue = new ChecksumValue();
		checksumValue.setChecksumEncoding(checksumEncodingEnum);
		checksumValue.setChecksumStringValue(checksumData);
		checksumValue.setChecksumTransmissionFormat(irodsChecksumValue);
		log.info("checksumValue from iRODS:{}", checksumValue);
		return checksumValue;

	}

}
