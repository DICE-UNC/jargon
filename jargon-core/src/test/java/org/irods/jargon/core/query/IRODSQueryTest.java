package org.irods.jargon.core.query;

import junit.framework.Assert;

import org.irods.jargon.core.exception.JargonException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class IRODSQueryTest {

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

	@Test(expected = JargonException.class)
	public final void testInstanceNullQuery() throws Exception {
		IRODSGenQuery.instance(null, 0);

	}

	@Test(expected = JargonException.class)
	public final void testInstanceZeroResults() throws Exception {
		IRODSGenQuery.instance("test", 0);
	}

}
