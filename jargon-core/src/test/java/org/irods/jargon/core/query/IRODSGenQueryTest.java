package org.irods.jargon.core.query;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import junit.framework.Assert;

public class IRODSGenQueryTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public final void testInstance() throws Exception {
		IRODSGenQuery query = IRODSGenQuery.instance("test", 1);
		Assert.assertEquals("test", query.getQueryString());
		Assert.assertEquals(1, query.getNumberOfResultsDesired());
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testInstanceNullQuery() throws Exception {
		IRODSGenQuery.instance(null, 0);

	}

	@Test(expected = IllegalArgumentException.class)
	public final void testInstanceZeroResults() throws Exception {
		IRODSGenQuery.instance("test", 0);
	}

}
