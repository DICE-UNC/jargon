package org.irods.jargon.core.checksum;

import java.util.Properties;

import org.junit.Assert;

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

	@Test
	public void testGetEncodingFromIrodsWhenSHA2() throws Exception {
		String irodsString = "sha2:blah949204902";

		IRODSAccount account = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);

		ChecksumManager checksumManager = new ChecksumManagerImpl(account,
				irodsAccessObjectFactory);

		ChecksumValue checksumValue = checksumManager
				.determineChecksumEncodingFromIrodsData(irodsString);

		ChecksumEncodingEnum checksumEncodingEnum = checksumValue
				.getChecksumEncoding();
		Assert.assertEquals("should have picked sha2",
				ChecksumEncodingEnum.SHA256, checksumEncodingEnum);
		Assert.assertEquals("blah949204902",
				checksumValue.getChecksumStringValue());
		Assert.assertEquals(irodsString,
				checksumValue.getChecksumTransmissionFormat());

	}

	@Test
	public void testGetEncodingFromIrodsWhenMD5() throws Exception {
		String md5String = "blah949204902";

		IRODSAccount account = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);

		ChecksumManager checksumManager = new ChecksumManagerImpl(account,
				irodsAccessObjectFactory);

		ChecksumValue checksumValue = checksumManager
				.determineChecksumEncodingFromIrodsData(md5String);

		ChecksumEncodingEnum checksumEncodingEnum = checksumValue
				.getChecksumEncoding();
		Assert.assertEquals("should have picked md5", ChecksumEncodingEnum.MD5,
				checksumEncodingEnum);
		Assert.assertEquals(md5String, checksumValue.getChecksumStringValue());
		Assert.assertEquals(md5String,
				checksumValue.getChecksumTransmissionFormat());

	}

	@Test(expected = ChecksumMethodUnavailableException.class)
	public void testGetEncodingFromIrodsWhenBogus() throws Exception {
		String md5String = "bogus:blah949204902";

		IRODSAccount account = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);

		ChecksumManager checksumManager = new ChecksumManagerImpl(account,
				irodsAccessObjectFactory);
		checksumManager.determineChecksumEncodingFromIrodsData(md5String);

	}

	@Test
	public void testGetEncodingFromIrodsWhenNull() throws Exception {
		String md5String = null;

		IRODSAccount account = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);

		ChecksumManager checksumManager = new ChecksumManagerImpl(account,
				irodsAccessObjectFactory);
		ChecksumValue actual = checksumManager
				.determineChecksumEncodingFromIrodsData(md5String);
		Assert.assertNull(actual);

	}

	@Test
	public void testGetEncodingFromIrodsWhenBlank() throws Exception {
		String md5String = "";

		IRODSAccount account = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);

		ChecksumManager checksumManager = new ChecksumManagerImpl(account,
				irodsAccessObjectFactory);
		ChecksumValue actual = checksumManager
				.determineChecksumEncodingFromIrodsData(md5String);
		Assert.assertNull(actual);

	}

}
