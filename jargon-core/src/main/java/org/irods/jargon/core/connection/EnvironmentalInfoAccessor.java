/**
 *
 */
package org.irods.jargon.core.connection;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.MiscSvrInfo;
import org.irods.jargon.core.packinstr.Tag;
import org.irods.jargon.core.utils.IRODSConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Obtain information about the connected irods server.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class EnvironmentalInfoAccessor {

	private IRODSMidLevelProtocol irodsProtocol = null;
	private final Logger log = LogManager.getLogger(EnvironmentalInfoAccessor.class);

	public EnvironmentalInfoAccessor(final IRODSMidLevelProtocol irodsProtocol) throws JargonException {
		if (irodsProtocol == null) {
			throw new JargonException("null irodsProtocol");
		}
		if (!irodsProtocol.isConnected()) {
			throw new JargonException("irods protocol is not connected");
		}
		this.irodsProtocol = irodsProtocol;

	}

	/**
	 * Class to access underlying {@code IRODSServerProperties}. Note that this uses
	 * a caching optimization.
	 *
	 * @return {@link IRODSServerProperties}
	 * @throws JargonException for iRODS error
	 */
	public IRODSServerProperties getIRODSServerProperties() throws JargonException {
		log.debug("getting irods server properties");

		log.debug("checking for cached properties...");

		if (irodsProtocol.getIrodsSession() != null) {
			IRODSServerProperties cached = irodsProtocol.getIrodsSession().getDiscoveredServerPropertiesCache()
					.retrieveIRODSServerProperties(irodsProtocol.getIrodsAccount().getHost(),
							irodsProtocol.getIrodsAccount().getZone());

			if (cached != null) {
				log.debug("returning cached props:{}", cached);
				return cached;
			}
		}

		Tag response = irodsProtocol.irodsFunction(IRODSConstants.RODS_API_REQ, "", MiscSvrInfo.API_NBR);
		log.debug("server response obtained");
		int serverType = response.getTag(MiscSvrInfo.SERVER_TYPE_TAG).getIntValue();

		IRODSServerProperties.IcatEnabled icatEnabled = null;
		if (serverType == 1) {
			icatEnabled = IRODSServerProperties.IcatEnabled.ICAT_ENABLED;
		} else {
			icatEnabled = IRODSServerProperties.IcatEnabled.NO_ICAT;
		}

		int serverBootTime = response.getTag(MiscSvrInfo.SERVER_BOOT_TIME_TAG).getIntValue();
		String relVersion = response.getTag(MiscSvrInfo.REL_VERSION_TAG).getStringValue();
		String apiVersion = response.getTag(MiscSvrInfo.API_VERSION_TAG).getStringValue();
		String rodsZone = response.getTag(MiscSvrInfo.RODS_ZONE_TAG).getStringValue();
		IRODSServerProperties props = IRODSServerProperties.instance(icatEnabled, serverBootTime, relVersion,
				apiVersion, rodsZone);

		if (irodsProtocol.getIrodsSession() != null) {
			irodsProtocol.getIrodsSession().getDiscoveredServerPropertiesCache().cacheIRODSServerProperties(
					irodsProtocol.getIrodsAccount().getHost(), irodsProtocol.getIrodsAccount().getZone(), props);
			log.debug("cached the props for host and zone:{}", props);
		}
		return props;
	}

}
