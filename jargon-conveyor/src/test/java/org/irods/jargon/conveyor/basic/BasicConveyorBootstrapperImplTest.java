package org.irods.jargon.conveyor.basic;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.irods.jargon.conveyor.core.ConveyorBootstrapper;
import org.irods.jargon.conveyor.core.ConveyorService;
import org.irods.jargon.conveyor.core.GridAccountService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class BasicConveyorBootstrapperImplTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testBasicConveyorBootstrapperImpl() {
		ConveyorBootstrapConfiguration conveyorBootstrapConfiguration = new ConveyorBootstrapConfiguration();
		ConveyorBootstrapper conveyorBootstrapper = new BasicConveyorBootstrapperImpl(conveyorBootstrapConfiguration);
		Assert.assertNotNull("should not happen", conveyorBootstrapper);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testBasicConveyorBootstrapperImplNullConfigInConstructor() {
		ConveyorBootstrapConfiguration conveyorBootstrapConfiguration = null;
		ConveyorBootstrapper conveyorBootstrapper = new BasicConveyorBootstrapperImpl(conveyorBootstrapConfiguration);
		Assert.assertNotNull("should not happen", conveyorBootstrapper);
	}

	@Test
	public void testBootstrap() throws Exception {
		ConveyorBootstrapConfiguration conveyorBootstrapConfiguration = new ConveyorBootstrapConfiguration();
		ConveyorBootstrapper conveyorBootstrapper = new BasicConveyorBootstrapperImpl(conveyorBootstrapConfiguration);
		ConveyorService service = conveyorBootstrapper.bootstrap();
		service.shutdown();
		TestCase.assertNotNull("no executor in service after bootstrap", service.getConveyorExecutorService());
		TestCase.assertNotNull("no gridAccountService after bootstrap", service.getGridAccountService());
		TestCase.assertNotNull("gridAccountService does not have reference to executor after bootstrap", ((GridAccountService) service.getGridAccountService()).getConveyorExecutorService());
	}

}
