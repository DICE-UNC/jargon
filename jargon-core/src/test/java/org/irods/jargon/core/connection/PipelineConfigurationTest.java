package org.irods.jargon.core.connection;

import org.irods.jargon.core.protovalues.EncryptionAlgorithmEnum;
import org.junit.Test;

import junit.framework.Assert;

public class PipelineConfigurationTest {

	@Test
	public void testInstance() throws Exception {
		SettableJargonProperties jargonProperties = new SettableJargonProperties();
		jargonProperties.setEncryptionAlgorithmEnum(EncryptionAlgorithmEnum.AES_256_CBC);
		jargonProperties.setEncryptionKeySize(123);
		jargonProperties.setEncryptionNumberHashRounds(456);
		jargonProperties.setEncryptionSaltSize(789);

		PipelineConfiguration actual = PipelineConfiguration.instance(jargonProperties);
		Assert.assertEquals(jargonProperties.getEncryptionAlgorithmEnum(), actual.getEncryptionAlgorithmEnum());
		Assert.assertEquals(jargonProperties.getEncryptionKeySize(), actual.getEncryptionKeySize());
		Assert.assertEquals(jargonProperties.getEncryptionNumberHashRounds(), actual.getEncryptionNumberHashRounds());
		Assert.assertEquals(jargonProperties.getEncryptionSaltSize(), actual.getEncryptionSaltSize());

	}

}
