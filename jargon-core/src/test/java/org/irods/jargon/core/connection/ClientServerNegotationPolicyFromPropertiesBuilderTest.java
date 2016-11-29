package org.irods.jargon.core.connection;

import junit.framework.Assert;

import org.irods.jargon.core.connection.ClientServerNegotiationPolicy.SslNegotiationPolicy;
import org.junit.Test;
import org.mockito.Mockito;

public class ClientServerNegotationPolicyFromPropertiesBuilderTest {

	@Test
	public void testClientServerNegotationPolicyFromPropertiesBuilder()
			throws Exception {
		SettableJargonProperties jargonProperties = new SettableJargonProperties();
		jargonProperties.setNegotiationPolicy(SslNegotiationPolicy.CS_NEG_REQ);
		IRODSSession irodsSession = Mockito.mock(IRODSSession.class);
		Mockito.when(irodsSession.getJargonProperties()).thenReturn(
				jargonProperties);
		ClientServerNegotationPolicyFromPropertiesBuilder builder = new ClientServerNegotationPolicyFromPropertiesBuilder(
				irodsSession);
		ClientServerNegotiationPolicy actual = builder
				.buildClientServerNegotiationPolicyFromJargonProperties();
		Assert.assertEquals(SslNegotiationPolicy.CS_NEG_REQ,
				actual.getSslNegotiationPolicy());
	}
}
