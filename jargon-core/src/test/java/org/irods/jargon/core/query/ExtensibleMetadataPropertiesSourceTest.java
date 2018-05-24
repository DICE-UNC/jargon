package org.irods.jargon.core.query;

import org.irods.jargon.core.exception.JargonException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import junit.framework.Assert;

public class ExtensibleMetadataPropertiesSourceTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testCreateInstance() throws Exception {
		ExtensibleMetaDataSource source = new ExtensibleMetadataPropertiesSource();
		Assert.assertNotNull("null extensible metadata source, some creation problem occurred", source);
	}

	@Test
	public void testCreateInstanceFromProperties() throws Exception {
		ExtensibleMetaDataSource source = new ExtensibleMetadataPropertiesSource();
		ExtensibleMetaDataMapping mapping = source.generateExtensibleMetaDataMapping();
		Assert.assertNotNull(mapping);
	}

	@Test(expected = JargonException.class)
	public void testCreateInstanceFromNullProperties() throws Exception {
		new ExtensibleMetadataPropertiesSource(null);

	}

	@Test(expected = JargonException.class)
	public void testCreateInstanceFromBlankProperties() throws Exception {
		new ExtensibleMetadataPropertiesSource("");

	}

	@Test(expected = JargonException.class)
	public void testCreateInstanceFromMissingTestProperties() throws Exception {
		ExtensibleMetaDataSource source = new ExtensibleMetadataPropertiesSource("i_dont_exist.properties");
		source.generateExtensibleMetaDataMapping();
	}

	@Test
	public void testCreateInstanceFromTestPropertiesAndVerifyMapping() throws Exception {
		ExtensibleMetaDataSource source = new ExtensibleMetadataPropertiesSource("test_extended_icat_data.properties");
		ExtensibleMetaDataMapping mapping = source.generateExtensibleMetaDataMapping();
		String colName1 = mapping.getColumnNameFromIndex("10001");
		Assert.assertEquals("COL_TEST_ID", colName1);
	}

}
