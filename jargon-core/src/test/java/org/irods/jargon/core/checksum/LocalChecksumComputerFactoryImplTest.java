package org.irods.jargon.core.checksum;

import org.irods.jargon.core.protovalues.ChecksumEncodingEnum;
import org.junit.Test;

import junit.framework.Assert;

public class LocalChecksumComputerFactoryImplTest {

	@Test
	public void testInstanceForMD5() throws Exception {
		LocalChecksumComputerFactory factory = new LocalChecksumComputerFactoryImpl();
		MD5LocalChecksumComputerStrategy actual = (MD5LocalChecksumComputerStrategy) factory
				.instance(ChecksumEncodingEnum.MD5);
		Assert.assertNotNull(actual);
	}

	@Test
	public void testInstanceForSHA256() throws Exception {
		LocalChecksumComputerFactory factory = new LocalChecksumComputerFactoryImpl();
		SHA256LocalChecksumComputerStrategy actual = (SHA256LocalChecksumComputerStrategy) factory
				.instance(ChecksumEncodingEnum.SHA256);
		Assert.assertNotNull(actual);
	}

	@Test(expected = ChecksumMethodUnavailableException.class)
	public void testInstanceForStrong() throws Exception {
		LocalChecksumComputerFactory factory = new LocalChecksumComputerFactoryImpl();
		factory.instance(ChecksumEncodingEnum.STRONG);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testInstanceForNull() throws Exception {
		LocalChecksumComputerFactory factory = new LocalChecksumComputerFactoryImpl();
		factory.instance(null);

	}

}
