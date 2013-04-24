package org.irods.jargon.conveyor.core;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ConveyorExecutorServiceImplTest {

	private static ConveyorExecutorService conveyorExecutorService = new ConveyorExecutorServiceImpl();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		conveyorExecutorService.shutdown();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testExecWhenNull() throws Exception {
		conveyorExecutorService.processTransferAndHandleReturn(null, false);
	}

}
