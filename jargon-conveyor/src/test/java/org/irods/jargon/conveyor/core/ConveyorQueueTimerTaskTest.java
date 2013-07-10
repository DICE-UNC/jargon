package org.irods.jargon.conveyor.core;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

public class ConveyorQueueTimerTaskTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testInit() throws Exception {
		ConveyorService conveyorService = Mockito.mock(ConveyorService.class);
		QueueManagerService queueManager = Mockito
				.mock(QueueManagerService.class);
		conveyorService.setQueueManagerService(queueManager);
		Mockito.when(conveyorService.getQueueManagerService()).thenReturn(
				queueManager);

		ConveyorQueueTimerTask timer = new ConveyorQueueTimerTask();
		timer.setConveyorService(conveyorService);
		timer.init();
		timer.run();
		Mockito.verify(queueManager).dequeueNextOperation();

	}

	@Test
	public void testRunWhenPaused() throws Exception {
		ConveyorService conveyorService = Mockito.mock(ConveyorService.class);
		QueueManagerService queueManager = Mockito
				.mock(QueueManagerService.class);
		conveyorService.setQueueManagerService(queueManager);
		Mockito.when(conveyorService.getQueueManagerService()).thenReturn(
				queueManager);

		ConveyorQueueTimerTask timer = new ConveyorQueueTimerTask();
		timer.setConveyorService(conveyorService);
		timer.init();
		timer.setPaused(true);
		timer.run();
		Mockito.verify(queueManager, Mockito.never()).dequeueNextOperation();

	}

	@Test(expected = ConveyorRuntimeException.class)
	public void testInitNoConveyor() throws Exception {
		ConveyorQueueTimerTask timer = new ConveyorQueueTimerTask();
		timer.init();

	}

	@Test(expected = ConveyorRuntimeException.class)
	public void testRunWithoutInit() throws Exception {
		ConveyorService conveyorService = Mockito.mock(ConveyorService.class);
		QueueManagerService queueManager = Mockito
				.mock(QueueManagerService.class);
		conveyorService.setQueueManagerService(queueManager);
		Mockito.when(conveyorService.getQueueManagerService()).thenReturn(
				queueManager);

		ConveyorQueueTimerTask timer = new ConveyorQueueTimerTask();
		timer.setConveyorService(conveyorService);
		timer.run();

	}

}
