package org.irods.jargon.core.packinstr;

import junit.framework.Assert;

import org.junit.Test;

public class DataObjInpForQuerySpecCollTest {

	@Test
	public final void testInstanceForQuerySpecColl() throws Exception {
		DataObjInpForQuerySpecColl actual = DataObjInpForQuerySpecColl
				.instance("blah");
		Assert.assertEquals("wrong api number",
				DataObjInpForQuerySpecColl.QUERY_SPEC_COLL_API_NBR,
				actual.getApiNumber());
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testInstanceForQuerySpecCollNullFile() throws Exception {
		DataObjInpForQuerySpecColl.instance(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testInstanceForQuerySpecCollBlankFile() throws Exception {
		DataObjInpForQuerySpecColl.instance("");
	}
}
