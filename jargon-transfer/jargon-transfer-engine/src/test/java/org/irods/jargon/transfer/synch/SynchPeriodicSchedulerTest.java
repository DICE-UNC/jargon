package org.irods.jargon.transfer.synch;

import junit.framework.Assert;

import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.transfer.engine.TransferManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

public class SynchPeriodicSchedulerTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public final void testConstructor() throws Exception {
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		TransferManager transferManager = Mockito.mock(TransferManager.class);
		new SynchPeriodicScheduler(transferManager, irodsAccessObjectFactory);
		Assert.assertTrue(true);
	}

}
