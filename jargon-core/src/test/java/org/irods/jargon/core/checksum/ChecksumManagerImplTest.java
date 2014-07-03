package org.irods.jargon.core.checksum;

import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.DiscoveredServerPropertiesCache;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSServerProperties;
import org.irods.jargon.core.connection.IRODSServerProperties.IcatEnabled;
import org.irods.jargon.core.connection.SettableJargonProperties;
import org.irods.jargon.core.protovalues.ChecksumEncodingEnum;
import org.irods.jargon.core.pub.EnvironmentalInfoAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

public class ChecksumManagerImplTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();

	}

	@Test
	public void testDetermineChecksumEncodingForTargetServer331Normal()
			throws Exception {
		IRODSAccount account = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);

		DiscoveredServerPropertiesCache serverPropertiesCache = new DiscoveredServerPropertiesCache();
		Mockito.when(
				irodsAccessObjectFactory.getDiscoveredServerPropertiesCache())
				.thenReturn(serverPropertiesCache);

		SettableJargonProperties jargonProperties = new SettableJargonProperties();
		jargonProperties.setChecksumEncoding(ChecksumEncodingEnum.DEFAULT);
		Mockito.when(irodsAccessObjectFactory.getJargonProperties())
				.thenReturn(jargonProperties);

		IRODSServerProperties irodsServerProperties = IRODSServerProperties
				.instance(IcatEnabled.ICAT_ENABLED, 100, "rods3.3.1", "D",
						"zone");
		irodsServerProperties.setEirods(false);

		EnvironmentalInfoAO environmentalInfoAO = Mockito
				.mock(EnvironmentalInfoAO.class);
		Mockito.when(environmentalInfoAO.getIRODSServerProperties())
				.thenReturn(irodsServerProperties);

		Mockito.when(irodsAccessObjectFactory.getEnvironmentalInfoAO(account))
				.thenReturn(environmentalInfoAO);

		ChecksumManager checksumManager = new ChecksumManagerImpl(account,
				irodsAccessObjectFactory);

		ChecksumEncodingEnum actual = checksumManager
				.determineChecksumEncodingForTargetServer();
		Assert.assertEquals("did not set MD5 for normal",
				ChecksumEncodingEnum.MD5, actual);
		String cachedEncoding = serverPropertiesCache.retrieveValue(
				account.getHost(), account.getZone(),
				DiscoveredServerPropertiesCache.CHECKSUM_TYPE);
		Assert.assertNotNull("did not get cached encoding", cachedEncoding);
		Assert.assertEquals("did not correctly cache encoding",
				ChecksumEncodingEnum.MD5.toString(), cachedEncoding);

	}

	@Test
	public void testDetermineChecksumEncodingForTargetServerConsortiumStrong()
			throws Exception {
		IRODSAccount account = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);

		DiscoveredServerPropertiesCache serverPropertiesCache = new DiscoveredServerPropertiesCache();
		Mockito.when(
				irodsAccessObjectFactory.getDiscoveredServerPropertiesCache())
				.thenReturn(serverPropertiesCache);

		SettableJargonProperties jargonProperties = new SettableJargonProperties();
		jargonProperties.setChecksumEncoding(ChecksumEncodingEnum.STRONG);
		Mockito.when(irodsAccessObjectFactory.getJargonProperties())
				.thenReturn(jargonProperties);

		IRODSServerProperties irodsServerProperties = IRODSServerProperties
				.instance(IcatEnabled.ICAT_ENABLED, 100, "rods4.0.2", "D",
						"zone");
		irodsServerProperties.setEirods(true);

		EnvironmentalInfoAO environmentalInfoAO = Mockito
				.mock(EnvironmentalInfoAO.class);
		Mockito.when(environmentalInfoAO.getIRODSServerProperties())
				.thenReturn(irodsServerProperties);

		Mockito.when(irodsAccessObjectFactory.getEnvironmentalInfoAO(account))
				.thenReturn(environmentalInfoAO);

		ChecksumManager checksumManager = new ChecksumManagerImpl(account,
				irodsAccessObjectFactory);

		ChecksumEncodingEnum actual = checksumManager
				.determineChecksumEncodingForTargetServer();
		Assert.assertEquals("did not set sha256 for strong",
				ChecksumEncodingEnum.SHA256, actual);

		String cachedEncoding = serverPropertiesCache.retrieveValue(
				account.getHost(), account.getZone(),
				DiscoveredServerPropertiesCache.CHECKSUM_TYPE);
		Assert.assertNotNull("did not get cached encoding", cachedEncoding);
		Assert.assertEquals("did not correctly cache encoding",
				ChecksumEncodingEnum.SHA256.toString(), cachedEncoding);

	}

	@Test
	public void testDetermineChecksumEncodingForTargetServerCachedMD5()
			throws Exception {
		IRODSAccount account = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);

		DiscoveredServerPropertiesCache serverPropertiesCache = new DiscoveredServerPropertiesCache();
		serverPropertiesCache.cacheAProperty(account.getHost(),
				account.getZone(),
				DiscoveredServerPropertiesCache.CHECKSUM_TYPE,
				ChecksumEncodingEnum.MD5.toString());
		Mockito.when(
				irodsAccessObjectFactory.getDiscoveredServerPropertiesCache())
				.thenReturn(serverPropertiesCache);

		SettableJargonProperties jargonProperties = new SettableJargonProperties();
		jargonProperties.setChecksumEncoding(ChecksumEncodingEnum.STRONG);
		Mockito.when(irodsAccessObjectFactory.getJargonProperties())
				.thenReturn(jargonProperties);

		IRODSServerProperties irodsServerProperties = IRODSServerProperties
				.instance(IcatEnabled.ICAT_ENABLED, 100, "rods4.0.2", "D",
						"zone");
		irodsServerProperties.setEirods(true);

		EnvironmentalInfoAO environmentalInfoAO = Mockito
				.mock(EnvironmentalInfoAO.class);
		Mockito.when(environmentalInfoAO.getIRODSServerProperties())
				.thenReturn(irodsServerProperties);

		Mockito.when(irodsAccessObjectFactory.getEnvironmentalInfoAO(account))
				.thenReturn(environmentalInfoAO);

		ChecksumManager checksumManager = new ChecksumManagerImpl(account,
				irodsAccessObjectFactory);

		ChecksumEncodingEnum actual = checksumManager
				.determineChecksumEncodingForTargetServer();
		Assert.assertEquals("did not set md5 for cached value",
				ChecksumEncodingEnum.MD5, actual);

		String cachedEncoding = serverPropertiesCache.retrieveValue(
				account.getHost(), account.getZone(),
				DiscoveredServerPropertiesCache.CHECKSUM_TYPE);
		Assert.assertNotNull("did not get cached encoding", cachedEncoding);
		Assert.assertEquals("did not correctly cache encoding",
				ChecksumEncodingEnum.MD5.toString(), cachedEncoding);

	}

}
