package org.irods.jargon.ticket;

import java.net.URL;
import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

public class TicketDistributionServiceImplTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static boolean testTicket = false;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		testTicket = testingPropertiesLoader.isTestRemoteExecStream(testingProperties);

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testGetTicketDistributionServiceNullContext() throws Exception {
		if (!testTicket) {
			return;
		}

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito.mock(IRODSAccessObjectFactory.class);
		TicketServiceFactory ticketServiceFactory = new TicketServiceFactoryImpl(irodsAccessObjectFactory);
		TicketDistributionContext ticketDistributionContext = null;
		ticketServiceFactory.instanceTicketDistributionService(irodsAccount, ticketDistributionContext);

	}

	/**
	 * Get a ticket distribution for a valid ticket and context
	 *
	 * @throws Exception
	 */
	@Test
	public final void testGetTicketDistributionForTicket() throws Exception {
		if (!testTicket) {
			return;
		}

		String host = "localhost";
		int port = 8080;
		boolean ssl = false;
		String context = "/idrop-web/tickets/redeemTicket";

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito.mock(IRODSAccessObjectFactory.class);
		TicketServiceFactory ticketServiceFactory = new TicketServiceFactoryImpl(irodsAccessObjectFactory);
		TicketDistributionContext ticketDistributionContext = new TicketDistributionContext();
		TicketDistributionService ticketDistributionService = ticketServiceFactory
				.instanceTicketDistributionService(irodsAccount, ticketDistributionContext);
		ticketDistributionContext.setContext(context);
		ticketDistributionContext.setHost(host);
		ticketDistributionContext.setPort(port);
		ticketDistributionContext.setSsl(ssl);
		Ticket ticket = new Ticket();
		ticket.setTicketString("xxx");
		ticket.setIrodsAbsolutePath("/yyy");
		TicketDistribution ticketDistribution = ticketDistributionService.getTicketDistributionForTicket(ticket);
		Assert.assertNotNull("null ticket distribution returned", ticketDistribution);
		Assert.assertNotNull("no ticket in ticketDistribution", ticketDistribution.getTicket());
		Assert.assertNotNull("no irods uri in ticketDistribution", ticketDistribution.getIrodsAccessURI());
		Assert.assertNotNull("no ticket URL in ticket distribution", ticketDistribution.getTicketURL());
		URL url = ticketDistribution.getTicketURL();
		Assert.assertEquals("bad url host", host, url.getHost());
		Assert.assertEquals("bad port", port, url.getPort());
		Assert.assertEquals("should be http", "http", url.getProtocol());
		Assert.assertNotNull("no ticket landing URL in ticket distribution",
				ticketDistribution.getTicketURLWithLandingPage());

	}

	/**
	 * Get a ticket distribution for a valid ticket and context where the ticket
	 * abspath has embedded spaces. Make sure URL Encodes correctly
	 *
	 * @throws Exception
	 */
	@Test
	public final void testGetTicketDistributionForTicketWithSpacesInAbsPath() throws Exception {
		if (!testTicket) {
			return;
		}

		String host = "localhost";
		int port = 8080;
		boolean ssl = false;
		String context = "/idrop-web/tickets/redeem Ticket";

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito.mock(IRODSAccessObjectFactory.class);
		TicketServiceFactory ticketServiceFactory = new TicketServiceFactoryImpl(irodsAccessObjectFactory);
		TicketDistributionContext ticketDistributionContext = new TicketDistributionContext();
		TicketDistributionService ticketDistributionService = ticketServiceFactory
				.instanceTicketDistributionService(irodsAccount, ticketDistributionContext);
		ticketDistributionContext.setContext(context);
		ticketDistributionContext.setHost(host);
		ticketDistributionContext.setPort(port);
		ticketDistributionContext.setSsl(ssl);
		Ticket ticket = new Ticket();
		ticket.setTicketString("xxx");
		ticket.setIrodsAbsolutePath("/yyy");
		TicketDistribution ticketDistribution = ticketDistributionService.getTicketDistributionForTicket(ticket);
		Assert.assertNotNull("null ticket distribution returned", ticketDistribution);
		Assert.assertNotNull("no ticket in ticketDistribution", ticketDistribution.getTicket());
		Assert.assertNotNull("no irods uri in ticketDistribution", ticketDistribution.getIrodsAccessURI());
		Assert.assertNotNull("no ticket URL in ticket distribution", ticketDistribution.getTicketURL());
		URL url = ticketDistribution.getTicketURL();
		Assert.assertEquals("bad url host", host, url.getHost());
		Assert.assertEquals("bad port", port, url.getPort());
		Assert.assertEquals("should be http", "http", url.getProtocol());
		Assert.assertNotNull("no ticket landing URL in ticket distribution",
				ticketDistribution.getTicketURLWithLandingPage());

	}

	/**
	 * Get a ticket distribution for a valid ticket and context
	 *
	 * @throws Exception
	 */
	@Test
	public final void testGetTicketDistributionForTicketWithSSL() throws Exception {
		if (!testTicket) {
			return;
		}

		String host = "localhost";
		int port = 8080;
		boolean ssl = true;
		String context = "/idrop-web/tickets/redeemTicket";

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito.mock(IRODSAccessObjectFactory.class);
		TicketServiceFactory ticketServiceFactory = new TicketServiceFactoryImpl(irodsAccessObjectFactory);
		TicketDistributionContext ticketDistributionContext = new TicketDistributionContext();
		TicketDistributionService ticketDistributionService = ticketServiceFactory
				.instanceTicketDistributionService(irodsAccount, ticketDistributionContext);
		ticketDistributionContext.setContext(context);
		ticketDistributionContext.setHost(host);
		ticketDistributionContext.setPort(port);
		ticketDistributionContext.setSsl(ssl);
		Ticket ticket = new Ticket();
		ticket.setTicketString("xxx");
		ticket.setIrodsAbsolutePath("/yyy");
		TicketDistribution ticketDistribution = ticketDistributionService.getTicketDistributionForTicket(ticket);
		Assert.assertNotNull("null ticket distribution returned", ticketDistribution);
		Assert.assertNotNull("no ticket in ticketDistribution", ticketDistribution.getTicket());
		Assert.assertNotNull("no irods uri in ticketDistribution", ticketDistribution.getIrodsAccessURI());
		Assert.assertNotNull("no ticket URL in ticket distribution", ticketDistribution.getTicketURL());
		URL url = ticketDistribution.getTicketURL();
		Assert.assertEquals("bad url host", host, url.getHost());
		Assert.assertEquals("bad port", port, url.getPort());
		Assert.assertEquals("should be https", "https", url.getProtocol());

	}

	/*
	 * Get a ticket distribution for a valid ticket and context where I don't have a
	 * mid-tier host, so no URL generated <<<<<<< HEAD
	 * 
	 */
	@Test
	public final void testGetTicketDistributionForTicketNoHostInContext() throws Exception {
		if (!testTicket) {
			return;
		}

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito.mock(IRODSAccessObjectFactory.class);
		TicketServiceFactory ticketServiceFactory = new TicketServiceFactoryImpl(irodsAccessObjectFactory);
		TicketDistributionContext ticketDistributionContext = new TicketDistributionContext();
		TicketDistributionService ticketDistributionService = ticketServiceFactory
				.instanceTicketDistributionService(irodsAccount, ticketDistributionContext);
		Ticket ticket = new Ticket();
		ticket.setTicketString("xxx");
		ticket.setIrodsAbsolutePath("yyy");
		TicketDistribution ticketDistribution = ticketDistributionService.getTicketDistributionForTicket(ticket);
		Assert.assertNotNull("null ticket distribution returned", ticketDistribution);
		Assert.assertNotNull("no ticket in ticketDistribution", ticketDistribution.getTicket());
		Assert.assertNotNull("no irods uri in ticketDistribution", ticketDistribution.getIrodsAccessURI());
		Assert.assertNull("no host, so should be no ticket URL in ticket distribution",
				ticketDistribution.getTicketURL());

	}

	/*
	 * Get a ticket distribution for a ticket that appears not to be initialized
	 *
	 * @throws Exception
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testGetTicketDistributionForTicketThatsMissingTicketString() throws Exception {
		if (!testTicket) {
			return;
		}

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito.mock(IRODSAccessObjectFactory.class);
		TicketServiceFactory ticketServiceFactory = new TicketServiceFactoryImpl(irodsAccessObjectFactory);
		TicketDistributionContext ticketDistributionContext = new TicketDistributionContext();

		TicketDistributionService ticketDistributionService = ticketServiceFactory
				.instanceTicketDistributionService(irodsAccount, ticketDistributionContext);
		Ticket ticket = new Ticket();
		ticketDistributionService.getTicketDistributionForTicket(ticket);

	}

	/*
	 * Get a ticket distribution for a ticket that appears not to be initialized
	 * with an irods path
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testGetTicketDistributionForTicketThatsMissingIRODSPath() throws Exception {
		if (!testTicket) {
			return;
		}

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito.mock(IRODSAccessObjectFactory.class);
		TicketServiceFactory ticketServiceFactory = new TicketServiceFactoryImpl(irodsAccessObjectFactory);
		TicketDistributionContext ticketDistributionContext = new TicketDistributionContext();
		TicketDistributionService ticketDistributionService = ticketServiceFactory
				.instanceTicketDistributionService(irodsAccount, ticketDistributionContext);
		Ticket ticket = new Ticket();
		ticket.setTicketString("xxx");
		ticketDistributionService.getTicketDistributionForTicket(ticket);

	}

}
