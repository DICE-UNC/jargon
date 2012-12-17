package org.irods.jargon.conveyor.core;

import java.util.Properties;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

public class ConveyorExecutorServiceImplTest {
	
	private static ConveyorExecutorService conveyorExecutorService = new ConveyorExecutorServiceImpl();
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	
	}
	
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		conveyorExecutorService.shutdown();
	}

	
	@Test(expected=IllegalArgumentException.class)
	public void testExecWhenNull() throws Exception {
		conveyorExecutorService.executeConveyorCallable(null, false);
	}
	
	@Test
	public void testMockCallable() throws Exception {
		Properties conveyorProperties = new Properties();
		conveyorProperties.setProperty(ConveyorExecutorService.TRY_LOCK_TIMEOUT, "1");
		ConveyorExecutorService testService = new ConveyorExecutorServiceImpl();
		testService.setExecutorServiceProperties(conveyorProperties);
		AbstractConveyorCallable callable = Mockito.mock(AbstractConveyorCallable.class);
		ConveyorExecutionFuture future = new ConveyorExecutionFuture();
		Mockito.when(callable.call()).thenReturn(future);
		ConveyorExecutionFuture actual = testService.executeConveyorCallable(callable, false);
		Assert.assertNotNull("did not get future back", actual);
	}
	
	@Test(expected=ConveyorExecutionException.class)
	public void testMockCallableThrowsException() throws Exception {
		AbstractConveyorCallable callable = Mockito.mock(AbstractConveyorCallable.class);
		Mockito.when(callable.call()).thenThrow(new Exception("exception"));
		Properties conveyorProperties = new Properties();
		conveyorProperties.setProperty(ConveyorExecutorService.TRY_LOCK_TIMEOUT, "1");
		conveyorExecutorService.executeConveyorCallable(callable, true);
	}
	
	@Test(expected=ConveyorExecutionException.class)
	public void testMockCallableThrowsExceptionNullProperties() throws Exception {
		AbstractConveyorCallable callable = Mockito.mock(AbstractConveyorCallable.class);
		ConveyorExecutorService testService = new ConveyorExecutorServiceImpl();
		testService.executeConveyorCallable(callable, true);
	}
	
	/**
	 * Test a valid lock/unlock sequence
	 * @throws Exception
	 */
	@Test
	public void testLockQueue() throws Exception {
		ConveyorExecutorService testService = new ConveyorExecutorServiceImpl();
		testService.lockQueue();
		testService.unlockQueue();
		testService.lockQueue();
		testService.unlockQueue();
	}
	
	@Test(expected=ConveyorExecutionTimeoutException.class)
	public void testExecuteConveyorCallableWhenTimeoutExpected() throws Exception {
		Properties conveyorProperties = new Properties();
		conveyorProperties.setProperty(ConveyorExecutorService.TRY_LOCK_TIMEOUT, "1");
		ConveyorExecutorService testService = new ConveyorExecutorServiceImpl();
		testService.setExecutorServiceProperties(conveyorProperties);
		testService.lockQueue();

		// queue locked, now try to run transfer, which should time out
		AbstractConveyorCallable callable = Mockito.mock(AbstractConveyorCallable.class);
		testService.executeConveyorCallable(callable, true);
		
	}
	
	@Test(expected=ConveyorExecutionException.class)
	public void testExecuteConveyorCallableWhenPropsPresentButTimeoutMissing() throws Exception {
		Properties conveyorProperties = new Properties();
		ConveyorExecutorService testService = new ConveyorExecutorServiceImpl();
		testService.setExecutorServiceProperties(conveyorProperties);
		testService.lockQueue();

		// queue locked, now try to run transfer, which should time out
		AbstractConveyorCallable callable = Mockito.mock(AbstractConveyorCallable.class);
		testService.executeConveyorCallable(callable, true);
		
	}


}
