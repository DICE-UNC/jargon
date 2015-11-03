package org.irods.jargon.core.transfer;

import junit.framework.Assert;

import org.irods.jargon.core.connection.NegotiatedClientServerConfiguration;
import org.irods.jargon.core.connection.PipelineConfiguration;
import org.irods.jargon.core.connection.SettableJargonProperties;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.protovalues.EncryptionAlgorithmEnum;
import org.irods.jargon.core.transfer.ParallelEncryptionCipherWrapper.Mode;
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
		negotiatedClientServerConfiguration.initKey(pipelineConfiguration);
		ParallelEncryptionCipherWrapper actual = EncryptionWrapperFactory
				.instance(pipelineConfiguration,
						negotiatedClientServerConfiguration, Mode.ENCRYPT);
		Assert.assertNotNull(actual);
		Assert.assertTrue(actual instanceof AesCipherWrapper);
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
		negotiatedClientServerConfiguration.initKey(pipelineConfiguration);
		EncryptionWrapperFactory.instance(pipelineConfiguration,
				negotiatedClientServerConfiguration, Mode.ENCRYPT);

	}
}
