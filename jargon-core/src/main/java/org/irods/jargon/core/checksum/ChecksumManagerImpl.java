/**
 *
 */
package org.irods.jargon.core.checksum;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.irods.jargon.core.connection.DiscoveredServerPropertiesCache;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSServerProperties;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.protovalues.ChecksumEncodingEnum;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.EnvironmentalInfoAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;

/**
 * Various methods to compute and determine checksums
 * <p>
 * Note this implementation is very basic and will be expanded later.
 * {@link DataObjectAO} has other checksum support that will eventually migrate
 * here.
 *
 * @author Mike Conway - DICE
 *
 */
public class ChecksumManagerImpl implements ChecksumManager {

	private final IRODSAccount irodsAccount;
	private final IRODSAccessObjectFactory irodsAccessObjectFactory;

	public static final Logger log = LogManager.getLogger(ChecksumManagerImpl.class);

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
	public ChecksumEncodingEnum determineChecksumEncodingForTargetServer() throws JargonException {

		log.info("determineChecksumEncodingForTargetServer()");

		log.info("checking discovered cache to see if I have stored a checksum type...");

		String checksumTypeRetrievedFromCache = irodsAccessObjectFactory.getDiscoveredServerPropertiesCache()
				.retrieveValue(irodsAccount.getHost(), irodsAccount.getZone(),
						DiscoveredServerPropertiesCache.CHECKSUM_TYPE);

		if (checksumTypeRetrievedFromCache != null) {
			log.info("found cached checksum encoding:{}", checksumTypeRetrievedFromCache);
			return ChecksumEncodingEnum.findTypeByString(checksumTypeRetrievedFromCache);
		}

		/*
		 * No cache hit...If I've specified in the jargon.properties exactly, use that
		 * encoding
		 */

		ChecksumEncodingEnum encodingFromProperties = irodsAccessObjectFactory.getJargonProperties()
				.getChecksumEncoding();

		if (encodingFromProperties == null) {
			throw new JargonRuntimeException("jargon properties has null checksum encoding");
		}

		log.info("encoding from properties:{}", encodingFromProperties);

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
		 * The jargon properties settings need some interpretation based on the version
		 * and other discoverable qualities of the target server.
		 */

		EnvironmentalInfoAO environmentalInfoAO = irodsAccessObjectFactory.getEnvironmentalInfoAO(irodsAccount);

		IRODSServerProperties serverProperties = environmentalInfoAO.getIRODSServerProperties();

		boolean isConsortium = serverProperties.isAtLeastIrods410();

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
		} else if (serverProperties.isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods3.3.1")) {
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
		irodsAccessObjectFactory.getDiscoveredServerPropertiesCache().cacheAProperty(irodsAccount.getHost(),
				irodsAccount.getZone(), DiscoveredServerPropertiesCache.CHECKSUM_TYPE, checksumEncoding.toString());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.checksum.ChecksumManager#
	 * determineChecksumEncodingFromIrodsData(java.lang.String)
	 */
	@Override
	public ChecksumValue determineChecksumEncodingFromIrodsData(final String irodsChecksumValue)
			throws ChecksumMethodUnavailableException {

		log.info("determineChecksumEncodingFromIrodsData()");

		if (irodsChecksumValue == null || irodsChecksumValue.isEmpty()) {
			return null;
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
				throw new ChecksumMethodUnavailableException("unknown checksum type:" + beforeColon);
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
		byte[] digest = new byte[0];

		/*
		 * add additional representations based on the checksum type
		 */

		if (checksumEncodingEnum == ChecksumEncodingEnum.MD5) {
			log.debug("adding variants of checksum for md5");
			try {
				digest = Hex.decodeHex(checksumData);
			} catch (DecoderException e) {
				log.error("error decoding a hex value:{}", checksumData, e);
				throw new JargonRuntimeException(e);
			}

		} else if (checksumEncodingEnum == ChecksumEncodingEnum.SHA256) {
			log.debug("adding variants of checksum for sha256");
			digest = Base64.decodeBase64(checksumData);

		} else {
			log.error("unable to find an encoder for: {}", checksumEncodingEnum);
			throw new ChecksumMethodUnavailableException("cannot find encoding method for checksum");
		}

		checksumValue.setBinaryChecksumValue(digest);
		checksumValue.setBase64ChecksumValue(Base64.encodeBase64String(digest));
		checksumValue.setHexChecksumValue(Hex.encodeHexString(digest));

		log.info("checksumValue from iRODS:{}", checksumValue);
		return checksumValue;

	}

}
