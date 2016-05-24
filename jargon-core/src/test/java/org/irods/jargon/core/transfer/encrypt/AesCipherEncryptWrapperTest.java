package org.irods.jargon.core.transfer.encrypt;

import junit.framework.Assert;

import org.irods.jargon.core.connection.NegotiatedClientServerConfiguration;
import org.irods.jargon.core.connection.PipelineConfiguration;
import org.irods.jargon.core.connection.SettableJargonProperties;
import org.irods.jargon.core.exception.EncryptionException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.protovalues.EncryptionAlgorithmEnum;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.utils.RandomUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class AesCipherEncryptWrapperTest {

	private static IRODSFileSystem irodsFileSystem = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		irodsFileSystem = IRODSFileSystem.instance();
		SettableJargonProperties settableJargonProperties = new SettableJargonProperties(
				irodsFileSystem.getJargonProperties());
		irodsFileSystem.getIrodsSession().setJargonProperties(
				settableJargonProperties);

	}

	@AfterClass
	public static void afterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test(expected = EncryptionException.class)
	public void testEncryptAesNoInit() throws JargonException {
		byte[] source = RandomUtils.generateRandomBytesOfLength(2048);
		SettableJargonProperties props = (SettableJargonProperties) irodsFileSystem
				.getJargonProperties();
		props.setEncryptionAlgorithmEnum(EncryptionAlgorithmEnum.AES_256_CBC);
		props.setEncryptionKeySize(128);
		props.setEncryptionNumberHashRounds(65536);
		props.setEncryptionSaltSize(8);
		PipelineConfiguration pipelineConfiguration = PipelineConfiguration
				.instance(props);
		NegotiatedClientServerConfiguration config = new NegotiatedClientServerConfiguration(
				true);
		config.initKey(pipelineConfiguration);

		AesCipherEncryptWrapper wrapper = new AesCipherEncryptWrapper(
				pipelineConfiguration, config);

		wrapper.encrypt(source);

	}

	@Test
	public void testEncryptAes() throws JargonException {
		byte[] source = RandomUtils.generateRandomBytesOfLength(2048);
		SettableJargonProperties props = (SettableJargonProperties) irodsFileSystem
				.getJargonProperties();
		props.setEncryptionAlgorithmEnum(EncryptionAlgorithmEnum.AES_256_CBC);
		props.setEncryptionKeySize(256);
		props.setEncryptionNumberHashRounds(65536);
		props.setEncryptionSaltSize(8);
		PipelineConfiguration pipelineConfiguration = PipelineConfiguration
				.instance(props);
		NegotiatedClientServerConfiguration config = new NegotiatedClientServerConfiguration(
				true);
		config.initKey(pipelineConfiguration);

		AesCipherEncryptWrapper wrapper = new AesCipherEncryptWrapper(
				pipelineConfiguration, config);
		wrapper.init();

		EncryptionBuffer actual = wrapper.encrypt(source);
		Assert.assertNotNull(actual);
		Assert.assertFalse("no encrypted data",
				actual.getEncryptedData().length == 0);
		Assert.assertFalse("no iv",
				actual.getInitializationVector().length == 0);

	}

}
