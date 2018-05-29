package org.irods.jargon.core.transfer.encrypt;

import java.util.Properties;

import org.irods.jargon.core.connection.NegotiatedClientServerConfiguration;
import org.irods.jargon.core.connection.PipelineConfiguration;
import org.irods.jargon.core.connection.SettableJargonProperties;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.protovalues.EncryptionAlgorithmEnum;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.utils.RandomUtils;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.junit.Assert;

public class AesCipherEncryptWrapperTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static IRODSFileSystem irodsFileSystem;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		irodsFileSystem = IRODSFileSystem.instance();
	}

	@AfterClass
	public static void tearDown() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@After
	public void cleanUpIrods() throws Exception {
		irodsFileSystem.closeAndEatExceptions();

	}

	@Test
	public void testEncryptAes() throws JargonException {
		/*
		 * Only run if ssl enabled
		 */
		if (!testingPropertiesHelper.isTestSsl(testingProperties)) {
			return;
		}
		byte[] source = RandomUtils.generateRandomBytesOfLength(2048);
		SettableJargonProperties props = (SettableJargonProperties) irodsFileSystem.getJargonProperties();
		props.setEncryptionAlgorithmEnum(EncryptionAlgorithmEnum.AES_256_CBC);
		props.setEncryptionKeySize(256);
		props.setEncryptionNumberHashRounds(65536);
		props.setEncryptionSaltSize(8);
		PipelineConfiguration pipelineConfiguration = PipelineConfiguration.instance(props);
		NegotiatedClientServerConfiguration config = new NegotiatedClientServerConfiguration(true);
		AESKeyGenerator generator = new AESKeyGenerator(pipelineConfiguration, config);
		config.setSecretKey(generator.generateKey());
		AesCipherEncryptWrapper wrapper = new AesCipherEncryptWrapper(pipelineConfiguration, config);

		EncryptionBuffer actual = wrapper.encrypt(source);
		Assert.assertNotNull(actual);
		Assert.assertFalse("no encrypted data", actual.getEncryptedData().length == 0);
		Assert.assertFalse("no iv", actual.getInitializationVector().length == 0);

	}

}
