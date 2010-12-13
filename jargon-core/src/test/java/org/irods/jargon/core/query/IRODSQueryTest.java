package org.irods.jargon.core.query;

import static org.junit.Assert.*;
import junit.framework.TestCase;

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
		IRODSQuery query = IRODSQuery.instance("test", 1);
		TestCase.assertEquals("test", query.getQueryString());
		TestCase.assertEquals(1, query.getNumberOfResultsDesired());
	}

	@Test(expected = JargonException.class)
	public final void testInstanceNullQuery() throws Exception {
		IRODSQuery query = IRODSQuery.instance(null, 0);

	}

	@Test(expected = JargonException.class)
	public final void testInstanceZeroResults() throws Exception {
		IRODSQuery query = IRODSQuery.instance("test", 0);
	}

}
