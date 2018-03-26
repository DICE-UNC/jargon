package org.irods.jargon.core.packinstr;

import org.junit.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TransferOptionsTest {
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public final void testCopyConstructorDoChecksum() throws Exception {
		TransferOptions expected = new TransferOptions();
		expected.setComputeChecksumAfterTransfer(true);
		TransferOptions actual = new TransferOptions(expected);
		Assert.assertEquals("did not properly set checksum option",
				expected.isComputeChecksumAfterTransfer(),
				actual.isComputeChecksumAfterTransfer());
	}

	@Test
	public final void testCopyConstructorVerifyChecksum() throws Exception {
		TransferOptions expected = new TransferOptions();
		expected.setComputeAndVerifyChecksumAfterTransfer(true);
		TransferOptions actual = new TransferOptions(expected);
		Assert.assertEquals("did not properly set verify checksum option",
				expected.isComputeAndVerifyChecksumAfterTransfer(),
				actual.isComputeAndVerifyChecksumAfterTransfer());
	}

	@Test
	public final void testCopyConstructorIntraFileStatusCallbacks()
			throws Exception {
		TransferOptions expected = new TransferOptions();
		expected.setIntraFileStatusCallbacks(true);
		TransferOptions actual = new TransferOptions(expected);
		Assert.assertEquals("did not properly set intraFileStatusCallbacks",
				expected.isIntraFileStatusCallbacks(),
				actual.isIntraFileStatusCallbacks());
	}

}
