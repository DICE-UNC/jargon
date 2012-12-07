package org.irods.jargon.conveyor.core;

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
		conveyorExecutorService.executeConveyorCallable(null);
		
	}
	
	@Test
	public void testMockCallable() throws Exception {
		AbstractConveyorCallable callable = Mockito.mock(AbstractConveyorCallable.class);
		ConveyorExecutionFuture future = new ConveyorExecutionFuture();
		Mockito.when(callable.call()).thenReturn(future);
		ConveyorExecutionFuture actual = conveyorExecutorService.executeConveyorCallable(callable);
		Assert.assertNotNull("did not get future back", actual);
	}
	
	@Test(expected=ConveyorExecutionException.class)
	public void testMockCallableThrowsException() throws Exception {
		AbstractConveyorCallable callable = Mockito.mock(AbstractConveyorCallable.class);
		Mockito.when(callable.call()).thenThrow(new Exception("exception"));
		conveyorExecutorService.executeConveyorCallable(callable);
		
	}
	
	

}
