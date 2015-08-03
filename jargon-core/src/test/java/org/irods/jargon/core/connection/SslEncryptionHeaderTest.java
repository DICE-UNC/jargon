package org.irods.jargon.core.connection;

import junit.framework.Assert;

import org.irods.jargon.core.protovalues.EncryptionAlgorithmEnum;
import org.junit.Test;
import org.mockito.Mockito;

public class SslEncryptionHeaderTest {

	@Test
	public void testSslEncryptionHeader() {
		IRODSSession session = Mockito.mock(IRODSSession.class);
		new SslEncryptionHeader(session);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSslEncryptionHeaderNullSession() {
		new SslEncryptionHeader(null);
	}

	@Test
	public void testInstanceBytesForSslParameters() throws Exception {
		IRODSSession session = Mockito.mock(IRODSSession.class);
		JargonProperties jargonProperties = new SettableJargonProperties();
		Mockito.when(session.getJargonProperties())
				.thenReturn(jargonProperties);
		SslEncryptionHeader header = new SslEncryptionHeader(session);
		byte[] actual = header.instanceBytesForSslParameters(
				EncryptionAlgorithmEnum.AES_256_CBC, 123, 456, 789);
		Assert.assertNotNull(actual);
		Assert.assertFalse(actual.length == 0);
	}

}
