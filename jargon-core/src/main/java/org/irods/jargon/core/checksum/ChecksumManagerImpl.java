/**
 * 
 */
package org.irods.jargon.core.checksum;

import org.irods.jargon.core.connection.DiscoveredServerPropertiesCache;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSServerProperties;
import org.irods.jargon.core.connection.JargonProperties.ChecksumEncoding;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.pub.EnvironmentalInfoAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tools to manage negotiation and generation of checksums
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
	public ChecksumEncoding determineChecksumEncodingForTargetServer()
			throws JargonException {

		log.info("determineChecksumEncodingForTargetServer()");

		log.info("checking discovered cache to see if I have stored a checksum type...");
		ChecksumEncoding checksumEncoding = null;

		Object checksumTypeRetrievedFromCache = irodsAccessObjectFactory
				.getDiscoveredServerPropertiesCache().retrieveValue(
						irodsAccount.getHost(), irodsAccount.getZone(),
						DiscoveredServerPropertiesCache.CHECKSUM_TYPE);

		if (checksumTypeRetrievedFromCache != null) {
			checksumEncoding = (ChecksumEncoding) checksumTypeRetrievedFromCache;
			log.info("discovered preferred encoding as:{}", checksumEncoding);
			return checksumEncoding;
		}

		/*
		 * No cache hit...If I've specified in the jargon.properties exactly,
		 * use that encoding
		 */

		ChecksumEncoding encodingFromProperties = irodsAccessObjectFactory
				.getJargonProperties().getChecksumEncoding();

		if (encodingFromProperties == null) {
			throw new JargonRuntimeException(
					"jargon properties has null checksum encoding");
		}

		if (encodingFromProperties.equals(ChecksumEncoding.MD5.toString())) {
			log.info("jargon properties specifies MD5");
			cacheEncoding(ChecksumEncoding.MD5);
			return ChecksumEncoding.MD5;
		} else if (encodingFromProperties.equals(ChecksumEncoding.SHA256
				.toString())) {
			log.info("jargon properties specifies SHA256");
			cacheEncoding(ChecksumEncoding.SHA256);
			return ChecksumEncoding.SHA256;
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
			if (encodingFromProperties == ChecksumEncoding.DEFAULT) {
				log.info("checksumEncoding set to SHA256");
				cacheEncoding(ChecksumEncoding.SHA256);
				return ChecksumEncoding.SHA256;
			} else if (encodingFromProperties == ChecksumEncoding.STRONG) {
				log.info("checksumEncoding set to SHA256");
				cacheEncoding(ChecksumEncoding.SHA256);
				return ChecksumEncoding.SHA256;
			} else {
				log.error("unhandled checksum type:{}", encodingFromProperties);
				throw new JargonException("unknown checksum type");
			}
		} else if (serverProperties
				.isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods3.3.1")) {
			log.info("is at least iRODS3.3.1");
			if (encodingFromProperties == ChecksumEncoding.DEFAULT) {
				log.info("checksumEncoding set to MD5");
				cacheEncoding(ChecksumEncoding.MD5);
				return ChecksumEncoding.MD5;
			} else if (encodingFromProperties == ChecksumEncoding.STRONG) {
				log.info("checksumEncoding set to SHA256");
				cacheEncoding(ChecksumEncoding.SHA256);
				return ChecksumEncoding.SHA256;
			} else {
				log.error("unhandled checksum type:{}", encodingFromProperties);
				throw new JargonException("unknown checksum type");
			}
		} else {
			log.info("checksumEncoding set to MD5");
			cacheEncoding(ChecksumEncoding.MD5);
			return ChecksumEncoding.MD5;
		}

	}

	private void cacheEncoding(ChecksumEncoding checksumEncoding) {
		irodsAccessObjectFactory.getDiscoveredServerPropertiesCache()
				.cacheAProperty(irodsAccount.getHost(), irodsAccount.getZone(),
						DiscoveredServerPropertiesCache.CHECKSUM_TYPE,
						checksumEncoding.toString());
	}

}
