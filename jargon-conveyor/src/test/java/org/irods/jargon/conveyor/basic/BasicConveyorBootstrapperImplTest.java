package org.irods.jargon.conveyor.basic;

import junit.framework.Assert;

import org.irods.jargon.conveyor.core.ConveyorBootstrapper;
import org.irods.jargon.conveyor.core.ConveyorService;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

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
		ConveyorBootstrapper conveyorBootstrapper = new BasicConveyorBootstrapperImpl(
				conveyorBootstrapConfiguration);
		Assert.assertNotNull("should not happen", conveyorBootstrapper);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBasicConveyorBootstrapperImplNullConfigInConstructor() {
		ConveyorBootstrapConfiguration conveyorBootstrapConfiguration = null;
		ConveyorBootstrapper conveyorBootstrapper = new BasicConveyorBootstrapperImpl(
				conveyorBootstrapConfiguration);
		Assert.assertNotNull("should not happen", conveyorBootstrapper);
	}

	@Test
	public void testBootstrap() throws Exception {
		ConveyorBootstrapConfiguration conveyorBootstrapConfiguration = new ConveyorBootstrapConfiguration();
		ConveyorBootstrapper conveyorBootstrapper = new BasicConveyorBootstrapperImpl(
				conveyorBootstrapConfiguration);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		ConveyorService service = conveyorBootstrapper
				.bootstrap(irodsAccessObjectFactory);
		service.shutdown();
		Assert.assertNotNull("no executor in service after bootstrap",
				service.getConveyorExecutorService());
		Assert.assertNotNull("no gridAccountService after bootstrap",
				service.getGridAccountService());
		Assert.assertNotNull(
				"gridAccountService does not have reference to executor after bootstrap",
				service.getGridAccountService().getConveyorExecutorService());
	}

}
