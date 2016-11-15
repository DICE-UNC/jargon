package org.irods.jargon.core.transfer.encrypt;

import javax.crypto.Cipher;

import junit.framework.Assert;

import org.irods.jargon.core.connection.NegotiatedClientServerConfiguration;
import org.irods.jargon.core.connection.PipelineConfiguration;
import org.irods.jargon.core.connection.SettableJargonProperties;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.protovalues.EncryptionAlgorithmEnum;
import org.junit.Test;

public class EncryptionWrapperFactoryTest {

	@Test
	public void testGetAesFromFactory() throws Exception {
		SettableJargonProperties props = new SettableJargonProperties();
		props.setEncryptionKeySize(256);
		props.setEncryptionAlgorithmEnum(EncryptionAlgorithmEnum.AES_256_CBC);
		PipelineConfiguration pipelineConfiguration = PipelineConfiguration
				.instance(props);
		NegotiatedClientServerConfiguration negotiatedClientServerConfiguration = new NegotiatedClientServerConfiguration(
				true);
		AESKeyGenerator keyGen = new AESKeyGenerator(pipelineConfiguration,
				negotiatedClientServerConfiguration);
		negotiatedClientServerConfiguration.setSecretKey(keyGen.generateKey());
		ParallelCipherWrapper actual = EncryptionWrapperFactory.instance(
				pipelineConfiguration, negotiatedClientServerConfiguration,
				Cipher.ENCRYPT_MODE);
		Assert.assertNotNull(actual);
		Assert.assertTrue(actual instanceof AesCipherEncryptWrapper);
	}

	@Test(expected = JargonRuntimeException.class)
	public void testGetAesFromFactoryNoSslNegotiated() throws Exception {
		SettableJargonProperties props = new SettableJargonProperties();
		props.setEncryptionKeySize(256);
		props.setEncryptionAlgorithmEnum(EncryptionAlgorithmEnum.AES_256_CBC);
		PipelineConfiguration pipelineConfiguration = PipelineConfiguration
				.instance(props);
		NegotiatedClientServerConfiguration negotiatedClientServerConfiguration = new NegotiatedClientServerConfiguration(
				false);
		EncryptionWrapperFactory.instance(pipelineConfiguration,
				negotiatedClientServerConfiguration, Cipher.ENCRYPT_MODE);

	}
}
