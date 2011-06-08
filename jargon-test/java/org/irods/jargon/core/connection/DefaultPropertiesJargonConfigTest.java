package org.irods.jargon.core.connection;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

/*
 * Rather than bind the test to the props too closely, test if I can access the given props with no errors and we're good
 */
public class DefaultPropertiesJargonConfigTest {

	private static JargonProperties props;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		props = new DefaultPropertiesJargonConfig();
	}

	@Test
	public void testIsUseParallelTransfer() throws Exception {
		props.isUseParallelTransfer();
		Assert.assertTrue(true);
	}

	@Test
	public void testGetMaxParallelThreads() throws Exception {
		props.getMaxParallelThreads();
		Assert.assertTrue(true);
	}

}
