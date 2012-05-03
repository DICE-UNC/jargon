package org.irods.jargon.core.connection;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSServerProperties.IcatEnabled;
import org.junit.Test;

public class IRODSServerPropertiesTest {

	@Test
	public void testIsTheIrodsServerAtLeastAtTheGivenReleaseVersionWhenIsLater() {
		String serverVersion = "rods2.3";
		String queryVersion = "rods2.2.1";
		IRODSServerProperties irodsServerProperties = IRODSServerProperties
				.instance(IcatEnabled.ICAT_ENABLED, 100, serverVersion, "", "");
		boolean isLater = irodsServerProperties
				.isTheIrodsServerAtLeastAtTheGivenReleaseVersion(queryVersion);
		Assert.assertTrue("server should be at version", isLater);
	}

	@Test
	public void testIsTheIrodsServerAtLeastAtTheGivenReleaseVersionWhenIsEquals() {
		String serverVersion = "rods2.3";
		String queryVersion = serverVersion;
		IRODSServerProperties irodsServerProperties = IRODSServerProperties
				.instance(IcatEnabled.ICAT_ENABLED, 100, serverVersion, "", "");
		boolean isLater = irodsServerProperties
				.isTheIrodsServerAtLeastAtTheGivenReleaseVersion(queryVersion);
		Assert.assertTrue("server should be at version", isLater);
	}

	@Test
	public void testIsTheIrodsServerAtLeastAtTheGivenReleaseVersionWhenIsNot() {
		String serverVersion = "rods2.3";
		String queryVersion = "rods2.4.1";
		IRODSServerProperties irodsServerProperties = IRODSServerProperties
				.instance(IcatEnabled.ICAT_ENABLED, 100, serverVersion, "", "");
		boolean isLater = irodsServerProperties
				.isTheIrodsServerAtLeastAtTheGivenReleaseVersion(queryVersion);
		Assert.assertFalse("server should not be at version", isLater);
	}

	@Test
	public void testSupportsTicketsWhenDoes() {
		String serverVersion = "rods3.1";
		IRODSServerProperties irodsServerProperties = IRODSServerProperties
				.instance(IcatEnabled.ICAT_ENABLED, 100, serverVersion, "", "");
		boolean isTickets = irodsServerProperties.isSupportsTickets();
		Assert.assertTrue("server should supoort tickets", isTickets);
	}

	@Test
	public void testSupportsTicketsWhenDoesnt() {
		String serverVersion = "rods2.5";
		IRODSServerProperties irodsServerProperties = IRODSServerProperties
				.instance(IcatEnabled.ICAT_ENABLED, 100, serverVersion, "", "");
		boolean isTickets = irodsServerProperties.isSupportsTickets();
		Assert.assertFalse("server shouldnt supoort tickets", isTickets);
	}

}
