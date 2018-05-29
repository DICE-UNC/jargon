package org.irods.jargon.core.query;

import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ExtensibleMetaDataMappingTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public final void testInstance() throws Exception {
		Map<String, String> metaDataSource = new HashMap<String, String>();
		String key1 = "key1";
		String val1 = "10003";
		String key2 = "key2";
		String val2 = "10004";
		metaDataSource.put(key1, val1);
		metaDataSource.put(key2, val2);

		ExtensibleMetaDataMapping metaDataMapping = ExtensibleMetaDataMapping.instance(metaDataSource);

		Assert.assertNotNull(metaDataMapping);

	}

	@Test
	public final void testGetIndexFromColumnName() throws Exception {
		Map<String, String> metaDataSource = new HashMap<String, String>();
		String key1 = "key1";
		String val1 = "10003";
		String key2 = "key2";
		String val2 = "10004";
		metaDataSource.put(key1, val1);
		metaDataSource.put(key2, val2);

		ExtensibleMetaDataMapping metaDataMapping = ExtensibleMetaDataMapping.instance(metaDataSource);

		String index = metaDataMapping.getIndexFromColumnName(key1);
		Assert.assertEquals(val1, index);
	}

	@Test
	public final void testGetColumnNameFromIndex() throws Exception {
		Map<String, String> metaDataSource = new HashMap<String, String>();
		String key1 = "key1";
		String val1 = "10003";
		String key2 = "key2";
		String val2 = "10004";
		metaDataSource.put(key1, val1);
		metaDataSource.put(key2, val2);

		ExtensibleMetaDataMapping metaDataMapping = ExtensibleMetaDataMapping.instance(metaDataSource);

		String actual = metaDataMapping.getColumnNameFromIndex(val1);
		Assert.assertEquals(key1, actual);

	}

	@Test
	public final void testCreateThenObtainInstanceAgainCheckOverride() throws Exception {
		Map<String, String> metaDataSource = new HashMap<String, String>();
		String key1 = "key1";
		String val1 = "10003";
		String key2 = "key2";
		String val2 = "10004";
		metaDataSource.put(key1, val1);
		metaDataSource.put(key2, val2);

		ExtensibleMetaDataMapping metaDataMapping = ExtensibleMetaDataMapping.instance(metaDataSource);
		metaDataSource = new HashMap<String, String>();
		String key3 = "key3";
		String val3 = "10004";
		String key4 = "key4";
		String val4 = "10005";
		metaDataSource.put(key3, val3);
		metaDataSource.put(key4, val4);
		metaDataMapping = ExtensibleMetaDataMapping.instance(metaDataSource);
		String actual = metaDataMapping.getColumnNameFromIndex(val3);
		Assert.assertEquals("stale data still in metadata mapping, should have overridden", key3, actual);
	}

}
