package org.irods.jargon.ticket;

import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

public class TicektServiceFactoryImplTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static IRODSAccount irodsAccount;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
	}

	@Test
	public final void testTicketServiceFactoryImpl() {
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito.mock(IRODSAccessObjectFactory.class);
		new TicketServiceFactoryImpl(irodsAccessObjectFactory);
		// non errror is a pass
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testTicketServiceFactoryImplNullFactory() {
		new TicketServiceFactoryImpl(null);
	}

	@Test
	public final void testInstanceTicketAdminService() throws Exception {
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito.mock(IRODSAccessObjectFactory.class);
		TicketServiceFactory factory = new TicketServiceFactoryImpl(irodsAccessObjectFactory);
		TicketAdminService adminService = factory.instanceTicketAdminService(irodsAccount);
		Assert.assertNotNull("null admin service", adminService);

	}

	@Test
	public final void testInstanceTicketClientOperations() throws Exception {
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito.mock(IRODSAccessObjectFactory.class);
		TicketServiceFactory factory = new TicketServiceFactoryImpl(irodsAccessObjectFactory);
		TicketClientOperations clientOperations = factory.instanceTicketClientOperations(irodsAccount);
		Assert.assertNotNull("null service", clientOperations);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testInstanceTicketAdminServiceNullAccount() throws Exception {
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito.mock(IRODSAccessObjectFactory.class);
		TicketServiceFactory factory = new TicketServiceFactoryImpl(irodsAccessObjectFactory);
		factory.instanceTicketAdminService(null);

	}

	@Test(expected = IllegalArgumentException.class)
	public final void testInstanceTicketClientOperationsNullAccount() throws Exception {
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito.mock(IRODSAccessObjectFactory.class);
		TicketServiceFactory factory = new TicketServiceFactoryImpl(irodsAccessObjectFactory);
		factory.instanceTicketClientOperations(null);
	}

}
