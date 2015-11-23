package org.irods.jargon.core.packinstr;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class DataObjInpForUnregisterTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * Can i properly create w/o error
	 *
	 * @throws Exception
	 */
	@Test
	public final void testInstanceForDelete() throws Exception {
		DataObjInpForUnregister.instanceForDelete("path", false);
	}

	/**
	 * null path
	 *
	 * @throws Exception
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testInstanceForDeleteNullPath() throws Exception {
		DataObjInpForUnregister.instanceForDelete(null, false);
	}

	/**
	 * blank path
	 *
	 * @throws Exception
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testInstanceForDeleteBlankPath() throws Exception {
		DataObjInpForUnregister.instanceForDelete("", false);
	}

}
