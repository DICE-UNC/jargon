package org.irods.jargon.core.packinstr;

import org.junit.Assert;
import org.junit.Test;

public class ModDataObjMetaInpTest {

	@Test
	public void testExpiryDateMod() throws Exception {
		ModDataObjMetaInp meta = ModDataObjMetaInp.instanceForModExpDate("foo", "11-21-1962");
		String tagString = meta.getParsedTags();
		Assert.assertNotNull("null tag string", tagString);
		Assert.assertTrue("did not find objPath", tagString.contains("<objPath>foo</objPath>"));
		Assert.assertTrue("did not find expDate", tagString.contains("11-21-1962"));
	}

}
