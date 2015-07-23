package org.irods.jargon.core.packinstr;

import junit.framework.Assert;

import org.irods.jargon.core.connection.ClientServerNegotiationPolicy.SslNegotiationPolicy;
import org.irods.jargon.core.exception.ClientServerNegotiationException;
import org.junit.Test;

public class ClientServerNegotiationStructTest {

	@Test
	public void testInstanceFromTagDontCare() throws Exception {
		Tag tag = new Tag(ClientServerNegotiationStruct.NEG_PI);
		tag.addTag("status", 1);
		tag.addTag("result", SslNegotiationPolicy.CS_NEG_DONT_CARE.name());
		ClientServerNegotiationStruct struct = ClientServerNegotiationStruct
				.instanceFromTag(tag);
		Assert.assertNotNull(struct);
		Assert.assertEquals(1, struct.getStatus());
		Assert.assertEquals(SslNegotiationPolicy.CS_NEG_DONT_CARE,
				struct.getSslNegotiationPolicy());
		Assert.assertTrue(struct.wasThisASuccess());

	}

	@Test
	public void testInstanceFromTagDontCareNotSuccess() throws Exception {
		Tag tag = new Tag(ClientServerNegotiationStruct.NEG_PI);
		tag.addTag("status", -1);
		tag.addTag("result", SslNegotiationPolicy.CS_NEG_DONT_CARE.name());
		ClientServerNegotiationStruct struct = ClientServerNegotiationStruct
				.instanceFromTag(tag);
		Assert.assertFalse(struct.wasThisASuccess());

	}

	@Test
	public void testInstanceFromTagRefuse() throws Exception {
		Tag tag = new Tag(ClientServerNegotiationStruct.NEG_PI);
		tag.addTag("status", 1);
		tag.addTag("result", SslNegotiationPolicy.CS_NEG_REFUSE.name());
		ClientServerNegotiationStruct struct = ClientServerNegotiationStruct
				.instanceFromTag(tag);
		Assert.assertNotNull(struct);
		Assert.assertEquals(1, struct.getStatus());
		Assert.assertEquals(SslNegotiationPolicy.CS_NEG_REFUSE,
				struct.getSslNegotiationPolicy());

	}

	@Test
	public void testInstanceFromTagReq() throws Exception {
		Tag tag = new Tag(ClientServerNegotiationStruct.NEG_PI);
		tag.addTag("status", 1);
		tag.addTag("result", SslNegotiationPolicy.CS_NEG_REQ.name());
		ClientServerNegotiationStruct struct = ClientServerNegotiationStruct
				.instanceFromTag(tag);
		Assert.assertNotNull(struct);
		Assert.assertEquals(1, struct.getStatus());
		Assert.assertEquals(SslNegotiationPolicy.CS_NEG_REQ,
				struct.getSslNegotiationPolicy());

	}

	@Test(expected = ClientServerNegotiationException.class)
	public void testInstanceFromTagInvalidResult() throws Exception {
		Tag tag = new Tag(ClientServerNegotiationStruct.NEG_PI);
		tag.addTag("status", 1);
		tag.addTag("result", "nonsensical");
		ClientServerNegotiationStruct.instanceFromTag(tag);

	}

}
