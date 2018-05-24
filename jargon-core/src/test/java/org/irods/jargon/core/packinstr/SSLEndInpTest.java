package org.irods.jargon.core.packinstr;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import junit.framework.Assert;

public class SSLEndInpTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public final void testInstance() {
		SSLEndInp sslStartInp = SSLEndInp.instance();
		Assert.assertNotNull("null sslStartInp");
		Assert.assertEquals("wrong API number", SSLEndInp.SSL_END_API_NBR, sslStartInp.getApiNumber());
	}

}
