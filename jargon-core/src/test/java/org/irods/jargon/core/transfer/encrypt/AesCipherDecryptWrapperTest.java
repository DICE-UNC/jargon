package org.irods.jargon.core.transfer.encrypt;

import java.nio.charset.StandardCharsets;

import org.irods.jargon.core.connection.NegotiatedClientServerConfiguration;
import org.irods.jargon.core.connection.PipelineConfiguration;
import org.irods.jargon.core.connection.SettableJargonProperties;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.protovalues.EncryptionAlgorithmEnum;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class AesCipherDecryptWrapperTest {

	private static IRODSFileSystem irodsFileSystem = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		irodsFileSystem = IRODSFileSystem.instance();
		SettableJargonProperties settableJargonProperties = new SettableJargonProperties(
				irodsFileSystem.getJargonProperties());
		irodsFileSystem.getIrodsSession().setJargonProperties(settableJargonProperties);

	}

	@AfterClass
	public static void afterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public void testEncryptRoundTrip() throws JargonException {
		String begin = "aj;kj;ljlkjfjkdjfiaewjafasdf";
		byte[] source = begin.getBytes(StandardCharsets.UTF_8);

		SettableJargonProperties props = (SettableJargonProperties) irodsFileSystem.getJargonProperties();
		props.setEncryptionAlgorithmEnum(EncryptionAlgorithmEnum.AES_256_CBC);
		props.setEncryptionKeySize(EncryptionAlgorithmEnum.AES_256_CBC.getKeySize());
		props.setEncryptionNumberHashRounds(8);
		props.setEncryptionSaltSize(8);
		PipelineConfiguration pipelineConfiguration = PipelineConfiguration.instance(props);
		NegotiatedClientServerConfiguration config = new NegotiatedClientServerConfiguration(true);
		AESKeyGenerator generator = new AESKeyGenerator(pipelineConfiguration, config);
		config.setSecretKey(generator.generateKey());

		AesCipherEncryptWrapper wrapper = new AesCipherEncryptWrapper(pipelineConfiguration, config);
		EncryptionBuffer encrypted = wrapper.encrypt(source);

		// now decrypt

		AesCipherDecryptWrapper decryptWrapper = new AesCipherDecryptWrapper(pipelineConfiguration, config);

		byte[] decrypted = decryptWrapper.decrypt(encrypted);
		String result = new String(decrypted, StandardCharsets.UTF_8);
		Assert.assertEquals("didnt match encrypted data", begin, result);

	}
}
