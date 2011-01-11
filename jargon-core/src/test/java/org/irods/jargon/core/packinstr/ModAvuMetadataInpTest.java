package org.irods.jargon.core.packinstr;

import junit.framework.Assert;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.domain.AvuData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ModAvuMetadataInpTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testInstanceForAddCollectionMetadata() throws Exception {
		AvuData avuData = AvuData.instance("attrib", "value", "unit");
		ModAvuMetadataInp modAvu = ModAvuMetadataInp
				.instanceForAddCollectionMetadata("hello", avuData);
		Assert.assertNotNull("null modAvu returned from initializer", modAvu);
	}

	@Test
	public void testInstanceForDeleteCollectionMetadata() throws Exception {
		AvuData avuData = AvuData.instance("attrib", "value", "unit");
		ModAvuMetadataInp modAvu = ModAvuMetadataInp
				.instanceForDeleteCollectionMetadata("hello", avuData);
		Assert.assertNotNull("null modAvu returned from initializer", modAvu);
		Assert.assertEquals("wrong action",
				ModAvuMetadataInp.ActionType.REMOVE, modAvu.getActionType());
	}

	@Test(expected = JargonException.class)
	public void testInstanceForAddCollectionMetadataNullTarget()
			throws Exception {
		AvuData avuData = AvuData.instance("attrib", "value", "unit");
		ModAvuMetadataInp.instanceForAddCollectionMetadata(null, avuData);
	}

	@Test(expected = JargonException.class)
	public void testInstanceForAddCollectionMetadataBlankTarget()
			throws Exception {
		AvuData avuData = AvuData.instance("attrib", "value", "unit");
		ModAvuMetadataInp.instanceForAddCollectionMetadata("", avuData);
	}

	@Test(expected = JargonException.class)
	public void testInstanceForAddCollectionMetadataNullAvu() throws Exception {
		ModAvuMetadataInp.instanceForAddCollectionMetadata("hello", null);
	}

	@Test
	public void testGetParsedTagsForCollectionAddAvu() throws Exception {
		AvuData avuData = AvuData.instance("attrib", "value", "unit");
		ModAvuMetadataInp modAvu = ModAvuMetadataInp
				.instanceForAddCollectionMetadata("target", avuData);

		StringBuilder sb = new StringBuilder();
		sb.append("<ModAVUMetadataInp_PI><arg0>add</arg0>\n");
		sb.append("<arg1>-c</arg1>\n");
		sb.append("<arg2>target</arg2>\n");
		sb.append("<arg3>attrib</arg3>\n");
		sb.append("<arg4>value</arg4>\n");
		sb.append("<arg5>unit</arg5>\n");
		sb.append("<arg6></arg6>\n");
		sb.append("<arg7></arg7>\n");
		sb.append("<arg8></arg8>\n");
		sb.append("<arg9></arg9>\n");
		sb.append("</ModAVUMetadataInp_PI>\n");

		Assert.assertEquals("packing instruction is malformed", sb.toString(),
				modAvu.getParsedTags());
	}

}
