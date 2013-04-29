package org.irods.jargon.core.connection;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSServerProperties.IcatEnabled;
import org.junit.BeforeClass;
import org.junit.Test;

public class DiscoveredServerPropertiesCacheTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCacheAPropertyNullHost() {
		String testHost = null;
		String testZone = "zone1";
		String testKey = "key1";
		String testValue = "value1";
		DiscoveredServerPropertiesCache target = new DiscoveredServerPropertiesCache();
		target.cacheAProperty(testHost, testZone, testKey, testValue);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCacheAPropertyBlankHost() {
		String testHost = "";
		String testZone = "zone1";
		String testKey = "key1";
		String testValue = "value1";
		DiscoveredServerPropertiesCache target = new DiscoveredServerPropertiesCache();
		target.cacheAProperty(testHost, testZone, testKey, testValue);
	}

	@Test
	public void testCacheAPropertyNullZone() {
		String testHost = "host";
		String testZone = null;
		String testKey = "key1";
		String testValue = "value1";
		DiscoveredServerPropertiesCache target = new DiscoveredServerPropertiesCache();
		target.cacheAProperty(testHost, testZone, testKey, testValue);
	}

	@Test
	public void testCacheAPropertyBlankZone() {
		String testHost = "host";
		String testZone = "";
		String testKey = "key1";
		String testValue = "value1";
		DiscoveredServerPropertiesCache target = new DiscoveredServerPropertiesCache();
		target.cacheAProperty(testHost, testZone, testKey, testValue);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCacheAPropertyNullKey() {
		String testHost = "host";
		String testZone = "zone";
		String testKey = null;
		String testValue = "value1";
		DiscoveredServerPropertiesCache target = new DiscoveredServerPropertiesCache();
		target.cacheAProperty(testHost, testZone, testKey, testValue);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCacheAPropertyBlankKey() {
		String testHost = "host";
		String testZone = "zone";
		String testKey = "";
		String testValue = "value1";
		DiscoveredServerPropertiesCache target = new DiscoveredServerPropertiesCache();
		target.cacheAProperty(testHost, testZone, testKey, testValue);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCacheAPropertyNullValue() {
		String testHost = "host";
		String testZone = "zone";
		String testKey = "key";
		String testValue = null;
		DiscoveredServerPropertiesCache target = new DiscoveredServerPropertiesCache();
		target.cacheAProperty(testHost, testZone, testKey, testValue);
	}

	@Test
	public void testCacheAPropertyBlankValue() {
		String testHost = "host";
		String testZone = "zone";
		String testKey = "key";
		String testValue = "";
		DiscoveredServerPropertiesCache target = new DiscoveredServerPropertiesCache();
		target.cacheAProperty(testHost, testZone, testKey, testValue);
		// should be ok, no error
	}

	@Test
	public void testRetrieveValue() {
		String testHost = "host1";
		String testZone = "zone1";
		String testKey = "key1";
		String testValue = "value1";
		DiscoveredServerPropertiesCache target = new DiscoveredServerPropertiesCache();
		target.cacheAProperty(testHost, testZone, testKey, testValue);
		String val = target.retrieveValue(testHost, testZone, testKey);
		Assert.assertEquals(testValue, val);
	}

	@Test
	public void testRetrieveValueNoCache() {
		String testHost = "host1";
		String testZone = "zone1";
		String testKey = "key1";
		DiscoveredServerPropertiesCache target = new DiscoveredServerPropertiesCache();
		String val = target.retrieveValue(testHost, testZone, testKey);
		Assert.assertNull(val);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRetrieveValueNullHost() {
		String testHost = null;
		String testZone = "zone1";
		String testKey = "key1";
		DiscoveredServerPropertiesCache target = new DiscoveredServerPropertiesCache();
		target.retrieveValue(testHost, testZone, testKey);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testRetrieveValueBlankHost() {
		String testHost = "";
		String testZone = "zone1";
		String testKey = "key1";
		DiscoveredServerPropertiesCache target = new DiscoveredServerPropertiesCache();
		target.retrieveValue(testHost, testZone, testKey);
	}

	@Test
	public void testRetrieveValueNullZone() {
		String testHost = "host1";
		String testZone = null;
		String testKey = "key1";
		DiscoveredServerPropertiesCache target = new DiscoveredServerPropertiesCache();
		target.retrieveValue(testHost, testZone, testKey);

	}

	@Test
	public void testRetrieveValueBlankZone() {
		String testHost = "host1";
		String testZone = "";
		String testKey = "key1";
		DiscoveredServerPropertiesCache target = new DiscoveredServerPropertiesCache();
		target.retrieveValue(testHost, testZone, testKey);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRetrieveValueNullKey() {
		String testHost = "host1";
		String testZone = "zone1";
		String testKey = null;
		DiscoveredServerPropertiesCache target = new DiscoveredServerPropertiesCache();
		target.retrieveValue(testHost, testZone, testKey);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRetrieveValueBlankKey() {
		String testHost = "host1";
		String testZone = "zone1";
		String testKey = "";
		DiscoveredServerPropertiesCache target = new DiscoveredServerPropertiesCache();
		target.retrieveValue(testHost, testZone, testKey);
	}

	@Test
	public void testDeleteCachedProperty() {
		String testHost = "host1";
		String testZone = "zone1";
		String testKey = "key1";
		String testValue = "value1";
		DiscoveredServerPropertiesCache target = new DiscoveredServerPropertiesCache();
		target.cacheAProperty(testHost, testZone, testKey, testValue);
		target.deleteCachedProperty(testHost, testZone, testKey);
		String val = target.retrieveValue(testHost, testZone, testKey);
		Assert.assertNull(val);
	}

	@Test
	public void testDeleteCachedPropertyNoCacheForZone() {
		String testHost = "host1";
		String testZone = "zone1";
		String testKey = "key1";
		DiscoveredServerPropertiesCache target = new DiscoveredServerPropertiesCache();
		target.deleteCachedProperty(testHost, testZone, testKey);
		String val = target.retrieveValue(testHost, testZone, testKey);
		Assert.assertNull(val);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDeleteCachedPropertyNullHost() {
		String testHost = null;
		String testZone = "zone1";
		String testKey = "key1";
		DiscoveredServerPropertiesCache target = new DiscoveredServerPropertiesCache();
		target.deleteCachedProperty(testHost, testZone, testKey);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDeleteCachedPropertyBlankHost() {
		String testHost = "";
		String testZone = "zone1";
		String testKey = "key1";
		DiscoveredServerPropertiesCache target = new DiscoveredServerPropertiesCache();
		target.deleteCachedProperty(testHost, testZone, testKey);
	}

	@Test
	public void testDeleteCachedPropertyNullZone() {
		String testHost = "host";
		String testZone = null;
		String testKey = "key1";
		DiscoveredServerPropertiesCache target = new DiscoveredServerPropertiesCache();
		target.deleteCachedProperty(testHost, testZone, testKey);
	}

	@Test
	public void testDeleteCachedPropertyBlankZone() {
		String testHost = "host";
		String testZone = "";
		String testKey = "key1";
		DiscoveredServerPropertiesCache target = new DiscoveredServerPropertiesCache();
		target.deleteCachedProperty(testHost, testZone, testKey);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDeleteCachedPropertyNullKey() {
		String testHost = "host";
		String testZone = "zone";
		String testKey = null;
		DiscoveredServerPropertiesCache target = new DiscoveredServerPropertiesCache();
		target.deleteCachedProperty(testHost, testZone, testKey);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDeleteCachedPropertyBlankKey() {
		String testHost = "host";
		String testZone = "zone";
		String testKey = "";
		DiscoveredServerPropertiesCache target = new DiscoveredServerPropertiesCache();
		target.deleteCachedProperty(testHost, testZone, testKey);
	}
	
	public void testCacheIRODSServerProperties() throws Exception {
		String host = "host";
		String zone = "zone";
		IRODSServerProperties props = IRODSServerProperties.instance(IcatEnabled.ICAT_ENABLED, 111, "x", "x", "x");
		DiscoveredServerPropertiesCache cache = new DiscoveredServerPropertiesCache();
		cache.cacheIRODSServerProperties(host, zone, props);
		
		IRODSServerProperties actual = cache.retrieveIRODSServerProperties(host, zone);
		Assert.assertNotNull(actual);
		}
	
	public void testCacheIRODSServerPropertiesWrongHost() throws Exception {
		String host = "host";
		String zone = "zone";
		IRODSServerProperties props = IRODSServerProperties.instance(IcatEnabled.ICAT_ENABLED, 111, "x", "x", "x");
		DiscoveredServerPropertiesCache cache = new DiscoveredServerPropertiesCache();
		cache.cacheIRODSServerProperties(host, zone, props);
		
		IRODSServerProperties actual = cache.retrieveIRODSServerProperties("notthehost", zone);
		Assert.assertNull(actual);
		}

}
