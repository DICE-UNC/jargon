package org.irods.jargon.core.connection;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ConnectionProgressStatusTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testInstanceForSend() {
		ConnectionProgressStatus status = ConnectionProgressStatus.instanceForSend(10);
		Assert.assertEquals("byte count not sent", 10, status.getByteCount());
	}

}
