/**
 *
 */
package org.irods.jargon.core.packinstr;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class AbstractIRODSPackingInstructionTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.packinstr.AbstractIRODSPackingInstruction#createKeyValueTag(java.util.List)}
	 * .
	 */
	@Test
	public final void testCreateKeyValueTag() throws Exception {
		// use DataObjInp to wrap class
		DataObjInp dataObjInp = DataObjInp.instance("/abspath", DataObjInp.DEFAULT_CREATE_MODE,
				DataObjInp.OpenFlags.READ, 0L, 0L, "testResource", null);

		// create a 2x2 key value pair and inspect the tag
		List<KeyValuePair> kvps = new ArrayList<KeyValuePair>();
		kvps.add(KeyValuePair.instance("testkey", "testvalue"));
		Tag tag = dataObjInp.createKeyValueTag(kvps);
		String expectedValue = "<KeyValPair_PI><ssLen>1</ssLen>\n<keyWord>testkey</keyWord>\n<svalue>testvalue</svalue>\n</KeyValPair_PI>\n";
		Assert.assertEquals("unexpected tag generated for kvp", expectedValue, tag.parseTag());
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.packinstr.AbstractIRODSPackingInstruction#createKeyValueTag(java.util.List)}
	 * .
	 */
	@Test
	public final void testCreateKeyValueTag2Kvps() throws Exception {
		// use DataObjInp to wrap class
		DataObjInp dataObjInp = DataObjInp.instance("/abspath", DataObjInp.DEFAULT_CREATE_MODE,
				DataObjInp.OpenFlags.READ, 0L, 0L, "testResource", null);
		// create a 2x2 key value pair and inspect the tag
		List<KeyValuePair> kvps = new ArrayList<KeyValuePair>();
		kvps.add(KeyValuePair.instance("testkey", "testvalue"));
		kvps.add(KeyValuePair.instance("testkey2", "testvalue2"));
		Tag tag = dataObjInp.createKeyValueTag(kvps);
		String expectedValue = "<KeyValPair_PI><ssLen>2</ssLen>\n<keyWord>testkey</keyWord>\n<keyWord>testkey2</keyWord>\n<svalue>testvalue</svalue>\n<svalue>testvalue2</svalue>\n</KeyValPair_PI>\n";
		Assert.assertEquals("unexpected tag generated for kvp", expectedValue, tag.parseTag());
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.packinstr.AbstractIRODSPackingInstruction#createKeyValueTag(java.util.List)}
	 * .
	 */
	@Test
	public final void testCreateKeyValueTagNoKvps() throws Exception {
		// use DataObjInp to wrap class
		DataObjInp dataObjInp = DataObjInp.instance("/abspath", DataObjInp.DEFAULT_CREATE_MODE,
				DataObjInp.OpenFlags.READ, 0L, 0L, "testResource", null);
		// create a 2x2 key value pair and inspect the tag
		List<KeyValuePair> kvps = new ArrayList<KeyValuePair>();
		Tag tag = dataObjInp.createKeyValueTag(kvps);
		String expectedValue = "<KeyValPair_PI><ssLen>0</ssLen>\n</KeyValPair_PI>\n";
		Assert.assertEquals("unexpected tag generated for kvp", expectedValue, tag.parseTag());
	}
}
