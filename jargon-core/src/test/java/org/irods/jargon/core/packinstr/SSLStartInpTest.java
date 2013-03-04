package org.irods.jargon.core.packinstr;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class SSLStartInpTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public final void testInstance() {
		SSLStartInp sslStartInp = SSLStartInp.instance();
		Assert.assertNotNull("null sslStartInp");
		Assert.assertEquals("wrong API number", SSLStartInp.SSL_START_API_NBR,
				sslStartInp.getApiNumber());
	}

}
