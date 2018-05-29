package org.irods.jargon.core.connection;

import org.irods.jargon.core.connection.ClientServerNegotiationPolicy.SslNegotiationPolicy;
import org.junit.Assert;
import org.junit.Test;

public class ClientServerNegotiationPolicyTest {

	@Test
	public void testBuildStartupOptionsForNegotiation() {
		ClientServerNegotiationPolicy policy = new ClientServerNegotiationPolicy();
		policy.setSslNegotiationPolicy(SslNegotiationPolicy.CS_NEG_DONT_CARE);
		String actual = policy.buildStartupOptionsForNegotiation();
		Assert.assertEquals(ClientServerNegotiationPolicy.REQUEST_NEGOTIATION_STARTUP_OPTION, actual);
	}

	@Test
	public void testBuildStartupOptionsForNegotiationNoPolicy() {
		ClientServerNegotiationPolicy policy = new ClientServerNegotiationPolicy();
		policy.setSslNegotiationPolicy(SslNegotiationPolicy.NO_NEGOTIATION);
		String actual = policy.buildStartupOptionsForNegotiation();
		Assert.assertEquals("", actual);
	}

}
